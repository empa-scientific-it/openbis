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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageTransformationDTO;

/**
 * Helper class for retrieving and/or creating entities associated with the imaging database: experiments, containers, channels and datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Tomasz Pylak
 */
public class ImagingDatabaseHelper
{
    private final IImagingQueryDAO dao;

    private ImagingDatabaseHelper(IImagingQueryDAO dao)
    {
        this.dao = dao;
    }

    /**
     * Creates channels connected to the specified dataset id.
     */
    public static ImagingChannelsMap getOrCreateDatasetChannels(IImagingQueryDAO dao, long datasetId,
            List<Channel> channels)
    {
        ChannelOwner channelOwner = ChannelOwner.createDataset(datasetId);
        return new ImagingChannelsCreator(dao).getOrCreateChannelsMap(channelOwner, channels);
    }

    /** @return container id */
    public static long getOrCreateExperimentAndContainer(IImagingQueryDAO dao,
            HCSContainerDatasetInfo info)
    {
        return doGetOrCreateExperimentAndContainer(dao, info).getContainerId();
    }

    /**
     * NOTE: Code responsible for trying to get sample and experiment from the DB and creating them if they don't exist is in synchronized block and
     * uses currently opened transaction. Then the transaction is closed and data set is added to the DB in second transaction. If second transaction
     * will be rolled back sample and experiment created in first transaction will stay in the DB.
     */
    private static ExperimentAndContainerIds doGetOrCreateExperimentAndContainer(
            IImagingQueryDAO dao, HCSContainerDatasetInfo info)
    {
        synchronized (IImagingQueryDAO.class)
        {
            CreatedOrFetchedEntity exp = getOrCreateExperiment(dao, info);
            CreatedOrFetchedEntity cont = getOrCreateContainer(dao, info, exp.getId());
            if (exp.hasAlreadyExisted() == false || cont.hasAlreadyExisted() == false)
            {
                // without this commit other threads will not see the new experiment/sample when the
                // synchronized block ends
                dao.commit();
            }
            return new ExperimentAndContainerIds(exp.getId(), cont.getId());
        }
    }

    /**
     * NOTE: Code responsible for trying to get sample and experiment from the DB and creating them if they don't exist is in synchronized block and
     * uses currently opened transaction. Then the transaction is closed and data set is added to the DB in second transaction. If second transaction
     * will be rolled back sample and experiment created in first transaction will stay in the DB.
     */
    public static ExperimentWithChannelsAndContainer getOrCreateExperimentWithChannelsAndContainer(
            IImagingQueryDAO dao, HCSContainerDatasetInfo info, List<Channel> channels)
    {
        ImagingDatabaseHelper helper = new ImagingDatabaseHelper(dao);
        synchronized (IImagingQueryDAO.class)
        {
            CreatedOrFetchedEntity exp = getOrCreateExperiment(dao, info);
            long expId = exp.getId();
            CreatedOrFetchedEntity cont = getOrCreateContainer(dao, info, expId);
            ImagingChannelsMap channelsMap =
                    helper.getOrCreateChannels(ChannelOwner.createExperiment(expId), channels);
            // without this commit other threads will not see the new experiment/sample/channels
            // when the synchronized block ends.
            dao.commit();
            return new ExperimentWithChannelsAndContainer(expId, cont.getId(), channelsMap);
        }
    }

    private ImagingChannelsMap getOrCreateChannels(ChannelOwner channelOwner, List<Channel> channels)
    {
        return new ImagingChannelsCreator(dao).getOrCreateChannelsMap(channelOwner, channels);
    }

    private static CreatedOrFetchedEntity getOrCreateContainer(IImagingQueryDAO dao,
            HCSContainerDatasetInfo info, long expId)
    {
        String containerPermId = info.getContainerSamplePermId();
        Long containerId = dao.tryGetContainerIdPermId(containerPermId);
        if (containerId != null)
        {
            return new CreatedOrFetchedEntity(true, containerId);
        } else
        {
            ImgContainerDTO container =
                    new ImgContainerDTO(containerPermId, info.getContainerRows(),
                            info.getContainerColumns(), expId);
            containerId = dao.addContainer(container);
            return new CreatedOrFetchedEntity(false, containerId);
        }
    }

