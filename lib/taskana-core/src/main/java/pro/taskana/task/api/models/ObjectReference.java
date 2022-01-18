package pro.taskana.task.api.models;

import pro.taskana.common.api.exceptions.InvalidArgumentException;

/** Attachment-Interface to specify Attachment Attributes. */
public interface ObjectReference {

  String getId();

  void setId(String id);

  String getTaskId();

  void setTaskId(String taskId);

  String getCompany();

  void setCompany(String company);

  String getSystem();

  void setSystem(String system);

  String getSystemInstance();

  void setSystemInstance(String systemInstance);

  String getType();

  void setType(String type);

  String getValue();

  void setValue(String value);

  ObjectReference copy();

  static void validate(ObjectReference objectReference, String objRefType, String objName)
      throws InvalidArgumentException {
    // check that all values in the ObjectReference are set correctly
    if (objectReference == null) {
      throw new InvalidArgumentException(
          String.format("%s of %s must not be null.", objRefType, objName));
    } else if (objectReference.getCompany() == null || objectReference.getCompany().isEmpty()) {
      throw new InvalidArgumentException(
          String.format("Company of %s of %s must not be empty", objRefType, objName));
    } else if (objectReference.getType() == null || objectReference.getType().length() == 0) {
      throw new InvalidArgumentException(
          String.format("Type of %s of %s must not be empty", objRefType, objName));
    } else if (objectReference.getValue() == null || objectReference.getValue().length() == 0) {
      throw new InvalidArgumentException(
          String.format("Value of %s of %s must not be empty", objRefType, objName));
    }
  }
}
