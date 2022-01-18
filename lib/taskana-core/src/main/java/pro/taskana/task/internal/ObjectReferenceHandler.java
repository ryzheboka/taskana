package pro.taskana.task.internal;

import java.util.List;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.taskana.task.api.exceptions.AttachmentPersistenceException;
import pro.taskana.task.api.models.ObjectReference;
import pro.taskana.task.api.models.Task;
import pro.taskana.task.internal.models.TaskImpl;

public class ObjectReferenceHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentHandler.class);
  private final ObjectReferenceMapper objectReferenceMapper;

  ObjectReferenceHandler(ObjectReferenceMapper objectReferenceMapper) {
    this.objectReferenceMapper = objectReferenceMapper;
  }

  void insertNewObjectReferenceOnTaskCreation(TaskImpl task) throws AttachmentPersistenceException {
    List<ObjectReference> objectReferences = task.getObjectReferences();

    if (objectReferences != null) {
      for (ObjectReference objectReference : objectReferences) {
        // verifyAttachment(attachmentImpl, task.getDomain());
        initObjectReference(objectReference, task);

        try {
          System.out.println(objectReference.toString());
          objectReferenceMapper.insert(objectReference);
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "TaskService.createTask() for TaskId={} INSERTED an object reference={}.",
                task.getId(),
                objectReference);
          }
        } catch (PersistenceException e) {
          throw new AttachmentPersistenceException(objectReference.getId(), task.getId(), e);
        }
      }
    }
  }

  private void initObjectReference(ObjectReference objectReference, Task newTask) {
    if (objectReference.getTaskId() == null) {
      objectReference.setTaskId(newTask.getId());
    }
  }
}
