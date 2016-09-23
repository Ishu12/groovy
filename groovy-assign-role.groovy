import hudson.model.*
import jenkins.model.Jenkins
import hudson.*
import hudson.security.*
import java.util.*
import com.michelin.cio.hudson.plugins.rolestrategy.*
import java.lang.reflect.*

def ldapGroupName = "ishuallawadhi@in.ibm.com"
def projectPrefix = "AutoGrill-User"
  
def authStrategy = Hudson.instance.getAuthorizationStrategy()

if(authStrategy instanceof RoleBasedAuthorizationStrategy){
  RoleBasedAuthorizationStrategy roleAuthStrategy = (RoleBasedAuthorizationStrategy) authStrategy

  // Make constructors available
  Constructor[] constrs = Role.class.getConstructors();
  for (Constructor<?> c : constrs) {
    c.setAccessible(true);
  }
  // Make the method assignRole accessible
  Method assignRoleMethod = RoleBasedAuthorizationStrategy.class.getDeclaredMethod("assignRole", String.class, Role.class, String.class);
assignRoleMethod.setAccessible(true);

  // Create role
  Set<Permission> permissions = new HashSet<Permission>();
  permissions.add(Permission.fromId("hudson.model.Item.Read"));
  permissions.add(Permission.fromId("hudson.model.Item.Build"));
  permissions.add(Permission.fromId("hudson.model.Item.Workspace"));
  permissions.add(Permission.fromId("hudson.model.Item.Cancel"));
  // The release permission is only available when the release plugin is installed
  String releasePermission = Permission.fromId("hudson.model.Item.Release");
  if (releasePermission != null) {
    permissions.add(releasePermission);
  }
  permissions.add(Permission.fromId("hudson.model.Run.Delete"));
  permissions.add(Permission.fromId("hudson.model.Run.Update"));
  Role newRole = new Role(projectPrefix, projectPrefix + ".*", permissions);
  roleAuthStrategy.addRole(RoleBasedAuthorizationStrategy.PROJECT, newRole);

  // assign the role
  roleAuthStrategy.assignRole(RoleBasedAuthorizationStrategy.PROJECT, newRole, ldapGroupName);
  roleAuthStrategy.assignRole(RoleBasedAuthorizationStrategy.GLOBAL, newRole, ldapGroupName);
  
  println "OK"
}
else {
  println "Role Strategy Plugin not found!"
}
