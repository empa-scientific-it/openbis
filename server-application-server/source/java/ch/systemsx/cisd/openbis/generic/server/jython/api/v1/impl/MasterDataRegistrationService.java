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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationService;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;

/**
 * @author Kaloyan Enimanev
 */
public class MasterDataRegistrationService implements IMasterDataRegistrationService
{

    private final EncapsulatedCommonServer commonServer;

    private final List<MasterDataRegistrationTransaction> createdTransactions =
            new ArrayList<MasterDataRegistrationTransaction>();

    public MasterDataRegistrationService(EncapsulatedCommonServer commonServer)
    {
        this.commonServer = commonServer;
    }

    @Override
    public IMasterDataRegistrationTransaction transaction()
    {
        MasterDataRegistrationTransaction transaction =
                new MasterDataRegistrationTransaction(commonServer);
        createdTransactions.add(transaction);
        return transaction;
    }

    void commit() throws MasterDataRegistrationException
    {
        List<MasterDataTransactionErrors> transactionErrors =
                new ArrayList<MasterDataTransactionErrors>();
        for (MasterDataRegistrationTransaction transaction : createdTransactions)
        {
            transaction.commit();
            if (transaction.hasErrors())
            {
                transactionErrors.add(transaction.getTransactionErrors());
            }
        }

        if (false == transactionErrors.isEmpty())
        {
            throw new MasterDataRegistrationException(
                    "Some of the executed transactions have failed to commit", transactionErrors);
        }
    }
}
