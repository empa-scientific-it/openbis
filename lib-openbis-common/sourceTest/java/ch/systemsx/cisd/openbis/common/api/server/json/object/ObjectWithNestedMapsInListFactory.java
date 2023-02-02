/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.api.server.json.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class ObjectWithNestedMapsInListFactory extends ObjectFactory<List>
{

    @Override
    public List createObjectToSerialize()
    {
        List list = new ArrayList();
        list.add(new ObjectWithContainerTypesFactory().createObjectToSerialize());
        return list;
    }

    @Override
    public Object createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        List list = new ArrayList();
        list.add(new ObjectWithContainerTypesFactory()
                .createExpectedMapAfterSerialization(objectCounter));
        return list;
    }

    @Override
    public Object createMapToDeserialize(ObjectCounter objectCounter, ObjectType objectType)
    {
        Collection list = new ArrayList();
        list.add(new ObjectWithContainerTypesFactory().createMapToDeserialize(objectCounter,
                objectType));
        return list;
    }

    @Override
    public List createExpectedObjectAfterDeserialization()
    {
        List list = new ArrayList();
        list.add(new ObjectWithContainerTypesFactory().createExpectedObjectAfterDeserialization());
        return list;
    }

}
