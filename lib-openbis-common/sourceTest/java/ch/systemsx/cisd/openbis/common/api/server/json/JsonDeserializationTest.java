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

package ch.systemsx.cisd.openbis.common.api.server.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.openbis.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectNestedChildFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectNestedFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithContainerTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithDateTypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithDateTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithEnumTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithIgnoredProperties;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithIgnoredPropertiesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInCollectionFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInListFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInMapFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrimitiveTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrivateAccess;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrivateAccessFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithRenamedProperties;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithRenamedPropertiesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithReusedReferences;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithReusedReferencesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithSelfReference;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithSelfReferenceFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeAAFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeAFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeBFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeButNoSubtypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeButNoSubtypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeC;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeCFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeInterface1;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeInterface2;

/**
 * @author pkupczyk
 */
public class JsonDeserializationTest
{

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface1() throws Exception
    {
        testDeserialize(new ObjectWithTypeFactory(), ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface2() throws Exception
    {
        testDeserialize(new ObjectWithTypeFactory(), ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootType() throws Exception
    {
        testDeserialize(new ObjectWithTypeFactory(), ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserialize(new ObjectWithTypeAFactory(), ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserialize(new ObjectWithTypeAFactory(), ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAFactory(), ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAFactory(), ObjectWithTypeA.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithFirstLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAFactory(), ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserialize(new ObjectWithTypeAAFactory(), ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserialize(new ObjectWithTypeAAFactory(), ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAAFactory(), ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAAFactory(), ObjectWithTypeA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserialize(new ObjectWithTypeAAFactory(), ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithNestedRootTypeToNestedRootType() throws Exception
    {
        testDeserialize(new ObjectNestedFactory(), ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedRootType() throws Exception
    {
        testDeserialize(new ObjectNestedChildFactory(), ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedSubType() throws Exception
    {
        testDeserialize(new ObjectNestedChildFactory(), ObjectNestedChild.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithPrimitiveTypes() throws Exception
    {
        testDeserialize(new ObjectWithPrimitiveTypesFactory(), ObjectWithPrimitiveTypes.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithNestedTypes() throws Exception
    {
        testDeserialize(new ObjectWithNestedTypesFactory(), ObjectWithNestedTypes.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithEnumTypes() throws Exception
    {
        testDeserialize(new ObjectWithEnumTypesFactory(), ObjectWithEnumTypes.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithDateTypes() throws Exception
    {
        testDeserialize(new ObjectWithDateTypesFactory(), ObjectWithDateTypes.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithContainerTypes() throws Exception
    {
        testDeserialize(new ObjectWithContainerTypesFactory(), ObjectWithContainerTypes.class);
    }

    @Test
    public void testDeserializeJsonWithIgnoredProperties() throws Exception
    {
        testDeserialize(new ObjectWithIgnoredPropertiesFactory(), ObjectWithIgnoredProperties.class);
    }

    @Test
    public void testDeserializeJsonWithRenamedProperties() throws Exception
    {
        testDeserialize(new ObjectWithRenamedPropertiesFactory(), ObjectWithRenamedProperties.class);
    }

    @Test
    public void testDeserializeJsonWithPrivateAccess() throws Exception
    {
        testDeserialize(new ObjectWithPrivateAccessFactory(), ObjectWithPrivateAccess.class);
    }

    @Test
    public void testDeserializeJsonWithSelfReference() throws Exception
    {
        testDeserialize(new ObjectWithSelfReferenceFactory(), ObjectWithSelfReference.class);
    }

    @Test
    public void testDeserializeJsonWithReusedReferences() throws Exception
    {
        testDeserialize(new ObjectWithReusedReferencesFactory(), ObjectWithReusedReferences.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithoutTypeToPolymorphicType() throws Exception
    {
        Object object =
                new ObjectWithTypeFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.NONE);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToPolymorphicType() throws Exception
    {
        testDeserialize(new ObjectWithTypeFactory(), ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithoutTypeToNotPolymorphicType() throws Exception
    {
        Object object =
                new ObjectWithTypeButNoSubtypesFactory().createMapToDeserialize(
                        new ObjectCounter(), ObjectType.NONE);
        testDeserialize(object, ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToNotPolymorphicType() throws Exception
    {
        testDeserialize(new ObjectWithTypeButNoSubtypesFactory(), ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithLegacyClassDefinedInClassMapping() throws Exception
    {
        Object object =
                new ObjectWithTypeAFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.CLASS);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMapping() throws Exception
    {
        Object object =
                new ObjectWithTypeCFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.CLASS);
        testDeserialize(object, ObjectWithType.class);
    }

    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMappingButMatchingType()
            throws Exception
    {
        Map<String, Object> map =
                (Map<String, Object>) new ObjectWithTypeCFactory().createMapToDeserialize(
                        new ObjectCounter(), ObjectType.NONE);
        map.put(JsonConstants.getLegacyClassField(), ObjectWithTypeCFactory.TYPE);
        testDeserialize(map, ObjectWithTypeC.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInDifferentInheritanceTrees()
            throws Exception
    {
        Object object =
                new ObjectWithTypeAFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.CLASS);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInSameInheritanceTree()
            throws Exception
    {
        Object object =
                new ObjectWithTypeCFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.CLASS);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInDifferentInheritanceTrees()
            throws Exception
    {
        Object object =
                new ObjectWithTypeAFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.TYPE);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInSameInheritanceTree() throws Exception
    {
        Object object =
                new ObjectWithTypeBFactory().createMapToDeserialize(new ObjectCounter(),
                        ObjectType.TYPE);
        testDeserialize(object, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInCollection() throws Exception
    {
        testDeserialize(new ObjectWithNestedMapsInCollectionFactory(), Collection.class);
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInMap() throws Exception
    {
        testDeserialize(new ObjectWithNestedMapsInMapFactory(), Map.class);
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInList() throws Exception
    {
        testDeserialize(new ObjectWithNestedMapsInListFactory(), List.class);
    }

    private void testDeserialize(ObjectFactory<?> factory, Class<?> rootClass) throws Exception
    {
        Object expected = factory.createExpectedObjectAfterDeserialization();

        for (ObjectType objectType : ObjectType.values())
        {
            if (ObjectType.NONE.equals(objectType))
            {
                continue;
            }
            Object object = factory.createMapToDeserialize(new ObjectCounter(), objectType);
            Object result = testDeserialize(object, rootClass);
            Assert.assertEquals(result, expected);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T testDeserialize(Object object, Class<?> rootClass) throws Exception
    {
        String jsonFromObject = new ObjectMapper().writeValueAsString(object);
        Object objectFromJson = new JsonTestObjectMapper().readValue(jsonFromObject, rootClass);
        return (T) objectFromJson;
    }
}
