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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A file abstraction.
 * 
 * @author Franz-Josef Elmer
 */
public interface IFile extends ISelfTestable
{
    /**
     * Returns the absolute path of this file.
     */
    public String getAbsolutePath();

    /**
     * Copies the file denoted by this abstract pathname to given <var>destinationFile</var>.
     * <p>
     * Note that, depending on the implementation, it effectively copies this abstract pathname or makes an hard link of it.
     * </p>
     */
    public void copyTo(File destinationFile);

    /**
     * Copies given <code>sourceFile</code> to the file denoted by this abstract pathname.
     * <p>
     * Note that, depending on the implementation, it effectively copies the given <var>sourceFile</var> or makes an hard link of it.
     * </p>
     */
    public void copyFrom(File sourceFile);

    public byte[] read();

    public void write(byte[] data);

    public void delete();
}
