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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Base class of a page of a {@link Wizard}. Should be subclassed by overriding {@link #init()}, {@link #deactivate()}, {@link #activate()},
 * {@link #destroy()} if needed.
 * 
 * @author Franz-Josef Elmer
 */
public class WizardPage<M extends IWizardDataModel> extends LayoutContainer
{
    private static final String _LEFT_CONTENT = "_left_content";

    public static final String PREVIOUS_BUTTON_LABEL_KEY = "wizard_page_previous_button_label";

    public static final String NEXT_BUTTON_LABEL_KEY = "wizard_page_next_button_label";

    public static final String FINISH_BUTTON_LABEL_KEY = "wizard_page_finish_button_label";

    private final IMessageProvider messageProvider;

    protected final M model;

    private final IWizardState state;

    private final LayoutContainer leftContent;

    private final LayoutContainer rightContent;

    private Button previousButton;

    private Button nextOrFinishButton;

    /**
     * Creates an instance for specified message provider, wizard state and wizard data model. Subclasses should create in the constructor only widget
     * components which do not need sever calls. Components created/populated by server callbacks should be created in {@link #init()}.
     */
    public WizardPage(IMessageProvider messageProvider, IWizardState state, final M model)
    {
        super(new BorderLayout());
        this.messageProvider = messageProvider;
        this.state = state;
        this.model = model;
        LayoutContainer leftCenterPanel = new LayoutContainer(new CenterLayout());
        leftCenterPanel.setStyleAttribute("background-color", "white");
        add(leftCenterPanel, new BorderLayoutData(LayoutRegion.WEST, 0.3f, 50, 200));
        leftContent = new LayoutContainer(new RowLayout());
        leftCenterPanel.add(leftContent);
        ContentPanel rightPanel = new ContentPanel(new RowLayout());
        rightPanel.setHeaderVisible(false);
        add(rightPanel, new BorderLayoutData(LayoutRegion.CENTER));
        LayoutContainer rightCenterPanel = new LayoutContainer(new RowLayout());
        rightContent = new LayoutContainer(new RowLayout());
        rightCenterPanel.add(rightContent, new RowData(1, 1, new Margins(10)));
        rightPanel.add(rightCenterPanel);

        final WizardWorkflowModel workflowModel = model.getWorkflow();
        if (workflowModel.hasPreviousState(state))
        {
            previousButton = new Button(messageProvider.getMessage(PREVIOUS_BUTTON_LABEL_KEY));
            previousButton.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        workflowModel.previousState();
                    }
                });
            rightPanel.addButton(previousButton);
        }
        String key =
                workflowModel.hasNextState(state) ? NEXT_BUTTON_LABEL_KEY : FINISH_BUTTON_LABEL_KEY;
        nextOrFinishButton = new Button(messageProvider.getMessage(key));
        nextOrFinishButton.setEnabled(false);
        nextOrFinishButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    deactivate();
                    workflowModel.nextState();
                }
            });
        rightPanel.addButton(nextOrFinishButton);
    }

    IWizardState getWizardState()
    {
        return state;
    }

    /**
     * Sets the content of the left panel by an HTML snippet defined in the dictionary by the specified key. Note that the complete key in the
     * dictionary reads <code>&lt;wizard
     * state&gt;_left_content</code>.
     */
    public void setLeftContentByDictionary()
    {
        setLeftContentBy(new Html(messageProvider.getMessage(state + _LEFT_CONTENT)));
    }

    /**
     * Sets the content of the left hand part of this wizard page.
     */
    public void setLeftContentBy(Component component)
    {
        leftContent.removeAll();
        leftContent.add(component, new RowData(1, 1, new Margins(10)));
    }

    /**
     * Adds specified component to the right hand part of this wizard page. All components are layout by a vertical {@link RowLayout}.
     */
    public void addToRightContent(Component component, RowData layoutData)
    {
        rightContent.add(component, layoutData);
    }

    /**
     * Enables/disables next/finish button.
     */
    public void enableNextButton(boolean enabled)
    {
        nextOrFinishButton.setEnabled(enabled);
    }

    /**
     * Initializes this wizard page when it is shown the first time in the wizard.
     * <p>
     * This is a hook method which does nothing. It should be overridden if needed.
     */
    public void init()
    {
    }

    /**
     * Activates this page. This is called after {@link #deactivate()} has been invoked for the previous page (if any).
     * <p>
     * This is a hook method which does nothing. It should be overridden if needed.
     */
    public void activate()
    {
    }

    /**
     * Deactivates this page. This is called after the next/finish button has been pressed but before {@link #activate()} for the next page has been
     * invoked. After invoking this method and before invocation of {@link #activate()} the method
     * {@link IWizardDataModel#determineNextState(IWizardState)} will be invoked if more than one next states are possible.
     * <p>
     * This is a hook method which does nothing. It should be overridden if needed.
     */
    public void deactivate()
    {
    }

    /**
     * Destroys this page. It will be invoked when the wizard has successfully be finished. That is, after {@link IWizardDataModel#finish()} has been
     * invoked. This method can be used to release resources.
     * <p>
     * This is a hook method which does nothing. It should be overridden if needed.
     */
    public void destroy()
    {

    }
}
