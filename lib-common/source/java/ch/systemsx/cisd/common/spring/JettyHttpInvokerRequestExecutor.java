package ch.systemsx.cisd.common.spring;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.marathon.util.spring.StreamSupportingRemoteInvocation;
import com.marathon.util.spring.StreamSupportingRemoteInvocationResult;

public class JettyHttpInvokerRequestExecutor extends AbstractHttpInvokerRequestExecutor
{
    private static final long RESPONSE_BUFFER_SIZE = 1000 * FileUtils.ONE_MB;

    private static final Log log =
            LogFactory.getLog(JettyHttpInvokerRequestExecutor.class);

    private final HttpClient client;

    private final long serverTimeoutInMillis;

    public JettyHttpInvokerRequestExecutor(HttpClient client, long serverTimeoutInMillis)
    {
        this.client = client;
        this.serverTimeoutInMillis = serverTimeoutInMillis <= 0 ? serverTimeoutInMillis : Math.max(1000, serverTimeoutInMillis);
    }

    protected RemoteInvocationResult doExecuteBasicRequest(HttpInvokerClientConfiguration config,
            ByteArrayOutputStream baos) throws Exception
    {
        Request request =
                client.POST(config.getServiceUrl()).content(new BytesContentProvider(baos.toByteArray()))
                        .timeout(serverTimeoutInMillis, TimeUnit.MILLISECONDS);

        FutureResponseListener listener = new FutureResponseListener(request, (int) RESPONSE_BUFFER_SIZE);
        request.send(listener);

        ContentResponse response = listener.get();
        return readRemoteInvocationResult(new ByteArrayInputStream(response.getContent()), config.getCodebaseUrl());
    }

    @Override
    protected ByteArrayOutputStream getByteArrayOutputStream(final RemoteInvocation invocation)
            throws IOException
    {
        // The constant is private in AbstractHttpInvokerRequestExecutor.
        final WorkaroundByteArrayOutputStream baos =
                new WorkaroundByteArrayOutputStream(1024, invocation);
        writeRemoteInvocation(invocation, baos);
        return baos;
    }

