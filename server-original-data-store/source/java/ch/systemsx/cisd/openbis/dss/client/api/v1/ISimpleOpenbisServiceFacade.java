/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.io.File;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;

/**
 * Provides a simplified view of an openBIS backend by allowing the following operations :
 * 
 * <pre>
 *  1. basic listing for the common openBIS entities kinds: projects, experiments, samples, and data sets.
 *  2. uploading datasets to openBIS
 *  3. downloading data sets from openBIS
 * </pre>
 * 
 * @author Kaloyan Enimanev
 */
public interface ISimpleOpenbisServiceFacade
{

    /**
     * Returns the minor version of the general information service.
     */
    public int getMinorVersionInformationService();

    /**
     * Returns all available projects.
     */
    @Retry
    List<Project> listProjects();

    /**
     * Return all spaces enriched with their projects and role assignments.
     */
    @Retry
    List<SpaceWithProjectsAndRoleAssignments> getSpacesWithProjects();

    /**
     * Return {@link Experiment} objects for a set of given experiment identifiers. If some of the specified experiment identifiers does not exist in
     * openBIS it will be silently ignored.
     */
    @Retry
    List<Experiment> getExperiments(List<String> experimentIdentifiers);

    /**
     * Return all experiments for a given list of project identifiers. If some of the specified project identifiers does not exist in openBIS it will
     * be silently ignored.
     */
    @Retry
    List<Experiment> listExperimentsForProjects(List<String> projectIdentifiers);

    /**
     * Return all experiments having samples for a given list of project identifiers. If some of the specified project identifiers does not exist in
     * openBIS it will be silently ignored.
     */
    @Retry
    List<Experiment> listExperimentsHavingSamplesForProjects(List<String> projectIdentifiers);

    /**
     * Return all experiments having data sets for a given list of project identifiers. If some of the specified project identifiers does not exist in
     * openBIS it will be silently ignored.
     */
    @Retry
    List<Experiment> listExperimentsHavingDataSetsForProjects(List<String> projectIdentifiers);

    /**
     * Return {@link Sample} objects for a set of given sample identifiers. If some of the specified sample identifiers does not exist in openBIS it
     * will be silently ignored.
     */
    @Retry
    List<Sample> getSamples(List<String> sampleIdentifiers);

    /**
     * Return {@link Sample} objects for a set of given sample identifiers. If some of the specified sample identifiers does not exist in openBIS it
     * will be silently ignored.
     * 
     * @param sampleIdentifiers sample identifiers for which samples should be retrieved
     * @param fetchOptions Options that control which parts of the samples are fetched.
     */
    @Retry
    List<Sample> getSamples(List<String> sampleIdentifiers,
            EnumSet<SampleFetchOption> fetchOptions);

    /**
     * Return all samples for a given list of experiments identifiers. If some of the specified experiment identifiers does not exist in openBIS it
     * will be silently ignored.
     */
    @Retry
    List<Sample> listSamplesForExperiments(List<String> experimentIdentifiers);

    /**
     * Return all samples for a given list of experiments identifiers. If some of the specified experiment identifiers does not exist in openBIS it
     * will be silently ignored.
     * 
     * @param experimentIdentifiers experiment identifiers for which samples should be retrieved
     * @param fetchOptions Options that control which parts of the samples are fetched.
     */
    @Retry
    List<Sample> listSamplesForExperiments(List<String> experimentIdentifiers,
            EnumSet<SampleFetchOption> fetchOptions);

    /**
     * Returns all samples for specified experiment and of specified type.
     * 
     * @return an empty list if specified identifier and/or type do not exist.
     */
    @Retry
    List<Sample> listSamplesForExperimentAndSampleType(String experimentPermId, String sampleType);

    /**
     * Returns all samples which are components or children of the specified sample.
     * 
     * @return an empty list if the specified sample does not exist.
     */
    @Retry
    List<Sample> listSamplesOfSample(String samplePermId);

    /**
     * Return all samples for a given list of project identifiers. If some of the specified project identifiers does not exist in openBIS it will be
     * silently ignored.
     */
    @Retry
    public List<Sample> listSamplesForProjects(List<String> projectIdentifiers);

    /**
     * Return all samples for a given list of project identifiers. If some of the specified project identifiers does not exist in openBIS it will be
     * silently ignored.
     * 
     * @param projectIdentifiers project identifiers for which samples should be retrieved
     * @param fetchOptions Options that control which parts of the samples are fetched.
     */
    @Retry
    public List<Sample> listSamplesForProjects(List<String> projectIdentifiers,
            EnumSet<SampleFetchOption> fetchOptions);

    /**
     * Return a {@link DataSet} object for for the given code. If some of the specified data set code does not exist in openBIS, null will be
     * returned.
     * 
     * @return The requested data set, or null if it does not exist.
     */
    @Retry
    DataSet getDataSet(String dataSetCode);

    /**
     * Return {@link DataSet} objects for given a set of codes. If some of the specified data set codes does not exist in openBIS it will be silently
     * ignored.
     */
    @Retry
    List<DataSet> getDataSets(List<String> dataSetCodes);

    /**
     * Return all data sets for a given list of experiments identifiers. If some of the specified experiment identifiers does not exist in openBIS it
     * will be silently ignored.
     */
    @Retry
    List<DataSet> listDataSetsForExperiments(List<String> experimentIdentifiers);

    /**
     * Returns all data sets of specified experiment.
     * 
     * @return an empty list if the experiment does not exists.
     */
    @Retry
    List<DataSet> listDataSetsForExperiment(String experimentPermId);

    /**
     * Return all data sets for a given list of sample identifiers. If some of the specified sample identifiers does not exist in openBIS it will be
     * silently ignored.
     */
    @Retry
    List<DataSet> listDataSetsForSamples(List<String> sampleIdentifiers);

    /**
     * Returns all data sets of specified sample.
     * 
     * @return an empty list if the sample does not exists.
     */
    @Retry
    List<DataSet> listDataSetsForSample(String samplePermId);

    /**
     * Returns all data set types available in openBIS.
     */
    @Retry
    List<DataSetType> listDataSetTypes();

    /**
     * Returns all sample types available in openBIS.
     */
    @Retry
    List<SampleType> listSampleTypes();

    /**
     * Returns all experiment types available in openBIS.
     */
    @Retry
    List<ExperimentType> listExperimentTypes();

    /**
     * Return all vocabularies available in openBIS together with the contained vocabulary terms.
     */
    @Retry
    List<Vocabulary> listVocabularies();

    /**
     * Upload a new data set to the DSS.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A proxy to the newly added data set
     */
    public DataSet putDataSet(NewDataSetDTO newDataset, File dataSetFile);

    /**
     * Validates a data set.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return A list of validation errors. The list is empty if there were no validation errors.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Extracts metadata from a file.
     * 
     * @param newDataset The new data set that should be registered
     * @param dataSetFile A file or folder containing the data
     * @return Map of property name/value pairs.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Retry
    public Map<String, Serializable> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException;

    /**
     * Checks whether the session is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    @Retry
    public void checkSession() throws InvalidSessionException;

    /**
     * Logs out from openBIS and frees all associated resources on the server.
     * <p>
     * IMPORTANT NOTE: If clients fail to call this method after finishing interaction with openBIS, then their session will be kept on the server
     * until it expires. It is considered a security risk to leave openBIS sessions open.
     */
    public void logout();
}
