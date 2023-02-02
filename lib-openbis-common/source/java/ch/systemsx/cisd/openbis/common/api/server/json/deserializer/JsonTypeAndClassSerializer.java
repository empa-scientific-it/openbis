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

package ch.systemsx.cisd.openbis.common.api.server.json.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
public class JsonTypeAndClassSerializer extends AsPropertyTypeSerializer
{

    public JsonTypeAndClassSerializer(TypeIdResolver idRes, BeanProperty property, String propName)
    {
        super(idRes, property, propName);
    }

    @Override
    public JsonTypeAndClassSerializer forProperty(BeanProperty prop)
    {
        if (_property == prop)
            return this;
        return new JsonTypeAndClassSerializer(this._idResolver, prop, this._typePropertyName);
    }

    @Override
    public WritableTypeId writeTypePrefix(JsonGenerator g, WritableTypeId idMetadata) throws IOException
    {
        Object value = idMetadata.forValue;
        JsonToken valueShape = idMetadata.valueShape;
        if (JsonToken.START_OBJECT == valueShape)
        {
            g.writeStartObject();
            if (isValueWithType(value))
            {
                g.writeStringField(_typePropertyName, idFromValue(value));
            }
        } else if (JsonToken.VALUE_STRING == valueShape)
        {
            if (isValueWithType(value))
            {
                g.writeTypePrefix(idMetadata);
            }
        } else if (JsonToken.START_ARRAY == valueShape)
        {
            g.writeStartArray();
        } else
        {
            g.writeTypePrefix(idMetadata);
        }
        return idMetadata;
    }

    @Override
    public WritableTypeId writeTypeSuffix(JsonGenerator g, WritableTypeId idMetadata) throws IOException
    {
        Object value = idMetadata.forValue;
        JsonToken valueShape = idMetadata.valueShape;
        if (JsonToken.START_OBJECT == valueShape)
        {
            g.writeEndObject();
        } else if (JsonToken.VALUE_STRING == valueShape)
        {
            if (isValueWithType(value))
            {
                g.writeTypeSuffix(idMetadata);
            }
        } else if (JsonToken.START_ARRAY == valueShape)
        {
            g.writeEndArray();
        } else
        {
            g.writeTypeSuffix(idMetadata);
        }
        return idMetadata;
    }

    private boolean isValueWithType(Object value)
    {
        return value != null && value.getClass().isAnnotationPresent(JsonObject.class)
                && !value.getClass().isEnum();
    }

}
