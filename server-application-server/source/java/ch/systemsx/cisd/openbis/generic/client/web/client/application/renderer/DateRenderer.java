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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * A <i>static</i> class to render {@link java.util.Date}.
 * 
 * @author Christian Ribeaud
 */
public final class DateRenderer
{
    /**
     * Default date time format pattern.
     * <p>
     * Suitable for displaying full registration date.
     * </p>
     */
    public static final String DEFAULT_DATE_FORMAT_PATTERN =
            BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN;

    /**
     * Default {@link DateTimeFormat}.
     */
    public static final DateTimeFormat DEFAULT_DATE_TIME_FORMAT = DateTimeFormat
            .getFormat(DEFAULT_DATE_FORMAT_PATTERN);

    /**
     * Short date time format pattern.
     * <p>
     * Suitable for choosing a date from a calendar.
     * </p>
     */
    public static final String SHORT_DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    /**
     * Default {@link DateTimeFormat}.
     */
    public static final DateTimeFormat SHORT_DATE_TIME_FORMAT = DateTimeFormat
            .getFormat(SHORT_DATE_FORMAT_PATTERN);

    private DateRenderer()
    {
        // Can not be instantiated.
    }

    /**
     * Renders given <var>date</var>.
     */
    public final static String renderDate(final Date date)
    {
        return renderDate(date, null);
    }

    /**
     * Renders given <var>date</var>.
     */
    public final static String renderDate(final Date date, final String formatOrNull)
    {
        assert date != null : "Unspecified date.";
        if (formatOrNull == null || formatOrNull.equals(DEFAULT_DATE_FORMAT_PATTERN))
        {
            return DEFAULT_DATE_TIME_FORMAT.format(date);
        }
        return DateTimeFormat.getFormat(formatOrNull).format(date);
    }
}
