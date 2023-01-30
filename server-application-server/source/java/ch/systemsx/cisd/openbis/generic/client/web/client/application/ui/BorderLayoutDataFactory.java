/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

/**
 * Factory for {@link BorderLayoutData}
 * 
 * @author Piotr Buczek
 */
public class BorderLayoutDataFactory
{

    public final static BorderLayoutData create(LayoutRegion layoutRegion, float size)
    {
        BorderLayoutData layoutData = new BorderLayoutData(layoutRegion, size, 20, 2000);
        setupCommonProperties(layoutData);
        return layoutData;
    }

    public final static BorderLayoutData create(LayoutRegion layoutRegion)
    {
        BorderLayoutData layoutData = new BorderLayoutData(layoutRegion, 200, 20, 2000);
        setupCommonProperties(layoutData);
        return layoutData;
    }

    private final static void setupCommonProperties(BorderLayoutData layoutData)
    {
        layoutData.setSplit(true);
        layoutData.setMargins(new Margins(2));
        // TODO 2009-11-05, IA: Disabled because tree located in a collapsed panel does not refresh
        // correctly
        layoutData.setCollapsible(false);
        layoutData.setFloatable(false);
    }

}
