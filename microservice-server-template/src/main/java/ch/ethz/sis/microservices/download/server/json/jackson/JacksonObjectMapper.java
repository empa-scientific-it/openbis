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
