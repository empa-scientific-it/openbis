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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * @author Franz-Josef Elmer
 */
public class CommandPutTest extends AbstractFileSystemTestCase
{
    private final class MockCommandPut extends CommandPut
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

    private File dataSetExample;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dssComponent = context.mock(IDssComponent.class);
        dataSet = context.mock(IDataSetDss.class);
        dataSetExample = new File(workingDirectory, "data-set");
        dataSetExample.mkdirs();
        FileUtilities.writeToFile(new File(dataSetExample, "data.txt"), "hello");
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
                    DataSetOwner owner = new DataSetOwner(DataSetOwnerType.EXPERIMENT, "/s/p/e");
                    FileInfoDssDTO info = new FileInfoDssDTO("data.txt", "data.txt", false, 5);
                    final NewDataSetDTO newDataSetDTO =
                            new NewDataSetDTO("MY_TYPE", owner, dataSetExample.getName(), Arrays
                                    .asList(info));
                    BaseMatcher<NewDataSetDTO> newDataSetDtoMatcher =
                            new BaseMatcher<NewDataSetDTO>()
                                {

                                    @Override
                                    public boolean matches(Object item)
                                    {
                                        assertEquals(newDataSetDTO.toString(), item.toString());
                                        return true;
                                    }

                                    @Override
                                    public void describeTo(Description description)
                                    {
                                    }
                                };
                    one(dssComponent).putDataSet(with(newDataSetDtoMatcher),
                            with(equal(dataSetExample)));
                    will(returnValue(dataSet));

                    one(dssComponent).validateDataSet(with(newDataSetDtoMatcher),
                            with(equal(dataSetExample)));
                    will(returnValue(new ArrayList<ValidationError>()));

                    one(dataSet).getCode();
                    will(returnValue("ds1"));

                    one(dssComponent).logout();
                }
            });
        ICommand command = new MockCommandPut();

        ResultCode exitCode =
                command.execute(new String[]
                { "-s", "url", "-u", "user", "-p", "pswd", "-t", "my_type", "EXPERIMENT",
                        "/s/p/e", dataSetExample.getPath() });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }
}
