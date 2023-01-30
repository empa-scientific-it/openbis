/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class DataSetAuthorizationExecutor implements IDataSetAuthorizationExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    private boolean canCreate(PersonPE person)
    {
        if (person.isSystemUser())
        {
            return true;
        }

        AuthorizationServiceUtils authorization = new AuthorizationServiceUtils(null, person);
        return authorization.doesUserHaveRole(RoleWithHierarchy.SPACE_ETL_SERVER);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_DATASET")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void canCreate(IOperationContext context, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
        if (dataSet instanceof ExternalDataPE == false)
        {
            return;
        }

        boolean isCreatorPersonAllowed = false;
        boolean isPersonAllowed = false;

        if (context.getSession().tryGetCreatorPerson() != null)
        {
            isCreatorPersonAllowed = canCreate(context.getSession().tryGetCreatorPerson());
        }

        if (context.getSession().tryGetPerson() != null)
        {
            isPersonAllowed = canCreate(context.getSession().tryGetPerson());
        }

        if (false == isCreatorPersonAllowed && false == isPersonAllowed)
        {
            throw new UserFailureException(
                    "Data set creation can be only executed by a system user or a user with at least " + RoleWithHierarchy.SPACE_ETL_SERVER
                            + " role.");
        }

        DataSetPEByExperimentOrSampleIdentifierValidator validator = new DataSetPEByExperimentOrSampleIdentifierValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (false == validator.doValidation(dataSet.getRegistrator(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(new DataSetPermId(dataSet.getPermId()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_DATASET")
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void canUpdate(IOperationContext context, IDataSetId id, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
        boolean isStorageConfirmed;
        if (dataSet instanceof ExternalDataPE)
        {
            isStorageConfirmed = ((ExternalDataPE) dataSet).isStorageConfirmation();
        } else
        {
            isStorageConfirmed = true;
        }

        DataSetPEByExperimentOrSampleIdentifierValidator validator = new DataSetPEByExperimentOrSampleIdentifierValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        if (isStorageConfirmed
                && false == validator.doValidation(context.getSession().tryGetPerson(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.PROJECT_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_DATASET")
    public void canDelete(IOperationContext context, IDataSetId id, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
        canUpdate(context, id, dataSet);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_DATASET")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_DATASET")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("FREEZE_DATASET")
    public void canFreeze(IOperationContext context, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("ARCHIVE_DATASET")
    public void canArchive(IOperationContext context, IDataSetId id, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UNARCHIVE_DATASET")
    public void canUnarchive(IOperationContext context, IDataSetId id, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("LOCK_DATASET")
    public void canLock(IOperationContext context, IDataSetId dataSetId, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("UNLOCK_DATASET")
    public void canUnlock(IOperationContext context, IDataSetId dataSetId, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
    }

}
