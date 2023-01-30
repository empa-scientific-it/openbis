/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Implementation of {@link IDatasetLocationNode} that uses {@link AbstractExternalData} object as a source of data set locations.
 * 
 * @author pkupczyk
 */
public class ExternalDataLocationNode implements IDatasetLocationNode
{

    private final AbstractExternalData externalData;

    private final Integer orderInParentContainer;

    public ExternalDataLocationNode(AbstractExternalData externalData)
    {
        this(externalData, null);
    }

    private ExternalDataLocationNode(AbstractExternalData externalData, Integer orderInParentContainer)
    {
        if (externalData == null)
        {
            throw new IllegalArgumentException("ExternalData cannot be null");
        }
        this.externalData = externalData;
        this.orderInParentContainer = orderInParentContainer;
    }

    @Override
    public IDatasetLocation getLocation()
    {
        DatasetLocation datasetLocation = new DatasetLocation();
        datasetLocation.setDatasetCode(externalData.getCode());
        PhysicalDataSet dataSet = externalData.tryGetAsDataSet();
        datasetLocation.setDataSetLocation(dataSet == null ? null : dataSet.getLocation());
        datasetLocation.setDataStoreCode(externalData.getDataStore().getCode());
        datasetLocation.setDataStoreUrl(externalData.getDataStore().getHostUrl());
        datasetLocation.setOrderInContainer(orderInParentContainer);
        return datasetLocation;
    }

    @Override
    public boolean isContainer()
    {
        return externalData.isContainer();
    }

    @Override
    public Collection<IDatasetLocationNode> getComponents()
    {
        if (isContainer() == false)
        {
            return Collections.emptyList();
        }

        ContainerDataSet container = externalData.tryGetAsContainerDataSet();
        List<AbstractExternalData> components = container.getContainedDataSets();

        if (components != null)
        {
            TreeMap<String, IDatasetLocationNode> componentsLocationNodes =
                    new TreeMap<String, IDatasetLocationNode>();
            String containerCode = container.getCode();
            for (AbstractExternalData component : components)
            {
                componentsLocationNodes.put(component.getCode(),
                        new ExternalDataLocationNode(component, component.getOrderInContainer(containerCode)));
            }

            return componentsLocationNodes.values();
        } else
        {
            return Collections.emptyList();
        }
    }

}
