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

package ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.DataSetDataLoader;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.ExperimentDataLoader;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.EntityDataLoaderFactory;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.IEntityDataLoader;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.SampleDataLoader;

/**
 * Test cases for {@link EntityDataLoaderFactory}.
 * 
 * @author Izabela Adamczyk
 */
public class GroupLoaderFactoryTest extends AssertJUnit
{

    private Mockery context;

    private IDAOFactory daoFactory;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateExperimentLoader() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getExperimentDAO();
                    will(returnValue(null));
                }
            });
        IEntityDataLoader loader = new EntityDataLoaderFactory(daoFactory).create(EntityKind.EXPERIMENT);
        assertTrue(loader instanceof ExperimentDataLoader);
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateSampleLoader() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getSampleDAO();
                    will(returnValue(null));
                }
            });
        IEntityDataLoader loader = new EntityDataLoaderFactory(daoFactory).create(EntityKind.SAMPLE);
        assertTrue(loader instanceof SampleDataLoader);
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateDataSetLoader() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDataDAO();
                    will(returnValue(null));
                }
            });
        IEntityDataLoader loader = new EntityDataLoaderFactory(daoFactory).create(EntityKind.DATA_SET);
        assertTrue(loader instanceof DataSetDataLoader);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testFailCreateMaterialLoader() throws Exception
    {
        new EntityDataLoaderFactory(daoFactory).create(EntityKind.MATERIAL);
    }
}
