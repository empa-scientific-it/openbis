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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@SuppressWarnings(
{ "unchecked", "rawtypes" })
public class ObjectWithContainerTypesFactory extends
        ObjectWithContainerTypesFactoryAbstract<ObjectWithContainerTypes>
{

    public static final String TYPE = "ObjectWithContainerTypes";

    public static final String CLASS = ".LegacyObjectWithContainerTypes";

    public static final String COLLECTION_WITHOUT_TYPE = "collectionWithoutType";

    public static final String COLLECTION_WITH_OBJECT_TYPE = "collectionWithObjectType";

    public static final String COLLECTION_WITH_SPECIFIC_TYPE = "collectionWithSpecificType";

    public static final String LINKED_HASH_SET_WITHOUT_TYPE = "linkedHashSetWithoutType";

    public static final String LINKED_HASH_SET_WITH_OBJECT_TYPE = "linkedHashSetWithObjectType";

    public static final String LINKED_HASH_SET_WITH_SPECIFIC_TYPE = "linkedHashSetWithSpecificType";

    public static final String LIST_WITHOUT_TYPE = "listWithoutType";

    public static final String LIST_WITH_OBJECT_TYPE = "listWithObjectType";

    public static final String LIST_WITH_SPECIFIC_TYPE = "listWithSpecificType";

    public static final String LINKED_LIST_WITHOUT_TYPE = "linkedListWithoutType";

    public static final String LINKED_LIST_WITH_OBJECT_TYPE = "linkedListWithObjectType";

    public static final String LINKED_LIST_WITH_SPECIFIC_TYPE = "linkedListWithSpecificType";

    public static final String MAP_WITHOUT_TYPE = "mapWithoutType";

    public static final String MAP_WITH_OBJECT_TYPE = "mapWithObjectType";

    public static final String MAP_WITH_SPECIFIC_TYPE = "mapWithSpecificType";

    public static final String LINKED_HASH_MAP_WITHOUT_TYPE = "linkedHashMapWithoutType";

    public static final String LINKED_HASH_MAP_WITH_OBJECT_TYPE = "linkedHashMapWithObjectType";

    public static final String LINKED_HASH_MAP_WITH_SPECIFIC_TYPE = "linkedHashMapWithSpecificType";

    public static final String ARRAY_WITH_OBJECT_TYPE = "arrayWithObjectType";

    public static final String ARRAY_WITH_SPECIFIC_TYPE = "arrayWithSpecificType";

    @Override
    public ObjectWithContainerTypes createObjectToSerialize()
    {
        ObjectWithContainerTypes object = new ObjectWithContainerTypes();
        object.collectionWithoutType = addObjectsToSerialize(new ArrayList(), false);
        object.collectionWithObjectType = addObjectsToSerialize(new ArrayList(), false);
        object.collectionWithSpecificType = addObjectsToSerialize(new ArrayList(), true);
        object.linkedHashSetWithoutType = addObjectsToSerialize(new LinkedHashSet(), false);
        object.linkedHashSetWithObjectType = addObjectsToSerialize(new LinkedHashSet(), false);
        object.linkedHashSetWithSpecificType = addObjectsToSerialize(new LinkedHashSet(), true);
        object.listWithoutType = addObjectsToSerialize(new ArrayList(), false);
        object.listWithObjectType = addObjectsToSerialize(new ArrayList(), false);
        object.listWithSpecificType = addObjectsToSerialize(new ArrayList(), true);
        object.linkedListWithoutType = addObjectsToSerialize(new LinkedList(), false);
        object.linkedListWithObjectType = addObjectsToSerialize(new LinkedList(), false);
        object.linkedListWithSpecificType = addObjectsToSerialize(new LinkedList(), true);
        object.mapWithoutType = putObjectsToSerialize(new LinkedHashMap(), false);
        object.mapWithObjectType = putObjectsToSerialize(new LinkedHashMap(), false);
        object.mapWithSpecificType = putObjectsToSerialize(new LinkedHashMap(), true);
        object.linkedHashMapWithoutType = putObjectsToSerialize(new LinkedHashMap(), false);
        object.linkedHashMapWithObjectType = putObjectsToSerialize(new LinkedHashMap(), false);
        object.linkedHashMapWithSpecificType = putObjectsToSerialize(new LinkedHashMap(), true);

        Collection arrayWithObjectTypeItems = addObjectsToSerialize(new ArrayList(), false);
        Collection arrayWithSpecificTypeItems = addObjectsToSerialize(new ArrayList(), true);

        object.arrayWithObjectType =
                arrayWithObjectTypeItems.toArray(new Object[arrayWithObjectTypeItems.size()]);
        object.arrayWithSpecificType =
                (ObjectWithType[]) arrayWithSpecificTypeItems
                        .toArray(new ObjectWithType[arrayWithSpecificTypeItems.size()]);

        return object;
    }

    @Override
    public Object createExpectedMapAfterSerialization(ObjectCounter objectCounter)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, ObjectType.TYPE);
        map.putField(ARRAY_WITH_OBJECT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(ARRAY_WITH_SPECIFIC_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, true));
        map.putField(COLLECTION_WITH_OBJECT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(COLLECTION_WITH_SPECIFIC_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, true));
        map.putField(COLLECTION_WITHOUT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LINKED_HASH_MAP_WITH_OBJECT_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, false));
        map.putField(LINKED_HASH_MAP_WITH_SPECIFIC_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, true));
        map.putField(LINKED_HASH_MAP_WITHOUT_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, false));
        map.putField(LINKED_HASH_SET_WITH_OBJECT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LINKED_HASH_SET_WITH_SPECIFIC_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, true));
        map.putField(LINKED_HASH_SET_WITHOUT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LINKED_LIST_WITH_OBJECT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LINKED_LIST_WITH_SPECIFIC_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, true));
        map.putField(LINKED_LIST_WITHOUT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LIST_WITH_OBJECT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(LIST_WITH_SPECIFIC_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, true));
        map.putField(LIST_WITHOUT_TYPE,
                addExpectedMapsAfterSerialization(new ArrayList(), objectCounter, false));
        map.putField(MAP_WITH_OBJECT_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, false));
        map.putField(MAP_WITH_SPECIFIC_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, true));
        map.putField(MAP_WITHOUT_TYPE,
                putExpectedMapsAfterSerialization(new LinkedHashMap(), objectCounter, false));
        return map.toMap();
    }

    @Override
    public Object createMapToDeserialize(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(ARRAY_WITH_OBJECT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(ARRAY_WITH_SPECIFIC_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, true));
        map.putField(COLLECTION_WITH_OBJECT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(COLLECTION_WITH_SPECIFIC_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, true));
        map.putField(COLLECTION_WITHOUT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LINKED_HASH_MAP_WITH_OBJECT_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, false));
        map.putField(LINKED_HASH_MAP_WITH_SPECIFIC_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, true));
        map.putField(LINKED_HASH_MAP_WITHOUT_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, false));
        map.putField(LINKED_HASH_SET_WITH_OBJECT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LINKED_HASH_SET_WITH_SPECIFIC_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, true));
        map.putField(LINKED_HASH_SET_WITHOUT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LINKED_LIST_WITH_OBJECT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LINKED_LIST_WITH_SPECIFIC_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, true));
        map.putField(LINKED_LIST_WITHOUT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LIST_WITH_OBJECT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(LIST_WITH_SPECIFIC_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, true));
        map.putField(LIST_WITHOUT_TYPE,
                addMapsToDeserialize(new ArrayList(), objectCounter, objectType, false));
        map.putField(MAP_WITH_OBJECT_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, false));
        map.putField(MAP_WITH_SPECIFIC_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, true));
        map.putField(MAP_WITHOUT_TYPE,
                putMapsToDeserialize(new LinkedHashMap(), objectCounter, objectType, false));
        return map.toMap();
    }

    @Override
    public ObjectWithContainerTypes createExpectedObjectAfterDeserialization()
    {
        ObjectWithContainerTypes object = new ObjectWithContainerTypes();
        object.collectionWithObjectType =
                addExpectedObjectsAfterDeserialization(new ArrayList(), false);
        object.collectionWithSpecificType =
                addExpectedObjectsAfterDeserialization(new ArrayList(), true);
        object.collectionWithoutType =
                addExpectedObjectsAfterDeserialization(new ArrayList(), false);
        object.linkedHashSetWithoutType =
                addExpectedObjectsAfterDeserialization(new LinkedHashSet(), false);
        object.linkedHashSetWithObjectType =
                addExpectedObjectsAfterDeserialization(new LinkedHashSet(), false);
        object.linkedHashSetWithSpecificType =
                addExpectedObjectsAfterDeserialization(new LinkedHashSet(), true);
        object.listWithoutType = addExpectedObjectsAfterDeserialization(new ArrayList(), false);
        object.listWithObjectType = addExpectedObjectsAfterDeserialization(new ArrayList(), false);
        object.listWithSpecificType = addExpectedObjectsAfterDeserialization(new ArrayList(), true);
        object.linkedListWithoutType =
                addExpectedObjectsAfterDeserialization(new LinkedList(), false);
        object.linkedListWithObjectType =
                addExpectedObjectsAfterDeserialization(new LinkedList(), false);
        object.linkedListWithSpecificType =
                addExpectedObjectsAfterDeserialization(new LinkedList(), true);
        object.mapWithoutType = putExpectedObjectsAfterDeserialization(new HashMap(), false);
        object.mapWithObjectType = putExpectedObjectsAfterDeserialization(new HashMap(), false);
        object.mapWithSpecificType = putExpectedObjectsAfterDeserialization(new HashMap(), true);
        object.linkedHashMapWithoutType =
                putExpectedObjectsAfterDeserialization(new LinkedHashMap(), false);
        object.linkedHashMapWithObjectType =
                putExpectedObjectsAfterDeserialization(new LinkedHashMap(), false);
        object.linkedHashMapWithSpecificType =
                putExpectedObjectsAfterDeserialization(new LinkedHashMap(), true);

        Collection arrayWithObjectTypeItems =
                addExpectedObjectsAfterDeserialization(new ArrayList(), false);
        Collection arrayWithSpecificTypeItems =
                addExpectedObjectsAfterDeserialization(new ArrayList(), true);

        object.arrayWithObjectType =
                arrayWithObjectTypeItems.toArray(new Object[arrayWithObjectTypeItems.size()]);
        object.arrayWithSpecificType =
                (ObjectWithType[]) arrayWithSpecificTypeItems
                        .toArray(new ObjectWithType[arrayWithSpecificTypeItems.size()]);

        return object;
    }

}
