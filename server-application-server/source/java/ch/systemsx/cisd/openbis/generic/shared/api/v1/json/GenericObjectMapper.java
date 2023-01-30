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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

import ch.systemsx.cisd.openbis.common.api.server.json.deserializer.JsonDeserializerFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.openbis.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;
import ch.systemsx.cisd.openbis.common.api.server.json.serializer.JsonSerializerFactory;

/**
 * Jackson library object mapper used in generic OpenBIS.
 * 
 * @author pkupczyk
 */
public class GenericObjectMapper extends ObjectMapper
{

    public GenericObjectMapper()
    {
        super(null, null, new DefaultDeserializationContext.Impl(new JsonDeserializerFactory(
                GenericJsonClassValueToClassObjectsMapping.getInstance())));

        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(
                GenericJsonClassValueToClassObjectsMapping.getInstance()));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(new JsonBaseTypeToSubTypesMapping()));
        setSerializerFactory(new JsonSerializerFactory());
    }

}
