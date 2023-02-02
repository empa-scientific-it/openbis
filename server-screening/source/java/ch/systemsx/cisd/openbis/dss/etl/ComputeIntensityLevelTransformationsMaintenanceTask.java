/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueDecorator;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformationBuffer;
import ch.systemsx.cisd.openbis.dss.etl.jython.v1.SimpleImageDataSetRegistrator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;

/**
 * @author Jakub Straszewski
 */
public class ComputeIntensityLevelTransformationsMaintenanceTask implements IMaintenanceTask
{

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ComputeIntensityLevelTransformationsMaintenanceTask.class);

    /*
     * Settings
     */

    private static final String COMPUTE_MIN_MAX_LEVELS_KEY = "compute-min-max-levels";

    private static final String DEFAULT_COMPUTE_MIN_MAX_LEVELS = "true";

    private static final String MIN_LEVEL_KEY = "min-level";

    private static final String MAX_LEVEL_KEY = "max-level";

    private static final String INTENSITY_THRESHOLD_KEY = "intensity-threshold";

    private static final String TRASNFORMATION_CODE_KEY = "transformation-code";

    private static final String TRASNFORMATION_LABEL_KEY = "transformation-label";

    private static final String TRANSFORMATION_DESC_KEY = "transformation-description";

    private static final String IS_DEFAULT_TRANSFORMATION_KEY = "is-default-transformation";

    private static final String STATUS_FILENAME_KEY = "status-filename";

    private static final String BATCH_SIZE_KEY = "batch-size";

    private boolean computeMinMaxLevels;

    private int minLevel;

    private int maxLevel;

    private float intensityThreshold;

    private String transformationCode;

    private String transformationLabel;

    private String transformationDescription;

    private boolean isDefaultTransformation;

    private int batchSize;

    private File queueFile;

    /*
     * Resources
     */
    private DataSource dataSource;

    private IImagingTransformerDAO dao;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider contentProvider;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);

        dao = QueryTool.getQuery(dataSource, IImagingTransformerDAO.class);
        dao.listSpots(0L); // testing correct database set up
        service = ServiceProvider.getOpenBISService();
        contentProvider = ServiceProvider.getHierarchicalContentProvider();

        computeMinMaxLevels =
                Boolean.valueOf(properties.getProperty(COMPUTE_MIN_MAX_LEVELS_KEY,
                        DEFAULT_COMPUTE_MIN_MAX_LEVELS));

        minLevel = PropertyUtils.getInt(properties, MIN_LEVEL_KEY, -1);
        maxLevel = PropertyUtils.getInt(properties, MAX_LEVEL_KEY, -1);

        if (false == computeMinMaxLevels)
        {
            if (minLevel == -1 || maxLevel == -1)
            {
                throw new ConfigurationFailureException(
                        "The intensity ranges levels must be correctly defined if calculating them is disabled.");
            }
        }

        intensityThreshold =
                (float) PropertyUtils.getDouble(properties, INTENSITY_THRESHOLD_KEY, -1);
        if (computeMinMaxLevels && (intensityThreshold < 0 || intensityThreshold > 1))
        {
            throw new ConfigurationFailureException(
                    "The "
                            + INTENSITY_THRESHOLD_KEY
                            + " property must be provided as a real number between 0 and 1 if the levels are calculated.");
        }

        transformationCode =
                PropertyUtils.getMandatoryProperty(properties, TRASNFORMATION_CODE_KEY);

        transformationLabel = properties.getProperty(TRASNFORMATION_LABEL_KEY, "Fixed rescaling");

        transformationDescription =
                properties
                        .getProperty(
                                TRANSFORMATION_DESC_KEY,
                                SimpleImageDataSetRegistrator.OPTIMAL_DATASET_INTENSITY_RESCALING_DESCRIPTION);

        String isDefaultTransformationString =
                PropertyUtils.getMandatoryProperty(properties, IS_DEFAULT_TRANSFORMATION_KEY);

        isDefaultTransformation = Boolean.valueOf(isDefaultTransformationString);

        String queueFilePath = PropertyUtils.getMandatoryProperty(properties, STATUS_FILENAME_KEY);
        queueFile = new File(queueFilePath);

        batchSize = PropertyUtils.getInt(properties, BATCH_SIZE_KEY, 1);
        if (batchSize < 1)
        {
            throw new ConfigurationFailureException("Batch size must be at least 1");
        }
    }

    /**
     * method overriden to create an underlying database connection.
     */
    @Override
    public void execute()
    {
        PersistentExtendedBlockingQueueDecorator<Long> queue =
                PersistentExtendedBlockingQueueFactory.<Long> createSmartPersist(queueFile);
        try
        {
            executeTransformations(queue);
        } catch (Exception e)
        {
            operationLog.error(e);
        }
        queue.close();
    }

    private void executeTransformations(PersistentExtendedBlockingQueueDecorator<Long> queue)
    {
        for (int i = 0; i < batchSize; i++)
        {
            boolean result = executeOnce(queue);

            if (false == result)
            {
                return;
            }
        }
    }

    /**
     * Return true if there are still potentially datasets to process.
     */
    private boolean executeOnce(PersistentExtendedBlockingQueueDecorator<Long> queue)
    {
        long lastSeenId = getLastSeenIDFromQueue(queue);
        Long nextId = dao.tryGetNextDatasetId(lastSeenId);

        // if this value is null, it means there are no newer datasets
        if (nextId != null)
        {
            if (false == dao.hasDatasetDefinedTransformation(nextId, transformationCode))
            {
                executeTransformations(nextId);
            }
            storeLastSeenInTheQueue(queue, nextId);
            return true;
        }
        return false;
    }

    private void executeTransformations(long datasetId)
    {
        ImgImageDatasetDTO imageDataset = dao.tryGetImageDatasetById(datasetId);
        List<ImgChannelDTO> channels = dao.getChannelsByDatasetId(datasetId);

        operationLog.info("Will create transformation " + transformationCode + " for the dataset "
                + imageDataset.getPermId());

        final Levels intensityRange;
        if (computeMinMaxLevels)
        {
            intensityRange = computeIntensityRange(imageDataset, channels);
            if (intensityRange != null)
            {
                operationLog.info("Calculated intensity ranges for dataset "
                        + imageDataset.getPermId() + ". " + intensityRange);
            }
        } else
        {
            intensityRange = new Levels(minLevel, maxLevel);
            operationLog.info("Using default intensity range values " + intensityRange);
        }

        // if intensity ranges are null, it means that this transformation cannot be applied to this
        // dataset. Thus we do nothing
        if (intensityRange != null)
        {
            IImageTransformerFactory factory = createImageTransformerFactory(intensityRange);

            for (ImgChannelDTO ch : channels)
            {
                // execute inserts on dao, but without a commit
                addCommonIntensityRangeTransformation(ch, factory);
            }

            // commits the dao transaction
            dao.commit();
            operationLog.info("Succesfully applied intensity ranges transformation to "
                    + imageDataset.getPermId() + " on " + channels.size() + " channels.");
        } else
        {
            operationLog.warn("Could not calculate intensity ranges for dataset "
                    + imageDataset.getPermId() + ". Will be skipped.");
        }
    }

    private IImageTransformerFactory createImageTransformerFactory(final Levels intensityRange)
    {
        ImageTransformationBuffer buffer = new ImageTransformationBuffer();

        ImageTransformation imageTransformation =
                buffer.appendRescaleGrayscaleIntensity(intensityRange.getMinLevel(),
                        intensityRange.getMaxLevel(), "N/A");

        IImageTransformerFactory factory = imageTransformation.getImageTransformerFactory();
        return factory;
    }

    /**
     * Computes intensity ranges for all images in the dataset.
     * 
     * @return calculated levels, or null if calculation cannot be done because of a permanent problem
     */
    private Levels computeIntensityRange(ImgImageDatasetDTO imageDataset,
            List<ImgChannelDTO> channels)
    {
        AbstractExternalData dataset = service.tryGetDataSet(imageDataset.getPermId());

        if (dataset == null)
        {
            operationLog
                    .warn("The dataset specified in the image_data_set doesn't exist in openbis.");
            return null;
        }

        IHierarchicalContent content = contentProvider.asContent(dataset);

        final List<File> imagePaths = new LinkedList<File>();

        for (ImgChannelDTO ch : channels)
        {
            List<ImgImageEnrichedDTO> images =
                    dao.listHCSImages(imageDataset.getPermId(), ch.getCode());

            for (ImgImageEnrichedDTO img : images)
            {
                File imageFile = content.getNode(img.getFilePath()).getFile();
                imagePaths.add(imageFile);
                if (false == imageFile.exists())
                {
                    throw new IllegalStateException("The image file for dataset "
                            + imageDataset.getPermId() + " doesn't exist.");
                }
            }
        }

        return SimpleImageDataSetRegistrator.tryComputeCommonIntensityRange(null, imagePaths,
                intensityThreshold);
    }

    private void addCommonIntensityRangeTransformation(ImgChannelDTO channel,
            IImageTransformerFactory factory)
    {
        ImgImageTransformationDTO dto =
                new ImgImageTransformationDTO(transformationCode, transformationLabel,
                        transformationDescription, isDefaultTransformation, channel.getId(),
                        factory, false);

        dao.addImageTransformation(dto);
    }

    private void storeLastSeenInTheQueue(PersistentExtendedBlockingQueueDecorator<Long> queue,
            long lastSeenId)
    {
        queue.offer(lastSeenId);
        while (queue.size() > 1)
        {
            queue.remove();
        }
    }

    private long getLastSeenIDFromQueue(PersistentExtendedBlockingQueueDecorator<Long> queue)
    {
        long lastSeenId = -1;
        if (queue.size() > 0)
        {
            lastSeenId = queue.peek();
        }
        return lastSeenId;
    }

}
