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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Predicate for {@link NewExternalData} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class NewExternalDataPredicate extends AbstractPredicate<NewExternalData>
{
    private final SampleIdentifierPredicate sampleIdentifierPredicate;

    private final SamplePermIdStringPredicate samplePermIdPredicate;

    private final ExperimentAugmentedCodePredicate experimentIdentifierPredicate;

    private final DataSetCodeCollectionPredicate dataSetCodeCollectionPredicate;

    public NewExternalDataPredicate()
    {
        sampleIdentifierPredicate = new SampleIdentifierPredicate(false, true);
        samplePermIdPredicate = new SamplePermIdStringPredicate(false);
        experimentIdentifierPredicate = new ExperimentAugmentedCodePredicate(true);
        dataSetCodeCollectionPredicate = new DataSetCodeCollectionPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        sampleIdentifierPredicate.init(provider);
        samplePermIdPredicate.init(provider);
        experimentIdentifierPredicate.init(provider);
        dataSetCodeCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "new data set";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            NewExternalData value)
    {
        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        if (value.getSampleIdentifierOrNull() != null)
        {
            Status status = sampleIdentifierPredicate.evaluate(person, allowedRoles, value.getSampleIdentifierOrNull());
            if (status.isError())
            {
                return status;
            }
        }

        if (value.getSamplePermIdOrNull() != null)
        {
            Status status = samplePermIdPredicate.evaluate(person, allowedRoles, value.getSamplePermIdOrNull());
            if (status.isError())
            {
                return status;
            }
        }

        if (value.getExperimentIdentifierOrNull() != null)
        {
            Status status = experimentIdentifierPredicate.evaluate(person, allowedRoles, value.getExperimentIdentifierOrNull().toString());
            if (status.isError())
            {
                return status;
            }
        }

        if (value.getParentDataSetCodes() != null)
        {
            Status status = dataSetCodeCollectionPredicate.evaluate(person, allowedRoles, value.getParentDataSetCodes());
            if (status.isError())
            {
                return status;
            }
        }

        return Status.OK;
    }

}
