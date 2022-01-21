package pro.taskana.task.api.models;

/** ObjectReference-Interface to specify Attachment Attributes. */
public interface ObjectReference {

  /**
   * Gets the id of the objectReference.
   *
   * @return attachmentId
   */
  String getId();

  /**
   * Gets the id of the associated task.
   *
   * @return taskId
   */
  String getTaskId();

  String getCompany();

  String getSystem();

  String getSystemInstance();

  String getType();

  String getValue();

  ObjectReference copy();
}
