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

/**
 * Interface to image meta data.
 *
 * @author Franz-Josef Elmer
 */
public interface IImageMetaData
{
    /**
     * Returns the unique id of this image meta data.
     */
    public long getId();
    
    /**
     * Returns <code>true</code> if these are the meta data of the original image.
     */
    public boolean isOriginal();
    
    /**
     * Returns the size of the image.
     */
    public Geometry getSize();
    
    /**
     * Returns the color depth if known.
     * 
     * @return <code>null</code> if color depth is unknown.
     */
    public Integer getColorDepth();
    
    /**
     * Returns the file type if known.
     * 
     * @return <code>null</code> if file type is unknown.
     */
    public String getFileType();
    
}
