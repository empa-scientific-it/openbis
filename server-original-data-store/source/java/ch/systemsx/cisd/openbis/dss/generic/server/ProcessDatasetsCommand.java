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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Command which processes datasets using the specified plugin instance. This is essentially an adapter to {@link IProcessingPluginTask}.
 * 
 * @author Tomasz Pylak
 */
public class ProcessDatasetsCommand extends AbstractDataSetDescriptionBasedCommand
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ProcessDatasetsCommand.class);

    private static final long serialVersionUID = 1L;

    private final IProcessingPluginTask task;

    private final Map<String, String> parameterBindings;

    private final String userEmailOrNull;

    private final String sessionTokenOrNull;

    private final DatastoreServiceDescription serviceDescription;

    private MailClientParameters mailClientParameters;

    private transient IMailClient mailClient;

    private String userId;

    public ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userId, String userEmailOrNull,
            String sessionTokenOrNull, DatastoreServiceDescription serviceDescription,
            MailClientParameters mailClientParameters)
    {
        this(task, datasets, parameterBindings, userId, userEmailOrNull, sessionTokenOrNull,
                serviceDescription, new MailClient(mailClientParameters));
        this.mailClientParameters = mailClientParameters;
    }

    ProcessDatasetsCommand(IProcessingPluginTask task, List<DatasetDescription> datasets,
            Map<String, String> parameterBindings, String userId, String userEmailOrNull,
            String sessionTokenOrNull, DatastoreServiceDescription serviceDescription,
            IMailClient mailClient)
    {
        super(datasets);
        this.task = task;
        this.parameterBindings = parameterBindings;
        this.userId = userId;
        this.userEmailOrNull = userEmailOrNull;
        this.sessionTokenOrNull = sessionTokenOrNull;
        this.serviceDescription = serviceDescription;
        this.mailClient = mailClient;
    }

    @Private
    // For unit tests
    static final class ProxyMailClient implements IMailClient
    {
        private final IMailClient mailClient;

        private boolean mailSent;

        ProxyMailClient(IMailClient mailClient)
        {
            this.mailClient = mailClient;
        }

        boolean hasMailSent()
        {
            return mailSent;
        }

        @Override
        public void sendMessage(String subject, String content, String replyToOrNull,
                From fromOrNull, String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessage(subject, content, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        @Override
        public void sendEmailMessage(String subject, String content, EMailAddress replyToOrNull,
                EMailAddress fromOrNull, EMailAddress... recipients)
                throws EnvironmentFailureException
        {
            mailClient.sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        @Override
        @SuppressWarnings("deprecation")
        public void sendMessageWithAttachment(String subject, String content, String filename,
                DataHandler attachmentContent, String replyToOrNull, From fromOrNull,
                String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessageWithAttachment(subject, content, filename, attachmentContent,
                    replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        @Override
        public void sendEmailMessageWithAttachment(String subject, String content, String filename,
                DataHandler attachmentContent, EMailAddress replyToOrNull, EMailAddress fromOrNull,
                EMailAddress... recipients) throws EnvironmentFailureException
        {
            mailClient.sendEmailMessageWithAttachment(subject, content, filename,
                    attachmentContent, replyToOrNull, fromOrNull, recipients);
            mailSent = true;
        }

        @Override
        public void sendTestEmail()
        {
            mailClient.sendTestEmail();
        }

    }

    @Override
    public void execute(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider dataSetDirectoryProvider)
    {
        String errorMessageOrNull = null;
        ProcessingStatus processingStatusOrNull = null;
        ProxyMailClient proxyMailClient = new ProxyMailClient(getMailClient());
        try
        {
            DataSetProcessingContext context =
                    createDataSetProcessingContext(contentProvider, dataSetDirectoryProvider,
                            proxyMailClient);
            processingStatusOrNull = task.process(dataSets, context);
        } catch (RuntimeException e)
        {
            // exception message should be readable for users
            errorMessageOrNull = e.getMessage() == null ? "" : e.getMessage();
            throw e;
        } finally
        {
            if (StringUtils.isBlank(userEmailOrNull))
            {
                if (errorMessageOrNull != null)
                {
                    String warning =
                            String.format(
                                    "Dataset processing '%s' has failed with a message '%s' but"
                                            + " the person who triggered processing has no email address specified."
                                            + " No notification has been sent.",
                                    serviceDescription.getKey(), errorMessageOrNull);
                    operationLog.warn(warning);
                }
                return;
            }
            if (proxyMailClient.hasMailSent() == false || errorMessageOrNull != null)
            {
                createContentAndSendMessage(errorMessageOrNull, processingStatusOrNull);
            }
        }
    }

    /**
     * Can be overridden in unit tests to avoid trying to create the openBIS service.
     */
    DataSetProcessingContext createDataSetProcessingContext(
            IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider dataSetDirectoryProvider, ProxyMailClient proxyMailClient)
    {
        return new DataSetProcessingContext(contentProvider, dataSetDirectoryProvider,
                parameterBindings, proxyMailClient, userId, userEmailOrNull,
                sessionTokenOrNull);
    }

    private void createContentAndSendMessage(String errorMessageOrNull,
            ProcessingStatus processingStatusOrNull)
    {
        final StringBuilder contentBuilder = new StringBuilder();
        final String subject;
        if (errorMessageOrNull != null)
        {
            // create error message content
            subject = getShortDescription(" processing failed");
            contentBuilder.append(getDescription(subject));
            contentBuilder.append("\n\nError message:\n");
            contentBuilder.append(errorMessageOrNull);
        } else
        {
            // create success message content
            subject = getShortDescription(" processing finished");
            if (processingStatusOrNull != null)
            {
                if (isEmpty(processingStatusOrNull))
                {
                    return; // no message sent
                }
                contentBuilder.append(generateDescription(processingStatusOrNull));
            } else
            {
                contentBuilder.append(getDescription(subject));
            }
        }
        sendMessage(subject, contentBuilder.toString(), userEmailOrNull);
    }

    private void sendMessage(String subject, String content, String recipient)
    {
        getMailClient().sendMessage(subject, content, null, null, recipient);
    }

    private String getShortDescription(String suffix)
    {
        return String.format("'%s' [%s]%s", serviceDescription.getLabel(), serviceDescription.getKey(), suffix);
    }

    private String getDescription(String prefix)
    {
        return String.format("%s on %d data set(s): \n%s", prefix, dataSets.size(),
                CollectionUtils.abbreviate(getDataSetCodes(), 20));
    }

    @Override
    public String getDescription()
    {
        return getDescription(getShortDescription(""));
    }

    @Override
    public String getType()
    {
        return task.getClass().getSimpleName();
    }

    private static String getDataSetCodes(List<String> datasets)
    {
        if (datasets.isEmpty())
        {
            return "";
        } else
        {
            final StringBuilder sb = new StringBuilder();
            for (String dataset : datasets)
            {
                sb.append(dataset);
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    private static String generateDescription(ProcessingStatus processingStatus)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("This is an automatically generated report from the completed processing of data sets in openBIS.\n");
        List<String> successfullyProcessed = processingStatus.getDatasetsByStatus(Status.OK);
        if (successfullyProcessed != null && successfullyProcessed.isEmpty() == false)
        {
            sb.append("- number of successfully processed data sets: ");
            sb.append(successfullyProcessed.size());
            sb.append(". Datasets: ");
            sb.append(getDataSetCodes(successfullyProcessed));
            sb.append("\n");
        }
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        for (Status errorStatus : errorStatuses)
        {
            List<String> unsuccessfullyProcessed =
                    processingStatus.getDatasetsByStatus(errorStatus);
            sb.append("- processing of ");
            sb.append(unsuccessfullyProcessed.size());
            sb.append(" data set(s) failed because: ");
            sb.append(" " + errorStatus + ". Datasets: ");
            sb.append(getDataSetCodes(unsuccessfullyProcessed));
            sb.append("\n");
        }
        return sb.toString();
    }

    private static boolean isEmpty(ProcessingStatus processingStatus)
    {
        if (processingStatus.getDatasetsByStatus(Status.OK).isEmpty() == false)
        {
            return false;
        }
        List<Status> errorStatuses = processingStatus.getErrorStatuses();
        for (Status errorStatus : errorStatuses)
        {
            if (processingStatus.getDatasetsByStatus(errorStatus).isEmpty() == false)
            {
                return false;
            }
        }
        return true;
    }

    private IMailClient getMailClient()
    {
        if (mailClient == null)
        {
            mailClient = new MailClient(mailClientParameters);
        }
        return mailClient;
    }

    String getUserId()
    {
        return userId;
    }

    String tryGetUserEmail()
    {
        return userEmailOrNull;
    }

}
