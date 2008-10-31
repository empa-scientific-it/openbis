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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleViewer extends LayoutContainer
{
    private static final String PREFIX = "generic-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final SampleGeneration sampleGeneration;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private Grid<SampleModel> partOfSamplesGrid;

    private Grid<ExternalDataModel> externalDataGrid;

    public GenericSampleViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final SampleGeneration sampleGeneration)
    {
        setId(ID_PREFIX + sampleGeneration.getGenerator().getIdentifier());
        setLayout(new BorderLayout());
        this.sampleGeneration = sampleGeneration;
        this.viewContext = viewContext;
        // Left panel
        add(createLeftPanel(), createLeftBorderLayoutData());
        // Right panel
        add(createRightPanel(), createRightBorderLayoutData());
    }

    private final static BorderLayoutData createRightBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        return data;
    }

    private final Component createRightPanel()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        // 'Part of' samples
        ContentPanel panel =
                createContentPanel(viewContext.getMessageProvider().getMessage("part_of_heading"));
        final ListLoader<BaseListLoadConfig> sampleLoader =
                createListLoader(createRpcProxyForPartOfSamples());
        final ListStore<SampleModel> sampleListStore = createListStore(sampleLoader);
        partOfSamplesGrid =
                new Grid<SampleModel>(sampleListStore, createPartOfSamplesColumnModel());
        partOfSamplesGrid.setLoadMask(true);
        panel.add(partOfSamplesGrid);
        container.add(panel, new RowData(1, 0.5, new Margins(0, 5, 5, 0)));
        // External data
        panel =
                createContentPanel(viewContext.getMessageProvider().getMessage(
                        "external_data_heading"));
        final ListLoader<BaseListLoadConfig> externalDataLoader =
                createListLoader(createRpcProxyForExternalData());
        final ListStore<ExternalDataModel> externalDataListStore =
                createListStore(externalDataLoader);
        externalDataGrid =
                new Grid<ExternalDataModel>(externalDataListStore, createExternalDataColumnModel());
        externalDataGrid.setLoadMask(true);
        panel.add(externalDataGrid);
        container.add(panel, new RowData(1, 0.5, new Margins(0, 5, 0, 0)));
        return container;
    }

    private final static ContentPanel createContentPanel(final String heading)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setHeading(heading);
        panel.setLayout(new FitLayout());
        panel.setBodyBorder(true);
        panel.setBorders(false);
        panel.setInsetBorder(true);
        return panel;
    }

    private final <T> ListLoader<BaseListLoadConfig> createListLoader(
            final RpcProxy<BaseListLoadConfig, BaseListLoadResult<T>> rpcProxy)
    {
        final BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>> baseListLoader =
                new BaseListLoader<BaseListLoadConfig, BaseListLoadResult<T>>(rpcProxy);
        return baseListLoader;
    }

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>> createRpcProxyForPartOfSamples()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<SampleModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
                {
                    final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
                    sampleCriteria.setContainerIdentifier(sampleGeneration.getGenerator()
                            .getIdentifier());
                    viewContext.getService().listSamples(sampleCriteria,
                            new ArrayList<PropertyType>(),
                            new ListSamplesCallback(viewContext, callback));
                }
            };
    }

    private final RpcProxy<BaseListLoadConfig, BaseListLoadResult<ExternalDataModel>> createRpcProxyForExternalData()
    {
        return new RpcProxy<BaseListLoadConfig, BaseListLoadResult<ExternalDataModel>>()
            {

                //
                // RpcProxy
                //

                @Override
                public final void load(final BaseListLoadConfig loadConfig,
                        final AsyncCallback<BaseListLoadResult<ExternalDataModel>> callback)
                {
                    viewContext.getService().listExternalData(
                            sampleGeneration.getGenerator().getIdentifier(),
                            new ListExternalDataCallback(viewContext, callback));
                }
            };
    }

    private final <T extends ModelData> ListStore<T> createListStore(
            final ListLoader<BaseListLoadConfig> loader)
    {
        return new ListStore<T>(loader);
    }

    private final ColumnModel createPartOfSamplesColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        configs.add(ColumnConfigFactory.createCodeColumnConfig(messageProvider));
        configs.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
        return new ColumnModel(configs);
    }

    private final ColumnModel createExternalDataColumnModel()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        configs.add(ColumnConfigFactory.createCodeColumnConfig(messageProvider));
        configs.add(ColumnConfigFactory.createRegistrationDateColumnConfig(messageProvider));
        configs.add(ColumnConfigFactory.createRegistratorColumnConfig(messageProvider));
        configs.add(createLocationColumnConfig());
        configs.add(createFileFormatTypeColumnConfig());
        return new ColumnModel(configs);
    }

    private final ColumnConfig createFileFormatTypeColumnConfig()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.FILE_FORMAT_TYPE);
        columnConfig.setHeader(viewContext.getMessageProvider().getMessage("file_format_type"));
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private final ColumnConfig createLocationColumnConfig()
    {
        final ColumnConfig columnConfig = ColumnConfigFactory.createMenuDisableColumnConfig();
        columnConfig.setId(ModelDataPropertyNames.LOCATION);
        columnConfig.setHeader(viewContext.getMessageProvider().getMessage("location"));
        columnConfig.setWidth(100);
        return columnConfig;
    }

    private final static BorderLayoutData createLeftBorderLayoutData()
    {
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 300, 100, 500);
        data.setMargins(new Margins(0, 5, 0, 0));
        data.setSplit(true);
        data.setCollapsible(true);
        data.setFloatable(false);
        return data;
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final SampleGeneration sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getGenerator();
        final SampleType sampleType = sample.getSampleType();
        final Invalidation invalidation = sample.getInvalidation();
        final Sample[] generated = sampleGeneration.getGenerated();
        properties.put(messageProvider.getMessage("sample"), sample.getCode());
        properties.put(messageProvider.getMessage("sample_type"), sampleType);
        properties.put(messageProvider.getMessage("registrator"), sample.getRegistrator());
        properties.put(messageProvider.getMessage("registration_date"), sample
                .getRegistrationDate());
        if (generated.length > 0)
        {
            properties.put(messageProvider.getMessage("generated_samples"), generated);
        }
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage("invalidation"), invalidation);
        }
        Sample generatedFrom = sample;
        for (int i = 0; i < sampleType.getGeneratedFromHierarchyDepth() && generatedFrom != null; i++)
        {
            generatedFrom = generatedFrom.getGeneratedFrom();
            properties.put(messageProvider.getMessage("generated_from", i + 1), generatedFrom);
        }
        for (final SampleProperty property : sample.getProperties())
        {
            final String simpleCode =
                    property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(simpleCode, property);
        }
        return properties;
    }

    private final Component createLeftPanel()
    {
        final ContentPanel panel = new ContentPanel(new FlowLayout());
        panel.setHeading(viewContext.getMessageProvider().getMessage("sample_properties_heading"));
        panel.add(createPropertyGrid());
        return panel;
    }

    private final PropertyGrid createPropertyGrid()
    {
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        final Map<String, Object> properties = createProperties(messageProvider, sampleGeneration);
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                .createSampleTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .createSamplePropertyValueRenderer(messageProvider, true));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(SampleProperty.class, PropertyValueRenderers
                .createSamplePropertyPropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    /**
     * Load the stores.
     */
    public final void loadStores()
    {
        externalDataGrid.getStore().getLoader().load();
        partOfSamplesGrid.getStore().getLoader().load();
    }

    //
    // Helper classes
    //

    final class ListSamplesCallback extends AbstractAsyncCallback<List<Sample>>
    {
        private final AsyncCallback<BaseListLoadResult<SampleModel>> delegate;

        ListSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<SampleModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final List<Sample> result)
        {
            final List<SampleModel> sampleModels = SampleModel.asSampleModels(result);
            final BaseListLoadResult<SampleModel> baseListLoadResult =
                    new BaseListLoadResult<SampleModel>(sampleModels);
            delegate.onSuccess(baseListLoadResult);
        }
    }

    final class ListExternalDataCallback extends AbstractAsyncCallback<List<ExternalData>>
    {
        private final AsyncCallback<BaseListLoadResult<ExternalDataModel>> delegate;

        ListExternalDataCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final AsyncCallback<BaseListLoadResult<ExternalDataModel>> callback)
        {
            super(viewContext);
            this.delegate = callback;
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void finishOnFailure(final Throwable caught)
        {
            delegate.onFailure(caught);
        }

        @Override
        protected final void process(final List<ExternalData> result)
        {
            final List<ExternalDataModel> externalDataModels =
                    ExternalDataModel.asExternalDataModels(result);
            final BaseListLoadResult<ExternalDataModel> baseListLoadResult =
                    new BaseListLoadResult<ExternalDataModel>(externalDataModels);
            delegate.onSuccess(baseListLoadResult);
        }
    }
}