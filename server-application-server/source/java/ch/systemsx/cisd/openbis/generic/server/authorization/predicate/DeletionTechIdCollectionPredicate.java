/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;

/**
 * @author Pawel Glyzewski
 */
@ShouldFlattenCollections(value = false)
public class DeletionTechIdCollectionPredicate extends AbstractSpacePredicate<List<TechId>>
{

    private ExperimentAccessPECollectionPredicate experimentAccessPECollectionPredicate;

    private SampleAccessPECollectionPredicate sampleAccessPECollectionPredicate;

    private DataSetAccessPECollectionPredicate dataSetAccessPECollectionPredicate;

    public DeletionTechIdCollectionPredicate()
    {
        this.experimentAccessPECollectionPredicate = new ExperimentAccessPECollectionPredicate();
        this.sampleAccessPECollectionPredicate = new SampleAccessPECollectionPredicate(false);
        this.dataSetAccessPECollectionPredicate = new DataSetAccessPECollectionPredicate();
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        experimentAccessPECollectionPredicate.init(provider);
        sampleAccessPECollectionPredicate.init(provider);
        dataSetAccessPECollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "deletion technical id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<TechId> value)
    {
        Set<ExperimentAccessPE> experiments = authorizationDataProvider.getDeletedExperimentCollectionAccessData(value);

        Status experimentStatus = experimentAccessPECollectionPredicate.evaluate(person, allowedRoles, experiments);
        if (experimentStatus != Status.OK)
        {
            return experimentStatus;
        }

        Set<SampleAccessPE> samples = authorizationDataProvider.getDeletedSampleCollectionAccessData(value);

        Status sampleStatus = sampleAccessPECollectionPredicate.evaluate(person, allowedRoles, samples);
        if (sampleStatus != Status.OK)
        {
            return sampleStatus;
        }

        Set<DataSetAccessPE> dataSets = authorizationDataProvider.getDeletedDatasetCollectionAccessData(value);

        Status dataSetStatus = dataSetAccessPECollectionPredicate.evaluate(person, allowedRoles, dataSets);
        if (dataSetStatus != Status.OK)
        {
            return dataSetStatus;
        }

        return Status.OK;
    }
}
