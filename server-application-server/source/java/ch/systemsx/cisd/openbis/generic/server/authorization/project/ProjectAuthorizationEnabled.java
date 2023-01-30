/*
 * Copyright 2017 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.authorization.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.IObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.role.IRole;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.IRolesProvider;

/**
 * @author pkupczyk
 */
public class ProjectAuthorizationEnabled<O> implements IProjectAuthorization<O>
{

    private IAuthorizationDataProvider dataProvider;

    private IRolesProvider rolesProvider;

    private IObjectsProvider<O> objectsProvider;

    private Collection<IObject<O>> objectsWithAccess;

    private Collection<IObject<O>> objectsWithoutAccess;

    private boolean initialized;

    ProjectAuthorizationEnabled(IAuthorizationDataProvider dataProvider, IRolesProvider rolesProvider, IObjectsProvider<O> objectsProvider)
    {
        this.dataProvider = dataProvider;
        this.rolesProvider = rolesProvider;
        this.objectsProvider = objectsProvider;
    }

    @Override
    public Collection<O> getObjectsWithAccess()
    {
        init();

        Collection<O> results = new ArrayList<O>();
        for (IObject<O> objectWithAccess : objectsWithAccess)
        {
            results.add(objectWithAccess.getOriginalObject());
        }
        return results;
    }

    @Override
    public Collection<O> getObjectsWithoutAccess()
    {
        init();

        Collection<O> results = new ArrayList<O>();
        for (IObject<O> objectWithoutAccess : objectsWithoutAccess)
        {
            results.add(objectWithoutAccess.getOriginalObject());
        }
        return results;
    }

    private void init()
    {
        if (initialized)
        {
            return;
        }

        objectsWithAccess = new ArrayList<IObject<O>>();
        objectsWithoutAccess = new ArrayList<IObject<O>>();

        Collection<IObject<O>> objects = objectsProvider.getObjects(dataProvider);

        if (objects == null)
        {
            objects = Collections.emptyList();
        }

        Collection<IRole> roles = rolesProvider.getRoles(dataProvider);

        if (roles == null)
        {
            roles = Collections.emptyList();
        }

        Map<ProjectKey, Collection<IObject<O>>> projectToObjectsMap = new LinkedHashMap<ProjectKey, Collection<IObject<O>>>();

        for (IObject<O> object : objects)
        {
            if (object.getProject() == null)
            {
                objectsWithoutAccess.add(object);
            } else
            {
                ProjectKey projectKey = new ProjectKey(object.getProject());

                Collection<IObject<O>> projectObjects = projectToObjectsMap.get(projectKey);
                if (projectObjects == null)
                {
                    projectObjects = new ArrayList<IObject<O>>();
                    projectToObjectsMap.put(projectKey, projectObjects);
                }

                projectObjects.add(object);
            }
        }

        for (Map.Entry<ProjectKey, Collection<IObject<O>>> entry : projectToObjectsMap.entrySet())
        {
            IProject project = entry.getKey().getProject();
            Collection<IObject<O>> projectObjects = entry.getValue();

            if (hasAccess(roles, project))
            {
                objectsWithAccess.addAll(projectObjects);
            } else
            {
                objectsWithoutAccess.addAll(projectObjects);
            }
        }

        initialized = true;
    }

    private boolean hasAccess(Collection<IRole> roles, IProject project)
    {
        for (IRole role : roles)
        {
            if (hasAccess(role, project))
            {
                return true;
            }
        }

        return false;
    }

    private boolean hasAccess(IRole role, IProject project)
    {
        IProject roleProject = role.getProject();

        if (roleProject == null)
        {
            return false;
        }

        boolean idNN = areNotNull(project.getId(), roleProject.getId());
        boolean permIdNN = areNotNull(project.getPermId(), roleProject.getPermId());
        boolean identifierNN = areNotNull(project.getIdentifier(), roleProject.getIdentifier());

        boolean idEqual = areEqual(project.getId(), roleProject.getId());
        boolean permIdEqual = areEqual(project.getPermId(), roleProject.getPermId());
        boolean identifierEqual = areEqual(project.getIdentifier(), roleProject.getIdentifier());

        if (idNN && permIdNN)
        {
            return idEqual && permIdEqual;
        }
        if (idNN)
        {
            return idEqual;
        }
        if (permIdNN)
        {
            return permIdEqual;
        }
        if (identifierNN)
        {
            return identifierEqual;
        }

        return false;
    }

    private static class ProjectKey
    {

        private IProject project;

        public ProjectKey(IProject project)
        {
            if (project == null)
            {
                throw new IllegalArgumentException("Project cannot be null");
            }
            this.project = project;
        }

        public IProject getProject()
        {
            return project;
        }

        @Override
        public int hashCode()
        {
            Long id = project.getId();

            if (id != null)
            {
                return id.hashCode();
            }

            String permId = project.getPermId();

            if (permId != null)
            {
                return permId.hashCode();
            }

            String identifier = project.getIdentifier();

            if (identifier != null)
            {
                return identifier.hashCode();
            }

            return 0;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            ProjectKey other = (ProjectKey) obj;

            return areEqual(project.getId(), other.project.getId()) && areEqual(project.getPermId(), other.project.getPermId())
                    && areEqual(project.getIdentifier(), other.project.getIdentifier());
        }

    }

    private static boolean areEqual(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        } else
        {
            return o1.equals(o2);
        }
    }

    private static boolean areNotNull(Object o1, Object o2)
    {
        return o1 != null && o2 != null;
    }

}
