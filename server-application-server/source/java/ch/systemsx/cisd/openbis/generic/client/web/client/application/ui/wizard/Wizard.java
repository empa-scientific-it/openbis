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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * Container of {@link WizardPage} instance. Manages changing between pages.
 *
 * @author Franz-Josef Elmer
 */
public class Wizard<M extends IWizardDataModel> extends LayoutContainer implements IDisposableComponent
{
    private final CardLayout layout;

    private final M model;

    private final WizardWorkflowModel workflowModel;

    private final Map<IWizardState, WizardPage<M>> pages =
            new HashMap<IWizardState, WizardPage<M>>();

    private final Set<WizardPage<M>> visitedPages = new HashSet<WizardPage<M>>();

    /**
     * Creates an instance for the specified wizard data model.
     */
    public Wizard(M model)
    {
        layout = new CardLayout();
        setLayout(layout);
        this.model = model;
        workflowModel = model.getWorkflow();
        workflowModel.addStateChangeListener(new IWizardStateChangeListener()
            {
                @Override
                public void stateChanged(IWizardState previousStateOrNull,
                        IWizardState currentStateOrNull)
                {
                    changePage(previousStateOrNull, currentStateOrNull);
                }
            });

    }

    public M getWizardDataModel()
    {
        return model;
    }

    /**
     * Registers specified wizard page.
     */
    public void register(WizardPage<M> page)
    {
        pages.put(page.getWizardState(), page);
    }

    /**
     * Starts this wizard.
     */
    public void start()
    {
        workflowModel.nextState();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        for (WizardPage<M> page : visitedPages)
        {
            page.destroy();
        }
    }

    private void changePage(IWizardState previousStateOrNull, IWizardState currentStateOrNull)
    {
        if (currentStateOrNull != null)
        {
            WizardPage<M> nextPage = pages.get(currentStateOrNull);
            if (nextPage != null)
            {
                nextPage.activate();
                if (visitedPages.contains(nextPage) == false)
                {
                    nextPage.init();
                    visitedPages.add(nextPage);
                    add(nextPage);
                }
                layout.setActiveItem(nextPage);
            }
        } else if (workflowModel.hasNextState(currentStateOrNull) == false)
        {
            String message = model.finish();
            MessageBox.info("Info", message, null);
            Widget parent = getParent();
            if (parent instanceof TabItem)
            {
                ((TabItem) parent).close();
            }
        }
    }
}
