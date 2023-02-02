/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.security.MD5ChecksumCalculator;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelStackDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;

/**
 * Helper class for easy handling of HCS image dataset standard structure with no code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSDatasetLoader implements IImageDatasetLoader
{
    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HCSDatasetLoader.class);

    protected final IImagingReadonlyQueryDAO query;

    protected final ImgImageDatasetDTO dataset;

    protected ImgContainerDTO containerOrNull;

    protected ImgExperimentDTO experimentOrNull;

    protected Integer channelCount;

    protected List<ImgChannelDTO> channels;

    protected Map<Long/* channel id */, List<ImgImageTransformationDTO>> availableImageTransformationsMap;

    private final String mergedChannelTransformerFactorySignatureOrNull;

    /**
     * @return null if the dataset is not found in the imaging database
     */
    public static HCSDatasetLoader tryCreate(IImagingReadonlyQueryDAO query, String datasetPermId)
    {
        ImgImageDatasetDTO dataset = query.tryGetImageDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            operationLog.warn(String.format(
                    "No dataset with code '%s' found in the imaging database.", datasetPermId));
            return null;
        } else
        {
            return new HCSDatasetLoader(query, dataset);
        }
    }

    protected HCSDatasetLoader(IImagingReadonlyQueryDAO query, ImgImageDatasetDTO dataset)
    {
        this.query = query;
        this.dataset = dataset;

        Long containerId = dataset.getContainerId();
        if (containerId != null)
        {
            this.containerOrNull = query.getContainerById(containerId);
            this.experimentOrNull = query.tryGetExperimentById(containerOrNull.getExperimentId());
        } else
        {
            this.containerOrNull = null;
            this.experimentOrNull = null;
        }

        this.mergedChannelTransformerFactorySignatureOrNull =
                tryGetImageTransformerFactorySignatureForMergedChannels();
        this.channels = loadChannels();
        this.availableImageTransformationsMap = loadAvailableImageTransformations();
    }

    private Map<Long, List<ImgImageTransformationDTO>> loadAvailableImageTransformations()
    {
        List<ImgImageTransformationDTO> imageTransformations =
                query.listImageTransformationsByDatasetId(dataset.getId());
        if (imageTransformations.size() == 0 && containerOrNull != null)
        {
            imageTransformations =
                    query.listImageTransformationsByExperimentId(containerOrNull.getExperimentId());
        }
        return GroupByMap.create(imageTransformations,
                new IGroupKeyExtractor<Long, ImgImageTransformationDTO>()
                    {
                        @Override
                        public Long getKey(ImgImageTransformationDTO transformation)
                        {
                            return transformation.getChannelId();
                        }
                    }).getMap();
    }

    private List<ImgChannelDTO> loadChannels()
    {
        List<ImgChannelDTO> myChannels = query.getChannelsByDatasetId(dataset.getId());
        if (myChannels.size() == 0 && containerOrNull != null)
        {
            myChannels = query.getChannelsByExperimentId(containerOrNull.getExperimentId());
        }
        return myChannels;
    }

    private String tryGetImageTransformerFactorySignatureForMergedChannels()
    {
        byte[] imageTransformerFactory = dataset.getSerializedImageTransformerFactory();
        if (imageTransformerFactory == null && experimentOrNull != null)
        {
            imageTransformerFactory = experimentOrNull.getSerializedImageTransformerFactory();
        }
        return tryGetSignature(imageTransformerFactory);
    }

    protected final ImgContainerDTO tryGetContainer()
    {
        return containerOrNull;
    }

    protected final ImgImageDatasetDTO getDataset()
    {
        return dataset;
    }

    public int getChannelCount()
    {
        return channels.size();
    }

    @Override
    public List<ImageChannelStack> listImageChannelStacks(WellLocation wellLocationOrNull)
    {
        List<ImgChannelStackDTO> stacks;
        if (wellLocationOrNull != null)
        {
            int spotYRow = wellLocationOrNull.getRow();
            int spotXColumn = wellLocationOrNull.getColumn();
            stacks = query.listChannelStacks(dataset.getId(), spotXColumn, spotYRow);
        } else
        {
            stacks = query.listSpotlessChannelStacks(dataset.getId());
        }
        return convert(stacks);
    }

    private static List<ImageChannelStack> convert(List<ImgChannelStackDTO> stacks)
    {
        List<ImageChannelStack> result = new ArrayList<ImageChannelStack>();
        for (ImgChannelStackDTO stack : stacks)
        {
            result.add(convert(stack));
        }
        return result;
    }

    private static ImageChannelStack convert(ImgChannelStackDTO stack)
    {
        return new ImageChannelStack(stack.getId(), stack.getRow(), stack.getColumn(),
                stack.getT(), stack.getZ(), stack.getSeriesNumber());
    }

    @Override
    public ImageDatasetParameters getImageParameters()
    {
        ImageDatasetParameters params = new ImageDatasetParameters();
        params.setDatasetCode(dataset.getPermId());
        if (containerOrNull != null)
        {
            params.setRowsNum(containerOrNull.getNumberOfRows());
            params.setColsNum(containerOrNull.getNumberOfColumns());
        }
        params.setTileRowsNum(getDataset().getFieldNumberOfRows());
        params.setTileColsNum(getDataset().getFieldNumberOfColumns());
        params.setIsMultidimensional(dataset.getIsMultidimensional());
        params.setMergedChannelTransformerFactorySignature(mergedChannelTransformerFactorySignatureOrNull);
        params.setInternalChannels(convertChannels());
        return params;
    }

    private List<InternalImageChannel> convertChannels()
    {
        List<InternalImageChannel> convertedChannels = new ArrayList<InternalImageChannel>();
        for (ImgChannelDTO channelDTO : channels)
        {
            InternalImageChannel channel = convert(channelDTO);
            convertedChannels.add(channel);
        }
        return convertedChannels;
    }

    private InternalImageChannel convert(ImgChannelDTO channelDTO)
    {
        List<InternalImageTransformationInfo> availableImageTransformations =
                convertTransformations(availableImageTransformationsMap.get(channelDTO.getId()));
        return new InternalImageChannel(channelDTO.getCode(), channelDTO.getLabel(),
                channelDTO.getDescription(), channelDTO.getWavelength(),
                availableImageTransformations);
    }

    private static List<InternalImageTransformationInfo> convertTransformations(
            List<ImgImageTransformationDTO> transformationsOrNull)
    {
        if (transformationsOrNull == null)
        {
            return new ArrayList<InternalImageTransformationInfo>();
        } else
        {
            Collection<InternalImageTransformationInfo> transformations =
                    org.apache.commons.collections4.CollectionUtils
                            .collect(
                                    transformationsOrNull,
                                    new org.apache.commons.collections4.Transformer<ImgImageTransformationDTO, InternalImageTransformationInfo>()
                                        {
                                            @Override
                                            public InternalImageTransformationInfo transform(
                                                    ImgImageTransformationDTO transformation)
                                            {
                                                return convert(transformation);
                                            }
                                        });
            return new LinkedList<InternalImageTransformationInfo>(transformations);
        }
    }

    private static InternalImageTransformationInfo convert(ImgImageTransformationDTO transformation)
    {
        String transformationSignature =
                tryGetSignature(transformation.getSerializedImageTransformerFactory());
        return new InternalImageTransformationInfo(transformation.getCode(),
                transformation.getLabel(), transformation.getDescription(),
                transformationSignature, transformation.getIsDefault());
    }

    private static String tryGetSignature(byte[] bytesOrNull)
    {
        if (bytesOrNull == null)
        {
            return null;
        }
        return MD5ChecksumCalculator.calculate(bytesOrNull).substring(0, 10);
    }
}