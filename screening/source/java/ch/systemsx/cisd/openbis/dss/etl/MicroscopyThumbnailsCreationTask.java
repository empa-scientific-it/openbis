/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.jython.evaluator.IJythonEvaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ICredentials;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.plugins.AbstractMaintenanceTaskWithStateFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetUpdatable;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageContainerDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonBasedProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningJythonIngestionService;
import ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython.ScreeningPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * @author Franz-Josef Elmer
 */
public class MicroscopyThumbnailsCreationTask extends AbstractMaintenanceTaskWithStateFile
{

    private static final String DATA_SET_CONTAINER_TYPE_KEY = "data-set-container-type";

    private static final String DATA_SET_CONTAINER_TYPE_DEFAULT = "MICROSCOPY_IMG_CONTAINER";

    private static final String DATA_SET_THUMBNAIL_TYPE_REGEX_KEY = "data-set-thumbnail-type-regex";

    private static final String DATA_SET_THUMBNAIL_TYPE_REGEX_DEFAULT = "MICROSCOPY_IMG_THUMBNAIL";

    private static final String MAIN_DATA_SET_TYPE_REGEX_KEY = "main-data-set-type-regex";

    private static final String MAIN_DATA_SET_TYPE_REGEX_DEFAULT = "MICROSCOPY_IMG";

    private static final String MAX_NUMBER_OF_DATA_SETS_KEY = "max-number-of-data-sets";

    private static final int MAX_NUMBER_OF_DATA_SETS_DEFAULT = 1000;

    private static final String MAX_NUMBER_OF_WORKERS_KEY = "maximum-number-of-workers";

