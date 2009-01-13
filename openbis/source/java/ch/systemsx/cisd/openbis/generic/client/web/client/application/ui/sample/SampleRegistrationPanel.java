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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The {@link LayoutContainer} extension for registering a sample.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleRegistrationPanel extends ContentPanel
{
    private final SampleTypeSelectionWidget sampleTypeSelection;

    public static final String ID_SUFFIX = "sample-registration";

    public static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    public SampleRegistrationPanel(final CommonViewContext viewContext)
    {
        setId(ID);
        setScrollMode(Scroll.AUTO);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, ID_SUFFIX, false);
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        setTopComponent(toolBar);
        sampleTypeSelection.addSelectionChangedListener(new SampleTypeSelectionChangeListener(
                viewContext));
    }

    private class SampleTypeSelectionChangeListener extends
            SelectionChangedListener<SampleTypeModel>
    {

        private Widget sampleRegistrationWidget;

        private final CommonViewContext viewContext;

        private PreviousSelection<SampleTypeModel> previousSelection =
                new PreviousSelection<SampleTypeModel>();

        public SampleTypeSelectionChangeListener(CommonViewContext viewContext)
        {
            this.viewContext = viewContext;
        }

        @Override
        public void selectionChanged(final SelectionChangedEvent<SampleTypeModel> se)
        {
            final SampleTypeModel sampleTypeModel = se.getSelectedItem();
            if (sampleTypeModel != null)
            {
                final SampleType sampleType = sampleTypeModel.get(ModelDataPropertyNames.OBJECT);
                final EntityKind entityKind = EntityKind.SAMPLE;
                if (sampleRegistrationWidget == null)

                {
                    showRegistrationForm(sampleType, entityKind);
                    previousSelection.update(sampleTypeModel);
                } else
                {
                    new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE),
                            viewContext.getMessage(Dict.CONFIRM_CLOSE_MSG))
                        {
                            @Override
                            protected void onYes()
                            {
                                showRegistrationForm(sampleType, entityKind);
                                previousSelection.update(sampleTypeModel);
                            }

                            @Override
                            protected void onNo()
                            {
                                List<SampleTypeModel> selection = new ArrayList<SampleTypeModel>();
                                selection.add(previousSelection.getValue());
                                sampleTypeSelection.disableEvents(true);
                                sampleTypeSelection.setSelection(selection);
                                sampleTypeSelection.disableEvents(false);
                            }
                        }.show();
                }
            }
        }

        private void showRegistrationForm(final SampleType sampleType, final EntityKind entityKind)
        {
            removeAll();
            sampleRegistrationWidget =
                    viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                            sampleType).createClientPlugin(entityKind)
                            .createRegistrationForEntityType(sampleType);
            add(sampleRegistrationWidget);
            layout();
        }

        private class PreviousSelection<T>
        {
            T value;

            void update(T newValue)
            {
                this.value = newValue;
            }

            T getValue()
            {
                return value;
            }
        }
    }
}