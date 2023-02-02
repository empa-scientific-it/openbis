/*
 * Copyright 2012 ETH Zuerich, CISD
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

/**
 * @author pkupczyk
 */
public class ImageResolution implements Serializable, Comparable<ImageResolution>
{

    private static final long serialVersionUID = 1L;

    private int width;

    private int height;

    private boolean isOriginal;

    // GWT only
    @SuppressWarnings("unused")
    private ImageResolution()
    {
    }

    public ImageResolution(int width, int height, boolean isOriginal)
    {
        this.width = width;
        this.height = height;
        this.isOriginal = isOriginal;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean isOriginal()
    {
        return isOriginal;
    }

    @Override
    public int compareTo(ImageResolution o)
    {
        if (this == o)
        {
            return 0;
        }
        if (width < o.width)
        {
            return -1;
        } else if (width > o.width)
        {
            return 1;
        } else
        {
            if (height < o.height)
            {
                return -1;
            } else if (height > o.height)
            {
                return 1;
            } else
            {
                return 0;
            }
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImageResolution other = (ImageResolution) obj;
        if (height != other.height)
            return false;
        if (width != other.width)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return width + "x" + height;
    }

}
