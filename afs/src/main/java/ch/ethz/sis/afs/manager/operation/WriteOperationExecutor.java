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

package ch.ethz.sis.afs.manager.operation;

import ch.ethz.sis.shared.io.File;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.afs.dto.Transaction;
import ch.ethz.sis.afs.dto.operation.OperationName;
import ch.ethz.sis.afs.dto.operation.WriteOperation;
import ch.ethz.sis.afs.exception.AFSExceptions;

import java.util.Arrays;

import static ch.ethz.sis.afs.exception.AFSExceptions.MD5NotMatch;
import static ch.ethz.sis.afs.exception.AFSExceptions.PathIsDirectory;

public class WriteOperationExecutor implements OperationExecutor<WriteOperation> {

    //
    // Singleton
    //

    private static final WriteOperationExecutor instance;

    static {
        instance = new WriteOperationExecutor();
    }

    private WriteOperationExecutor() {
    }

    public static WriteOperationExecutor getInstance() {
        return instance;
    }

    //
    // Operation
    //


    @Override
    public boolean prepare(Transaction transaction, WriteOperation operation) throws Exception {
        // 1. Check that if the file exists, is not a directory
        boolean exists = IOUtils.exists(operation.getSource());
        if (exists) {
            File existingFile = IOUtils.getFile(operation.getSource());
            if (existingFile.getDirectory()) {
                AFSExceptions.throwInstance(PathIsDirectory, OperationName.Write.name(), operation.getSource());
            }
        }

        // 1. Validate new data
        byte[] md5Hash = IOUtils.getMD5(operation.getData());
        if (!Arrays.equals(md5Hash, operation.getMd5Hash())) {
            AFSExceptions.throwInstance(MD5NotMatch, OperationName.Write.name(), operation.getSource());
        }
        // 2. Create temporal file if has not been created already
        String tempFilePath = OperationExecutor.getTempPath(transaction, operation.getSource());
        if (!IOUtils.exists(tempFilePath)) {
            IOUtils.createDirectories(IOUtils.getParentPath(tempFilePath));
            if (!exists) {
                IOUtils.createFile(tempFilePath);
            } else {
                IOUtils.copy(operation.getSource(), tempFilePath);
            }
        }

        // 3. Flush bytes
        IOUtils.write(tempFilePath, operation.getOffset(), operation.getData());
        return true;
    }

    @Override
    public boolean commit(Transaction transaction, WriteOperation operation) throws Exception {
        String tempFilePath = OperationExecutor.getTempPath(transaction, operation.getSource());
        if (!IOUtils.exists(operation.getSource())) {
            IOUtils.createDirectories(IOUtils.getParentPath(operation.getSource()));
        }
        if (IOUtils.exists(tempFilePath)) {
            IOUtils.move(tempFilePath, operation.getSource());
        }
        return true;
    }
}
