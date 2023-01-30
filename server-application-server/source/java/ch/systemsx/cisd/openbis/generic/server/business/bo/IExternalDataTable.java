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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * A <i>Business Object</i> to load a list of {@link ExternalDataPE}.
 * 
 * @author Christian Ribeaud
 */
@Deprecated
public interface IExternalDataTable
{
    /**
     * Loads data sets specified by their codes. Data set codes will be ignored if no {@link ExternalDataPE} could be found. Properties will be loaded
     * too depending on <var>withProperties</var> value. Optionally if <var>lockForUpdate</var> is <var>true</var> all updates to loaded data sets
     * from other transactions will be blocked until current transaction is finished.
     */
    void loadByDataSetCodes(List<String> dataSetCodes, boolean withProperties, boolean lockForUpdate);

    /**
     * Loads data sets which are linked to the sample with given <var>sampleId</var>.
     */
    void loadBySampleTechId(final TechId sampleId);

    /**
     * Loads data sets which are linked to the experiment with given <var>experimentId</var>.
     */
    void loadByExperimentTechId(final TechId experimentId);

    /**
     * Returns the loaded {@link ExternalDataPE}.
     */
    List<ExternalDataPE> getExternalData();

    /**
     * Sets the specified external data as they were loaded.
     */
    void setExternalData(List<ExternalDataPE> externalData);

    /**
     * Deletes loaded data sets for specified reason.
     */
    void deleteLoadedDataSets(String reason);

    /**
     * Uploads loaded data sets to CIFEX server as specified in the upload context.
     * 
     * @return a message or an empty string.
     */
    String uploadLoadedDataSetsToCIFEX(DataSetUploadContext uploadContext);

    /**
     * Schedules archiving of loaded data sets. Only available data sets that are not locked will be archived.
     * 
     * @param removeFromDataStore when set to <code>true</code> the data sets will be removed from the data store after a successful archiving
     *            operation.
     * @return number of data sets scheduled for archiving.
     */
    int archiveDatasets(boolean removeFromDataStore);

    /**
     * Schedules unarchiving of loaded data sets. Only archived datasets will be unarchived.
     * 
     * @return number of data sets scheduled for unarchiving.
     */
    int unarchiveDatasets();

    /**
     * Locks loaded data sets.
     * 
     * @return number of data sets scheduled for locking.
     */
    int lockDatasets();

    /**
     * Unlocks loaded data sets.
     * 
     * @return number of data sets scheduled for unlocking.
     */
    int unlockDatasets();

    /** Creates a report from specified datasets using the specified datastore service. */
    TableModel createReportFromDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes);

    /**
     * Schedules processing of specified datasets with specified parameter bindings using the specified datastore service.
     * 
     * @param parameterBindings Should be a map where additional entries can be added.
     */
    void processDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes, Map<String, String> parameterBindings);

    /**
     * Loads data sets that belong to chosen data store.
     */
    public void loadByDataStore(DataStorePE dataStore);

    void update(List<NewDataSet> updates);

    void save();

    /**
     * Gets the link from a service that supports the IReportingPluginTask#createLink method.
     */
    LinkModel retrieveLinkFromDataSet(String key, String datastoreCode, String dataSetCode);

}
