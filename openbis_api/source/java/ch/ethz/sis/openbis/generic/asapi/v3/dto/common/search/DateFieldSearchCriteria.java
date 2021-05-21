/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonObject("as.dto.common.search.DateFieldSearchCriteria")
public abstract class DateFieldSearchCriteria extends AbstractFieldSearchCriteria<IDate>
{
    @JsonIgnore
    public static final ShortDateFormat SHORT_DATE_FORMAT = new ShortDateFormat();

    @JsonIgnore
    public static final NormalDateFormat NORMAL_DATE_FORMAT = new NormalDateFormat();

    @JsonIgnore
    public static final LongDateFormat LONG_DATE_FORMAT = new LongDateFormat();

    @JsonIgnore
    public static final List<IDateFormat> DATE_FORMATS = new ArrayList<IDateFormat>();

    private static final long serialVersionUID = 1L;

    static
    {
        DATE_FORMATS.add(LONG_DATE_FORMAT);
        DATE_FORMATS.add(NORMAL_DATE_FORMAT);
        DATE_FORMATS.add(SHORT_DATE_FORMAT);
    }

    private ITimeZone timeZone = new ServerTimeZone();

    protected DateFieldSearchCriteria(String fieldName, SearchFieldType fieldType)
    {
        super(fieldName, fieldType);
    }

    public void thatEquals(Date date)
    {
        setFieldValue(new DateObjectEqualToValue(date));
    }

    public void thatEquals(String date)
    {
        setFieldValue(new DateEqualToValue(date));
    }

    public void thatIsLaterThanOrEqualTo(Date date)
    {
        setFieldValue(new DateObjectLaterThanOrEqualToValue(date));
    }

    public void thatIsLaterThanOrEqualTo(String date)
    {
        setFieldValue(new DateLaterThanOrEqualToValue(date));
    }

    public void thatIsLaterThan(final Date date)
    {
        setFieldValue(new DateObjectLaterThanValue(date));
    }

    public void thatIsLaterThan(final String date)
    {
        setFieldValue(new DateLaterThanValue(date));
    }

    public void thatIsEarlierThanOrEqualTo(Date date)
    {
        setFieldValue(new DateObjectEarlierThanOrEqualToValue(date));
    }

    public void thatIsEarlierThanOrEqualTo(String date)
    {
        setFieldValue(new DateEarlierThanOrEqualToValue(date));
    }

    public void thatIsEarlierThan(final Date date)
    {
        setFieldValue(new DateObjectEarlierThanValue(date));
    }

    public void thatIsEarlierThan(final String date)
    {
        setFieldValue(new DateEarlierThanValue(date));
    }

    public DateFieldSearchCriteria withServerTimeZone()
    {
        this.timeZone = new ServerTimeZone();
        return this;
    }

    public DateFieldSearchCriteria withTimeZone(int hourOffset)
    {
        this.timeZone = new TimeZone(hourOffset);
        return this;
    }

    public void setTimeZone(ITimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    public ITimeZone getTimeZone()
    {
        return timeZone;
    }

    @Override
    public void setFieldValue(IDate value)
    {
        checkValueFormat(value);
        super.setFieldValue(value);
    }

    private static void checkValueFormat(IDate value)
    {
        if (value instanceof AbstractDateValue)
        {
            for (IDateFormat dateFormat : DATE_FORMATS)
            {
                if (formatValue(((AbstractDateValue) value).getValue(), dateFormat) != null)
                {
                    return;
                }
            }

            throw new IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: "
                    + DATE_FORMATS);
        }
    }

    @JsonIgnore
    public static Date formatValue(final String value, final IDateFormat dateFormat)
    {
        try
        {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat.getFormat());
            simpleDateFormat.setLenient(false);
            return simpleDateFormat.parse(value);
        } catch (final ParseException e)
        {
            return null;
        }
    }

}