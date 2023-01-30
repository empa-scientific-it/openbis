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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractEntityGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.GridUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.DatasetImageOverviewUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid extends AbstractEntityGrid<AbstractExternalData>
{
    public static final String SHOW_DETAILS_BUTTON_ID_SUFFIX = "_show-details-button";

    private final ICriteriaProvider<PropertyTypesCriteria> criteriaProvider;

    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, String gridId, DisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, true, displayTypeIDGenerator);
        this.criteriaProvider = createCriteriaProvider();
        setId(browserId);

        extendBottomToolbar();
        linkProject();
    }

    private void linkProject()
    {
        ICellListenerAndLinkGenerator<AbstractExternalData> listenerAndLinkGenerator = new ICellListenerAndLinkGenerator<AbstractExternalData>()
            {
                @Override
                public void handle(TableModelRowWithObject<AbstractExternalData> rowItem,
                        boolean specialKeyPressed)
                {
                    Project project = tryGetProject(rowItem.getObjectOrNull());
                    final String href = LinkExtractor.tryExtract(project);
                    OpenEntityDetailsTabHelper.open(viewContext, project,
                            specialKeyPressed, href);
                }
                
                @Override
                public String tryGetLink(AbstractExternalData entity,
                        ISerializableComparable comparableValue)
                {
                    Project project = tryGetProject(entity);
                    return project == null ? null : LinkExtractor.tryExtract(project);
                }

                private Project tryGetProject(AbstractExternalData dataSet)
                {
                    Experiment experiment = dataSet.getExperiment();
                    Sample sample = dataSet.getSample();
                    Project project = null;
                    if (experiment != null)
                    {
                        project = experiment.getProject();
                    } else if (sample != null)
                    {
                        project = sample.getProject();
                    }
                    return project;
                }
            };
        registerListenerAndLinkGenerator(ExternalDataGridColumnIDs.PROJECT, listenerAndLinkGenerator);
        registerListenerAndLinkGenerator(ExternalDataGridColumnIDs.PROJECT_IDENTIFIER, listenerAndLinkGenerator);
    }

    @Override
    protected boolean supportsExportForUpdate()
    {
        return true;
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>> callback)
    {
        // TODO Auto-generated method stub

    }

    // adds show, show-details and invalidate buttons
    protected void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        if (viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                    getId() + SHOW_DETAILS_BUTTON_ID_SUFFIX, asShowEntityInvoker(false)));
            addButton(createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                    asShowEntityInvoker(true)));

            addTaggingButtons();

            final String deleteTitle = viewContext.getMessage(Dict.BUTTON_DELETE);
            final String deleteAllTitle = deleteTitle + " All";
            final Button deleteButton =
                    new Button(deleteAllTitle, new AbstractCreateDialogListener()
                        {

                            @Override
                            protected Dialog createDialog(
                                    List<TableModelRowWithObject<AbstractExternalData>> dataSets,
                                    IBrowserGridActionInvoker invoker)
                            {
                                return new DataSetListDeletionConfirmationDialog(viewContext,
                                        createRefreshCallback(invoker),
                                        getSelectedAndDisplayedItemsAction().execute());
                            }
                        });
            changeButtonTitleOnSelectedItems(deleteButton, deleteAllTitle, deleteTitle);
            addButton(deleteButton);
        }

        Button uploadButton =
                new Button(viewContext.getMessage(Dict.BUTTON_UPLOAD_DATASETS),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(
                                        List<TableModelRowWithObject<AbstractExternalData>> dataSets,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new DataSetUploadConfirmationDialog(dataSets,
                                            getSelectedAndDisplayedItemsAction(), getTotalCount(),
                                            viewContext);
                                }
                            });
        if (StringUtils.isBlank(viewContext.getModel().getApplicationInfo().getCifexURL()))
        {
            uploadButton.disable();
        }
        addButton(uploadButton);
        if (viewContext.getModel().getApplicationInfo().isArchivingConfigured()
                && viewContext.isSimpleOrEmbeddedMode() == false)
        {
            addButton(createArchivingMenu());
        }
        addEntityOperationsSeparator();

        allowMultipleSelection();
    }

    protected ICriteriaProvider<PropertyTypesCriteria> getCriteriaProvider()
    {
        return criteriaProvider;
    }

    /*
     * Provides property types which should be shown as the grid columns. Takes into account what types of datasets are displayed and does not show
     * property types which are not assigned to any of those types.
     */
    private ICriteriaProvider<PropertyTypesCriteria> createCriteriaProvider()
    {
        final EntityKind entityKind = getEntityKindOrNull();
        return new PropertyTypesCriteriaProvider(viewContext, entityKind)
            {
                @Override
                public PropertyTypesCriteria tryGetCriteria()
                {
                    PropertyTypesCriteria propertyTypesCriteria = super.tryGetCriteria();
                    return propertyTypesCriteria;
                }
            };
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    private final TextToolItem createArchivingMenu()
    {
        return new DataSetArchivingMenu(viewContext, getSelectedAndDisplayedItemsAction(),
                createRefreshGridAction());
    }

    public final static class SelectedAndDisplayedItems
    {
        // describes all items which are displayed in the grid (including all grid pages)
        private final TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> displayedItemsConfig;

        // currently selected items
        private final List<TableModelRowWithObject<AbstractExternalData>> selectedItems;

        private final int displayedItemsCount;

        public SelectedAndDisplayedItems(List<TableModelRowWithObject<AbstractExternalData>> selectedItems,
                TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> displayedItemsConfig,
                int displayedItemsCount)
        {
            this.displayedItemsConfig = displayedItemsConfig;
            this.selectedItems = selectedItems;
            this.displayedItemsCount = displayedItemsCount;
        }

        public TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> getDisplayedItemsConfig()
        {
            return displayedItemsConfig;
        }

        public int getDisplayedItemsCount()
        {
            return displayedItemsCount;
        }

        public List<TableModelRowWithObject<AbstractExternalData>> getSelectedItems()
        {
            return selectedItems;
        }

        public List<AbstractExternalData> getSelectedDataSets()
        {
            List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
            for (TableModelRowWithObject<AbstractExternalData> item : selectedItems)
            {
                dataSets.add(item.getObjectOrNull());
            }
            return dataSets;
        }

        public DisplayedOrSelectedDatasetCriteria createCriteria(boolean selected)
        {
            if (selected)
            {
                List<TableModelRowWithObject<AbstractExternalData>> items = getSelectedItems();
                List<String> datasetCodes = new ArrayList<String>();
                for (TableModelRowWithObject<AbstractExternalData> row : items)
                {
                    datasetCodes.add(row.getObjectOrNull().getCode());
                }
                return DisplayedOrSelectedDatasetCriteria.createSelectedItems(datasetCodes);
            } else
            {
                return DisplayedOrSelectedDatasetCriteria
                        .createDisplayedItems(getDisplayedItemsConfig());
            }
        }
    }

    public final IDelegatedActionWithResult<SelectedAndDisplayedItems> getSelectedAndDisplayedItemsAction()
    {
        return new IDelegatedActionWithResult<SelectedAndDisplayedItems>()
            {
                @Override
                public SelectedAndDisplayedItems execute()
                {
                    return new SelectedAndDisplayedItems(getSelectedBaseObjects(),
                            createTableExportCriteria(), getTotalCount());
                }
            };
    }

    @Override
    protected ColumnDefsAndConfigs<TableModelRowWithObject<AbstractExternalData>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<AbstractExternalData>> schema =
                super.createColumnsDefinition();
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.REGISTRATOR,
                PersonRenderer.REGISTRATOR_RENDERER);
        schema.setGridCellRendererFor(CommonGridColumnIDs.MODIFIER,
                PersonRenderer.MODIFIER_RENDERER);
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.SHOW_DETAILS_LINK,
                createShowDetailsLinkCellRenderer());
        schema.setGridCellRendererFor(ExternalDataGridColumnIDs.OVERVIEW,
                createOverviewCellRenderer());
        return schema;
    }

    private GridCellRenderer<BaseEntityModel<?>> createOverviewCellRenderer()
    {
        final String sessionID = viewContext.getModel().getSessionContext().getSessionID();
        return new GridCellRenderer<BaseEntityModel<?>>()
            {

                @Override
                public Object render(BaseEntityModel<?> model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
                        Grid<BaseEntityModel<?>> grid)
                {
                    AbstractExternalData dataset = (AbstractExternalData) model.getBaseObject();
                    return tryCreateOverviewLink(dataset);
                }

                private String tryCreateOverviewLink(AbstractExternalData dataset)
                {
                    final String permId = dataset.getPermId();
                    final String dssBaseURL = dataset.getDataStore().getHostUrl();
                    final String typeCode = dataset.getDataSetType().getCode();
                    final Set<String> typePatternsWithImageOverview =
                            getWebClientConfiguration().getDataSetTypePatternsWithImageOverview();
                    if (matches(typePatternsWithImageOverview, typeCode))
                    {
                        return DatasetImageOverviewUtilities.createEmbededImageHtml(dssBaseURL,
                                permId, typeCode, sessionID);
                    } else
                    {
                        return null;
                    }
                }
            };

    }

    private static boolean matches(Set<String> patternsSet, String value)
    {
        for (String pattern : patternsSet)
        {
            if (value.matches(pattern))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ExternalDataGridColumnIDs.CODE,
                ExternalDataGridColumnIDs.FILE_FORMAT_TYPE);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return GridUtils.getRelevantModifications(ObjectKind.DATA_SET, getCriteriaProvider());
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<AbstractExternalData> dataSet,
            boolean editMode, boolean inBackground)
    {
        showEntityInformationHolderViewer(dataSet.getObjectOrNull(), editMode, inBackground);
    }

    @Override
    protected EntityKind getEntityKindOrNull()
    {
        return EntityKind.DATA_SET;
    }
}
