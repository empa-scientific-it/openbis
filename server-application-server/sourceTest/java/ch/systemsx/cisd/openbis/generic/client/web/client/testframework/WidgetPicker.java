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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import com.google.gwt.user.client.ui.Widget;

/**
 * Widget handler which picks the first widget having a specified ID. Allows to use regular expression wildcards.
 * 
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public class WidgetPicker implements IWidgetHandler<Widget>
{
    private final String id;

    private Widget pickedWidget;

    /**
     * Creates an instance for the specified widget ID. Given <var>id</var> may contain regular expression wildcards.
     */
    public WidgetPicker(final String id)
    {
        assert id != null : "Unspecified widget id.";
        this.id = id;
    }

    /**
     * Returns the picked widget or <code>null</code>.
     */
    public final Widget tryToGetPickedWidget()
    {
        return pickedWidget;
    }

    protected boolean acceptWidgetId(final String widgetIdOrNull)
    {
        return widgetIdOrNull == null ? false : widgetIdOrNull.matches(id);
    }

    //
    // IWidgetHandler
    //

    @Override
    public final boolean handle(final Widget widgetOrNull)
    {
        final String widgetId = GWTTestUtil.tryToGetWidgetID(widgetOrNull);
        if (acceptWidgetId(widgetId))
        {
            pickedWidget = widgetOrNull;
            return true;
        }
        return false;
    }

}
