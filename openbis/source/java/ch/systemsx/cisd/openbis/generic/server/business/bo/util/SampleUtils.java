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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomasz Pylak
 */
public class SampleUtils
{
    public static List<SamplePE> getExperimentSamples(long experimentTechId) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        List<TechId> techIds = sampleDAO.listSampleIdsByExperimentIds(TechId.createList(experimentTechId));
        List<Long> techIdsAsLongs = new ArrayList<>();
        for (TechId id : techIds) {
            Long idId = id.getId();
            techIdsAsLongs.add(idId);
        }
        List<SamplePE> samplePES = sampleDAO.listByIDs(techIdsAsLongs);
        return samplePES;
    }

    /** for all experiment samples which belonged to a space the specified space will be set */
    public static void setSamplesSpace(ExperimentPE experiment, SpacePE space)
    {
        for (SamplePE sample : SampleUtils.getExperimentSamples(experiment.getId()))
        {
            if (sample.getSpace() != null)
            {
                sample.setSpace(space);
            }
        }
    }

    public static UserFailureException createWrongSampleException(DataPE data, SamplePE sample,
            String reason)
    {
        return UserFailureException.fromTemplate(
                "The dataset '%s' cannot be connected to the sample '%s' because %s.",
                data.getCode(), sample == null ? "?" : sample.getIdentifier(), reason);
    }

    public static void assertProjectSamplesEnabled(SamplePE samplePE, ProjectPE project)
    {
        if (SamplePE.projectSamplesEnabled == false)
        {
            throw new UserFailureException("Can not assign sample " + samplePE.getIdentifier()
                    + " to project " + project.getIdentifier() + " because project samples are not enabled.");
        }

    }
}