    private static final int MAX_NUMBER_OF_WORKERS_DEFAULT = 1;

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            MicroscopyThumbnailsCreationTask.class);

    private Properties properties;

    private String dataSetContainerType;

    private Pattern dataSetThumbnailTypePattern;

    private Pattern mainDataSetTypePattern;

    private int maxCount;

    private int maxNumberOfWorkers;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        this.properties = properties;
        defineStateFile(properties, getDirectoryProvider().getStoreRoot());
        dataSetContainerType = properties.getProperty(DATA_SET_CONTAINER_TYPE_KEY, DATA_SET_CONTAINER_TYPE_DEFAULT);
        dataSetThumbnailTypePattern = PropertyUtils.getPattern(properties, DATA_SET_THUMBNAIL_TYPE_REGEX_KEY, DATA_SET_THUMBNAIL_TYPE_REGEX_DEFAULT);
        mainDataSetTypePattern = PropertyUtils.getPattern(properties, MAIN_DATA_SET_TYPE_REGEX_KEY, MAIN_DATA_SET_TYPE_REGEX_DEFAULT);
        maxCount = PropertyUtils.getInt(properties, MAX_NUMBER_OF_DATA_SETS_KEY, MAX_NUMBER_OF_DATA_SETS_DEFAULT);
        maxNumberOfWorkers = PropertyUtils.getInt(properties, MAX_NUMBER_OF_WORKERS_KEY, MAX_NUMBER_OF_WORKERS_DEFAULT);
    }

    @Override
    public void execute()
    {
        String sessionToken = login();
        Date lastRegistrationDate = getLastRegistrationDate(new Date(0));
        String lastCode = getLastCode();
        operationLog.info("Search for data sets of type " + dataSetContainerType + " which are younger than "
                + renderTimeStamp(lastRegistrationDate) + (lastCode != null ? " and code after " + lastCode : ""));
        SearchResult<DataSet> searchResult = searchForNewDataSets(sessionToken, lastRegistrationDate, lastCode);
        List<DataSet> containerDataSets = searchResult.getObjects();
        int totalCount = searchResult.getTotalCount();
        operationLog.info(totalCount + " data sets found."
                + (totalCount > containerDataSets.size() ? " Handle the first " + containerDataSets.size() : ""));
        AtomicInteger numberOfCreatedThumbnailDataSets = new AtomicInteger(0);
        Collection<FailureRecord<DataSet>> result = ParallelizedExecutor.process(containerDataSets,
                new ITaskExecutor<DataSet>()
                    {
                        @Override
                        public Status execute(DataSet containerDataSet)
                        {
                            if (hasNoThumbnails(containerDataSet) && containerDataSet.getComponents().isEmpty() == false)
                            {
                                operationLog.info("Generate thumbnails for data set " + containerDataSet.getCode());
                                try
                                {
                                    int numberOfDataSets = createThumbnailDataSet(sessionToken, containerDataSet);
                                    numberOfCreatedThumbnailDataSets.addAndGet(numberOfDataSets);
                                } catch (Throwable t)
                                {
                                    operationLog.error("Generating thumbnails for data set "
                                            + containerDataSet.getCode() + " failed:", t);
                                    return Status.createError(t.toString());
                                }
                            }
                            return Status.OK;
                        }
                    },
                getMachineLoad(), maxNumberOfWorkers, "thumbnail creation", 0, false);
        List<DataSet> failedDataSets = extractFailedDataSets(result);
        updateTimeStampFile(numberOfCreatedThumbnailDataSets.get(), containerDataSets, failedDataSets);
        operationLog.info(numberOfCreatedThumbnailDataSets + " thumbnail data sets have been created.");
    }

    double getMachineLoad()
    {
        return 0.5;
    }

    private SearchResult<DataSet> searchForNewDataSets(String sessionToken, Date lastRegistrationDate, String lastCode)
    {
        DataSetSearchCriteria searchCriteria = new DataSetSearchCriteria();
        searchCriteria.withType().withCode().thatEquals(dataSetContainerType);
        searchCriteria.withRegistrationDate().thatIsLaterThanOrEqualTo(lastRegistrationDate);
        if (lastCode != null)
        {
            searchCriteria.withCode().thatIsGreaterThan(lastCode);
        }
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withComponents().withType();
        fetchOptions.withComponents().withExperiment();
        fetchOptions.withComponents().withSample();
        fetchOptions.sortBy().registrationDate();
        fetchOptions.sortBy().code();
        if (maxCount > 0)
        {
            fetchOptions.from(0);
            fetchOptions.count(maxCount);
        }
        return getService().searchDataSets(sessionToken, searchCriteria, fetchOptions);
    }

    private List<DataSet> extractFailedDataSets(Collection<FailureRecord<DataSet>> result)
    {
        if (result == null)
        {
            return null;
        }
        List<DataSet> failedDataSets = result.stream().map(FailureRecord::getFailedItem).collect(Collectors.toList());
        sortDataSets(failedDataSets);
        return failedDataSets;
    }

    private void updateTimeStampFile(int numberOfCreatedThumbnailDataSets, List<DataSet> containerDataSets,
            List<DataSet> failedDataSets)
    {
        if (failedDataSets.isEmpty())
        {
            updateTimeStampFileWithLastDataSet(containerDataSets);
        } else
        {
            DataSet oldestFailedDataSet = failedDataSets.get(0);
            if (numberOfCreatedThumbnailDataSets > 0)
            {
                DataSet youngestNotFailedDataSet = tryGetYoungestNotFailedDataSet(containerDataSets, oldestFailedDataSet);
                if (youngestNotFailedDataSet != null)
                {
                    operationLog.info("Oldest failed data set: " + oldestFailedDataSet.getCode()
                            + ", youngest not failed data set: " + youngestNotFailedDataSet.getCode());
                    updateTimeStampFile(renderForComparison(youngestNotFailedDataSet));
                } else
                {
                    operationLog.info("Oldest failed data set: " + oldestFailedDataSet.getCode()
                            + ", time stamp file not updated.");
                }
            } else
            {
                operationLog.info("Ignoring failed data sets " + CollectionUtils.abbreviate(failedDataSets, 10));
                updateTimeStampFileWithLastDataSet(containerDataSets);
            }
        }
    }

    private void updateTimeStampFileWithLastDataSet(List<DataSet> containerDataSets)
    {
        if (containerDataSets.isEmpty() == false)
        {
            DataSet lastDataSet = containerDataSets.get(containerDataSets.size() - 1);
            operationLog.info("Update time stamp file with data set " + lastDataSet.getCode());
            updateTimeStampFile(renderForComparison(lastDataSet));
        }
    }

    private DataSet tryGetYoungestNotFailedDataSet(List<DataSet> dataSets, DataSet oldestFailedDataSet)
    {
        Object renderedOldestFailedDataSet = renderForComparison(oldestFailedDataSet);
        for (int i = 0; i < dataSets.size(); i++)
        {
            DataSet dataSet = dataSets.get(i);
            if (renderForComparison(dataSet).equals(renderedOldestFailedDataSet))
            {
                return i > 0 ? dataSets.get(i - 1) : null;
            }
        }
        return null;
    }

    private void sortDataSets(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new SimpleComparator<DataSet, String>()
            {
                @Override
                public String evaluate(DataSet dataSet)
                {
                    return renderForComparison(dataSet);
                }
            });
    }

    private String renderForComparison(DataSet dataSet)
    {
        return renderTimeStampAndCode(dataSet.getRegistrationDate(), dataSet.getCode());
    }

    private int createThumbnailDataSet(String sessionToken, DataSet containerDataSet)
    {
        TableModel tableModel = createThumbnailDataSetViaIngestionService(sessionToken, containerDataSet);
        List<TableModelRow> rows = tableModel.getRows();
        if (rows.isEmpty() == false)
        {
            List<ISerializableComparable> row = rows.get(0).getValues();
            if (row.isEmpty() == false)
            {
                List<TableModelColumnHeader> headers = tableModel.getHeader();
                if (headers.size() > 1 && headers.get(1).getTitle().equals("Error"))
                {
                    throw new RuntimeException(row.get(1).toString());
                } else
                {
                    ISerializableComparable cell = row.get(0);
                    if (cell instanceof IntegerTableCell)
                    {
                        return (int) ((IntegerTableCell) cell).getNumber();
                    }
                    operationLog.warn("Not an integer: " + cell);
                }
            }
        }
        return 0;
    }

    TableModel createThumbnailDataSetViaIngestionService(String sessionToken, DataSet containerDataSet)
    {
        String containerCode = containerDataSet.getCode();
        IImagingReadonlyQueryDAO imageDb = getImageDb();
        ImageDataSetStructure imageDataSetStructure = Utils.getImageDataSetStructure(imageDb, containerCode);
        SimpleImageContainerDataConfig config = new SimpleImageContainerDataConfig();

        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = createScriptRunner(imageDataSetStructure, config);
        Properties ingestionServiceProperties = createIngestionServiceProperties();
        File storeRoot = getDirectoryProvider().getStoreRoot();
        ScreeningJythonIngestionService ingestionService =
                new ScreeningJythonIngestionService(ingestionServiceProperties, storeRoot, scriptRunnerFactory)
                    {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public TableModel process(IDataSetRegistrationTransactionV2 transaction,
                                Map<String, Object> parameters, DataSetProcessingContext context)
                        {
                            // Populate object 'config' by the jython script based on 'imageDataSetStructure'
                            super.process(transaction, parameters, context);
                            int numberOfThumbnailDataSets = composeThumbnailDataSet(transaction, containerDataSet,
                                    imageDataSetStructure, config, context);
                            return new TableModel(Arrays.asList(new TableModelColumnHeader()),
                                    Arrays.asList(new TableModelRow(Arrays.asList(
                                            new IntegerTableCell(numberOfThumbnailDataSets)))));
                        }
                    };
        IHierarchicalContentProvider contentProvider = getHierarchicalContentProvider();
        DataSetProcessingContext context =
                new DataSetProcessingContext(contentProvider, null, null, null, null, null, sessionToken);
        return ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
    }

    private int composeThumbnailDataSet(IDataSetRegistrationTransactionV2 transaction, DataSet containerDataSet,
            ImageDataSetStructure imageDataSetStructure, SimpleImageDataConfig config, DataSetProcessingContext context)
    {
        int numberOfThumbnailDataSets = 0;
        String containerCode = containerDataSet.getCode();
        IDataSetUpdatable container = transaction.getDataSetForUpdate(containerCode);
        DataSet mainDataSet = getMainDataSet(containerDataSet);
        IImageGenerationAlgorithm imageGenerationAlgorithm = config.getImageGenerationAlgorithm();
        if (imageGenerationAlgorithm != null)
        {
            List<IDataSet> thumbnailDatasets = new ArrayList<>();
            IImageProvider imageProvider = new ImageCache();
            IHierarchicalContent content = context.getHierarchicalContentProviderUnfiltered().asContent(containerCode);
            imageGenerationAlgorithm.setContent(content);
            ImageDataSetInformation imageDataSetInformation = new ImageDataSetInformation();
            imageDataSetInformation.setImageDataSetStructure(imageDataSetStructure);
            long t0 = System.currentTimeMillis();
            List<BufferedImage> images = imageGenerationAlgorithm.generateImages(imageDataSetInformation,
                    thumbnailDatasets, imageProvider);
            operationLog.info(images.size() + " thumbnails have been created for data set " + containerCode
                    + " in " + (System.currentTimeMillis() - t0) + " msec.");
            IDataSet dataSet = Utils.createDataSetAndImageFiles(transaction, imageGenerationAlgorithm, images);
            List<String> components = new ArrayList<>(container.getContainedDataSetCodes());
            components.add(dataSet.getDataSetCode());
            container.setContainedDataSetCodes(components);
            ISearchService searchService = transaction.getSearchService();
            Experiment experiment = mainDataSet.getExperiment();
            if (experiment != null)
            {
                dataSet.setExperiment(searchService.getExperimentByPermId(experiment.getPermId().getPermId()));
            }
            Sample sample = mainDataSet.getSample();
            if (sample != null)
            {
                dataSet.setSample(searchService.getSampleByPermId(sample.getPermId().getPermId()));
            }
            numberOfThumbnailDataSets++;
        }

        List<ThumbnailsStorageFormat> thumbnailFormats = config.getImageStorageConfiguration().getThumbnailsStorageFormat();
        for (ThumbnailsStorageFormat thumbnailFormat : thumbnailFormats)
        {
            // to be implemented when needed
        }

        return numberOfThumbnailDataSets;
    }

    private Properties createIngestionServiceProperties()
    {
        Properties ingestionServiceProperties = new Properties(properties);
        ingestionServiceProperties.setProperty(DefaultStorageProcessor.DO_NOT_CREATE_ORIGINAL_DIR_KEY, "true");
        return ingestionServiceProperties;
    }

    private ScreeningPluginScriptRunnerFactory createScriptRunner(ImageDataSetStructure imageDataSetStructure,
            SimpleImageContainerDataConfig config)
    {
        ScreeningPluginScriptRunnerFactory scriptRunnerFactory = new ScreeningPluginScriptRunnerFactory(
                JythonBasedProcessingPlugin.getScriptPathProperty(properties))
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected IJythonEvaluator createEvaluator(String scriptString, String[] jythonPath,
                        DataSetProcessingContext context)
                {
                    synchronized(MicroscopyThumbnailsCreationTask.this)
                    {
                        IJythonEvaluator evaluator = super.createEvaluator(scriptString, jythonPath, context);
                        evaluator.set("image_data_set_structure", imageDataSetStructure);
                        evaluator.set("image_config", config);
                        return evaluator;
                    }
                }
            };
        return scriptRunnerFactory;
    }

    private boolean hasNoThumbnails(DataSet containerDataSet)
    {
        return getFirstMatchingComponentOrNull(containerDataSet, dataSetThumbnailTypePattern) == null;
    }

    private DataSet getMainDataSet(DataSet containerDataSet)
    {
        return getFirstMatchingComponentOrNull(containerDataSet, mainDataSetTypePattern);
    }

    private DataSet getFirstMatchingComponentOrNull(DataSet containerDataSet, Pattern pattern)
    {
        for (DataSet component : containerDataSet.getComponents())
        {
            if (pattern.matcher(component.getType().getCode()).matches())
            {
                return component;
            }
        }
        return null;
    }

    private String login()
    {
        ICredentials credentials = getEtlServerCredentials();
        return getService().login(credentials.getUserId(), credentials.getPassword());
    }

    protected IApplicationServerApi getService()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    protected IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }

    protected IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }

    protected IImagingReadonlyQueryDAO getImageDb()
    {
        return DssScreeningUtils.getQuery();
    }

}
