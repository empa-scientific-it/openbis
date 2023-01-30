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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IContentCopy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;

/**
 * {@link ContentPanel} containing dataset properties.
 * 
 * @author Piotr Buczek
 */
public class DataSetPropertiesPanel extends ContentPanel
{
    public static final String PROPERTIES_ID_PREFIX = GenericConstants.ID_PREFIX
            + "dataset-properties-section_";

    private final AbstractExternalData dataset;

    private final IViewContext<?> viewContext;

    public DataSetPropertiesPanel(final AbstractExternalData dataset, final IViewContext<?> viewContext)
    {
        setHeading("Data Set Properties");
        this.dataset = dataset;
        this.viewContext = viewContext;
        final PropertyGrid propertyGrid = createPropertyGrid();
        add(propertyGrid);
    }

    private final PropertyGrid createPropertyGrid()
    {
        final IMessageProvider messageProvider = viewContext;
        final Map<String, Object> properties = createProperties(viewContext);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + dataset.getIdentifier());
        propertyGrid.registerPropertyValueRenderer(DataSetType.class,
                PropertyValueRenderers.createDataSetTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(DataStore.class,
                PropertyValueRenderers.createDataStorePropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final DataSetType datasetType = dataset.getDataSetType();
        final Deletion deletion = dataset.getDeletion();
        final Sample sample = dataset.getSample();

        properties.put(messageProvider.getMessage(Dict.DATA_SET),
                new ExternalHyperlink(dataset.getPermId(), dataset.getPermlink()));

        properties.put(messageProvider.getMessage(Dict.DATA_SET_TYPE), datasetType);

        if (dataset.isLinkData())
        {
            LinkDataSet linkDataSet = dataset.tryGetAsLinkDataSet();
            int counter = 1;
            for (IContentCopy copy : linkDataSet.getCopies())
            {
                properties.put("Copy " + counter++, copy.getLocation());
            }
        }

        properties.put(messageProvider.getMessage(Dict.DATA_SET_KIND), dataset.getDataSetKind());

        properties.put(messageProvider.getMessage(Dict.SOURCE_TYPE), dataset.getSourceType());

        properties.put(messageProvider.getMessage(Dict.DATA_PRODUCER_CODE),
                dataset.getDataProducerCode());
        properties.put(messageProvider.getMessage(Dict.PRODUCTION_DATE),
                dataset.getProductionDate());

        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), dataset.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE),
                dataset.getRegistrationDate());
        List<ContainerDataSet> containerDataSets = dataset.getContainerDataSets();
        if (containerDataSets.size() == 1)
        {
            properties.put(messageProvider.getMessage(Dict.CONTAINER_DATA_SET), containerDataSets.get(0));
        }
        Experiment experiment = dataset.getExperiment();
        if (experiment != null)
        {
            properties.put(messageProvider.getMessage(Dict.PROJECT), experiment.getProject());
            properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment);
        }

        PhysicalDataSet concreteDataSet = dataset.tryGetAsDataSet();
        if (concreteDataSet != null)
        {
            properties.put(messageProvider.getMessage(Dict.LOCATION),
                    concreteDataSet.getFullLocation());
            if (viewContext.getModel().getApplicationInfo().isArchivingConfigured())
            {
                properties.put(messageProvider.getMessage(Dict.ARCHIVING_STATUS), concreteDataSet
                        .getStatus().getDescription());
                properties.put(messageProvider.getMessage(Dict.PRESENT_IN_ARCHIVE),
                        concreteDataSet.isPresentInArchive());
            }
            properties.put(messageProvider.getMessage(Dict.DATA_STORE),
                    concreteDataSet.getDataStore());
            properties.put(messageProvider.getMessage(Dict.IS_COMPLETE),
                    concreteDataSet.getComplete());
            properties.put(messageProvider.getMessage(Dict.FILE_FORMAT_TYPE), concreteDataSet
                    .getFileFormatType().getCode());
        }

        if (sample != null)
        {
            properties.put(messageProvider.getMessage(Dict.SAMPLE), sample);
        }
        if (deletion != null)
        {
            properties.put(messageProvider.getMessage(Dict.DELETION), deletion);
        }

        PropertiesPanelUtils.addMetaprojects(viewContext, properties, dataset.getMetaprojects());
        PropertiesPanelUtils.addEntityProperties(viewContext, properties, dataset.getProperties());

        return properties;
    }
}
