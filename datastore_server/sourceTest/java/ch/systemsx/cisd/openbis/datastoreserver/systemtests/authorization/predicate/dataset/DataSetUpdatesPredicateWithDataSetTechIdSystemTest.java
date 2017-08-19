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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetTechIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonDataSetPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class DataSetUpdatesPredicateWithDataSetTechIdSystemTest extends CommonDataSetPredicateSystemTest<DataSetUpdatesDTO>
{

    @Override
    protected DataSetUpdatesDTO createNonexistentObject(Object param)
    {
        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setDatasetId(DataSetTechIdUtil.createNonexistentObject(param));
        return updates;
    }

    @Override
    protected DataSetUpdatesDTO createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setDatasetId(DataSetTechIdUtil.createObject(this, spacePE, projectPE, param));
        return updates;
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<DataSetUpdatesDTO> objects, Object param)
    {
        getBean(DataSetPredicateTestService.class).testDataSetUpdatesPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No data set updates specified.");
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
