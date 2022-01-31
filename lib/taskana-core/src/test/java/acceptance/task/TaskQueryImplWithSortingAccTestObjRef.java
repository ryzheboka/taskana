package acceptance.task;

import static acceptance.DefaultTestEntities.defaultTestClassification;
import static acceptance.DefaultTestEntities.defaultTestObjectReference;
import static org.assertj.core.api.Assertions.assertThat;

import acceptance.DefaultTestEntities;
import java.security.PrivilegedActionException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import testapi.TaskanaInject;
import testapi.TaskanaIntegrationTest;

import pro.taskana.classification.api.ClassificationService;
import pro.taskana.classification.api.models.ClassificationSummary;
import pro.taskana.common.api.BaseQuery.SortDirection;
import pro.taskana.common.api.security.CurrentUserContext;
import pro.taskana.common.test.security.WithAccessId;
import pro.taskana.task.api.TaskService;
import pro.taskana.task.api.models.ObjectReference;
import pro.taskana.task.api.models.TaskSummary;
import pro.taskana.task.internal.builder.ObjectReferenceBuilder;
import pro.taskana.task.internal.builder.TaskAttachmentBuilder;
import pro.taskana.task.internal.builder.TaskBuilder;
import pro.taskana.workbasket.api.WorkbasketPermission;
import pro.taskana.workbasket.api.WorkbasketService;
import pro.taskana.workbasket.api.models.WorkbasketSummary;
import pro.taskana.workbasket.internal.builder.WorkbasketAccessItemBuilder;

@TaskanaIntegrationTest
class TaskQueryImplWithSortingAccTestObjRef {

  @TaskanaInject TaskService taskService;
  @TaskanaInject WorkbasketService workbasketService;
  @TaskanaInject CurrentUserContext currentUserContext;
  @TaskanaInject ClassificationService classificationService;

  ClassificationSummary defaultClassificationSummary;

