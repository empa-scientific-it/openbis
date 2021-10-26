/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.deletion;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DeletionUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.deletion.DeletionPredicateTestService;

/**
 * @author pkupczyk
 */
public abstract class DeletionTechIdCollectionPredicateSystemTest extends CommonPredicateSystemTest<TechId>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected TechId createNonexistentObject(Object param)
    {
        return DeletionUtil.createNonexistentObject(param);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<TechId> objects, Object param)
    {
        try
        {
            getBean(DeletionPredicateTestService.class).testDeletionTechIdCollectionPredicate(user.getSessionProvider(), objects);
        } finally
        {
            if (objects != null)
            {
                for (TechId object : objects)
                {
                    if (object != null && !object.equals(this.createNonexistentObject(param)))
                    {
                        getCommonService().untrash(object.getId());
                    }
                }
            }
        }
    }

    @Override
    protected CommonPredicateSystemTestAssertions<TechId> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<TechId>(super.getAssertions())
        {
            @Override
            public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
            {
                if (user.isDisabledProjectUser())
                {
                    assertAuthorizationFailureExceptionThatNoRoles(t);
                } else
                {
                    assertException(t, NullPointerException.class, null);
                }
            }

            @Override
            public void assertWithNullCollection(ProjectAuthorizationUser user, Throwable t, Object param)
            {
                if (user.isDisabledProjectUser())
                {
                    assertAuthorizationFailureExceptionThatNoRoles(t);
                } else
                {
                    assertException(t, UserFailureException.class, "No deletion technical id specified.");
                }
            }
        };
    }

}