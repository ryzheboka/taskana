package acceptance.task.query;

import static org.assertj.core.api.Assertions.assertThat;
import static pro.taskana.testapi.DefaultTestEntities.defaultTestClassification;
import static pro.taskana.testapi.DefaultTestEntities.defaultTestObjectReference;
import static pro.taskana.testapi.DefaultTestEntities.defaultTestWorkbasket;

import java.security.PrivilegedActionException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import pro.taskana.classification.api.ClassificationService;
import pro.taskana.classification.api.models.ClassificationSummary;
import pro.taskana.common.api.BaseQuery.SortDirection;
import pro.taskana.common.api.TaskanaEngine;
import pro.taskana.common.api.security.CurrentUserContext;
import pro.taskana.common.internal.InternalTaskanaEngine;
import pro.taskana.common.internal.util.IdGenerator;
import pro.taskana.task.api.TaskService;
import pro.taskana.task.api.models.ObjectReference;
import pro.taskana.task.api.models.TaskSummary;
import pro.taskana.task.internal.TaskMapper;
import pro.taskana.task.internal.TaskServiceImpl;
import pro.taskana.task.internal.models.TaskImpl;
import pro.taskana.testapi.TaskanaInject;
import pro.taskana.testapi.TaskanaIntegrationTest;
import pro.taskana.testapi.builder.TaskAttachmentBuilder;
import pro.taskana.testapi.builder.TaskBuilder;
import pro.taskana.testapi.builder.WorkbasketAccessItemBuilder;
import pro.taskana.testapi.security.WithAccessId;
import pro.taskana.workbasket.api.WorkbasketPermission;
import pro.taskana.workbasket.api.WorkbasketService;
import pro.taskana.workbasket.api.models.WorkbasketSummary;
import pro.taskana.workbasket.internal.WorkbasketAccessMapper;
import pro.taskana.workbasket.internal.WorkbasketServiceImpl;
import pro.taskana.workbasket.internal.models.WorkbasketAccessItemImpl;

@TaskanaIntegrationTest
public class TaskQueryPerformanceTest {

  @TaskanaInject TaskService taskService;
  @TaskanaInject WorkbasketService workbasketService;
  @TaskanaInject CurrentUserContext currentUserContext;
  @TaskanaInject ClassificationService classificationService;
  @TaskanaInject TaskanaEngine taskanaEngine;

  ClassificationSummary defaultClassificationSummary;

