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

package ch.systemsx.cisd.openbis.uitest.dsl;

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
public class StartApplicationServer
{

    public static String go() throws Exception
    {
        Runnable r = new Runnable()
            {
                @Override
                public void run()
                {

                    /*
                     * com.google.gwt.dev.DevMode.main(new String[] { "-startupUrl", "ch.systemsx.cisd.openbis.OpenBIS/index.html",
                     * "ch.systemsx.cisd.openbis.OpenBIS", "-war", "../openbis/targets/www", "-logLevel", "INFO" });
                     */
                    Server server = new Server(10000);

                    WebAppContext context = new WebAppContext();

                    if (new File("targets/gradle/openbis-war/openbis.war").exists() == false)
                    {
                        context.setDescriptor("targets/www/WEB-INF/web.xml");
                        context.setResourceBase("targets/www");
                    } else
                    {
                        context.setWar("targets/gradle/openbis-war/openbis.war");
                    }

                    context.setContextPath("/");
                    context.setParentLoaderPriority(true);
                    context.addAliasCheck(new AllowSymLinkAliasChecker());

                    server.setHandler(context);
                    try
                    {
                        server.start();
                        server.join();
                    } catch (Exception ex)
                    {
                        // TODO Auto-generated catch block
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
        return "http://localhost:10000";
    }

}
