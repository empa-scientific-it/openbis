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
import java.util.HashSet;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@SuppressWarnings(
{ "rawtypes", "unchecked" })
public class ObjectWithNestedMapsInCollectionFactory extends ObjectFactory<Collection>
{

    @Override
    public Collection createObjectToSerialize()
    {
        Collection collection = new HashSet();
        collection.add(new ObjectWithContainerTypesFactory().createObjectToSerialize());
        return collection;
    }

    @Override
    public Object createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        Collection collection = new HashSet();
        collection.add(new ObjectWithContainerTypesFactory()
                .createExpectedMapAfterSerialization(objectCounter));
        return collection;
    }

    @Override
    public Object createMapToDeserialize(ObjectCounter objectCounter, ObjectType objectType)
    {
        Collection collection = new HashSet();
        collection.add(new ObjectWithContainerTypesFactory().createMapToDeserialize(objectCounter,
                objectType));
        return collection;
    }

    @Override
    public Collection createExpectedObjectAfterDeserialization()
    {
        Collection collection = new ArrayList();
        collection.add(new ObjectWithContainerTypesFactory()
                .createExpectedObjectAfterDeserialization());
        return collection;
    }

}
