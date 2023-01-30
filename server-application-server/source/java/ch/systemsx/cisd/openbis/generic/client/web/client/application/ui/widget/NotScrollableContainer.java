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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

/**
 * Extension of {@link LayoutContainer} which will neither have scrollbars switched on nor the content cut. It is usable when only the parent
 * container should have the scrollbar.
 * 
 * @author Tomasz Pylak
 */
public class NotScrollableContainer extends LayoutContainer
{
    @Override
    public void setScrollMode(Scroll scroll)
    {
        // do nothing
    }
}
