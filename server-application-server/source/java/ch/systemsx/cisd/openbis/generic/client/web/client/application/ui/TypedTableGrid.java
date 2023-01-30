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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IGenericImageBundle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ShowRelatedDatasetsDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.GridDisplaySettings;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplaySettingsGetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.MaterialRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.MultilineStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.TimestampStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.VocabularyTermStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core.CustomColumnStringRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ComponentEventLogger.EventPair;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GridCustomColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar.PagingToolBarButtonKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.CustomColumnsMetadataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ExtendedGridView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisplayTypeIDProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IModification;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ModificationsData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.PendingFetchManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.TableExportType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter.FilterToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityEditorTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils.DisplayInfoTime;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Constants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IUpdateResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig.ResultSetFetchMode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * Abstract superclass of all grids based on {@link TypedTableModel}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class TypedTableGrid<T extends Serializable> extends LayoutContainer implements
        IDatabaseModificationObserver, IDisplayTypeIDProvider,
        IColumnDefinitionProvider<TableModelRowWithObject<T>>
{
    private static final IGenericImageBundle IMAGE_BUNDLE = GWT
            .<IGenericImageBundle> create(IGenericImageBundle.class);

    public static final String GRID_POSTFIX = "-grid";

    /**
     * Do not display more than this amount of columns in the report, web browsers have problem with it
     */
    private static final int MAX_SHOWN_COLUMNS = 200;

    /**
     * Called when user wants to export the data. It can happen only after a previous refresh of the data has taken place. The export criteria has
     * only the cache key
     */
    abstract protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<T>> exportCriteria,
            AbstractAsyncCallback<String> callback);

    // --------

    /**
     * If user selected some entities in given browser first a dialog is shown where he can select between showing data sets related to
     * selected/displayed entities. Then a tab is displayed where these related data sets are listed.<br>
     * <br>
     * If no entities were selected in given browser the tab is displayed where data sets related to all entities displayed in the grid are listed.
     */
    protected static final <E extends IEntityInformationHolder> void showRelatedDataSets(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final TypedTableGrid<E> browser)
    {
        final List<TableModelRowWithObject<E>> selectedEntities = browser.getSelectedBaseObjects();
        final TableExportCriteria<TableModelRowWithObject<E>> displayedEntities =
                browser.createTableExportCriteria();
        if (selectedEntities.isEmpty())
        {
            // no entity selected - show datasets related to all displayed
            RelatedDataSetCriteria<E> relatedCriteria =
                    RelatedDataSetCriteria.<E> createDisplayedEntities(displayedEntities);
            ShowRelatedDatasetsDialog.showRelatedDatasetsTab(viewContext, relatedCriteria);
        } else
        {
            // > 0 entity selected - show dialog with all/selected radio
            new ShowRelatedDatasetsDialog<E>(viewContext, selectedEntities, displayedEntities,
                    browser.getTotalCount()).show();
        }
    }

    private final class CellListenerAndLinkGenerator implements ICellListenerAndLinkGenerator<T>
    {
        private final EntityKind entityKind;

        private final TableModelColumnHeader header;

        private CellListenerAndLinkGenerator(EntityKind entityKind, TableModelColumnHeader header)
        {
            this.entityKind = entityKind;
            this.header = header;
        }

        @Override
        public String tryGetLink(T entity, final ISerializableComparable value)
        {
            if (value == null || value.toString().length() == 0)
            {
                return null;
            }
            if (value instanceof EntityTableCell)
            {
                EntityTableCell entityTableCell = (EntityTableCell) value;
                if (entityTableCell.isMissing() || entityTableCell.isFake())
                {
                    return null;
                }
                String permId = entityTableCell.getPermId();
                if (entityTableCell.getEntityKind() == EntityKind.MATERIAL)
                {
                    return LinkExtractor.tryExtract(MaterialIdentifier.tryParseIdentifier(permId));
                } else
                {
                    return LinkExtractor.createPermlink(entityTableCell.getEntityKind(), permId);
                }

            } else if (header.isLinkEntitiesOnly())
            {
                return null;
            }
            return LinkExtractor.createPermlink(entityKind, value.toString());
        }

        @Override
        public void handle(TableModelRowWithObject<T> rowItem, boolean specialKeyPressed)
        {
            ISerializableComparable cellValue = rowItem.getValues().get(header.getIndex());
            if (cellValue instanceof EntityTableCell)
            {
                EntityTableCell entityTableCell = (EntityTableCell) cellValue;
                String permId = entityTableCell.getPermId();
                if (entityTableCell.getEntityKind() == EntityKind.MATERIAL)
                {
                    MaterialIdentifier materialIdentifier =
                            MaterialIdentifier.tryParseIdentifier(permId);
                    OpenEntityDetailsTabHelper.open(viewContext, materialIdentifier,
                            specialKeyPressed);
                } else if (permId.length() != 0)
                {
                    OpenEntityDetailsTabHelper.open(viewContext, entityTableCell.getEntityKind(),
                            permId, specialKeyPressed);
                } else
                {
                    OpenEntityDetailsTabHelper.open(viewContext,
                            new BasicEntityDescription(entityTableCell.getEntityKind(),
                                    entityTableCell.getIdentifierOrNull()), specialKeyPressed);
                }
            } else
            {
                OpenEntityDetailsTabHelper.open(viewContext, entityKind, cellValue.toString(),
                        specialKeyPressed);
            }
        }
    }

    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final ICellListener<TableModelRowWithObject<T>> showEntityViewerLinkClickListener;

    protected final TableModificationsManager tableModificationsManager;

    // ------ private section. NOTE: it should remain unaccessible to subclasses! ---------------

    private static final int PAGE_SIZE = Constants.GRID_PAGE_SIZE;

    // set to true to see some useful debugging messages
    private static final boolean DEBUG = false;

    private final PagingLoader<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> pagingLoader;

    private final ContentPanel contentPanel;

    private final Grid<BaseEntityModel<TableModelRowWithObject<T>>> grid;

    private final ColumnListener<TableModelRowWithObject<T>, BaseEntityModel<TableModelRowWithObject<T>>> columnListener;

    private final boolean refreshAutomatically;

    // the toolbar has the refresh and export buttons besides the paging controls
    private final BrowserGridPagingToolBar pagingToolbar;

    // used to change displayed filter widgets
    private final FilterToolbar<TableModelRowWithObject<T>> filterToolbar;

    private final ToolBar modificationsToolbar;

    private final IDisplayTypeIDGenerator displayTypeIDGenerator;

    // --------- private non-final fields

    // available columns definitions
    private Set<IColumnDefinition<TableModelRowWithObject<T>>> columnDefinitions;

    private final CustomColumnsMetadataProvider customColumnsMetadataProvider;

    // result set key of the last refreshed data
    private String resultSetKeyOrNull;

    // Keeps track of the pending fetch (only tracks 1)
    private final PendingFetchManager pendingFetchManager;

    private IDataRefreshCallback refreshCallback;

    private LayoutContainer bottomToolbars;

    private ColumnModel fullColumnModel;

    private final Map<String, ICellListenerAndLinkGenerator<T>> listenerLinkGenerators =
            new HashMap<String, ICellListenerAndLinkGenerator<T>>();

    private List<TableModelColumnHeader> headers;

    private List<IColumnDefinitionUI<TableModelRowWithObject<T>>> columnUIDefinitions;

    private String downloadURL;

    private Map<String, IColumnDefinition<TableModelRowWithObject<T>>> columnDefinitionsMap;

    private String currentGridDisplayTypeID;

    private List<IColumnDefinitionUI<TableModelRowWithObject<T>>> visibleColDefinitions;

    protected final String gridId;

    protected TypedTableGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this(viewContext, browserId, false, displayTypeIDGenerator);
    }

    /**
     * @param refreshAutomatically should the data be automatically loaded when the grid is rendered for the first time?
     * @param browserId unique id of the browser grid
     */
    protected TypedTableGrid(IViewContext<ICommonClientServiceAsync> viewContext, String browserId,
            boolean refreshAutomatically, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        this.gridId = browserId + GRID_POSTFIX;
        pendingFetchManager = new PendingFetchManager();
        this.displayTypeIDGenerator = displayTypeIDGenerator;
        this.viewContext = viewContext;
        int logID = log("create browser grid " + gridId);
        this.refreshAutomatically = refreshAutomatically;
        this.pagingLoader = createPagingLoader();
        this.customColumnsMetadataProvider = new CustomColumnsMetadataProvider();
        this.grid = createGrid(pagingLoader, gridId);
        // WORKAROUND
        // Lazy loading of rows causes tests using experiment browser fail (selection of
        // project in project tree grid doesn't work).
        // Turning it off for all grids is the safest solution for our system tests framework
        // and should improve GUI speed in development mode a bit.
        grid.setLazyRowRender(0);
        this.pagingToolbar =
                new BrowserGridPagingToolBar(asActionInvoker(), viewContext, PAGE_SIZE, gridId);
        pagingToolbar.bind(pagingLoader);
        this.filterToolbar =
                new FilterToolbar<TableModelRowWithObject<T>>(viewContext, gridId, this,
                        createApplyFiltersDelagator());
        this.tableModificationsManager = new TableModificationsManager();
        this.modificationsToolbar =
                new TableModificationsToolbar(viewContext, tableModificationsManager);

        this.contentPanel = createEmptyContentPanel();
        bottomToolbars = createBottomToolbars(contentPanel, pagingToolbar);
        configureBottomToolbarSyncSize();
        contentPanel.add(grid);
        contentPanel.setBottomComponent(bottomToolbars);
        contentPanel.setHeaderVisible(false);
        columnListener =
                new ColumnListener<TableModelRowWithObject<T>, BaseEntityModel<TableModelRowWithObject<T>>>(
                        grid);
        showEntityViewerLinkClickListener = createShowEntityViewerLinkClickListener();
        registerLinkClickListenerFor(Dict.CODE, showEntityViewerLinkClickListener);
        setLayout(new FitLayout());
        add(contentPanel);

        configureLoggingBetweenEvents(logID);

        grid.addListener(Events.HeaderContextMenu, new Listener<GridEvent<ModelData>>()
            {
                @Override
                public void handleEvent(final GridEvent<ModelData> ge)
                {
                    Menu menu = ge.getMenu();
                    int itemCount = menu.getItemCount();
                    for (int i = 2; i < itemCount; i++)
                    {
                        menu.remove(menu.getItem(2));
                    }
                }
            });
        if (viewContext.getModel().isEmbeddedMode())
        {
            removeButtons(PagingToolBarButtonKind.CONFIG, PagingToolBarButtonKind.REFRESH);
        }
        setId(browserId);
        pagingToolbar.setId(gridId + "-paging-toolbar");
    }

    public void removeButtons(PagingToolBarButtonKind... buttonKinds)
    {
        pagingToolbar.removeButtons(buttonKinds);
    }

    private ICellListener<TableModelRowWithObject<T>> createShowEntityViewerLinkClickListener()
    {
        return new ICellListener<TableModelRowWithObject<T>>()
            {
                @Override
                public void handle(TableModelRowWithObject<T> rowItem, boolean keyPressed)
                {
                    showEntityViewer(rowItem, false, keyPressed);
                }
            };
    }

    private void configureBottomToolbarSyncSize()
    {
        // fixes problems with:
        // - no 'overflow' button when some buttons don't fit into pagingToolbar
        pagingLoader.addListener(Loader.Load, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    pagingToolbar.syncSize();
                }
            });
        // - hidden paging toolbar
        pagingToolbar.addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    contentPanel.syncSize();
                }
            });
        // - bottom toolbar is not resized when new filter row appears
        filterToolbar.addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    contentPanel.syncSize();
                }
            });
    }

    private void configureLoggingBetweenEvents(int logID)
    {
        if (viewContext.isLoggingEnabled())
        {
            ComponentEventLogger logger = new ComponentEventLogger(viewContext, getId());
            logger.prepareLoggingBetweenEvents(contentPanel, EventPair.RENDER);
            logger.prepareLoggingBetweenEvents(this, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(grid, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(contentPanel, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(bottomToolbars, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(filterToolbar, EventPair.LAYOUT);
            logger.prepareLoggingBetweenEvents(pagingToolbar, EventPair.LAYOUT);
            viewContext.logStop(logID);
        }
    }

    private int log(String message)
    {
        return viewContext.log(message + " [" + getId() + "]");
    }

    protected void showEntityInformationHolderViewer(IEntityInformationHolderWithPermId entity,
            boolean editMode, boolean inBackground)
    {
        if (editMode
                && OpenEntityEditorTabClickListener.forbidDeletedEntityModification(viewContext,
                        entity))
        {
            return;
        }
        final EntityKind entityKind = entity.getEntityKind();
        final AbstractTabItemFactory tabView;
        BasicEntityType entityType = entity.getEntityType();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entityType);
        final IClientPlugin<BasicEntityType, IEntityInformationHolderWithPermId> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        if (editMode)
        {
            tabView = createClientPlugin.createEntityEditor(entity);
        } else
        {
            tabView = createClientPlugin.createEntityViewer(entity);
        }
        tabView.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    /** Refreshes the grid without showing the loading progress bar */
    private void refreshGridSilently()
    {
        grid.setLoadMask(false);
        int id = log("refresh silently");
        refresh();
        grid.setLoadMask(true);
        viewContext.logStop(id);
    }

    /**
     * Shows/hides the load mask immediately.
     * 
     * @param loadMask Load mask is shown if <code>true</code> otherwise it is hidden.
     */
    public void setLoadMaskImmediately(boolean loadMask)
    {
        if (grid.isRendered())
        {
            if (loadMask)
            {
                grid.el().mask(GXT.MESSAGES.loadMask_msg());
            } else
            {
                grid.el().unmask();
            }
        }

    }

    /**
     * Registers the specified listener for clicks on links in the specified column.
     * 
     * @param columnID Column ID. Not case sensitive.
     * @param listener Listener handle single clicks.
     */
    protected final void registerLinkClickListenerFor(final String columnID,
            final ICellListener<TableModelRowWithObject<T>> listener)
    {
        if (viewContext.isSimpleOrEmbeddedMode() == false)
        {
            columnListener.registerLinkClickListener(columnID, listener);
        }
    }

    protected final void registerLinkClickListenerForAnyMode(final String columnID,
            final ICellListener<TableModelRowWithObject<T>> listener)
    {
        columnListener.registerLinkClickListener(columnID, listener);
    }

    /**
     * Allows multiple selection instead of single selection.
     */
    protected final void allowMultipleSelection()
    {
        grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
    }

    public void disallowMultipleSelection()
    {
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private List<TableModelRowWithObject<T>> getGridElements()
    {
        List<BaseEntityModel<TableModelRowWithObject<T>>> models = grid.getStore().getModels();
        List<TableModelRowWithObject<T>> elements = new ArrayList<TableModelRowWithObject<T>>();
        for (BaseEntityModel<TableModelRowWithObject<T>> model : models)
        {
            elements.add(model.getBaseObject());
        }
        return elements;
    }

    private IDelegatedAction createApplyFiltersDelagator()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    if (resultSetKeyOrNull != null && pendingFetchManager.hasNoPendingFetch())
                    {
                        ResultSetFetchConfig<String> fetchConfig =
                                ResultSetFetchConfig.createFetchFromCache(resultSetKeyOrNull);

                        SortInfo sortInfo = getGridSortInfo();
                        if (sortInfo != null)
                        {
                            pagingLoader.setSortField(sortInfo.getSortField());
                            pagingLoader.setSortDir(translate(sortInfo.getSortDir()));
                        }

                        reloadData(fetchConfig);
                    }
                }
            };
    }

    /** @return this grid as a disposable component with a specified toolbar at the top. */
    protected DisposableEntityChooser<TableModelRowWithObject<T>> asDisposableWithToolbar(
            final IDisposableComponent toolbar)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolbar.getComponent());
        container.add(this, new RowData(1, 1));

        return asDisposableEntityChooser(container, toolbar);
    }

    /** @return this grid as a disposable component */
    public final DisposableEntityChooser<TableModelRowWithObject<T>> asDisposableWithoutToolbar()
    {
        return asDisposableEntityChooser(this);
    }

    /**
     * @return this grid as a disposable component with a specified toolbar at the top and a tree on the left.
     */
    protected final DisposableEntityChooser<TableModelRowWithObject<T>> asDisposableWithToolbarAndTree(
            final IDisposableComponent toolbar, final Component tree, String headerOrNull)
    {
        // WORKAROUND: BorderLayout causes problems when rendered in a tab
        // We use RowLayout here but we loose the split this way.
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout(Orientation.VERTICAL));
        container.add(toolbar.getComponent(), new RowData(1, -1));

        final LayoutContainer subContainer = new LayoutContainer();
        subContainer.setLayout(new RowLayout(Orientation.HORIZONTAL));
        subContainer.add(tree, new RowData(300, 1));
        setHeader(headerOrNull);
        subContainer.add(this, new RowData(1, 1));
        container.add(subContainer, new RowData(1, 1));

        return asDisposableEntityChooser(container, toolbar);
    }

    protected final void setHeader(String headerOrNull)
    {
        if (headerOrNull != null)
        {
            this.contentPanel.setHeaderVisible(true);
            this.contentPanel.setHeading(headerOrNull);
        } else
        {
            this.contentPanel.setHeaderVisible(false);
        }
    }

    protected final DisposableEntityChooser<TableModelRowWithObject<T>> asDisposableEntityChooser(
            final Component mainComponent, final IDisposableComponent... disposableComponents)
    {
        final TypedTableGrid<T> self = this;
        return new DisposableEntityChooser<TableModelRowWithObject<T>>()
            {
                @Override
                public List<TableModelRowWithObject<T>> getSelected()
                {
                    List<BaseEntityModel<TableModelRowWithObject<T>>> items = getSelectedItems();
                    List<TableModelRowWithObject<T>> result =
                            new ArrayList<TableModelRowWithObject<T>>();
                    for (BaseEntityModel<TableModelRowWithObject<T>> item : items)
                    {
                        result.add(item.getBaseObject());
                    }
                    return result;
                }

                @Override
                public void dispose()
                {
                    debug("dispose a browser");
                    self.disposeCache();
                    for (IDisposableComponent disposableComponent : disposableComponents)
                    {
                        disposableComponent.dispose();
                    }
                }

                @Override
                public Component getComponent()
                {
                    return mainComponent;
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return self.getRelevantModifications();
                }

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    self.update(observedModifications);
                }

            };
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        if (refreshAutomatically)
        {
            int id = log("layout automatically");
            layout();
            viewContext.logStop(id);
            id = log("refresh automatically");
            refresh();
            viewContext.logStop(id);
        }
    }

    private PagingLoader<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> createPagingLoader()
    {
        final RpcProxy<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> proxy =
                new RpcProxy<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>>()
                    {
                        @Override
                        protected void load(
                                Object loadConfig,
                                AsyncCallback<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> callback)
                        {
                            loadData((PagingLoadConfig) loadConfig, callback);
                        }
                    };
        final BasePagingLoader<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> newPagingLoader =
                new BasePagingLoader<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>>(
                        proxy);
        newPagingLoader.setRemoteSort(true);

        return newPagingLoader;
    }

    private void loadData(
            final PagingLoadConfig loadConfig,
            final AsyncCallback<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> callback)
    {
        if (pendingFetchManager.hasNoPendingFetch())
        {
            // this can happen when user wants to sort data - the refresh method is not called
            if (resultSetKeyOrNull == null)
            {
                // data are not yet cached, so we ignore this call - should not really happen
                return;
            }
            pendingFetchManager.pushPendingFetchConfig(ResultSetFetchConfig
                    .createFetchFromCache(resultSetKeyOrNull));
        }
        GridFilters<TableModelRowWithObject<T>> filters = filterToolbar.getFilters();
        final DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig =
                createPagingConfig(loadConfig, filters, getGridDisplayTypeID());
        debug("create a refresh callback " + pendingFetchManager.tryTopPendingFetchConfig());
        final ListEntitiesCallback listCallback =
                new ListEntitiesCallback(viewContext, callback, resultSetConfig);

        listEntities(resultSetConfig, listCallback);
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    void debug(String msg)
    {
        if (DEBUG)
        {
            String text =
                    "[grid: " + getGridDisplayTypeID() + ", cache: " + resultSetKeyOrNull + "] "
                            + msg;
            System.out.println(text);
        }
    }

    private DefaultResultSetConfig<String, TableModelRowWithObject<T>> createPagingConfig(
            PagingLoadConfig loadConfig, GridFilters<TableModelRowWithObject<T>> filters,
            String gridDisplayId)
    {
        int limit = loadConfig.getLimit();
        int offset = loadConfig.getOffset();
        com.extjs.gxt.ui.client.data.SortInfo sortInfo = loadConfig.getSortInfo();

        DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig =
                new DefaultResultSetConfig<String, TableModelRowWithObject<T>>();
        resultSetConfig.setLimit(limit);
        resultSetConfig.setOffset(offset);
        resultSetConfig.setAvailableColumns(columnDefinitions);
        SortInfo translatedSortInfo = translateSortInfo(sortInfo);
        Set<String> columnIDs = getIDsOfColumnsToBeShown();
        resultSetConfig.setIDsOfPresentedColumns(columnIDs);
        resultSetConfig.setSortInfo(translatedSortInfo);
        resultSetConfig.setFilters(filters);
        resultSetConfig.setCacheConfig(pendingFetchManager.tryTopPendingFetchConfig());
        resultSetConfig.setGridDisplayId(gridDisplayId);
        resultSetConfig.setCustomColumnErrorMessageLong(viewContext.getDisplaySettingsManager()
                .isDebuggingModeEnabled());
        return resultSetConfig;
    }

    private Set<String> getIDsOfColumnsToBeShown()
    {
        Set<String> columnIDs = new HashSet<String>();
        DisplaySettingsManager manager = viewContext.getDisplaySettingsManager();
        List<ColumnSetting> columnSettings = manager.getColumnSettings(getGridDisplayTypeID());
        if (columnSettings != null)
        {
            for (ColumnSetting columnSetting : columnSettings)
            {
                if (columnSetting.isHidden() == false)
                {
                    columnIDs.add(columnSetting.getColumnID());
                }
            }
        }
        List<IColumnDefinition<TableModelRowWithObject<T>>> visibleColumns =
                getVisibleColumns(columnDefinitions);
        for (IColumnDefinition<TableModelRowWithObject<T>> definition : visibleColumns)
        {
            columnIDs.add(definition.getIdentifier());
        }
        return columnIDs;
    }

    private static <T> SortInfo translateSortInfo(com.extjs.gxt.ui.client.data.SortInfo sortInfo)
    {
        return translateSortInfo(sortInfo.getSortField(), sortInfo.getSortDir());
    }

    private static <T> SortInfo translateSortInfo(String sortFieldId,
            com.extjs.gxt.ui.client.Style.SortDir sortDir)
    {
        SortInfo sortInfo = new SortInfo();
        sortInfo.setSortField(sortFieldId);
        sortInfo.setSortDir(translate(sortDir));

        return sortInfo;
    }

    private static SortDir translate(com.extjs.gxt.ui.client.Style.SortDir sortDir)
    {
        if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.ASC))
        {
            return SortDir.ASC;
        } else if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.DESC))
        {
            return SortDir.DESC;
        } else if (sortDir.equals(com.extjs.gxt.ui.client.Style.SortDir.NONE))
        {
            return SortDir.NONE;
        } else
        {
            throw new IllegalStateException("unknown sort dir: " + sortDir);
        }
    }

    private static com.extjs.gxt.ui.client.Style.SortDir translate(SortDir sortDir)
    {
        if (sortDir.equals(SortDir.ASC))
        {
            return com.extjs.gxt.ui.client.Style.SortDir.ASC;
        } else if (sortDir.equals(SortDir.DESC))
        {
            return com.extjs.gxt.ui.client.Style.SortDir.DESC;
        } else if (sortDir.equals(SortDir.NONE))
        {
            return com.extjs.gxt.ui.client.Style.SortDir.NONE;
        } else
        {
            throw new IllegalStateException("unknown sort dir: " + sortDir);
        }
    }

    /** @return number of rows in the grid */
    public final int getRowNumber()
    {
        return grid.getStore().getCount();
    }

    public final class ListEntitiesCallback extends
            AbstractAsyncCallback<ResultSet<TableModelRowWithObject<T>>>
    {
        private final AsyncCallback<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> delegate;

        // configuration with which the listing was called
        private DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig;

        private int logID;

        private boolean reloadingPhase;

        public ListEntitiesCallback(
                final IViewContext<?> viewContext,
                final AsyncCallback<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> delegate,
                final DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig)
        {
            super(viewContext);
            this.delegate = delegate;
            this.resultSetConfig = resultSetConfig;
            logID = log("load data");
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void finishOnFailure(final Throwable caught)
        {
            reenableAfterFailure();
            // no need to show error message - it should be shown by DEFAULT_CALLBACK_LISTENER
            caught.printStackTrace();
            delegate.onFailure(caught);
        }

        public final void reenableAfterFailure()
        {
            grid.el().unmask();
            onComplete(false);
            pagingToolbar.enable(); // somehow enabling toolbar is lost in its handleEvent() method
        }

        @Override
        protected void performSuccessActionOrIgnore(final IDelegatedAction successAction)
        {
            if (tableModificationsManager.isTableDirty())
            {
                final Listener<MessageBoxEvent> listener = new Listener<MessageBoxEvent>()
                    {
                        @Override
                        public void handleEvent(MessageBoxEvent me)
                        {
                            if (me.getButtonClicked().getItemId().equals(Dialog.YES))
                            {
                                tableModificationsManager.saveModifications();
                            } else
                            {
                                tableModificationsManager.cancelModifications();
                            }
                            successAction.execute();
                        }
                    };
                final String title =
                        viewContext.getMessage(Dict.CONFIRM_SAVE_TABLE_MODIFICATIONS_DIALOG_TITLE);
                final String msg =
                        viewContext
                                .getMessage(Dict.CONFIRM_SAVE_TABLE_MODIFICATIONS_DIALOG_MESSAGE);
                MessageBox.confirm(title, msg, listener);
            } else
            {
                successAction.execute();
            }
        }

        @Override
        protected final void process(final ResultSet<TableModelRowWithObject<T>> result)
        {
            viewContext.logStop(logID);
            logID = log("process loaded data");
            // save the key of the result, later we can refer to the result in the cache using this
            // key
            String key = result.getResultSetKey();
            saveCacheKey(key);
            GridRowModels<TableModelRowWithObject<T>> rowModels = result.getList();
            boolean partial = result.isPartial();

            if (reloadingPhase)
            {
                reloadingPhase = false;
            } else if (partial)
            {
                reloadingPhase = true;
                BasePagingLoadConfig loadConfig = new BasePagingLoadConfig();
                loadConfig.setLimit(resultSetConfig.getLimit());
                loadConfig.setOffset(resultSetConfig.getOffset());
                SortInfo sortInfo = resultSetConfig.getSortInfo();
                if (sortInfo != null)
                {
                    String sortField = sortInfo.getSortField();
                    if (sortField != null)
                    {
                        loadConfig.setSortField(sortField);
                        loadConfig.setSortDir(translate(sortInfo.getSortDir()));
                    }
                }
                resultSetConfig =
                        createPagingConfig(loadConfig, filterToolbar.getFilters(),
                                resultSetConfig.tryGetGridDisplayId());
                resultSetConfig.setCacheConfig(ResultSetFetchConfig
                        .createFetchFromCacheAndRecompute(key));
                this.reuse();
                listEntities(resultSetConfig, this);
            }
            // convert the result to the model data for the grid control
            final List<BaseEntityModel<TableModelRowWithObject<T>>> models =
                    createModels(rowModels);
            final PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>> loadResult =
                    new BasePagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>(models,
                            resultSetConfig.getOffset(), result.getTotalLength());

            delegate.onSuccess(loadResult);
            pagingToolbar.enableExportButton();
            pagingToolbar.updateDefaultConfigButton(true);

            if (reloadingPhase == false)
            {
                pagingToolbar.enable();
                filterToolbar.refreshColumnFiltersDistinctValues(rowModels
                        .getColumnDistinctValues());
            } else
            {
                pagingToolbar.disableForLoadingRest();
            }
            onComplete(true);

            viewContext.logStop(logID);
        }

        // notify that the refresh is done
        private void onComplete(boolean wasSuccessful)
        {
            pendingFetchManager.popPendingFetch();
            refreshCallback.postRefresh(wasSuccessful);
        }

        private List<BaseEntityModel<TableModelRowWithObject<T>>> createModels(
                final GridRowModels<TableModelRowWithObject<T>> gridRowModels)
        {
            final List<BaseEntityModel<TableModelRowWithObject<T>>> result =
                    new ArrayList<BaseEntityModel<TableModelRowWithObject<T>>>();
            initializeModelCreation();
            for (final GridRowModel<TableModelRowWithObject<T>> entity : gridRowModels)
            {
                BaseEntityModel<TableModelRowWithObject<T>> model = createModel(entity);
                result.add(model);
            }
            return result;
        }

        private void initializeModelCreation()
        {
            Set<String> visibleColumnIds = getIDsOfVisibleColumns();
            List<IColumnDefinitionUI<TableModelRowWithObject<T>>> colDefinitions =
                    createColDefinitions();
            visibleColDefinitions =
                    new ArrayList<IColumnDefinitionUI<TableModelRowWithObject<T>>>();
            for (IColumnDefinitionUI<TableModelRowWithObject<T>> definition : colDefinitions)
            {
                if (visibleColumnIds.contains(definition.getIdentifier()))
                {
                    visibleColDefinitions.add(definition);
                }
            }
        }

        @Override
        /* Note: we want to differentiate between callbacks in different subclasses of this grid. */
        public String getCallbackId()
        {
            return grid.getId();
        }
    }

    private Set<String> getIDsOfVisibleColumns()
    {
        Set<String> visibleColumnIds = new HashSet<String>();
        for (int i = 0, n = fullColumnModel.getColumnCount(); i < n; i++)
        {
            ColumnConfig column = fullColumnModel.getColumn(i);
            if (column.isHidden() == false)
            {
                visibleColumnIds.add(column.getId());
            }
        }
        return visibleColumnIds;
    }

    protected String getDefaultFileName()
    {
        return getGridDisplayTypeID().toLowerCase();
    }

    // wraps this browser into the interface appropriate for the toolbar. If this class would just
    // implement the interface it could be very confusing for the code reader.
    protected IBrowserGridActionInvoker asActionInvoker()
    {
        final TypedTableGrid<T> delegate = this;
        return new IBrowserGridActionInvoker()
            {

                @Override
                public boolean supportsExportForUpdate()
                {
                    return delegate.supportsExportForUpdate();
                }

                @Override
                public void export(final TableExportType type)
                {
                    String defaultFileName = getDefaultFileName() + ".tsv";
                    FormPanel form = new FormPanel();
                    form.setHeaderVisible(false);
                    form.setBorders(false);
                    form.setBodyBorder(false);
                    form.setFieldWidth(450);
                    final TextField<String> fileNameField = new TextField<String>();
                    fileNameField.setFieldLabel("File name");
                    fileNameField.setSelectOnFocus(true);
                    fileNameField.setAutoValidate(true);
                    fileNameField.setValue(defaultFileName);
                    fileNameField.setWidth(300);
                    form.add(fileNameField);
                    final SimpleDialog dialog =
                            new SimpleDialog(form, "Export File Name", "Export", viewContext);
                    dialog.setFocusWidget(fileNameField);
                    final IDelegatedAction acceptAction = new IDelegatedAction()
                        {
                            @Override
                            public void execute()
                            {
                                delegate.export(type, fileNameField.getValue());
                            }
                        };
                    dialog.setAcceptAction(acceptAction);
                    dialog.setEnableOfAcceptButton(true);
                    fileNameField.addKeyListener(new EnterKeyListener()
                        {
                            @Override
                            protected void onEnterKey()
                            {
                                dialog.hide();
                                acceptAction.execute();
                            }
                        });
                    dialog.setSize(600, 120);
                    dialog.show();
                }

                @Override
                public void refresh()
                {
                    int id = log("refresh in action invoker");
                    delegate.refresh();
                    viewContext.logStop(id);
                }

                @Override
                public void configure()
                {
                    delegate.configureColumnSettings();
                }

                @Override
                public void toggleFilters(boolean show)
                {
                    if (show)
                    {
                        int logId = log("adding filters");
                        delegate.showFiltersBar();
                        viewContext.logStop(logId);
                    } else
                    {
                        int logId = log("removing filters");
                        bottomToolbars.remove(filterToolbar);
                        bottomToolbars.layout();
                        viewContext.logStop(logId);
                    }
                }

            };
    }

    protected boolean supportsExportForUpdate()
    {
        return false;
    }

    protected void showFiltersBar()
    {
        // always show filters under modifications
        int position = bottomToolbars.getItemCount() > 1 ? 1 : 0;
        bottomToolbars.insert(filterToolbar, position);
        bottomToolbars.layout();
    }

    private void showModificationsBar()
    {
        if (bottomToolbars.getItems().contains(modificationsToolbar) == false)
        {
            GWTUtils.displayInfo(viewContext.getMessage(Dict.TABLE_MODIFICATIONS_INFO_TITLE),
                    viewContext.getMessage(Dict.TABLE_MODIFICATIONS_INFO_TEXT),
                    DisplayInfoTime.LONG);
            bottomToolbars.insert(modificationsToolbar, 0);
            bottomToolbars.layout();
        }
    }

    private void hideModificationsBar()
    {
        bottomToolbars.remove(modificationsToolbar);
        bottomToolbars.layout();
    }

    protected static interface ISelectedEntityInvoker<M>
    {
        void invoke(M selectedItem, boolean keyPressed);
    }

    protected final ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>> asShowEntityInvoker(
            final boolean editMode)
    {
        return new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>>()
            {
                @Override
                public void invoke(BaseEntityModel<TableModelRowWithObject<T>> selectedItem,
                        boolean keyPressed)
                {
                    if (selectedItem != null)
                    {
                        showEntityViewer(selectedItem.getBaseObject(), editMode, keyPressed);
                    }
                }
            };
    }

    private ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>> createNotImplementedInvoker()
    {
        return new ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>>()
            {
                @Override
                public void invoke(BaseEntityModel<TableModelRowWithObject<T>> selectedItem,
                        boolean keyPressed)
                {
                    GWTUtils.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING),
                            viewContext.getMessage(Dict.NOT_IMPLEMENTED));
                }
            };
    }

    /**
     * @return a button which has no action but is enabled only when one entity in the grid is selected. Useful only for writing prototypes.
     */
    protected final Button createSelectedItemDummyButton(final String title)
    {
        return createSelectedItemButton(title, createNotImplementedInvoker());
    }

    /**
     * @return like {@link #createSelectedItemButton(String, ISelectedEntityInvoker)} with button id set
     */
    protected final Button createSelectedItemButton(final String title, final String id,
            final ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>> invoker)
    {
        final Button button = createSelectedItemButton(title, invoker);
        button.setId(id);
        return button;
    }

    /**
     * @return a button which is enabled only when one entity in the grid is selected. When button is pressed, the specified invoker action is
     *         performed.
     */
    protected final Button createSelectedItemButton(final String title,
            final ISelectedEntityInvoker<BaseEntityModel<TableModelRowWithObject<T>>> invoker)
    {
        final Button button = new Button(title, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    List<BaseEntityModel<TableModelRowWithObject<T>>> selectedItems =
                            getSelectedItems();
                    if (selectedItems.isEmpty() == false)
                    {
                        invoker.invoke(selectedItems.get(0), false);
                    }
                }
            });
        enableButtonOnSelectedItem(button);
        return button;
    }

    /**
     * @return a button which is enabled only when at least one entity in the grid is selected, and with specified selection listener set.
     */
    protected final Button createSelectedItemsButton(final String title,
            SelectionListener<ButtonEvent> listener)
    {
        Button button = new Button(title);
        button.addSelectionListener(listener);
        enableButtonOnSelectedItems(button);
        return button;
    }

    /** adds given <var>button</var> to grid {@link PagingToolBar} */
    protected final void addButton(Button button)
    {
        pagingToolbar.add(button);
    }

    /**
     * Given <var>button</var> will be enabled only if at least one item is selected in the grid.
     */
    protected final void enableButtonOnSelectedItems(final Button button)
    {
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean enabled = se.getSelection().size() > 0;
                    button.setEnabled(enabled);
                }

            });
    }

    /**
     * Given <var>button</var> will be enabled only if exactly one item is selected in the grid.
     */
    protected final void enableButtonOnSelectedItem(final Button button)
    {
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean enabled = se.getSelection().size() == 1;
                    button.setEnabled(enabled);
                }

            });
    }

    /**
     * Given <var>button</var> will have title changed depending on number of items selected in the grid.
     */
    protected final void changeButtonTitleOnSelectedItems(final Button button,
            final String noSelectedItemsTitle, final String selectedItemsTitle)
    {
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {
                    boolean noSelected = se.getSelection().size() == 0;
                    button.setText(noSelected ? noSelectedItemsTitle : selectedItemsTitle);
                }

            });
    }

    public void addGridSelectionChangeListener(Listener<SelectionChangedEvent<ModelData>> listener)
    {
        grid.getSelectionModel().addListener(Events.SelectionChange, listener);
    }

    /**
     * Returns all models of selected items or an empty list if nothing selected.
     */
    public final List<BaseEntityModel<TableModelRowWithObject<T>>> getSelectedItems()
    {
        return grid.getSelectionModel().getSelectedItems();
    }

    /**
     * Returns all base objects of selected items or an empty list if nothing selected.
     */
    protected final List<TableModelRowWithObject<T>> getSelectedBaseObjects()
    {
        List<BaseEntityModel<TableModelRowWithObject<T>>> items = getSelectedItems();
        List<TableModelRowWithObject<T>> data = new ArrayList<TableModelRowWithObject<T>>();
        for (BaseEntityModel<TableModelRowWithObject<T>> item : items)
        {
            data.add(item.getBaseObject());
        }
        return data;
    }

    protected final IDelegatedAction createRefreshGridAction()
    {
        return createRefreshGridAction(null);
    }

    protected final IDelegatedAction createRefreshGridAction(final IDataRefreshCallback externalDataRefreshCallbackOrNull)
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    int id = log("execute refresh grid action");
                    refresh(externalDataRefreshCallbackOrNull);
                    viewContext.logStop(id);
                }
            };
    }

    protected final IDelegatedAction createRefreshGridSilentlyAction()
    {
        return new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    int id = log("execute refresh grid silently action");
                    TypedTableGrid.this.refreshGridSilently();
                    viewContext.logStop(id);
                }
            };
    }

    /** Refreshes grid and filters (resets filter selection) */
    protected final void refreshGridWithFilters()
    {
        // N.B. -- The order in which things are refreshed and configured is significant

        // export and config buttons are enabled when ListEntitiesCallback is complete
        pagingToolbar.disableExportButton();
        pagingToolbar.updateDefaultConfigButton(false);

        // Need to reset filter fields *before* refreshing the grid so the list can
        // be correctly retrieved
        filterToolbar.resetFilterFields();
        filterToolbar.resetFilterSelectionWithoutApply();

        int id = log("refresh grid with filters");
        refresh();
        viewContext.logStop(id);

        // Need to refresh the filter toolbar *after* refreshing the grid, because it
        // has a dependency on information from the grid that gets updated with the refresh
        filterToolbar.refresh();
    }

    protected final void updateDefaultRefreshButton()
    {
        boolean isEnabled = isRefreshEnabled();
        pagingToolbar.updateDefaultRefreshButton(isEnabled);
    }

    /**
     * Refreshes the grid.
     * <p>
     * Note that, doing so, the result set associated on the server side will be removed.
     * </p>
     */
    protected final void refresh(boolean refreshColumnsDefinition)
    {
        refresh(null, refreshColumnsDefinition);
    }

    /**
     * @param externalRefreshCallbackOrNull external class can define it's own refresh callback method. It will be merged with the internal one.
     */
    protected final void refresh(final IDataRefreshCallback externalRefreshCallbackOrNull,
            boolean refreshColumnsDefinition)
    {
        int id = log("refresh (refreshColumnsDefinition=" + refreshColumnsDefinition + ")");
        pagingToolbar.updateDefaultRefreshButton(false);

        SortInfo sortInfo = getGridSortInfo();
        if (sortInfo != null)
        {
            pagingLoader.setSortField(sortInfo.getSortField());
            pagingLoader.setSortDir(translate(sortInfo.getSortDir()));
        }

        debug("clean cache for refresh");
        this.refreshCallback = createRefreshCallback(externalRefreshCallbackOrNull);
        if (columnDefinitions == null || refreshColumnsDefinition)
        {
            recreateColumnModelAndRefreshColumnsWithFilters();
        }
        reloadData(createDisposeAndRefreshFetchMode());
        viewContext.logStop(id);
    }

    private ResultSetFetchConfig<String> createDisposeAndRefreshFetchMode()
    {
        if (resultSetKeyOrNull != null)
        {
            return ResultSetFetchConfig.createClearComputeAndCache(resultSetKeyOrNull);
        } else
        {
            return ResultSetFetchConfig.createComputeAndCache();
        }
    }

    private ColumnDefsAndConfigs<TableModelRowWithObject<T>> createColumnDefsAndConfigs()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<T>> defsAndConfigs = createColumnsDefinition();
        // add custom columns
        List<GridCustomColumnInfo> customColumnsMetadata =
                customColumnsMetadataProvider.getCustomColumnsMetadata();
        if (customColumnsMetadata.size() > 0)
        {
            List<IColumnDefinitionUI<TableModelRowWithObject<T>>> customColumnsDefs =
                    createCustomColumnDefinitions(customColumnsMetadata);
            defsAndConfigs.addColumns(customColumnsDefs, viewContext);

            for (GridCustomColumnInfo gridCustomColumnInfo : customColumnsMetadata)
            {

                DataTypeCode columnType = gridCustomColumnInfo.getDataType();
                GridCellRenderer<BaseEntityModel<?>> columnRenderer = null;

                if (DataTypeCode.REAL.equals(columnType))
                {
                    columnRenderer =
                            new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                                    .getRealNumberFormatingParameters());

                } else if (DataTypeCode.VARCHAR.equals(columnType))
                {
                    columnRenderer = new CustomColumnStringRenderer();
                }

                if (columnRenderer != null)
                {
                    defsAndConfigs.setGridCellRendererFor(gridCustomColumnInfo.getCode(),
                            columnRenderer);
                }
            }
        }

        return defsAndConfigs;
    }

    protected final void recreateColumnModelAndRefreshColumnsWithFilters()
    {
        int logId = log("recreateColumnModelAndRefreshColumnsWithFilters");

        ColumnDefsAndConfigs<TableModelRowWithObject<T>> defsAndConfigs =
                createColumnDefsAndConfigs();

        this.columnDefinitions = defsAndConfigs.getColumnDefs();
        ColumnModel columnModel = createColumnModel(defsAndConfigs.getColumnConfigs());

        refreshColumnsAndFilters(columnModel);

        viewContext.logStop(logId);
    }

    private static <T> List<IColumnDefinitionUI<T>> createCustomColumnDefinitions(
            List<GridCustomColumnInfo> customColumnsMetadata)
    {
        List<IColumnDefinitionUI<T>> defs = new ArrayList<IColumnDefinitionUI<T>>();
        for (GridCustomColumnInfo columnMetadata : customColumnsMetadata)
        {
            IColumnDefinitionUI<T> colDef = new GridCustomColumnDefinition<T>(columnMetadata);
            defs.add(colDef);
        }
        return defs;
    }

    private void refreshColumnsAndFilters(ColumnModel columnModel)
    {
        ColumnModel newColumnModel = columnModel;
        GridDisplaySettings settings = tryApplyDisplaySettings(newColumnModel);
        if (settings != null && settings.getColumnConfigs() != null)
        {
            newColumnModel = createColumnModel(settings.getColumnConfigs());
            rebuildFiltersFromIds(settings.getFilteredColumnIds());
        } else
        {
            filterToolbar.rebuildColumnFilters(getInitialFilters());
        }
        changeColumnModel(newColumnModel, settings != null ? settings.getSortField() : null,
                settings != null ? settings.getSortDir() : null);
    }

    private void hideLoadingMask()
    {
        if (grid.isRendered() && grid.el() != null)
        {
            grid.el().unmask();
        }
    }

    private GridDisplaySettings tryApplyDisplaySettings(ColumnModel columnModel)
    {
        List<IColumnDefinition<TableModelRowWithObject<T>>> initialFilters = getInitialFilters();
        return viewContext.getDisplaySettingsManager().tryApplySettings(getGridDisplayTypeID(),
                columnModel, extractColumnIds(initialFilters), getGridSortInfo());
    }

    private void reconfigureGrid(ColumnModel columnModelOfVisible)
    {
        List<Listener<?>> sortlisteners =
                new ArrayList<Listener<?>>(grid.getListeners(Events.SortChange));
        for (Listener<?> listener : sortlisteners)
        {
            grid.removeListener(Events.SortChange, listener);
        }

        grid.reconfigure(grid.getStore(), columnModelOfVisible);

        for (Listener<?> listener : sortlisteners)
        {
            grid.addListener(Events.SortChange, listener);
        }
    }

    private void changeColumnModel(ColumnModel columnModel, String sortField, SortDir sortDir)
    {
        fullColumnModel = columnModel;

        int logId = log("grid reconfigure");
        ColumnModel columnModelOfVisible = trimToVisibleColumns(columnModel);

        if (sortDir != null && sortField != null)
        {
            pagingLoader.setSortDir(translate(sortDir));
            pagingLoader.setSortField(sortField);
        }
        reconfigureGrid(columnModelOfVisible);

        viewContext.logStop(logId);
        registerGridSettingsChangesListener();
        // add listeners of full column model to trimmed model
        List<Listener<? extends BaseEvent>> listeners =
                fullColumnModel.getListeners(Events.WidthChange);
        for (Listener<? extends BaseEvent> listener : listeners)
        {
            columnModelOfVisible.addListener(Events.WidthChange, listener);
            columnModelOfVisible.addListener(Events.ColumnMove, listener); // track drag&drop
        }
    }

    private ColumnModel trimToVisibleColumns(ColumnModel columnModel)
    {
        int maxVisibleColumns = getWebClientConfiguration().getMaxVisibleColumns();
        int counter = 0;
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        for (int i = 0, n = columnModel.getColumnCount(); i < n; i++)
        {
            ColumnConfig column = columnModel.getColumn(i);
            if (column.isHidden() == false)
            {
                counter++;
                if (counter <= maxVisibleColumns)
                {
                    columns.add(column);
                } else
                {
                    column.setHidden(true);
                }
            }
        }
        if (counter > maxVisibleColumns)
        {
            saveColumnDisplaySettings(); // save changes made to full model
            InfoConfig infoConfig =
                    new InfoConfig(viewContext.getMessage(Dict.VISIBLE_COLUMNS_LIMITED_TITLE),
                            viewContext.getMessage(Dict.VISIBLE_COLUMNS_LIMITED_MSG,
                                    maxVisibleColumns, counter));
            infoConfig.height = 100; // a bit higher
            infoConfig.display = 5000; // 5s
            Info.display(infoConfig);
        }
        ColumnModel trimmedModel = createColumnModel(columns);
        return trimmedModel;
    }

    private void registerGridSettingsChangesListener()
    {
        viewContext.getDisplaySettingsManager().registerGridSettingsChangesListener(
                getGridDisplayTypeID(), createDisplaySettingsUpdater());
    }

    // Refreshes the data, does not clear the cache. Does not change the column model.
    // Default visibility so that friend classes can use -- should otherwise be considered private
    void reloadData(ResultSetFetchConfig<String> resultSetFetchConfig)
    {
        if (pendingFetchManager.hasPendingFetch())
        {
            debug("Cannot reload the data with the mode '" + resultSetFetchConfig
                    + "'; there is an unfinished request already: "
                    + pendingFetchManager.tryTopPendingFetchConfig());
            return;
        }

        pendingFetchManager.pushPendingFetchConfig(resultSetFetchConfig);
        pagingLoader.load(0, PAGE_SIZE);
    }

    private IDisplaySettingsGetter createDisplaySettingsUpdater()
    {
        return new IDisplaySettingsGetter()
            {
                @Override
                public ColumnModel getColumnModel()
                {
                    return TypedTableGrid.this.getFullColumnModel();
                }

                @Override
                public List<String> getFilteredColumnIds()
                {
                    return filterToolbar.extractFilteredColumnIds();
                }

                @Override
                public Object getModifier()
                {
                    return TypedTableGrid.this;
                }

                @Override
                public SortInfo getSortState()
                {
                    return TypedTableGrid.this.getGridSortInfo();
                }
            };
    }

    // returns true if some filters have changed
    // Default visibility so that friend classes can use -- should otherwise be considered private
    boolean rebuildFiltersFromIds(List<String> filteredColumnIds)
    {
        List<IColumnDefinition<TableModelRowWithObject<T>>> filteredColumns =
                getColumnDefinitions(filteredColumnIds);
        return filterToolbar.rebuildColumnFilters(filteredColumns);
    }

    @Override
    public List<IColumnDefinition<TableModelRowWithObject<T>>> getColumnDefinitions(
            List<String> columnIds)
    {
        Map<String, IColumnDefinition<TableModelRowWithObject<T>>> colsMap =
                asColumnIdMap(columnDefinitions);
        List<IColumnDefinition<TableModelRowWithObject<T>>> columns =
                new ArrayList<IColumnDefinition<TableModelRowWithObject<T>>>();
        for (String columnId : columnIds)
        {
            IColumnDefinition<TableModelRowWithObject<T>> colDef = colsMap.get(columnId);
            assert colDef != null : "Cannot find a column '" + columnId;
            columns.add(colDef);
        }
        return columns;
    }

    protected final String createGridDisplayTypeID(String suffixOrNull)
    {
        if (displayTypeIDGenerator == null)
        {
            throw new IllegalStateException("Undefined display type ID generator.");
        }
        if (suffixOrNull == null)
        {
            return displayTypeIDGenerator.createID();
        } else
        {
            return displayTypeIDGenerator.createID(suffixOrNull);
        }
    }

    protected EntityKind getEntityKindOrNull()
    {
        return null;
    }

    private IDataRefreshCallback createRefreshCallback(
            IDataRefreshCallback externalRefreshCallbackOrNull)
    {
        IDataRefreshCallback internalCallback = createInternalPostRefreshCallback();
        if (externalRefreshCallbackOrNull == null)
        {
            return internalCallback;
        } else
        {
            return mergeCallbacks(internalCallback, externalRefreshCallbackOrNull);
        }
    }

    private IDataRefreshCallback createInternalPostRefreshCallback()
    {
        return new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    if (customColumnsMetadataProvider.getHasChangedAndSetFalse())
                    {
                        recreateColumnModelAndRefreshColumnsWithFilters();
                    }

                    updateDefaultRefreshButton();

                    if (wasSuccessful)
                    {
                        hideLoadingMask();
                        pagingToolbar.updateDefaultConfigButton(true);
                        pagingToolbar.enableExportButton();
                    }
                }
            };
    }

    private static IDataRefreshCallback mergeCallbacks(final IDataRefreshCallback c1,
            final IDataRefreshCallback c2)
    {
        return new IDataRefreshCallback()
            {
                @Override
                public void postRefresh(boolean wasSuccessful)
                {
                    c1.postRefresh(wasSuccessful);
                    c2.postRefresh(wasSuccessful);
                }
            };
    }

    private List<IColumnDefinition<TableModelRowWithObject<T>>> getVisibleColumns(
            Set<IColumnDefinition<TableModelRowWithObject<T>>> availableColumns)
    {
        Map<String, IColumnDefinition<TableModelRowWithObject<T>>> availableColumnsMap =
                asColumnIdMap(availableColumns);
        return getVisibleColumns(availableColumnsMap, fullColumnModel);
    }

    private List<IColumnDefinition<TableModelRowWithObject<T>>> getNonCustomColumns(
            Set<IColumnDefinition<TableModelRowWithObject<T>>> availableColumns)
    {

        ArrayList<IColumnDefinition<TableModelRowWithObject<T>>> nonCustom =
                new ArrayList<IColumnDefinition<TableModelRowWithObject<T>>>();

        for (IColumnDefinition<TableModelRowWithObject<T>> c : availableColumns)
        {
            if (c.isCustom() == false)
            {
                nonCustom.add(c);
            }
        }

        return nonCustom;
    }

    private void saveCacheKey(final String newResultSetKey)
    {
        resultSetKeyOrNull = newResultSetKey;
        debug("saving new cache key");
    }

    protected void disposeCache()
    {
        removeResultSet(resultSetKeyOrNull);
        resultSetKeyOrNull = null;
    }

    protected void removeResultSet(String resultSetKey)
    {
        if (resultSetKey != null)
        {
            viewContext.getService().removeResultSet(resultSetKey,
                    new VoidAsyncCallback<Void>(viewContext));
        }
    }

    /**
     * Export always deals with data from the previous refresh operation
     * 
     * @param allColumns whether all columns should be exported
     */
    private void export(TableExportType type, String fileName)
    {
        export(type, new ExportEntitiesCallback(viewContext, fileName));
    }

    /**
     * Shows the dialog allowing to configure visibility and order of the table columns.
     */
    private void configureColumnSettings()
    {
        assert grid != null && grid.getColumnModel() != null : "Grid must be loaded";
        ColumnSettingsConfigurer<T> columnSettingsConfigurer =
                new ColumnSettingsConfigurer<T>(this, viewContext, filterToolbar,
                        customColumnsMetadataProvider, resultSetKeyOrNull,
                        pendingFetchManager.tryTopPendingFetchConfig());
        columnSettingsConfigurer.showDialog();
    }

    private void saveColumnDisplaySettings()
    {
        IDisplaySettingsGetter settingsUpdater = createDisplaySettingsUpdater();
        viewContext.getDisplaySettingsManager().storeSettings(getGridDisplayTypeID(),
                settingsUpdater, false);
    }

    // @Private - for tests
    public final void export(TableExportType type, final AbstractAsyncCallback<String> callback)
    {
        final TableExportCriteria<TableModelRowWithObject<T>> exportCriteria =
                createTableExportCriteria(type);

        prepareExportEntities(exportCriteria, callback);
    }

    // for visible columns
    protected final TableExportCriteria<TableModelRowWithObject<T>> createTableExportCriteria()
    {
        return createTableExportCriteria(TableExportType.VISIBLE);
    }

    private final TableExportCriteria<TableModelRowWithObject<T>> createTableExportCriteria(
            TableExportType type)
    {
        assert columnDefinitions != null : "refresh before exporting!";
        assert resultSetKeyOrNull != null : "refresh before exporting, resultSetKey is null!";

        final List<IColumnDefinition<TableModelRowWithObject<T>>> columnDefs =
                TableExportType.VISIBLE.equals(type) ? getVisibleColumns(columnDefinitions)
                        : (TableExportType.FOR_UPDATE.equals(type) ? getNonCustomColumns(columnDefinitions)
                                : new ArrayList<IColumnDefinition<TableModelRowWithObject<T>>>(
                                        columnDefinitions));
        SortInfo sortInfo = getGridSortInfo();
        EntityKind entityKindForUpdateOrNull =
                TableExportType.FOR_UPDATE.equals(type) ? getEntityKindOrNull() : null;
        final TableExportCriteria<TableModelRowWithObject<T>> exportCriteria =
                new TableExportCriteria<TableModelRowWithObject<T>>(resultSetKeyOrNull, sortInfo,
                        filterToolbar.getFilters(), entityKindForUpdateOrNull, columnDefs,
                        columnDefinitions, getGridDisplayTypeID());
        return exportCriteria;
    }

    // returns info about sorting in current grid
    public SortInfo getGridSortInfo()
    {
        ListStore<BaseEntityModel<TableModelRowWithObject<T>>> store = grid.getStore();
        return translateSortInfo(store.getSortField(), store.getSortDir());
    }

    /** @return the number of all objects cached in the browser */
    public int getTotalCount()
    {
        return pagingToolbar.getTotalCount();
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    void refreshColumnsSettings()
    {
        grid.setLoadMask(false);
        grid.getView().refresh(true);
        grid.setLoadMask(true);
    }

    protected final void addEntityOperationsLabel()
    {
        pagingToolbar.addEntityOperationsLabel();
    }

    protected final void addEntityOperationsSeparator()
    {
        pagingToolbar.add(new SeparatorToolItem());
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createMultilineStringCellRenderer()
    {
        return new MultilineStringCellRenderer();
    }

    @SuppressWarnings("deprecation")
    protected GridCellRenderer<BaseEntityModel<?>> createInternalLinkCellRenderer()
    {
        // NOTE: this renderer doesn't support special rendering of deleted entities
        return LinkRenderer.createLinkRenderer();
    }

    protected WebClientConfiguration getWebClientConfiguration()
    {
        return viewContext.getModel().getApplicationInfo().getWebClientConfiguration();
    }

    // ------- generic static helpers

    private static <T> List<String> extractColumnIds(List<IColumnDefinition<T>> columns)
    {
        List<String> columnsIds = new ArrayList<String>();
        for (IColumnDefinition<T> column : columns)
        {
            columnsIds.add(column.getIdentifier());
        }
        return columnsIds;
    }

    // Default visibility so that friend classes can use -- should otherwise be considered private
    static List<ColumnDataModel> createColumnsSettingsModel(ColumnModel cm,
            List<String> filteredColumnsIds)
    {
        Set<String> filteredColumnsMap = new HashSet<String>(filteredColumnsIds);
        int cols = cm.getColumnCount();
        List<ColumnDataModel> list = new ArrayList<ColumnDataModel>();
        for (int i = 0; i < cols; i++)
        {
            if (cm.getColumnHeader(i) == null || cm.getColumnHeader(i).equals("") || cm.isFixed(i))
            {
                continue;
            }
            String columnId = cm.getColumnId(i);
            boolean isVisible = cm.isHidden(i) == false;
            boolean hasFilter = filteredColumnsMap.contains(columnId);
            list.add(new ColumnDataModel(cm.getColumnHeader(i), isVisible, hasFilter, columnId));
        }
        return list;
    }

    private static ContentPanel createEmptyContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.setBorders(false);
        contentPanel.setBodyBorder(false);
        contentPanel.setLayout(new FitLayout());
        return contentPanel;
    }

    // creates filter and paging toolbars
    private static <T> LayoutContainer createBottomToolbars(final Container<?> parentContainer,
            ToolBar pagingToolbar)
    {
        LayoutContainer bottomToolbars = new ContainerKeeper(parentContainer);
        bottomToolbars.setMonitorWindowResize(true);
        bottomToolbars.setLayout(new RowLayout(com.extjs.gxt.ui.client.Style.Orientation.VERTICAL));
        // Adding paging toolbar before data are loaded fixes problems with the toolbar not visible
        // if user quickly changes tab / hides section before it is layouted. On the other hand
        // it is slower than adding it after requesting server for data.
        bottomToolbars.add(pagingToolbar, new RowData(1, -1));
        // filter toolbar is added on request
        return bottomToolbars;
    }

    private static final class ContainerKeeper extends LayoutContainer
    {
        private final Container<?> parentContainer;

        private ContainerKeeper(Container<?> parentContainer)
        {
            this.parentContainer = parentContainer;
        }

        @Override
        protected void onWindowResize(int aWidth, int aHeight)
        {
            super.onWindowResize(aWidth, aHeight);
            if (isVisible())
            {
                this.setWidth(parentContainer.getWidth());
            }
        }
    }

    private Grid<BaseEntityModel<TableModelRowWithObject<T>>> createGrid(
            PagingLoader<PagingLoadResult<BaseEntityModel<TableModelRowWithObject<T>>>> dataLoader,
            String gridId)
    {

        ListStore<BaseEntityModel<TableModelRowWithObject<T>>> listStore =
                new ListStore<BaseEntityModel<TableModelRowWithObject<T>>>(dataLoader);
        ColumnModel columnModel = createColumnModel(new ArrayList<ColumnConfig>());
        EditorGrid<BaseEntityModel<TableModelRowWithObject<T>>> editorGrid =
                new EditorGrid<BaseEntityModel<TableModelRowWithObject<T>>>(listStore, columnModel);
        editorGrid.setId(gridId);
        editorGrid.setLoadMask(true);
        editorGrid
                .setSelectionModel(new GridSelectionModel<BaseEntityModel<TableModelRowWithObject<T>>>());
        editorGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        editorGrid.setView(new ExtendedGridView());
        editorGrid.setStripeRows(true);
        editorGrid.setColumnReordering(true);
        editorGrid.setClicksToEdit(ClicksToEdit.TWO);
        editorGrid.addListener(Events.BeforeEdit,
                new Listener<GridEvent<BaseEntityModel<TableModelRowWithObject<T>>>>()
                    {
                        @Override
                        public void handleEvent(
                                GridEvent<BaseEntityModel<TableModelRowWithObject<T>>> event)
                        {
                            if (viewContext.isSimpleOrEmbeddedMode())
                            {
                                MessageBox
                                        .info("Not Allowed",
                                                "Sorry, table cell editing is not allowed in current viewing mode",
                                                null);
                                event.setCancelled(true);
                            } else if (tableModificationsManager.isSaving())
                            {
                                MessageBox.info("Not Allowed",
                                        "Sorry, table cell editing is not allowed during "
                                                + "saving of recently changed table cells.", null);
                                event.setCancelled(true);
                            } else
                            {
                                BaseEntityModel<TableModelRowWithObject<T>> model =
                                        event.getModel();
                                String columnID = event.getProperty();
                                boolean editable = isEditable(model, columnID);
                                if (editable == false)
                                {
                                    showNonEditableTableCellMessage(model, columnID);
                                }
                                event.setCancelled(editable == false);
                            }
                        }
                    });
        editorGrid.addListener(Events.AfterEdit,
                new Listener<GridEvent<BaseEntityModel<TableModelRowWithObject<T>>>>()
                    {
                        @Override
                        public void handleEvent(
                                GridEvent<BaseEntityModel<TableModelRowWithObject<T>>> event)
                        {
                            BaseEntityModel<TableModelRowWithObject<T>> model = event.getModel();
                            String columnID = event.getProperty();
                            Object value = event.getValue();
                            String newValueNotNull = StringUtils.toStringEmptyIfNull(value);
                            String oldValueNotNull =
                                    StringUtils.toStringEmptyIfNull(event.getStartValue());
                            if (oldValueNotNull.equals(newValueNotNull))
                            {
                                event.setCancelled(true);
                            } else
                            {
                                showModificationsBar();
                                if (value instanceof VocabularyTerm)
                                {
                                    VocabularyTerm term = (VocabularyTerm) value;
                                    value = term.getCode();
                                }
                                tableModificationsManager.handleEditingEvent(model, columnID,
                                        StringUtils.toStringOrNull(value));
                            }
                        }
                    });
        editorGrid.addListener(Events.SortChange, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    saveColumnDisplaySettings();
                }
            });

        return editorGrid;
    }

    /**
     * Returns <code>true</code> if cell specified by model and column ID is editable. Default implementation false.
     */
    protected boolean isEditable(BaseEntityModel<TableModelRowWithObject<T>> model, String columnID)
    {
        return false;
    }

    /**
     * Shows a message that the table cell of specified column and row (model) isn't editable.
     */
    protected void showNonEditableTableCellMessage(
            BaseEntityModel<TableModelRowWithObject<T>> model, String columnID)
    {
        MessageBox.info("Not Editable", "Sorry, this table cell isn't editable", null);
    }

    /**
     * Tries to return the property of specified properties holder which is specified by the property column name without a prefix like
     * <code>property-</code> but with prefix which distinguishes internal from externally name space.
     */
    protected IEntityProperty tryGetProperty(IEntityPropertiesHolder propertiesHolder,
            String propertyColumnNameWithoutPrefix)
    {
        String propertyTypeCode =
                CodeConverter.getPropertyTypeCode(propertyColumnNameWithoutPrefix);
        List<IEntityProperty> properties = propertiesHolder.getProperties();
        for (IEntityProperty property : properties)
        {
            if (property.getPropertyType().getCode().equals(propertyTypeCode))
            {
                return property;
            }
        }
        return null;
    }

    // this should be the only place where we create the grid column model.
    private static ColumnModel createColumnModel(List<ColumnConfig> columConfigs)
    {
        return new ColumnModel(columConfigs);
    }

    ColumnModel getFullColumnModel()
    {
        return fullColumnModel;
    }

    private static final class ExportEntitiesCallback extends AbstractAsyncCallback<String>
    {
        private String fileName;

        public ExportEntitiesCallback(final IViewContext<ICommonClientServiceAsync> viewContext, String fileName)
        {
            super(viewContext);
            this.fileName = fileName;
        }

        @Override
        protected void process(final String exportDataKey)
        {
            final URLMethodWithParameters methodWithParameters =
                    new URLMethodWithParameters(
                            GenericConstants.FILE_EXPORTER_DOWNLOAD_SERVLET_NAME);
            methodWithParameters.addParameter(GenericConstants.EXPORT_CRITERIA_KEY_PARAMETER,
                    exportDataKey);
            methodWithParameters.addParameter(GenericConstants.EXPORT_FILE_NAME, fileName);
            methodWithParameters.addParameter(GenericConstants.TIMESTAMP_PARAMETER, Long.toString(System.currentTimeMillis()));
            WindowUtils.openWindow(methodWithParameters.toString());
        }

    }

    // creates a map to quickly find a definition by its identifier
    private static <T> Map<String, IColumnDefinition<T>> asColumnIdMap(
            final Set<IColumnDefinition<T>> defs)
    {
        final Map<String, IColumnDefinition<T>> map = new HashMap<String, IColumnDefinition<T>>();
        for (final IColumnDefinition<T> def : defs)
        {
            map.put(def.getIdentifier(), def);
        }
        return map;
    }

    /**
     * @param availableColumns map of all available columns definitions.
     * @param columnModel describes the visual properties of the columns. Connected with availableColumns by column id.
     * @return list of columns definitions for those columns which are currently shown
     */
    private static <T/* column definition */> List<T> getVisibleColumns(
            final Map<String, T> availableColumns, final ColumnModel columnModel)
    {
        final List<T> selectedColumnDefs = new ArrayList<T>();
        final int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++)
        {
            if (columnModel.isHidden(i) == false)
            {
                final String columnId = columnModel.getColumnId(i);
                selectedColumnDefs.add(availableColumns.get(columnId));
            }
        }
        return selectedColumnDefs;
    }

    protected final AbstractAsyncCallback<Void> createEmptyCallback()
    {
        return new AbstractAsyncCallback<Void>(viewContext)
            {
                @Override
                protected void process(Void result)
                {
                }
            };
    }

    /** Creates callback that refreshes the grid. */
    protected final AbstractAsyncCallback<Void> createRefreshCallback(
            IBrowserGridActionInvoker invoker)
    {
        return new RefreshCallback(viewContext, invoker);
    }

    /** Callback that refreshes the grid. */
    private static final class RefreshCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        public RefreshCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }

        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }

    /** {@link SelectionListener} that creates a dialog with selected data items. */
    protected abstract class AbstractCreateDialogListener extends SelectionListener<ButtonEvent>
    {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
            List<TableModelRowWithObject<T>> data = getSelectedBaseObjects();
            IBrowserGridActionInvoker invoker = asActionInvoker();
            if (validateSelectedData(data))
            {
                createDialog(data, invoker).show();
            }
        }

        /**
         * If specified data is valid returns true, otherwise returns false. Dialog will be shown only if this method returns true. Default
         * implementation always returns true.
         */
        protected boolean validateSelectedData(List<TableModelRowWithObject<T>> data)
        {
            return true;
        }

        protected abstract Dialog createDialog(List<TableModelRowWithObject<T>> data,
                IBrowserGridActionInvoker invoker);
    }

    //
    // Table Modifications
    //

    public class ModelModificationsCallBack
    {
        BaseEntityModel<TableModelRowWithObject<T>> model;

        List<IModification> modifications;

        AsyncCallback<IUpdateResult> callBack;

        public ModelModificationsCallBack(BaseEntityModel<TableModelRowWithObject<T>> model, List<IModification> modifications,
                AsyncCallback<IUpdateResult> callBack)
        {
            this.model = model;
            this.modifications = modifications;
            this.callBack = callBack;
        }

        public BaseEntityModel<TableModelRowWithObject<T>> getModel()
        {
            return model;
        }

        public void setModel(BaseEntityModel<TableModelRowWithObject<T>> model)
        {
            this.model = model;
        }

        public List<IModification> getModifications()
        {
            return modifications;
        }

        public void setModifications(List<IModification> modifications)
        {
            this.modifications = modifications;
        }

        public AsyncCallback<IUpdateResult> getCallBack()
        {
            return callBack;
        }

        public void setCallBack(AsyncCallback<IUpdateResult> callBack)
        {
            this.callBack = callBack;
        }

        @Override
        public String toString()
        {
            return "ModelModificationsCallBack [model=" + model + ", modifications=" + modifications + ", callBack=" + callBack + "]";
        }
    }

    /**
     * Apply specified modifications to the model. Should be overriden by subclasses. Default implementation does nothing.
     */
    protected void applyModifications(List<ModelModificationsCallBack> modificationsListWithCallbacks,
            String resultSetKey)
    {
    }

    private class TableModificationsManager
    {
        private final ModificationsData<T> modificationsData = new ModificationsData<T>();

        public boolean isSaving()
        {
            return modificationsData.isSaving();
        }

        public boolean isTableDirty()
        {
            return modificationsData.isApplyModificationsComplete() == false
                    && modificationsData.isSaving() == false;
        }

        public void saveModifications()
        {
            modificationsData.handleModifications(new ModificationsData.IModificationsHandler<T>()
                {
                    @Override
                    public void applyModifications(Map<BaseEntityModel<TableModelRowWithObject<T>>, List<IModification>> modificationsByModel)
                    {
                        ArrayList<TypedTableGrid<T>.ModelModificationsCallBack> result = new ArrayList<ModelModificationsCallBack>();
                        for (Entry<BaseEntityModel<TableModelRowWithObject<T>>, List<IModification>> entry : modificationsByModel
                                .entrySet())
                        {
                            BaseEntityModel<TableModelRowWithObject<T>> model = entry.getKey();
                            List<IModification> modifications = entry.getValue();

                            AsyncCallback<IUpdateResult> callBack =
                                    createApplyModificationsCallback(model, modifications);
                            result.add(new ModelModificationsCallBack(model, modifications, callBack));
                        }
                        TypedTableGrid.this.applyModifications(result, resultSetKeyOrNull);
                    }
                });
        }

        public void cancelModifications()
        {
            clearModifications();
            grid.getStore().rejectChanges();
            refresh(); // WORKAROUND remove this refresh after LMS-2397 is resolved
        }

        public void handleEditingEvent(BaseEntityModel<TableModelRowWithObject<T>> model,
                String columnID, String newValueOrNull)
        {
            modificationsData.addModification(model, columnID, newValueOrNull);
        }

        private AsyncCallback<IUpdateResult> createApplyModificationsCallback(
                final BaseEntityModel<TableModelRowWithObject<T>> model,
                final List<IModification> modifications)
        {
            return new AbstractAsyncCallback<IUpdateResult>(viewContext)
                {
                    @Override
                    protected void process(IUpdateResult result)
                    {
                        processErrorMessage(result.tryGetErrorMessage());
                    }

                    @Override
                    public void finishOnFailure(Throwable caught)
                    {
                        processErrorMessage(caught.getMessage());
                    }

                    private void processErrorMessage(String errorMessageOrNull)
                    {
                        modificationsData.handleResponseAfterModificationHasBeenApplied(model,
                                errorMessageOrNull);
                        if (modificationsData.isApplyModificationsComplete())
                        {
                            onApplyModificationsComplete(model);
                        }
                    }
                };
        }

        private void onApplyModificationsComplete(BaseEntityModel<TableModelRowWithObject<T>> model)
        {
            if (modificationsData.hasFailedModifications())
            {
                String failureTitle = modificationsData.createFailureTitle();
                String failureReport = modificationsData.createFailedModificationsReport();
                GWTUtils.alert(failureTitle, failureReport);
                refresh();
            } else
            {
                GWTUtils.displayInfo("All modifications successfully applied.");
                model.setOutdated(true);
                model.set(CommonGridColumnIDs.MODIFIER, viewContext.getModel().getLoggedInPerson());
                model.set(CommonGridColumnIDs.MODIFICATION_DATE, SimpleDateRenderer
                        .renderDate((new DateTableCell(new Date())).getDateTime()));
                grid.getStore().commitChanges(); // no need to refresh - everything should be valid
            }
            clearModifications();
            refreshCacheSilently();
        }

        private void refreshCacheSilently()
        {
            DefaultResultSetConfig<String, TableModelRowWithObject<T>> config =
                    createPagingConfig(new BasePagingLoadConfig(), filterToolbar.getFilters(),
                            getGridDisplayTypeID());
            config.setCacheConfig(ResultSetFetchConfig.createRecomputeAndCache(resultSetKeyOrNull));
            final int id = TypedTableGrid.this.log("refreshing cache silently");
            listTableRows(config, new AbstractAsyncCallback<TypedTableResultSet<T>>(
                    TypedTableGrid.this.viewContext)
                {
                    @Override
                    protected void process(TypedTableResultSet<T> result)
                    {
                        viewContext.logStop(id);
                    }
                });
        }

        private void clearModifications()
        {
            modificationsData.clearData();
            hideModificationsBar();
        }

    }

    /** Toolbar for handling table modifications */
    private class TableModificationsToolbar extends ToolBar
    {

        public TableModificationsToolbar(final IMessageProvider messageProvider,
                final TableModificationsManager manager)
        {
            add(new Label(messageProvider.getMessage(Dict.TABLE_MODIFICATIONS)));

            final AbstractImagePrototype confirmIcon =
                    AbstractImagePrototype.create(IMAGE_BUNDLE.getConfirmIcon());
            final AbstractImagePrototype cancelIcon =
                    AbstractImagePrototype.create(IMAGE_BUNDLE.getCancelIcon());
            add(new Button("Save", confirmIcon, new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent be)
                    {
                        manager.saveModifications();
                    }
                }));
            add(new Button("Cancel", cancelIcon, new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent be)
                    {
                        manager.cancelModifications();
                    }
                }));
        }
    }

    /**
     * To be subclassed if columns in the grid depend on the internal grid configuration.
     * 
     * @return id at which grid display settings are saved.
     */
    @Override
    public String getGridDisplayTypeID()
    {
        return createGridDisplayTypeID(null);
    }

    protected void setDownloadURL(String downloadURL)
    {
        this.downloadURL = downloadURL;
    }

    /**
     * Lists table rows. Implementations of this method usually call a server method.
     */
    protected abstract void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<T>> callback);

    /**
     * Creates a column model with all available columns from scratch without taking user settings into account.
     * 
     * @return definition of all the columns in the grid
     */
    protected ColumnDefsAndConfigs<TableModelRowWithObject<T>> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<TableModelRowWithObject<T>> definitions =
                ColumnDefsAndConfigs.create(createColDefinitions(), viewContext);
        Set<IColumnDefinition<TableModelRowWithObject<T>>> columnDefs = definitions.getColumnDefs();
        columnDefinitionsMap = new HashMap<String, IColumnDefinition<TableModelRowWithObject<T>>>();
        for (IColumnDefinition<TableModelRowWithObject<T>> definition : columnDefs)
        {
            String identifier = definition.getIdentifier();
            columnDefinitionsMap.put(identifier, definition);
        }
        if (headers != null)
        {
            for (TableModelColumnHeader header : headers)
            {
                final String id = header.getId();
                final GridCellRenderer<BaseEntityModel<?>> specificRendererOrNull =
                        tryGetSpecificRenderer(header.getDataType(), header.getIndex());
                if (specificRendererOrNull != null)
                {
                    definitions.setGridCellRendererFor(id, specificRendererOrNull);
                } else if (tryGetCellListenerAndLinkGenerator(id) != null)
                {
                    definitions.setGridCellRendererFor(id,
                            LinkRenderer.createLinkRenderer(true, header.getIndex()));
                }
            }
        }
        return definitions;
    }

    private GridCellRenderer<BaseEntityModel<?>> tryGetSpecificRenderer(DataTypeCode dataType,
            int columnIndex)
    {
        if (dataType == null)
        {
            return null;
        }
        // NOTE: keep in sync with AbstractPropertyColRenderer.getPropertyColRenderer
        switch (dataType)
        {
            case CONTROLLEDVOCABULARY:
                return new VocabularyTermStringCellRenderer(columnIndex);
            case MATERIAL:
                return new MaterialRenderer(columnIndex);
            case HYPERLINK:
                return LinkRenderer.createExternalLinkRenderer();
            case REAL:
                return new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                        .getRealNumberFormatingParameters());
            case MULTILINE_VARCHAR:
                return new MultilineStringCellRenderer();
            case TIMESTAMP:
                return new TimestampStringCellRenderer();
            case DATE:
                return new DateStringCellRenderer();
            case XML:
                return new MultilineStringCellRenderer();
            default:
                return null;
        }
    }

    /** Converts specified entity into a grid row model */
    protected BaseEntityModel<TableModelRowWithObject<T>> createModel(
            GridRowModel<TableModelRowWithObject<T>> entity)
    {
        return new BaseEntityModel<TableModelRowWithObject<T>>(entity, visibleColDefinitions);
    }

    /**
     * Registers for the specified column a cell listener and link generator. This method should be called in the constructor.
     */
    protected void registerListenerAndLinkGenerator(String columnID,
            final ICellListenerAndLinkGenerator<T> listenerLinkGenerator)
    {
        listenerLinkGenerators.put(columnID, listenerLinkGenerator);
        registerLinkClickListenerFor(columnID, listenerLinkGenerator);
    }

    protected void registerListenerAndLinkGeneratorForAnyMode(String columnID,
            final ICellListenerAndLinkGenerator<T> listenerLinkGenerator)
    {
        listenerLinkGenerators.put(columnID, listenerLinkGenerator);
        registerLinkClickListenerForAnyMode(columnID, listenerLinkGenerator);
    }

    private List<IColumnDefinitionUI<TableModelRowWithObject<T>>> createColDefinitions()
    {
        if (columnUIDefinitions == null)
        {
            List<IColumnDefinitionUI<TableModelRowWithObject<T>>> list =
                    new ArrayList<IColumnDefinitionUI<TableModelRowWithObject<T>>>();
            if (headers != null)
            {
                String sessionID = viewContext.getModel().getSessionContext().getSessionID();
                for (final TableModelColumnHeader header : headers)
                {
                    String title = header.getTitle();
                    String columnId = header.getId();
                    if (title == null)
                    {
                        title = viewContext.getMessage(translateColumnIdToDictionaryKey(columnId));
                    }
                    // support for entity links
                    ICellListenerAndLinkGenerator<T> linkGeneratorOrNull =
                            tryGetCellListenerAndLinkGenerator(columnId);
                    final EntityKind entityKind = header.tryGetEntityKind();
                    if (linkGeneratorOrNull == null && entityKind != null)
                    {
                        linkGeneratorOrNull = new CellListenerAndLinkGenerator(entityKind, header);
                        registerListenerAndLinkGenerator(columnId, linkGeneratorOrNull);
                    }
                    //
                    TypedTableGridColumnDefinitionUI<T> definition =
                            new TypedTableGridColumnDefinitionUI<T>(header, title, downloadURL,
                                    sessionID, linkGeneratorOrNull);
                    if (list.size() > MAX_SHOWN_COLUMNS)
                    {
                        definition.setHidden(true);
                    }
                    list.add(definition);
                }
            }
            columnUIDefinitions = list;
        }
        return columnUIDefinitions;
    }

    protected ICellListenerAndLinkGenerator<T> tryGetCellListenerAndLinkGenerator(String columnId)
    {
        return listenerLinkGenerators.get(columnId);
    }

    /**
     * Translates a column ID to a key used to get title of the column from a dictionary. This method can be overridden by subclasses.
     * 
     * @return <code>getId() + "_" + columnID</code>
     */
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return getId() + "_" + columnID;
    }

    private void listEntities(
            final DefaultResultSetConfig<String, TableModelRowWithObject<T>> resultSetConfig,
            final AbstractAsyncCallback<ResultSet<TableModelRowWithObject<T>>> callback)
    {
        AbstractAsyncCallback<TypedTableResultSet<T>> extendedCallback =
                new AbstractAsyncCallback<TypedTableResultSet<T>>(viewContext)
                    {
                        @Override
                        protected void process(TypedTableResultSet<T> result)
                        {
                            ResultSet<TableModelRowWithObject<T>> resultSet = result.getResultSet();
                            // don't need to recreate columns when paging or filtering
                            if (resultSetConfig.getCacheConfig().getMode() != ResultSetFetchMode.FETCH_FROM_CACHE)
                            {
                                headers = resultSet.getList().getColumnHeaders();
                                columnUIDefinitions = null;
                                List<GridCustomColumnInfo> customColumnMetadata =
                                        resultSet.getList().getCustomColumnsMetadata();
                                customColumnsMetadataProvider
                                        .setCustomColumnsMetadata(customColumnMetadata);
                                recreateColumnModelAndRefreshColumnsWithFilters();

                                saveColumnDisplaySettings();
                            }
                            callback.onSuccess(resultSet);
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            callback.finishOnFailure(caught);
                        }
                    };
        listTableRows(resultSetConfig, extendedCallback);
        currentGridDisplayTypeID = getGridDisplayTypeID();
    }

    /** @return should the refresh button be enabled? */
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    boolean ignoreVisibleColumnsLimit = false;

    /**
     * Refreshes the browser if the grid display type ID has changed because this means a different set of display settings. Thus column models and
     * filters should be refreshed before data loading.
     */
    protected void refresh()
    {
        refresh(null);
    }

    protected void refresh(final IDataRefreshCallback externalRefreshCallbackOrNull)
    {
        String gridDisplayTypeID = getGridDisplayTypeID();
        refresh(externalRefreshCallbackOrNull, gridDisplayTypeID.equals(currentGridDisplayTypeID) == false);
    }

    /**
     * Shows the detail view for the specified entity
     */
    protected void showEntityViewer(TableModelRowWithObject<T> entity, boolean editMode,
            boolean inBackground)
    {
    }

    /** @return on which fields filters should be switched on by default? */
    private List<IColumnDefinition<TableModelRowWithObject<T>>> getInitialFilters()
    {

        List<IColumnDefinition<TableModelRowWithObject<T>>> definitions =
                new ArrayList<IColumnDefinition<TableModelRowWithObject<T>>>();
        List<String> ids = getColumnIdsOfFilters();
        for (String id : ids)
        {
            IColumnDefinition<TableModelRowWithObject<T>> definition = columnDefinitionsMap.get(id);
            if (definition != null)
            {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    protected List<String> getColumnIdsOfFilters()
    {
        return Collections.<String> emptyList();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[] {};
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

    protected List<T> getContainedGridElements()
    {
        List<TableModelRowWithObject<T>> wrappedElements = getGridElements();
        List<T> elements = new ArrayList<T>();
        for (TableModelRowWithObject<T> wrappedElement : wrappedElements)
        {
            elements.add(wrappedElement.getObjectOrNull());
        }
        return elements;
    }
}
