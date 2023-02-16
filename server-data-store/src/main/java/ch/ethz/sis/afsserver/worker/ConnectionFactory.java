/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.worker;

import ch.ethz.sis.afs.manager.TransactionManager;
import ch.ethz.sis.afs.startup.AtomicFileSystemParameter;
import ch.ethz.sis.shared.pool.AbstractFactory;
import ch.ethz.sis.shared.startup.Configuration;
import ch.ethz.sis.afs.manager.TransactionConnection;

public class ConnectionFactory extends AbstractFactory<Configuration, Configuration, TransactionConnection> {

    private TransactionManager transactionManager;

    @Override
    public void init(Configuration configuration) throws Exception {
        transactionManager = new TransactionManager(
                configuration.getSharableInstance(AtomicFileSystemParameter.jsonObjectMapperClass),
                configuration.getStringProperty(AtomicFileSystemParameter.writeAheadLogRoot),
                configuration.getStringProperty(AtomicFileSystemParameter.storageRoot));
        transactionManager.reCommitTransactionsAfterCrash();
    }

    @Override
    public TransactionConnection create(Configuration configuration) throws Exception {
        return transactionManager.getTransactionConnection();
    }
}
