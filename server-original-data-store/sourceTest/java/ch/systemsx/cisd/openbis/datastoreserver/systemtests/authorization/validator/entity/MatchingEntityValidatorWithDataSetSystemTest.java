/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.entity;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class MatchingEntityValidatorWithDataSetSystemTest extends MatchingEntityValidatorSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected MatchingEntity createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        DataPE dataSetPE = getDataSet(spacePE, projectPE, (DataSetKind) param);

        Space space = null;

        if (dataSetPE.getSpace() != null)
        {
            space = new Space();
            space.setCode(dataSetPE.getSpace().getCode());
        }

        MatchingEntity entity = new MatchingEntity();
        entity.setEntityKind(EntityKind.DATA_SET);
        entity.setId(dataSetPE.getId());
        entity.setSpace(space);

        return entity;
    }

    @Override
    protected CommonValidatorSystemTestAssertions<MatchingEntity> getAssertions()
    {
        return new CommonValidatorSystemTestDataSetAssertions<MatchingEntity>(super.getAssertions());
    }

}
