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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public abstract class NewDataSetsWithTypePredicateSystemTest<O> extends CommonPredicateSystemTest<O>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    protected void evaluateObjects(ProjectAuthorizationUser user, List<O> objects, Object param, NewDataSetField<O> field)
    {
        List<NewDataSet> newDataSets = null;

        // if objects == null then we want newDataSets == null
        if (objects != null)
        {
            newDataSets = new ArrayList<NewDataSet>();

            for (O object : objects)
            {
                NewDataSet newDataSet = null;

                // if object == null then we want newDataSet == null
                if (object != null)
                {
                    newDataSet = new NewDataSet();
                    field.set(newDataSet, object);
                }

                newDataSets.add(newDataSet);
            }
        }

        NewDataSetsWithTypes newDataSetsWithTypes = new NewDataSetsWithTypes();
        newDataSetsWithTypes.setNewDataSets(newDataSets);

        getBean(DataSetPredicateTestService.class).testNewDataSetsWithTypePredicate(user.getSessionProvider(), newDataSetsWithTypes);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<O> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<O>(super.getAssertions())
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
                        assertNoException(t);
                    }
                }
            };
    }

    public static interface NewDataSetField<O>
    {

        public void set(NewDataSet dataSet, O value);

    }

}
