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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SpaceModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * The toolbar of sample browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
final class SampleBrowserToolbar extends ToolBar implements ISampleCriteriaProvider,
        IDisposableComponent
{
    public static final String ID = "sample-browser-toolbar";

    private static final String PREFIX = ID + "_";

    public static final String INCLUDE_GROUP_CHECKBOX_ID = GenericConstants.ID_PREFIX + PREFIX
            + "include-group-checkbox";

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final SpaceSelectionWidget selectSpaceCombo;

    private final IViewContext<?> viewContext;

    private final boolean excludeWithoutExperiment;

    public SampleBrowserToolbar(final IViewContext<?> viewContext, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment, String initialSpaceOrNull,
            String initialSampleTypeOrNull, SampleTypeDisplayID sampleTypeDisplayID)
    {
        this.viewContext = viewContext;
        this.excludeWithoutExperiment = excludeWithoutExperiment;
        selectSampleTypeCombo =
                new SampleTypeSelectionWidget(viewContext, ID, true, true, false,
                        initialSampleTypeOrNull, sampleTypeDisplayID);
        selectSpaceCombo =
                new SpaceSelectionWidget(viewContext, ID, addShared, addAll, initialSpaceOrNull);
        display();
    }

    public SampleBrowserToolbar(final IViewContext<?> viewContext, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment,
            SampleTypeDisplayID sampleTypeDisplayID)
    {
        this(viewContext, addShared, addAll, excludeWithoutExperiment, null, null,
                sampleTypeDisplayID);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
        if (selectSpaceCombo != null)
        {
            selectSpaceCombo.dispose();
        }
    }

    @Override
    public ListSampleDisplayCriteria tryGetCriteria()
    {
        final SampleType selectedType = tryGetSelectedSampleType();
        if (selectedType == null)
        {
            return null;
        }
        final Space selectedSpace = selectSpaceCombo.tryGetSelectedSpace();
        if (selectedSpace == null)
        {
            return null;
        }
        boolean sharedSpace = SpaceSelectionWidget.isSharedSpace(selectedSpace);
        boolean allSpaces = SpaceSelectionWidget.isAllSpaces(selectedSpace);
        final boolean includeInstance = sharedSpace || allSpaces;
        final boolean includeSpace = sharedSpace == false;

        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(selectedType);
        criteria.setSpaceCode(SpaceSelectionWidget.tryToGetSpaceCode(selectedSpace));
        criteria.setIncludeSpace(includeSpace);
        criteria.setIncludeInstance(includeInstance);
        criteria.setExcludeWithoutExperiment(excludeWithoutExperiment);
        return new ListSampleDisplayCriteria(criteria);
    }

    private SampleType tryGetSelectedSampleType()
    {
        return selectSampleTypeCombo.tryGetSelectedSampleType();
    }

    public void setCriteriaChangedListeners(final IDelegatedAction action)
    {
        selectSpaceCombo.addSelectionChangedListener(new SelectionChangedListener<SpaceModel>()
            {

                @Override
                public void selectionChanged(SelectionChangedEvent<SpaceModel> se)
                {
                    action.execute();
                }
            });
        selectSampleTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                    {

                        @Override
                        public void selectionChanged(SelectionChangedEvent<SampleTypeModel> se)
                        {
                            action.execute();
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(selectSampleTypeCombo);
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.GROUP) + GenericConstants.LABEL_SEPARATOR));
        add(selectSpaceCombo);
    }

    //
    // ToolBar
    //

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                createOrDelete(ObjectKind.SPACE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback entityTypeRefreshCallback)
    {
        if (containsAnyModificationsOf(observedModifications, ObjectKind.SAMPLE_TYPE)
                || containsAnyModificationsOf(observedModifications,
                        ObjectKind.PROPERTY_TYPE_ASSIGNMENT))
        {
            selectSampleTypeCombo.refreshStore(entityTypeRefreshCallback);
        } else
        {
            entityTypeRefreshCallback.postRefresh(true);
        }
        if (observedModifications.contains(createOrDelete(ObjectKind.SPACE)))
        {
            selectSpaceCombo.refreshStore();
        }

    }

    private boolean containsAnyModificationsOf(Set<DatabaseModificationKind> observedModifications,
            ObjectKind objectKind)
    {
        return observedModifications.contains(createOrDelete(objectKind))
                || observedModifications.contains(edit(objectKind));
    }

}
