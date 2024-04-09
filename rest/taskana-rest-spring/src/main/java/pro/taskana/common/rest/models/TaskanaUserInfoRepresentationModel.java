package pro.taskana.common.rest.models;

import java.util.ArrayList;
import java.util.List;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.NonNull;
import pro.taskana.common.api.TaskanaRole;

/** EntityModel class for user information. */
public class TaskanaUserInfoRepresentationModel
    extends RepresentationModel<TaskanaUserInfoRepresentationModel> {

  /** The user Id of the current user. */
  private String userId;
  /** All groups the current user is a member of. */
  private List<String> groupIds = new ArrayList<>();
  /** All permissions the current user has. */
  private List<String> permissionIds = new ArrayList<>();
  /** All taskana roles the current user fulfills. */
  private List<TaskanaRole> roles = new ArrayList<>();

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public List<String> getGroupIds() {
    return groupIds;
  }

  public void setGroupIds(List<String> groupIds) {
    this.groupIds = groupIds;
  }

  public List<String> getPermissionIds() {
    return permissionIds;
  }

  public void setPermissionIds(List<String> permissionIds) {
    this.permissionIds = permissionIds;
  }

  public List<TaskanaRole> getRoles() {
    return roles;
  }

  public void setRoles(List<TaskanaRole> roles) {
    this.roles = roles;
  }

  @Override
  public @NonNull String toString() {
    return "TaskanaUserInfoRepresentationModel [userId="
        + userId
        + ", groupIds="
        + groupIds
        + ", permissionIds="
        + permissionIds
        + ", roles="
        + roles
        + "]";
  }
}
