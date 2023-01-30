/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.search;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public class AbstractSearchCriteriaTranslatorTest
{

    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IEntityPropertyTypeDAO entityPropertyTypeDAO;

    @BeforeMethod
    protected void beforeMethod(Method method)
    {
        System.out.println(">>>>>>>>>>>>>>>>>> BEFORE " + method.getName() + "\n");

        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        entityPropertyTypeDAO = context.mock(IEntityPropertyTypeDAO.class);

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    will(returnValue(entityPropertyTypeDAO));

                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityPropertyTypeDAO));

                    allowing(entityPropertyTypeDAO).listPropertyTypeCodes();
                    will(returnValue(Arrays.asList("PROPERTY", " PROPERTY_2")));
                }
            });
    }

    @AfterMethod
    protected void afterMethod(Method method)
    {
        context.assertIsSatisfied();
        System.out.println("\n<<<<<<<<<<<<<<<<<< AFTER " + method.getName());
    }

}
