/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSession;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload.FastDownloadSessionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id.DataSetFilePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloader;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.DSPropertyUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.V3Facade;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IngestionService;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.util.IRowBuilder;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

class DataSetRegistrationIngestionService extends IngestionService<DataSetInformation>
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataSetRegistrationIngestionService.class);

    private final DataSetCreation dataSet;

    private final String harvesterTempDir;

    private SyncConfig config;

    public DataSetRegistrationIngestionService(SyncConfig config, File storeRoot, DataSetCreation dataSetCreation,
            Logger operationLog)
    {
        super(createIngestionServiceProperties(), storeRoot);
        this.config = config;
        this.dataSet = dataSetCreation;
        this.harvesterTempDir = config.getHarvesterTempDir();
    }

    private static Properties createIngestionServiceProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(DefaultStorageProcessor.DO_NOT_CREATE_ORIGINAL_DIR_KEY, "true");
        return properties;
    }

    @Override
    protected TableModel process(IDataSetRegistrationTransactionV2 transaction, Map<String, Object> parameters, DataSetProcessingContext context)
    {
        ISampleImmutable sample = getSampleForUpdate(transaction);
        IExperimentImmutable experiment = getExperimentForUpdate(transaction);
        List<NewProperty> dataSetProperties = getProperties(dataSet);

        String dataSetCode = dataSet.getCode();
        if (transaction.getSearchService().getDataSet(dataSetCode) == null)
        {
            // REGISTER NEW DATA SET after downloading the data set files
            transaction.setUserId(config.getHarvesterUser());
            File storeRoot = transaction.getGlobalState().getStoreRootDir();
            File temp = new File(storeRoot, this.harvesterTempDir);
            temp.mkdirs();
            File dir = new File(temp, dataSetCode);
            dir.mkdirs();
            try
            {
                downloadDataSetFiles(temp, dataSetCode);
            } catch (Exception e)
            {
                return errorTableModel(parameters, e);
            }
            if (config.isDryRun() == false)
            {
                String dataSetType = ((EntityTypePermId) dataSet.getTypeId()).getPermId();
                IDataSet ds = transaction.createNewDataSet(dataSetType, dataSetCode);
                ds.setDataSetKind(DataSetKind.valueOf(dataSet.getDataSetKind().toString()));
                ds.setSample(sample);
                ds.setExperiment(experiment);
                for (NewProperty newProperty : dataSetProperties)
                {
                    ds.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
                }
                
                for (File f : dir.listFiles())
                {
                    transaction.moveFile(f.getAbsolutePath(), ds);
                }
            }
            return summaryTableModel(parameters, "Added");
        } 
        if (config.isDryRun() == false)
        {
            // UPDATE data set meta data excluding the container/contained relationships
            IDataSetUpdatable dataSetForUpdate = transaction.getDataSetForUpdate(dataSetCode);
            dataSetForUpdate.setSample(sample);
            dataSetForUpdate.setExperiment(experiment);
            List<? extends IDataSetId> parentIds = dataSet.getParentIds();
            if (parentIds != null)
            {
                dataSetForUpdate.setParentDatasets(parentIds.stream().map(Object::toString).collect(Collectors.toList()));
            }

            // synchronize property changes including properties that were set to empty values
            List<String> existingPropertyCodes = dataSetForUpdate.getAllPropertyCodes();
            Set<String> newPropertyCodes = DSPropertyUtils.extractPropertyNames(dataSetProperties);
            for (NewProperty newProperty : dataSetProperties)
            {
                dataSetForUpdate.setPropertyValue(newProperty.getPropertyCode(), newProperty.getValue());
            }
            // set the properties that are in the harvester but not in the data source anymore, to ""
            existingPropertyCodes.removeAll(newPropertyCodes);
            for (String propCode : existingPropertyCodes)
            {
                dataSetForUpdate.setPropertyValue(propCode, "");
            }
        }
        return summaryTableModel(parameters, "Updated");
    }

    private IExperimentImmutable getExperimentForUpdate(IDataSetRegistrationTransactionV2 transaction)
    {
        if (dataSet.getExperimentId() != null && config.isDryRun() == false)
        {
            return transaction.getExperimentForUpdate(dataSet.getExperimentId().toString());
        }
        return null;
    }

    private ISampleImmutable getSampleForUpdate(IDataSetRegistrationTransactionV2 transaction)
    {
        if (dataSet.getSampleId() != null && config.isDryRun() == false)
        {
            return transaction.getSampleForUpdate(dataSet.getSampleId().toString());
        }
        return null;
    }

    private List<NewProperty> getProperties(DataSetCreation metadata)
    {
        return metadata.getProperties().entrySet().stream()
                .map(e -> new NewProperty(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    protected TableModel errorTableModel(Map<String, Object> parameters, Throwable e)
    {
        operationLog.error("Error occurred while registering data set " + dataSet.getCode(), e);
        return super.errorTableModel(parameters, e);
    }

    private TableModel summaryTableModel(Map<String, Object> parameters, String summary)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("Parameters");
        builder.addHeader(summary);
        IRowBuilder row = builder.addRow();
        row.setCell("Parameters", parameters.toString());
        return builder.getTableModel();
    }

    private void downloadDataSetFiles(File dir, String dataSetCode)
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        V3Facade v3Facade = new V3Facade(config);
        FastDownloadSessionOptions options = new FastDownloadSessionOptions();
        options.withWishedNumberOfStreams(config.getWishedNumberOfStreams());
        DataSetFilePermId filePermId = new DataSetFilePermId(new DataSetPermId(dataSetCode));
        FastDownloadSession downloadSession = v3Facade.createFastDownloadSession(Arrays.asList(filePermId), options);
        new FastDownloader(downloadSession).downloadTo(dir);
        operationLog.info("Download time for data set " + dataSetCode + " to " + dir + ": " + stopWatch);
    }
}