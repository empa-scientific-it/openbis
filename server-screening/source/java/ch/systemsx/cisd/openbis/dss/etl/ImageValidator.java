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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.DatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimension;

/**
 * A class that validates images.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageValidator
{
    private final DatasetOwnerInformation dataSetInformation;

    private final IMailClient mailClient;

    private final File incomingDataSetDirectory;

    private final ImageFileExtractionResult extractionResult;

    private final Logger operationLog;

    private final Logger notificationLog;

    private final boolean notifyIfPlateIncomplete;

    private AbstractImageChecklist imageCheckList;

    /**
     * Constructor.
     * 
     * @param dataSetInformation
     * @param mailClient
     * @param incomingDataSetDirectory
     * @param extractionResult
     * @param operationLog
     */
    public ImageValidator(DatasetOwnerInformation dataSetInformation, IMailClient mailClient,
            File incomingDataSetDirectory, ImageFileExtractionResult extractionResult,
            Logger operationLog, Logger notificationLog, boolean notifyIfPlateIncomplete)
    {
        super();
        this.dataSetInformation = dataSetInformation;
        this.mailClient = mailClient;
        this.incomingDataSetDirectory = incomingDataSetDirectory;
        this.extractionResult = extractionResult;
        this.operationLog = operationLog;
        this.notificationLog = notificationLog;
        this.notifyIfPlateIncomplete = notifyIfPlateIncomplete;
    }

    /**
     * Validate the images and throw exceptions if they are not valid.
     */
    public boolean validateImages()
    {
        initializeImageCheckList();
        checkImagesForDuplicates();
        if (extractionResult.getInvalidFiles().size() > 0)
        {
            throw UserFailureException.fromTemplate("Following invalid files %s have been found.",
                    CollectionUtils.abbreviate(extractionResult.getInvalidFiles(), 10));
        }
        if (extractionResult.getImages().size() == 0)
        {
            throw UserFailureException.fromTemplate(
                    "No extractable files were found inside a dataset '%s'."
                            + " Have you changed your naming convention?",
                    incomingDataSetDirectory.getAbsolutePath());
        }
        return checkCompleteness();
    }

    private void initializeImageCheckList()
    {
        List<Channel> channels = extractionResult.getChannels();
        Geometry tileGeometry = extractionResult.getTileGeometry();

        PlateDimension plateGeometry;
        try
        {
            plateGeometry = getPlateGeometry();
        } catch (EnvironmentFailureException e)
        {
            plateGeometry = null;
        }
        List<String> channelCodes = new ArrayList<String>();
        for (Channel channel : channels)
        {
            channelCodes.add(channel.getCode());
        }
        imageCheckList =
                (null == plateGeometry) ? new MicroscopyImageChecklist(channelCodes, tileGeometry)
                        : new HCSImageCheckList(channelCodes, plateGeometry, tileGeometry);
    }

    private void checkImagesForDuplicates()
    {
        List<AcquiredSingleImage> images = extractionResult.getImages();
        for (AcquiredSingleImage image : images)
        {
            imageCheckList.checkOff(image);
        }
        imageCheckList.checkForDuplicates();
    }

    private boolean checkCompleteness()
    {
        String dataSetFileName = incomingDataSetDirectory.getName();
        final boolean complete = imageCheckList.getCheckedOnFullLocationsSize() == 0;
        if (complete == false)
        {
            final String message = imageCheckList.getIncompleteDataSetErrorMessage(dataSetFileName);
            operationLog.warn(message);
            if (mailClient != null && notifyIfPlateIncomplete)
            {
                Experiment experiment = dataSetInformation.tryGetExperiment();
                assert experiment != null : "dataset not connected to an experiment: "
                        + dataSetInformation;
                String email = null;
                if (experiment.getRegistrator() != null)
                {
                    email = experiment.getRegistrator().getEmail();
                }
                if (StringUtils.isBlank(email) == false)
                {
                    try
                    {
                        mailClient.sendMessage("Incomplete data set '" + dataSetFileName + "'",
                                message, null, null, email);
                    } catch (final EnvironmentFailureException e)
                    {
                        notificationLog.error("Couldn't send the following e-mail to '" + email
                                + "': " + message, e);
                    }
                } else
                {
                    notificationLog.error("Unspecified e-mail address of experiment registrator "
                            + experiment.getRegistrator());
                }
            }
        }
        return complete;
    }

    private PlateDimension getPlateGeometry()
    {
        return HCSContainerDatasetInfo.getPlateGeometry(dataSetInformation);
    }
}
