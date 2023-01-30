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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * The {@link LayoutContainer} extension for batch registration and update of samples of certain type.
 * 
 * @author Christian Ribeaud
 */
public final class SampleBatchRegisterUpdatePanel extends LayoutContainer
{
    private static final String ID_SUFFIX = "sample-batch-registration";

    private static final String ID = GenericConstants.ID_PREFIX + ID_SUFFIX;

    private final SampleTypeSelectionWidget sampleTypeSelection;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static String getId(final boolean update)
    {
        return ID + "_" + (update == true ? "update" : "registration");
    }

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        SampleBatchRegisterUpdatePanel panel =
                new SampleBatchRegisterUpdatePanel(viewContext, update);
        return new DatabaseModificationAwareComponent(panel, panel.sampleTypeSelection);
    }

    private SampleBatchRegisterUpdatePanel(
            final IViewContext<ICommonClientServiceAsync> viewContext, final boolean update)
    {
        this.viewContext = viewContext;
        setId(getId(update));
        setScrollMode(Scroll.AUTO);
        sampleTypeSelection =
                new SampleTypeSelectionWidget(viewContext, ID_SUFFIX, false, false, true, null,
                        createDisplayID(update));
        final ToolBar toolBar = createToolBar();
        add(toolBar);
        sampleTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                    {

                        //
                        // SelectionChangedListener
                        //

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<SampleTypeModel> se)
                        {
                            final SampleType sampleType =
                                    sampleTypeSelection.tryGetSelectedSampleType();
                            if (sampleType != null)
                            {
                                removeAll();
                                final EntityKind entityKind = EntityKind.SAMPLE;
                                add(toolBar);
                                final IClientPlugin<EntityType, IIdAndCodeHolder> createClientPlugin =
                                        viewContext.getClientPluginFactoryProvider()
                                                .getClientPluginFactory(entityKind, sampleType)
                                                .createClientPlugin(entityKind);
                                final Widget batchOperationWidget;
                                if (update)
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchUpdateForEntityType(sampleType);
                                } else
                                {
                                    batchOperationWidget =
                                            createClientPlugin
                                                    .createBatchRegistrationForEntityType(sampleType);
                                }
                                add(batchOperationWidget);
                                layout();
                            }
                        }
                    });
    }

    private SampleTypeDisplayID createDisplayID(final boolean update)
    {
        return update ? SampleTypeDisplayID.SAMPLE_BATCH_UPDATE
                : SampleTypeDisplayID.SAMPLE_BATCH_REGISTRATION;
    }

    private final ToolBar createToolBar()
    {
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(sampleTypeSelection);
        return toolBar;
    }
}
