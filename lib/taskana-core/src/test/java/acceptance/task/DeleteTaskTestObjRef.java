package acceptance.task;

import static acceptance.DefaultTestEntities.defaultTestClassification;
import static acceptance.DefaultTestEntities.defaultTestObjectReference;
import static acceptance.DefaultTestEntities.defaultTestWorkbasket;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.TaskanaEngineProxy;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testapi.TaskanaInject;
import testapi.TaskanaIntegrationTest;

import pro.taskana.classification.api.ClassificationService;
import pro.taskana.classification.api.models.ClassificationSummary;
import pro.taskana.common.api.TaskanaEngine;
import pro.taskana.common.test.security.WithAccessId;
import pro.taskana.task.api.TaskService;
import pro.taskana.task.api.TaskState;
import pro.taskana.task.api.models.ObjectReference;
import pro.taskana.task.api.models.Task;
import pro.taskana.task.internal.ObjectReferenceMapper;
import pro.taskana.task.internal.builder.ObjectReferenceBuilder;
import pro.taskana.task.internal.builder.TaskBuilder;
import pro.taskana.workbasket.api.WorkbasketPermission;
import pro.taskana.workbasket.api.WorkbasketService;
import pro.taskana.workbasket.api.models.WorkbasketSummary;
import pro.taskana.workbasket.internal.builder.WorkbasketAccessItemBuilder;

@TaskanaIntegrationTest
public class DeleteTaskTestObjRef {
  @TaskanaInject TaskService taskService;
  @TaskanaInject WorkbasketService workbasketService;
  @TaskanaInject ClassificationService classificationService;
  @TaskanaInject TaskanaEngine taskanaEngine;

  ClassificationSummary defaultClassificationSummary;
  WorkbasketSummary defaultWorkbasketSummary;
  ObjectReference defaultObjectReference;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
    defaultWorkbasketSummary = defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService);

    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(defaultWorkbasketSummary.getId())
        .accessId("user-1-1")
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService);
    defaultObjectReference = defaultTestObjectReference().build();
  }

  @WithAccessId(user = "admin")
  @Test
  void should_DeleteObjectReferences_When_DeletingTask() throws Exception {
    ObjectReference objRef1 =
        ObjectReferenceBuilder.newObjectReference()
            .company("FirstCompany")
            .value("FirstValue")
            .type("FirstType")
            .build();
    ObjectReference objRef2 =
        ObjectReferenceBuilder.newObjectReference()
            .company("SecondCompany")
            .value("SecondValue")
            .type("SecondType")
            .build();

    Task createdTask =
        createCompletedTask().objectReferences(objRef1, objRef2).buildAndStore(taskService);
    taskService.deleteTask(createdTask.getId());
    TaskanaEngineProxy engineProxy = new TaskanaEngineProxy(taskanaEngine);
    ObjectReferenceMapper objectReferenceMapper =
        engineProxy.getEngine().getSqlSession().getMapper(ObjectReferenceMapper.class);

    try {
      engineProxy.openConnection();
      assertThat(objectReferenceMapper.findObjectReferencesByTaskId(createdTask.getId()))
          .hasSize(0);
    } finally {
      engineProxy.returnConnection();
    }
  }

  @WithAccessId(user = "admin")
  @Test
  void should_DeleteObjectReferences_When_MultipleTasksAreDeleted() throws Exception {
    ObjectReference objRef1 =
        ObjectReferenceBuilder.newObjectReference()
            .company("FirstCompany")
            .value("FirstValue")
            .type("FirstType")
            .build();
    ObjectReference objRef2 =
        ObjectReferenceBuilder.newObjectReference()
            .company("SecondCompany")
            .value("SecondValue")
            .type("SecondType")
            .build();

    Task firstCreatedTask =
        createCompletedTask().objectReferences(objRef1).buildAndStore(taskService);
    Task secondCreatedTask =
        createCompletedTask().objectReferences(objRef1, objRef2).buildAndStore(taskService);
    taskService.deleteTasks(List.of(firstCreatedTask.getId(), secondCreatedTask.getId()));
    TaskanaEngineProxy engineProxy = new TaskanaEngineProxy(taskanaEngine);
    ObjectReferenceMapper objectReferenceMapper =
        engineProxy.getEngine().getSqlSession().getMapper(ObjectReferenceMapper.class);

    try {
      engineProxy.openConnection();
      assertThat(
              objectReferenceMapper.findObjectReferencesByTaskIds(
                  List.of(firstCreatedTask.getId(), secondCreatedTask.getId())))
          .hasSize(0);
    } finally {
      engineProxy.returnConnection();
    }
  }

  private TaskBuilder createCompletedTask() {
    return (TaskBuilder.newTask()
        .classificationSummary(defaultClassificationSummary)
        .workbasketSummary(defaultWorkbasketSummary)
        .primaryObjRef(defaultObjectReference)
        .state(TaskState.COMPLETED));
  }
}
