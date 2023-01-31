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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssCommandFactoryTest extends AssertJUnit
{
    DssCommandFactory factory;

    @BeforeMethod
    public void setUp()
    {
        factory = new DssCommandFactory();
    }

    @Test
    public void testNameMapping()
    {
        ICommand cmd;
        cmd = factory.tryCommandForName("ls");
        assertEquals(CommandLs.class, cmd.getClass());

        cmd = factory.tryCommandForName("get");
        assertEquals(CommandGet.class, cmd.getClass());
    }

    @Test
    public void testHelp()
    {
        ICommand cmd;
        cmd = factory.tryCommandForName("help");
        String[] noArgs = {};
        cmd.execute(noArgs);

        System.out.print("\n");
        cmd = factory.tryCommandForName("help");
        String[] lsArgs =
        { "ls" };
        cmd.execute(lsArgs);

        System.out.print("\n");
        cmd = factory.tryCommandForName("help");
        String[] getArgs =
        { "get" };
        cmd.execute(getArgs);
    }

    @Test
    public void testHelpFormat()
    {
        ICommand cmd;
        cmd = factory.getHelpCommand();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bos);
        cmd.printUsage(out);
        String helpText =
                "usage: dss_client.sh COMMAND [options...] <command arguments>\n" + "\n"
                        + "Commands:\n" + " ls\n" + " get\n" + " put\n" + " testvalid\n"
                        + " testextract\n" + "\n" + "Options:\n"
                        + " [-T,--timeout] N           : Timeout in seconds\n"
                        + " [-p,--password] VAL        : User login password\n"
                        + " [-s,--server-base-url] VAL : URL for openBIS Server (required)\n"
                        + " [-u,--username] VAL        : User login name\n";
        assertEquals(helpText, bos.toString());
    }
}
