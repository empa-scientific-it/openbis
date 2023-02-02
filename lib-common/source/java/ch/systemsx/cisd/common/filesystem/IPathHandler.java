/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

/**
 * A role for handling paths. The paths are supposed to go away when they have been handled successfully.
 * 
 * @see IStoreHandler
 * @author Bernd Rinn
 */
public interface IPathHandler extends IStopSignaler
{
    /**
     * Handles the <var>path</var>. Successful handling is indicated by <var>path</var> being gone when the method returns.
     */
    public void handle(File path);
}