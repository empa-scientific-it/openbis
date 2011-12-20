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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Criterion based on the file type.
 *
 * @author Franz-Josef Elmer
 */
public class FileTypeCriterion extends AbstractImageSelectionCriterion
{
    private final Set<String> fileTypes;

    /**
     * Creates an instance which accepts all images of specified file types.
     */
    public FileTypeCriterion(String... fileTypes)
    {
        this.fileTypes = new HashSet<String>(Arrays.asList(fileTypes));
    }

    @Override
    protected boolean accept(IImageSetMetaData imageMetaData)
    {
        return fileTypes.contains(imageMetaData.getFileType());
    }

}
