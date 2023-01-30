/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectIdentifierObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * A {@link IValidator} implementation suitable for {@link MatchingEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntityValidator extends AbstractCollectionValidator<MatchingEntity>
{

    private final IValidator<Space> groupValidator;

    public MatchingEntityValidator()
    {
        groupValidator = new SpaceValidator();
    }

    @Override
    public Collection<MatchingEntity> getValid(PersonPE person, Collection<MatchingEntity> entities)
    {
        if (isInstanceUser(person))
        {
            return entities;
        }

        Set<String> spacesWithValidProjects = getSpacesWithValidProjects(person);

        Set<Long> validMaterialIds = new HashSet<Long>();
        Set<Long> validExperimentIds = new HashSet<Long>();
        Set<Long> validSampleIds = new HashSet<Long>();
        Set<Long> validDataSetIds = new HashSet<Long>();

        Set<Long> potentiallyValidExperimentIds = new HashSet<Long>();
        Set<Long> potentiallyValidSampleIds = new HashSet<Long>();
        Set<Long> potentiallyValidDataSetIds = new HashSet<Long>();

        for (MatchingEntity entity : entities)
        {
            if (entity == null)
            {
                continue;
            }

            if (entity.tryGetSpace() == null)
            {
                if (EntityKind.MATERIAL.equals(entity.getEntityKind()))
                {
                    validMaterialIds.add(entity.getId());
                } else if (EntityKind.SAMPLE.equals(entity.getEntityKind()))
                {
                    validSampleIds.add(entity.getId());
                }
            } else
            {
                boolean valid = groupValidator.isValid(person, entity.tryGetSpace());

                if (valid)
                {
                    if (EntityKind.EXPERIMENT.equals(entity.getEntityKind()))
                    {
                        validExperimentIds.add(entity.getId());
                    } else if (EntityKind.SAMPLE.equals(entity.getEntityKind()))
                    {
                        validSampleIds.add(entity.getId());
                    } else if (EntityKind.DATA_SET.equals(entity.getEntityKind()))
                    {
                        validDataSetIds.add(entity.getId());
                    }
                } else
                {
                    boolean potentiallyValid = spacesWithValidProjects.contains(entity.tryGetSpace().getCode());

                    if (potentiallyValid)
                    {
                        if (EntityKind.EXPERIMENT.equals(entity.getEntityKind()))
                        {
                            potentiallyValidExperimentIds.add(entity.getId());
                        } else if (EntityKind.SAMPLE.equals(entity.getEntityKind()))
                        {
                            potentiallyValidSampleIds.add(entity.getId());
                        } else if (EntityKind.DATA_SET.equals(entity.getEntityKind()))
                        {
                            potentiallyValidDataSetIds.add(entity.getId());
                        }
                    }
                }
            }
        }

        IAuthorizationConfig authorizationConfig = authorizationDataProvider.getAuthorizationConfig();

        if (authorizationConfig.isProjectLevelEnabled() && authorizationConfig.isProjectLevelUser(person.getUserId()))
        {
            Map<ProjectIdentifier, Boolean> validatedProjects = new HashMap<ProjectIdentifier, Boolean>();

            if (false == potentiallyValidExperimentIds.isEmpty())
            {
                Set<ExperimentAccessPE> accessDatas = authorizationDataProvider.getExperimentCollectionAccessData(
                        TechId.createList(new ArrayList<Long>(potentiallyValidExperimentIds)), false);

                for (ExperimentAccessPE accessData : accessDatas)
                {
                    if (isValidProject(person, accessData.getProjectIdentifier(), validatedProjects))
                    {
                        validExperimentIds.add(accessData.getExperimentId());
                    }
                }
            }

            if (false == potentiallyValidSampleIds.isEmpty())
            {
                Set<SampleAccessPE> accessDatas = authorizationDataProvider.getSampleCollectionAccessDataByTechIds(
                        TechId.createList(new ArrayList<Long>(potentiallyValidSampleIds)), false);

                for (SampleAccessPE accessData : accessDatas)
                {
                    if (isValidProject(person, accessData.getProjectIdentifier(), validatedProjects))
                    {
                        validSampleIds.add(accessData.getSampleId());
                    }
                }
            }

            if (false == potentiallyValidDataSetIds.isEmpty())
            {
                Set<DataSetAccessPE> accessDatas = authorizationDataProvider.getDatasetCollectionAccessDataByTechIds(
                        TechId.createList(new ArrayList<Long>(potentiallyValidDataSetIds)), false);

                for (DataSetAccessPE accessData : accessDatas)
                {
                    if (isValidProject(person, accessData.getProjectIdentifier(), validatedProjects))
                    {
                        validDataSetIds.add(accessData.getDataSetId());
                    }
                }
            }
        }

        List<MatchingEntity> validEntities = new ArrayList<MatchingEntity>();

        for (MatchingEntity entity : entities)
        {
            if (entity != null)
            {
                boolean valid = false;

                if (EntityKind.MATERIAL.equals(entity.getEntityKind()))
                {
                    valid = validMaterialIds.contains(entity.getId());
                } else if (EntityKind.EXPERIMENT.equals(entity.getEntityKind()))
                {
                    valid = validExperimentIds.contains(entity.getId());
                } else if (EntityKind.SAMPLE.equals(entity.getEntityKind()))
                {
                    valid = validSampleIds.contains(entity.getId());
                } else if (EntityKind.DATA_SET.equals(entity.getEntityKind()))
                {
                    valid = validDataSetIds.contains(entity.getId());
                }

                if (valid)
                {
                    validEntities.add(entity);
                }
            }
        }

        return validEntities;
    }

    private boolean isInstanceUser(PersonPE person)
    {
        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }
        }
        return false;
    }

    private Set<String> getSpacesWithValidProjects(PersonPE person)
    {
        Set<String> spaces = new HashSet<String>();

        for (RoleAssignmentPE role : person.getAllPersonRoles())
        {
            if (role.getProject() != null)
            {
                spaces.add(role.getProject().getSpace().getCode());
            }
        }

        return spaces;
    }

    private boolean isValidProject(PersonPE person, ProjectIdentifier project, Map<ProjectIdentifier, Boolean> cache)
    {
        Boolean valid = cache.get(project);

        if (valid != null)
        {
            return valid;
        } else
        {
            if (project == null)
            {
                valid = false;
            } else
            {
                IProjectAuthorization<ProjectIdentifier> pa = new ProjectAuthorizationBuilder<ProjectIdentifier>()
                        .withData(authorizationDataProvider)
                        .withUser(new UserProviderFromPersonPE(person))
                        .withRoles(new RolesProviderFromPersonPE(person))
                        .withObjects(new ProjectProviderFromProjectIdentifierObject(project))
                        .build();
                valid = pa.getObjectsWithoutAccess().isEmpty();
            }

            cache.put(project, valid);

            return valid;
        }
    }

}
