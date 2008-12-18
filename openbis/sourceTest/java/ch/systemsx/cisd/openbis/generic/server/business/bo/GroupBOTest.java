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

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * Test cases for corresponding {@link GroupBO} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupBOTest
{
    private BufferedAppender logRecorder;

    private Mockery context;

    private IDAOFactory daoFactory;

    private IGroupDAO groupDAO;

    private final GroupBO createGroupBO()
    {
        return new GroupBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        groupDAO = context.mock(IGroupDAO.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testSaveWithNullGroup()
    {
        boolean fail = true;
        try
        {
            createGroupBO().save();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithNullCode()
    {
        final GroupBO projectBO = createGroupBO();
        boolean fail = true;
        try
        {
            projectBO.define(null, null, null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testDefineAndSave()
    {
        final GroupBO projectBO = createGroupBO();
        final DatabaseInstancePE instance = new DatabaseInstancePE();
        final GroupPE groupDTO = new GroupPE();
        groupDTO.setCode("MY_CODE");
        groupDTO.setDatabaseInstance(instance);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(instance));

                    one(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));

                    one(groupDAO).createGroup(groupDTO);
                }
            });
        projectBO.define(groupDTO.getCode(), null, null);
        projectBO.save();
        context.assertIsSatisfied();
    }
}