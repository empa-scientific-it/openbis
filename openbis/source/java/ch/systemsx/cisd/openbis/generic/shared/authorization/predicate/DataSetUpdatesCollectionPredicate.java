/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdCollectionPredicate.DataSetTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Predicate for lists of {@link DataSetUpdatesDTO} instances. Checks that the user has update
 * rights for all data sets. In addition for all data sets with changed samples or experiments it is
 * check that the user has access rights for those samples and experiments.
 * 
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(value = false)
public class DataSetUpdatesCollectionPredicate extends
        AbstractPredicate<List<? extends DataSetUpdatesDTO>>
{
    private final DataSetTechIdCollectionPredicate dataSetTechIdCollectionPredicate;

    private final SpaceIdentifierPredicate spacePredicate;

    private final SampleOwnerIdentifierCollectionPredicate sampleCollectionPredicate;

    public DataSetUpdatesCollectionPredicate()
    {
        dataSetTechIdCollectionPredicate = new DataSetTechIdCollectionPredicate();
        sampleCollectionPredicate = new SampleOwnerIdentifierCollectionPredicate();
        spacePredicate = new SpaceIdentifierPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        dataSetTechIdCollectionPredicate.init(provider);
        sampleCollectionPredicate.init(provider);
        spacePredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "data set updates collection";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<? extends DataSetUpdatesDTO> value)
    {
        List<TechId> techIds = new ArrayList<TechId>();
        List<SampleOwnerIdentifier> sampleIdentifiers = new ArrayList<SampleOwnerIdentifier>();
        for (DataSetUpdatesDTO dataSetUpdates : value)
        {
            techIds.add(dataSetUpdates.getDatasetId());
            ExperimentIdentifier experimentIdentifier =
                    dataSetUpdates.getExperimentIdentifierOrNull();
            if (experimentIdentifier != null)
            {
                Status result =
                        spacePredicate.doEvaluation(person, allowedRoles, experimentIdentifier);
                if (result.isOK() == false)
                {
                    return result;
                }
            }
            SampleIdentifier sampleIdentifier = dataSetUpdates.getSampleIdentifierOrNull();
            if (sampleIdentifier != null)
            {
                sampleIdentifiers.add(sampleIdentifier);
            }
        }
        Status result =
                dataSetTechIdCollectionPredicate.doEvaluation(person, allowedRoles, techIds);
        if (result.isOK() == false)
        {
            return result;
        }
        return sampleCollectionPredicate.doEvaluation(person, allowedRoles, sampleIdentifiers);
    }
}
