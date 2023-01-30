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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.experiment;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ListExperimentByPermId extends AbstractListObjectById<ExperimentPermId, ExperimentPE>
{

    private IExperimentDAO experimentDAO;

    public ListExperimentByPermId(IExperimentDAO experimentDAO)
    {
        this.experimentDAO = experimentDAO;
    }

    @Override
    public Class<ExperimentPermId> getIdClass()
    {
        return ExperimentPermId.class;
    }

    @Override
    public ExperimentPermId createId(ExperimentPE experiment)
    {
        return new ExperimentPermId(experiment.getPermId());
    }

    @Override
    public List<ExperimentPE> listByIds(IOperationContext context, List<ExperimentPermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (ExperimentPermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return experimentDAO.listByPermID(permIds);
    }

}
