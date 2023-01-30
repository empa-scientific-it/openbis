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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.Set;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MaterialTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * The toolbar of material browser.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialBrowserToolbar extends ToolBar implements IDisposableComponent,
        ICriteriaProvider<ListMaterialDisplayCriteria>
{
    public static final String ID = "material-browser-toolbar";

    private final MaterialTypeSelectionWidget selectMaterialTypeCombo;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MaterialBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            String initialMaterialTypeOrNull, String displayTypeIdOrNull)
    {
        this.viewContext = viewContext;
        this.selectMaterialTypeCombo =
                MaterialTypeSelectionWidget.create(viewContext, displayTypeIdOrNull,
                        initialMaterialTypeOrNull, ID);
        display();
    }

    public void setCriteriaChangedListeners(final IDelegatedAction action)
    {
        selectMaterialTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<MaterialTypeModel>()
                    {

                        @Override
                        public void selectionChanged(SelectionChangedEvent<MaterialTypeModel> se)
                        {
                            action.execute();
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.MATERIAL_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(selectMaterialTypeCombo);
    }

    @Override
    public final ListMaterialDisplayCriteria tryGetCriteria()
    {
        final MaterialType selectedType = selectMaterialTypeCombo.tryGetSelectedMaterialType();
        if (selectedType == null)
        {
            return null;
        }
        return ListMaterialDisplayCriteria.createForMaterialType(selectedType);
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
        { createOrDelete(ObjectKind.MATERIAL_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications,
            IDataRefreshCallback entityTypeRefreshCallback)
    {
        if (observedModifications.contains(createOrDelete(ObjectKind.MATERIAL_TYPE))
                || observedModifications
                        .contains(createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT))
                || observedModifications.contains(edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)))
        {
            selectMaterialTypeCombo.refreshStore(entityTypeRefreshCallback);
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
        if (selectMaterialTypeCombo != null)
        {
            selectMaterialTypeCombo.dispose();
        }
    }

}