    private static CreatedOrFetchedEntity getOrCreateExperiment(IImagingQueryDAO dao,
            HCSContainerDatasetInfo info)
    {
        String experimentPermId = info.getExperimentPermId();
        ImgExperimentDTO experiment = dao.tryGetExperimentByPermId(experimentPermId);
        if (experiment != null)
        {
            return new CreatedOrFetchedEntity(true, experiment.getId());
        } else
        {
            Long expId = dao.addExperiment(experimentPermId);
            return new CreatedOrFetchedEntity(false, expId);
        }
    }

    private static class CreatedOrFetchedEntity
    {
        private final boolean alreadyExisted;

        private final long id;

        public CreatedOrFetchedEntity(boolean alreadyExisted, long id)
        {
            this.alreadyExisted = alreadyExisted;
            this.id = id;
        }

        public boolean hasAlreadyExisted()
        {
            return alreadyExisted;
        }

        public long getId()
        {
            return id;
        }
    }

    private static class ExperimentAndContainerIds
    {
        private final long experimentId;

        private final long containerId;

        public ExperimentAndContainerIds(long experimentId, long containerId)
        {
            this.experimentId = experimentId;
            this.containerId = containerId;
        }

        public long getExperimentId()
        {
            return experimentId;
        }

        public long getContainerId()
        {
            return containerId;
        }
    }

    public static class ImagingChannelsMap
    {
        private final Map<String/* channel code */, Long/* tech id */> channelsMap;

        public ImagingChannelsMap(Map<String, Long> channelsMap)
        {
            this.channelsMap = channelsMap;
        }

        /** channel must be defined */
        public long getChannelId(String channelCode)
        {
            Long channelId = channelsMap.get(channelCode);
            if (channelId == null)
            {
                throw new UserFailureException("Undefined channel " + channelCode);
            }
            return channelId;
        }
    }

    public static class ExperimentWithChannelsAndContainer extends ExperimentAndContainerIds
    {
        private final ImagingChannelsMap channelsMap;

        public ExperimentWithChannelsAndContainer(long experimentId, long containerId,
                ImagingChannelsMap channelsMap)
        {
            super(experimentId, containerId);
            this.channelsMap = channelsMap;
        }

        public ImagingChannelsMap getChannelsMap()
        {
            return channelsMap;
        }
    }

    /** Logic to find or create channels */
    @Private
    static class ImagingChannelsCreator
    {
        private final IImagingQueryDAO dao;

        public ImagingChannelsCreator(IImagingQueryDAO dao)
        {
            this.dao = dao;
        }

        public ImagingChannelsMap getOrCreateChannelsMap(ChannelOwner channelOwner,
                List<Channel> channels)
        {
            Map<String, Long> map = getOrCreateChannels(channelOwner, channels);
            return new ImagingChannelsMap(map);
        }

        private Map<String, Long> getOrCreateChannels(ChannelOwner channelOwner,
                List<Channel> channels)
        {
            fillMissingChannelColors(channels);
            if (channelOwner.tryGetExperimentId() != null)
            {
                long expId = channelOwner.tryGetExperimentId();
                List<ImgChannelDTO> allChannels = dao.getChannelsByExperimentId(expId);
                if (allChannels.size() == 0)
                {
                    return createChannels(channelOwner, channels);
                } else
                {
                    return updateExperimentChannels(expId, channels, allChannels);
                }
            } else
            {
                List<ImgChannelDTO> allChannels =
                        dao.getChannelsByDatasetId(channelOwner.tryGetDatasetId());
                if (allChannels.size() == 0)
                {
                    return createChannels(channelOwner, channels);
                } else
                {
                    Map<String, Long> map = new HashMap<String, Long>();
                    for (ImgChannelDTO channelDTO : allChannels)
                    {

                        addChannel(map, channelDTO);
                    }
                    return map;
                }
            }
        }

