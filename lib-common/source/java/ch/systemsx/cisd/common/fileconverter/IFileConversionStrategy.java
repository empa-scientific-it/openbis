/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

/**
 * A strategy for file conversion operations.
 * 
 * @author Bernd Rinn
 */
public interface IFileConversionStrategy
{
    /**
     * Checks whether the <var>inFile</var> can be converted by this strategy.
     * 
     * @return The file that it should be converted into, or <code>null</code>, if <var>inFile</var> cannot be converted by this strategy.
     */
    public File tryCheckConvert(File inFile);

    /**
     * Returns <code>true</code>, if the original file should be deleted after successful conversion.
     */
    public boolean deleteOriginalFile();

    /**
     * Returns the actual conversion method.
     */
    public IFileConversionMethod getConverter();

}
