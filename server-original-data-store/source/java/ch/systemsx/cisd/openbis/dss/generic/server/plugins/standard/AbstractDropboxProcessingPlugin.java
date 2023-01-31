/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * The base class for processing plugins that employ a {@link IPostRegistrationDatasetHandler}.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDropboxProcessingPlugin extends AbstractDatastorePlugin implements
        IProcessingPluginTask
{

    private static final long serialVersionUID = 1L;

    @Private
    static final String SEND_DETAILED_EMAIL_KEY = "send-detailed-email";

    private final static String MISSING_DIRECTORY_MSG = "with missing directory";

    private final static String EMPTY_DIRECTORY_MSG = "with empty directory";

    private final static String MORE_THAN_ONE_ITEM_MSG = "with more than one item in the directory";

    private final IPostRegistrationDatasetHandler dropboxHandler;

    private final ITimeProvider timeProvider;

    private final boolean sendingDetailedEMail;

    /**
     * Note that this class is not a valid processing plugin as it does not provide the appropriate constructor.
     */
    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler)
    {
        this(properties, storeRoot, dropboxHandler, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler, ITimeProvider timeProvider)
    {
        super(properties, storeRoot);
        this.dropboxHandler = dropboxHandler;
        this.timeProvider = timeProvider;
        sendingDetailedEMail = PropertyUtils.getBoolean(properties, SEND_DETAILED_EMAIL_KEY, false);
    }

    @Override
    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            long startTime = timeProvider.getTimeInMilliseconds();
            Status status = processDataset(dataset, context);
            if (sendingDetailedEMail)
            {
                sendDetailedEMail(startTime, dataset, status, context);
            }
            result.addDatasetStatus(dataset, status);
        }
        return result;
    }

    private void sendDetailedEMail(long startTime, DatasetDescription dataSetDescription,
            Status status, DataSetProcessingContext context)
    {
        String dataSet =
                render(dataSetDescription.getDataSetCode(), dataSetDescription.getDataSetTypeCode());
        String experiment =
                render(dataSetDescription.getExperimentIdentifier(),
                        dataSetDescription.getExperimentTypeCode());
        String sampleOrNull =
                render(dataSetDescription.getSampleIdentifier(),
                        dataSetDescription.getSampleTypeCode());
        boolean withSample = sampleOrNull != null;
        boolean withExperiment = experiment != null;
        boolean processingFailed = status.isError();
        String processingDescription = getProcessingDescription(dataSetDescription, context);
        Template template = getEMailMessageTemplate(processingFailed, withSample, withExperiment).createFreshCopy();
        String subject;
        if (processingFailed)
        {
            template.bind("error", status.tryGetErrorMessage());
            subject = "Processing of data set " + dataSet + " failed";
        } else
        {
            subject = "Data set " + dataSet + " successfully processed";
        }
        template.bind("processing-description", processingDescription);
        template.bind("data-set", dataSet);
        if (withExperiment)
        {
            template.bind("experiment", experiment);
        }
        if (withSample)
        {
            template.bind("sample", sampleOrNull);
        }
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN);
        template.bind("start-time", dateFormat.format(new Date(startTime)));
        template.bind("end-time", dateFormat.format(new Date(timeProvider.getTimeInMilliseconds())));
        IMailClient mailClient = context.getMailClient();
        EMailAddress eMailAddress = new EMailAddress(context.getUserEmailOrNull());
        mailClient.sendEmailMessage(subject, template.createText(), null, null, eMailAddress);
    }

    private Template getEMailMessageTemplate(boolean withError, boolean withSample, boolean withExperiment)
    {
        return new Template(
                (withError ? "Processing of data set ${data-set} failed.\nReason: ${error}"
                        : "Successfully processed data set ${data-set}.")
                        + "\n\nProcessing details:\n"
                        + "Description: ${processing-description}\n"
                        + (withExperiment ? "Experiment: ${experiment}\n" : "")
                        + (withSample ? "Sample: ${sample}\n" : "")
                        + "Started: ${start-time}.\n" + "Finished: ${end-time}.");
    }

    /**
     * Returns a description to be used in e-mails.
     */
    protected abstract String getProcessingDescription(DatasetDescription dataset, DataSetProcessingContext context);

    private String render(String entity, String entityType)
    {
        return entity == null ? null : entity + " [" + entityType + "]";
    }

    private Status processDataset(DatasetDescription dataset, DataSetProcessingContext context)
    {
        File originalDir = getDataSubDir(context.getDirectoryProvider(), dataset);
        if (originalDir.isDirectory() == false)
        {
            operationLog
                    .warn("Dataset directory does not exist and will be silently excluded from the processing: "
                            + originalDir.getPath());
            return Status.createError(MISSING_DIRECTORY_MSG);
        }
        File[] datasetFiles = FileUtilities.listFiles(originalDir);
        if (datasetFiles.length == 1)
        {
            DataSetInformation datasetInfo = createDatasetInfo(dataset);
            return dropboxHandler.handle(datasetFiles[0], datasetInfo,
                    context.getParameterBindings());
        } else
        {
            operationLog.error(String.format("Exactly one item was expected in the '%s' directory,"
                    + " but %d have been found. Nothing will be processed.",
                    originalDir.getParent(), datasetFiles));
            final String errorMsg =
                    datasetFiles.length > 1 ? MORE_THAN_ONE_ITEM_MSG : EMPTY_DIRECTORY_MSG;
            return Status.createError(errorMsg);
        }
    }

    private DataSetInformation createDatasetInfo(DatasetDescription dataset)
    {
        String datasetTypeCode = dataset.getDataSetTypeCode();
        String spaceCode = dataset.getSpaceCode();
        String projectCode = dataset.getProjectCode();
        String sampleCode = dataset.getSampleCode();
        String experimentCode = dataset.getExperimentCode();
        DataSetInformation datasetInfo = new DataSetInformation();
        datasetInfo.setDataSetType(new DataSetType(datasetTypeCode));
        datasetInfo.setDataSetKind(DataSetKind.PHYSICAL);
        datasetInfo.setSampleCode(sampleCode);
        datasetInfo.setProjectCode(projectCode);
        datasetInfo.setSpaceCode(spaceCode);
        datasetInfo.setDataSetCode(dataset.getDataSetCode());
        if (experimentCode != null)
        {
            ExperimentIdentifier expIdent = new ExperimentIdentifier(spaceCode, projectCode, experimentCode);
            datasetInfo.setExperimentIdentifier(expIdent);
        }
        if (sampleCode != null)
        {
            SampleIdentifier sampleIdentifier;
            if (projectCode != null)
            {
                ProjectIdentifier projectIdentifier = new ProjectIdentifier(spaceCode, projectCode);
                sampleIdentifier = new SampleIdentifier(projectIdentifier, sampleCode);
            } else
            {
                SpaceIdentifier spaceIdentifier = new SpaceIdentifier(spaceCode);
                sampleIdentifier = new SampleIdentifier(spaceIdentifier, sampleCode);
            }
            datasetInfo.setSampleIdentifier(sampleIdentifier);
        }
        return datasetInfo;
    }

}
