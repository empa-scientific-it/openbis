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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.ExperimentIdentifierUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleIdentifierUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * @author pkupczyk
 */
public class AtomicOperationsPredicateWithDataSetRegistrationSystemTest extends AtomicOperationsPredicateSystemTest
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected AtomicEntityOperationDetails createNonexistentObject(Object param)
    {
        NewExternalData newDataSet = new NewExternalData();

        switch ((DataSetKind) param)
        {
            case EXPERIMENT:
                newDataSet.setExperimentIdentifierOrNull(ExperimentIdentifierUtil.createNonexistentObject(null));
                break;
            case SPACE_SAMPLE:
                newDataSet.setSampleIdentifierOrNull(SampleIdentifierUtil.createNonexistentObject(SampleKind.SPACE));
                break;
            case PROJECT_SAMPLE:
                newDataSet.setSampleIdentifierOrNull(SampleIdentifierUtil.createNonexistentObject(SampleKind.PROJECT));
                break;
            case EXPERIMENT_SAMPLE:
                newDataSet.setSampleIdentifierOrNull(SampleIdentifierUtil.createNonexistentObject(SampleKind.EXPERIMENT));
                break;
            default:
                throw new RuntimeException();
        }

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.dataSet(newDataSet);

        return builder.getDetails();
    }

    @Override
    protected AtomicEntityOperationDetails createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        DataPE dataSet = getDataSet(spacePE, projectPE, (DataSetKind) param);
        NewExternalData newDataSet = new NewExternalData();

        if (dataSet.getExperiment() != null)
        {
            newDataSet.setExperimentIdentifierOrNull(IdentifierHelper.createExperimentIdentifier(dataSet.getExperiment()));
        }

        if (dataSet.tryGetSample() != null)
        {
            newDataSet.setSampleIdentifierOrNull(IdentifierHelper.createSampleIdentifier(dataSet.tryGetSample()));
        }

        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.dataSet(newDataSet);

        return builder.getDetails();
    }

    @Override
    protected CommonPredicateSystemTestAssertions<AtomicEntityOperationDetails> getAssertions()
    {
        return new CommonPredicateSystemTestDataSetAssertions<AtomicEntityOperationDetails>(super.getAssertions());
    }

}
