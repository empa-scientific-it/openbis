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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CustomImportComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GeneralImportComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.RoleAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetBatchUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetUploadForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion.DeletionGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.NewEntityTypeForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBatchRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBatchRegistrationUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.browser.MetaprojectBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBatchRegisterUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationTypeFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script.ScriptGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script.ScriptRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp.WebAppComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp.WebAppUrl;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.LoggingConsole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebApp;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.Window;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * <p>
 * Note that the returned object must be a {@link ITabItem} implementation.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class ComponentProvider
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private IMainPanel mainTabPanelOrNull;

    public ComponentProvider(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    private String getMessage(String key)
    {
        return viewContext.getMessage(key);
    }

    private ITabItem createTab(String title, IDisposableComponent component)
    {
        return DefaultTabItem.create(title, component, viewContext);
    }

    // creates a tab which requires confirmation before it can be closed
    private ITabItem createRegistrationTab(final String title,
            DatabaseModificationAwareComponent component)
    {
        return DefaultTabItem.create(title, component, viewContext, true);
    }

    private ITabItem createRegistrationTabWithoutCloseConfirmation(final String title,
            DatabaseModificationAwareComponent component)
    {
        return DefaultTabItem.create(title, component, viewContext, false);
    }

    /**
     * Creates a tab with the specified component. The tab is unaware of database modifications and will not be automatically refreshed if changes
     * occur.
     */
    private ITabItem createSimpleTab(String title, Component component,
            boolean isCloseConfirmationNeeded)
    {
        return DefaultTabItem.createUnaware(title, component, isCloseConfirmationNeeded,
                viewContext);
    }

    public AbstractTabItemFactory getSampleBrowser(final String initialGroupOrNull,
            final String initialSampleTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            SampleBrowserGrid.create(viewContext, initialGroupOrNull,
                                    initialSampleTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return SampleBrowserGrid.MAIN_BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {

                    return getMessage(Dict.SAMPLE_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSampleBrowserLink(initialGroupOrNull,
                            initialSampleTypeOrNull);
                }
            };
    }

    public final AbstractTabItemFactory getSampleBrowser()
    {
        return getSampleBrowser(null, null);
    }

    public final AbstractTabItemFactory getMaterialBrowser()
    {
        return getMaterialBrowser(null);
    }

    public final AbstractTabItemFactory getMaterialBrowser(final String initialMaterialTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            MaterialBrowserGrid.createWithTypeChooser(viewContext,
                                    initialMaterialTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return MaterialBrowserGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createMaterialBrowserLink(initialMaterialTypeOrNull);
                }

            };
    }

    public final AbstractTabItemFactory getGroupBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = SpaceGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SpaceGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.GROUP, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SPACE_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getScriptBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = ScriptGrid.create(viewContext, null, null);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ScriptGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SCRIPT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SCRIPT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getAuthorizationGroupBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = AuthorizationGroupGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return AuthorizationGroupGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.AUTHORIZATION_GROUPS,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.AUTHORIZATION_GROUP_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getRoleAssignmentBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = RoleAssignmentGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return RoleAssignmentGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ROLES, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.ROLE_ASSIGNMENT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getPersonBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PersonGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PersonGrid.createBrowserId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.USERS, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PERSON_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getSampleRegistration(final ActionContext context)
    {
        return getSampleRegistration(context, null);
    }

    public final AbstractTabItemFactory getSampleRegistration(final ActionContext context,
            final SampleRegistrationTypeFilter filter)
    {
        AbstractTabItemFactory tab = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleRegistrationPanel.create(viewContext, context, filter);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleRegistrationPanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
        tab.setForceReopen(true);
        return tab;
    }

    public final AbstractTabItemFactory getSampleRegistration()
    {
        return getSampleRegistration(new ActionContext());
    }

    public final AbstractTabItemFactory getExperimentRegistration(final ActionContext context)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ExperimentRegistrationPanel.create(viewContext, context);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentRegistrationPanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getExperimentBatchRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ExperimentBatchRegistrationPanel.create(viewContext, false);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentBatchRegistrationPanel.getId(false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT, HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_IMPORT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getExperimentBatchUpdate()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ExperimentBatchRegistrationPanel.create(viewContext, true);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentBatchRegistrationPanel.getId(true);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getExperimentRegistration()
    {
        return getExperimentRegistration(new ActionContext());
    }

    public final AbstractTabItemFactory getSampleBatchRegistration()
    {
        final boolean update = false;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleBatchRegisterUpdatePanel.create(viewContext, update);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleBatchRegisterUpdatePanel.getId(update);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_BATCH_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getSampleBatchUpdate()
    {
        final boolean update = true;

        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleBatchRegisterUpdatePanel.create(viewContext, true);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleBatchRegisterUpdatePanel.getId(update);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public final AbstractTabItemFactory getDataSetBatchUpdate()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            DataSetBatchUpdatePanel.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetBatchUpdatePanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getMaterialBatchRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            MaterialBatchRegistrationUpdatePanel.create(viewContext, false);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return MaterialBatchRegistrationUpdatePanel.getId(false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_IMPORT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getMaterialBatchUpdate()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            MaterialBatchRegistrationUpdatePanel.create(viewContext, true);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return MaterialBatchRegistrationUpdatePanel.getId(true);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getVocabularyRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    Component component = new VocabularyRegistrationForm(viewContext);
                    return createSimpleTab(getTabTitle(), component, true);
                }

                @Override
                public String getId()
                {
                    return VocabularyRegistrationForm.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.VOCABULARY,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.VOCABULARY_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getProjectRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ProjectRegistrationForm.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ProjectRegistrationForm.createId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROJECT_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getScriptRegistration(final EntityKind entityKindOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    Component component =
                            ScriptRegistrationForm.create(viewContext, entityKindOrNull);
                    return createSimpleTab(getTabTitle(), component, true);
                }

                @Override
                public String getId()
                {
                    return ScriptRegistrationForm.createId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SCRIPT, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SCRIPT_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getVocabularyBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = VocabularyGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return VocabularyGrid.GRID_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.VOCABULARY, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.VOCABULARY_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getProjectBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = ProjectGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ProjectGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROJECT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getMetaprojectBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser = new MetaprojectBrowser(viewContext);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return MetaprojectBrowser.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.METAPROJECT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.METAPROJECT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createMetaprojectBrowserLink();
                }

            };
    }

    public final AbstractTabItemFactory getDeletionBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = DeletionGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DeletionGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific(getMessage(Dict.DELETION_BROWSER));
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DELETION_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public AbstractTabItemFactory getExperimentBrowser(final String initialSpaceOrNull,
            final String initialProjectOrNull, final String initialExperimentTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            ExperimentBrowserGrid.create(viewContext, initialSpaceOrNull,
                                    initialProjectOrNull, initialExperimentTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return ExperimentBrowserGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createExperimentBrowserLink(initialSpaceOrNull,
                            initialProjectOrNull, initialExperimentTypeOrNull);
                }

            };
    }

    public AbstractTabItemFactory getExperimentBrowser()
    {
        return getExperimentBrowser(null, null, null);
    }

    public AbstractTabItemFactory getPropertyTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PropertyTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROPERTY_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROPERTY_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            PropertyTypeRegistrationForm.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeRegistrationForm.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROPERTY_TYPE,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROPERTY_TYPE_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeAssignmentBrowser(final EntityType entity, final boolean isEntityTypeEdit)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PropertyTypeAssignmentGrid.create(viewContext, entity, null, isEntityTypeEdit);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    if (entity != null)
                    {
                        return PropertyTypeAssignmentGrid.BROWSER_ID + " " + entity.getEntityKind().name() + " " + entity.getCode();
                    } else
                    {
                        return PropertyTypeAssignmentGrid.BROWSER_ID;
                    }
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ASSIGNMENT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    if (entity != null)
                    {
                        return getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS) + " " + entity.getEntityKind().name() + " " + entity.getCode();
                    } else
                    {
                        return getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS);
                    }
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeExperimentTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.EXPERIMENT,
                Dict.ASSIGN_EXPERIMENT_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeMaterialTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.MATERIAL,
                Dict.ASSIGN_MATERIAL_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeDataSetTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.DATA_SET,
                Dict.ASSIGN_DATA_SET_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeSampleTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.SAMPLE, Dict.ASSIGN_SAMPLE_PROPERTY_TYPE);
    }

    private AbstractTabItemFactory getPropertyTypeAssignmentForm(final EntityKind entityKind,
            final String tabTitleMessageKey)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            PropertyTypeAssignmentForm.create(viewContext, entityKind);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeAssignmentForm.createId(entityKind);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ASSIGNMENT,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(tabTitleMessageKey);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetSearch()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser = DataSetSearchHitGrid.create(viewContext);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return DataSetSearchHitGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.SEARCH);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_SEARCH);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSearchLink(EntityKind.DATA_SET);
                }
            };
    }

    public AbstractTabItemFactory getSampleSearch()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser = SampleSearchHitGrid.create(viewContext);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return SampleSearchHitGrid.SEARCH_BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.SEARCH);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_SEARCH);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSearchLink(EntityKind.SAMPLE);
                }
            };
    }

    public AbstractTabItemFactory getSampleTypeBrowser()
    {
        final ComponentProvider componentProvider = this;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = SampleTypeGrid.create(viewContext, componentProvider);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE_TYPE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public AbstractTabItemFactory getMaterialTypeBrowser()
    {
        final ComponentProvider componentProvider = this;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = MaterialTypeGrid.create(viewContext, componentProvider);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return MaterialTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getNewEntityTypeForm(final EntityKind kind, final EntityType type)
    {
        final ComponentProvider componentProvider = this;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component = NewEntityTypeForm.create(kind, type, viewContext, componentProvider);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return NewEntityTypeForm.getTabId(kind, type);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ADMINISTRATION, HelpPageAction.VIEW);
                }

                @Override
                public String getTabTitle()
                {
                    String entityKind = EntityTypeUtils.translatedEntityKindForUI(viewContext, kind);
                    if (type == null) // Create new entity option
                    {
                        return "New " + entityKind + " Type";
                    } else
                    // Edit existing entity option
                    {
                        return "Edit " + entityKind + " Type " + type.getCode();
                    }
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getExperimentTypeBrowser()
    {
        final ComponentProvider componentProvider = this;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = ExperimentTypeGrid.create(viewContext, componentProvider);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetTypeBrowser()
    {
        final ComponentProvider componentProvider = this;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = DataSetTypeGrid.create(viewContext, componentProvider);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetUploadTab(final String initialSampleIdentifierOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            DataSetUploadForm.create(viewContext, initialSampleIdentifierOrNull);
                    return createRegistrationTabWithoutCloseConfirmation(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetUploadForm.createId(initialSampleIdentifierOrNull);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_UPLOAD);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getFileFormatTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = FileFormatTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return FileFormatTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.FILE_TYPE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.FILE_FORMAT_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory createGeneralImport()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    TabContent libraryImportTab = new GeneralImportComponent(viewContext);
                    return createRegistrationTab(getTabTitle(),
                            DatabaseModificationAwareComponent.wrapUnaware(libraryImportTab));
                }

                @Override
                public String getId()
                {
                    return GeneralImportComponent.createId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.GENERAL_IMPORT,
                            HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return GeneralImportComponent.getTabTitle(viewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getCustomImport()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    CustomImportComponent customImportTab = new CustomImportComponent(viewContext);
                    return createRegistrationTab(getTabTitle(),
                            DatabaseModificationAwareComponent.wrapUnaware(customImportTab));
                }

                @Override
                public String getId()
                {
                    return CustomImportComponent.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.CUSTOM_IMPORT,
                            HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.CUSTOM_IMPORT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public IMainPanel tryGetMainTabPanel()
    {
        return mainTabPanelOrNull;
    }

    public void setMainPanel(IMainPanel mainTabPanel)
    {
        this.mainTabPanelOrNull = mainTabPanel;
    }

    public AbstractTabItemFactory getLoggingConsole()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return createSimpleTab(getTabTitle(), LoggingConsole.create(viewContext), false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    // null would be better
                    return new HelpPageIdentifier(HelpPageDomain.ADMINISTRATION,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getId()
                {
                    return LoggingConsole.ID;
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.LOGGING_CONSOLE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public AbstractTabItemFactory createWebApp(final WebApp webApp)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    WebAppUrl url =
                            new WebAppUrl(Window.Location.getProtocol(), Window.Location.getHost(),
                                    Window.Location.getPath(), webApp.getCode(), viewContext
                                            .getModel().getSessionContext().getSessionID());
                    return createRegistrationTab(getTabTitle(),
                            DatabaseModificationAwareComponent
                                    .wrapUnaware(new WebAppComponent(url)));
                }

                @Override
                public String getId()
                {
                    return WebAppComponent.getId(webApp.getCode());
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.WEB_APP, HelpPageAction.VIEW);
                }

                @Override
                public String getTabTitle()
                {
                    return webApp.getLabel();
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }
}
