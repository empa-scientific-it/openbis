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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.ExperimentIdentifierUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class NewExternalDataPredicateWithExperimentIdentifierSystemTest extends NewExternalDataPredicateSystemTest<ExperimentIdentifier>
{

    @Override
    protected ExperimentIdentifier createNonexistentObject(Object param)
    {
        return ExperimentIdentifierUtil.createNonexistentObject(param);
    }

    @Override
    protected ExperimentIdentifier createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return ExperimentIdentifierUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<ExperimentIdentifier> objects, Object param)
    {
        NewExternalData data = null;

        if (objects.get(0) != null)
        {
            data = new NewExternalData();
            data.setExperimentIdentifierOrNull(objects.get(0));
        }

        getBean(DataSetPredicateTestService.class).testNewExternalDataPredicate(user.getSessionProvider(), data);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<ExperimentIdentifier> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<ExperimentIdentifier>(super.getAssertions());
    }

}
