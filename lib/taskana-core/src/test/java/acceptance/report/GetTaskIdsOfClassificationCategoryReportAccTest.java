package acceptance.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;

import pro.taskana.common.api.exceptions.InvalidArgumentException;
import pro.taskana.common.api.exceptions.NotAuthorizedException;
import pro.taskana.common.test.security.JaasExtension;
import pro.taskana.common.test.security.WithAccessId;
import pro.taskana.monitor.api.MonitorService;
import pro.taskana.monitor.api.SelectedItem;
import pro.taskana.monitor.api.TaskTimestamp;
import pro.taskana.monitor.api.reports.header.TimeIntervalColumnHeader;
import pro.taskana.task.api.TaskCustomField;
import pro.taskana.task.api.TaskState;

@ExtendWith(JaasExtension.class)
class GetTaskIdsOfClassificationCategoryReportAccTest extends AbstractReportAccTest {

  private static final MonitorService MONITOR_SERVICE = taskanaEngine.getMonitorService();

  private static final SelectedItem EXTERN = new SelectedItem("EXTERN", null, -5, -2);
  private static final SelectedItem AUTOMATIC =
      new SelectedItem("AUTOMATIC", null, Integer.MIN_VALUE, -11);
  private static final SelectedItem MANUAL = new SelectedItem("MANUAL", null, 0, 0);

  @Test
  void testRoleCheck() {
    ThrowingCallable call =
        () ->
            MONITOR_SERVICE
                .createClassificationCategoryReportBuilder()
                .listTaskIdsForSelectedItems(List.of(), TaskTimestamp.DUE);
    assertThatThrownBy(call).isInstanceOf(NotAuthorizedException.class);
  }

  @WithAccessId(user = "monitor")
  @TestFactory
  Stream<DynamicTest> should_NotThrowError_When_BuildReportForTaskState() {
    Iterator<TaskTimestamp> iterator = Arrays.stream(TaskTimestamp.values()).iterator();

    ThrowingConsumer<TaskTimestamp> test =
        timestamp -> {
          ThrowingCallable callable =
              () ->
                  MONITOR_SERVICE
                      .createClassificationCategoryReportBuilder()
                      .listTaskIdsForSelectedItems(List.of(EXTERN), timestamp);
          assertThatCode(callable).doesNotThrowAnyException();
        };

    return DynamicTest.stream(iterator, t -> "for " + t, test);
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_SelectCompletedItems_When_CompletedTimeStampIsRequested() throws Exception {
    final List<TimeIntervalColumnHeader> columnHeaders = List.of(new TimeIntervalColumnHeader(0));

    final List<SelectedItem> selectedItems = List.of(new SelectedItem("EXTERN", null, 0, 0));

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.COMPLETED);

    assertThat(ids)
        .containsExactly(
            "TKI:000000000000000000000000000000000001", "TKI:000000000000000000000000000000000002");
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTaskIdsOfCategoryReport() throws Exception {
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000006",
            "TKI:000000000000000000000000000000000020",
            "TKI:000000000000000000000000000000000021",
            "TKI:000000000000000000000000000000000022",
            "TKI:000000000000000000000000000000000023",
            "TKI:000000000000000000000000000000000024",
            "TKI:000000000000000000000000000000000026",
            "TKI:000000000000000000000000000000000027",
            "TKI:000000000000000000000000000000000028",
            "TKI:000000000000000000000000000000000031",
            "TKI:000000000000000000000000000000000032");
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTaskIdsOfCategoryReportWithWorkbasketFilter() throws Exception {
    final List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .workbasketIdIn(workbasketIds)
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000006",
            "TKI:000000000000000000000000000000000020",
            "TKI:000000000000000000000000000000000026",
            "TKI:000000000000000000000000000000000031");
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTaskIdsOfCategoryReportWithStateFilter() throws Exception {
    final List<TaskState> states = List.of(TaskState.READY);
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .stateIn(states)
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000006",
            "TKI:000000000000000000000000000000000020",
            "TKI:000000000000000000000000000000000021",
            "TKI:000000000000000000000000000000000022",
            "TKI:000000000000000000000000000000000023",
            "TKI:000000000000000000000000000000000024",
            "TKI:000000000000000000000000000000000026",
            "TKI:000000000000000000000000000000000027",
            "TKI:000000000000000000000000000000000028",
            "TKI:000000000000000000000000000000000031",
            "TKI:000000000000000000000000000000000032");
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTaskIdsOfCategoryReportWithCategoryFilter() throws Exception {
    final List<String> categories = List.of("AUTOMATIC", "MANUAL");
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .classificationCategoryIn(categories)
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000006",
            "TKI:000000000000000000000000000000000031",
            "TKI:000000000000000000000000000000000032");
  }

