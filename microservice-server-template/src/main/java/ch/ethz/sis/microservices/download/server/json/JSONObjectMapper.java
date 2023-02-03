package ch.ethz.sis.microservices.download.server.json;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;

public interface JSONObjectMapper
{

    <T> T readValue(InputStream src, Class<T> valueType) throws Exception;

    <T> T readValue(InputStream src, TypeReference<T> typeRef) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
