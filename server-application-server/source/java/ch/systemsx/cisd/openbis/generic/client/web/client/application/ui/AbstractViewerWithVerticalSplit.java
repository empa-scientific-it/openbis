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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.BorderLayoutHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;

/**
 * {@link AbstractViewer} extension with additional support for vertical layout with left panel state (expanded size / collapsed) saved in display
 * settings.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractViewerWithVerticalSplit<D extends IEntityInformationHolder> extends
        AbstractViewer<D>
{
    protected AbstractViewerWithVerticalSplit(IViewContext<?> viewContext, String id)
    {
        super(viewContext, id);
    }

    protected AbstractViewerWithVerticalSplit(final IViewContext<?> viewContext, String title,
            String id, boolean withToolBar)
    {
        super(viewContext, title, id, withToolBar);
    }

    protected final static BorderLayoutData createRightBorderLayoutData()
    {
        return BorderLayoutHelper.createRightBorderLayoutData();
    }

    /**
     * Creates {@link BorderLayoutData} for the left panel extracting initial size from display settings.
     */
    protected final BorderLayoutData createLeftBorderLayoutData()
    {
        return getHelper().createLeftBorderLayoutData();
    }

    /**
     * Adds listeners and sets up the initial left panel state.
     * <p>
     * Id for display settings needs to be initialized before this method is called.
     */
    protected void configureLeftPanel(final Component panel)
    {
        getHelper().configureLeftPanel(panel);
    }

    private BorderLayoutHelper getHelper()
    {
        return new BorderLayoutHelper(viewContext, (BorderLayout) getLayout(), displayIdSuffix);
    }
}
