/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.FileOperations;

/**
 * Utility class for image file operations.
 * 
 * @author Izabela Adamczyk
 */
public class ImageFileExtractorUtils
{

    public static final String[] IMAGE_EXTENSIONS = new String[]
    { "tif", "tiff", "jpg", "jpeg", "gif", "png" };

    public static List<File> listImageFiles(final File directory)
    {
        return FileOperations.getInstance().listFiles(directory, IMAGE_EXTENSIONS, true);
    }

}
