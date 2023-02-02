/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * @author pkupczyk
 */
public class MemorySizeFormatter
{

    public static long parse(String size)
    {
        for (Format format : Format.values())
        {
            if (format.canParse(size))
            {
                return format.parse(size);
            }
        }

        throw new IllegalArgumentException("Could not parse memory size value: " + size);
    }

    public static String format(long size)
    {
        for (Format format : Format.values())
        {
            if (format.canFormat(size))
            {
                return format.format(size);
            }
        }

        throw new IllegalArgumentException("Could not format memory size value: " + size);
    }

    private enum Format
    {

        ZERO("(0+)\\s*(|b|B)", "", 0)
        {
            @Override
            public boolean canFormat(long size)
            {
                return size == 0;
            }
        },
        BYTE("([0-9]+)\\s*(|b|B)", "b", 0),
        KILO("([0-9]+)\\s*(k|K)", "k", 1),
        MEGA("([0-9]+)\\s*(m|M)", "m", 2),
        GIGA("([0-9]+)\\s*(g|G)", "g", 3),
        TERA("([0-9]+)\\s*(t|T)", "t", 4)
        {
            @Override
            public boolean canFormat(long size)
            {
                return size > 0;
            }
        };

        private Pattern parsePattern;

        private String formatUnit;

        private int power;

        Format()
        {
        }

        Format(String parsePattern, String formatUnit, int power)
        {
            this.parsePattern = Pattern.compile(parsePattern);
            this.formatUnit = formatUnit;
            this.power = power;
        }

        public boolean canParse(String size)
        {
            return parsePattern.matcher(size).matches();
        }

        public long parse(String size)
        {
            Matcher m = parsePattern.matcher(size);
            m.matches();
            long value = Long.valueOf(m.group(1));
            long factor = (long) Math.pow(FileUtils.ONE_KB, power);
            return value * factor;
        }

        public boolean canFormat(long size)
        {
            long lowerBound = (long) Math.pow(FileUtils.ONE_KB, power);
            long upperBound = (long) Math.pow(FileUtils.ONE_KB, power + 1);
            return lowerBound <= size && size < upperBound;
        }

        public String format(long size)
        {
            double factor = Math.pow(FileUtils.ONE_KB, power);
            double value = size / factor;

            if (Math.abs(value - Math.round(value)) < 0.1)
            {
                return String.format("%d", Math.round(value)) + formatUnit;
            } else
            {
                return String.format("%.1f", value) + formatUnit;
            }

        }
    }

}
