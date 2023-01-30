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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataTransactionErrors.TransactionError;

/**
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationException extends HighLevelException
{

    private static final long serialVersionUID = 1L;

    private final List<MasterDataTransactionErrors> transactionErrors;

    public MasterDataRegistrationException(String message,
            List<MasterDataTransactionErrors> transactionErrors)
    {
        super(message);
        this.transactionErrors = transactionErrors;
    }

    public List<MasterDataTransactionErrors> getTransactionErrors()
    {
        return transactionErrors;
    }

    /**
     * Logs the accumulated errors.
     */
    public void logErrors(ISimpleLogger errorLogger)
    {
        for (MasterDataTransactionErrors errors : getTransactionErrors())
        {
            for (TransactionError error : errors.getErrors())
            {
                errorLogger.log(LogLevel.ERROR, error.getDescription());
            }
        }
    }

}
