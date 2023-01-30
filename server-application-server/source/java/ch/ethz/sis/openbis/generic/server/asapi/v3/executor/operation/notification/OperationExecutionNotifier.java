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

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionNotification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecutionEmailNotification;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionNotifier implements IOperationExecutionNotifier
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationExecutionNotifier.class);

    @Autowired
    private IOperationExecutionEmailNotifier emailNotifier;

    public OperationExecutionNotifier()
    {
    }

    OperationExecutionNotifier(IOperationExecutionEmailNotifier emailNotifier)
    {
        this.emailNotifier = emailNotifier;
    }

    @Override
    public void executionNew(String code, IOperationExecutionNotification notification)
    {
        if (notification == null)
        {
            return;
        }

        // make sure it is a supported notification before we start the execution
        if (false == notification instanceof OperationExecutionEmailNotification)
        {
            throw new UnsupportedNotificationException(code, notification);
        }
    }

    @Override
    public void executionFinished(String code, String description, List<String> operations, List<String> results,
            IOperationExecutionNotification notification)
    {
        if (notification == null)
        {
            return;
        }

        try
        {
            if (notification instanceof OperationExecutionEmailNotification)
            {
                emailNotifier.executionFinished(code, description, operations, results, (OperationExecutionEmailNotification) notification);
            } else
            {
                // should not normally happen, still somebody can modify the database by hand
                throw new UnsupportedNotificationException(code, notification);
            }
        } catch (Exception e)
        {
            // do not throw an exception - we don't want to make the execution fail
            operationLog.warn("Couldn't notify about a finished execution", e);
        }
    }

    @Override
    public void executionFailed(String code, String description, List<String> operations, String error, IOperationExecutionNotification notification)
    {
        if (notification == null)
        {
            return;
        }

        try
        {
            if (notification instanceof OperationExecutionEmailNotification)
            {
                emailNotifier.executionFailed(code, description, operations, error, (OperationExecutionEmailNotification) notification);
            } else
            {
                // should not normally happen, still somebody can modify the database by hand
                throw new UnsupportedNotificationException(code, notification);
            }
        } catch (Exception e)
        {
            // do not throw an exception - we don't want to make the execution fail
            operationLog.warn("Couldn't notify about a failed execution", e);
        }
    }

    private class UnsupportedNotificationException extends IllegalArgumentException
    {

        private static final long serialVersionUID = 1L;

        public UnsupportedNotificationException(String code, IOperationExecutionNotification notification)
        {
            super("Unsupported notification " + notification.getClass().getName() + " found for operation execution with id " + code);
        }

    }

}
