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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.dataset;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetCodeUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonPredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class DataSetReferencePredicateSystemTest extends CommonPredicateScreeningSystemTest<DatasetReference>
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected DatasetReference createNonexistentObject(Object param)
    {
        String dataSetCode = DataSetCodeUtil.createNonexistentObject(param);
        return new DatasetReference(-1L, dataSetCode, null, null, null, null, null, null, null, null, null);
    }

    @Override
    protected DatasetReference createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        String dataSetCode = DataSetCodeUtil.createObject(this, spacePE, projectPE, param);
        return new DatasetReference(-1L, dataSetCode, null, null, null, null, null, null, null, null, null);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<DatasetReference> objects, Object param)
    {
        getBean(DataSetPredicateScreeningTestService.class).testDataSetReferencePredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<DatasetReference> getAssertions()
    {
        return new CommonPredicateSystemTestDataSetAssertions<DatasetReference>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, UserFailureException.class, "No data set reference specified.");
                    }
                }
            };
    }

}
