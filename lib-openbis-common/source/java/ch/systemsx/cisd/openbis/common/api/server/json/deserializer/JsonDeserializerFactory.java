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
import java.io.StringWriter;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeNameIdResolver;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class JsonDeserializerFactory extends BeanDeserializerFactory
{

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping =
            new JsonStaticClassValueToClassObjectsMapping();

    public JsonDeserializerFactory(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        super(new DeserializerFactoryConfig());
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext ctxt, ArrayType type,
            BeanDescription beanDesc) throws JsonMappingException
    {
        ArrayType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(ctxt, type, beanDesc));
        return super.createArrayDeserializer(ctxt, newType, beanDesc);
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext ctxt,
            CollectionType type, BeanDescription beanDesc) throws JsonMappingException
    {
        CollectionType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(ctxt, type, beanDesc));
        return super.createCollectionDeserializer(ctxt, newType, beanDesc);
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext ctxt,
            CollectionLikeType type, BeanDescription beanDesc) throws JsonMappingException
    {
        CollectionLikeType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(ctxt, type, beanDesc));
        return super.createCollectionLikeDeserializer(ctxt, newType, beanDesc);
    }

    @Override
    public JsonDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type,
            BeanDescription beanDesc) throws JsonMappingException
    {
        MapType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(ctxt, type, beanDesc));
        return super.createMapDeserializer(ctxt, newType, beanDesc);
    }

    @Override
    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext ctxt,
            MapLikeType type, BeanDescription beanDesc) throws JsonMappingException
    {
        MapLikeType newType =
                type.withContentTypeHandler(createContentTypeDeserializer(ctxt, type, beanDesc));
        return super.createMapLikeDeserializer(ctxt, newType, beanDesc);
    }

    private TypeDeserializer createContentTypeDeserializer(DeserializationContext ctxt,
            JavaType containerType, BeanDescription beanDesc)
    {
        JavaType contentType = containerType.getContentType();

        if (contentType == null || contentType.getRawClass() == null)
        {
            return null;
        }

        Class<?> contentClass = contentType.getRawClass();

        if (contentClass.equals(Object.class) || (contentClass.isAnnotationPresent(JsonObject.class) && false == contentClass.isEnum()))
        {
            DeserializationConfig config = ctxt.getConfig();

            BeanDescription bean = config.introspectClassAnnotations(contentType.getRawClass());
            AnnotatedClass ac = bean.getClassInfo();
            AnnotationIntrospector ai = config.getAnnotationIntrospector();
            Collection<NamedType> subtypes =
                    config.getSubtypeResolver().collectAndResolveSubtypes(ac, config, ai);
            TypeIdResolver resolver =
                    TypeNameIdResolver.construct(config, contentType, subtypes, false, true);
            JsonTypeAndClassWithFallbackDeserializer deserializer =
                    new JsonTypeAndClassWithFallbackDeserializer(contentType, subtypes, resolver,
                            JsonConstants.getTypeField());
            deserializer.setClassValueToClassObjectsMapping(this.classValueToClassObjectsMapping);
            return deserializer;
        } else
        {
            return null;
        }
    }

    private class JsonTypeAndClassWithFallbackDeserializer extends JsonTypeAndClassDeserializer
    {

        public JsonTypeAndClassWithFallbackDeserializer(
                JsonTypeAndClassWithFallbackDeserializer src, BeanProperty property)
        {
            super(src, property);
        }

        public JsonTypeAndClassWithFallbackDeserializer(JavaType type,
                Collection<NamedType> subtypes, TypeIdResolver idRes, String typePropName)
        {
            super(type, subtypes, idRes, typePropName, false);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop)
        {
            return new JsonTypeAndClassWithFallbackDeserializer(this, prop);
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser parser, DeserializationContext ctxt)
                throws IOException
        {
            String json = parserToString(parser);
            JsonFactory factory = new JsonFactory(parser.getCodec());

            try
            {
                JsonParser jp1 = factory.createJsonParser(json);
                jp1.nextToken();
                return super.deserializeTypedFromObject(jp1, ctxt);
            } catch (IOException e)
            {
                try
                {
                    JsonParser jp2 = factory.createJsonParser(json);
                    jp2.nextToken();
                    return super.deserializeWithoutType(jp2, ctxt, null);
                } catch (IOException e2)
                {
                    throw new IllegalArgumentException("Could not deserialize object.\nAttempt of deserialization with type failed with: \n" + e
                            + "\nAttempt of deserialization without type failed with: \n" + e2);
                }
            }
        }

        private String parserToString(JsonParser parser)
        {
            try
            {
                JsonFactory factory = new JsonFactory(parser.getCodec());
                StringWriter writer = new StringWriter();
                JsonGenerator generator = factory.createJsonGenerator(writer);
                generator.setCodec(parser.getCodec());
                JsonNode node = parser.readValueAs(JsonNode.class);
                generator.writeTree(node);
                return writer.toString();
            } catch (IOException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
    }

}
