/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.EntityVisitModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityVisitComparatorByTimeStamp;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * History of last visited detail views.
 * 
 * @author Franz-Josef Elmer
 */
public class HistoryWidget extends ContentPanel
{
    private static final class SimpleModel extends SimplifiedBaseModel
    {
        private static final long serialVersionUID = 1L;

        public SimpleModel(String item)
        {
            set(ModelDataPropertyNames.CODE, item);
        }
    }

    public HistoryWidget(final IViewContext<?> viewContext)
    {
        setLayout(new FitLayout());
        setHeaderVisible(true);
        setHeading(viewContext.getMessage(Dict.LAST_VISITS));

        final TreeStore<ModelData> store = createStore(viewContext);
        final Button clearButton =
                new Button(viewContext.getMessage(Dict.CLEAR), new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            List<EntityVisit> visits =
                                    viewContext.getDisplaySettingsManager().getVisits();
                            visits.clear();
                            store.removeAll();
                        }
                    });
        getHeader().addTool(clearButton);

        ColumnModel columnModel = createColumnModel(viewContext);

        final TreeGrid<ModelData> treeGrid = new TreeGrid<ModelData>(store, columnModel);
        treeGrid.setAutoExpandColumn(ModelDataPropertyNames.CODE);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getTreeView().setSortingEnabled(false);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        treeGrid.setAutoExpand(true);
        treeGrid.setHideHeaders(true);
        add(treeGrid);
    }

    private TreeStore<ModelData> createStore(final IViewContext<?> viewContext)
    {
        TreeStore<ModelData> store = new TreeStore<ModelData>();
        List<EntityVisit> visits = viewContext.getDisplaySettingsManager().getVisits();
        Collections.sort(visits, new EntityVisitComparatorByTimeStamp());
        EntityKind[] values = EntityKind.values();
        for (EntityKind entityKind : values)
        {
            String entityKindAsString = entityKind.toString();
            Map<String, List<EntityVisit>> typeToVisitMap =
                    new HashMap<String, List<EntityVisit>>();
            for (EntityVisit visit : visits)
            {
                if (entityKindAsString.equals(visit.getEntityKind()))
                {
                    List<EntityVisit> list = typeToVisitMap.get(visit.getEntityTypeCode());
                    if (list == null)
                    {
                        list = new ArrayList<EntityVisit>();
                        typeToVisitMap.put(visit.getEntityTypeCode(), list);
                    }
                    list.add(visit);
                }
            }
            if (typeToVisitMap.isEmpty() == false)
            {
                SimpleModel item = new SimpleModel(viewContext.getMessage(entityKind.name().toLowerCase()) + "s");
                store.add(item, true);
                String[] types = typeToVisitMap.keySet().toArray(new String[0]);
                Arrays.sort(types);
                for (String type : types)
                {
                    SimpleModel typeModel = new SimpleModel(type);
                    store.add(item, typeModel, true);
                    List<EntityVisit> list = typeToVisitMap.get(type);
                    Set<String> permIds = new HashSet<String>();
                    for (EntityVisit visit : list)
                    {
                        String permID = visit.getPermID();
                        if (permIds.contains(permID) == false)
                        {
                            permIds.add(permID);
                            store.add(typeModel, new EntityVisitModel(visit), false);
                        }
                    }
                }
            }
        }
        return store;
    }

    private ColumnModel createColumnModel(final IViewContext<?> viewContext)
    {
        ColumnConfig columnConfig = new ColumnConfig(ModelDataPropertyNames.CODE, "", 1);
        columnConfig.setRenderer(new WidgetTreeGridCellRenderer<ModelData>()
            {
                @Override
                public Widget getWidget(ModelData model, String property, ColumnData config,
                        int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
                {
                    if (model instanceof EntityVisitModel)
                    {
                        EntityVisitModel evm = (EntityVisitModel) model;

                        EntityVisit visit = evm.getVisit();
                        final String displayText = visit.getIdentifier();
                        final EntityKind entityKind = EntityKind.valueOf(visit.getEntityKind());
                        final String permID;
                        final String href;

                        if (entityKind == EntityKind.MATERIAL)
                        {
                            permID = StringEscapeUtils.unescapeHtml(visit.getPermID());
                            href =
                                    LinkExtractor.tryExtract(MaterialIdentifier
                                            .tryParseIdentifier(permID));
                        } else
                        {
                            permID = visit.getPermID();
                            href = LinkExtractor.createPermlink(entityKind, permID);
                        }
                        final ClickHandler listener = new ClickHandler()
                            {
                                @Override
                                public void onClick(ClickEvent event)
                                {
                                    OpenEntityDetailsTabHelper.open(viewContext, entityKind,
                                            permID,
                                            WidgetUtils.ifSpecialKeyPressed(event.getNativeEvent()));
                                }
                            };
                        // TODO 2011-06-17, Piotr Buczek: deletion (requires request to server)
                        final Widget link = LinkRenderer.getLinkWidget(displayText, listener, href);
                        final String date =
                                DateRenderer.renderDate(new Date(visit.getTimeStamp()),
                                        BasicConstant.DATE_HOURS_MINUTES_PATTERN);

                        FlowPanel panel = new FlowPanel();
                        panel.add(link);
                        panel.add(new InlineHTML(" (" + date + ")"));
                        return panel;
                    }
                    return new Label(model.get(property).toString());
                }
            });
        columnConfig.setMenuDisabled(true);
        return new ColumnModel(Arrays.asList(columnConfig));
    }
}
