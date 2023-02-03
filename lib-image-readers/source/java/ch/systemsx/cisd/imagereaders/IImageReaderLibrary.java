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

package ch.systemsx.cisd.imagereaders;

import java.util.List;

/**
 * Encapsulates a collection image readers coming from a third-party library.
 * 
 * @author Bernd Rinn
 */
public interface IImageReaderLibrary
{
    /**
     * Return library name.
     */
    public String getName();
    
    /**
     * Return a collection with the names of the image readers available in the library.
     */
    public List<String> getReaderNames();
    
    /**
     * Returns an {@link IImageReader} for a specified name. Can return <code>null</code> if no
     * reader with the specified name is available.
     */
    public IImageReader tryGetReader(String readerName);

    /**
     * Tries to find a suitable reader for a specified <var>fileName</var>. May return
     * <code>null</code> if no suitable reader is found.
     * <p>
     * The behavior of this method may vary across libraries. For example, some image libraries can
     * use the suffix of <var>fileName</var> to find the right reader, while others might attempt to
     * open the file and apply heuristics on its content to determine the appropriate reader.
     */
    public IImageReader tryGetReaderForFile(String fileName);
    
}
