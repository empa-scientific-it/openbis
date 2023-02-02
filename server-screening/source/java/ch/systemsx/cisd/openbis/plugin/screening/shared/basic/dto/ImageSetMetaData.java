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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Meta data of common for all images of a set.
 *
 * @author Franz-Josef Elmer
 */
public class ImageSetMetaData implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long id;

    private boolean original;

    private int width;

    private int height;

    private Integer colorDepth;

    private String fileType;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean isOriginal()
    {
        return original;
    }

    public void setOriginal(boolean original)
    {
        this.original = original;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public Integer getColorDepth()
    {
        return colorDepth;
    }

    public void setColorDepth(Integer colorDepth)
    {
        this.colorDepth = colorDepth;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

}
