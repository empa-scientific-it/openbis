/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

/**
 * @author Tomasz Pylak
 */
public class ToStringUtil
{
    /** Helps in implementing toString() methods */
    public static final void appendNameAndObject(final StringBuilder buffer, final String name,
            final Object object)
    {
        if (object != null)
        {
            buffer.append(name).append("::").append(object).append(";");
        }
    }

}
