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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.ExternalDataUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class EntityHistoryValidatorWithDataSetSystemTest extends EntityHistoryValidatorSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected EntityHistory createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        EntityHistory history = new EntityHistory();
        history.setRelatedEntity(ExternalDataUtil.createObject(this, spacePE, projectPE, param));
        return history;
    }

    @Override
    protected CommonValidatorSystemTestAssertions<EntityHistory> getAssertions()
    {
        return new CommonValidatorSystemTestDataSetAssertions<EntityHistory>(super.getAssertions());
    }

}
