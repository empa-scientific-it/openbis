/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;

/**
 * Asynchronous version of {@link IGenericClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientServiceAsync extends IClientServiceAsync
{
    /**
     * @see IGenericClientService#getSampleGenerationInfo(TechId)
     */
    public void getSampleGenerationInfo(final TechId sampleId,
            AsyncCallback<SampleParentWithDerived> asyncCallback);

    /**
     * @see IGenericClientService#getSampleInfo(TechId)
     */
    public void getSampleInfo(final TechId sampleId, AsyncCallback<Sample> asyncCallback);

    /**
     * @see IGenericClientService#registerSample(String, NewSample)
     */
    public void registerSample(final String sessionKey, final NewSample sample,
            final AsyncCallback<Sample> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#registerSamplesWithSilentOverrides(SampleType, String, String, String, boolean, String, String, boolean)
     */
    public void registerSamplesWithSilentOverrides(final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            String sessionKey, boolean async, String userEmail, String defaultGroupIdentifier, boolean updateExisting,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#registerSamples(SampleType, String, boolean, String, String, boolean)
     */
    public void registerSamples(final SampleType sampleType, String sessionKey, boolean async, String userEmail,
            String defaultGroupIdentifier, boolean updateExisting,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#updateSamplesWithSilentOverrides(SampleType, String, String, String, boolean, String, String)
     */
    public void updateSamplesWithSilentOverrides(final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            String sessionKey, boolean async, String userEmail, String defaultGroupIdentifier,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateSamples(SampleType, String, boolean, String, String)
     */
    public void updateSamples(final SampleType sampleType, String sessionKey, boolean async, String userEmail,
            String defaultGroupIdentifier,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#updateExperiments(ExperimentType, String, boolean, String)
     */
    public void updateExperiments(final ExperimentType experimentType, String sessionKey, boolean async, String userEmail,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#getDataSetInfo(TechId)
     */
    public void getDataSetInfo(TechId datasetTechId,
            final AsyncCallback<AbstractExternalData> datasetInfoCallback);

    /**
     * @see IGenericClientService#registerExperiment(String,String,NewExperiment)
     */
    public void registerExperiment(final String attachmentsSessionKey, String samplesSessionKey,
            NewExperiment newExp, AsyncCallback<Experiment> assyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#registerMaterials(MaterialType, boolean, String, boolean, String)
     */
    public void registerMaterials(MaterialType materialType, boolean updateExisting,
            String sessionKey, boolean async, String userEmail,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback);

    /**
     * @see IGenericClientService#updateMaterials(MaterialType, String, boolean, boolean, String)
     */
    public void updateMaterials(MaterialType materialType, String sessionKey,
            boolean ignoreUnregisteredMaterials, boolean async, String userEmail,
            final AsyncCallback<List<BatchRegistrationResult>> asyncCallback);

    /**
     * @see IGenericClientService#updateExperiment(ExperimentUpdates)
     */
    public void updateExperiment(ExperimentUpdates experimentUpdates,
            final AsyncCallback<ExperimentUpdateResult> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateMaterial(TechId, List, String[], Date)
     */
    public void updateMaterial(final TechId materialId, List<IEntityProperty> properties,
            String[] metaprojects, Date version, final AsyncCallback<Date> asyncCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#updateSample(SampleUpdates)
     */
    public void updateSample(SampleUpdates updates,
            final AsyncCallback<SampleUpdateResult> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateDataSet(DataSetUpdates)
     */
    public void updateDataSet(DataSetUpdates updates,
            final AsyncCallback<DataSetUpdateResult> asyncCallback) throws UserFailureException;

    /**
     * @see IGenericClientService#updateDataSets(DataSetType,String, boolean, String)
     */
    public void updateDataSets(DataSetType dataSetType, String sessionKey, boolean async, String userEmail,
            AsyncCallback<List<BatchRegistrationResult>> updateDataSetsCallback);

    /**
     * @see IGenericClientService#registerExperiments(ExperimentType, String, boolean, String)
     */
    public void registerExperiments(ExperimentType experimentType, String sessionKey, boolean async, String userEmail,
            AsyncCallback<List<BatchRegistrationResult>> registerExperimentsCallback);

    /**
     * @see IGenericClientService#registerOrUpdateSamplesAndMaterials(String, String, boolean, boolean, String)
     */
    public void registerOrUpdateSamplesAndMaterials(final String sessionKey,
            final String defaultGroupIdentifier, boolean updateExisting, boolean async,
            String userEmail,
            AsyncCallback<List<BatchRegistrationResult>> registerSamplesAndMaterialsCallback)
            throws UserFailureException;

    /**
     * @see IGenericClientService#uploadedSamplesInfo(SampleType, String)
     */
    public void uploadedSamplesInfo(
            final SampleType sampleType,
            final String sessionKey,
            final AsyncCallback<Map<String, Object>> registerSamplesAndMaterialsCallback);
}
