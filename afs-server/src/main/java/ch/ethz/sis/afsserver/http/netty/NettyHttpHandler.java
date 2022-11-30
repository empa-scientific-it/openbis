package ch.ethz.sis.afsserver.http.netty;

import ch.ethz.sis.afsserver.http.HttpServerHandler;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(NettyHttpServer.class);
    private static final byte[] NOT_FOUND = "404 NOT FOUND".getBytes();
    private static final ByteBuf NOT_FOUND_BUFFER = Unpooled.wrappedBuffer(NOT_FOUND);

    private final String uri;
    private final HttpServerHandler httpServerHandler;

    public NettyHttpHandler(String uri, HttpServerHandler httpServerHandler) {
        this.uri = uri;
        this.httpServerHandler = httpServerHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;
            if (request.uri().equals(uri) && (request.method() == HttpMethod.POST || request.method() == HttpMethod.GET)) {
                FullHttpResponse response = null;
                ByteBuf content = request.content();
                try {
                    ByteBufInputStream inputStream = new ByteBufInputStream(content);
                    byte[] output = httpServerHandler.process(inputStream);
                    response = getHttpResponse(
                            HttpResponseStatus.OK,
                            "application/json",
                            Unpooled.wrappedBuffer(output),
                            output.length);
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } finally {
                    content.release();
                }
            } else {
                FullHttpResponse response = getHttpResponse(
                            HttpResponseStatus.NOT_FOUND,
                            "text/plain",
                            NOT_FOUND_BUFFER,
                            NOT_FOUND.length);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.catching(cause);
        byte[] causeBytes = cause.getMessage().getBytes();
        FullHttpResponse response = getHttpResponse(
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "text/plain",
                    Unpooled.wrappedBuffer(causeBytes),
                    causeBytes.length
                    );
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public FullHttpResponse getHttpResponse(
            HttpResponseStatus status,
            String contentType,
            ByteBuf content,
            int contentLength) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                content
        );
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        return response;
    }
}
