/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;

import ch.systemsx.cisd.openbis.common.api.server.json.deserializer.JsonDeserializerFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.openbis.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;
import ch.systemsx.cisd.openbis.common.api.server.json.serializer.JsonSerializerFactory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.JsonBaseTypeToSubTypesMapping;

/**
 * Jackson library object mapper used in screening OpenBIS.
 * 
 * @author pkupczyk
 */
public class ScreeningObjectMapper extends ObjectMapper
{

    public ScreeningObjectMapper()
    {
        super(null, null, new DefaultDeserializationContext.Impl(new JsonDeserializerFactory(
                ScreeningJsonClassValueToClassObjectsMapping.getInstance())));

        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(
                ScreeningJsonClassValueToClassObjectsMapping.getInstance()));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(
                new JsonBaseTypeToSubTypesMapping()));
        setSerializerFactory(new JsonSerializerFactory());
    }

}
