/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Date;

/**
 * This class is able to render a data in a fix format both at GWT side and at the server side.
 * 
 * @author Tomasz Pylak
 */
public class SimpleDateRenderer
{
    private static final String DATE_SEP = "-";

    private static final String TIME_SEP = ":";

    private static final int MINUTES_PER_HOUR = 60;

    /** renders a date in yyyy-MM-dd HH:mm:ss zzz format */
    @SuppressWarnings("deprecation")
    public final static String renderDate(final Date dateOrNull)
    {
        if (dateOrNull == null)
        {
            return "";
        }
        int year = dateOrNull.getYear() + 1900;
        int month = dateOrNull.getMonth() + 1;
        int day = dateOrNull.getDate();
        int hour = dateOrNull.getHours();
        int min = dateOrNull.getMinutes();
        int sec = dateOrNull.getSeconds();

        StringBuffer sb = new StringBuffer();
        sb.append(year);
        sb.append(DATE_SEP);
        zeroPaddingNumber(sb, month);
        sb.append(DATE_SEP);
        zeroPaddingNumber(sb, day);

        sb.append(" ");
        zeroPaddingNumber(sb, hour);
        sb.append(TIME_SEP);
        zeroPaddingNumber(sb, min);
        sb.append(TIME_SEP);
        zeroPaddingNumber(sb, sec);

        sb.append(" ");
        appendGMT(sb, dateOrNull.getTimezoneOffset());

        return sb.toString();
    }

    // Converts to string. Adds "0" at the beginning if value is one digit.
    private static void zeroPaddingNumber(StringBuffer buf, int value)
    {
        buf.append(value < 10 ? "0" : "");
        buf.append(value);
    }

    /**
     * Method copied from com.google.gwt.i18n.client.DateTimeFormat class.<br>
     * Generate GMT timezone string for given date.
     * 
     * @param buf where timezone string will be appended to
     */
    private static void appendGMT(StringBuffer buf, int timezoneOffset)
    {
        int value = -timezoneOffset;

        if (value < 0)
        {
            buf.append("GMT-");
            value = -value; // suppress the '-' sign for text display.
        } else
        {
            buf.append("GMT+");
        }

        zeroPaddingNumber(buf, value / MINUTES_PER_HOUR);
        buf.append(':');
        zeroPaddingNumber(buf, value % MINUTES_PER_HOUR);
    }
}
