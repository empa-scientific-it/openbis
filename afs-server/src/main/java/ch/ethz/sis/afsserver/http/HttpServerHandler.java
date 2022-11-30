package ch.ethz.sis.afsserver.http;

import java.io.InputStream;

public interface HttpServerHandler {
    public byte[] process(InputStream requestBody);
}
