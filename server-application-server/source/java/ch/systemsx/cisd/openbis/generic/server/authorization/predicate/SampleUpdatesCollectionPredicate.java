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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Predicate for a list of {@link SampleUpdatesDTO} instances. The logical is similar to {@link SampleUpdatesPredicate}.
 * 
 * @author Franz-Josef Elmer
 */
@ShouldFlattenCollections(value = false)
public class SampleUpdatesCollectionPredicate extends AbstractPredicate<List<SampleUpdatesDTO>>
{

    private final ProjectIdentifierPredicate projectIdentifierPredicate;

    private final SampleTechIdCollectionPredicate sampleTechIdCollectionPredicate;

    private final SampleIdentifierCollectionPredicate sampleIdentifierCollectionPredicate;

    public SampleUpdatesCollectionPredicate()
    {
        this.projectIdentifierPredicate = new ProjectIdentifierPredicate();
        this.sampleTechIdCollectionPredicate = new SampleTechIdCollectionPredicate(false);
        this.sampleIdentifierCollectionPredicate = new SampleIdentifierCollectionPredicate(false);
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        projectIdentifierPredicate.init(provider);
        sampleTechIdCollectionPredicate.init(provider);
        sampleIdentifierCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample updates collection";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<SampleUpdatesDTO> value)
    {
        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        IServiceConversationProgressListener progressListener =
                ServiceConversationsThreadContext.getProgressListener();
        List<TechId> techIds = new ArrayList<TechId>(value.size());
        List<SampleIdentifier> containerIdentifiers = new ArrayList<SampleIdentifier>();

        int index = 0;
        for (SampleUpdatesDTO sampleUpdates : value)
        {
            if (sampleUpdates == null)
            {
                throw UserFailureException.fromTemplate("No sample updates specified.");
            }
            TechId sampleId = sampleUpdates.getSampleIdOrNull();
            if (sampleId != null)
            {
                techIds.add(sampleId);
            }
            Status result = SampleUpdatesPredicate.evaluateBasedOnExperimentOrProject(projectIdentifierPredicate,
                    person, allowedRoles, sampleUpdates);
            if (result.isOK() == false)
            {
                return result;
            }

            if (sampleUpdates.getContainerIdentifierOrNull() != null)
            {
                SampleIdentifier containerIdentifier = SampleIdentifierFactory.parse(sampleUpdates.getContainerIdentifierOrNull());
                containerIdentifiers.add(containerIdentifier);
            }

            progressListener.update("authorizeSampleUpdates", value.size(), ++index);
        }

        Status result = sampleTechIdCollectionPredicate.doEvaluation(person, allowedRoles, techIds);
        if (result.isOK() == false)
        {
            return result;
        }

        if (false == containerIdentifiers.isEmpty())
        {
            result = sampleIdentifierCollectionPredicate.doEvaluation(person, allowedRoles, containerIdentifiers);
            if (result.isOK() == false)
            {
                return result;
            }
        }

        return result;
    }

}
