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

package ch.systemsx.cisd.openbis.common.api.server.json.resolver;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import ch.systemsx.cisd.openbis.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.openbis.common.api.server.json.deserializer.JsonTypeAndClassDeserializer;
import ch.systemsx.cisd.openbis.common.api.server.json.deserializer.JsonTypeAndClassSerializer;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class JsonTypeAndClassResolverBuilder extends StdTypeResolverBuilder
{

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping;

    public JsonTypeAndClassResolverBuilder(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
        init(Id.NAME, null);
        inclusion(As.PROPERTY);
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType,
            Collection<NamedType> subtypes)
    {
        TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
        JsonTypeAndClassDeserializer deserializer =
                new JsonTypeAndClassDeserializer(baseType, subtypes, idRes, _typeProperty,
                        _typeIdVisible);
        deserializer.setClassValueToClassObjectsMapping(classValueToClassObjectsMapping);
        return deserializer;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes)
    {
        TypeIdResolver resolver = idResolver(config, baseType, subtypes, true, false);
        return new JsonTypeAndClassSerializer(resolver, null, JsonConstants.getTypeField());
    }

}
