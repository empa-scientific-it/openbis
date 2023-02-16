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
package ch.ethz.sis.afs.api;

import java.util.List;
import java.util.UUID;

public interface TwoPhaseTransactionAPI {

    //
    // 2PT API
    //

    void begin(UUID transactionId) throws Exception; // Starts or recovers an existing transaction

    Boolean prepare() throws Exception; // Prepares the transaction serializing it to disk, if is already prepared ignores the command

    void commit() throws Exception; // Commits the transaction, under any circumstances

    void rollback() throws Exception; // Rollback a begin or prepared transaction

    List<UUID> recover() throws Exception; // Returns the list of transactions on prepare state, used internally on crash recovery scenarios.

}