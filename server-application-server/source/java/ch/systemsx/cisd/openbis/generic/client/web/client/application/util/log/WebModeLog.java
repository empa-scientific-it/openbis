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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.core.client.Duration;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DOMUtils;

/**
 * <code>LogImpl</code> implementation for the <i>Web Mode</i>.
 * 
 * @author Christian Ribeaud
 */
final class WebModeLog extends LogImpl
{
    private final LoggingDialog loggingDialog;

    WebModeLog()
    {
        loggingDialog = new LoggingDialog();
    }

    //
    // LogImpl
    //

    @Override
    public final void hide()
    {
        loggingDialog.hide();
    }

    @Override
    public final void show()
    {
        loggingDialog.show();
    }

    @Override
    public final void log(final String message)
    {
        loggingDialog.append(message);
    }

    @Override
    public final void logTimeTaken(final Duration duration, final String taskName)
    {
        final String message = taskName + " took " + duration.elapsedMillis() / 1000F + "s";
        loggingDialog.append(message);
    }

    //
    // Helper Classes
    //

    private final static class LoggingDialog extends Dialog
    {
        private StringBuffer buffer;

        private Html html;

        LoggingDialog()
        {
            setHeading("Logging Console");
            setScrollMode(Scroll.AUTO);
            setWidth(500);
            setHeight(300);
            setBodyStyle("backgroundColor: #ffffff;");
            setCollapsible(true);
            setHideOnButtonClick(true);
        }

        /** Appends given <code>text</code> to <code>ContentPanel</code>. */
        final void append(final String text)
        {
            if (buffer == null)
            {
                buffer = new StringBuffer();
            }
            if (buffer.length() > 0)
            {
                buffer.append(DOMUtils.BR);
            }
            buffer.append(text);
            System.out.println(buffer);
            if (html == null)
            {
                html = addText(buffer.toString());
            } else
            {
                html.setHtml(buffer.toString());
            }
            layout();
        }
    }
}
