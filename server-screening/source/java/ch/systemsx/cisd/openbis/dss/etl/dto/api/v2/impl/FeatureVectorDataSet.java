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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.IFeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author Franz-Josef Elmer
 */
public class FeatureVectorDataSet extends DataSet<FeatureVectorDataSetInformation> implements
        IFeatureVectorDataSet
{
    private final DataSet<FeatureVectorDataSetInformation> dataSet;

    public FeatureVectorDataSet(DataSet<FeatureVectorDataSetInformation> dataSet,
            IEncapsulatedOpenBISService service)
    {
        super(dataSet.getRegistrationDetails(), dataSet.getDataSetStagingFolder(), service);
        this.dataSet = dataSet;
    }

    @Override
    public void setAnalysisProcedure(String analysisProcedure)
    {
        dataSet.getRegistrationDetails().getDataSetInformation()
                .setAnalysisProcedure(analysisProcedure);
    }

    @Override
    public boolean equals(Object obj)
    {
        return dataSet.equals(obj);
    }

    @Override
    public DataSetRegistrationDetails<? extends FeatureVectorDataSetInformation> getRegistrationDetails()
    {
        return dataSet.getRegistrationDetails();
    }

    @Override
    public String getDataSetCode()
    {
        return dataSet.getDataSetCode();
    }

    @Override
    public IExperimentImmutable getExperiment()
    {
        return dataSet.getExperiment();
    }

    @Override
    public ISampleImmutable getSample()
    {
        return dataSet.getSample();
    }

    @Override
    public String getFileFormatType()
    {
        return dataSet.getFileFormatType();
    }

    @Override
    public int getSpeedHint()
    {
        return dataSet.getSpeedHint();
    }

    @Override
    public String getDataSetType()
    {
        return dataSet.getDataSetType();
    }

    @Override
    public DataSetType getDataSetTypeWithPropertyTypes()
    {
        return dataSet.getDataSetTypeWithPropertyTypes();
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return dataSet.getPropertyValue(propertyCode);
    }

    @Override
    public List<String> getAllPropertyCodes()
    {
        return dataSet.getAllPropertyCodes();
    }

    @Override
    public List<String> getParentDatasets()
    {
        return dataSet.getParentDatasets();
    }

    @Override
    public List<String> getContainedDataSetCodes()
    {
        return dataSet.getContainedDataSetCodes();
    }

    @Override
    public List<IDataSetImmutable> getChildrenDataSets()
    {
        return dataSet.getChildrenDataSets();
    }

    @Override
    public String getContainerDataSet()
    {
        return dataSet.getContainerDataSet();
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem()
    {
        return dataSet.getExternalDataManagementSystem();
    }

    @Override
    public String getExternalCode()
    {
        return dataSet.getExternalCode();
    }

    @Override
    public int hashCode()
    {
        return dataSet.hashCode();
    }

    @Override
    public void setExperiment(IExperimentImmutable experiment)
    {
        dataSet.setExperiment(experiment);
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        dataSet.setSample(sampleOrNull);
    }

    @Override
    public void setFileFormatType(String fileFormatTypeCode)
    {
        dataSet.setFileFormatType(fileFormatTypeCode);
    }

    @Override
    public boolean isMeasuredData()
    {
        return dataSet.isMeasuredData();
    }

    @Override
    public void setMeasuredData(boolean measuredData)
    {
        dataSet.setMeasuredData(measuredData);
    }

    @Override
    public void setSpeedHint(int speedHint)
    {
        dataSet.setSpeedHint(speedHint);
    }

    @Override
    public void setDataSetType(String dataSetTypeCode)
    {
        dataSet.setDataSetType(dataSetTypeCode);
    }

    @Override
    public void setDataSetKind(DataSetKind dataSetKind)
    {
        dataSet.setDataSetKind(dataSetKind);
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        dataSet.setPropertyValue(propertyCode, propertyValue);
    }

    @Override
    public void setParentDatasets(List<String> parentDatasetCodes)
    {
        dataSet.setParentDatasets(parentDatasetCodes);
    }

    @Override
    public boolean isContainerDataSet()
    {
        return dataSet.isContainerDataSet();
    }

    @Override
    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        dataSet.setContainedDataSetCodes(containedDataSetCodes);
    }

    @Override
    public boolean isContainedDataSet()
    {
        return dataSet.isContainedDataSet();
    }

    @Override
    public void setExternalDataManagementSystem(
            IExternalDataManagementSystemImmutable externalDataManagementSystem)
    {
        dataSet.setExternalDataManagementSystem(externalDataManagementSystem);
    }

    @Override
    public boolean isLinkDataSet()
    {
        return dataSet.isLinkDataSet();
    }

    @Override
    public void setExternalCode(String externalCode)
    {
        dataSet.setExternalCode(externalCode);
    }

    @Override
    public boolean isNoFileDataSet()
    {
        return dataSet.isNoFileDataSet();
    }

    @Override
    public String toString()
    {
        return dataSet.toString();
    }

    @Override
    public File tryDataSetContents()
    {
        return dataSet.tryDataSetContents();
    }

}
