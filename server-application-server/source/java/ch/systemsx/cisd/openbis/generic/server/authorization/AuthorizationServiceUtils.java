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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.MetaprojectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The class containing some authorizing methods
 * 
 * @author Jakub Straszewski
 * @author Pawel Glyzewski
 */
public class AuthorizationServiceUtils
{
    private final IDAOFactory daoFactory;

    private final PersonPE user;

    private final List<RoleWithIdentifier> userRoles;

    public AuthorizationServiceUtils(IDAOFactory daoFactory, String userId)
    {
        this(daoFactory, getUserByName(daoFactory, userId));
    }

    public AuthorizationServiceUtils(IDAOFactory daoFactory, PersonPE user)
    {
        this.daoFactory = daoFactory;

        this.user = user;
        this.userRoles = DefaultAccessController.getUserRoles(user);
    }

    public boolean doesUserHaveRole(String roleCode, String spaceOrNull)
    {
        RoleWithHierarchy methodRole;
        try
        {
            // get the role
            RoleLevel roleLevel = (spaceOrNull == null) ? RoleLevel.INSTANCE : RoleLevel.SPACE;
            methodRole =
                    RoleWithHierarchy.valueOf(roleLevel,
                            RoleWithHierarchy.RoleCode.valueOf(roleCode));
        } catch (Exception e)
        {
            // the only possible reason for this exception is incorrect role

            // ETL_SERVICE role is intentionally omitted in the error messages
            if (spaceOrNull == null)
            {
                throw new IllegalArgumentException(
                        "Incorrect role "
                                + roleCode
                                + " specified. The correct roles for space are ADMIN, USER, POWER_USER, OBSERVER");
            } else
            {
                throw new IllegalArgumentException("Incorrect role " + roleCode
                        + " specified. The correct instance roles are ADMIN, OBSERVER");
            }
        }

        if (user.getAllPersonRoles().size() == 0)
        {
            return false;
        }

        // getRoles() takes all the roles stronger/equal than the role
        List<RoleWithIdentifier> retainedUserRoles =
                DefaultAccessController.retainMatchingRoleWithIdentifiers(
                        new ArrayList<RoleWithIdentifier>(userRoles), methodRole.getRoles());

        if (retainedUserRoles.size() == 0)
        {
            return false;
        }

        if (spaceOrNull != null)
        {
            SpaceIdentifierPredicate predicate = new SpaceIdentifierPredicate();

            predicate.init(new AuthorizationDataProvider(daoFactory));

            final Status status =
                    predicate.evaluate(user, retainedUserRoles, new SpaceIdentifier(spaceOrNull));

            return (status.getFlag().equals(StatusFlag.OK));
        }
        return true;

    }

    public boolean doesUserHaveRole(RoleWithHierarchy role)
    {
        if (user.getAllPersonRoles().size() == 0)
        {
            return false;
        }

        // getRoles() takes all the roles stronger/equal than the role
        List<RoleWithIdentifier> retainedUserRoles =
                DefaultAccessController.retainMatchingRoleWithIdentifiers(
                        new ArrayList<RoleWithIdentifier>(userRoles), role.getRoles());

        if (retainedUserRoles.size() == 0)
        {
            return false;
        }

        return true;
    }

