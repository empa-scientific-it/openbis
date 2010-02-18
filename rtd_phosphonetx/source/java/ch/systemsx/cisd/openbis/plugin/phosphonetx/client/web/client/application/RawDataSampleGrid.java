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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GenericTableBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SerializableComparableIDDecorator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleGrid extends GenericTableBrowserGrid
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "raw_data_sample_browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        RawDataSampleGrid grid = new RawDataSampleGrid(viewContext);
        DisposableEntityChooser<GenericTableRow> disposable = grid.asDisposableWithoutToolbar();
        return new DatabaseModificationAwareComponent(disposable.getComponent(), disposable);
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    RawDataSampleGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.RAW_DATA_SAMPLE_BROWSER_GRID);
        specificViewContext = viewContext;
        registerLinkClickListenerFor("CODE", new ICellListener<GenericTableRow>()
            {
                public void handle(GenericTableRow rowItem)
                {
                    showEntityViewer(rowItem, false);
                }
            });
        allowMultipleSelection();
        addEntityOperationsLabel();
        RawDataProcessingMenu button =
                new RawDataProcessingMenu(viewContext,
                        new IDelegatedActionWithResult<List<GenericTableRow>>()
                            {
                                public List<GenericTableRow> execute()
                                {
                                    return getSelectedBaseObjects();
                                }
                            });
        enableButtonOnSelectedItems(button);
        addButton(button);
    }

    @Override
    protected void listTableRows(IResultSetConfig<String, GenericTableRow> resultSetConfig,
            AsyncCallback<GenericTableResultSet> callback)
    {
        specificViewContext.getService().listRawDataSamples(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<GenericTableRow> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportRawDataSamples(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                    createOrDelete(ObjectKind.GROUP),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    @Override
    protected void showEntityViewer(final GenericTableRow entity, boolean editMode)
    {
        showEntityInformationHolderViewer(new IEntityInformationHolder()
            {

                public String getCode()
                {
                    return entity.tryToGetValue(0).toString();
                }

                public Long getId()
                {
                    return ((SerializableComparableIDDecorator) entity.tryToGetValue(0)).getID();
                }

                public BasicEntityType getEntityType()
                {
                    BasicEntityType type = new BasicEntityType();
                    type.setCode("MS_INJECTION");
                    return type;
                }

                public EntityKind getEntityKind()
                {
                    return EntityKind.SAMPLE;
                }
            }, editMode);
    }
}
