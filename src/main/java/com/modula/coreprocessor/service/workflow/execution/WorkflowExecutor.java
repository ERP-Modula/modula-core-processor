package com.modula.coreprocessor.service.workflow.execution;

import com.modula.common.domain.workflow.execution.IntegrationOutputObject;
import com.modula.common.domain.workflow.execution.OutputInterfaceField;
import com.modula.common.domain.workflow.execution.WorkflowInstance;
import com.modula.common.domain.workflow.step.Step;
import com.modula.coreprocessor.domain.dto.integration.ExecutorTask;
import com.modula.coreprocessor.service.StepService;
import com.modula.coreprocessor.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

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
            //TODO check workflow instances count and send message to notification service
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

        //TODO send to kafka
        String stepSource = currStep.getSource();
        kafkaExecutorProducerService.sendTaskToIntegrationModule(workflowInstance.getId(), stepConf, stepSource);


        workflowInstance.setCurrentStepId(stepId);
        workflowInstanceService.saveInstance(workflowInstance);
    }

    private void fillStepConfVariables(Map<String, String> conf, List<IntegrationOutputObject> context) {
        for (Map.Entry<String, String> entry : conf.entrySet()) {
            String val = entry.getValue();
            String[] splitVal = val.split(":");

            //значит что значение это ссылка на переменную
            if (splitVal.length > 1) {
                String moduleName = splitVal[0];
                String linkStr = splitVal[1];

                String varValue = findVariableValue(context, moduleName, linkStr);
                if (varValue != null) entry.setValue(varValue);
            }
        }
    }

    private String findVariableValue(List<IntegrationOutputObject> context, String moduleName, String linkStr) {
        IntegrationOutputObject outputObject = context.stream().filter(o -> o.getModuleName().equals(moduleName))
                .findAny().orElse(null);

        if (outputObject == null) return null;

        String[] path = linkStr.split(".");
        int currPathPart = 0;
        List<OutputInterfaceField> currLayer = outputObject.getFields();
        while (currPathPart < path.length) {

            int finalCurrPathPart = currPathPart;
            Optional<OutputInterfaceField> field = currLayer.stream().filter(f -> f.getName().equals(path[finalCurrPathPart])).findFirst();
            if (field.isEmpty()) return null;

            if (currPathPart + 1 == path.length)
                return field.get().getValue();

            currPathPart++;
            currLayer = field.get().getSpec();
        }

        return null;
    }

    private void addIntegrationOutputResultToContext(WorkflowInstance workflowInstance, IntegrationOutputObject outputObject) {
        workflowInstance.getContext().add(outputObject);
    }
}
