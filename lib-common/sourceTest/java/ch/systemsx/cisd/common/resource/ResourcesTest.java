/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.resource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class ResourcesTest
{

    @Test
    public void testReleasingShouldReleaseAllResourcesEvenIfSomeOfThemAreFailing()
    {
        Mockery context = new Mockery();

        final IReleasable failingResource1 = context.mock(IReleasable.class, "failingResource1");
        final IReleasable failingResource2 = context.mock(IReleasable.class, "failingResource2");
        final IReleasable notFailingResource1 = context.mock(IReleasable.class, "notFailingResource1");
        final IReleasable notFailingResource2 = context.mock(IReleasable.class, "notFailingResource2");

        Resources resources = new Resources();
        resources.add(notFailingResource1);
        resources.add(failingResource1);
        resources.add(failingResource2);
        resources.add(notFailingResource2);

        context.checking(new Expectations()
            {
                {
                    one(notFailingResource1).release();

                    one(failingResource1).release();
                    will(throwException(new RuntimeException("Cannot release resource 1 !!!")));

                    one(failingResource2).release();
                    will(throwException(new RuntimeException("Cannot release resource 2 !!!")));

                    one(notFailingResource2).release();
                }
            });

        resources.release();

        context.assertIsSatisfied();
    }

    @Test
    public void testReleasingTwiceWithoutClearShouldReleaseOnce()
    {
        Mockery context = new Mockery();

        final IReleasable resource = context.mock(IReleasable.class);

        Resources resources = new Resources();
        resources.add(resource);

        context.checking(new Expectations()
            {
                {
                    one(resource).release();
                }
            });

        resources.release();
        resources.release();

        context.assertIsSatisfied();
    }

}
