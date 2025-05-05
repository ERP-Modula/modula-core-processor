package com.modula.coreprocessor.service.workflow.execution;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import com.modula.common.domain.workflow.execution.OutputInterfaceField;
import com.modula.common.domain.workflow.execution.WorkflowInstance;
import com.modula.common.domain.workflow.execution.events.ExecutorTask;
import com.modula.common.domain.workflow.step.Step;
import com.modula.coreprocessor.service.StepService;
import com.modula.coreprocessor.service.WorkflowInstanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WorkflowExecutor {

    private final WorkflowInstanceService workflowInstanceService;
    private final StepService stepService;
    private final KafkaExecutorProducerService kafkaExecutorProducerService;

    /**
     * сейчас выполнение находится после!! currStepId и до!! nextSteps
     * сервис должен добавить результат исполнения в контекст и отправить на исполнение следующие шаги
     *
     * @param task результат исполнения currStep
     */

    @Transactional
    public void handleTask(ExecutorTask task) {
        WorkflowInstance workflowInstance = workflowInstanceService.getInstance(task.getWorkflowInstanceId());

        // добавляем результаты с прошлого шага
        addIntegrationOutputResultToContext(workflowInstance, task.getIntegrationOutput());

        List<UUID> nextSteps = new ArrayList<>();

        if (task.getIsFirstStep()) {
            nextSteps.add(workflowInstance.getFirstStepId());
        } else {
            Step currStep = stepService.getStepById(workflowInstance.getCurrentStepId());
            nextSteps.addAll(currStep.getNextStepId());
        }

        // если нет следующих шагов, то был выполнен последний
        if (nextSteps.isEmpty()) {
            workflowInstance.setIsDone(true);
            workflowInstanceService.saveInstance(workflowInstance);
            return;
        }

        // если нет разветвления, но выполнение продолжается с текущийм workflowInstance
        if (nextSteps.size() == 1) {
            executeStep(workflowInstance, nextSteps.get(0));
            return;
        }

        // если есть разветвление, то добавляем workflowInstance на каждую дополнительную ветвь исполнения
        for (int i = 1; i < nextSteps.size(); ++i) {
            UUID nextStepId = nextSteps.get(i);
            WorkflowInstance newInstance = workflowInstanceService.cloneInstance(workflowInstance, nextStepId);
            executeStep(newInstance, nextStepId);
        }
    }

    private void executeStep(WorkflowInstance workflowInstance, UUID stepId) {
        List<IntegrationOutputObject> context = workflowInstance.getContext();
        Step currStep = stepService.getStepById(stepId);

        Map<String, String> stepConf = new HashMap<>(currStep.getParametersConfiguration());

        fillStepConfVariables(stepConf, context);

        String stepSource = currStep.getSource();
        kafkaExecutorProducerService.sendTaskToIntegrationModule(workflowInstance.getId(), stepConf, stepSource, stepId);


        workflowInstance.setCurrentStepId(stepId);
        workflowInstanceService.saveInstance(workflowInstance);
    }

    private void fillStepConfVariables(Map<String, String> conf, List<IntegrationOutputObject> context) {
        for (Map.Entry<String, String> entry : conf.entrySet()) {
            String val = entry.getValue();
            String[] splitVal = val.split(":");

            //если условие верно, значит что значение это ссылка на переменную
            if (splitVal.length > 1) {
                String stepId = splitVal[0];
                String linkStr = splitVal[1];
                String varValue = null;
                try {
                    varValue = findVariableValue(context, stepId, linkStr);
                } catch (Exception ignored) {}
                if (varValue != null) entry.setValue(varValue);
            }
        }
    }

    private String findVariableValue(List<IntegrationOutputObject> context, String stepId, String linkStr) throws NumberFormatException{
        IntegrationOutputObject outputObject = context.stream().filter(o -> o.getStepId().equals(UUID.fromString(stepId)))
                .findAny().orElse(null);

        if (outputObject == null) return null;

        String[] path = linkStr.split("\\.");
        int currPathPart = 0;
        List<OutputInterfaceField> currLayer = outputObject.getFields();
        while (currPathPart < path.length) {

            int index = -1;

            int finalCurrPathPart = currPathPart;
            String fieldName = path[finalCurrPathPart];

            if (path[currPathPart].indexOf('[') != -1) {
                // путь содержит обращение к массиву
                int b1 = path[currPathPart].indexOf('[');
                int b2 = path[currPathPart].indexOf(']');
                index = Integer.parseInt(path[currPathPart].substring(b1 + 1, b2));
                fieldName = path[currPathPart].substring(0, b1);
            }

            OutputInterfaceField field;
            String finalFieldName = fieldName;
            Stream<OutputInterfaceField> stream = currLayer.stream().filter(f -> f.getName().equals(finalFieldName));
            if (index != -1) {
                field = stream.toList().get(index);
            } else {
                Optional<OutputInterfaceField> fieldOptional = stream.findFirst();
                if (fieldOptional.isEmpty()) return null;
                field = fieldOptional.get();
            }

            if (currPathPart + 1 == path.length)
                return field.getValue();

            currPathPart++;
            currLayer = field.getSpec();
        }

        return null;
    }

    private void addIntegrationOutputResultToContext(WorkflowInstance workflowInstance, IntegrationOutputObject outputObject) {
        if (outputObject != null)
            workflowInstance.getContext().add(outputObject);
    }
}
