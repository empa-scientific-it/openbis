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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.viewer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.dialog.MetaprojectDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity.MetaprojectEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.form.MetaprojectEditForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * @author pkupczyk
 */
public final class MetaprojectViewer extends
        AbstractViewerWithVerticalSplit<IEntityInformationHolder> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "metaproject-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private static final String DISPLAY_ID_SUFFIX = "metaproject-viewer";

    private final Long metaprojectId;

    private Metaproject originalMetaproject;

    public static DatabaseModificationAwareComponent create(final IViewContext<?> viewContext,
            final Long metaprojectId)
    {
        MetaprojectViewer viewer = new MetaprojectViewer(viewContext, metaprojectId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private MetaprojectViewer(final IViewContext<?> viewContext, final Long metaprojectId)
    {
        super(viewContext, createId(metaprojectId));
        this.metaprojectId = metaprojectId;
        this.displayIdSuffix = DISPLAY_ID_SUFFIX;
        setLayout(new BorderLayout());
        extendToolBar();
        reloadAllData();
    }

    public static String createId(final Long metaprojectId)
    {
        return ID_PREFIX + metaprojectId;
    }

    @Override
    protected void fillBreadcrumbWidgets(List<Widget> widgets)
    {
        widgets.add(new Label(originalMetaproject.getOwnerId()));
        super.fillBreadcrumbWidgets(widgets);
    }

    private void extendToolBar()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return;
        }
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    new MetaprojectDeletionConfirmationDialog(viewContext,
                            originalMetaproject.getId(), createPermanentDeletionCallback()).show();
                }
            }));
    }

    @Override
    protected void reloadAllData()
    {
        reloadData(new AbstractAsyncCallback<Metaproject>(viewContext)
            {
                @Override
                protected void process(Metaproject result)
                {
                    recreateView(result);
                }

                @Override
                public void finishOnFailure(Throwable caught)
                {
                    setupRemovedEntityView();
                }
            });
    }

    protected void reloadData(AbstractAsyncCallback<Metaproject> callback)
    {
        viewContext.getCommonService().getMetaproject(metaprojectId, callback);
    }

    private void recreateView(final Metaproject metaproject)
    {
        updateOriginalMetaproject(metaproject);
        removeAll();

        MetaprojectPropertiesPanel properties =
                new MetaprojectPropertiesPanel(viewContext, metaprojectId);
        add(properties, createLeftBorderLayoutData());
        configureLeftPanel(properties);

        final Html loading = new Html(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        add(loading, createRightBorderLayoutData());
        layout();

        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    final MetaprojectEntities entities =
                            new MetaprojectEntities(viewContext, getId());
                    entities.addListener(MetaprojectEntities.ENTITIES_CHANGED,
                            new Listener<BaseEvent>()
                                {
                                    @Override
                                    public void handleEvent(BaseEvent be)
                                    {
                                        reloadAllData();
                                    }
                                });
                    entities.showEntities(metaprojectId, new IDelegatedAction()
                        {
                            @Override
                            public void execute()
                            {
                                remove(loading);
                                add(entities, createRightBorderLayoutData());
                                layout();
                            }
                        });
                }
            });
    }

    private void updateOriginalMetaproject(Metaproject metaproject)
    {
        this.originalMetaproject = metaproject;
        updateBreadcrumbs();
        setToolBarButtonsEnabled(true);
    }

    @Override
    public void setupRemovedEntityView()
    {
        removeAll();
        updateTitle(getOriginalDataDescription() + " does not exist any more.");
        setToolBarButtonsEnabled(false);
    }

    @Override
    protected String getOriginalDataDescription()
    {
        return viewContext.getMessage(Dict.METAPROJECT) + " " + originalMetaproject.getName();
    }

    @Override
    protected void showEntityEditor(boolean inBackground)
    {
        AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            MetaprojectEditForm.create(viewContext, metaprojectId);
                    return DefaultTabItem.create(getTabTitle(), component, viewContext, true);
                }

                @Override
                public String getId()
                {
                    return MetaprojectEditForm.createId(metaprojectId);
                }

                @Override
                public String getTabTitle()
                {
                    return AbstractRegistrationForm.getEditTitle(viewContext, Dict.METAPROJECT,
                            originalMetaproject);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.METAPROJECT, HelpPageAction.EDIT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
        tabFactory.setInBackground(inBackground);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadAllData();
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
    protected String getDeleteButtonLabel()
    {
        return viewContext.getMessage(Dict.BUTTON_DELETE_METAPROJECT);
    }
}
