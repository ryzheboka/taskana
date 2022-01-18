package pro.taskana.task.internal;

import java.util.List;
import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.taskana.common.api.exceptions.InvalidArgumentException;
import pro.taskana.common.internal.util.IdGenerator;
import pro.taskana.task.api.exceptions.ObjectReferencePersistenceException;
import pro.taskana.task.api.models.ObjectReference;
import pro.taskana.task.api.models.Task;
import pro.taskana.task.internal.models.ObjectReferenceImpl;
import pro.taskana.task.internal.models.TaskImpl;

public class ObjectReferenceHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentHandler.class);
  private final ObjectReferenceMapper objectReferenceMapper;

  ObjectReferenceHandler(ObjectReferenceMapper objectReferenceMapper) {
    this.objectReferenceMapper = objectReferenceMapper;
  }

  void insertNewObjectReferenceOnTaskCreation(TaskImpl task)
      throws ObjectReferencePersistenceException, InvalidArgumentException {
    List<ObjectReference> objectReferences = task.getObjectReferences();

    if (objectReferences != null) {
      for (ObjectReference objectReference : objectReferences) {
        ObjectReferenceImpl objectReferenceImpl = (ObjectReferenceImpl) objectReference;
        initObjectReference(objectReferenceImpl, task);
        ObjectReference.validate(objectReferenceImpl, "ObjectReference", "Task");
        try {
          // System.out.println(objectReference.toString());
          objectReferenceMapper.insert(objectReferenceImpl);
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "TaskService.createTask() for TaskId={} INSERTED an object reference={}.",
                task.getId(),
                objectReference);
          }
        } catch (PersistenceException e) {
          throw new ObjectReferencePersistenceException(objectReference.getId(), task.getId(), e);
        }
      }
    }
  }

  private void initObjectReference(ObjectReference objectReference, Task newTask) {
    if (objectReference.getTaskId() == null) {
      objectReference.setTaskId(newTask.getId());
    }
    if (objectReference.getId() == null) {
      objectReference.setId(IdGenerator.generateWithPrefix(IdGenerator.ID_PREFIX_OBJECT_REFERENCE));
    }
  }
}
