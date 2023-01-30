/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IPredicate} based on a {@link NewDataSetsWithTypes}.
 * 
 * @author Izabela Adamczyk
 */
@ShouldFlattenCollections(value = false)
public class NewDataSetsWithTypePredicate extends AbstractPredicate<NewDataSetsWithTypes>
{
    private final DataSetCodeCollectionPredicate dataSetCodeCollectionPredicate;

    private final SampleAugmentedCodePredicate sampleIdentifierPredicate;

    private final ExperimentAugmentedCodePredicate experimentIdentifierPredicate;

    public NewDataSetsWithTypePredicate()
    {
        dataSetCodeCollectionPredicate = new DataSetCodeCollectionPredicate();
        sampleIdentifierPredicate = new SampleAugmentedCodePredicate(false);
        experimentIdentifierPredicate = new ExperimentAugmentedCodePredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        dataSetCodeCollectionPredicate.init(provider);
        sampleIdentifierPredicate.init(provider);
        experimentIdentifierPredicate.init(provider);
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            NewDataSetsWithTypes dataSets)
    {
        if (dataSets == null || dataSets.getNewDataSets() == null || dataSets.getNewDataSets().isEmpty())
        {
            return Status.OK;
        }

        Status status = dataSetCodeCollectionPredicate.doEvaluation(person, allowedRoles, getDataSetCodes(dataSets.getNewDataSets()));
        if (status.isError())
        {
            return status;
        }

        Collection<String> sampleIdentifiers = getSampleIdentifiers(dataSets.getNewDataSets());
        for (String sampleIdentifier : sampleIdentifiers)
        {
            status = sampleIdentifierPredicate.doEvaluation(person, allowedRoles, sampleIdentifier);
            if (status.isError())
            {
                return status;
            }
        }

        Collection<String> experimentIdentifiers = getExperimentIdentifiers(dataSets.getNewDataSets());
        for (String experimentIdentifier : experimentIdentifiers)
        {
            status = experimentIdentifierPredicate.doEvaluation(person, allowedRoles, experimentIdentifier);
            if (status.isError())
            {
                return status;
            }
        }

        status = dataSetCodeCollectionPredicate.doEvaluation(person, allowedRoles, getParentCodes(dataSets.getNewDataSets()));
        if (status.isError())
        {
            return status;
        }

        status = dataSetCodeCollectionPredicate.doEvaluation(person, allowedRoles, getContainerCodes(dataSets.getNewDataSets()));
        if (status.isError())
        {
            return status;
        }

        return status;

    }

    private List<String> getDataSetCodes(List<NewDataSet> dataSets)
    {
        Set<String> dataSetCodes = new HashSet<String>();

        for (NewDataSet dataSet : dataSets)
        {
            if (dataSet.getCode() != null)
            {
                dataSetCodes.add(dataSet.getCode());
            }
        }

        return new ArrayList<String>(dataSetCodes);
    }

    private Collection<String> getSampleIdentifiers(List<NewDataSet> dataSets)
    {
        Set<String> sampleIdentifiers = new HashSet<String>();

        for (NewDataSet dataSet : dataSets)
        {
            if (dataSet.getSampleIdentifierOrNull() != null)
            {
                sampleIdentifiers.add(dataSet.getSampleIdentifierOrNull());
            }
        }

        return sampleIdentifiers;
    }

    private Collection<String> getExperimentIdentifiers(List<NewDataSet> dataSets)
    {
        Set<String> experimentIdentifiers = new HashSet<String>();

        for (NewDataSet dataSet : dataSets)
        {
            if (dataSet.getExperimentIdentifier() != null)
            {
                experimentIdentifiers.add(dataSet.getExperimentIdentifier());
            }
        }

        return experimentIdentifiers;
    }

    private List<String> getParentCodes(List<NewDataSet> dataSets)
    {
        Set<String> parentCodes = new HashSet<String>();

        for (NewDataSet dataSet : dataSets)
        {
            if (dataSet.getParentsIdentifiersOrNull() != null)
            {
                for (String parentCode : dataSet.getParentsIdentifiersOrNull())
                {
                    parentCodes.add(parentCode);
                }
            }
        }

        return new ArrayList<String>(parentCodes);
    }

    private List<String> getContainerCodes(List<NewDataSet> dataSets)
    {
        Set<String> containerCodes = new HashSet<String>();

        for (NewDataSet dataSet : dataSets)
        {
            if (dataSet.getContainerIdentifierOrNull() != null)
            {
                containerCodes.add(dataSet.getContainerIdentifierOrNull());
            }
        }

        return new ArrayList<String>(containerCodes);
    }

    @Override
    public String getCandidateDescription()
    {
        return "new data sets";
    }

}
