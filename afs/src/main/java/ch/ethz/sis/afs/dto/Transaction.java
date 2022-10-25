/*
 * Copyright 2022 ETH Zürich, SIS
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

package ch.ethz.sis.afs.dto;

import ch.ethz.sis.afs.dto.operation.Operation;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/*
 * Transactions are modeled as dtos and are serialized to disk on the commit step
 * before finally executing with the intention they can be replayed in case of system failure.
 */
@Value
public class Transaction {

    /*
     * Base directory where the commitLog directory will be written
     */
    private String writeAheadLogRoot;

    /*
     * Base directory where the final storage is
     */
    private String storageRoot;

    /*
     * Identifier of the transaction, will be used as directory name on the commitLog
     */
    private UUID uuid;


    private List<Operation> operations;
}
