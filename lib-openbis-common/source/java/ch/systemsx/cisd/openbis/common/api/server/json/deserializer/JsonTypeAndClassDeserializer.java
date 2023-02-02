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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;

/**
 * Custom JSON deserializer that recognizes objects with both @type and @class fields. Deserializing objects with @class field requires and
 * appropriate entry in classValueToClassObjectsMapping. If @class value is not found in the mapping then a JSON object is not deserialized and an
 * exception is thrown. If the mapping is found then a class that is assignable to the deserialization base type is picked. If there is no such class
 * or there is more than one then an exception is thrown. Please remember that @class field is now deprecated and the support for that field is
 * maintained only for backwards compatibility.
 * 
 * @author pkupczyk
 */
public class JsonTypeAndClassDeserializer extends AsPropertyTypeDeserializer
{

    private boolean hasSubtypes;

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping =
            new JsonStaticClassValueToClassObjectsMapping();

    public JsonTypeAndClassDeserializer(JsonTypeAndClassDeserializer src, BeanProperty property)
    {
        super(src, property);
        hasSubtypes = src.hasSubtypes;
        classValueToClassObjectsMapping = src.classValueToClassObjectsMapping;
    }

    public JsonTypeAndClassDeserializer(JavaType type, Collection<NamedType> subtypes,
            TypeIdResolver idRes, String typePropName, boolean typeIdVisible)
    {
        super(type, idRes, typePropName, typeIdVisible, null);
        hasSubtypes = subtypes != null && subtypes.size() > 1;
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop)
    {
        return new JsonTypeAndClassDeserializer(this, prop);
    }

    @Override
    public Object deserializeTypedFromAny(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        return deserializeTypedFromObject(jp, ctxt);
    }

    @Override
    public Object deserializeTypedFromArray(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        return deserializeTypedFromObject(jp, ctxt);
    }

    @Override
    public Object deserializeTypedFromObject(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        // but first, sanity check to ensure we have START_OBJECT or FIELD_NAME
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT)
        {
            t = jp.nextToken();
        } else if (t == JsonToken.START_ARRAY)
        {
            /*
             * This is most likely due to the fact that not all Java types are serialized as JSON Objects; so if "as-property" inclusion is requested,
             * serialization of things like Lists must be instead handled as if "as-wrapper-array" was requested. But this can also be due to some
             * custom handling: so, if "defaultImpl" is defined, it will be asked to handle this case.
             */
            return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
        } else if (t != JsonToken.FIELD_NAME)
        {
            return _deserializeTypedUsingDefaultImpl(jp, ctxt, null);
        }
        // Ok, let's try to find the property. But first, need token buffer...
        TokenBuffer tb = null;

        for (; t == JsonToken.FIELD_NAME; t = jp.nextToken())
        {
            String name = jp.getCurrentName();
            jp.nextToken(); // to point to the value

            if (JsonConstants.getTypeField().equals(name))
            {
                return deserializeWithType(jp, ctxt, tb, jp.getText());
            } else if (JsonConstants.getLegacyClassField().equals(name))
            {
                return deserializeWithLegacyClass(jp, ctxt, tb);
            }

            if (tb == null)
            {
                tb = new TokenBuffer(null, false);
            }

            tb.writeFieldName(name);
            tb.copyCurrentStructure(jp);
        }

