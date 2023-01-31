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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.PathPrefixPrepender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Standard implementation of <code>IProcessor</code>.
 * 
 * @author Christian Ribeaud
 */
final class StandardProcessor implements IProcessor
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, StandardProcessor.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, StandardProcessor.class);

    private final IFileFactory fileFactory;

    private final PathPrefixPrepender pathPrefixPrepender;

    private final String parametersFileName;

    private final MessageFormat finishedFileFormat;

    private final StorageFormat inputDataFormat;

    private final String dataSetCodePrefixGlueCharacter;

    public StandardProcessor(final IFileFactory fileFactory, final StorageFormat inputDataFormat,
            final PathPrefixPrepender pathPrefixPrepender, final String parametersFileName,
            final String finishedFileNameTemplate, final String dataSetCodePrefixGlueCharacter)
    {
        assert fileFactory != null : "Unspecified IFileFactory.";
        assert inputDataFormat != null : "Unspecified StorageFormat.";
        assert pathPrefixPrepender != null : "Unspecified PathPrefixPrepender.";
        assert parametersFileName != null : "Unspecified parameters file name.";
        assert finishedFileNameTemplate != null : "Unspecified finished file name template.";
        assert dataSetCodePrefixGlueCharacter != null : "Unspecified data set code prefix glue character.";
        this.fileFactory = fileFactory;
        this.inputDataFormat = inputDataFormat;
        this.pathPrefixPrepender = pathPrefixPrepender;
        this.parametersFileName = parametersFileName;
        this.finishedFileFormat = new MessageFormat(finishedFileNameTemplate);
        this.dataSetCodePrefixGlueCharacter = dataSetCodePrefixGlueCharacter;
    }

    private void createDataSetForProcessing(final File dataSet, final IFile dataSetForProcessing)
    {
        final StopWatch watch = new StopWatch();
        watch.start();
        dataSetForProcessing.copyFrom(dataSet);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data set '" + dataSet.getName() + "' copied into '"
                    + dataSetForProcessing.getAbsolutePath() + "', took " + watch + ".");
        }
    }

    private void createProcessingParameters(final ProcessingInstructionDTO instruction,
            final IFile processingDirectory, final List<IFile> itemsToRemoveInCaseOfError)
    {
        final byte[] instructionDataOrNull = instruction.getParameters();
        if (instructionDataOrNull != null)
        {
            final IFile parametersFile =
                    fileFactory.create(processingDirectory, parametersFileName);
            itemsToRemoveInCaseOfError.add(parametersFile);
            parametersFile.write(instructionDataOrNull);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Processing parameters written into '"
                        + parametersFile.getAbsolutePath() + "'.");
            }
        }
    }

    private void createFinishedFile(final IFile processingDirectory, final String dataSetName)
    {
        final String finishedFileName = finishedFileFormat.format(new String[]
        { dataSetName });
        final IFile finishedFile = fileFactory.create(processingDirectory, finishedFileName);
        finishedFile.write(new byte[0]);
    }

    private final String getDataSetName(final DataSetInformation dataSetInformation,
            final File dataSet)
    {
        final String dataSetName = dataSet.getName();
        final String parentDataSetCode = dataSetInformation.getDataSetCode();
        if (StringUtils.isNotEmpty(parentDataSetCode))
        {
            return parentDataSetCode + dataSetCodePrefixGlueCharacter + dataSetName;
        }
        return dataSetName;
    }

    //
    // IProcessor
    //

    @Override
    public final StorageFormat getRequiredInputDataFormat()
    {
        return inputDataFormat;
    }

    @Override
    public final void initiateProcessing(final ProcessingInstructionDTO instruction,
            final DataSetInformation dataSetInformation, final File dataSet)
    {
        assert instruction != null : "Unspecified instruction.";
        assert dataSet != null : "Unspecified data set.";
        assert dataSetInformation != null : "Unspecified data set information.";
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Start initialization of processing.");
        }
        final String processingPath = pathPrefixPrepender.addPrefixTo(instruction.getPath());
        final IFile processingDirectory = fileFactory.create(processingPath);
        processingDirectory.check();
        final String dataSetName = getDataSetName(dataSetInformation, dataSet);
        final IFile dataSetForProcessing = fileFactory.create(processingDirectory, dataSetName);
        final List<IFile> itemsToRemoveInCaseOfError = new ArrayList<IFile>(2);
        itemsToRemoveInCaseOfError.add(dataSetForProcessing);
        try
        {
            InterruptedExceptionUnchecked.check();
            createDataSetForProcessing(dataSet, dataSetForProcessing);
            InterruptedExceptionUnchecked.check();
            createProcessingParameters(instruction, processingDirectory, itemsToRemoveInCaseOfError);
            InterruptedExceptionUnchecked.check();
            createFinishedFile(processingDirectory, dataSetName);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Processing initiated.");
            }
        } catch (final Exception ex)
        {
            for (final IFile item : itemsToRemoveInCaseOfError)
            {
                item.delete();
            }
            if (ex instanceof InterruptedExceptionUnchecked)
            {
                operationLog
                        .warn(String
                                .format(
                                        "Requested to stop initiation of processing, rolled back: [data set: '%s'].",
                                        dataSetForProcessing.getAbsolutePath()));
            } else
            {
                notificationLog.error(String.format(
                        "Error when initiating processing, rolled back: [data set: '%s'].",
                        dataSetForProcessing.getAbsolutePath()), ex);
            }
        }
    }
}