    @Override
    protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config,
            final ByteArrayOutputStream baos) throws Exception
    {
        final WorkaroundByteArrayOutputStream wbaos = (WorkaroundByteArrayOutputStream) baos;
        if (wbaos.getRemoteInvocation() instanceof StreamSupportingRemoteInvocation)
        {
            return doExecuteRequest(config, wbaos, (StreamSupportingRemoteInvocation) wbaos
                    .getRemoteInvocation());
        } else
        {
            return doExecuteBasicRequest(config, baos);
        }
    }

    @Override
    protected RemoteInvocationResult readRemoteInvocationResult(final InputStream is,
            final String codebaseUrl) throws IOException, ClassNotFoundException
    {
        final RemoteInvocationResult ret = super.readRemoteInvocationResult(is, codebaseUrl);
        if (!(ret instanceof StreamSupportingRemoteInvocationResult))
        {
            is.close();
        }
        return ret;
    }

    @Override
    protected ObjectInputStream createObjectInputStream(final InputStream is,
            final String codebaseUrl) throws IOException
    {
        return new SourceStreamPreservingObjectInputStream(is, codebaseUrl);
    }

    @Override
    protected RemoteInvocationResult doReadRemoteInvocationResult(final ObjectInputStream ois)
            throws IOException, ClassNotFoundException
    {
        final RemoteInvocationResult source = super.doReadRemoteInvocationResult(ois);
        if (source instanceof StreamSupportingRemoteInvocationResult)
        {
            ((StreamSupportingRemoteInvocationResult) source)
                    .setClientSideInputStream(((SourceStreamPreservingObjectInputStream) ois)
                            .getSourceInputStream());
        }

        return source;
    }

    //
    // HELPER METHODS
    //

    /**
     * Execute a request to send the given serialized remote invocation.
     * <p>
     * Implementations will usually call <code>readRemoteInvocationResult</code> to deserialize a returned RemoteInvocationResult object.
     * 
     * @param config the HTTP invoker configuration that specifies the target service
     * @param baos the ByteArrayOutputStream that contains the serialized RemoteInvocation object
     * @return the RemoteInvocationResult object
     * @throws IOException if thrown by I/O operations
     * @throws ClassNotFoundException if thrown during deserialization
     * @see #readRemoteInvocationResult(java.io.InputStream, String)
     */
    protected RemoteInvocationResult doExecuteRequest(final HttpInvokerClientConfiguration config,
            final ByteArrayOutputStream baos, final StreamSupportingRemoteInvocation invocation)
            throws IOException, ClassNotFoundException
    {

        final Request postMethod;

        if (invocation.getClientSideInputStream() != null)
        {
            final ByteArrayInputStream serializedInvocation =
                    new ByteArrayInputStream(baos.toByteArray());

            // We don't want to close the client side input stream unless the remote
            // method closes the input stream, so we "shield" the close for now.
            final InputStream body =
                    new CompositeInputStream(
                            new InputStream[]
                            {
                                    serializedInvocation,
                                    new CloseShieldedInputStream(invocation
                                            .getClientSideInputStream()) });
            postMethod = client.POST(config.getServiceUrl())
                    .header(HTTP_HEADER_CONTENT_TYPE, "application/x-java-serialized-object-with-stream")
                    .content(new InputStreamContentProvider(body), "application/x-java-serialized-object-with-stream");

        } else
        {
            postMethod = client.POST(config.getServiceUrl()).content(new BytesContentProvider(baos.toByteArray()));
        }

        InputStreamResponseListener listener = new InputStreamResponseListener();
        postMethod.timeout(serverTimeoutInMillis, TimeUnit.MILLISECONDS).send(listener);

        final RemoteInvocationResult ret =
                readRemoteInvocationResult(listener.getInputStream(), config
                        .getCodebaseUrl());
        if (ret instanceof StreamSupportingRemoteInvocationResult)
        {
            final StreamSupportingRemoteInvocationResult ssrir =
                    (StreamSupportingRemoteInvocationResult) ret;

            // Close the local InputStream parameter if the remote method
            // explicitly closed the InputStream parameter on the other side.
            if (invocation.getClientSideInputStream() != null)
            {
                if (ssrir.getMethodClosedParamInputStream() != null)
                {
                    if (Boolean.TRUE.equals(ssrir.getMethodClosedParamInputStream()))
                    {
                        invocation.getClientSideInputStream().close();
                    }
                } else
                {
                    warnInputStreamParameterStateNotSpecified(invocation);
                }
            }

            // If there is a return stream, then we need to leave the PostMethod
            // connection open until the return stream is closed, so augment the
            // return stream for this.
            if (ssrir.getHasReturnStream())
            {
                final InputStream sourceRetIs = ssrir.getClientSideInputStream();
                if (sourceRetIs != null)
                {
                    ssrir.setClientSideInputStream(new FilterInputStream(sourceRetIs)
                        {
                            @Override
                            public void close() throws IOException
                            {
                                super.close();
                            }
                        });
                }
            }
        } else if (invocation.getClientSideInputStream() != null)
        {
            warnInputStreamParameterStateNotSpecified(invocation);
        }
        return ret;
    }

    private void warnInputStreamParameterStateNotSpecified(
            final StreamSupportingRemoteInvocation invocation)
    {
        log.warn("Remote method invocation with InputStream parameter did not indicate if remote method closed the InputStream parameter!  Will leave the stream open.  RemoteInvocation: "
                + invocation);
    }

    //
    // INNER CLASSES
    //

    /**
     * Works around the "final" executeRequest(...) method and sneaks in the RemoteInvocation reference.
     */
    public static class WorkaroundByteArrayOutputStream extends ByteArrayOutputStream
    {
        private final RemoteInvocation remoteInvocation;

        public WorkaroundByteArrayOutputStream(final int size,
                final RemoteInvocation remoteInvocation)
        {
            super(size);
            this.remoteInvocation = remoteInvocation;
        }

        public RemoteInvocation getRemoteInvocation()
        {
            return this.remoteInvocation;
        }
    }

    /**
     * Prevents the source InputStream from being closed, but allows the ObjectInputStream to go through the motions of closing the stream so that its
     * internal state is properly cleared.
     */
    public static class SourceStreamPreservingObjectInputStream extends
            CodebaseAwareObjectInputStream
    {
        private InputStream sourceInputStream;

        public SourceStreamPreservingObjectInputStream(final InputStream in,
                final String codebaseUrl) throws IOException
        {
            // Prevent the source InputStream from being closed when the
            // ObjectInputStream is closed. We do it this way rather than
            // overriding close() to ensure that ObjectInputStream has a chance to
            // clear out itself when close() is called.
            super(new CloseShieldedInputStream(in), codebaseUrl);
            this.sourceInputStream = in;
        }

        public InputStream getSourceInputStream()
        {
            return this.sourceInputStream;
        }
    }

    /**
     * Shields an underlying InputStream from being closed.
     */
    public static class CloseShieldedInputStream extends FilterInputStream
    {
        public CloseShieldedInputStream(final InputStream in)
        {
            super(in);
        }

        @Override
        public void close() throws IOException
        {
        }
    }

    /**
     * Allows multiple InputStreams to be composited into a single InputStream.
     */
    public static class CompositeInputStream extends FilterInputStream
    {
        private InputStream[] inputStreams;

        private int currentInputStreamIdx;

        public CompositeInputStream(final InputStream[] inputStreams)
        {
            super(inputStreams[0]);
            this.inputStreams = inputStreams;
            this.currentInputStreamIdx = 0;
        }

        public InputStream[] getInputStreams()
        {
            return this.inputStreams;
        }

        public int getCurrentInputStreamIdx()
        {
            return this.currentInputStreamIdx;
        }

        protected InputStream incCurrentInputStream() throws IOException
        {
            if ((++this.currentInputStreamIdx) >= getInputStreams().length)
            {
                return null;
            } else
            {
                this.in.close();
                return this.in = getInputStreams()[this.currentInputStreamIdx];
            }
        }

        @Override
        public int read() throws IOException
        {
            final int read = super.read();
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read();
                }
            } else
            {
                return read;
            }
        }

        @Override
        public int read(byte b[]) throws IOException
        {
            final int read = super.read(b);
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read(b);
                }
            } else
            {
                return read;
            }
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException
        {
            final int read = super.read(b, off, len);
            if (read == -1)
            {
                if (incCurrentInputStream() == null)
                {
                    return -1;
                } else
                {
                    return read(b, off, len);
                }
            } else
            {
                return read;
            }
        }

        @Override
        public void close() throws IOException
        {
            // All InputStreams preceeding the current one have already been closed.
            // Be sure to close all InputStreams following the current one as well.
            final InputStream[] myInputStreams = getInputStreams();
            for (int i = getCurrentInputStreamIdx(); i < myInputStreams.length; i++)
            {
                myInputStreams[i].close();
            }
        }
    }

}
