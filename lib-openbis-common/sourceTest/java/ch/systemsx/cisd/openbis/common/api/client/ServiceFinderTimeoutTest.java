/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.api.client;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.remoting.RemoteAccessException;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.IRpcService;

/**
 * @author Kaloyan Enimanev
 */
public class ServiceFinderTimeoutTest extends AssertJUnit
{

    public interface ITestInterace extends IRpcService
    {
        public void timeoutProvokingMethod();
    }

    private static final int ONE_SECOND_TIMEOUT = 1000;

    private NotRespondingServer server = new NotRespondingServer();

    @BeforeClass
    public void startNotRespondingServer() throws Exception
    {
        server.start();
    }

    @AfterClass
    public void stopNotRespondingServer()
    {
        server.stop();
    }

    @Test(timeOut = 5 * ONE_SECOND_TIMEOUT)
    public void testTimeout()
    {
        ServiceFinder finder = new ServiceFinder("", "");
        try
        {
            finder.createService(ITestInterace.class, "http://localhost:" + server.getPort(),
                    ONE_SECOND_TIMEOUT);
            fail("Timeout exception expected");
        } catch (RemoteAccessException rae)
        {
            assertEquals(ExecutionException.class, rae.getCause().getClass());
        }
    }

    /**
     * A server that accepts client connections but never responds to their requests.
     */
    public static class NotRespondingServer
    {

        private ServerSocket serverSocket;

        private AtomicBoolean keepRunning = new AtomicBoolean(true);

        public void start() throws Exception
        {
            serverSocket = new ServerSocket(0);
            serverSocket.setSoTimeout(1000);
            Runnable serverThread = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            runInternal();
                        } catch (Throwable t)
                        {
                            t.printStackTrace();
                        }
                    }

                };
            new Thread(serverThread).start();
        }

        private void runInternal() throws Exception
        {

            while (keepRunning.get())
            {
                Socket client = null;
                try
                {
                    client = serverSocket.accept();
                } catch (SocketTimeoutException ste)
                {
                    continue;
                }

                final InputStream clientInput = client.getInputStream();

                Runnable reader = new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            try
                            {
                                int b = 0;
                                do
                                {
                                    // read the
                                    b = clientInput.read();
                                } while (b != -1);

                            } catch (Throwable t)
                            {
                                t.printStackTrace();
                            }
                        }

                    };
                new Thread(reader).start();
            }
        }

        public void stop()
        {
            keepRunning.set(false);
        }

        public int getPort()
        {
            return serverSocket.getLocalPort();
        }

        public static void main(String[] args) throws Exception
        {
            new NotRespondingServer().start();
        }

    }

}