        private Map<String, Long> updateExperimentChannels(long expId, List<Channel> channels,
                List<ImgChannelDTO> allChannels)
        {
            Map<String/* name */, ImgChannelDTO> existingChannels = asNameMap(allChannels);
            Map<String, Long> map = new HashMap<String, Long>();
            for (Channel channel : channels)
            {
                ImgChannelDTO channelDTO =
                        updateExperimentChannel(channel, expId, existingChannels);
                addChannel(map, channelDTO);
            }
            return map;
        }

        private Map<String, Long> createChannels(ChannelOwner channelOwner, List<Channel> channels)
        {
            Map<String, Long> map = new HashMap<String, Long>();
            for (Channel channel : channels)
            {
                ImgChannelDTO channelDTO = createChannel(channel, channelOwner);
                addChannel(map, channelDTO);
            }
            return map;
        }

        @Private
        static void fillMissingChannelColors(List<Channel> channels)
        {
            Set<Integer> usedColorsIndieces = createUsedColorsIndiecesSet(channels);
            for (Channel channel : channels)
            {
                if (channel.tryGetChannelColor() == null)
                {
                    int colorIndex = getSmallestUnused(usedColorsIndieces);
                    ChannelColor color = ChannelColor.createFromIndex(colorIndex);
                    channel.setChannelColor(color);
                    usedColorsIndieces.add(colorIndex);
                }
            }
        }

        private static Set<Integer> createUsedColorsIndiecesSet(List<Channel> channels)
        {
            Set<Integer> usedColorsIndieces = new HashSet<Integer>();
            for (Channel channel : channels)
            {
                ChannelColorRGB channelColorRGB = channel.tryGetChannelColor();
                if (channelColorRGB != null)
                {
                    ChannelColor channelColor = findNearestPlainChannelColor(channelColorRGB);
                    usedColorsIndieces.add(channelColor.getColorOrderIndex());
                }
            }
            return usedColorsIndieces;
        }

        @Private
        static ChannelColor findNearestPlainChannelColor(ChannelColorRGB color)
        {
            int r = color.getR();
            int g = color.getG();
            int b = color.getB();
            int max = (r > g ? r : g);
            max = (b > max ? b : max);

            if (r == max)
            {
                if (r == g)
                {
                    return ChannelColor.RED_GREEN;
                } else if (r == b)
                {
                    return ChannelColor.RED_BLUE;
                } else
                {
                    return ChannelColor.RED;
                }
            } else if (g == max)
            {
                if (g == b)
                {
                    return ChannelColor.GREEN_BLUE;
                } else
                {
                    return ChannelColor.GREEN;
                }
            } else
            {
                return ChannelColor.BLUE;
            }
        }

        private static int getSmallestUnused(Set<Integer> usedColorsIndieces)
        {
            int colorIndex = 0;
            while (usedColorsIndieces.contains(colorIndex))
            {
                colorIndex++;
            }
            return colorIndex;
        }

        private static void addChannel(Map<String, Long> map, ImgChannelDTO channelDTO)
        {
            map.put(channelDTO.getCode(), channelDTO.getId());
        }

        private static Map<String, ImgChannelDTO> asNameMap(List<ImgChannelDTO> channels)
        {
            Map<String, ImgChannelDTO> nameMap = new HashMap<String, ImgChannelDTO>();
            for (ImgChannelDTO channel : channels)
            {
                nameMap.put(channel.getCode(), channel);
            }
            return nameMap;
        }

