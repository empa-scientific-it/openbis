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
 * A line filter for <code>ReaderParser</code>.
 * 
 * @author Christian Ribeaud
 */
public interface ILineFilter
{

    /**
     * If given <code>line</code> should be accepted or not.
     * 
     * @param line the line read from the <code>Reader</code>. Can not be <code>null</code>.
     */
    public <T> boolean acceptLine(final ILine<T> line);
}
