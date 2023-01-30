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

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * A <i>Java Bean</i> that filters a list of results.
 * <p>
 * Typically this filter is applied to results returned by the database before sending them to the client.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class LimitFilter extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private final static int NO_LIMIT = -1;

    public LimitFilter(int limit)
    {
        this.limit = limit;
    }

    /**
     * Creates the empty filter.
     */
    public LimitFilter()
    {
        this(NO_LIMIT);
    }

    public final static LimitFilter createEmpty()
    {
        return new LimitFilter();
    }

    /**
     * The maximum number of items the returned list should be composed of.
     */
    private int limit;

    /** At which index to apply {@link #limit}. */
    private int start;

    /**
     * Returns <code>limit</code>.
     * <p>
     * Never returns a value smaller than <code>NO_LIMIT</code>.
     * </p>
     */
    public final int getLimit()
    {
        return Math.max(limit, NO_LIMIT);
    }

    public final void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * Returns <code>limit</code>.
     * <p>
     * Never returns a negative value.
     * </p>
     */
    public final int getStart()
    {
        return Math.max(start, 0);
    }

    public final void setStart(int start)
    {
        this.start = start;
    }

    /** @return true if the filter has no effect */
    public final boolean isEmpty()
    {
        return this.equals(createEmpty());
    }
}
