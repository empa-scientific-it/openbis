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

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetCodeUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonDataSetPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset.NewDataSetsWithTypePredicateUtil.NewDataSetField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class NewDataSetsWithTypePredicateWithParentIdentifiersSystemTest extends CommonDataSetPredicateSystemTest<String>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected String createNonexistentObject(Object param)
    {
        return DataSetCodeUtil.createNonexistentObject(param);
    }

    @Override
    protected String createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return DataSetCodeUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<String> objects, Object param)
    {
        NewDataSetsWithTypePredicateUtil.evaluateObjects(sessionProvider, objects, new NewDataSetField<String>()
            {
                @Override
                public void set(NewDataSet dataSet, String parentCode)
                {
                    dataSet.setParentsIdentifiersOrNull(parentCode);
                }
            });
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
