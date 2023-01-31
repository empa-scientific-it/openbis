/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.MultiKeyMap;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

class MasterData
{
    private final INameTranslator nameTranslator;

    private Map<String, FileFormatType> fileFormatTypesToProcess = new HashMap<String, FileFormatType>();

    private Map<String, ExternalDms> externalDataManagementSystemsToProcess = new HashMap<>();

    private Map<String, Script> validationPluginsToProcess = new HashMap<String, Script>();

    private Map<String, NewVocabulary> vocabulariesToProcess = new HashMap<String, NewVocabulary>();

    private Map<String, PropertyType> propertyTypesToProcess = new HashMap<String, PropertyType>();

    private Map<String, SampleType> sampleTypesToProcess = new HashMap<String, SampleType>();

    private Map<String, DataSetType> dataSetTypesToProcess = new HashMap<String, DataSetType>();

    private Map<String, ExperimentType> experimentTypesToProcess = new HashMap<String, ExperimentType>();

    private Map<String, MaterialType> materialTypesToProcess = new HashMap<String, MaterialType>();

    private MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess = new MultiKeyMap<String, List<NewETPTAssignment>>();

    private NameMapper vocabularyNameMapper;

    private NameMapper propertyTypeNameMapper;

    public MasterData(INameTranslator nameTranslator)
    {
        this.nameTranslator = nameTranslator;
    }

    public INameTranslator getNameTranslator()
    {
        return nameTranslator;
    }

    public MultiKeyMap<String, List<NewETPTAssignment>> getPropertyAssignmentsToProcess()
    {
        return propertyAssignmentsToProcess;
    }

    public Map<String, Script> getValidationPluginsToProcess()
    {
        return validationPluginsToProcess;
    }

    public void setValidationPluginsToProcess(Map<String, Script> validationPluginsToProcess)
    {
        this.validationPluginsToProcess = validationPluginsToProcess;
    }

    public Map<String, PropertyType> getPropertyTypesToProcess()
    {
        return propertyTypesToProcess;
    }

    public Map<String, DataSetType> getDataSetTypesToProcess()
    {
        return dataSetTypesToProcess;
    }

    public Map<String, ExperimentType> getExperimentTypesToProcess()
    {
        return experimentTypesToProcess;
    }

    public Map<String, MaterialType> getMaterialTypesToProcess()
    {
        return materialTypesToProcess;
    }

    public Map<String, SampleType> getSampleTypesToProcess()
    {
        return sampleTypesToProcess;
    }

    public Map<String, NewVocabulary> getVocabulariesToProcess()
    {
        return vocabulariesToProcess;
    }

    public Map<String, FileFormatType> getFileFormatTypesToProcess()
    {
        return fileFormatTypesToProcess;
    }

    public NameMapper getVocabularyNameMapper()
    {
        return vocabularyNameMapper;
    }

    public NameMapper getPropertyTypeNameMapper()
    {
        return propertyTypeNameMapper;
    }

    public void setFileFormatTypesToProcess(Map<String, FileFormatType> fileFormatTypesToProcess)
    {
        this.fileFormatTypesToProcess = fileFormatTypesToProcess;
    }

    public Map<String, ExternalDms> getExternalDataManagementSystemsToProcess()
    {
        return externalDataManagementSystemsToProcess;
    }

    public void setExternalDataManagementSystemsToProcess(Map<String, ExternalDms> edmsToProcess)
    {
        this.externalDataManagementSystemsToProcess = edmsToProcess;
    }

    public void setVocabulariesToProcess(Map<String, NewVocabulary> vocabulariesToProcess)
    {
        this.vocabulariesToProcess = vocabulariesToProcess;
    }

    public void setPropertyTypesToProcess(Map<String, PropertyType> propertyTypesToProcess)
    {
        this.propertyTypesToProcess = propertyTypesToProcess;
    }

    public void setSampleTypesToProcess(Map<String, SampleType> sampleTypesToProcess)
    {
        this.sampleTypesToProcess = sampleTypesToProcess;
    }

    public void setDataSetTypesToProcess(Map<String, DataSetType> dataSetTypesToProcess)
    {
        this.dataSetTypesToProcess = dataSetTypesToProcess;
    }

    public void setExperimentTypesToProcess(Map<String, ExperimentType> experimentTypesToProcess)
    {
        this.experimentTypesToProcess = experimentTypesToProcess;
    }

    public void setMaterialTypesToProcess(Map<String, MaterialType> materialTypesToProcess)
    {
        this.materialTypesToProcess = materialTypesToProcess;
    }

    public void setPropertyAssignmentsToProcess(MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess)
    {
        this.propertyAssignmentsToProcess = propertyAssignmentsToProcess;
    }

    public void setVocabularyNameMapper(NameMapper vocabularyNameMapper)
    {
        this.vocabularyNameMapper = vocabularyNameMapper;
    }

    public void setPropertyTypeNameMapper(NameMapper propertyTypeNameMapper)
    {
        this.propertyTypeNameMapper = propertyTypeNameMapper;
    }
}