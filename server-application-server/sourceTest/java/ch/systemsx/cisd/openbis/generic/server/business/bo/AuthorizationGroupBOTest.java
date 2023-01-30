/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.AuthorizationGroupBO.IAuthorizationGroupFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * Test cases for corresponding {@link AuthorizationGroupBO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = AuthorizationGroupBO.class)
public final class AuthorizationGroupBOTest extends AbstractBOTest
{
    private IAuthorizationGroupFactory groupFactory;

    @Override
    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        groupFactory = context.mock(IAuthorizationGroupFactory.class);
    }

    private final AuthorizationGroupBO createBO()
    {
        return new AuthorizationGroupBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, groupFactory,
                managedPropertyEvaluatorFactory, null, null);
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testFailDefineNull()
    {
        final AuthorizationGroupBO bo = createBO();
        bo.define(null);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testFailDefineAndSave()
    {
        final AuthorizationGroupBO bo = createBO();
        final NewAuthorizationGroup newAuthorizationGroup = createNewAuthorizationGroup();
        final AuthorizationGroupPE authGroupPE = createAuthorizationGroup();
        context.checking(new Expectations()
            {
                {
                    one(groupFactory).create(newAuthorizationGroup,
                            ManagerTestTool.EXAMPLE_SESSION.tryGetPerson());
                    will(returnValue(authGroupPE));
                }
            });
        bo.define(newAuthorizationGroup);
        context.checking(new Expectations()
            {
                {
                    one(authorizationGroupDAO).create(with(authGroupPE));
                    will(throwException(new DataIntegrityViolationException(
                            "Invalid authorization group")));
                }
            });
        bo.save();
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSave()
    {
        final AuthorizationGroupBO bo = createBO();

        final NewAuthorizationGroup newAuthorizationGroup = createNewAuthorizationGroup();
        final AuthorizationGroupPE authGroupPE = createAuthorizationGroup();
        context.checking(new Expectations()
            {
                {
                    one(groupFactory).create(newAuthorizationGroup,
                            ManagerTestTool.EXAMPLE_SESSION.tryGetPerson());
                    will(returnValue(authGroupPE));
                }
            });
        bo.define(newAuthorizationGroup);

        context.checking(new Expectations()
            {
                {
                    one(authorizationGroupDAO).create(with(authGroupPE));
                }
            });
        bo.save();

        context.assertIsSatisfied();
    }

    private AuthorizationGroupPE createAuthorizationGroup()
    {
        return new AuthorizationGroupPE();
    }

    private NewAuthorizationGroup createNewAuthorizationGroup()
    {
        return new NewAuthorizationGroup();
    }

}
