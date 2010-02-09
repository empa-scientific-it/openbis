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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.GeneMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateDatasetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateSampleViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>screening</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ClientPluginFactory extends AbstractClientPluginFactory<ScreeningViewContext>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final ScreeningViewContext createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        Set<String> types = new HashSet<String>();
        if (entityKind == EntityKind.SAMPLE)
        {
            types.add(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
            types.add(ScreeningConstants.WELL_PLUGIN_TYPE_CODE);
        } else if (entityKind == EntityKind.MATERIAL)
        {
            types.add(ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
        } else if (entityKind == EntityKind.DATA_SET)
        {
            types.add(ScreeningConstants.IMAGE_DATASET_PLUGIN_TYPE_CODE);
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, I extends IIdentifiable> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        ScreeningViewContext viewContext = getViewContext();
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin(viewContext);
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin(viewContext);
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new DatasetClientPlugin(viewContext);
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class MaterialClientPlugin extends DelegatedClientPlugin<MaterialType>
    {
        private MaterialClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.MATERIAL);
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable materialId)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        IViewContext<IScreeningClientServiceAsync> viewContext = getViewContext();
                        final DatabaseModificationAwareComponent viewer =
                                GeneMaterialViewer.create(viewContext, materialId, null);
                        return createMaterialViewerTab(materialId, viewer, viewContext);
                    }

                    public String getId()
                    {
                        return GeneMaterialViewer.createId(materialId);
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return GeneMaterialViewer.getHelpPageIdentifier();
                    }
                };
        }
    }

    /** opens gene viewer with a selected experiment */
    public static final void openGeneMaterialViewer(final IIdentifiable materialId,
            final ExperimentIdentifier experimentIdentifier,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        ITabItemFactory tab = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    final DatabaseModificationAwareComponent viewer =
                            GeneMaterialViewer
                                    .create(viewContext, materialId, experimentIdentifier);
                    return createMaterialViewerTab(materialId, viewer, viewContext);
                }

                public String getId()
                {
                    return GeneMaterialViewer.createId(materialId);
                }

                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return GeneMaterialViewer.getHelpPageIdentifier();
                }
            };
        DispatcherHelper.dispatchNaviEvent(tab);
    }

    private static ITabItem createMaterialViewerTab(final IIdentifiable materialId,
            final DatabaseModificationAwareComponent viewer, IViewContext<?> viewContext)
    {
        return createViewerTab(viewer, materialId, Dict.MATERIAL, viewContext);
    }

    private final class DatasetClientPlugin extends DelegatedClientPlugin<DataSetType>
    {
        private ScreeningViewContext screeningViewContext;

        private DatasetClientPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.DATA_SET);
            this.screeningViewContext = viewContext;
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateDatasetViewer.create(screeningViewContext, identifiable);
                        return createViewerTab(viewer, identifiable, Dict.DATA_SET,
                                screeningViewContext);
                    }

                    public String getId()
                    {
                        final TechId sampleId = TechId.create(identifiable);
                        return PlateDatasetViewer.createId(sampleId);
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Dataset Viewer");
                    }
                };
        }
    }

    private final class SampleClientPlugin extends DelegatedClientPlugin<SampleType>
    {
        private ScreeningViewContext screeningViewContext;

        private SampleClientPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.SAMPLE);
            this.screeningViewContext = viewContext;
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateSampleViewer.create(screeningViewContext, identifiable);
                        return createViewerTab(viewer, identifiable, Dict.SAMPLE,
                                screeningViewContext);
                    }

                    public String getId()
                    {
                        final TechId sampleId = TechId.create(identifiable);
                        return PlateSampleViewer.createId(sampleId);
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Sample Viewer");
                    }
                };
        }
    }

    private static ITabItem createViewerTab(DatabaseModificationAwareComponent viewer,
            ICodeProvider codeProvider, String dictTitleKey, IViewContext<?> viewContext)
    {
        String title = getViewerTitle(dictTitleKey, codeProvider, viewContext);
        return DefaultTabItem.create(title, viewer, viewContext, false);
    }

    private static String getViewerTitle(String dictTitleKey, ICodeProvider codeProvider,
            IMessageProvider messageProvider)
    {
        return AbstractViewer.getTitle(messageProvider, dictTitleKey, codeProvider);
    }

    /**
     * delegates all operations to generic plugin, should be subclasssed and the needed
     * functionality can override the default behaviour
     */
    private static class DelegatedClientPlugin<T extends EntityType> implements
            IClientPlugin<T, IIdentifiable>
    {
        private final IClientPlugin<T, IIdentifiable> delegator;

        private DelegatedClientPlugin(IViewContext<?> viewContext, EntityKind entityKind)
        {
            this.delegator = createGenericClientFactory(viewContext).createClientPlugin(entityKind);
        }

        private static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory createGenericClientFactory(
                IViewContext<?> viewContext)
        {
            ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory clientPluginFactory =
                    new ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory(
                            viewContext.getCommonViewContext());
            return clientPluginFactory;
        }

        public ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return delegator.createEntityViewer(identifiable);
        }

        public Widget createBatchRegistrationForEntityType(final T entityType)
        {
            return delegator.createBatchRegistrationForEntityType(entityType);
        }

        public Widget createBatchUpdateForEntityType(final T entityType)
        {
            return delegator.createBatchUpdateForEntityType(entityType);
        }

        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return delegator.createEntityEditor(identifiable);
        }

        public DatabaseModificationAwareWidget createRegistrationForEntityType(T entityType)
        {
            return delegator.createRegistrationForEntityType(entityType);
        }
    }
}
