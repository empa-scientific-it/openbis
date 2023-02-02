/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;

import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A role that can do conversion operations on a file. Call {@link ISelfTestable#check()} to see whether the conversion operation is available on this
 * system.
 * 
 * @author Bernd Rinn
 */
public interface IFileConversionMethod extends ISelfTestable
{
    /**
     * Returns <code>true</code>, if this conversion method is available.
     */
    public boolean isAvailable();

    /**
     * Performs the file conversion on <var>inFile</var>, creating a file <var>outFile</var> in the process.
     * 
     * @return <code>true</code>, if the conversion has been performed successful.
     */
    public boolean convert(File inFile, File outFile);

}
