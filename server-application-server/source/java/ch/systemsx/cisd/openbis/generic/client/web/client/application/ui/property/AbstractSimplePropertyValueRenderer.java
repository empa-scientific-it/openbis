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

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A default <code>PropertyValueRenderer</code> implementation for simple property values which catches <code>null</code> value by rendering
 * <code>-</code> and renders as an inline html.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractSimplePropertyValueRenderer<T> extends
        AbstractPropertyValueRenderer<T>
{
    /** The table <code>null</code> value representation. */
    public static final String TABLE_NULL_VALUE = "-";

    public AbstractSimplePropertyValueRenderer(final IMessageProvider messageProvider)
    {
        super(messageProvider);
    }

    /** Here we are sure to get a non-<code>null</code> value. */
    protected abstract String renderNotNull(final T value);

    private final String render(final T object)
    {
        if (object == null)
        {
            return TABLE_NULL_VALUE;
        }
        return renderNotNull(object);
    }

    @Override
    public Widget getAsWidget(final T object)
    {
        // default widget is a simple HTML span element wrapped around text
        return new InlineHTML(render(object));
    }
}