        private ImgChannelDTO updateExperimentChannel(Channel channel, long expId,
                Map<String, ImgChannelDTO> existingChannels)
        {
            ImgChannelDTO channelDTO =
                    makeChannelDTO(channel, ChannelOwner.createExperiment(expId));
            String channelCode = channelDTO.getCode();
            ImgChannelDTO existingChannel = existingChannels.get(channelCode);
            if (existingChannel == null)
            {
                throw createInvalidNewExperimentChannelException(expId, existingChannels,
                        channelCode);
            }
            // a channel with a specified name already exists for an experiment, its description
            // will be updated. Wavelength will be updated only if it was null before.
            if (channelDTO.getWavelength() == null)
            {
                channelDTO.setWavelength(existingChannel.getWavelength());
            }
            if (existingChannel.getWavelength() != null
                    && existingChannel.getWavelength().equals(channelDTO.getWavelength()) == false)
            {
                throw UserFailureException
                        .fromTemplate(
                                "There are already datasets registered for the experiment "
                                        + "which use the same channel code, but with a different wavelength! "
                                        + "Channel %s, old wavelength %d, new wavelength %d.",
                                channelCode, existingChannel.getWavelength(),
                                channelDTO.getWavelength());
            }
            channelDTO.setId(existingChannel.getId());
            dao.updateChannel(channelDTO);
            return channelDTO;
        }

        private static UserFailureException createInvalidNewExperimentChannelException(long expId,
                Map<String, ImgChannelDTO> existingChannels, String channelName)
        {
            return UserFailureException.fromTemplate(
                    "Experiment with id '%d' has already some channels registered "
                            + "and does not have a channel with a code '%s'. "
                            + "Register a new experiment to use new channels. "
                            + "Available channel names in this experiment: %s.", expId,
                    channelName, existingChannels.keySet());
        }

        private ImgChannelDTO createChannel(Channel channel, ChannelOwner channelOwner)
        {
            ImgChannelDTO channelDTO = makeChannelDTO(channel, channelOwner);
            long channelId = dao.addChannel(channelDTO);
            channelDTO.setId(channelId);
            saveChannelImageTransformations(channelId, channel.getAvailableTransformations());
            return channelDTO;
        }

        private void saveChannelImageTransformations(long channelId,
                ImageTransformation[] transformations)
        {
            if (transformations.length == 0)
            {
                return;
            }
            ArrayList<ImgImageTransformationDTO> transformationDTOs =
                    new ArrayList<ImgImageTransformationDTO>();
            for (ImageTransformation transformation : transformations)
            {
                ImgImageTransformationDTO transformationDTO =
                        createTransformationDTO(transformation, channelId);
                if (transformationDTO.getIsDefault())
                {
                    // default transformation should be always first
                    transformationDTOs.add(0, transformationDTO);
                } else
                {
                    // add at the end
                    transformationDTOs.add(transformationDTO);
                }
            }
            dao.addImageTransformations(transformationDTOs);
        }

        private ImgImageTransformationDTO createTransformationDTO(ImageTransformation tr,
                long channelId)
        {
            return new ImgImageTransformationDTO(tr.getCode(), tr.getLabel(), tr.getDescription(),
                    tr.isDefault(), channelId, tr.getImageTransformerFactory(), tr.isEditable());
        }

        private static ImgChannelDTO makeChannelDTO(Channel channel, ChannelOwner channelOwner)
        {
            ChannelColorRGB channelColor = channel.tryGetChannelColor();
            assert channelColor != null : "channel color should be specified at this point";

            return new ImgChannelDTO(channel.getCode(), channel.tryGetDescription(),
                    channel.tryGetWavelength(), channelOwner.tryGetDatasetId(),
                    channelOwner.tryGetExperimentId(), channel.getLabel(), channelColor.getR(),
                    channelColor.getG(), channelColor.getB());
        }
    }

    /** DTO to store channel owner: dataset id or experiment id */
    private static class ChannelOwner
    {
        private final Long expIdOrNull;

        private final Long datasetIdOrNull;

        public static ChannelOwner createDataset(long datasetId)
        {
            return new ChannelOwner(null, datasetId);
        }

        public static ChannelOwner createExperiment(long expId)
        {
            return new ChannelOwner(expId, null);
        }

        private ChannelOwner(Long expIdOrNull, Long datasetIdOrNull)
        {
            this.expIdOrNull = expIdOrNull;
            this.datasetIdOrNull = datasetIdOrNull;
        }

        public Long tryGetExperimentId()
        {
            return expIdOrNull;
        }

        public Long tryGetDatasetId()
        {
            return datasetIdOrNull;
        }

    }
}
