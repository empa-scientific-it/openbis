package ch.ethz.sis.shared.json.jackson;

import java.io.InputStream;
import java.nio.charset.Charset;

public interface JSONObjectMapper {

    Charset getCharset();

    void setCharset(Charset charset);

    <T> T readValue(InputStream src, Class<T> valueType) throws Exception;

    byte[] writeValue(Object value) throws Exception;

}