  @WithAccessId(user = "admin")
  @BeforeAll
  void setup() throws Exception {
    InternalTaskanaEngine taskanaEngine = ((TaskServiceImpl) taskService).getTaskanaEngine();
    defaultClassificationSummary =
        defaultTestClassification().buildAndStoreAsSummary(classificationService, "admin");
    TaskMapper taskMapper = ((TaskServiceImpl) taskService).getTaskMapper();
    WorkbasketAccessMapper waMapper =
        ((WorkbasketServiceImpl) taskanaEngine.getEngine().getWorkbasketService())
            .getWorkbasketAccessMapper();
    for (int i = 0; i < 300; i++) {
      ClassificationSummary c =
          defaultTestClassification().buildAndStoreAsSummary(classificationService, "admin");
      WorkbasketSummary wb = createWorkbasketWithPermission();
      WorkbasketAccessItemImpl wa =
          (WorkbasketAccessItemImpl)
              WorkbasketAccessItemBuilder.newWorkbasketAccessItem()
                  .workbasketId(wb.getId())
                  .accessId("user-1-1")
                  .permission(WorkbasketPermission.OPEN)
                  .permission(WorkbasketPermission.READ)
                  .permission(WorkbasketPermission.APPEND)
                  .buildAndStore(workbasketService, "businessadmin");
      System.out.println("here");
      final int wi = i;
      taskanaEngine.executeInDatabaseConnection(
          () -> {
            for (int k = 0; k < 300; k++) {
              // System.out.println(wa.getWorkbasketKey());
              wa.setAccessId(IdGenerator.generateWithPrefix("U").substring(0, 6) + k + " " + wi);
              wa.setId(IdGenerator.generateWithPrefix("WA").substring(0, 10) + k + wi);
              waMapper.insert(wa);
            }
          });
      TaskImpl t =
          (TaskImpl)
              taskInWorkbasket(wb, c, String.valueOf(i * 1000000 + 100000000))
                  .buildAndStore(taskService, "admin");
      // System.out.println("here");
      final int u = i;
      taskanaEngine.executeInDatabaseConnection(
          () -> {
            for (int j = 0; j < 300; j++) {
              t.setId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_TASK));
              t.setExternalId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_EXT_TASK));
              ObjectReference objRef = t.getPrimaryObjRef();
              objRef.setCompany(String.valueOf(u * 1000 + j));
              objRef.setValue(String.valueOf(u * 1000 + j));
              t.setPrimaryObjRef(objRef);
              // System.out.println("There");
              taskMapper.insert(t);
            }
          });
      /*
      Connection con = taskanaEngine.g
      Statement stmt=con.createStatement();
      stmt.executeQuery("INSERT INTO TASK VALUES('TKI:" + String.valueOf(i*100+j)
          +"', 'ETI:000000000000000000000000000000000000', RELATIVE_DATE(-1)   , RELATIVE_DATE(0)     , null                 , RELATIVE_DATE(0)     , RELATIVE_DATE(0)     , RELATIVE_DATE(-1)    , RELATIVE_DATE(0)     , 'Task99'                  , 'creator_user_id'            , 'Lorem ipsum was n Quatsch dolor sit amet.', 'Some custom Note'                       , 1       , -1             , 'CLAIMED'   , 'MANUAL'                 , 'T2000'           , '"
          + c.getId() + "', '" +  wb.getId() +"' , 'USER-1-1' , 'DOMAIN_A', 'BPI21'            , 'PBPI21'                  , 'user-1-1'  , 'MyCompany1', 'MySystem1', 'MyInstance1'      , 'MyType1', 'MyValue1'     , true   , false         , null         , 'NONE'        , null              , 'custom1'   , 'custom2'   , 'custom3'  , 'custom4'  , 'custom5'  , 'custom6'  , 'custom7'  , 'custom8'  , 'custom9'  , 'custom10' , 'custom11'  , 'custom12'  , 'custom13'  , 'abc'    , 'custom15'  , 'custom16' , 1           , 2           , 3           , 4           , 5           , 6           , 7           , 8   );");
      // System.out.println(t.getPrimaryObjRef().getValue());
      */

    }
  }

  @WithAccessId(user = "user-1-1")
  @Test
  void should_ReturnAllTasksFromWorkbasketAsAdmin_When_NoAccessItemForWorkbasketExists() {
    double max = 0.;
    System.out.println("Starting");
    for (int i = 0; i < 500; i++) {
      int a = i % 300;
      long startTime = System.currentTimeMillis();
      List<TaskSummary> list =
          taskService
              .createTaskQuery()
              .primaryObjectReferenceValueLike("%" + String.valueOf(0) + "%")
              .primaryObjectReferenceCompanyIn(String.valueOf(a * 1000))
              .orderByDue(SortDirection.DESCENDING)
              .orderByTaskId(SortDirection.ASCENDING)
              .list();
      long stopTime = System.currentTimeMillis();
      // System.out.println(list.get(0).getPrimaryObjRef().getCompany()+"***");
      if ((stopTime - startTime) > max) max = (stopTime - startTime);
      assertThat(list).hasSize(1);
      System.out.println(max);
    }
    System.out.println(max);
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

  private TaskAttachmentBuilder createAttachment() {
    return TaskAttachmentBuilder.newAttachment()
        .objectReference(defaultTestObjectReference().build())
        .classificationSummary(defaultClassificationSummary);
  }

  private TaskBuilder taskInWorkbasket(WorkbasketSummary wb, ClassificationSummary c, String s) {
    return TaskBuilder.newTask()
        .classificationSummary(c)
        .primaryObjRef(defaultTestObjectReference().company(s).value(s).build())
        .workbasketSummary(wb);
  }

  private WorkbasketSummary createWorkbasketWithPermission() throws PrivilegedActionException {
    WorkbasketSummary workbasketSummary =
        defaultTestWorkbasket().buildAndStoreAsSummary(workbasketService, "businessadmin");
    persistPermission(workbasketSummary);
    return workbasketSummary;
  }
}
