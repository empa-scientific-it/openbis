/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoadEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityKindItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeMetaprojectItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget.MetaprojectTreeEntityItemWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget.MetaprojectTreeEntityKindItemWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget.MetaprojectTreeItemWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.widget.MetaprojectTreeMetaprojectItemWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author pkupczyk
 */
public class MetaprojectTree extends TreeGrid<MetaprojectTreeItemData> implements
        IDisposableComponent
{

    public static final String ID_SUFFIX = "_metaproject-tree";

    public static final String COLUMN_ID = "metaproject-tree-column";

    private MetaprojectTreeItemData selectedItem;

    public MetaprojectTree(IViewContext<?> viewContext, String idPrefix)
    {
        this(viewContext, idPrefix, new MetaprojectTreeLoader(viewContext),
                new MetaprojectTreeColumns(viewContext));
    }

    private MetaprojectTree(final IViewContext<?> viewContext, String idPrefix,
            final MetaprojectTreeLoader loader, final MetaprojectTreeColumns columns)
    {
        super(new TreeStore<MetaprojectTreeItemData>(loader), columns.getModel());

        setId(idPrefix + ID_SUFFIX);
        setLazyRowRender(0);
        setBorders(true);
        setAutoExpandColumn(COLUMN_ID);

        getTreeView().setForceFit(true);
        getTreeView().setSortingEnabled(false);
        getTreeView().setBufferEnabled(false);

        getStyle().setNodeCloseIcon(null);
        getStyle().setNodeOpenIcon(null);

        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<MetaprojectTreeItemData>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<MetaprojectTreeItemData> se)
                        {
                            if (selectedItem != null)
                            {
                                MetaprojectTreeItemWidget selectedWidget =
                                        columns.getWidget(selectedItem);
                                selectedWidget.setSelected(false);
                            }

                            selectedItem = se.getSelectedItem();

                            if (selectedItem != null)
                            {
                                MetaprojectTreeItemWidget selectedWidget =
                                        columns.getWidget(selectedItem);
                                selectedWidget.setSelected(true);
                            }
                        }
                    });

        loader.addListener(Loader.Load, new Listener<TreeLoadEvent>()
            {
                @Override
                public void handleEvent(TreeLoadEvent event)
                {
                    List<MetaprojectTreeItemData> items = event.getData();

                    if (items != null && items.size() > 0
                            && items.get(0) instanceof MetaprojectTreeMetaprojectItemData)
                    {
                        for (MetaprojectTreeItemData item : items)
                        {
                            MetaprojectTree.this.setExpanded(item, true);
                        }
                    }
                }
            });

        loader.load();
    }

    private static class MetaprojectTreeProxy extends RpcProxy<List<MetaprojectTreeItemData>>
    {

        private IViewContext<?> viewContext;

        private Map<Long, MetaprojectAssignmentsCount> countMap;

        public MetaprojectTreeProxy(IViewContext<?> viewContext)
        {
            this.viewContext = viewContext;
        }

        @Override
        protected void load(Object parentObject,
                AsyncCallback<List<MetaprojectTreeItemData>> callback)
        {
            if (parentObject == null)
            {
                loadMetaprojects(callback);
            } else if (parentObject instanceof MetaprojectTreeMetaprojectItemData)
            {
                loadEntityKinds((MetaprojectTreeMetaprojectItemData) parentObject, callback);
            } else if (parentObject instanceof MetaprojectTreeEntityKindItemData)
            {
                loadEntities((MetaprojectTreeEntityKindItemData) parentObject, callback);
            } else
            {
                throw new IllegalArgumentException("Unsupported parent node: " + parentObject);
            }
        }

        private void initCountMap(final IDelegatedAction callback)
        {
            viewContext.getCommonService().listMetaprojectAssignmentsCounts(
                    new AbstractAsyncCallback<List<MetaprojectAssignmentsCount>>(viewContext)
                        {
                            @Override
                            protected void process(List<MetaprojectAssignmentsCount> result)
                            {
                                Map<Long, MetaprojectAssignmentsCount> map =
                                        new HashMap<Long, MetaprojectAssignmentsCount>();

                                for (MetaprojectAssignmentsCount resultItem : result)
                                {
                                    map.put(resultItem.getMetaproject().getId(), resultItem);
                                }

                                MetaprojectTreeProxy.this.countMap = map;
                                callback.execute();
                            }
                        });
        }

        private void loadMetaprojects(final AsyncCallback<List<MetaprojectTreeItemData>> callback)
        {
            if (countMap == null)
            {
                initCountMap(new IDelegatedAction()
                    {

                        @Override
                        public void execute()
                        {
                            loadMetaprojects(callback);
                        }
                    });
            } else
            {
                List<MetaprojectTreeItemData> items = new ArrayList<MetaprojectTreeItemData>();

                for (MetaprojectAssignmentsCount count : countMap.values())
                {
                    items.add(new MetaprojectTreeMetaprojectItemData(count.getMetaproject()));
                }

                Collections.sort(items, new Comparator<MetaprojectTreeItemData>()
                    {
                        @Override
                        public int compare(MetaprojectTreeItemData o1, MetaprojectTreeItemData o2)
                        {
                            MetaprojectTreeMetaprojectItemData m1 =
                                    (MetaprojectTreeMetaprojectItemData) o1;
                            MetaprojectTreeMetaprojectItemData m2 =
                                    (MetaprojectTreeMetaprojectItemData) o2;
                            return m1.getMetaproject().getName()
                                    .compareTo(m2.getMetaproject().getName());
                        }
                    });

                callback.onSuccess(items);
            }
        }

        private void loadEntityKinds(final MetaprojectTreeMetaprojectItemData parent,
                final AsyncCallback<List<MetaprojectTreeItemData>> callback)
        {
            if (countMap == null)
            {
                initCountMap(new IDelegatedAction()
                    {

                        @Override
                        public void execute()
                        {
                            loadEntityKinds(parent, callback);
                        }
                    });
            } else
            {
                Long metaprojectId = parent.getMetaproject().getId();
                MetaprojectAssignmentsCount count = countMap.get(metaprojectId);
                List<MetaprojectTreeItemData> items = new ArrayList<MetaprojectTreeItemData>();

                if (count.getExperimentCount() > 0)
                {
                    items.add(new MetaprojectTreeEntityKindItemData(metaprojectId,
                            EntityKind.EXPERIMENT, count.getExperimentCount()));
                }
                if (count.getSampleCount() > 0)
                {
                    items.add(new MetaprojectTreeEntityKindItemData(metaprojectId,
                            EntityKind.SAMPLE, count.getSampleCount()));
                }
                if (count.getDataSetCount() > 0)
                {
                    items.add(new MetaprojectTreeEntityKindItemData(metaprojectId,
                            EntityKind.DATA_SET, count.getDataSetCount()));
                }
                if (count.getMaterialCount() > 0)
                {
                    items.add(new MetaprojectTreeEntityKindItemData(metaprojectId,
                            EntityKind.MATERIAL, count.getMaterialCount()));
                }

                callback.onSuccess(items);
            }
        }

        private void loadEntities(final MetaprojectTreeEntityKindItemData parent,
                final AsyncCallback<List<MetaprojectTreeItemData>> callback)
        {
            if (EntityKind.EXPERIMENT.equals(parent.getEntityKind()))
            {
                viewContext.getCommonService().listMetaprojectExperiments(
                        parent.getMetaprojectId(),
                        new MetaprojectLoadEntitiesCallback<Experiment>(viewContext, parent,
                                callback));
            } else if (EntityKind.SAMPLE.equals(parent.getEntityKind()))
            {
                viewContext.getCommonService().listMetaprojectSamples(parent.getMetaprojectId(),
                        new MetaprojectLoadEntitiesCallback<Sample>(viewContext, parent, callback));
            } else if (EntityKind.DATA_SET.equals(parent.getEntityKind()))
            {
                viewContext.getCommonService().listMetaprojectDataSets(
                        parent.getMetaprojectId(),
                        new MetaprojectLoadEntitiesCallback<AbstractExternalData>(viewContext, parent,
                                callback));
            } else if (EntityKind.MATERIAL.equals(parent.getEntityKind()))
            {
                viewContext.getCommonService()
                        .listMetaprojectMaterials(
                                parent.getMetaprojectId(),
                                new MetaprojectLoadEntitiesCallback<Material>(viewContext, parent,
                                        callback));
            } else
            {
                throw new IllegalArgumentException("Unsupported entity kind: "
                        + parent.getEntityKind());
            }
        }

        public void clearCache()
        {
            countMap = null;
        }

    }

    private static class MetaprojectLoadEntitiesCallback<T extends IEntityInformationHolderWithIdentifier>
            extends AbstractAsyncCallback<List<T>>
    {

        private MetaprojectTreeEntityKindItemData parent;

        private AsyncCallback<List<MetaprojectTreeItemData>> callback;

        public MetaprojectLoadEntitiesCallback(IViewContext<?> viewContext,
                MetaprojectTreeEntityKindItemData parent,
                AsyncCallback<List<MetaprojectTreeItemData>> callback)
        {
            super(viewContext);
            this.parent = parent;
            this.callback = callback;
        }

        @Override
        protected void process(List<T> entities)
        {
            List<MetaprojectTreeItemData> items = new ArrayList<MetaprojectTreeItemData>();

            for (IEntityInformationHolderWithIdentifier entity : entities)
            {
                items.add(new MetaprojectTreeEntityItemData(parent.getMetaprojectId(), entity));
            }

            Collections.sort(items, new Comparator<MetaprojectTreeItemData>()
                {
                    @Override
                    public int compare(MetaprojectTreeItemData o1, MetaprojectTreeItemData o2)
                    {
                        MetaprojectTreeEntityItemData e1 = (MetaprojectTreeEntityItemData) o1;
                        MetaprojectTreeEntityItemData e2 = (MetaprojectTreeEntityItemData) o2;

                        if (e1.isEntityStub() ^ e2.isEntityStub())
                        {
                            return e1.isEntityStub() ? -1 : 1;
                        } else
                        {
                            return e1.getEntityLabel().compareTo(e2.getEntityLabel());
                        }
                    }

                });

            callback.onSuccess(items);
        }
    }

    private static class MetaprojectTreeLoader extends BaseTreeLoader<MetaprojectTreeItemData>
    {

        public MetaprojectTreeLoader(final IViewContext<?> viewContext)
        {
            super(new MetaprojectTreeProxy(viewContext));
        }

        @Override
        public boolean hasChildren(MetaprojectTreeItemData parent)
        {
            return parent == null || parent instanceof MetaprojectTreeMetaprojectItemData
                    || parent instanceof MetaprojectTreeEntityKindItemData;
        }

        public void clearCache()
        {
            ((MetaprojectTreeProxy) getProxy()).clearCache();
        }
    }

    private static class MetaprojectTreeColumns
    {

        private ColumnModel columnModel;

        private Map<MetaprojectTreeItemData, MetaprojectTreeItemWidget> widgetMap =
                new HashMap<MetaprojectTreeItemData, MetaprojectTreeItemWidget>();

        public MetaprojectTreeColumns(final IViewContext<?> viewContext)
        {
            ColumnConfig column =
                    new ColumnConfig(COLUMN_ID,
                            viewContext.getMessage(Dict.METAPROJECT_TREE_COLUMN_HEADER), 1);
            column.setMenuDisabled(true);
            column.setSortable(false);
            column.setRenderer(new WidgetTreeGridCellRenderer<MetaprojectTreeItemData>()
                {
                    @Override
                    public Widget getWidget(MetaprojectTreeItemData data, String property,
                            ColumnData config, int rowIndex, int colIndex,
                            ListStore<MetaprojectTreeItemData> store,
                            Grid<MetaprojectTreeItemData> grid)
                    {

                        MetaprojectTreeItemWidget widget;

                        if (data instanceof MetaprojectTreeMetaprojectItemData)
                        {
                            widget =
                                    new MetaprojectTreeMetaprojectItemWidget(viewContext,
                                            (MetaprojectTreeMetaprojectItemData) data);
                        } else if (data instanceof MetaprojectTreeEntityKindItemData)
                        {
                            widget =
                                    new MetaprojectTreeEntityKindItemWidget(viewContext,
                                            (MetaprojectTreeEntityKindItemData) data);
                        } else if (data instanceof MetaprojectTreeEntityItemData)
                        {
                            widget =
                                    new MetaprojectTreeEntityItemWidget(viewContext,
                                            (MetaprojectTreeEntityItemData) data);
                        } else
                        {
                            throw new UnsupportedOperationException(
                                    "Unsupported metaproject tree item");
                        }

                        widgetMap.put(data, widget);
                        return widget;
                    }
                });

            columnModel = new ColumnModel(Arrays.asList(column));
        }

        public ColumnModel getModel()
        {
            return columnModel;
        }

        public MetaprojectTreeItemWidget getWidget(MetaprojectTreeItemData data)
        {
            return widgetMap.get(data);
        }

    }

    private class MetaprojectTreeState
    {

        Map<MetaprojectTreeItemData, Boolean> expandedState;

        List<MetaprojectTreeItemData> selectedState;

        public Map<MetaprojectTreeItemData, Boolean> getExpandedState()
        {
            return expandedState;
        }

        public void setExpandedState(Map<MetaprojectTreeItemData, Boolean> expandedState)
        {
            this.expandedState = expandedState;
        }

        public List<MetaprojectTreeItemData> getSelectedState()
        {
            return selectedState;
        }

        public void setSelectedState(List<MetaprojectTreeItemData> selectedState)
        {
            this.selectedState = selectedState;
        }

    }

    private MetaprojectTreeState recordState()
    {
        MetaprojectTreeState state = new MetaprojectTreeState();
        state.setExpandedState(recordExpandedState());
        state.setSelectedState(recordSelectedState());
        return state;
    }

    private void restoreState(MetaprojectTreeState state)
    {
        restoreExpandedState(state.getExpandedState());
        restoreSelectedState(state.getSelectedState());
    }

    private Map<MetaprojectTreeItemData, Boolean> recordExpandedState()
    {
        Map<MetaprojectTreeItemData, Boolean> expandedState =
                new HashMap<MetaprojectTreeItemData, Boolean>();

        for (MetaprojectTreeItemData item : getTreeStore().getAllItems())
        {
            expandedState.put(item, isExpanded(item));
        }

        return expandedState;
    }

    private List<MetaprojectTreeItemData> recordSelectedState()
    {
        final List<MetaprojectTreeItemData> selectedPath = new ArrayList<MetaprojectTreeItemData>();

        if (selectedItem != null)
        {
            MetaprojectTreeItemData currentItem = selectedItem;
            while (currentItem != null)
            {
                selectedPath.add(currentItem);
                currentItem = getTreeStore().getParent(currentItem);
            }
        }

        return selectedPath;
    }

    private void restoreExpandedState(Map<MetaprojectTreeItemData, Boolean> expandedState)
    {
        for (Map.Entry<MetaprojectTreeItemData, Boolean> entry : expandedState.entrySet())
        {
            setExpanded(entry.getKey(), entry.getValue());
        }
    }

    private void restoreSelectedState(List<MetaprojectTreeItemData> selectedPath)
    {
        for (MetaprojectTreeItemData selectedPathItem : selectedPath)
        {
            if (getTreeStore().contains(selectedPathItem))
            {
                getSelectionModel().select(false, selectedPathItem);
                break;
            }
        }
    }

    private MetaprojectTreeLoader getLoader()
    {
        return (MetaprojectTreeLoader) getTreeStore().getLoader();
    }

    public void refresh(final IDelegatedAction callback)
    {
        final MetaprojectTreeState state = recordState();

        LoadListener afterLoadListener = new LoadListener()
            {
                @Override
                public void loaderLoad(LoadEvent le)
                {
                    restoreState(state);
                    getLoader().removeLoadListener(this);

                    if (callback != null)
                    {
                        callback.execute();
                    }
                }
            };

        getLoader().clearCache();
        getLoader().addLoadListener(afterLoadListener);
        getLoader().load();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refresh(IDelegatedAction.DO_NOTHING);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> result = new HashSet<DatabaseModificationKind>();
        DatabaseModificationKind.addAny(result, ObjectKind.METAPROJECT);
        DatabaseModificationKind.addAny(result, ObjectKind.EXPERIMENT);
        DatabaseModificationKind.addAny(result, ObjectKind.SAMPLE);
        DatabaseModificationKind.addAny(result, ObjectKind.DATA_SET);
        DatabaseModificationKind.addAny(result, ObjectKind.MATERIAL);
        return result.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public Component getComponent()
    {
        return this;
    }

    @Override
    public void dispose()
    {
    }

}