    private static PersonPE getUserByName(IDAOFactory daoFactory, String userId)
    {
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);
        if (person == null)
        {
            throw new IllegalArgumentException("The user with id " + userId + " doesn't exist");
        }
        return person;
    }

    public List<String> filterDataSetCodes(List<String> dataSetCodes)
    {
        LinkedList<String> resultList = new LinkedList<String>();
        for (String dataSetCode : dataSetCodes)
        {
            if (canAccessDataSet(dataSetCode))
            {
                resultList.add(dataSetCode);
            }
        }
        return resultList;
    }

    public boolean canAccessDataSet(DataPE dataSet)
    {
        return canAccessDataSet(dataSet.getCode());
    }

    public boolean canAccessDataSet(AbstractExternalData dataSet)
    {
        return canAccessDataSet(dataSet.getCode());
    }

    private boolean canAccessDataSet(String dataSetCode)
    {
        DataSetCodePredicate predicate = new DataSetCodePredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status = predicate.evaluate(user, userRoles, dataSetCode);

        return (status.getFlag().equals(StatusFlag.OK));
    }

    public List<String> filterExperimentIds(List<String> experimentIds)
    {
        LinkedList<String> resultList = new LinkedList<String>();
        for (String experimentId : experimentIds)
        {
            if (canAccessExperiment(experimentId))
            {
                resultList.add(experimentId);
            }
        }
        return resultList;
    }

    public boolean canAccessMetaproject(MetaprojectPE metaprojectPE)
    {
        MetaprojectTechIdPredicate predicate = new MetaprojectTechIdPredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status =
                predicate.evaluate(user, userRoles, new TechId(metaprojectPE.getId()));

        return (status.getFlag().equals(StatusFlag.OK));
    }

    public void checkAccessMetaproject(MetaprojectPE metaprojectPE)
    {
        if (canAccessMetaproject(metaprojectPE) == false)
        {
            throw new AuthorizationFailureException("User: "
                    + (user != null ? user.getUserId() : null)
                    + " doesn't have access to metaproject: " + metaprojectPE.getIdentifier());
        }
    }

    private void checkEntityNotNull(Object entity, EntityKind entityKind, TechId entityId)
    {
        if (entity == null)
        {
            throw UserFailureException.fromTemplate("There is no %s with id '%s'.", entityKind.getDescription(), entityId);
        }
    }

    public void checkAccessEntity(EntityKind entityKind, TechId entityId)
    {
        boolean canAccess;

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            ExperimentPE experiment = daoFactory.getExperimentDAO().getByTechId(entityId);
            checkEntityNotNull(experiment, entityKind, entityId);
            canAccess = canAccessExperiment(experiment);
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            SamplePE sample = daoFactory.getSampleDAO().getByTechId(entityId);
            checkEntityNotNull(sample, entityKind, entityId);
            canAccess = canAccessSample(sample);
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            DataPE dataSet = daoFactory.getDataDAO().getByTechId(entityId);
            checkEntityNotNull(dataSet, entityKind, entityId);
            canAccess = canAccessDataSet(dataSet);
        } else if (EntityKind.MATERIAL.equals(entityKind))
        {
            canAccess = true;
        } else
        {
            throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
        }

        if (false == canAccess)
        {
            throw new AuthorizationFailureException("User: "
                    + (user != null ? user.getUserId() : null)
                    + " doesn't have access to entity: " + entityKind + " with id: " + entityId);
        }
    }

    private void checkEntityNotNull(Object entity, EntityKind entityKind, String entityPermId)
    {
        if (entity == null)
        {
            throw UserFailureException.fromTemplate("There is no %s with permId '%s'.", entityKind.getDescription(), entityPermId);
        }
    }

    public void checkAccessEntity(EntityKind entityKind, String entityPermId)
    {
        boolean canAccess;

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            ExperimentPE experiment = daoFactory.getExperimentDAO().tryGetByPermID(entityPermId);
            checkEntityNotNull(experiment, entityKind, entityPermId);
            canAccess = canAccessExperiment(experiment);
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(entityPermId);
            checkEntityNotNull(sample, entityKind, entityPermId);
            canAccess = canAccessSample(sample);
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(entityPermId);
            checkEntityNotNull(dataSet, entityKind, entityPermId);
            canAccess = canAccessDataSet(dataSet);
        } else if (EntityKind.MATERIAL.equals(entityKind))
        {
            canAccess = true;
        } else
        {
            throw new IllegalArgumentException("Unknown entity kind: " + entityKind);
        }

        if (false == canAccess)
        {
            throw new AuthorizationFailureException("User: "
                    + (user != null ? user.getUserId() : null)
                    + " doesn't have access to entity: " + entityKind + " with permId: " + entityPermId);
        }
    }

    public boolean canAccessExperiment(Experiment experiment)
    {
        return canAccessExperiment(experiment.getIdentifier());
    }

    public boolean canAccessExperiment(ExperimentPE experimentPE)
    {
        return canAccessExperiment(experimentPE.getIdentifier());
    }

    private boolean canAccessExperiment(String experimentId)
    {
        ExperimentAugmentedCodePredicate predicate = new ExperimentAugmentedCodePredicate();

        predicate.init(new AuthorizationDataProvider(daoFactory));

        final Status status = predicate.evaluate(user, userRoles, experimentId);

        return (status.getFlag().equals(StatusFlag.OK));
    }

    public List<String> filterSampleIds(List<String> sampleIds)
    {
        final LinkedList<String> resultList = new LinkedList<String>();
        for (String sampleIdentifier : sampleIds)
        {
            if (canAccessSample(sampleIdentifier))
            {
                resultList.add(sampleIdentifier);
            }
        }
        return resultList;
    }

    public boolean canAccessSample(SamplePE sample)
    {
        return canAccessSample(sample.getIdentifier());
    }

    public boolean canAccessSample(Sample sample)
    {
        return canAccessSample(sample.getIdentifier());
    }

    private boolean canAccessSample(String sampleIdentifier)
    {
        final IPredicate<String> predicate = createSampleIdentifierPredicate(daoFactory);
        return canAccessSample(predicate, sampleIdentifier);
    }

    private boolean canAccessSample(final IPredicate<String> predicate, final String sampleIdentifier)
    {
        final Status status = predicate.evaluate(user, userRoles, sampleIdentifier);
        return Status.OK.equals(status);
    }

    private static IPredicate<String> createSampleIdentifierPredicate(IDAOFactory daoFactory)
    {
        IPredicate<String> predicate = new SampleAugmentedCodePredicate();
        predicate.init(new AuthorizationDataProvider(daoFactory));
        return predicate;
    }

}
