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

package ch.systemsx.cisd.openbis.dss.etl.dynamix;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.common.parser.TabFileLoader;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Utility methods to map DynamiX specific position to well location.
 * 
 * @author Tomasz Pylak
 */
class WellLocationMappingUtils
{
    public static Map<DynamixWellPosition, WellLocation> parseWellLocationMap(File mappingFile)
    {
        final TabFileLoader<MappingEntry> parser =
                new TabFileLoader<MappingEntry>(MappingEntry.class);
        Map<String, String> defaults = Collections.emptyMap();
        List<MappingEntry> mappingEntries = parser.load(mappingFile, defaults);
        return createMapping(mappingEntries);
    }

    public static DynamixWellPosition parseWellPosition(String sideToken, String posToken)
    {
        return new DynamixWellPosition(Integer.valueOf(posToken), isRight(sideToken));
    }

    private static Map<DynamixWellPosition, WellLocation> createMapping(
            List<MappingEntry> mappingEntries)
    {
        Map<DynamixWellPosition, WellLocation> mapping =
                new HashMap<DynamixWellPosition, WellLocation>();
        for (MappingEntry entry : mappingEntries)
        {
            DynamixWellPosition wellPos = parseWellPosition(entry.getSide(), entry.getPosition());

            int row = Integer.valueOf(entry.getRow());
            int col = Integer.valueOf(entry.getColumn());
            WellLocation wellLoc = new WellLocation(row, col);

            mapping.put(wellPos, wellLoc);
        }
        return mapping;
    }

    private static boolean isRight(String sideToken)
    {
        return sideToken.equalsIgnoreCase("Right");
    }

    public static class MappingEntry extends AbstractHashable
    {
        private String position;

        private String side;

        private String row;

        private String column;

        public String getPosition()
        {
            return position;
        }

        @BeanProperty(label = "position")
        public void setPosition(String position)
        {
            this.position = position;
        }

        public String getSide()
        {
            return side;
        }

        @BeanProperty(label = "Side")
        public void setSide(String side)
        {
            this.side = side;
        }

        public String getRow()
        {
            return row;
        }

        @BeanProperty(label = "row")
        public void setRow(String row)
        {
            this.row = row;
        }

        public String getColumn()
        {
            return column;
        }

        @BeanProperty(label = "column")
        public void setColumn(String column)
        {
            this.column = column;
        }
    }

    static class DynamixWellPosition extends AbstractHashable
    {
        private final int position;

        private final boolean isRight;

        public DynamixWellPosition(int position, boolean isRight)
        {
            this.position = position;
            this.isRight = isRight;
        }

        public int getPosition()
        {
            return position;
        }

        public boolean isRight()
        {
            return isRight;
        }
    }

}
