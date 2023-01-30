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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.grid.MetaprojectGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * ` A button for selecting a metaproject from a list.
 * 
 * @author pkupczyk
 */
public class MetaprojectChooserButton extends Button implements
        IChosenEntitiesSetter<TableModelRowWithObject<Metaproject>>
{

    public static final String ID_SUFFIX = "_metaproject_chooser_button";

    private Field<?> field;

    private final List<IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>> listeners =
            new ArrayList<IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>>();

    public MetaprojectChooserButton(final IViewContext<?> viewContext, final String idPrefix,
            final IChosenEntitiesProvider<String> chosenProvider)
    {
        this(viewContext, idPrefix, chosenProvider, true);
    }

    public MetaprojectChooserButton(final IViewContext<?> viewContext, final String idPrefix,
            final IChosenEntitiesProvider<String> chosenProvider,
            final boolean possibleToAddMetaProjects)
    {
        super(viewContext.getMessage(Dict.ADD_METAPROJECT));

        setId(idPrefix + ID_SUFFIX);
        addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    final DisposableEntityChooser<TableModelRowWithObject<Metaproject>> chooserGrid =
                            MetaprojectGrid.createChooser(viewContext, chosenProvider);

                    if (possibleToAddMetaProjects)
                    {
                        new EntityChooserDialog<TableModelRowWithObject<Metaproject>>(chooserGrid,
                                MetaprojectChooserButton.this,
                                viewContext.getMessage(Dict.CHOOSE_METAPROJECT), viewContext,
                                new Button(viewContext
                                        .getMessage(Dict.BUTTON_CREATE_NEW_METAPROJECTS),
                                        getListenerForCreateMetaProjectButton(chooserGrid,
                                                viewContext,
                                                idPrefix))).show();
                    } else
                    {
                        new EntityChooserDialog<TableModelRowWithObject<Metaproject>>(chooserGrid,
                                MetaprojectChooserButton.this,
                                viewContext.getMessage(Dict.CHOOSE_METAPROJECT), viewContext)
                                .show();
                    }
                }

            });
    }

    private SelectionListener<ButtonEvent> getListenerForCreateMetaProjectButton(
            final DisposableEntityChooser<TableModelRowWithObject<Metaproject>> chooserGrid,
            final IViewContext<?> viewContext, final String idPrefix)
    {
        return new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(ButtonEvent event)
                {
                    final MetaprojectArea area = new MetaprojectArea(viewContext, idPrefix);
                    area.setAutoWidth(true);
                    area.setAutoHeight(true);

                    SimpleDialog simpleDialog = new SimpleDialog(area,
                            viewContext.getMessage(Dict.TOPIC_CREATE_METAPROJECTS),
                            viewContext.getMessage(Dict.BUTTON_CREATE_METAPROJECTS),
                            viewContext);
                    simpleDialog.setAutoHeight(true);
                    simpleDialog.setWidth(400);
                    simpleDialog.setScrollMode(Scroll.NONE);
                    simpleDialog.setAcceptAction(new IDelegatedAction()
                        {

                            @Override
                            public void execute()
                            {
                                for (String name : area.tryGetMetaprojects())
                                {
                                    viewContext.getCommonService().registerMetaProject(name,
                                            new AsyncCallback<Void>()
                                                {
                                                    @Override
                                                    public void onFailure(
                                                            Throwable caught)
                                                    {
                                                    }

                                                    @Override
                                                    public void onSuccess(
                                                            Void result)
                                                    {
                                                        Set<DatabaseModificationKind> kind =
                                                                new HashSet<DatabaseModificationKind>();
                                                        kind.add(new DatabaseModificationKind(
                                                                DatabaseModificationKind.ObjectKind.METAPROJECT,
                                                                DatabaseModificationKind.OperationKind.CREATE_OR_DELETE));
                                                        chooserGrid
                                                                .update(kind);
                                                    }

                                                }
                                            );
                                }
                            }
                        });
                    simpleDialog.show();
                }
            };
    }

    @Override
    public void setChosenEntities(List<TableModelRowWithObject<Metaproject>> entities)
    {
        if (entities == null)
        {
            return;
        }
        for (IChosenEntitiesListener<TableModelRowWithObject<Metaproject>> listener : listeners)
        {
            listener.entitiesChosen(entities);
        }
    }

    public void addChosenEntityListener(
            IChosenEntitiesListener<TableModelRowWithObject<Metaproject>> listener)
    {
        listeners.add(listener);
    }

    public Field<?> getField()
    {
        if (field == null)
        {
            field = new AdapterField(this);
            field.setLabelSeparator("");
        }
        return field;
    }

}
