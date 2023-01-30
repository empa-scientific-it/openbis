/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.AbstractListObjectById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author pkupczyk
 */
public class ListExperimentByIdentifier extends AbstractListObjectById<ExperimentIdentifier, ExperimentPE>
{

    private IProjectDAO projectDAO;

    private IExperimentDAO experimentDAO;

    public ListExperimentByIdentifier(IProjectDAO projectDAO, IExperimentDAO experimentDAO)
    {
        this.projectDAO = projectDAO;
        this.experimentDAO = experimentDAO;
    }

    @Override
    public Class<ExperimentIdentifier> getIdClass()
    {
        return ExperimentIdentifier.class;
    }

    @Override
    public ExperimentIdentifier createId(ExperimentPE experiment)
    {
        return new ExperimentIdentifier(experiment.getIdentifier());
    }

    @Override
    public List<ExperimentPE> listByIds(IOperationContext context, List<ExperimentIdentifier> experimentIdentifiers)
    {
        List<ExperimentPE> experiments = new LinkedList<ExperimentPE>();

        Map<ProjectPE, List<String>> projectToExperimentCodesMap = getProjectToExperimentCodesMap(experimentIdentifiers);

        for (Map.Entry<ProjectPE, List<String>> entry : projectToExperimentCodesMap.entrySet())
        {
            List<ExperimentPE> projectExperiments = experimentDAO.listByProjectAndCodes(entry.getKey(), entry.getValue());
            experiments.addAll(projectExperiments);
        }

        return experiments;
    }

    private Map<ProjectPE, List<String>> getProjectToExperimentCodesMap(List<ExperimentIdentifier> experimentIdentifiers)
    {
        Map<ProjectIdentifier, ProjectPE> projectIdentifierToProjectMap = new HashMap<ProjectIdentifier, ProjectPE>();
        Map<ProjectPE, List<String>> projectToExperimentCodesMap = new HashMap<ProjectPE, List<String>>();

        for (ExperimentIdentifier experimentIdentifier : experimentIdentifiers)
        {
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier coreExperimentIdentifier =
                    ExperimentIdentifierFactory.parse(experimentIdentifier.getIdentifier());

            ProjectIdentifier coreProjectIdentifier = new ProjectIdentifier(coreExperimentIdentifier.getSpaceCode(),
                    coreExperimentIdentifier.getProjectCode());

            ProjectPE project = null;

            if (projectIdentifierToProjectMap.containsKey(coreProjectIdentifier))
            {
                project = projectIdentifierToProjectMap.get(coreProjectIdentifier);
            } else
            {
                project = projectDAO.tryFindProject(coreProjectIdentifier.getSpaceCode(), coreProjectIdentifier.getProjectCode());
                projectIdentifierToProjectMap.put(coreProjectIdentifier, project);
            }

            if (project == null)
            {
                continue;
            }

            List<String> experimentCodes = projectToExperimentCodesMap.get(project);

            if (experimentCodes == null)
            {
                experimentCodes = new LinkedList<String>();
                projectToExperimentCodesMap.put(project, experimentCodes);
            }

            experimentCodes.add(coreExperimentIdentifier.getExperimentCode());
        }

        return projectToExperimentCodesMap;
    }
}
