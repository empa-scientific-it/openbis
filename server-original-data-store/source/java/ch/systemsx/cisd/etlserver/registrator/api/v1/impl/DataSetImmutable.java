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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * A data set that has already been stored in openBIS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetImmutable extends AbstractDataSetImmutable
{
    private static final class NullExperiment implements IExperimentImmutable
    {
        private String message;

        NullExperiment(String dataSetCode)
        {
            message = "No experiment defined for data set '" + dataSetCode + "'.";
        }

        @Override
        public IObjectId getEntityId()
        {
            throw new UnsupportedOperationException(message);
        }

        @Override
        public String getExperimentIdentifier()
        {
            throw new UnsupportedOperationException(message);
        }

        @Override
        public boolean isExistingExperiment()
        {
            return false;
        }

        @Override
        public String getExperimentType()
        {
            return null;
        }

        @Override
        public String getPropertyValue(String propertyCode)
        {
            return null;
        }

        @Override
        public String getPermId()
        {
            throw new UnsupportedOperationException(message);
        }
    }

    protected final AbstractExternalData dataSet;

    public DataSetImmutable(AbstractExternalData dataSet, IEncapsulatedBasicOpenBISService service)
    {
        super(service);
        this.dataSet = dataSet;
    }

    @Override
    public String getDataSetCode()
    {
        return dataSet.getCode();
    }

    @Override
    public IObjectId getEntityId()
    {
        return new DataSetCodeId(getDataSetCode());
    }

    @Override
    public IExperimentImmutable getExperiment()
    {
        Experiment experiment = dataSet.getExperiment();
        return experiment == null ? new NullExperiment(getDataSetCode()) : new ExperimentImmutable(experiment);
    }

    @Override
    public ISampleImmutable getSample()
    {
        Sample sample = dataSet.getSample();
        return sample == null ? null : new SampleImmutable(sample);
    }

    @Override
    public String getFileFormatType()
    {
        if (isContainerDataSet())
        {
            return null;
        } else
        {
            return dataSet.tryGetAsDataSet().getFileFormatType().getCode();
        }
    }

    @Override
    public boolean isMeasuredData()
    {
        return dataSet.isDerived() == false;
    }

    @Override
    public int getSpeedHint()
    {
        if (isContainerDataSet())
        {
            return Integer.MIN_VALUE;
        } else
        {
            return dataSet.tryGetAsDataSet().getSpeedHint();
        }
    }

    @Override
    public String getDataSetType()
    {
        return dataSet.getDataSetType().getCode();
    }

    @Override
    public DataSetKind getDataSetKind()
    {
        return DataSetKind.valueOf(dataSet.getDataSetKind().name());
    }

    @Override
    public DataSetType getDataSetTypeWithPropertyTypes()
    {
        return getDataSetTypeWithPropertyTypes(getDataSetType());
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(dataSet, propertyCode);
    }

    @Override
    public List<String> getAllPropertyCodes()
    {
        List<IEntityProperty> properties = dataSet.getProperties();
        List<String> codes = new ArrayList<String>();
        for (IEntityProperty property : properties)
        {
            codes.add(property.getPropertyType().getCode());
        }
        return codes;
    }

    @Override
    public List<String> getParentDatasets()
    {
        return Code.extractCodes(dataSet.getParents());
    }

    @Override
    public boolean isContainerDataSet()
    {
        return dataSet.isContainer();
    }

    @Override
    public boolean isContainedDataSet()
    {
        return dataSet.getContainerDataSets().isEmpty() == false;
    }

    @Override
    public String getContainerDataSet()
    {
        List<String> containerDataSets = getContainerDataSets();
        if (containerDataSets.isEmpty())
        {
            return null;
        }
        return containerDataSets.get(0);
    }

    @Override
    public List<String> getContainerDataSets()
    {
        List<ContainerDataSet> containerDataSets = dataSet.getContainerDataSets();
        List<String> result = new ArrayList<String>();
        for (ContainerDataSet containerDataSet : containerDataSets)
        {
            result.add(containerDataSet.getCode());
        }
        return result;
    }

    @Override
    public Integer getOrderInContainer(String containerDataSetCode)
    {
        return dataSet.getOrderInContainer(containerDataSetCode);
    }

    @Override
    public List<String> getContainedDataSetCodes()
    {
        if (isContainerDataSet())
        {
            return Code.extractCodes(dataSet.tryGetAsContainerDataSet().getContainedDataSets());
        } else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public List<IDataSetImmutable> getChildrenDataSets()
    {
        List<IDataSetImmutable> result = new ArrayList<IDataSetImmutable>();
        Collection<AbstractExternalData> children = dataSet.getChildren();
        if (children != null)
        {
            for (AbstractExternalData child : children)
            {
                result.add(new DataSetImmutable(child, service));
            }
        }
        return result;
    }

    @Override
    public boolean isLinkDataSet()
    {
        return dataSet.tryGetAsLinkDataSet() != null;
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem()
    {
        ExternalDataManagementSystem externalDMS =
                dataSet.tryGetAsLinkDataSet().getExternalDataManagementSystem();
        if (externalDMS != null)
        {
            return new ExternalDataManagementSystemImmutable(externalDMS);
        } else
        {
            return null;
        }
    }

    @Override
    public String getExternalCode()
    {
        return dataSet.tryGetAsLinkDataSet().getExternalCode();
    }

    @Override
    public boolean isNoFileDataSet()
    {
        return isContainerDataSet() || isLinkDataSet();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (false == (obj instanceof DataSetImmutable))
            return false;
        DataSetImmutable other = (DataSetImmutable) obj;
        if (getDataSetCode() == null)
        {
            if (other.getDataSetCode() != null)
                return false;
        } else if (!getDataSetCode().equals(other.getDataSetCode()))
            return false;
        return true;
    }

    @Override
    public boolean isPostRegistered()
    {
        return dataSet.isPostRegistered();
    }

}
