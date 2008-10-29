/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link ExternalDataTable} class.
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataTableTest
{

    private Mockery context;

    private IDAOFactory daoFactory;

    private IExternalDataDAO externalDataDAO;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    private ISampleDAO sampleDAO;

    private final ExternalDataTable createExternalDataTable()
    {
        return new ExternalDataTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        sampleDAO = context.mock(ISampleDAO.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        externalDataDAO = context.mock(IExternalDataDAO.class);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadBySampleIdentifierWithNullSampleIdentifier()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        boolean fail = true;
        try
        {
            externalDataTable.loadBySampleIdentifier(null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        fail = true;
        try
        {
            externalDataTable.getExternalData();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadBySampleIdentifierFailed()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        final String sampleCode = "CP-01";
        final String dbCode = "DB-1";
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(dbCode), sampleCode);
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode(dbCode);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));

                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(dbCode);
                    will(returnValue(databaseInstancePE));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstancePE);
                }
            });
        try
        {
            externalDataTable.loadBySampleIdentifier(sampleIdentifier);
            fail("'" + UserFailureException.class.getName() + "' expected.");
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadBySampleIdentifier()
    {
        final ExternalDataTable externalDataTable = createExternalDataTable();
        final String sampleCode = "CP-01";
        final String dbCode = "DB-1";
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(dbCode), sampleCode);
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode(dbCode);
        final SamplePE sample = new SamplePE();
        sample.setCode(sampleCode);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    allowing(daoFactory).getExternalDataDAO();
                    will(returnValue(externalDataDAO));

                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(dbCode);
                    will(returnValue(databaseInstancePE));

                    one(sampleDAO).tryFindByCodeAndDatabaseInstance(sampleCode, databaseInstancePE);
                    will(returnValue(sample));

                    one(externalDataDAO).listExternalData(sample, SourceType.DERIVED);
                    one(externalDataDAO).listExternalData(sample, SourceType.MEASUREMENT);
                }
            });
        externalDataTable.loadBySampleIdentifier(sampleIdentifier);
    }
}
