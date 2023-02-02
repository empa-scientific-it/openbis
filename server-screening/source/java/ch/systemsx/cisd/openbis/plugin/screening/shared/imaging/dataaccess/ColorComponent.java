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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess;

import java.awt.Color;

/**
 * @author Tomasz Pylak
 */
public enum ColorComponent
{
    RED
    {
        @Override
        public int getComponent(Color color)
        {
            return color.getRed();
        }

        @Override
        public Color extractSingleComponent(int rgb)
        {
            return new Color(getComponent(new Color(rgb)), 0, 0);
        }
    },

    GREEN
    {
        @Override
        public int getComponent(Color color)
        {
            return color.getGreen();
        }

        @Override
        public Color extractSingleComponent(int rgb)
        {
            return new Color(0, getComponent(new Color(rgb)), 0);
        }
    },

    BLUE
    {
        @Override
        public int getComponent(Color color)
        {
            return color.getBlue();
        }

        @Override
        public Color extractSingleComponent(int rgb)
        {
            return new Color(0, 0, getComponent(new Color(rgb)));
        }
    };

    public abstract int getComponent(Color color);

    /** creates the color which has only one component from the specified color, others are set to 0 */
    public abstract Color extractSingleComponent(int rgb);
}