/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.widget;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WidgetContext;

/**
 * @author anttil
 */
public class Button implements AtomicWidget
{

    private WidgetContext context;

    public void click()
    {
        context.click();
    }

    public boolean isPressed()
    {
        return "true".equalsIgnoreCase(context.getAttribute("aria-pressed"));
    }

    @Override
    public String getTagName()
    {
        return "button";
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }

    public String getText()
    {
        return context.getText();
    }
}
