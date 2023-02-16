/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import java.util.Collection;
import java.util.Map;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
public abstract class ObjectWithContainerTypesFactoryAbstract<T> extends ObjectFactory<T>
{

    public static final String ITEM_INTEGER = "itemInteger";

    public static final Integer ITEM_INTEGER_VALUE = Integer.valueOf(123);

    public static final String ITEM_STRING = "itemString";

    public static final String ITEM_STRING_VALUE = "abc";

    public static final String ITEM_WITH_KNOWN_TYPE = "itemWithKnownType";

    public static final String ITEM_WITH_KNOWN_UNIQUE_CLASS = "itemWithKnownUniqueClass";

    public static final String ITEM_WITH_RENAMED_PROPERTIES = "itemWithRenamedProperties";

    public static final String ITEM_WITH_IGNORED_PROPERTIES = "itemWithIgnoredProperties";

    public static final String ITEM_WITH_DATE_TYPES = "itemWithDateTypes";

    public static final String ITEM_WITH_ENUM_TYPES = "itemWithEnumTypes";

    public static final String ITEM_WITH_NESTED_TYPES = "itemWithNestedTypes";

    public static final String ITEM_WITH_PRIMITIVE_TYPES = "itemWithPrimitiveTypes";

    public static final String ITEM_WITH_PRIVATE_ACCESS = "itemWithPrivateAccess";

    public static final String ITEM_WITH_UNKNOWN_TYPE = "itemWithUnknownType";

