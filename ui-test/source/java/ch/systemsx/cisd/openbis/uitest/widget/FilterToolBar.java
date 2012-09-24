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

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.DeterminateAction;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WaitForRefreshOf;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WidgetContext;

/**
 * @author anttil
 */
public class FilterToolBar implements Widget
{

    private WidgetContext context;

    public void setCode(final String text, Grid refreshingGrid)
    {
        final Text t = context.find(".//input[contains(@id, 'Code-input')]", Text.class);

        new WaitForRefreshOf<Void>(refreshingGrid)
                .after(new DeterminateAction<Void>()
                    {
                        @Override
                        public Void execute()
                        {
                            t.write(text);
                            return null;
                        }
                    }).withTimeoutOf(10);

    }

    public void reset()
    {
        Button b = context.find(".//button[text()='Reset']", Button.class);
        b.click();
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }
}
