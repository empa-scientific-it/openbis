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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

import ch.systemsx.cisd.openbis.common.api.server.json.deserializer.JsonDeserializerFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonReflectionsBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;
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
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeALegalDuplicate;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeB;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeBFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeBIllegalDuplicate;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeButNoSubtypes;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeButNoSubtypesFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;
import ch.systemsx.cisd.openbis.common.api.server.json.serializer.JsonSerializerFactory;

/**
 * @author pkupczyk
 */
public class JsonTestObjectMapper extends ObjectMapper
{

    public JsonTestObjectMapper()
    {
        super(null, null, new DefaultDeserializationContext.Impl(new JsonDeserializerFactory(
                getClassMapping())));
        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(getClassMapping()));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(getTypeMapping()));
        setSerializerFactory(new JsonSerializerFactory());
        configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private static IJsonClassValueToClassObjectsMapping getClassMapping()
    {
        JsonStaticClassValueToClassObjectsMapping classMapping =
                new JsonStaticClassValueToClassObjectsMapping();
        classMapping.addClass(ObjectWithTypeFactory.CLASS, ObjectWithType.class);
        classMapping.addClass(ObjectWithTypeAFactory.CLASS, ObjectWithTypeA.class);
        classMapping.addClass(ObjectWithTypeAFactory.CLASS, ObjectWithTypeALegalDuplicate.class);
        classMapping.addClass(ObjectWithTypeAAFactory.CLASS, ObjectWithTypeAA.class);
        classMapping.addClass(ObjectWithTypeBFactory.CLASS, ObjectWithTypeB.class);
        classMapping.addClass(ObjectWithTypeBFactory.CLASS, ObjectWithTypeBIllegalDuplicate.class);
        classMapping.addClass(ObjectWithTypeButNoSubtypesFactory.CLASS,
                ObjectWithTypeButNoSubtypes.class);
        classMapping.addClass(ObjectNestedFactory.CLASS, ObjectNested.class);
        classMapping.addClass(ObjectNestedChildFactory.CLASS, ObjectNestedChild.class);
        classMapping
                .addClass(ObjectWithPrimitiveTypesFactory.CLASS, ObjectWithPrimitiveTypes.class);
        classMapping.addClass(ObjectWithNestedTypesFactory.CLASS, ObjectWithNestedTypes.class);
        classMapping.addClass(ObjectWithEnumTypesFactory.CLASS, ObjectWithEnumTypes.class);
        classMapping
                .addClass(ObjectWithContainerTypesFactory.CLASS, ObjectWithContainerTypes.class);
        classMapping.addClass(ObjectWithDateTypesFactory.CLASS, ObjectWithDateTypes.class);
        classMapping.addClass(ObjectWithIgnoredPropertiesFactory.CLASS,
                ObjectWithIgnoredProperties.class);
        classMapping.addClass(ObjectWithRenamedPropertiesFactory.CLASS,
                ObjectWithRenamedProperties.class);
        classMapping.addClass(ObjectWithPrivateAccessFactory.CLASS, ObjectWithPrivateAccess.class);
        classMapping.addClass(ObjectWithSelfReferenceFactory.CLASS, ObjectWithSelfReference.class);
        classMapping.addClass(ObjectWithReusedReferencesFactory.CLASS,
                ObjectWithReusedReferences.class);
        return classMapping;
    }

    private static IJsonBaseTypeToSubTypesMapping getTypeMapping()
    {
        return new JsonReflectionsBaseTypeToSubTypesMapping(new String[] { JsonTestObjectMapper.class.getPackage()
                .getName() });
    }

}
