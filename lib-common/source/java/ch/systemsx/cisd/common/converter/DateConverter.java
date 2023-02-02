/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A <code>Converter</code> implementation for {@link Date}.
 * <p>
 * {@link #getDefaultValue()} always returns the current system time.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class DateConverter implements Converter<Date>
{
    private final SimpleDateFormat dateFormat;

    /**
     * @param datePattern Must not be <code>null</code>.
     */
    public DateConverter(String datePattern)
    {
        assert datePattern != null;

        this.dateFormat = createDateFormat(datePattern);
    }

    /**
     * Create a date format for the specified pattern.
     * 
     * @param pattern The date pattern
     * @return The DateFormat
     */
    private final static SimpleDateFormat createDateFormat(String pattern)
    {
        return new SimpleDateFormat(pattern);
    }

    //
    // Converter
    //

    /**
     * Converts given <code>value</code> to a <code>Date</code>.
     * <p>
     * Return <code>null</code> if a <code>ParseException</code> occurs or if given <var>value</var> is <code>null</code>.
     * </p>
     */
    @Override
    public final Date convert(String value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return dateFormat.parse(value);
        } catch (ParseException ex)
        {
            return null;
        }
    }

    @Override
    public final Date getDefaultValue()
    {
        return new Date();
    }

}