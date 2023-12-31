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
package ch.systemsx.cisd.openbis.dss.etl.dto.api;

/**
 * Allowed colors in which channels can be presented.
 * 
 * @author Tomasz Pylak
 */
public enum ChannelColor
{
    BLUE(0), GREEN(1), RED(2), RED_GREEN(3), RED_BLUE(4), GREEN_BLUE(5);

    private static final int MAX_COLOR = calcMaxColorIndex();

    // If no mapping between channels and colors has been provided then channels get consecutive
    // colors. This field determines the order in which colors are assigned.
    // It is important for backward compatibility as well.
    private final int orderIndex;

    private ChannelColor(int orderIndex)
    {
        this.orderIndex = orderIndex;
    }

    private static int calcMaxColorIndex()
    {
        int max = 0;
        for (ChannelColor color : values())
        {
            max = Math.max(max, color.getColorOrderIndex());
        }
        return max;
    }

    public int getColorOrderIndex()
    {
        return orderIndex;
    }

    public static ChannelColor createFromIndex(int colorIndex)
    {
        for (ChannelColor color : values())
        {
            if (color.getColorOrderIndex() == colorIndex % (MAX_COLOR + 1))
            {
                return color;
            }
        }
        throw new IllegalStateException("Invalid color index: " + colorIndex + "!");
    }

    public ChannelColorRGB getRGB()
    {
        switch (this)
        {
            case RED:
                return new ChannelColorRGB(255, 0, 0);
            case GREEN:
                return new ChannelColorRGB(0, 255, 0);
            case BLUE:
                return new ChannelColorRGB(0, 0, 255);
            case RED_GREEN:
                return new ChannelColorRGB(255, 255, 0);
            case GREEN_BLUE:
                return new ChannelColorRGB(0, 255, 255);
            case RED_BLUE:
                return new ChannelColorRGB(255, 0, 255);
            default:
                throw new IllegalStateException("unhandled enum " + this);
        }
    }
}