  @WithAccessId(user = "businessadmin")
  @BeforeAll
  void setup() throws Exception {
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService);
  }

  private TaskAttachmentBuilder createAttachment() {
    return TaskAttachmentBuilder.newAttachment()
        .objectReference(defaultTestObjectReference().build())
        .classificationSummary(defaultClassificationSummary);
  }

  private TaskBuilder taskInWorkbasket(WorkbasketSummary wb) {
    return TaskBuilder.newTask()
        .classificationSummary(defaultClassificationSummary)
        .primaryObjRef(defaultTestObjectReference().build())
        .workbasketSummary(wb);
  }

  private WorkbasketSummary createWorkbasketWithPermission() throws PrivilegedActionException {
    WorkbasketSummary workbasketSummary =
        DefaultTestEntities.defaultTestWorkbasket()
            .buildAndStoreAsSummary(workbasketService, "businessadmin");
    persistPermission(workbasketSummary);
    return workbasketSummary;
  }

  private void persistPermission(WorkbasketSummary workbasketSummary)
      throws PrivilegedActionException {
    WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
        .workbasketId(workbasketSummary.getId())
        .accessId(currentUserContext.getUserid())
        .permission(WorkbasketPermission.OPEN)
        .permission(WorkbasketPermission.READ)
        .permission(WorkbasketPermission.APPEND)
        .buildAndStore(workbasketService, "businessadmin");
  }

  @Nested
  @TestInstance(Lifecycle.PER_CLASS)
  class SortTest {

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ObjectReferenceValue {
      WorkbasketSummary wb;
      TaskSummary taskSummary1;
      TaskSummary taskSummary2;
      TaskSummary taskSummary3;
      TaskSummary taskSummary4;

      @WithAccessId(user = "user-1-1")
      @BeforeAll
      void setup() throws Exception {
        wb = createWorkbasketWithPermission();
        ObjectReference objRef1 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("FirstType")
                .build();
        ObjectReference objRef2 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("SecondValue")
                .type("SecondType")
                .build();
        ObjectReference objRef3 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("SecondValue")
                .type("FirstType")
                .build();
        ObjectReference objRef1copy = objRef1.copy();

        taskSummary1 =
            taskInWorkbasket(wb).objectReferences(objRef1).buildAndStoreAsSummary(taskService);
        taskSummary2 =
            taskInWorkbasket(wb).objectReferences(objRef2).buildAndStoreAsSummary(taskService);
        taskSummary3 =
            taskInWorkbasket(wb).objectReferences(objRef3).buildAndStoreAsSummary(taskService);
        taskSummary4 =
            taskInWorkbasket(wb).objectReferences(objRef1copy).buildAndStoreAsSummary(taskService);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_returnSortedList_When_SortingByValueDesc() {
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .orderBySorValue(SortDirection.DESCENDING)
                .list();

        assertThat(results).hasSize(4);
        TaskSummary previousSummary = null;
        for (TaskSummary taskSummary : results) {
          if (previousSummary != null) {
            assertThat(
                    taskSummary
                            .getSecondaryObjectReferences()
                            .get(0)
                            .getValue()
                            .compareToIgnoreCase(
                                previousSummary.getSecondaryObjectReferences().get(0).getValue())
                        <= 0)
                .isTrue();
          }
          previousSummary = taskSummary;
        }
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_returnSortedList_When_SortingByValueDescAndTypeAsc() {
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .orderBySorValue(SortDirection.DESCENDING)
                .orderBySorType(SortDirection.ASCENDING)
                .list();

        assertThat(results).hasSize(4);
        TaskSummary previousSummary = null;
        for (TaskSummary taskSummary : results) {
          if (previousSummary != null) {
            assertThat(
                    taskSummary
                            .getSecondaryObjectReferences()
                            .get(0)
                            .getValue()
                            .compareToIgnoreCase(
                                previousSummary.getSecondaryObjectReferences().get(0).getValue())
                        <= 0)
                .isTrue();
            assertThat(
                    taskSummary
                                .getSecondaryObjectReferences()
                                .get(0)
                                .getValue()
                                .compareToIgnoreCase(
                                    previousSummary
                                        .getSecondaryObjectReferences()
                                        .get(0)
                                        .getValue())
                            != 0
                        || taskSummary
                                .getSecondaryObjectReferences()
                                .get(0)
                                .getType()
                                .compareToIgnoreCase(
                                    previousSummary.getSecondaryObjectReferences().get(0).getType())
                            >= 0)
                .isTrue();
          }
          previousSummary = taskSummary;
        }
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_returnSortedList_When_SortingByValueAsc() {
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .orderBySorValue(SortDirection.ASCENDING)
                .list();

        assertThat(results).hasSize(4);
        TaskSummary previousSummary = null;
        for (TaskSummary taskSummary : results) {
          if (previousSummary != null) {
            assertThat(
                    taskSummary
                            .getSecondaryObjectReferences()
                            .get(0)
                            .getValue()
                            .compareToIgnoreCase(
                                previousSummary.getSecondaryObjectReferences().get(0).getValue())
                        >= 0)
                .isTrue();
          }
          previousSummary = taskSummary;
        }
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ObjectReferenceType {
      WorkbasketSummary wb;
      TaskSummary taskSummary1;
      TaskSummary taskSummary2;
      TaskSummary taskSummary3;
      TaskSummary taskSummary4;

      @WithAccessId(user = "user-1-1")
      @BeforeAll
      void setup() throws Exception {
        wb = createWorkbasketWithPermission();
        ObjectReference objRef1 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("FirstType")
                .build();
        ObjectReference objRef2 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("SecondType")
                .build();
        ObjectReference objRef2copy = objRef2.copy();
        ObjectReference objRef1copy = objRef1.copy();

        taskSummary1 =
            taskInWorkbasket(wb).objectReferences(objRef1).buildAndStoreAsSummary(taskService);
        taskSummary2 =
            taskInWorkbasket(wb).objectReferences(objRef2).buildAndStoreAsSummary(taskService);
        taskSummary3 =
            taskInWorkbasket(wb).objectReferences(objRef1copy).buildAndStoreAsSummary(taskService);
        taskSummary4 =
            taskInWorkbasket(wb).objectReferences(objRef2copy).buildAndStoreAsSummary(taskService);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_returnSortedList_When_SortingByTypeDesc() {
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .orderBySorType(SortDirection.DESCENDING)
                .list();

        assertThat(results).hasSize(4);
        TaskSummary previousSummary = null;
        for (TaskSummary taskSummary : results) {
          if (previousSummary != null) {
            assertThat(
                    taskSummary
                            .getSecondaryObjectReferences()
                            .get(0)
                            .getType()
                            .compareToIgnoreCase(
                                previousSummary.getSecondaryObjectReferences().get(0).getType())
                        <= 0)
                .isTrue();
          }
          previousSummary = taskSummary;
        }
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_returnSortedList_When_SortingByTypeAsc() {
        List<TaskSummary> results =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .orderBySorType(SortDirection.ASCENDING)
                .list();

        assertThat(results).hasSize(4);
        TaskSummary previousSummary = null;
        for (TaskSummary taskSummary : results) {
          if (previousSummary != null) {
            assertThat(
                    taskSummary
                            .getSecondaryObjectReferences()
                            .get(0)
                            .getType()
                            .compareToIgnoreCase(
                                previousSummary.getSecondaryObjectReferences().get(0).getType())
                        >= 0)
                .isTrue();
          }
          previousSummary = taskSummary;
        }
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ObjectReferenceCompany {
      WorkbasketSummary wb;
      TaskSummary taskSummary1;
      TaskSummary taskSummary2;
      TaskSummary taskSummary3;

      @WithAccessId(user = "user-1-1")
      @BeforeAll
      void setup() throws Exception {
        wb = createWorkbasketWithPermission();
        ObjectReference objRef1 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("FirstType")
                .build();
        ObjectReference objRef2 =
            ObjectReferenceBuilder.newObjectReference()
                .company("SecondCompany")
                .value("FirstValue")
                .type("SecondType")
                .build();
        ObjectReference objRef2copy = objRef2.copy();
        ObjectReference objRef1copy = objRef1.copy();

        taskSummary1 =
            taskInWorkbasket(wb).objectReferences(objRef1).buildAndStoreAsSummary(taskService);
        taskSummary2 =
            taskInWorkbasket(wb).objectReferences(objRef2).buildAndStoreAsSummary(taskService);
        taskSummary3 =
            taskInWorkbasket(wb)
                .objectReferences(objRef2copy, objRef1copy)
                .buildAndStoreAsSummary(taskService);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorCompanyIn("FirstCompany")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorCompanyNotIn("FirstCompany")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary3, taskSummary2);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorCompanyLike("%Company")
                .list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary2, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ReturnEmptyList_When_QueryingForNonexistentTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorCompanyLike("%NoSuchCompany")
                .list();
        assertThat(list).isEmpty();
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotLike() {
        List<TaskSummary> list =
            taskService.createTaskQuery().workbasketIdIn(wb.getId()).sorCompanyNotLike("F%").list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary2, taskSummary3);
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ObjectReferenceSystem {
      WorkbasketSummary wb;
      TaskSummary taskSummary1;
      TaskSummary taskSummary2;
      TaskSummary taskSummary3;
      TaskSummary taskSummary4;

      @WithAccessId(user = "user-1-1")
      @BeforeAll
      void setup() throws Exception {
        wb = createWorkbasketWithPermission();
        ObjectReference objRef1 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("FirstType")
                .system("FirstSystem")
                .build();
        ObjectReference objRef2 =
            ObjectReferenceBuilder.newObjectReference()
                .company("SecondCompany")
                .value("FirstValue")
                .type("SecondType")
                .system("SecondSystem")
                .build();
        ObjectReference objRefNoSystem =
            ObjectReferenceBuilder.newObjectReference()
                .company("SecondCompany")
                .value("FirstValue")
                .type("SecondType")
                .build();
        ObjectReference objRef2copy = objRef2.copy();
        ObjectReference objRef1copy = objRef1.copy();

        taskSummary1 =
            taskInWorkbasket(wb).objectReferences(objRef1).buildAndStoreAsSummary(taskService);
        taskSummary2 =
            taskInWorkbasket(wb).objectReferences(objRef2).buildAndStoreAsSummary(taskService);
        taskSummary3 =
            taskInWorkbasket(wb)
                .objectReferences(objRef2copy, objRef1copy)
                .buildAndStoreAsSummary(taskService);
        taskSummary4 =
            taskInWorkbasket(wb)
                .objectReferences(objRefNoSystem)
                .buildAndStoreAsSummary(taskService);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemIn("FirstSystem")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemNotIn("FirstSystem")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary3, taskSummary2);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemLike("%System")
                .list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary2, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ReturnEmptyList_When_QueryingForNonexistentTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemLike("%NoSuchSystem")
                .list();
        assertThat(list).isEmpty();
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotLike() {
        List<TaskSummary> list =
            taskService.createTaskQuery().workbasketIdIn(wb.getId()).sorSystemNotLike("F%").list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary2, taskSummary3);
      }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class ObjectReferenceSystemInstance {
      WorkbasketSummary wb;
      TaskSummary taskSummary1;
      TaskSummary taskSummary2;
      TaskSummary taskSummary3;
      TaskSummary taskSummary4;

      @WithAccessId(user = "user-1-1")
      @BeforeAll
      void setup() throws Exception {
        wb = createWorkbasketWithPermission();
        ObjectReference objRef1 =
            ObjectReferenceBuilder.newObjectReference()
                .company("FirstCompany")
                .value("FirstValue")
                .type("FirstType")
                .systemInstance("FirstSystemInstance")
                .build();
        ObjectReference objRef2 =
            ObjectReferenceBuilder.newObjectReference()
                .company("SecondCompany")
                .value("FirstValue")
                .type("SecondType")
                .systemInstance("SecondSystemInstance")
                .build();
        ObjectReference objRefNoSystem =
            ObjectReferenceBuilder.newObjectReference()
                .company("SecondCompany")
                .value("FirstValue")
                .type("SecondType")
                .build();
        ObjectReference objRef2copy = objRef2.copy();
        ObjectReference objRef1copy = objRef1.copy();

        taskSummary1 =
            taskInWorkbasket(wb).objectReferences(objRef1).buildAndStoreAsSummary(taskService);
        taskSummary2 =
            taskInWorkbasket(wb).objectReferences(objRef2).buildAndStoreAsSummary(taskService);
        taskSummary3 =
            taskInWorkbasket(wb)
                .objectReferences(objRef2copy, objRef1copy)
                .buildAndStoreAsSummary(taskService);
        taskSummary4 =
            taskInWorkbasket(wb)
                .objectReferences(objRefNoSystem)
                .buildAndStoreAsSummary(taskService);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemInstanceIn("FirstSystemInstance")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotIn() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemInstanceNotIn("FirstSystemInstance")
                .list();

        assertThat(list).containsExactlyInAnyOrder(taskSummary3, taskSummary2);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemInstanceLike("%SystemInstance")
                .list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary1, taskSummary2, taskSummary3);
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ReturnEmptyList_When_QueryingForNonexistentTypeLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemInstanceLike("%NoSuchSystemInstance")
                .list();
        assertThat(list).isEmpty();
      }

      @WithAccessId(user = "user-1-1")
      @Test
      void should_ApplyFilter_When_QueryingForTypeNotLike() {
        List<TaskSummary> list =
            taskService
                .createTaskQuery()
                .workbasketIdIn(wb.getId())
                .sorSystemInstanceNotLike("F%")
                .list();
        assertThat(list).containsExactlyInAnyOrder(taskSummary2, taskSummary3);
      }
    }
  }
}
