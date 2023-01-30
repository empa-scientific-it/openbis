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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * The toolbar of experiment browser with a project selected with an external tree.
 * 
 * @author Piotr Buczek
 */
class ExperimentBrowserToolbar extends ToolBar implements IDisposableComponent,
        ICriteriaProvider<ListExperimentsCriteria>
{
    public static final String ID = "experiment-browser-toolbar";

    private final ExperimentTypeSelectionWidget selectExperimentTypeCombo;

    private final ProjectSelectionTreeGridContainer selectProjectTree;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            ProjectSelectionTreeGridContainer tree, String initialExperimentTypeOrNull)
    {
        this.viewContext = viewContext;
        selectExperimentTypeCombo =
                new ExperimentTypeSelectionWidget(viewContext, ID, true,
                        initialExperimentTypeOrNull);
        selectProjectTree = tree;
        display();
    }

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            ProjectSelectionTreeGridContainer tree)
    {
        this(viewContext, tree, null);
    }

    public void setCriteriaChangedListeners(final IDelegatedAction refreshAction)
    {
        selectExperimentTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<ExperimentTypeModel>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<ExperimentTypeModel> se)
                        {
                            refreshAction.execute();
                        }
                    });
        selectProjectTree.setSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    refreshAction.execute();
                }
            });
    }

    protected void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.EXPERIMENT_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(selectExperimentTypeCombo);
    }

    @Override
    public final ListExperimentsCriteria tryGetCriteria()
    {
        final ExperimentType selectedType =
                selectExperimentTypeCombo.tryGetSelectedExperimentType();
        if (selectedType == null)
        {
            return null;
        }
        final Project selectedProjectOrNull = selectProjectTree.tryGetSelectedProject();
        final Space selectedSpaceOrNull = selectProjectTree.tryGetSelectedSpace();
        if (selectedProjectOrNull == null && selectedSpaceOrNull == null)
        {
            return null;
        }
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        criteria.setExperimentType(selectedType);
        criteria.setProject(selectedProjectOrNull);
        criteria.setSpace(selectedSpaceOrNull);
        return criteria;
    }

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { createOrDelete(ObjectKind.EXPERIMENT_TYPE), createOrDelete(ObjectKind.PROJECT),
                edit(ObjectKind.PROJECT), createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback entityTypeRefreshCallback)
    {
        if (observedModifications.contains(createOrDelete(ObjectKind.PROJECT))
                || observedModifications.contains(edit(ObjectKind.PROJECT)))
        {
            selectProjectTree.update(observedModifications);
        } else if (observedModifications.contains(createOrDelete(ObjectKind.EXPERIMENT_TYPE))
                || observedModifications
                        .contains(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT))
                || observedModifications.contains(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)))
        {
            selectExperimentTypeCombo.refreshStore(entityTypeRefreshCallback);
        } else
        {
            entityTypeRefreshCallback.postRefresh(true);
        }
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
        selectExperimentTypeCombo.dispose();
        selectProjectTree.dispose();
    }

}
