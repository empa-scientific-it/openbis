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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import static ch.systemsx.cisd.openbis.systemtest.base.BaseTest.id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class ExternalDataBuilder extends Builder<AbstractExternalData>
{
    private static int number;

    private IServiceForDataStoreServer etlService;

    private SampleIdentifier sampleIdentifier;

    private ExperimentIdentifier experimentIdentifier;

    private List<String> parentCodes;

    private List<String> componentCodes;

    private Map<String, String> properties;

    private String code;

    private String dataSetTypeCode;

    private DataSetKind dataSetKind;

    private boolean container;

    public ExternalDataBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, IServiceForDataStoreServer etlService)
    {
        super(commonServer, genericServer);
        this.etlService = etlService;
        this.code = "DS" + number++;
        this.parentCodes = new ArrayList<String>();
        this.container = false;
        this.componentCodes = new ArrayList<String>();
        this.properties = new HashMap<String, String>();
    }

    public ExternalDataBuilder withType(String type)
    {
        this.dataSetTypeCode = type;
        return this;
    }

    public ExternalDataBuilder withKind(DataSetKind kind)
    {
    	this.dataSetKind = kind;
    	return this;
    }

    public ExternalDataBuilder inSample(Sample sample)
    {
        this.sampleIdentifier = id(sample);
        if (sample.getExperiment() != null)
        {
            inExperiment(sample.getExperiment());
        }
        return this;
    }

    public ExternalDataBuilder inExperiment(Experiment experiment)
    {
        this.experimentIdentifier = new ExperimentIdentifier(experiment);
        return this;
    }

    public ExternalDataBuilder withParents(AbstractExternalData... dataSets)
    {
        for (AbstractExternalData parent : dataSets)
        {
            this.parentCodes.add(parent.getCode());
        }
        return this;
    }

    public ExternalDataBuilder withParent(AbstractExternalData dataSet)
    {
        return this.withParents(dataSet);
    }

    public ExternalDataBuilder asContainer()
    {
        this.container = true;
        return this;
    }

    public ExternalDataBuilder withComponents(String... dataSetCodes)
    {
        componentCodes.addAll(Arrays.asList(dataSetCodes));
        return this;
    }

    public ExternalDataBuilder withComponent(AbstractExternalData data)
    {
        return this.withComponents(data.getCode());
    }

    public ExternalDataBuilder withProperty(String propertyTypeCode, String value)
    {
        properties.put(propertyTypeCode, value);
        return this;
    }

    @Override
    public AbstractExternalData create()
    {
        NewExternalData data = get();

        if (this.sampleIdentifier != null)
        {
            etlService.registerDataSet(sessionToken, sampleIdentifier, data);
        } else if (experimentIdentifier != null)
        {
            etlService.registerDataSet(sessionToken, experimentIdentifier, data);
        } else
        {
            throw new IllegalStateException("Neither sample nor experiment has been specified for data set "
                    + data.getCode());
        }

        return etlService.tryGetDataSet(sessionToken, this.code);

    }

    public NewExternalData get()
    {
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(dataSetTypeCode == null ? "DT" + number++ : dataSetTypeCode);
        registerDataSetType(dataSetType);

        NewExternalData data;
        if (this.container)
        {
            NewContainerDataSet cont = new NewContainerDataSet();
            cont.setContainedDataSetCodes(this.componentCodes);
            data = cont;
        } else
        {
            data = new NewExternalData();
        }
        data.setCode(this.code);
        data.setDataSetType(dataSetType);
        data.setDataSetKind(dataSetKind);
        data.setFileFormatType(new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE));
        data.setSampleIdentifierOrNull(this.sampleIdentifier);
        data.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
        data.setLocation(UUID.randomUUID().toString());
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataStoreCode("STANDARD");
        data.setExperimentIdentifierOrNull(this.experimentIdentifier);
        data.setParentDataSetCodes(this.parentCodes);
        List<NewProperty> dataSetProperties = new ArrayList<NewProperty>();
        Set<Entry<String, String>> set = properties.entrySet();
        for (Entry<String, String> entry : set)
        {
            dataSetProperties.add(new NewProperty(entry.getKey(), entry.getValue()));
        }
        data.setDataSetProperties(dataSetProperties);
        return data;
    }

    private void registerDataSetType(DataSetType dataSetType)
    {
        List<DataSetType> dataSetTypes = commonServer.listDataSetTypes(sessionToken);
        for (DataSetType type : dataSetTypes)
        {
            if (type.getCode().equals(dataSetType.getCode()))
            {
                if (properties.isEmpty() == false)
                {
                    Set<String> knownPropertyTypes = new HashSet<String>();
                    List<DataSetTypePropertyType> assignedPropertyTypes = type.getAssignedPropertyTypes();
                    for (DataSetTypePropertyType dtpt : assignedPropertyTypes)
                    {
                        knownPropertyTypes.add(dtpt.getPropertyType().getCode());
                    }
                    Set<String> unassignedPropertyTypes = properties.keySet();
                    unassignedPropertyTypes.removeAll(knownPropertyTypes);
                    registerAndAssignPropertyTypes(type, unassignedPropertyTypes);
                }
                return;
            }
        }
        commonServer.registerDataSetType(sessionToken, dataSetType);
        if (properties.isEmpty() == false)
        {
            registerAndAssignPropertyTypes(dataSetType, properties.keySet());
        }
    }

    protected void registerAndAssignPropertyTypes(DataSetType type, Set<String> unassignedPropertyTypes)
    {
        Set<String> knownPropertyTypes = getKnownPropertyTypes();
        for (String propertyTypeCode : unassignedPropertyTypes)
        {
            if (knownPropertyTypes.contains(propertyTypeCode) == false)
            {
                PropertyType propertyType = new PropertyType();
                propertyType.setCode(propertyTypeCode);
                DataType dataType = new DataType();
                dataType.setCode(DataTypeCode.VARCHAR);
                propertyType.setDataType(dataType);
                propertyType.setLabel(propertyTypeCode.toLowerCase());
                propertyType.setDescription("");
                commonServer.registerPropertyType(sessionToken, propertyType);
            }
            NewETPTAssignment assignment = new NewETPTAssignment();
            assignment.setEntityKind(type.getEntityKind());
            assignment.setEntityTypeCode(type.getCode());
            assignment.setPropertyTypeCode(propertyTypeCode);
            commonServer.assignPropertyType(sessionToken, assignment);
        }
    }

    private Set<String> getKnownPropertyTypes()
    {
        Set<String> knownPropertyTypes = new HashSet<String>();
        List<PropertyType> propertyTypes = commonServer.listPropertyTypes(sessionToken, false);
        for (PropertyType propertyType : propertyTypes)
        {
            knownPropertyTypes.add(propertyType.getCode());
        }
        return knownPropertyTypes;
    }

}