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

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A <code>PropertyValueRenderer</code> implementation for <code>Date</code>.
 * 
 * @author Christian Ribeaud
 */
public class DatePropertyValueRenderer extends AbstractSimplePropertyValueRenderer<Date>
{
    private final String pattern;

    public DatePropertyValueRenderer(final IMessageProvider messageProvider, final String pattern)
    {
        super(messageProvider);
        this.pattern = pattern;
    }

    public DatePropertyValueRenderer(final IMessageProvider messageProvider)
    {
        this(messageProvider, null);
    }

    //
    // AbstractPropertyValueRenderer
    //

    @Override
    protected final String renderNotNull(final Date value)
    {
        return DateRenderer.renderDate(value, pattern);
    }
}
