/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
public class ArrayPredicateTest extends AuthorizationTestCase
{
    private IPredicate<String> predicate;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    @Override
    public void setUp()
    {
        super.setUp();
        predicate = context.mock(IPredicate.class);
        context.checking(new Expectations()
            {
                {
                    one(predicate).init(provider);
                }
            });
    }

    @Test
    public void testEmptyArray()
    {
        ArrayPredicate<String> arrayPredicate = new ArrayPredicate<String>(predicate);
        arrayPredicate.init(provider);

        Status status = arrayPredicate.evaluate(createPerson(), createRoles(false), new String[0]);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithTwoElements()
    {
        ArrayPredicate<String> arrayPredicate = new ArrayPredicate<String>(predicate);
        arrayPredicate.init(provider);
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> roles = createRoles(false);
        context.checking(new Expectations()
            {
                {
                    one(predicate).evaluate(person, roles, "a");
                    will(returnValue(Status.OK));

                    one(predicate).evaluate(person, roles, "b");
                    will(returnValue(Status.OK));
                }
            });

        Status status = arrayPredicate.evaluate(person, roles, new String[] { "a", "b" });

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithTwoElementsOneFailing()
    {
        ArrayPredicate<String> arrayPredicate = new ArrayPredicate<String>(predicate);
        arrayPredicate.init(provider);
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> roles = createRoles(false);
        context.checking(new Expectations()
            {
                {
                    one(predicate).evaluate(person, roles, "a");
                    will(returnValue(Status.OK));

                    one(predicate).evaluate(person, roles, "b");
                    will(returnValue(Status.createError()));
                }
            });

        Status status = arrayPredicate.evaluate(person, roles, new String[] { "a", "b" });

        assertEquals(true, status.isError());
        context.assertIsSatisfied();
    }
}
