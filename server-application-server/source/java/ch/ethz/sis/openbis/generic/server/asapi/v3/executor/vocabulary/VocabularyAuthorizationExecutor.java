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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.bo.InternalVocabularyAuthorization;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyAuthorizationExecutor implements IVocabularyAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    @Capability("CREATE_VOCABULARY")
    public void canCreate(IOperationContext context, VocabularyPE entity)
    {
        new InternalVocabularyAuthorization().canCreateVocabulary(context.getSession(), entity);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_VOCABULARY")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_VOCABULARY")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY)
    @Capability("UPDATE_VOCABULARY")
    public void canUpdate(IOperationContext context, IVocabularyId id, VocabularyPE entity)
    {
        new InternalVocabularyAuthorization().canUpdateVocabulary(context.getSession(), entity);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    @Capability("DELETE_VOCABULARY")
    public void canDelete(IOperationContext context, IVocabularyId entityId, VocabularyPE entity)
    {
        new InternalVocabularyAuthorization().canDeleteVocabulary(context.getSession(), entity);
    }

}
