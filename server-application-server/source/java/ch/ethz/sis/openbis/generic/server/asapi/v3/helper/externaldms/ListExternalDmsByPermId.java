/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.externaldms;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;

/**
 * @author pkupczyk
 */
public class ListExternalDmsByPermId extends AbstractListObjectById<ExternalDmsPermId, ExternalDataManagementSystemPE>
{

    private IExternalDataManagementSystemDAO externalDmsDAO;

    public ListExternalDmsByPermId(IExternalDataManagementSystemDAO externalDmsDAO)
    {
        this.externalDmsDAO = externalDmsDAO;
    }

    @Override
    public Class<ExternalDmsPermId> getIdClass()
    {
        return ExternalDmsPermId.class;
    }

    @Override
    public ExternalDmsPermId createId(ExternalDataManagementSystemPE externalDms)
    {
        return new ExternalDmsPermId(externalDms.getCode());
    }

    @Override
    public List<ExternalDataManagementSystemPE> listByIds(IOperationContext context, List<ExternalDmsPermId> ids)
    {
        List<ExternalDataManagementSystemPE> externalDmses = new LinkedList<ExternalDataManagementSystemPE>();

        for (ExternalDmsPermId id : ids)
        {
            ExternalDataManagementSystemPE externalDms = externalDmsDAO.tryToFindExternalDataManagementSystemByCode(id.getPermId());
            if (externalDms != null)
            {
                externalDmses.add(externalDms);
            }
        }

        return externalDmses;
    }

}
