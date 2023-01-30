package ch.ethz.sis.afsserver.http;

public interface HttpServer {
    void start(int port, int maxContentLength, String uri, HttpServerHandler jsonrpc2Server);

    void shutdown(boolean gracefully);
}