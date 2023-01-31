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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * @author Franz-Josef Elmer
 */
public class CommandLsTest extends AssertJUnit
{
    private final class MockCommandLs extends CommandLs
    {
        @Override
        protected IDssComponent login(GlobalArguments args)
        {
            return dssComponent;
        }
    }

    private Mockery context;

    private IDssComponent dssComponent;

    private IDataSetDss dataSet;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dssComponent = context.mock(IDssComponent.class);
        dataSet = context.mock(IDataSetDss.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        context.checking(new Expectations()
            {
                {
                    one(dssComponent).getDataSet("ds1");
                    will(returnValue(dataSet));

                    one(dataSet).listFiles("root-dir", false);
                    will(returnValue(new FileInfoDssDTO[] {}));

                    one(dssComponent).logout();
                }
            });
        ICommand command = new MockCommandLs();

        ResultCode exitCode = command.execute(new String[]
        { "-s", "url", "-u", "user", "-p", "pswd", "ds1", "root-dir" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }
}
