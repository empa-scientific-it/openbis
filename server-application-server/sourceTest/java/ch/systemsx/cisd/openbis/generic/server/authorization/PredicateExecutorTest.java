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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Test cases for corresponding {@link PredicateExecutor} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = PredicateExecutor.class)
public final class PredicateExecutorTest extends AuthorizationTestCase
{

    private IPredicate<String> stringPredicate;

    private IPredicate<List<String>> stringCollectionPredicate;

    private IPredicateFactory predicateFactory;

    private IAuthorizationDAOFactory daoFactory;

    private PredicateExecutor predicateExecutor;

    private List<RoleWithIdentifier> createAllowedRoles()
    {
        return Collections.singletonList(createSpaceRole(RoleCode.USER, new SpaceIdentifier("3V")));
    }

    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        predicateFactory = context.mock(IPredicateFactory.class);
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        predicateExecutor = new PredicateExecutor();
        predicateExecutor.setPredicateFactory(predicateFactory);
        predicateExecutor.setDAOFactory(daoFactory);
    }

    /**
     * Called by tests that use the string predicate
     */
    @SuppressWarnings("unchecked")
    public void setUpStringPredicate()
    {
        stringPredicate = context.mock(IPredicate.class);
    }

    /**
     * Called by tests that use the string predicate
     */
    @SuppressWarnings("unchecked")
    public void setUpStringCollectionPredicate()
    {
        stringCollectionPredicate = context.mock(IPredicate.class);
    }

    @SuppressWarnings("unchecked")
    private final Class<? extends IPredicate<String>> castToStringPredicateClass()
    {
        return (Class<? extends IPredicate<String>>) stringPredicate.getClass();
    }

    @SuppressWarnings("unchecked")
    private final Class<? extends IPredicate<List<String>>> castToStringCollectionPredicateClass()
    {
        return (Class<? extends IPredicate<List<String>>>) stringCollectionPredicate.getClass();
    }

    @Test
    public final void testEvaluateWithSimpleObject()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final String value = StringUtilities.getString();
        setUpStringPredicate();
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(castToStringPredicateClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(with(any(IAuthorizationDataProvider.class)));

                    one(stringPredicate).evaluate(person, allowedRoles, value);
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, predicateExecutor.evaluate(person, allowedRoles, value,
                castToStringPredicateClass(), String.class, true));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEvaluateWithArray()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final String[] array = StringUtilities.getStrings(2);
        setUpStringPredicate();
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(castToStringPredicateClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(with(any(IAuthorizationDataProvider.class)));

                    one(stringPredicate).evaluate(person, allowedRoles, array[0]);
                    will(returnValue(Status.OK));

                    one(stringPredicate).evaluate(person, allowedRoles, array[1]);
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, predicateExecutor.evaluate(person, allowedRoles, array,
                castToStringPredicateClass(), String[].class, true));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEvaluateWithCollection()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final List<String> list = Arrays.asList(StringUtilities.getStrings(2));
        setUpStringPredicate();
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(castToStringPredicateClass());
                    will(returnValue(stringPredicate));

                    one(stringPredicate).init(with(any(IAuthorizationDataProvider.class)));

                    one(stringPredicate).evaluate(person, allowedRoles, list.get(0));
                    will(returnValue(Status.OK));

                    one(stringPredicate).evaluate(person, allowedRoles, list.get(1));
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, predicateExecutor.evaluate(person, allowedRoles, list,
                castToStringPredicateClass(), List.class, true));
        context.assertIsSatisfied();
    }

    @Test
    public final void testEvaluateWithCollectionNonFlattened()
    {
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> allowedRoles = createAllowedRoles();
        final List<String> list = Arrays.asList(StringUtilities.getStrings(2));
        setUpStringCollectionPredicate();
        context.checking(new Expectations()
            {
                {
                    one(predicateFactory).createPredicateForClass(
                            castToStringCollectionPredicateClass());
                    will(returnValue(stringCollectionPredicate));

                    one(stringCollectionPredicate)
                            .init(with(any(IAuthorizationDataProvider.class)));

                    one(stringCollectionPredicate).evaluate(person, allowedRoles, list);
                    will(returnValue(Status.OK));
                }
            });
        assertEquals(Status.OK, predicateExecutor.evaluate(person, allowedRoles, list,
                castToStringCollectionPredicateClass(), List.class, false));
        context.assertIsSatisfied();
    }
}