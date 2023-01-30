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

package ch.systemsx.cisd.openbis.test.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author anttil
 */
public class TestApplicationServer
{

    private int port;

    private String webXmlPath;

    private String rootPath;

    private String contextPath;

    private String dumpsPath;

    private boolean deamon;

    public String start() throws Exception
    {
        TestDatabase.restoreDumps(getDumpsPath());

        Runnable r = new Runnable()
            {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public void run()
                {
                    Server server = new Server(getPort());

                    WebAppContext context = new WebAppContext();

                    File war = new File("../../../targets/gradle/openbis-war/openbis.war");
                    if (war.exists())
                    {
                        context.setWar(war.getAbsolutePath());
                        context.setExtractWAR(true);
                        context.setTempDirectory(new File(System.getProperty("jetty.home") + "/webapps"));
                        context.addAliasCheck(new AllowSymLinkAliasChecker());
                    } else
                    {
                        context.setDescriptor(getWebXmlPath());
                        context.setResourceBase(getRootPath());
                    }
                    context.setContextPath(getContextPath());
                    context.setParentLoaderPriority(true);

                    server.setHandler(context);

                    try
                    {
                        server.start();
                        server.join();
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };

        PrintStream originalOut = System.out;

        PipedOutputStream outpipe = new PipedOutputStream();
        PipedInputStream inpipe = new PipedInputStream(outpipe);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inpipe));
        PrintStream newOut = new PrintStream(outpipe);
        System.setOut(newOut);

        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();

        String line;
        while ((line = reader.readLine()) != null)
        {
            originalOut.println(line);

            if (line.contains("SERVER STARTED"))
            {
                originalOut.println("SERVER START DETECTED");
                break;
            }
        }
        outpipe.close();
        inpipe.close();
        reader.close();
        newOut.close();

        System.setOut(originalOut);

        return "http://localhost:" + getPort();
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public void setWebXmlPath(String webXmlPath)
    {
        this.webXmlPath = webXmlPath;
    }

    public String getWebXmlPath()
    {
        return webXmlPath;
    }

    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

    public String getRootPath()
    {
        return rootPath;
    }

    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public void setDumpsPath(String dumpsPath)
    {
        this.dumpsPath = dumpsPath;
    }

    public String getDumpsPath()
    {
        return dumpsPath;
    }

    public void setDeamon(boolean deamon)
    {
        this.deamon = deamon;
    }

    public boolean isDeamon()
    {
        return deamon;
    }

}
