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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Width and height of an image.
 * 
 * @since 1.4
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("ImageSize")
public class ImageSize implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int width;

    private int height;

    /**
     * Creates an instance for specified width and height.
     */
    public ImageSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public final int getWidth()
    {
        return width;
    }

    public final int getHeight()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return width + "x" + height;
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
        ImageSize other = (ImageSize) obj;
        if (height != other.height)
            return false;
        if (width != other.width)
            return false;
        return true;
    }

    //
    // JSON-RPC
    //

    private ImageSize()
    {
    }

    private void setWidth(int width)
    {
        this.width = width;
    }

    private void setHeight(int height)
    {
        this.height = height;
    }

}
