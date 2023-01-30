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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AbstractEntityTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddEntityTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Grid displaying material types.
 * 
 * @author Tomasz Pylak
 */
public class MaterialTypeGrid extends AbstractEntityTypeGrid<MaterialType>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "material-type-browser";

    public static final String GRID_ID = BROWSER_ID + TypedTableGrid.GRID_POSTFIX;

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        final MaterialTypeGrid grid = new MaterialTypeGrid(viewContext, componentProvider);
        return grid.asDisposableWithoutToolbar();
    }

    private MaterialTypeGrid(IViewContext<ICommonClientServiceAsync> viewContext, ComponentProvider componentProvider)
    {
        super(viewContext, componentProvider, BROWSER_ID, GRID_ID);
    }

    @Override
    public AddEntityTypeDialog<MaterialType> getNewDialog(MaterialType newType)
    {
        return (AddEntityTypeDialog<MaterialType>) createRegisterEntityTypeDialog("New Material", newType, newType.getEntityKind());
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialType>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<MaterialType>> callback)
    {
        viewContext.getService().listMaterialTypes(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialType>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterialTypes(exportCriteria, callback);
    }

    @Override
    protected void register(MaterialType materialType, AsyncCallback<Void> registrationCallback)
    {
        viewContext.getService().registerMaterialType(materialType, registrationCallback);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected MaterialType createNewEntityType()
    {
        return new MaterialType();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] { createOrDelete(ObjectKind.MATERIAL_TYPE),
                edit(ObjectKind.MATERIAL_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE),
                edit(ObjectKind.PROPERTY_TYPE),
                createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
        };
    }
}
