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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.google.gwt.user.client.ui.Hyperlink;

/**
 * A {@link Hyperlink} widget extension that changes default display style from block to inline and keeps no history token.
 * 
 * @author Piotr Buczek
 */
public final class InlineHyperlink extends Hyperlink
{

    /**
     * Creates a hyperlink with its text specified (no history token) and inline display style.
     * 
     * @param text the hyperlink's text
     */
    public InlineHyperlink(String text)
    {
        super(text, null);
        addStyleName("inline");
    }

}
