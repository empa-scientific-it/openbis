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
package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.MoveOperation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.shared.io.IOUtils;

public class MoveOperationExecutor implements OperationExecutor<MoveOperation> {

    //
    // Singleton
    //

    private static final MoveOperationExecutor instance;

    static {
        instance = new MoveOperationExecutor();
    }

    private MoveOperationExecutor() {
    }

    public static MoveOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //

    @Override
    public boolean prepare(Transaction transaction, MoveOperation operation) throws Exception {
        if (!IOUtils.exists(operation.getSource())) {
            AFSExceptions.throwInstance(AFSExceptions.PathNotInStore, OperationName.Move.name(), operation.getSource());
        }
        if (IOUtils.exists(operation.getTarget())) {
            AFSExceptions.throwInstance(AFSExceptions.PathInStore, OperationName.Move.name(), operation.getTarget());
        }
        return true;
    }

    @Override
    public boolean commit(Transaction transaction, MoveOperation operation) throws Exception {
        if (IOUtils.exists(operation.getSource())) {
            if (!IOUtils.exists(operation.getTarget())) {
                IOUtils.createDirectories(IOUtils.getParentPath(operation.getTarget()));
            }
            IOUtils.move(operation.getSource(), operation.getTarget());
        }
        return false;
    }
}
