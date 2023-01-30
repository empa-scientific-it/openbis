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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * The predicate for the {@link AtomicEntityOperationDetails}. This check is always being performed as the user
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicOperationsPredicate extends AbstractPredicate<AtomicEntityOperationDetails>
{

    private final NewExperimentPredicate newExperimentPredicate;

    private final ExperimentUpdatesPredicate experimentUpdatesPredicate;

    private final NewSamplePredicate newSamplePredicate;

    private final SampleUpdatesCollectionPredicate sampleUpdatesPredicate;

    private final SampleIdentifierPredicate sampleIdentifierPredicate;

    private final ExperimentAugmentedCodePredicate experimentIdentifierPredicate;

    private final DataSetUpdatesCollectionPredicate dataSetUpdatesCollectionPredicate;

    private final NewProjectPredicate newProjectPredicate;

    private final ProjectUpdatesPredicate projectUpdatePredicate;

    public AtomicOperationsPredicate()
    {
        newExperimentPredicate = new NewExperimentPredicate();
        experimentUpdatesPredicate = new ExperimentUpdatesPredicate();
        newSamplePredicate = new NewSamplePredicate();
        sampleUpdatesPredicate = new SampleUpdatesCollectionPredicate();
        sampleIdentifierPredicate = new SampleIdentifierPredicate(true, true);
        experimentIdentifierPredicate = new ExperimentAugmentedCodePredicate(true);
        dataSetUpdatesCollectionPredicate = new DataSetUpdatesCollectionPredicate();
        newProjectPredicate = new NewProjectPredicate();
        projectUpdatePredicate = new ProjectUpdatesPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        newExperimentPredicate.init(provider);
        experimentUpdatesPredicate.init(provider);
        newSamplePredicate.init(provider);
        sampleUpdatesPredicate.init(provider);
        sampleIdentifierPredicate.init(provider);
        experimentIdentifierPredicate.init(provider);
        dataSetUpdatesCollectionPredicate.init(provider);
        newProjectPredicate.init(provider);
        projectUpdatePredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "atomic entity operations";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            AtomicEntityOperationDetails value)
    {
        AtomicOperationsPredicateEvaluator evaluator =
                new AtomicOperationsPredicateEvaluator(this, person, allowedRoles, value);
        return evaluator.evaluate();
    }

    /**
     * A class to evaluate all the sub-predicates.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class AtomicOperationsPredicateEvaluator
    {
        private final AtomicOperationsPredicate predicate;

        private final PersonPE person;

        private final List<RoleWithIdentifier> allowedRoles;

        private final AtomicEntityOperationDetails value;

        private final Status instanceWriteStatus;

        private final IServiceConversationProgressListener progressListener;

        private Status result = Status.OK;

        public AtomicOperationsPredicateEvaluator(AtomicOperationsPredicate predicate,
                PersonPE person, List<RoleWithIdentifier> allowedRoles,
                AtomicEntityOperationDetails value)
        {
            this.predicate = predicate;
            this.person = person;
            this.allowedRoles = allowedRoles;
            this.value = value;
            this.instanceWriteStatus = hasInstanceWritePermissions(person, allowedRoles);
            this.progressListener = ServiceConversationsThreadContext.getProgressListener();

        }

        public Status evaluate()
        {
            // Skip all further checks if the person has instance-wide write permissions.
            if (instanceWriteStatus.isOK())
            {
                return instanceWriteStatus;
            }

            // Evaluate all the predicates, stopping if we find an operation that is not allowed
            if (result.equals(Status.OK))
            {
                result = evaluateExperimentUpdatePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateNewExperimentPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateSampleUpdatePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateNewSamplePredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateDataSetRegistrationsPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateDataSetUpdatesPredicate();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateSpaceRegistrations();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateProjectRegistrations();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateProjectUpdates();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateMaterialRegistrations();
            }
            if (result.equals(Status.OK))
            {
                result = evaluateMaterialUpdates();
            }

            return result;
        }

        private Status evaluateSpaceRegistrations()
        {
            if (value.getSpaceRegistrations() != null && value.getSpaceRegistrations().size() > 0)
            {
                return instanceWriteStatus;
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateProjectRegistrations()
        {
            if (value.getProjectRegistrations() != null
                    && value.getProjectRegistrations().size() > 0)
            {
                int index = 0;
                for (NewProject newProject : value.getProjectRegistrations())
                {
                    Status status;

                    status =
                            predicate.newProjectPredicate.doEvaluation(person, allowedRoles,
                                    newProject);
                    if (status.equals(Status.OK) == false)
                    {
                        return status;
                    }
                    progressListener.update("authorizeProjectRegistrations", value
                            .getExperimentUpdates().size(), ++index);
                }
                return Status.OK;
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateProjectUpdates()
        {
            if (value.getProjectUpdates() != null
                    && value.getProjectUpdates().size() > 0)
            {
                int index = 0;
                for (ProjectUpdatesDTO projectToUpdate : value.getProjectUpdates())
                {
                    Status status;

                    status =
                            predicate.projectUpdatePredicate.doEvaluation(person, allowedRoles,
                                    projectToUpdate);
                    if (status.equals(Status.OK) == false)
                    {
                        return status;
                    }
                    progressListener.update("authorizeProjectUpdates", value
                            .getExperimentUpdates().size(), ++index);
                }
                return Status.OK;
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateMaterialRegistrations()
        {
            if (value.getMaterialRegistrations() != null
                    && value.getMaterialRegistrations().size() > 0)
            {
                return instanceWriteStatus;
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateMaterialUpdates()
        {
            if (value.getMaterialUpdates() != null && value.getMaterialUpdates().size() > 0)
            {
                return instanceWriteStatus;
            } else
            {
                return Status.OK;
            }
        }

        private Status evaluateExperimentUpdatePredicate()
        {
            int index = 0;
            for (ExperimentUpdatesDTO experimentUpdates : value.getExperimentUpdates())
            {
                Status status;

                status =
                        predicate.experimentUpdatesPredicate.doEvaluation(person, allowedRoles,
                                experimentUpdates);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
                progressListener.update("authorizeExperimentUpdates", value.getExperimentUpdates()
                        .size(), ++index);
            }
            return Status.OK;
        }

        private Status evaluateNewExperimentPredicate()
        {
            int index = 0;
            for (NewExperiment newExperiment : value.getExperimentRegistrations())
            {
                Status status;

                status =
                        predicate.newExperimentPredicate.doEvaluation(person, allowedRoles,
                                newExperiment);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
                progressListener.update("authorizeExperimentRegistration", value
                        .getExperimentUpdates().size(), ++index);
            }
            return Status.OK;
        }

        private Status evaluateSampleUpdatePredicate()
        {
            return predicate.sampleUpdatesPredicate.doEvaluation(person, allowedRoles,
                    value.getSampleUpdates());
        }

        private Status evaluateNewSamplePredicate()
        {
            int index = 0;
            for (NewSample newSample : value.getSampleRegistrations())
            {
                Status status =
                        predicate.newSamplePredicate.doEvaluation(person, allowedRoles, newSample);
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
                progressListener.update("authorizeSampleRegistration", value.getExperimentUpdates()
                        .size(), ++index);
            }
            return Status.OK;
        }

        private Status evaluateDataSetRegistrationsPredicate()
        {
            int index = 0;
            for (NewExternalData newExternalData : value.getDataSetRegistrations())
            {
                Status status =
                        evaluateDataSetPredicate(newExternalData.getSampleIdentifierOrNull(),
                                newExternalData.getExperimentIdentifierOrNull());
                if (status.equals(Status.OK) == false)
                {
                    return status;
                }
                progressListener.update("authorizeDatasetRegistration", value
                        .getExperimentUpdates().size(), ++index);
            }
            return Status.OK;
        }

        private Status evaluateDataSetUpdatesPredicate()
        {
            return predicate.dataSetUpdatesCollectionPredicate.doEvaluation(person, allowedRoles,
                    value.getDataSetUpdates());
        }

        private Status evaluateDataSetPredicate(SampleIdentifier sampleIdentifier,
                ExperimentIdentifier experimentIdentifier)
        {
            Status status;
            if (null != sampleIdentifier)
            {
                status =
                        predicate.sampleIdentifierPredicate.doEvaluation(person, allowedRoles,
                                sampleIdentifier);
            } else
            {
                status =
                        predicate.experimentIdentifierPredicate.doEvaluation(person,
                                allowedRoles, experimentIdentifier != null ? experimentIdentifier.toString() : null);
            }
            return status;
        }
    }
}
