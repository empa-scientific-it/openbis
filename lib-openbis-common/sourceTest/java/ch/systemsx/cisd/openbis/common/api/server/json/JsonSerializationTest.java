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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectNestedChildFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectNestedFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithContainerTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithDateTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithEnumTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithIgnoredPropertiesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInCollectionFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInListFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedMapsInMapFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithNestedTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrimitiveTypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithPrivateAccessFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithRenamedPropertiesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithReusedReferencesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithSelfReferenceFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeAAFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeAFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeButNoSubtypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory;

/**
 * @author pkupczyk
 */
public class JsonSerializationTest
{
    @Test
    public void testSerializeRootType() throws Exception
    {
        testSerialize(new ObjectWithTypeFactory());
    }

    @Test
    public void testSerializeFirstLevelSubType() throws Exception
    {
        testSerialize(new ObjectWithTypeAFactory());
    }

    @Test
    public void testSerializeSecondLevelSubType() throws Exception
    {
        testSerialize(new ObjectWithTypeAAFactory());
    }

    @Test
    public void testSerializeNestedRootType() throws Exception
    {
        testSerialize(new ObjectNestedFactory());
    }

    @Test
    public void testSerializeNestedSubType() throws Exception
    {
        testSerialize(new ObjectNestedChildFactory());
    }

    @Test
    public void testSerializePolymorphicType() throws Exception
    {
        testSerialize(new ObjectWithTypeFactory());
    }

    @Test
    public void testSerializeNotPolymorphicType() throws Exception
    {
        testSerialize(new ObjectWithTypeButNoSubtypesFactory());
    }

    @Test
    public void testSerializeObjectWithPrimitiveTypes() throws Exception
    {
        testSerialize(new ObjectWithPrimitiveTypesFactory());
    }

    @Test
    public void testSerializeObjectWithNestedTypes() throws Exception
    {
        testSerialize(new ObjectWithNestedTypesFactory());
    }

    @Test
    public void testSerializeObjectWithEnumTypes() throws Exception
    {
        testSerialize(new ObjectWithEnumTypesFactory());
    }

    @Test
    public void testSerializeObjectWithDateTypes() throws Exception
    {
        testSerialize(new ObjectWithDateTypesFactory());
    }

    @Test
    public void testSerializeObjectWithIgnoredProperties() throws Exception
    {
        testSerialize(new ObjectWithIgnoredPropertiesFactory());
    }

    @Test
    public void testSerializeObjectWithRenamedProperties() throws Exception
    {
        testSerialize(new ObjectWithRenamedPropertiesFactory());
    }

    @Test
    public void testSerializeObjectWithPrivateAccess() throws Exception
    {
        testSerialize(new ObjectWithPrivateAccessFactory());
    }

    @Test
    public void testSerializeObjectWithContainerTypes() throws Exception
    {
        testSerialize(new ObjectWithContainerTypesFactory());
    }

    @Test
    public void testSerializeObjectWithNestedMapsInCollection() throws Exception
    {
        testSerialize(new ObjectWithNestedMapsInCollectionFactory());
    }

    @Test
    public void testSerializeObjectWithNestedMapsInMap() throws Exception
    {
        testSerialize(new ObjectWithNestedMapsInMapFactory());
    }

    @Test
    public void testSerializeObjectWithNestedMapsInList() throws Exception
    {
        testSerialize(new ObjectWithNestedMapsInListFactory());
    }

    @Test
    public void testSerializeObjectWithSelfReference() throws Exception
    {
        testSerialize(new ObjectWithSelfReferenceFactory());
    }

    @Test
    public void testSerializeObjectWithReusedReferences() throws Exception
    {
        testSerialize(new ObjectWithReusedReferencesFactory());
    }

    @Test
    public void testMapWithValueOfTypeList() throws Exception
    {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("abc", Arrays.asList("A", "B", "C"));

        String jsonFromObject = new JsonTestObjectMapper().writeValueAsString(map);

        assertEquals(jsonFromObject, "{\"abc\":[\"A\",\"B\",\"C\"]}");
        Map<String, Set<String>> recreatedMap = new JsonTestObjectMapper().readValue(jsonFromObject, LinkedHashMap.class);
        assertEquals(recreatedMap.toString(), map.toString());
    }

    private void testSerialize(ObjectFactory<?> factory) throws Exception
    {
        Object object = factory.createObjectToSerialize();
        String jsonFromObject = new JsonTestObjectMapper().writeValueAsString(object);

        Object expectedObject = factory.createExpectedMapAfterSerialization(new ObjectCounter());
        String jsonFromExpectedMap = new ObjectMapper().writeValueAsString(expectedObject);

        assertEquals(jsonFromObject, jsonFromExpectedMap);
    }

}
