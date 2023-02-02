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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Criterion based on the image size.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("SizeCriterion")
public class SizeCriterion implements IImageRepresentationFormatSelectionCriterion
{
    private static final long serialVersionUID = 1L;

    public static enum Type
    {
        /**
         * Picks that format where the image size is the largest one which just fits into a bounding box specified by width and height.
         */
        LARGEST_IN_BOUNDING_BOX()
        {
            @Override
            void filter(int width, int height, List<ImageRepresentationFormat> formats,
                    List<ImageRepresentationFormat> filteredFormats)
            {
                List<ImageRepresentationFormat> smallerFormats =
                        new ArrayList<ImageRepresentationFormat>();
                INSIDE_BOUNDING_BOX.filter(width, height, formats, smallerFormats);
                if (smallerFormats.isEmpty() == false)
                {
                    Collections.sort(smallerFormats, SIZE_COMPARATOR);
                    filteredFormats.add(smallerFormats.get(smallerFormats.size() - 1));
                }
            }
        },
        /**
         * Picks all formats where the image size is inside a bounding box specified by width and height.
         */
        INSIDE_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<ImageRepresentationFormat> formats,
                    List<ImageRepresentationFormat> filteredFormats)
            {
                for (ImageRepresentationFormat format : formats)
                {
                    if (getWidth(format) <= width && getHeight(format) <= height)
                    {
                        filteredFormats.add(format);
                    }
                }
            }
        },
        /**
         * Picks that format where the image size is the smallest one covering a bounding box specified by width and height.
         */
        SMALLEST_COVERING_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<ImageRepresentationFormat> formats,
                    List<ImageRepresentationFormat> filteredFormats)
            {
                List<ImageRepresentationFormat> largerFormats =
                        new ArrayList<ImageRepresentationFormat>();
                COVERING_BOUNDING_BOX.filter(width, height, formats, largerFormats);
                if (largerFormats.isEmpty() == false)
                {
                    Collections.sort(largerFormats, SIZE_COMPARATOR);
                    filteredFormats.add(largerFormats.get(0));
                }
            }
        },
        /**
         * Picks all formats where the image size covers a bounding box specified by width and height.
         */
        COVERING_BOUNDING_BOX
        {
            @Override
            void filter(int width, int height, List<ImageRepresentationFormat> formats,
                    List<ImageRepresentationFormat> filteredFormats)
            {
                for (ImageRepresentationFormat format : formats)
                {
                    if (getWidth(format) >= width && getHeight(format) >= height)
                    {
                        filteredFormats.add(format);
                    }
                }
            }
        },
        /**
         * Picks all formats where the image size is exactly as specified by width and height.
         */
        EXACTLY
        {
            @Override
            void filter(int width, int height, List<ImageRepresentationFormat> formats,
                    List<ImageRepresentationFormat> filteredFormats)
            {
                for (ImageRepresentationFormat format : formats)
                {
                    if (getWidth(format) == width && getHeight(format) == height)
                    {
                        filteredFormats.add(format);
                    }
                }
            }
        };

        abstract void filter(int width, int height, List<ImageRepresentationFormat> formats,
                List<ImageRepresentationFormat> filteredFormats);
    }

    private static final Comparator<ImageRepresentationFormat> SIZE_COMPARATOR =
            new Comparator<ImageRepresentationFormat>()
                {
                    @Override
                    public int compare(ImageRepresentationFormat i1, ImageRepresentationFormat i2)
                    {
                        return area(i1) - area(i2);
                    }

                    private int area(ImageRepresentationFormat format)
                    {
                        return mapNull(format.getWidth()) * mapNull(format.getHeight());
                    }

                };

    private static int getWidth(ImageRepresentationFormat format)
    {
        return mapNull(format.getWidth());
    }

    private static int getHeight(ImageRepresentationFormat format)
    {
        return mapNull(format.getHeight());
    }

    private static int mapNull(Integer number)
    {
        return number == null ? 0 : number.intValue();
    }

    private int width;

    private int height;

    private Type type;

    /**
     * Creates an instance for the specified image size and criterion type.
     */
    public SizeCriterion(int width, int height, Type type)
    {
        this.width = width;
        this.height = height;
        if (type == null)
        {
            throw new IllegalArgumentException("Type not specified.");
        }
        this.type = type;
    }

    @Override
    public List<ImageRepresentationFormat> getMatching(
            List<ImageRepresentationFormat> imageRepresentationFormats)
    {
        List<ImageRepresentationFormat> filteredFormats =
                new ArrayList<ImageRepresentationFormat>();
        type.filter(width, height, imageRepresentationFormats, filteredFormats);
        return filteredFormats;
    }

    //
    // JSON-RPC
    //

    private SizeCriterion()
    {
    }

    private void setWidth(int width)
    {
        this.width = width;
    }

    private int getWidth()
    {
        return width;
    }

    private void setHeight(int height)
    {
        this.height = height;
    }

    private int getHeight()
    {
        return height;
    }

    private void setType(Type type)
    {
        this.type = type;
    }

    private Type getType()
    {
        return type;
    }

}
