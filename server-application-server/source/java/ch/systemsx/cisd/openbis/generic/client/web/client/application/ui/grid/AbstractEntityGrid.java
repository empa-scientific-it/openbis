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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IOnSuccessAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MetaprojectChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IUpdateResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIsStub;
import ch.systemsx.cisd.openbis.generic.shared.basic.ITaggable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Abstract super class of {@link TypedTableGrid}-based entity browsers.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractEntityGrid<E extends IEntityInformationHolderWithProperties & ITaggable & IIsStub>
        extends TypedTableGrid<E>
{

    public static final String TAG_BUTTON_ID_SUFFIX = "_tag";

    public static final String UNTAG_BUTTON_ID_SUFFIX = "_untag";

    public static final EventType ENTITY_TAGGED_EVENT = new EventType();

    public static final EventType ENTITY_UNTAGGED_EVENT = new EventType();

    public AbstractEntityGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, boolean refreshAutomatically,
            IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, refreshAutomatically, displayTypeIDGenerator);
    }

    public AbstractEntityGrid(IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId, IDisplayTypeIDGenerator displayTypeIDGenerator)
    {
        super(viewContext, browserId, displayTypeIDGenerator);
    }

    @Override
    protected boolean isEditable(BaseEntityModel<TableModelRowWithObject<E>> model, String columnID)
    {
        String propertyName = columnID.substring(SampleGridColumnIDs.PROPERTIES_PREFIX.length());
        E sample = model.getBaseObject().getObjectOrNull();
        BasicEntityType entityType = sample.getEntityType();
        IEntityProperty propertyOrNull = tryGetProperty(sample, propertyName);
        if (propertyOrNull != null && propertyOrNull.isScriptable())
        {
            return false;
        }
        List<IColumnDefinition<TableModelRowWithObject<E>>> columnDefinitions =
                getColumnDefinitions(Arrays.asList(columnID));
        IColumnDefinition<TableModelRowWithObject<E>> columnDefinition = columnDefinitions.get(0);
        return columnDefinition.tryToGetProperty(entityType.getCode()) != null;
    }

    @Override
    protected void applyModifications(List<TypedTableGrid<E>.ModelModificationsCallBack> modificationsListWithCallbacks, String resultSetKey)
    {
        List<EntityPropertyUpdates> allUpdates = new ArrayList<EntityPropertyUpdates>();
        final List<AsyncCallback<IUpdateResult>> callBacks = new ArrayList<AsyncCallback<IUpdateResult>>();
        for (ModelModificationsCallBack item : modificationsListWithCallbacks)
        {
            final EntityKind entityKind = getEntityKindOrNull();
            final TechId entityId = new TechId(item.getModel().getBaseObject().getId());
            final EntityPropertyUpdates updates =
                    new EntityPropertyUpdates(resultSetKey, entityKind, entityId);
            for (IModification modification : item.getModifications())
            {
                String propertyCode =
                        modification.getColumnID().substring(
                                SampleGridColumnIDs.PROPERTIES_PREFIX.length());
                updates.addModifiedProperty(propertyCode, modification.tryGetNewValue());
            }
            allUpdates.add(updates);
            callBacks.add(item.getCallBack());
        }
        AsyncCallback<IUpdateResult> callBack = new AsyncCallback<IUpdateResult>()
            {
                @Override
                public void onFailure(Throwable caught)
                {
                    for (AsyncCallback<IUpdateResult> asyncCallback : callBacks)
                    {
                        asyncCallback.onFailure(caught);
                    }
                }

                @Override
                public void onSuccess(IUpdateResult result)
                {
                    for (AsyncCallback<IUpdateResult> asyncCallback : callBacks)
                    {
                        asyncCallback.onSuccess(result);
                    }
                }
            };
        viewContext.getService().updateProperties(allUpdates, callBack);
    }

    @Override
    protected void showEntityViewer(TableModelRowWithObject<E> row, boolean editMode,
            boolean inBackground)
    {
        showEntityInformationHolderViewer(row.getObjectOrNull(), editMode, inBackground);
    }

    protected final GridCellRenderer<BaseEntityModel<?>> createShowDetailsLinkCellRenderer()
    {
        return LinkRenderer.createExternalLinkRenderer(viewContext
                .getMessage(Dict.SHOW_DETAILS_LINK_TEXT_VALUE));
    }

    protected String createDisplayIdSuffix(EntityKind entityKindOrNull, EntityType entityTypeOrNull)
    {
        String suffix = "";
        if (entityKindOrNull != null)
        {
            suffix += "-" + entityKindOrNull.toString();
        }
        if (entityTypeOrNull != null)
        {
            suffix += "-" + entityTypeOrNull.getCode();
        }
        return suffix;
    }

    private List<String> getMetaProjectsReferencedyByEachOf(List<ITaggable> taggables)
    {
        int itemCount = taggables.size();
        Map<String, Integer> counts = new HashMap<String, Integer>();

        for (ITaggable taggable : taggables)
        {
            Collection<Metaproject> metaProjects = taggable.getMetaprojects();

            if (metaProjects != null)
            {
                for (Metaproject metaProject : metaProjects)
                {
                    Integer count = counts.get(metaProject.getName());
                    if (count == null)
                    {
                        count = 0;
                    }
                    count++;
                    counts.put(metaProject.getName(), count);
                }
            }
        }

        List<String> result = new ArrayList<String>();
        for (String name : counts.keySet())
        {
            Integer count = counts.get(name);
            if (count != null && count == itemCount)
            {
                result.add(name);
            }
        }
        return result;
    }

    private List<String> getMetaProjectsReferencedByAtLeastOneOf(List<ITaggable> taggables)
    {
        Set<String> result = new HashSet<String>();

        for (ITaggable taggable : taggables)
        {
            Collection<Metaproject> metaprojects = taggable.getMetaprojects();
            if (metaprojects != null)
            {
                for (Metaproject metaProject : metaprojects)
                {
                    result.add(metaProject.getName());
                }
            }
        }
        return new ArrayList<String>(result);
    }

    protected final void addTaggingButtons()
    {
        addTaggingButtons(true);
    }

    protected final void addTaggingButtons(final boolean refresh)
    {
        final MetaprojectChooserButton tagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    List<BaseEntityModel<TableModelRowWithObject<E>>> selectedItems =
                                            getSelectedItems();
                                    List<ITaggable> taggables = new ArrayList<ITaggable>();
                                    for (BaseEntityModel<TableModelRowWithObject<E>> model : selectedItems)
                                    {
                                        taggables.add(model.getBaseObject().getObjectOrNull());
                                    }

                                    return getMetaProjectsReferencedyByEachOf(taggables);
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return true;
                                }
                            });

        tagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {

                            List<BaseEntityModel<TableModelRowWithObject<E>>> selectedItems =
                                    getSelectedItems();

                            EntityKind entityKind =
                                    selectedItems.get(0).getBaseObject().getObjectOrNull()
                                            .getEntityKind();

                            List<Long> entityIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<E>> item : selectedItems)
                            {
                                entityIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }

                            AbstractAsyncCallback<Void> callback;

                            if (refresh)
                            {
                                callback = createRefreshCallback(asActionInvoker());
                            } else
                            {
                                callback = createEmptyCallback();
                            }

                            viewContext.getCommonService().assignEntitiesToMetaProjects(entityKind,
                                    metaProjectIds, entityIds,
                                    fireEventOnSuccess(callback, ENTITY_TAGGED_EVENT));

                        }
                    });

        tagButton.setId(gridId + TAG_BUTTON_ID_SUFFIX);
        tagButton.setText(viewContext.getMessage(Dict.BUTTON_TAG));
        tagButton.setToolTip(viewContext.getMessage(Dict.BUTTON_TAG_TOOLTIP));
        enableButtonOnSelectedItemsIfNoStubsAreSelected(tagButton);
        addButton(tagButton);

        final MetaprojectChooserButton untagButton =
                new MetaprojectChooserButton(viewContext, getId(),
                        new IChosenEntitiesProvider<String>()
                            {
                                @Override
                                public List<String> getEntities()
                                {
                                    List<BaseEntityModel<TableModelRowWithObject<E>>> selectedItems =
                                            getSelectedItems();
                                    List<ITaggable> taggables = new ArrayList<ITaggable>();
                                    for (BaseEntityModel<TableModelRowWithObject<E>> model : selectedItems)
                                    {
                                        taggables.add(model.getBaseObject().getObjectOrNull());
                                    }
                                    return getMetaProjectsReferencedByAtLeastOneOf(taggables);
                                }

                                @Override
                                public boolean isBlackList()
                                {
                                    return false;
                                }
                            }, false);

        untagButton
                .addChosenEntityListener(new IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>()
                    {
                        @Override
                        public void entitiesChosen(
                                List<TableModelRowWithObject<Metaproject>> entities)
                        {
                            List<BaseEntityModel<TableModelRowWithObject<E>>> selectedItems =
                                    getSelectedItems();

                            EntityKind entityKind =
                                    selectedItems.get(0).getBaseObject().getObjectOrNull()
                                            .getEntityKind();

                            List<Long> entityIds = new ArrayList<Long>();
                            for (BaseEntityModel<TableModelRowWithObject<E>> item : selectedItems)
                            {
                                entityIds.add(item.getBaseObject().getObjectOrNull().getId());
                            }

                            List<Long> metaProjectIds = new ArrayList<Long>();
                            for (TableModelRowWithObject<Metaproject> row : entities)
                            {
                                metaProjectIds.add(row.getObjectOrNull().getId());
                            }

                            AbstractAsyncCallback<Void> callback;

                            if (refresh)
                            {
                                callback = createRefreshCallback(asActionInvoker());
                            } else
                            {
                                callback = createEmptyCallback();
                            }

                            viewContext.getCommonService().removeEntitiesFromMetaProjects(
                                    entityKind, metaProjectIds, entityIds,
                                    fireEventOnSuccess(callback, ENTITY_UNTAGGED_EVENT));

                        }
                    });

        untagButton.setId(gridId + UNTAG_BUTTON_ID_SUFFIX);
        untagButton.setText(viewContext.getMessage(Dict.BUTTON_UNTAG));
        untagButton.setToolTip(viewContext.getMessage(Dict.BUTTON_UNTAG_TOOLTIP));
        enableButtonOnSelectedItems(untagButton);
        addButton(untagButton);

    }

    private <T> AbstractAsyncCallback<T> fireEventOnSuccess(
            final AbstractAsyncCallback<T> callback, final EventType eventType)
    {
        callback.addOnSuccessAction(new IOnSuccessAction<T>()
            {
                @Override
                public void execute(T result)
                {
                    fireEvent(eventType);
                }
            });
        return callback;
    }

    protected void enableButtonOnSelectedItemsIfNoStubsAreSelected(final Button button)
    {
        button.setEnabled(false);
        addGridSelectionChangeListener(new Listener<SelectionChangedEvent<ModelData>>()
            {
                @Override
                public void handleEvent(SelectionChangedEvent<ModelData> se)
                {

                    List<BaseEntityModel<TableModelRowWithObject<E>>> selectedItems =
                            getSelectedItems();

                    List<IIsStub> selection = new ArrayList<IIsStub>();
                    for (final BaseEntityModel<TableModelRowWithObject<E>> model : selectedItems)
                    {
                        selection.add(new IIsStub()
                            {
                                @Override
                                public boolean isStub()
                                {
                                    return model.getBaseObject().getObjectOrNull().isStub();
                                }

                            });
                    }

                    boolean enabled;
                    if (selection.size() > 0)
                    {
                        enabled = true;
                        for (IIsStub taggable : selection)
                        {
                            if (taggable.isStub())
                            {
                                enabled = false;
                            }
                        }
                    } else
                    {
                        enabled = false;
                    }
                    button.setEnabled(enabled);
                }
            });
    }
}
