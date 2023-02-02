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

package ch.systemsx.cisd.common.parser.filter;

import ch.systemsx.cisd.common.parser.ILine;

/**
 * A default <code>LineFilter</code> implementation that excludes empty and comment lines.
 * <p>
 * A comment line starts with '#'.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ExcludeEmptyAndCommentLineFilter implements ILineFilter
{
    public static final ILineFilter INSTANCE = new ExcludeEmptyAndCommentLineFilter();

    private ExcludeEmptyAndCommentLineFilter()
    {
    }

    //
    // ILineFilter
    //

    @Override
    public final <T> boolean acceptLine(ILine<T> line)
    {
        assert line != null : "Unspecified line";
        final String trimmed = line.getText().trim();
        return trimmed.length() > 0 && trimmed.startsWith("#") == false;
    }
}