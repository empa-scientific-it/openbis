/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.microservices.download.server.json.jackson;

import ch.ethz.sis.microservices.download.server.json.JSONObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.InputStream;

public class JacksonObjectMapper implements JSONObjectMapper
{
    //
    // Singleton
    //
    private static final JacksonObjectMapper jacksonObjectMapper;

    static
    {
        jacksonObjectMapper = new JacksonObjectMapper();
    }

    public static JSONObjectMapper getInstance()
    {
        return jacksonObjectMapper;
    }

    //
    // Class implementation
    //

    private final ObjectMapper objectMapper;

    private JacksonObjectMapper()
    {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enableDefaultTyping();
    }

    @Override
    public <T> T readValue(final InputStream src, final Class<T> valueType) throws Exception
    {
        return objectMapper.readValue(src, valueType);
    }

    @Override
    public <T> T readValue(final InputStream src, final TypeReference<T> typeRef) throws Exception
    {
        return objectMapper.readValue(src, typeRef);
    }

    @Override
    public byte[] writeValue(final Object value) throws Exception
    {
        return objectMapper.writeValueAsBytes(value);
    }
}
