/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;

import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.EmptyJsonClassValueToClassObjectMapping;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;

/**
 * @author pkupczyk
 */
public class ProgressDetailsToStringBuilder
{

    private static final ProgressDetailsToStringBuilder instance = new ProgressDetailsToStringBuilder();

    private GenericObjectMapper mapper;

    private ProgressDetailsToStringBuilder()
    {
    }

    public String toString(Object object)
    {
        try
        {
            return getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static class ProgressDetailsAnnotationIntrospector extends JsonTypeAndClassAnnotationIntrospector
    {

        public ProgressDetailsAnnotationIntrospector()
        {
            super(new EmptyJsonClassValueToClassObjectMapping());
        }

        @Override
        public Object findFilterId(Annotated ac)
        {
            if (ac.hasAnnotation(JsonObject.class))
            {
                // all classed annotated with @JsonObject should use the same filter
                return new Object();
            } else
            {
                return null;
            }
        }

    }

    private static class ProgressDetailsNullKeySerializer extends JsonSerializer<Object>
    {

        @Override
        public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused)
                throws IOException, JsonProcessingException
        {
            jsonGenerator.writeFieldName("null");
        }
    }

    private synchronized GenericObjectMapper getMapper()
    {
        if (mapper == null)
        {
            mapper = new GenericObjectMapper();
            mapper.setAnnotationIntrospector(new ProgressDetailsAnnotationIntrospector());
            mapper.setFilters(new ProgressDetailsFilterProvider());
            mapper.setSerializationInclusion(Include.NON_EMPTY);
            mapper.getSerializerProvider().setNullKeySerializer(new ProgressDetailsNullKeySerializer());
        }

        return mapper;
    }

    public static ProgressDetailsToStringBuilder getInstance()
    {
        return instance;
    }

}
