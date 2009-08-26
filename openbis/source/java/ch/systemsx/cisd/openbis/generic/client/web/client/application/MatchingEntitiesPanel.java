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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.XDOM;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel.MatchingEntityColumnKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.RelatedDataSetGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * A {@link LayoutContainer} extension which displays the matching entities.
 * 
 * @author Christian Ribeaud
 */
final class MatchingEntitiesPanel extends AbstractBrowserGrid<MatchingEntity, MatchingEntityModel>
{
    static final String PREFIX = GenericConstants.ID_PREFIX + "matching-entities-panel_";

    static final String GRID_ID = PREFIX + "grid";

    public static final String SHOW_RELATED_DATASETS_BUTTON_ID =
            GRID_ID + "_show-related-datasets-button";

    private final SearchableEntity searchableEntity;

    private final String queryText;

    public IDisposableComponent asDisposableComponent()
    {
        return asDisposableWithoutToolbar();
    }

    public MatchingEntitiesPanel(IViewContext<ICommonClientServiceAsync> viewContext,
            SearchableEntity searchableEntity, String queryText)
    {
        // NOTE: refreshAutomatically is false, refreshing should be called manually
        super(viewContext, GRID_ID, false, false);
        this.searchableEntity = searchableEntity;
        this.queryText = queryText;
        setId(createId());

        updateDefaultRefreshButton();
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.SEARCH_RESULT_GRID);
        registerLinkClickListenerFor(MatchingEntityColumnKind.IDENTIFIER.id(),
                new ICellListener<MatchingEntity>()
                    {
                        public void handle(MatchingEntity rowItem)
                        {
                            showEntityViewer(rowItem, false);
                        }
                    });
        extendBottomToolbar();
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        String showRelatedDatasetsTitle = viewContext.getMessage(Dict.BUTTON_SHOW_RELATED_DATASETS);
        Button showRelatedDatasetsButton =
                createSelectedItemsButton(showRelatedDatasetsTitle,
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    final List<MatchingEntity> entities = getSelectedBaseObjects();
                                    new ShowRelatedDatasetsDialog(entities).show();
                                }
                            });
        showRelatedDatasetsButton.setId(SHOW_RELATED_DATASETS_BUTTON_ID);
        addButton(showRelatedDatasetsButton);
        allowMultipleSelection();

        addEntityOperationsSeparator();
    }

    private final class ShowRelatedDatasetsDialog extends
            AbstractDataConfirmationDialog<List<MatchingEntity>>
    {
        private static final int FIELD_WIDTH = 200;

        private static final int LABEL_WIDTH = 120;

        private Radio allOrSelectedRadio;

        public ShowRelatedDatasetsDialog(List<MatchingEntity> entities)
        {
            super(viewContext, entities, viewContext
                    .getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_TITLE));
            setWidth(LABEL_WIDTH + FIELD_WIDTH + 50);
        }

        @Override
        protected String createMessage()
        {
            return viewContext.getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_MESSAGE, data.size());
        }

        @Override
        protected final void extendForm()
        {
            formPanel.setLabelWidth(LABEL_WIDTH);
            formPanel.setFieldWidth(FIELD_WIDTH);
            formPanel.setBodyBorder(false);
            formPanel.setHeaderVisible(false);

            formPanel.add(createAllOrSelectedRadio());
        }

        private final RadioGroup createAllOrSelectedRadio()
        {
            final String radioGroupLabel =
                    viewContext.getMessage(Dict.SHOW_RELATED_DATASETS_DIALOG_RADIO_LABEL);
            final String selectedLabel =
                    viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, data.size());
            final String allLabel = viewContext.getMessage(Dict.ALL_RADIO, data.size());

            return WidgetUtils.createAllOrSelectedRadioGroup(allOrSelectedRadio =
                    WidgetUtils.createRadio(selectedLabel), WidgetUtils.createRadio(allLabel),
                    radioGroupLabel, data.size());
        }

        private boolean getSelected()
        {
            return WidgetUtils.isSelected(allOrSelectedRadio);
        }

        @Override
        protected void executeConfirmedAction()
        {
            final boolean selected = getSelected();
            RelatedDataSetCriteria criteria = createCriteria(selected);
            showRelatedDatasetsTab(criteria);
        }

        private RelatedDataSetCriteria createCriteria(boolean selected)
        {
            if (getSelected())
            {
                return RelatedDataSetCriteria.createSelectedEntities(getSelectedBaseObjects());
            } else
            {
                return RelatedDataSetCriteria.createDisplayedEntities(createTableExportCriteria());
            }
        }

        private void showRelatedDatasetsTab(final RelatedDataSetCriteria criteria)
        {
            final ITabItemFactory tabFactory = new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        IDisposableComponent component =
                                RelatedDataSetGrid.create(viewContext, criteria);
                        return DefaultTabItem.create(getTabTitle(), component, viewContext);
                    }

                    public String getId()
                    {
                        return RelatedDataSetGrid.BROWSER_ID + XDOM.getUniqueId();
                    }

                    private String getTabTitle()
                    {
                        return "Related Data Sets";
                    }
                };
            DispatcherHelper.dispatchNaviEvent(tabFactory);
        }

    }

    private static String createId()
    {
        return PREFIX + XDOM.getUniqueId();
    }

    @Override
    protected boolean isRefreshEnabled()
    {
        return true;
    }

    /** used to refresh the results of the previously executed query */
    @Override
    protected final void refresh()
    {
        super.refresh(null, null, false);
    }

    /** used to make a first data refresh, but can be also called many times */
    public final void refresh(final IDataRefreshCallback newRefreshCallback)
    {
        super.refresh(newRefreshCallback, null, true);
    }

    @Override
    protected void showEntityViewer(MatchingEntity matchingEntity, boolean editMode)
    {
        final EntityKind entityKind = matchingEntity.getEntityKind();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        matchingEntity.getEntityType());
        final IClientPlugin<EntityType, IIdentifiable> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final ITabItemFactory tabView =
                createClientPlugin.createEntityViewer(matchingEntity.asIdentifiable());
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected ColumnDefsAndConfigs<MatchingEntity> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<MatchingEntity> schema =
                BaseEntityModel.createColumnConfigs(MatchingEntityModel
                        .getStaticColumnsDefinition(), viewContext);
        schema.setGridCellRendererFor(MatchingEntityColumnKind.IDENTIFIER.id(), LinkRenderer
                .createLinkRenderer());
        return schema;
    }

    @Override
    protected MatchingEntityModel createModel(MatchingEntity entity)
    {
        return new MatchingEntityModel(entity);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, MatchingEntity> resultSetConfig,
            AbstractAsyncCallback<ResultSet<MatchingEntity>> callback)
    {
        viewContext.getService().listMatchingEntities(searchableEntity, queryText, resultSetConfig,
                callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<MatchingEntity> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMatchingEntities(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<MatchingEntity>> getInitialFilters()
    {
        return asColumnFilters(new MatchingEntityColumnKind[]
            { MatchingEntityColumnKind.ENTITY_TYPE, MatchingEntityColumnKind.IDENTIFIER,
                    MatchingEntityColumnKind.MATCHING_FIELD });
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.MATERIAL), edit(ObjectKind.MATERIAL),
                    createOrDelete(ObjectKind.SAMPLE), edit(ObjectKind.SAMPLE),
                    createOrDelete(ObjectKind.EXPERIMENT), edit(ObjectKind.EXPERIMENT),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    createOrDelete(ObjectKind.VOCABULARY_TERM), edit(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshGridSilently();
    }

}
