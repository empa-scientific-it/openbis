/*
 * Copyright 2008 ETH Zuerich, CISD
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
 * A role that can remove a file or directory.
 * 
 * @author Bernd Rinn
 */
public interface IFileRemover
{
    /**
     * Removes the given <var>fileToRemove</var>.
     * 
     * @param fileToRemove File or directory to remove. If it is a directory, it will be removed recursively.
     * @return <code>true</code> if the file or directory was removed successfully and <code>false</code> otherwise.
     */
    public boolean removeRecursively(File fileToRemove);
}