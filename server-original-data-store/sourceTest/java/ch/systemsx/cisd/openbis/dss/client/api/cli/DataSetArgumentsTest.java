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

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetArgumentsTest extends AssertJUnit
{

    private DataSetArguments arguments;

    private CmdLineParser parser;

    @BeforeMethod
    public void setUp()
    {
        arguments = new DataSetArguments();
        parser = new CmdLineParser(arguments);
    }

    @Test
    public void testParseBasicArguments()
    {
        String[] args =
        { "-u", "foo", "-p", "bar", "-s",
                TestInstanceHostUtils.getOpenBISUrl() + "/openbis",
                "20100318094819344-4" };
        try
        {
            parser.parseArgument(args);
            assertEquals("foo", arguments.getUsername());
            assertEquals("bar", arguments.getPassword());
            assertEquals(TestInstanceHostUtils.getOpenBISUrl() + "/openbis",
                    arguments.getServerBaseUrl());
            assertEquals("20100318094819344-4", arguments.getDataSetCode());
            assertTrue(arguments.isComplete());
            assertFalse(arguments.isHelp());
        } catch (CmdLineException c)
        {
            fail("Should have parsed arguments");
        }
    }

    @Test
    public void testParseIncompleteArguments()
    {
        String[] args =
        { "-u", "foo", "-p", "bar", "-s", TestInstanceHostUtils.getOpenBISUrl() + "/openbis" };
        try
        {
            parser.parseArgument(args);
            assertFalse(arguments.isComplete());
        } catch (CmdLineException c)
        {
            fail("Should have parsed arguments");
        }
    }
}