        if (hasSubtypes)
        {
            throw new JsonMappingException(
                    "Cannot deserialize a polymorphic type without type information");
        } else
        {
            return deserializeWithoutType(jp, ctxt, tb);
        }
    }

    @SuppressWarnings("resource")
    // TokenBuffer.close() just marks the buffer as closed.
    private Object deserializeWithType(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb,
            String type) throws IOException, JsonProcessingException
    {
        JsonParser actualJp = jp;
        TokenBuffer actualTb = tb;
        JsonDeserializer<Object> deser = _findDeserializer(ctxt, type);
        if (_typeIdVisible)
        { // need to merge id back in JSON input?
            if (actualTb == null)
            {
                actualTb = new TokenBuffer(null, false);
            }
            actualTb.writeFieldName(actualJp.getCurrentName());
            actualTb.writeString(type);
        }
        if (actualTb != null)
        { // need to put back skipped properties?
            actualJp = JsonParserSequence.createFlattened(actualTb.asParser(actualJp), actualJp);
        }
        // Must point to the next value; tb had no current, jp pointed to VALUE_STRING:
        actualJp.nextToken(); // to skip past String value
        // deserializer should take care of closing END_OBJECT as well
        return deser.deserialize(actualJp, ctxt);
    }

    private Object deserializeWithLegacyClass(JsonParser jp, DeserializationContext ctxt,
            TokenBuffer tb) throws IOException, JsonProcessingException
    {
        String classValue = jp.getText();

        List<Class<?>> classObjects = classValueToClassObjectsMapping.getClasses(classValue);

        if (classObjects != null && classObjects.size() > 0)
        {
            List<Class<?>> matchingClassObjects = new ArrayList<Class<?>>();

            for (Class<?> classObject : classObjects)
            {
                if (classObject != null && _baseType.getRawClass().isAssignableFrom(classObject))
                {
                    matchingClassObjects.add(classObject);
                }
            }

            if (matchingClassObjects.isEmpty())
            {
                throw new NoMatchingLegacyClassFoundException(classValue);
            } else if (matchingClassObjects.size() > 1)
            {
                throw new MoreThanOneMatchingLegacyClassFoundException(classValue,
                        matchingClassObjects);
            } else
            {
                JsonObject objectAnnotation =
                        matchingClassObjects.get(0).getAnnotation(JsonObject.class);
                if (objectAnnotation != null)
                {
                    return deserializeWithType(jp, ctxt, tb, objectAnnotation.value());
                } else
                {
                    throw new MissingAnnotationForLegacyClassException(classValue,
                            matchingClassObjects.get(0));
                }
            }

        }

        throw new NoMatchingLegacyClassesException(classValue);
    }

    public Object deserializeWithoutType(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb)
            throws IOException, JsonProcessingException
    {
        JsonToken t = jp.getCurrentToken();

        final JsonParser actualJp;
        final JsonDeserializer<Object> deserializer;

        JavaType type = _baseType;

        if (type.getRawClass().equals(Object.class))
        {
            if (t == JsonToken.START_ARRAY)
            {
                type =
                        ctxt.getConfig().getTypeFactory()
                                .constructRawCollectionType(Collection.class);
            } else if (t == JsonToken.START_OBJECT)
            {
                type = ctxt.getConfig().getTypeFactory().constructRawMapType(Map.class);
            }
        }

        deserializer = ctxt.findContextualValueDeserializer(type, _property);

        if (tb != null)
        {
            tb.writeEndObject();
            actualJp = tb.asParser(jp);
            // must move to point to the first token:
            actualJp.nextToken();
        } else
        {
            actualJp = jp;
        }

        return deserializer.deserialize(actualJp, ctxt);
    }

    public void setClassValueToClassObjectsMapping(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        if (classValueToClassObjectsMapping != null)
        {
            this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
        } else
        {
            this.classValueToClassObjectsMapping = new JsonStaticClassValueToClassObjectsMapping();
        }
    }

    private class NoMatchingLegacyClassesException extends JsonMappingException
    {

        private static final long serialVersionUID = 1L;

        public NoMatchingLegacyClassesException(String classValue)
        {
            super(
                    "Couldn't deserialize a JSON object with a legacy @class field value: '"
                            + classValue
                            + "'. No classes have been definded in @class => Java classes mapping for that value.");
        }
    }

    private class NoMatchingLegacyClassFoundException extends JsonMappingException
    {

        private static final long serialVersionUID = 1L;

        public NoMatchingLegacyClassFoundException(String classValue)
        {
            super(
                    "Couldn't deserialize a JSON object with a legacy @class field value: '"
                            + classValue
                            + "'. None of the classes: "
                            + classValueToClassObjectsMapping.getClasses(classValue)
                            + " definded in @class => Java classes mapping can be assigned to a base type: '"
                            + _baseType + "'.");
        }
    }

    private class MoreThanOneMatchingLegacyClassFoundException extends JsonMappingException
    {

        private static final long serialVersionUID = 1L;

        public MoreThanOneMatchingLegacyClassFoundException(String classValue,
                List<Class<?>> matchingClasses)
        {
            super(
                    "Couldn't deserialize a JSON object with a legacy @class field value: '"
                            + classValue
                            + "'. All of the classes: "
                            + matchingClasses
                            + " definded in @class => Java classes mapping can be assigned to a base type: '"
                            + _baseType + "'.");
        }

    }

    private class MissingAnnotationForLegacyClassException extends JsonMappingException
    {

        private static final long serialVersionUID = 1L;

        public MissingAnnotationForLegacyClassException(String classValue, Class<?> matchingClass)
        {
            super("Couldn't deserialize a JSON object with a legacy @class field value: '"
                    + classValue + "'. Class defined in @class => Java classes mapping: '"
                    + matchingClass + "' can be assigned to a base type: '" + _baseType
                    + "' but it doesn't contain @JsonObject annotation.");
        }

    }

}
