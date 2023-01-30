/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * <i>Data Access Object</i> for {@link ExperimentPE}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IExperimentDAO extends IGenericDAO<ExperimentPE>
{
    /**
     * Lists experiments of the specified project. Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(final List<ProjectPE> projects,
            boolean onlyHavingSamples, boolean onlyHavingDataSets) throws DataAccessException;

    /**
     * Lists experiments of the specified space. Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(final SpacePE space)
            throws DataAccessException;

    /**
     * Lists experiments with specified ids. Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(final Collection<Long> experimentIDs)
            throws DataAccessException;

    /**
     * Lists experiments of specified type, project and space. All criteria are optional. If no criteria is specified all experiments are returned.
     * Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final ProjectPE projectOrNull,
            final SpacePE spaceOrNull) throws DataAccessException;

    /**
     * Lists experiments of specified type, project and space. All criteria are optional. If no criteria is specified all experiments are returned.
     * Fetches also properties.
     */
    public List<ExperimentPE> listExperimentsWithProperties(
            final ExperimentTypePE experimentTypeOrNull, final List<ProjectPE> projectsOrNull,
            final SpacePE spaceOrNull, boolean onlyHavingSamples, boolean onlyHavingDataSets)
            throws DataAccessException;

    /**
     * Lists all registered experiments. Doesn't fetch properties.
     */
    public List<ExperimentPE> listExperiments() throws DataAccessException;

    /**
     * Returns {@link ExperimentPE} defined by given project and experiment code.
     */
    public ExperimentPE tryFindByCodeAndProject(ProjectPE project, String experimentCode);

    public List<ExperimentPE> listByProjectAndCodes(ProjectPE project, Collection<String> experimentCodes);

    /**
     * Lists experiments (with minimal additional information) belonging to the given <code>project</code> and having a property with the specified
     * value.
     */
    public List<ExperimentPE> listExperimentsByProjectAndProperty(final String propertyCode,
            final String propertyValue, final ProjectPE project) throws DataAccessException;

    /**
     * Inserts given {@link ExperimentPE} into the database or updates it if it already exists.
     */
    public void createOrUpdateExperiment(ExperimentPE experiment, PersonPE modifier)
            throws DataAccessException;

    /**
     * Return the codes of all samples assigned to <var>experiment</var>.
     */
    public List<String> getSampleCodes(ExperimentPE experiment);

    /**
     * Try to obtain the experiment for the given <var>permId</var>. Returns <code>null</code>, if no experiment with the given perm id exists.
     */
    public ExperimentPE tryGetByPermID(String permId);

    /**
     * List experiments by perm ids.
     */
    public List<ExperimentPE> listByPermID(Collection<String> permId);

    /**
     * List experiments by ids.
     */
    public List<ExperimentPE> listByIDs(Collection<Long> ids);

    /**
     * Returns a list of distinct spaces owning the experiments specified by their technical ids.
     */
    public List<SpacePE> listSpacesByExperimentIds(Collection<Long> ids);

    /**
     * Saves or updates given given experiments in the database.
     */
    public void createOrUpdateExperiments(List<ExperimentPE> experiments, PersonPE modifier, boolean clearCache);

    /**
     * Delete experiments with given ids by specified registrator with specified reason.
     */
    public void delete(List<TechId> experimentIds, PersonPE tryGetPerson, String reason);

}
