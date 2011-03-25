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

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * A factory for image readers.
 * <p>
 * Uses {@link ServiceLoader}s underneath to find out about the available libraries and readers.
 * 
 * @author Bernd Rinn
 * @author Kaloyan Enimanev
 */
public class ImageReaderFactory
{

    private static final ServiceLoader<IImageReaderLibrary> libraryServiceLoader = ServiceLoader
            .load(IImageReaderLibrary.class);

    /**
     * Returns an {@link IImageReader} for specified library name and reader name. Can return
     * <code>null</code> if no matching reader is found.
     */
    public static IImageReader tryGetReader(String libraryName, String readerName)
    {
        IImageReaderLibrary library = tryGetLibrary(libraryName);
        return (library == null) ? null : library.tryGetReader(readerName);
    }

    /**
     * Tries to find a suitable reader in a library for a specified <var>fileName</var>. May return
     * <code>null</code> if no suitable reader is found.
     * <p>
     * The behavior of this method may vary across libraries. For example, some image libraries can
     * use the suffix of <var>fileName</var> to find the right reader, while others might attempt to
     * open the file and apply heuristics on its content to determine the appropriate reader.
     */
    public static IImageReader tryGetImageReaderForFile(String libraryName, String fileName)
    {
        IImageReaderLibrary library = tryGetLibrary(libraryName);
        return (library == null) ? null : library.tryGetReaderForFile(fileName);
    }

    private static IImageReaderLibrary tryGetLibrary(String libraryName)
            throws IllegalArgumentException
    {
        Iterator<IImageReaderLibrary> iterator = libraryServiceLoader.iterator();
        while (iterator.hasNext())
        {
            IImageReaderLibrary library = iterator.next();
            if (library.getName().equals(libraryName))
            {
                return library;
            }
        }
        return null;
    }

}
