/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.io.File;

/**
 * A listener for progress in file upload / download.
 *
 * @author Franz-Josef Elmer
 */
public interface IProgressListener
{
    public void start(File file, String operationName, long fileSize, Long fileIdOrNull);

    public void reportProgress(int percentage, long numberOfBytes);

    public void finished(boolean successful);

    public void warningOccured(String warningMessage);

    public void exceptionOccured(Throwable throwable);
}
