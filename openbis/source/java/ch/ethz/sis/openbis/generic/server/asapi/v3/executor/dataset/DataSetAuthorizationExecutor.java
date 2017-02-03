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

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
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

    @Override
    @Capability("CREATE_DATASET")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void canCreate(IOperationContext context)
    {
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
    }

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
    public void canCreate(IOperationContext context, DataPE dataSet)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(dataSet.getRegistrator(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(new DataSetPermId(dataSet.getPermId()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
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

        if (isStorageConfirmed
                && false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_DATASET")
    public void canDelete(IOperationContext context, IDataSetId id, @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataSet)
    {
        canUpdate(context, id, dataSet);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_DATASET")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_DATASET")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("ARCHIVE_DATASET")
    public void canArchive(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UNARCHIVE_DATASET")
    public void canUnarchive(IOperationContext context)
    {
    }

}
