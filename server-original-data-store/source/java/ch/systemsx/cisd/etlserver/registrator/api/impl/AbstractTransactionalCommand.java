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

package ch.systemsx.cisd.etlserver.registrator.api.impl;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;

/**
 * Package-internal class to track and execute progress in the transaction.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractTransactionalCommand implements ITransactionalCommand
{
    private static final long serialVersionUID = 1L;

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractTransactionalCommand.class);

    public static Logger getOperationLog()
    {
        return operationLog;
    }

}
