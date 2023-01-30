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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Method;

import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;

/**
 * A return value filter.
 * <p>
 * A <code>IReturnValueFilter</code> should never throw an exception but should only take care of filtering.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IReturnValueFilter
{

    /**
     * Applies filtering on given <var>returnValue</var>.
     * 
     * @param returnValueOrNull the return value that should be filtered. If the method has a return value of type <code>void</code>, this would be
     *            <code>null</code>.
     */
    public Object applyFilter(final IAuthSession session, final Method method,
            final Object returnValueOrNull);
}
