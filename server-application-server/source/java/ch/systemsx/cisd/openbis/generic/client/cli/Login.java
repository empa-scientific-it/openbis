/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.ConsoleReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * A class to provide a login on an openBIS server. The login information will be put into a sub-directory of the user's home directory.
 * 
 * @author Bernd Rinn
 */
public class Login
{
    private final static File OPENBIS_DIR = new File(System.getProperty("user.home"), ".openbis");

    public final static File OPENBIS_TOKEN_FILE = new File(OPENBIS_DIR, "session-token");

    public final static File OPENBIS_USER_FILE = new File(OPENBIS_DIR, "user");

    public final static File OPENBIS_SERVER_URL_FILE = new File(OPENBIS_DIR, "server");

    private static class Parameters
    {
        private final CmdLineParser parser;

        @Argument()
        private final List<String> args = new ArrayList<String>();

        @Option(name = "u", longName = "user")
        private String user;

        @Option(name = "s", longName = "server")
        private String server;

        Parameters(String[] args)
        {
            parser = new CmdLineParser(this);
            try
            {
                parser.parseArgument(args);

                if (server == null)
                {
                    if (OPENBIS_SERVER_URL_FILE.exists())
                    {
                        BufferedReader br = null;
                        try
                        {
                            br = new BufferedReader(new FileReader(Login.OPENBIS_SERVER_URL_FILE));
                            server = br.readLine();
                            br.close();
                        } finally
                        {
                            closeQuietly(br);
                        }
                    } else
                    {
                        server = new ConsoleReader().readLine("Server URL: ");
                    }
                }

                if (user == null)
                {
                    if (OPENBIS_USER_FILE.exists())
                    {
                        BufferedReader br = null;
                        try
                        {
                            br = new BufferedReader(new FileReader(OPENBIS_USER_FILE));
                            user = br.readLine();
                            br.close();
                        } finally
                        {
                            closeQuietly(br);
                        }
                    } else
                    {
                        user = new ConsoleReader().readLine("User: ");
                    }
                }
            } catch (final Exception ex)
            {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }

        @SuppressWarnings("unused")
        public List<String> getArgs()
        {
            return Collections.unmodifiableList(args);
        }

        public String getUser()
        {
            return user;
        }

        public String getServer()
        {
            return server;
        }

    }

    private static void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            } catch (IOException ex)
            {
                // Silence this.
            }
        }
    }

    private static void write(String server, String user, String token)
    {
        BufferedWriter bw = null;
        try
        {
            if (OPENBIS_DIR.exists() == false)
            {
                OPENBIS_DIR.mkdir();
            }
            bw = new BufferedWriter(new FileWriter(OPENBIS_SERVER_URL_FILE));
            bw.write(server);
            bw.close();
            bw = new BufferedWriter(new FileWriter(OPENBIS_USER_FILE));
            bw.write(user);
            bw.close();
            bw = new BufferedWriter(new FileWriter(OPENBIS_TOKEN_FILE));
            bw.write(token);
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        } finally
        {
            closeQuietly(bw);
        }

    }

    public static void main(String[] args) throws IOException
    {
        BasicConfigurator.configure(new NullAppender());
        try
        {
            final Parameters params = new Parameters(args);
            if (params.getServer() == null)
            {
                throw new RuntimeException("Server not specified (give '-s').");
            }
            if (params.getUser() == null)
            {
                throw new RuntimeException("User not specified (give '-u').");
            }
            final String password =
                    new ConsoleReader().readLine("Password: ", Character.valueOf('*'));
            if (password == null || password.length() == 0)
            {
                throw new RuntimeException("Password empty.");
            }
            ServiceFinder generalInformationServiceFinder =
                    new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
            IGeneralInformationService service =
                    generalInformationServiceFinder.createService(IGeneralInformationService.class,
                            params.getServer());
            // Log in
            String sessionToken =
                    service.tryToAuthenticateForAllServices(params.getUser(), password);

            if (sessionToken == null)
            {
                throw new RuntimeException("Login failed.");
            }
            write(params.getServer(), params.getUser(), sessionToken);
        } catch (RuntimeException ex)
        {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null)
            {
                msg = ex.getCause().getClass().getSimpleName() + ": " + ex.getMessage();
            }
            System.err.println(msg);
            System.exit(1);
        }
    }

}
