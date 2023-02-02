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

package ch.systemsx.cisd.common.reflection;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A modified short prefix {@link ToStringStyle} which allows to work together with jmock and cruisecontrol. The problem was that in jmock exception
 * messages <code>]]&gt;</code> appeared which is the end of a <code>&lt;![CDATA[</code> section in the XML test report. This yields a hiccup in
 * cruisecontrol.
 * 
 * @author Franz-Josef Elmer
 */
public class ModifiedShortPrefixToStringStyle extends ToStringStyle
{
    public static final String CONTENT_END = "}";

    public static final String CONTENT_START = "{";

    private static final long serialVersionUID = 1L;

    /** The one and only one instance. */
    public static final ToStringStyle MODIFIED_SHORT_PREFIX_STYLE =
            new ModifiedShortPrefixToStringStyle();

    private ModifiedShortPrefixToStringStyle()
    {
        this.setUseShortClassName(true);
        this.setUseIdentityHashCode(false);
        setContentStart(CONTENT_START);
        setContentEnd(CONTENT_END);
    }

    /**
     * <p>
     * Ensure <code>Singleton</ode> after serialization.
     * </p>
     * 
     * @return the singleton
     */
    private Object readResolve()
    {
        return MODIFIED_SHORT_PREFIX_STYLE;
    }

}
