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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.browser;

import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComposite;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity.MetaprojectEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.MetaprojectTree;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityKindItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeMetaprojectItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.BorderLayoutHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class MetaprojectBrowser extends ContentPanel implements IDisposableComponent
{

    public static final String ID = GenericConstants.ID_PREFIX + "metaproject-browser";

    public static final String DISPLAY_ID_SUFFIX = "metaproject-browser";

    private IViewContext<?> viewContext;

    private MetaprojectTree tree;

    private MetaprojectBrowserTreePanel treePanel;

    private SelectionChangedListener<MetaprojectTreeItemData> treeListener;

    private MetaprojectEntities entities;

    private DisposableComposite composite = new DisposableComposite(this);

    public MetaprojectBrowser(final IViewContext<?> viewContext)
    {
        setId(ID);
        setLayout(new BorderLayout());
        setHeaderVisible(false);

        this.viewContext = viewContext;

        tree = new MetaprojectTree(viewContext, getId());
        treeListener = new SelectionChangedListener<MetaprojectTreeItemData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<MetaprojectTreeItemData> se)
                {
                    showEntities(se.getSelectedItem());
                }
            };
        tree.getSelectionModel().addSelectionChangedListener(treeListener);
        entities = new MetaprojectEntities(viewContext, getId());
        entities.addListener(MetaprojectEntities.ENTITIES_CHANGED, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    refresh();
                }
            });

        composite.addSubcomponent(tree);
        composite.addSubcomponent(entities);

        BorderLayoutData treeLayout = getHelper().createLeftBorderLayoutData();
        BorderLayoutData gridsLayout = BorderLayoutHelper.createRightBorderLayoutData();

        treePanel = new MetaprojectBrowserTreePanel(viewContext, tree);

        add(treePanel, treeLayout);
        add(entities, gridsLayout);

        layout();
    }

    @Override
    protected void onAttach()
    {
        super.onAttach();
        getHelper().configureLeftPanel(treePanel);
    }

    private void showEntities(MetaprojectTreeItemData item)
    {
        if (item == null)
        {
            entities.hideEntities();
        } else if (item instanceof MetaprojectTreeMetaprojectItemData)
        {
            MetaprojectTreeMetaprojectItemData metaprojectItem =
                    (MetaprojectTreeMetaprojectItemData) item;
            entities.showEntities(metaprojectItem.getMetaproject().getId(),
                    IDelegatedAction.DO_NOTHING);
        } else if (item instanceof MetaprojectTreeEntityKindItemData)
        {
            MetaprojectTreeEntityKindItemData entityKindItem =
                    (MetaprojectTreeEntityKindItemData) item;
            entities.showEntities(entityKindItem.getMetaprojectId(),
                    entityKindItem.getEntityKind(), IDelegatedAction.DO_NOTHING);
        } else if (item instanceof MetaprojectTreeEntityItemData)
        {
            MetaprojectTreeEntityItemData entityItem = (MetaprojectTreeEntityItemData) item;
            entities.showEntities(entityItem.getMetaprojectId(), entityItem.getEntity()
                    .getEntityKind(), IDelegatedAction.DO_NOTHING);
        }
    }

    public void refresh()
    {
        showEntities(null);
        tree.getSelectionModel().removeSelectionListener(treeListener);
        tree.refresh(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    showEntities(tree.getSelectionModel().getSelectedItem());
                    tree.getSelectionModel().addSelectionChangedListener(treeListener);
                }
            });
    }

    private BorderLayoutHelper getHelper()
    {
        return new BorderLayoutHelper(viewContext, (BorderLayout) getLayout(), DISPLAY_ID_SUFFIX);
    }

    @Override
    public Component getComponent()
    {
        return composite.getComponent();
    }

    @Override
    public void dispose()
    {
        composite.dispose();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return composite.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refresh();
    }

}