    public static final String ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS =
            "itemWithKnownButNotUniqueClass";

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <C extends Collection> C addObjectsToSerialize(C collection, boolean specific)
    {
        collection.add(new ObjectWithKnownTypeFactory().createObjectToSerialize());
        collection.add(new ObjectWithKnownUniqueClassFactory().createObjectToSerialize());
        if (!specific)
        {
            collection.add(ITEM_STRING_VALUE);
            collection.add(ITEM_INTEGER_VALUE);
            collection.add(new ObjectWithKnownButNotUniqueClassFactory().createObjectToSerialize());
            collection.add(new ObjectWithUnknownTypeFactory().createObjectToSerialize());
            collection.add(new ObjectWithPrivateAccessFactory().createObjectToSerialize());
            collection.add(new ObjectWithPrimitiveTypesFactory().createObjectToSerialize());
            collection.add(new ObjectWithNestedTypesFactory().createObjectToSerialize());
            collection.add(new ObjectWithEnumTypesFactory().createObjectToSerialize());
            collection.add(new ObjectWithDateTypesFactory().createObjectToSerialize());
            collection.add(new ObjectWithIgnoredPropertiesFactory().createObjectToSerialize());
            collection.add(new ObjectWithRenamedPropertiesFactory().createObjectToSerialize());
        }
        return collection;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <M extends Map> M putObjectsToSerialize(M map, boolean specific)
    {
        map.put(ITEM_WITH_KNOWN_TYPE, new ObjectWithKnownTypeFactory().createObjectToSerialize());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS,
                new ObjectWithKnownUniqueClassFactory().createObjectToSerialize());
        if (!specific)
        {
            map.put(ITEM_STRING, ITEM_STRING_VALUE);
            map.put(ITEM_INTEGER, ITEM_INTEGER_VALUE);
            map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                    new ObjectWithKnownButNotUniqueClassFactory().createObjectToSerialize());
            map.put(ITEM_WITH_UNKNOWN_TYPE,
                    new ObjectWithUnknownTypeFactory().createObjectToSerialize());
            map.put(ITEM_WITH_PRIVATE_ACCESS,
                    new ObjectWithPrivateAccessFactory().createObjectToSerialize());
            map.put(ITEM_WITH_PRIMITIVE_TYPES,
                    new ObjectWithPrimitiveTypesFactory().createObjectToSerialize());
            map.put(ITEM_WITH_NESTED_TYPES,
                    new ObjectWithNestedTypesFactory().createObjectToSerialize());
            map.put(ITEM_WITH_ENUM_TYPES,
                    new ObjectWithEnumTypesFactory().createObjectToSerialize());
            map.put(ITEM_WITH_DATE_TYPES,
                    new ObjectWithDateTypesFactory().createObjectToSerialize());
            map.put(ITEM_WITH_IGNORED_PROPERTIES,
                    new ObjectWithIgnoredPropertiesFactory().createObjectToSerialize());
            map.put(ITEM_WITH_RENAMED_PROPERTIES,
                    new ObjectWithRenamedPropertiesFactory().createObjectToSerialize());
        }
        return map;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <C extends Collection> C addExpectedMapsAfterSerialization(C collection,
            ObjectCounter objectCounter, boolean specific)
    {
        collection.add(new ObjectWithKnownTypeFactory()
                .createExpectedMapAfterSerialization(objectCounter));
        collection.add(new ObjectWithKnownUniqueClassFactory()
                .createExpectedMapAfterSerialization(objectCounter));
        if (!specific)
        {
            collection.add(ITEM_STRING_VALUE);
            collection.add(ITEM_INTEGER_VALUE);
            collection.add(new ObjectWithKnownButNotUniqueClassFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithUnknownTypeFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithPrivateAccessFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithPrimitiveTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithNestedTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithEnumTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithDateTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithIgnoredPropertiesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            collection.add(new ObjectWithRenamedPropertiesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
        }
        return collection;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <M extends Map> M putExpectedMapsAfterSerialization(M map,
            ObjectCounter objectCounter, boolean specific)
    {
        map.put(ITEM_WITH_KNOWN_TYPE,
                new ObjectWithKnownTypeFactory().createExpectedMapAfterSerialization(objectCounter));
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS, new ObjectWithKnownUniqueClassFactory()
                .createExpectedMapAfterSerialization(objectCounter));
        if (!specific)
        {
            map.put(ITEM_STRING, ITEM_STRING_VALUE);
            map.put(ITEM_INTEGER, ITEM_INTEGER_VALUE);
            map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                    new ObjectWithKnownButNotUniqueClassFactory()
                            .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_UNKNOWN_TYPE, new ObjectWithUnknownTypeFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_PRIVATE_ACCESS, new ObjectWithPrivateAccessFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_PRIMITIVE_TYPES, new ObjectWithPrimitiveTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_NESTED_TYPES, new ObjectWithNestedTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_ENUM_TYPES, new ObjectWithEnumTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_DATE_TYPES, new ObjectWithDateTypesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_IGNORED_PROPERTIES, new ObjectWithIgnoredPropertiesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
            map.put(ITEM_WITH_RENAMED_PROPERTIES, new ObjectWithRenamedPropertiesFactory()
                    .createExpectedMapAfterSerialization(objectCounter));
        }
        return map;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <C extends Collection> C addMapsToDeserialize(C collection,
            ObjectCounter objectCounter, ObjectType objectType, boolean specific)
    {
        collection.add(new ObjectWithKnownTypeFactory().createMapToDeserialize(objectCounter,
                objectType));
        collection.add(new ObjectWithKnownUniqueClassFactory().createMapToDeserialize(
                objectCounter, objectType));
        if (!specific)
        {
            collection.add(ITEM_STRING_VALUE);
            collection.add(ITEM_INTEGER_VALUE);
            collection.add(new ObjectWithKnownButNotUniqueClassFactory().createMapToDeserialize(
                    objectCounter, objectType));
            collection.add(new ObjectWithUnknownTypeFactory().createMapToDeserialize(objectCounter,
                    objectType));
            collection.add(new ObjectWithPrivateAccessFactory().createMapToDeserialize(
                    objectCounter, objectType));
            collection.add(new ObjectWithPrimitiveTypesFactory().createMapToDeserialize(
                    objectCounter, objectType));
            collection.add(new ObjectWithNestedTypesFactory().createMapToDeserialize(objectCounter,
                    objectType));
            collection.add(new ObjectWithEnumTypesFactory().createMapToDeserialize(objectCounter,
                    objectType));
            collection.add(new ObjectWithDateTypesFactory().createMapToDeserialize(objectCounter,
                    objectType));
            collection.add(new ObjectWithIgnoredPropertiesFactory().createMapToDeserialize(
                    objectCounter, objectType));
            collection.add(new ObjectWithRenamedPropertiesFactory().createMapToDeserialize(
                    objectCounter, objectType));
        }
        return collection;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <M extends Map> M putMapsToDeserialize(M map, ObjectCounter objectCounter,
            ObjectType objectType, boolean specific)
    {
        map.put(ITEM_WITH_KNOWN_TYPE,
                new ObjectWithKnownTypeFactory().createMapToDeserialize(objectCounter, objectType));
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS, new ObjectWithKnownUniqueClassFactory()
                .createMapToDeserialize(objectCounter, objectType));
        if (!specific)
        {
            map.put(ITEM_STRING, ITEM_STRING_VALUE);
            map.put(ITEM_INTEGER, ITEM_INTEGER_VALUE);
            map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                    new ObjectWithKnownButNotUniqueClassFactory().createMapToDeserialize(
                            objectCounter, objectType));
            map.put(ITEM_WITH_UNKNOWN_TYPE, new ObjectWithUnknownTypeFactory()
                    .createMapToDeserialize(objectCounter, objectType));
            map.put(ITEM_WITH_PRIVATE_ACCESS, new ObjectWithPrivateAccessFactory()
                    .createMapToDeserialize(objectCounter, objectType));
            map.put(ITEM_WITH_PRIMITIVE_TYPES, new ObjectWithPrimitiveTypesFactory()
                    .createMapToDeserialize(objectCounter, objectType));
            map.put(ITEM_WITH_NESTED_TYPES, new ObjectWithNestedTypesFactory()
                    .createMapToDeserialize(objectCounter, objectType));
            map.put(ITEM_WITH_ENUM_TYPES, new ObjectWithEnumTypesFactory().createMapToDeserialize(
                    objectCounter, objectType));
            map.put(ITEM_WITH_DATE_TYPES, new ObjectWithDateTypesFactory().createMapToDeserialize(
                    objectCounter, objectType));
            map.put(ITEM_WITH_IGNORED_PROPERTIES, new ObjectWithIgnoredPropertiesFactory()
                    .createMapToDeserialize(objectCounter, objectType));
            map.put(ITEM_WITH_RENAMED_PROPERTIES, new ObjectWithRenamedPropertiesFactory()
                    .createMapToDeserialize(objectCounter, objectType));
        }
        return map;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <C extends Collection> C addExpectedObjectsAfterDeserialization(C collection,
            boolean specific)
    {
        collection.add(new ObjectWithKnownTypeFactory().createExpectedObjectAfterDeserialization());
        collection.add(new ObjectWithKnownUniqueClassFactory()
                .createExpectedObjectAfterDeserialization());
        if (!specific)
        {
            collection.add(ITEM_STRING_VALUE);
            collection.add(ITEM_INTEGER_VALUE);
            collection.add(new ObjectWithKnownButNotUniqueClassFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithUnknownTypeFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithPrivateAccessFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithPrimitiveTypesFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithNestedTypesFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithEnumTypesFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithDateTypesFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithIgnoredPropertiesFactory()
                    .createExpectedObjectAfterDeserialization());
            collection.add(new ObjectWithRenamedPropertiesFactory()
                    .createExpectedObjectAfterDeserialization());
        }
        return collection;
    }

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    protected <M extends Map> M putExpectedObjectsAfterDeserialization(M map, boolean specific)
    {
        map.put(ITEM_WITH_KNOWN_TYPE, new ObjectWithKnownTypeFactory().createObjectToSerialize());
        map.put(ITEM_WITH_KNOWN_UNIQUE_CLASS,
                new ObjectWithKnownUniqueClassFactory().createExpectedObjectAfterDeserialization());
        if (!specific)
        {
            map.put(ITEM_STRING, ITEM_STRING_VALUE);
            map.put(ITEM_INTEGER, ITEM_INTEGER_VALUE);
            map.put(ITEM_WITH_KNOWN_BUT_NOT_UNIQUE_CLASS,
                    new ObjectWithKnownButNotUniqueClassFactory()
                            .createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_UNKNOWN_TYPE,
                    new ObjectWithUnknownTypeFactory().createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_PRIVATE_ACCESS,
                    new ObjectWithPrivateAccessFactory().createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_PRIMITIVE_TYPES, new ObjectWithPrimitiveTypesFactory()
                    .createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_NESTED_TYPES,
                    new ObjectWithNestedTypesFactory().createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_ENUM_TYPES,
                    new ObjectWithEnumTypesFactory().createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_DATE_TYPES,
                    new ObjectWithDateTypesFactory().createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_IGNORED_PROPERTIES, new ObjectWithIgnoredPropertiesFactory()
                    .createExpectedObjectAfterDeserialization());
            map.put(ITEM_WITH_RENAMED_PROPERTIES, new ObjectWithRenamedPropertiesFactory()
                    .createExpectedObjectAfterDeserialization());
        }
        return map;
    }

}
