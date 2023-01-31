/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.admin;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * @author Franz-Josef Elmer
 */
public class MoveDataSetsCommandTest extends AssertJUnit
{
    private Mockery context;

    private IDssServiceRpcGeneric dssService;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dssService = context.mock(IDssServiceRpcGeneric.class);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testValidArguments()
    {
        new MoveDataSetsCommand().parseArguments(toArray("-u", "user", "-p", "pswd", "3", "ds1",
                "ds2"));
    }

    @Test
    public void testMissingArguments()
    {
        MoveDataSetsCommand command = new MoveDataSetsCommand();
        try
        {
            command.parseArguments(toArray("-u", "user", "-p", "pswd", "3"));
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Usage: "
                            + AbstractCommand.BASH_COMMAND
                            + " "
                            + command.getName()
                            + " [options] <share id> <data set code 1> "
                            + "[<data set code 2> <data set code 3> ...]\n"
                            + " [-p,--password] VAL            : User login password\n"
                            + " [-sp,--service-properties] VAL : Path to DSS service.properties (default:\n"
                            + "                                  etc/service.properties)\n"
                            + " [-u,--username] VAL            : User login name\n" + "Example: "
                            + AbstractCommand.BASH_COMMAND + " " + command.getName()
                            + " -p VAL -sp VAL -u VAL <share id> <data set code 1> "
                            + "[<data set code 2> <data set code 3> ...]\n", ex.getMessage());
        }
    }

    @Test
    public void test()
    {
        context.checking(new Expectations()
            {
                {
                    one(dssService).shuffleDataSet(null, "ds1", "3");
                    one(dssService).shuffleDataSet(null, "ds2", "3");
                    one(dssService).shuffleDataSet(null, "ds3", "3");
                }
            });
        MoveDataSetsCommand command = new MoveDataSetsCommand()
            {
                {
                    service = dssService;
                }
            };

        command.parseArguments(toArray("-u", "user", "-p", "pswd", "3", "ds3", "ds2", "ds1", "ds1"));
        command.execute();

        context.assertIsSatisfied();
    }

    private String[] toArray(String... strings)
    {
        return strings;
    }

}
