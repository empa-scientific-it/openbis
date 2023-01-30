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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GeneralImportComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetBatchUpdateForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentBatchUpdateForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialBatchUpdateForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleBatchUpdateForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends AbstractClientPluginFactory<GenericViewContext>
{
    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final GenericViewContext createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new GenericViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    @Override
    @SuppressWarnings("unchecked")
    public final <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
        if (entityKind == null)
        {
            return new ClientPluginAdapter<T, I>()
                {
                    @Override
                    public Widget createBatchRegistrationForEntityType(final T entityType)
                    {
                        return createBatchUpdateForEntityType(entityType);
                    }

                    @Override
                    public Widget createBatchUpdateForEntityType(T entityType)
                    {
                        return new GeneralImportForm(getViewContext(),
                                GeneralImportComponent.createId(),
                                GeneralImportComponent.SESSION_KEY);
                    }

                };
        }
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin();
        }
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin();
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new DataSetClientPlugin();
        }

        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    @Override
    public final Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every sample type.");
    }

    private String getViewerTitle(final String entityKindDictKey,
            final IIdAndCodeHolder identifiable)
    {
        return AbstractViewer.getTitle(getViewContext(), entityKindDictKey, identifiable);
    }

    private String getEditorTitle(final String entityKindDictKey,
            final IIdAndCodeHolder identifiable)
    {
        return AbstractRegistrationForm.getEditTitle(getViewContext(), entityKindDictKey,
                identifiable);
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdAndCodeHolder>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent sampleViewer =
                                GenericSampleViewer.create(getViewContext(), entity);
                        return DefaultTabItem.create(getTabTitle(), sampleViewer, getViewContext(),
                                false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericSampleViewer.createId(entity);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.SAMPLE, entity);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleType,
                Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
                final ActionContext context)
        {
            GenericSampleRegistrationForm form =
                    new GenericSampleRegistrationForm(getViewContext(), inputWidgetDescriptions,
                            sampleType, context);
            return new DatabaseModificationAwareWidget(form, form);
        }

        @Override
        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }

        @Override
        public final Widget createBatchUpdateForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchUpdateForm(getViewContext(), sampleType);
        }

        @Override
        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericSampleEditForm.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getTabTitle(), component, getViewContext(),
                                true);
                    }

                    @Override
                    public String getId()
                    {
                        return AbstractGenericEntityRegistrationForm.createId(identifiable,
                                EntityKind.SAMPLE);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.EDIT);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getEditorTitle(Dict.SAMPLE, identifiable);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }

    }

    private final class MaterialClientPlugin extends
            ClientPluginAdapter<MaterialType, IIdAndCodeHolder>
    {

        @Override
        public final Widget createBatchRegistrationForEntityType(final MaterialType materialType)
        {
            return new GenericMaterialBatchRegistrationForm(getViewContext(), materialType);
        }

        @Override
        public Widget createBatchUpdateForEntityType(MaterialType entityType)
        {
            return new GenericMaterialBatchUpdateForm(getViewContext(), entityType);
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            final TechId techId = TechId.create(entity);
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent materialViewer =
                                GenericMaterialViewer.create(getViewContext(), techId);
                        return DefaultTabItem.create(getTabTitle(), materialViewer,
                                getViewContext(), false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericMaterialViewer.createId(techId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.MATERIAL, entity);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericMaterialEditForm
                                        .create(getViewContext(), identifiable, true);
                        return DefaultTabItem.create(getTabTitle(), component, getViewContext(),
                                true);
                    }

                    @Override
                    public String getId()
                    {
                        return AbstractGenericEntityRegistrationForm.createId(identifiable,
                                EntityKind.MATERIAL);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.EDIT);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getEditorTitle(Dict.MATERIAL, identifiable);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }
    }

    private final class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdAndCodeHolder>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent experimentViewer =
                                GenericExperimentViewer.create(getViewContext(),
                                        entity.getEntityType(), entity);
                        return DefaultTabItem.create(getTabTitle(), experimentViewer,
                                getViewContext(), false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericExperimentViewer.createId(entity);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.EXPERIMENT, entity);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public DatabaseModificationAwareWidget createRegistrationForEntityType(
                ExperimentType entityType,
                Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions,
                ActionContext context)
        {
            GenericExperimentRegistrationForm form =
                    new GenericExperimentRegistrationForm(getViewContext(),
                            inputWidgetDescriptions, context, entityType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        @Override
        public final Widget createBatchRegistrationForEntityType(final ExperimentType type)
        {
            return new GenericExperimentBatchRegistrationForm(getViewContext(), type);
        }

        @Override
        public Widget createBatchUpdateForEntityType(ExperimentType entityType)
        {
            return new GenericExperimentBatchUpdateForm(getViewContext(), entityType);
        }

        @Override
        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericExperimentEditForm.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getTabTitle(), component, getViewContext(),
                                true);
                    }

                    @Override
                    public String getId()
                    {
                        return AbstractGenericEntityRegistrationForm.createId(identifiable,
                                EntityKind.EXPERIMENT);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.EDIT);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getEditorTitle(Dict.EXPERIMENT, identifiable);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }
    }

    private final class DataSetClientPlugin extends
            ClientPluginAdapter<DataSetType, IIdAndCodeHolder>
    {

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent dataSetViewer =
                                GenericDataSetViewer.create(getViewContext(), entity);
                        return DefaultTabItem.create(getTabTitle(), dataSetViewer,
                                getViewContext(), false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericDataSetViewer.createId(entity);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.DATA_SET, entity);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericDataSetEditForm.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getTabTitle(), component, getViewContext(),
                                true);
                    }

                    @Override
                    public String getId()
                    {
                        return AbstractGenericEntityRegistrationForm.createId(identifiable,
                                EntityKind.DATA_SET);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.EDIT);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getEditorTitle(Dict.DATA_SET, identifiable);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }

        @Override
        public final Widget createBatchUpdateForEntityType(final DataSetType dataSetType)
        {
            return new GenericDataSetBatchUpdateForm(getViewContext(), dataSetType);
        }
    }

    @Override
    protected IModule maybeCreateModule()
    {
        return null;
    }

}
