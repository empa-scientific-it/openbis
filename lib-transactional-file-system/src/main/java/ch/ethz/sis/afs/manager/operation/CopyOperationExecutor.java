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

import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.CopyOperation;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.exception.AFSExceptions;

import static ch.ethz.sis.afs.exception.AFSExceptions.PathInStore;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathNotInStore;

public class CopyOperationExecutor implements OperationExecutor<CopyOperation> {

    //
    // Singleton
    //

    private static final CopyOperationExecutor instance;

    static {
        instance = new CopyOperationExecutor();
    }

    private CopyOperationExecutor() {
    }

    public static CopyOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //

    @Override
    public boolean prepare(Transaction transaction, CopyOperation operation) throws Exception {
        if (!IOUtils.exists(operation.getSource())) {
            AFSExceptions.throwInstance(PathNotInStore, OperationName.Copy.name(), operation.getSource());
        }
        if (IOUtils.exists(operation.getTarget())) {
            AFSExceptions.throwInstance(PathInStore, OperationName.Copy.name(), operation.getTarget());
        }
        String tempFileParent = IOUtils.getParentPath(OperationExecutor.getTempPath(transaction, operation.getTarget()));
        if (!IOUtils.exists(tempFileParent)) {
            IOUtils.createDirectories(tempFileParent);
        }
        IOUtils.copy(operation.getSource(), OperationExecutor.getTempPath(transaction, operation.getTarget()));
        return true;
    }

    @Override
    public boolean commit(Transaction transaction, CopyOperation operation) throws Exception {
        String tempFilePath = OperationExecutor.getTempPath(transaction, operation.getTarget());
        if (IOUtils.exists(tempFilePath)) {
            String targetFileParent = IOUtils.getParentPath(operation.getTarget());
            if (!IOUtils.exists(targetFileParent)) {
                IOUtils.createDirectories(targetFileParent);
            }
            IOUtils.move(tempFilePath, operation.getTarget());
        }
        return true;
    }
}
