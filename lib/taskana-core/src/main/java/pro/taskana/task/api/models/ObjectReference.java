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

  /**
   * Gets the company of the objectReference.
   *
   * @return company
   */
  String getCompany();

  /**
   * Sets the company of the objectReference.
   *
   * @param company the company of the object reference
   */
  void setCompany(String company);

  /**
   * Gets the system of the objectReference.
   *
   * @return system
   */
  String getSystem();

  /**
   * Sets the system of the objectReference.
   *
   * @param system the system of the objectReference
   */
  void setSystem(String system);

  /**
   * Gets the systemInstance of the objectReference.
   *
   * @return systemInstance
   */
  String getSystemInstance();

  /**
   * Sets the system instance of the objectReference.
   *
   * @param systemInstance the system instance of the objectReference
   */
  void setSystemInstance(String systemInstance);

  /**
   * Gets the type of the objectReference.
   *
   * @return type
   */
  String getType();

  /**
   * Sets the type of the objectReference.
   *
   * @param type the type of the objectReference
   */
  void setType(String type);

  /**
   * Gets the value of the objectReference.
   *
   * @return value
   */
  String getValue();

  /**
   * Sets the value of the objectReference.
   *
   * @param value the value of the objectReference
   */
  void setValue(String value);

  /**
   * Duplicates this objectReference without the id and taskId.
   *
   * @return a copy of this objectReference
   */
  ObjectReference copy();
}
