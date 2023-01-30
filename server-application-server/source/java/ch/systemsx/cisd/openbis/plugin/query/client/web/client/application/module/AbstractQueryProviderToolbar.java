/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * An abstract implementation of a query viewer toolbar that provides query information.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractQueryProviderToolbar extends ToolBar implements IQueryProvider
{
    private IDelegatedAction refreshViewerAction;

    protected final IViewContext<IQueryClientServiceAsync> viewContext;

    protected final Button executeButton;

    public AbstractQueryProviderToolbar(final IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        this.executeButton = createExecuteButton();
        setBorders(true);
    }

    abstract protected boolean isQueryValid();

    public final void setRefreshViewerAction(IDelegatedAction refreshViewerAction)
    {
        this.refreshViewerAction = refreshViewerAction;
    }

    private Button createExecuteButton()
    {
        return new Button(viewContext.getMessage(Dict.QUERY_EXECUTE),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            tryExecuteQuery();
                        }
                    });
    }

    protected final void tryExecuteQuery()
    {
        if (isQueryValid() && refreshViewerAction != null)
        {
            refreshViewerAction.execute();
        }
    }

}
