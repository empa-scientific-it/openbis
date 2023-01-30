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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import com.google.gwt.user.client.ui.Widget;

/**
 * A property value renderer for <code>Object</code>.
 * 
 * @author Christian Ribeaud
 * @see PropertyGrid
 */
public interface IPropertyValueRenderer<T>
{

    /**
     * @return A {@link Widget} displaying given <var>object</var>.
     */
    public Widget getAsWidget(final T object);
}
