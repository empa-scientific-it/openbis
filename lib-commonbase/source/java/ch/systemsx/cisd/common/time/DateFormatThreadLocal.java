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
package ch.systemsx.cisd.common.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A small <code>ThreadLocal</code> extension suitable for non-threadsafe {@link DateFormat}.
 * <p>
 * As <i>Javadoc</i> states, {@link DateFormat}s are inherently unsafe for multithreaded use.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class DateFormatThreadLocal extends ThreadLocal<SimpleDateFormat>
{

    /** The default date format pattern. */
    public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    /** The uniform date format used. */
    public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            new DateFormatThreadLocal(DATE_FORMAT_PATTERN);

    private final String pattern;

    public DateFormatThreadLocal(final String pattern)
    {
        this.pattern = pattern;
    }

    //
    // ThreadLocal
    //

    @Override
    protected final SimpleDateFormat initialValue()
    {
        return new SimpleDateFormat(pattern);
    }
}