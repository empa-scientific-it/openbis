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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.entity;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetCodeUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class BasicEntityDescriptionPredicateWithDataSetSystemTest extends BasicEntityDescriptionPredicateSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected BasicEntityDescription createNonexistentObject(Object param)
    {
        String code = DataSetCodeUtil.createNonexistentObject(param);
        return new BasicEntityDescription(EntityKind.DATA_SET, code);
    }

    @Override
    protected BasicEntityDescription createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        DataPE dataSetPE = getDataSet(spacePE, projectPE, (DataSetKind) param);
        return new BasicEntityDescription(EntityKind.DATA_SET, dataSetPE.getCode());
    }

    @Override
    protected CommonPredicateSystemTestAssertions<BasicEntityDescription> getAssertions()
    {
        return new CommonPredicateSystemTestDataSetAssertions<BasicEntityDescription>(super.getAssertions());
    }

}
