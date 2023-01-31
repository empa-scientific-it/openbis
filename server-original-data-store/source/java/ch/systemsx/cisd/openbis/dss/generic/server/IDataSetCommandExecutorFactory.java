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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;

/**
 * Factory of {@link IDataSetCommandExecutor} operating on a specified data store.
 * 
 * @author Franz-Josef Elmer
 */
interface IDataSetCommandExecutorFactory
{
    /**
     * Creates command executor for the specified data store. The file of the command queue will be located in <var>commandQueueDir</var> or
     * <var>commandQueueDir-nameOrNull</var> if <var>nameOrNull</var> isn't an empty string.
     */
    IDataSetCommandExecutor create(File store, File commandQueueDir, String nameOrNull);
}
