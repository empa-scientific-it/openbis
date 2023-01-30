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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A time range composed of a <code>from</code> and of an <code>until</code> date.
 * <p>
 * Note that both dates can be <code>null</code>.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class TimeInterval implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private Date from;

    private Date until;

    public TimeInterval()
    {
    }

    public TimeInterval(final Date from, final Date until)
    {
        check(from, until);
        this.from = from;
        this.until = until;
    }

    private void check(final Date fromDate, final Date untilDate)
    {
        if (fromDate != null && untilDate != null && fromDate.getTime() > untilDate.getTime())
        {
            throw new IllegalArgumentException("From date is after until date: " + fromDate + " > "
                    + untilDate);
        }
    }

    public final Date getFrom()
    {
        return from;
    }

    public final void setFrom(final Date from)
    {
        check(until, from);
        this.from = from;
    }

    public final Date getUntil()
    {
        return until;
    }

    public final void setUntil(final Date until)
    {
        check(until, from);
        this.until = until;
    }

    public final boolean isInside(final Date date)
    {
        assert date != null : "Unspecified date.";
        final long time = date.getTime();
        return (from == null || from.getTime() <= time)
                && (until == null || time <= until.getTime());
    }

    public final boolean overlapsWith(final TimeInterval timeInterval)
    {
        assert timeInterval != null : "Unspecified time interval.";
        return isBeforeOrEqual(from, timeInterval.getUntil())
                && isBeforeOrEqual(timeInterval.getFrom(), until);
    }

    /**
     * Whether given <var>olderDate</var> is before or equal to given <var>youngerDate</var>.
     * <p>
     * Returns <code>true</code> if one of given arguments is <code>null</code>.
     * </p>
     */
    private final static boolean isBeforeOrEqual(final Date olderDate, final Date youngerDate)
    {
        return olderDate == null || youngerDate == null
                || olderDate.getTime() <= youngerDate.getTime();
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof TimeInterval == false)
        {
            return false;
        }
        final TimeInterval that = (TimeInterval) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.from, from);
        builder.append(that.until, until);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(from);
        builder.append(until);
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
