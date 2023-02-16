/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * Builder of objects of type {@link ExternalDataPE}.
 * 
 * @author Franz-Josef Elmer
 */
public class ExternalDataPEBuilder
{
    private final ExternalDataPE dataSet = new ExternalDataPE();

    public ExternalDataPEBuilder(long id)
    {
        dataSet.setId(id);
    }

    public ExternalDataPE getDataSet()
    {
        return dataSet;
    }

    public ExternalDataPEBuilder code(String code)
    {
        dataSet.setCode(code);
        return this;
    }

    public ExternalDataPEBuilder store(String dataStoreCode)
    {
        DataStorePE store = new DataStorePE();
        store.setCode(dataStoreCode);
        dataSet.setDataStore(store);
        return this;
    }

    public ExternalDataPEBuilder type(String typeCode)
    {
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode(typeCode);
        dataSet.setDataSetType(dataSetType);
        return this;
    }

    public ExternalDataPEBuilder kind(String dataSetKind)
    {
    	dataSet.setDataSetKind(dataSetKind);
    	return this;
    }
    
    public ExternalDataPEBuilder experiment(ExperimentPE experiment)
    {
        dataSet.setExperiment(experiment);
        return this;
    }

    public ExternalDataPEBuilder sample(SamplePE sample)
    {
        dataSet.setSample(sample);
        return this;
    }

    public ExternalDataPEBuilder property(String code, DataTypeCode dataType, String value)
    {
        PropertyTypePE propertyType =
                CommonTestUtils.createPropertyType(code, dataType, null, null);
        DataSetTypePropertyTypePE dtpt = new DataSetTypePropertyTypePE();
        dtpt.setOrdinal(Long.valueOf(dataSet.getProperties().size()));
        dtpt.setPropertyType(propertyType);
        DataSetTypePE dataSetType = dataSet.getDataSetType();
        if (dataSetType == null)
        {
            throw new UserFailureException("Data set type has to be defined before properties.");
        }
        dataSetType.addDataSetTypePropertyType(dtpt);
        DataSetPropertyPE property = new DataSetPropertyPE();
        property.setEntityTypePropertyType(dtpt);
        property.setValue(value);
        dataSet.addProperty(property);
        return this;
    }

    public ExternalDataPEBuilder parent(DataPE parentDataSet)
    {
        RelationshipTypePE relationshipType = new RelationshipTypePE();
        relationshipType.setCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        DataSetRelationshipPE relationship =
                new DataSetRelationshipPE(parentDataSet, dataSet, relationshipType, null, new PersonPE());
        dataSet.addParentRelationship(relationship);
        parentDataSet.addChildRelationship(relationship);
        return this;
    }

}
