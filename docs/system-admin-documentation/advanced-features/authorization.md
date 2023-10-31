Authorization
=============

Authorization is a logic that decides whether a given user is allowed to perform a given operation on a given resource. In openBIS authorization decides if objects like spaces, projects, experiments, samples, datasets, materials can be created, read, updated or deleted by a given user.

Similar to other IT systems, openBIS access rights can be defined only for groups of resources rather than for each individual resource separately. In openBIS the purpose of such groups is served by spaces and projects. It means that an openBIS user can be given access to a specific space or a specific project (and all the entities that belong to that space or to that project) but cannot be given access just to a single experiment, sample or dataset from that space or that project.

In JIRA for instance, a user can be given access to a project and as a consequence to all issues in that project, but cannot be given access just to a single issue. Therefore in JIRA, it is a project that serves the purpose of a group of resources access rights can be defined for.

Apart from access to a space or a project, a user can be given openBIS instance access rights. With such rights a user can access any space or any project within that openBIS installation.

Having defined the 3 scopes (i.e. instance, space and project), we need to learn how to control what operations a user can perform on entities that belong to these scopes. This aspect in openBIS is controlled with "roles". There are 4 roles available:

    OBSERVER - can see objects in a given scope
    USER - as OBSERVER + can create/update objects in a given scope
    POWER_USER -  as USER + can delete objects in a given scope
    ADMIN - as POWER_USER + update/delete the scope itself

The above roles together with instance, space and project scopes that we have defined earlier give us the following combinations:

    PROJECT_OBSERVER - can see the project and all the entities that belong to the project
    PROJECT_USER - as PROJECT_OBSERVER + can create/update entities in the project
    PROJECT_POWER_USER - as PROJECT_USER + can delete entities in the project
    PROJECT_ADMIN - as PROJECT_POWER_USER + can update/delete the project
    SPACE_OBSERVER - can see the space and all the entities that belong to the space
    SPACE_USER - as SPACE_OBSERVER + can create/update entities in the space
    SPACE_POWER_USER - as SPACE_USER + can delete entities in the space
    SPACE_ADMIN - as SPACE_POWER_USER + can update/delete the space
    INSTANCE_OBSERVER - can see everything
    INSTANCE_ADMIN - can do everything

Please note that instance scope can be combined only with OBSERVER and ADMIN roles.

WARNING: The project scope is disabled by default. To enable it for all users you have to change openBIS service.properties as follows:

    authorization.project-level.enabled = true
    authorization.project-level.users = .*

The "enabled" property controls whether the project scope can be used in general, while the "users" property defines exactly which users can use it. Setting "enabled" property to "true" will only make the project roles appear in "Roles" configuration tool in openBIS generic UI. These roles can be then assigned to users and saved. Still these roles won't be used until a name of the user they are defined for matches the "users" regexp.

Last part of the openBIS authorization puzzle are users and user groups. So far we always assumed that a scope and a role will be directly assigned to a user, e.g. "John Doe" is an ADMIN of space "TEST". Such an approach is absolutely fine and works great until the number of users we have to manage is relatively small. As the user base grows and so the maintenance overhead, it becomes handy to find users with the same access rights, put them into a user group and assign the rights to the user group rather than to each individual user. This way by simply assigning a user to a group we give him/her all the rights that are defined for that group. It leads to a simpler, more consistent and easier to maintain configuration.