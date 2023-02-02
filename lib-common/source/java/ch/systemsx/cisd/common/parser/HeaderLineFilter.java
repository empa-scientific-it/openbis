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

package ch.systemsx.cisd.common.parser;

import ch.systemsx.cisd.common.parser.filter.ExcludeEmptyAndCommentLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * A <code>ILineFilter</code> implementation that extends {@link ExcludeEmptyAndCommentLineFilter} by excluding the header line (if
 * <code>&gt; 1</code>) as well.
 * 
 * @author Christian Ribeaud
 */
public final class HeaderLineFilter implements ILineFilter
{

    /**
     * The line number of the header line.
     * <p>
     * If we set it bigger than <code>-1</code>, we assume that the header contains mapping information and should be skipped by the parser.
     * </p>
     */
    private final int headerLineNumber;

    public HeaderLineFilter(int headerLineNumber)
    {
        this.headerLineNumber = headerLineNumber;
    }

    /**
     * Constructor for a line filter without a header line.
     */
    public HeaderLineFilter()
    {
        this(-1);
    }

    //
    // LineFilter
    //

    @Override
    public final <T> boolean acceptLine(ILine<T> line)
    {
        if (ExcludeEmptyAndCommentLineFilter.INSTANCE.acceptLine(line) == false
                || line.getNumber() == headerLineNumber)
        {
            return false;
        }
        return true;
    }

}