/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * Abstract superclass for calculating image transformations for images of each spot.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractSpotImagesTransformerProcessingPlugin extends AbstractDatastorePlugin
        implements IProcessingPluginTask
{
    protected abstract IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IHierarchicalContent hierarchicalContent);

    protected static final IImageTransformerFactoryProvider NO_TRANSFORMATION_PROVIDER =
            new IImageTransformerFactoryProvider()
                {
                    @Override
                    public IImageTransformerFactory tryGetTransformationFactory(
                            ImgImageEnrichedDTO image)
                    {
                        return null;
                    }
                };

    private static final long serialVersionUID = 1L;

    private static final String CHANNEL_CODE_PROPERTY = "channel";

    private static IImagingReadonlyQueryDAO query;

    private final String channelCode;

    public AbstractSpotImagesTransformerProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.channelCode = PropertyUtils.getMandatoryProperty(properties, CHANNEL_CODE_PROPERTY);
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> dataSets,
            DataSetProcessingContext context)
    {
        IImagingTransformerDAO transformerDAO = DssScreeningUtils.createImagingTransformerDAO();
        try
        {
            ProcessingStatus processingStatus = new ProcessingStatus();
            for (DatasetDescription dataSet : dataSets)
            {
                IHierarchicalContent hierarchicalContent =
                        createHierarchicalContent(context, dataSet);
                GroupByMap<Long, ImgImageEnrichedDTO> imagesBySpot = fetchImages(dataSet);
                for (Long spotId : imagesBySpot.getKeys())
                {
                    List<ImgImageEnrichedDTO> spotImages = imagesBySpot.tryGet(spotId);
                    calculateAndSetImageTransformation(spotImages, hierarchicalContent,
                            transformerDAO);
                }
                transformerDAO.commit();
                processingStatus.addDatasetStatus(dataSet, Status.OK);
            }
            return processingStatus;
        } finally
        {
            transformerDAO.close(true);
        }
    }

    interface IImageTransformerFactoryProvider
    {
        IImageTransformerFactory tryGetTransformationFactory(ImgImageEnrichedDTO image);
    }

    private static IImagingReadonlyQueryDAO getQuery()
    {
        if (query == null)
        {
            query = DssScreeningUtils.getQuery();
        }
        return query;
    }

    private void calculateAndSetImageTransformation(List<ImgImageEnrichedDTO> spotImages,
            IHierarchicalContent hierarchicalContent, IImagingTransformerDAO transformerDAO)
    {
        long start = System.currentTimeMillis();
        IImageTransformerFactoryProvider transformerFactoryProvider =
                getTransformationProvider(spotImages, hierarchicalContent);
        for (ImgImageEnrichedDTO image : spotImages)
        {
            IImageTransformerFactory transformationFactory =
                    transformerFactoryProvider.tryGetTransformationFactory(image);
            transformerDAO.saveTransformerFactoryForImage(image.getAcquiredImageId(),
                    transformationFactory);
        }
        operationLog.info(String.format("Processed %d images of the spot in %d msec.",
                spotImages.size(), (System.currentTimeMillis() - start)));
    }

    private IHierarchicalContent createHierarchicalContent(DataSetProcessingContext context,
            DatasetDescription dataSet)
    {
        return context.getHierarchicalContentProvider().asContent(dataSet.getDataSetCode());
    }

    private GroupByMap<Long, ImgImageEnrichedDTO> fetchImages(DatasetDescription dataset)
    {
        List<ImgImageEnrichedDTO> allImages =
                getQuery().listHCSImages(dataset.getDataSetCode(), channelCode);
        GroupByMap<Long, ImgImageEnrichedDTO> imagesBySpot =
                GroupByMap.create(allImages, new IGroupKeyExtractor<Long, ImgImageEnrichedDTO>()
                    {
                        @Override
                        public Long getKey(ImgImageEnrichedDTO image)
                        {
                            return image.getSpotId();
                        }
                    });
        if (allImages.size() == 0)
        {
            operationLog
                    .warn(String
                            .format("Dataset %s has no images for channel '%s' to process! Have you specified the correct channel code?",
                                    dataset.getDataSetCode(), channelCode));
        } else
        {
            operationLog
                    .info(String
                            .format("Dataset %s has %d images (devided between %d spots) for channel '%s' to process.",
                                    dataset.getDataSetCode(), allImages.size(), imagesBySpot
                                            .getKeys().size(), channelCode));
        }
        return imagesBySpot;
    }
}
