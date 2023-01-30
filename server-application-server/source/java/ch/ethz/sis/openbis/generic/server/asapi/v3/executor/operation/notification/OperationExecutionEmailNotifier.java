/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionEmailNotifier implements IOperationExecutionEmailNotifier
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationExecutionEmailNotifier.class);

    @Resource(name = ResourceNames.MAIL_CLIENT_PARAMETERS)
    private MailClientParameters mailClientParameters;

    private IMailClient mailClient;

    public OperationExecutionEmailNotifier()
    {
    }

    OperationExecutionEmailNotifier(IMailClient mailClient)
    {
        this.mailClient = mailClient;
    }

    @PostConstruct
    private void init()
    {
        this.mailClient = new MailClient(mailClientParameters);
    }

    @Override
    public void executionFinished(String code, String description, List<String> operations, List<String> results,
            OperationExecutionEmailNotification notification)
    {
        EMailAddress[] emailAddresses = getEmailAddresses(notification);

        if (emailAddresses.length > 0)
        {
            StringBuilder content = new StringBuilder();

            appendCodeAndDescription(content, code, description);
            appendOperationsAndResults(content, operations, results, true, true);

            mailClient.sendEmailMessage("Operation execution " + code + " finished", content.toString(), null, null, emailAddresses);

            operationLog.info("Send an email to " + Arrays.toString(emailAddresses) + " to notify that execution " + code + " finised");
        }
    }

    @Override
    public void executionFailed(String code, String description, List<String> operations, String error,
            OperationExecutionEmailNotification notification)
    {
        EMailAddress[] emailAddresses = getEmailAddresses(notification);

        if (emailAddresses.length > 0)
        {
            StringBuilder content = new StringBuilder();

            appendCodeAndDescription(content, code, description);
            appendOperationsAndResults(content, operations, null, true, false);
            appendError(content, error);

            mailClient.sendEmailMessage("Operation execution " + code + " failed", content.toString(), null, null, emailAddresses);

            operationLog.info("Send an email to " + Arrays.toString(emailAddresses) + " to notify that execution " + code + " failed");
        }
    }

    private EMailAddress[] getEmailAddresses(OperationExecutionEmailNotification notification)
    {
        List<EMailAddress> emailAddresses = new ArrayList<EMailAddress>();

        if (notification.getEmails() != null)
        {
            for (String email : notification.getEmails())
            {
                if (email != null && false == email.trim().isEmpty())
                {
                    emailAddresses.add(new EMailAddress(email));
                }
            }
        }

        return emailAddresses.toArray(new EMailAddress[] {});
    }

    private void appendCodeAndDescription(StringBuilder content, String code, String description)
    {
        content.append("Execution: " + code);
        if (description != null && description.trim().length() > 0)
        {
            content.append("\nDescription: " + description);
        }
    }

    private void appendOperationsAndResults(StringBuilder content, List<String> operations, List<String> results, boolean appendOperations,
            boolean appendResults)
    {
        List<String> notNullOperations = operations != null ? operations : Collections.<String> singletonList(null);
        List<String> notNullResults = results != null ? results : Collections.<String> singletonList(null);

        Iterator<String> iterOperations = notNullOperations.iterator();
        Iterator<String> iterResults = notNullResults.iterator();

        boolean manyOperationsOrResults = notNullOperations.size() > 1 || notNullResults.size() > 1;
        int index = 0;

        if (manyOperationsOrResults)
        {
            content.append("\n");
        }

        while (iterOperations.hasNext() || iterResults.hasNext())
        {
            String operation = iterOperations.hasNext() ? iterOperations.next() : null;
            String result = iterResults.hasNext() ? iterResults.next() : null;

            if (appendOperations)
            {
                content.append("\nOperation");
                if (manyOperationsOrResults)
                {
                    content.append(" " + (index + 1));
                }
                content.append(": " + operation);
            }

            if (appendResults)
            {
                content.append("\nResult");
                if (manyOperationsOrResults)
                {
                    content.append(" " + (index + 1));
                }
                content.append(": " + result);
            }

            if (appendOperations && appendResults && (iterOperations.hasNext() || iterResults.hasNext()))
            {
                content.append("\n");
            }

            index++;
        }

        if (manyOperationsOrResults)
        {
            content.append("\n");
        }
    }

    private void appendError(StringBuilder content, String error)
    {
        content.append("\nError: " + error);
    }

}
