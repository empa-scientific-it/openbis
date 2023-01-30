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

package ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.IObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.object.Object;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.data.project.IProject;

/**
 * @author pkupczyk
 */
public abstract class SingleObjectProvider<O> implements IObjectsProvider<O>
{

    private O originalObject;

    public SingleObjectProvider(O originalObject)
    {
        this.originalObject = originalObject;
    }

    @SuppressWarnings("hiding")
    protected abstract IProject createProject(IAuthorizationDataProvider dataProvider, O originalObject);

    @Override
    public Collection<O> getOriginalObjects()
    {
        return Arrays.asList(originalObject);
    }

    @Override
    public Collection<IObject<O>> getObjects(IAuthorizationDataProvider dataProvider)
    {
        IProject project = null;

        if (originalObject != null)
        {
            project = createProject(dataProvider, originalObject);
        }

        return Arrays.<IObject<O>> asList(new Object<O>(originalObject, project));
    }

}