  @WithAccessId(user = "monitor")
  @Test
  void testGetTaskIdsOfCategoryReportWithDomainFilter() throws Exception {
    final List<String> domains = List.of("DOMAIN_A");
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .domainIn(domains)
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000020",
            "TKI:000000000000000000000000000000000021",
            "TKI:000000000000000000000000000000000022",
            "TKI:000000000000000000000000000000000028");
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnTaskIdsOfCategoryReport_When_FilteringWithCustomAttributeIn() throws Exception {
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .customAttributeIn(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A")
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000020",
            "TKI:000000000000000000000000000000000024",
            "TKI:000000000000000000000000000000000027",
            "TKI:000000000000000000000000000000000031",
            "TKI:000000000000000000000000000000000032");
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnTaskIdsOfCategoryReport_When_FilteringWithCustomAttributeNotIn()
      throws Exception {
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .customAttributeNotIn(TaskCustomField.CUSTOM_2, "Vollkasko")
            .customAttributeNotIn(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A")
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids)
        .containsExactlyInAnyOrder(
            "TKI:000000000000000000000000000000000006",
            "TKI:000000000000000000000000000000000021",
            "TKI:000000000000000000000000000000000022",
            "TKI:000000000000000000000000000000000028");
  }

  @WithAccessId(user = "monitor")
  @Test
  void should_ReturnTaskIdsOfCategoryReport_When_FilteringWithCustomAttributeLike()
      throws Exception {
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(EXTERN, AUTOMATIC, MANUAL);

    List<String> ids =
        MONITOR_SERVICE
            .createClassificationCategoryReportBuilder()
            .withColumnHeaders(columnHeaders)
            .inWorkingDays()
            .customAttributeLike(TaskCustomField.CUSTOM_1, "%aeftsstelle A")
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    assertThat(ids).hasSize(5);
  }

  @WithAccessId(user = "monitor")
  @Test
  void testThrowsExceptionIfSubKeysAreUsed() {
    final List<TimeIntervalColumnHeader> columnHeaders = getListOfColumnHeaders();

    final List<SelectedItem> selectedItems = List.of(new SelectedItem("EXTERN", "INVALID", -5, -2));

    ThrowingCallable call =
        () ->
            MONITOR_SERVICE
                .createClassificationCategoryReportBuilder()
                .withColumnHeaders(columnHeaders)
                .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);
    assertThatThrownBy(call).isInstanceOf(InvalidArgumentException.class);
  }

  private List<TimeIntervalColumnHeader> getListOfColumnHeaders() {
    List<TimeIntervalColumnHeader> columnHeaders = new ArrayList<>();
    columnHeaders.add(new TimeIntervalColumnHeader(Integer.MIN_VALUE, -11));
    columnHeaders.add(new TimeIntervalColumnHeader(-10, -6));
    columnHeaders.add(new TimeIntervalColumnHeader(-5, -2));
    columnHeaders.add(new TimeIntervalColumnHeader(-1));
    columnHeaders.add(new TimeIntervalColumnHeader(0));
    columnHeaders.add(new TimeIntervalColumnHeader(1));
    columnHeaders.add(new TimeIntervalColumnHeader(2, 5));
    columnHeaders.add(new TimeIntervalColumnHeader(6, 10));
    columnHeaders.add(new TimeIntervalColumnHeader(11, Integer.MAX_VALUE));
    return columnHeaders;
  }
}
