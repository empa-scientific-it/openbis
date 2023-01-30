/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Jakub Straszewski
 */
public class DataSetRegistrationCache
{
    private HashMap<ExperimentIdentifier, ExperimentPE> experiments = new HashMap<>();

    private HashMap<SampleIdentifier, SamplePE> samples = new HashMap<>();

    private HashMap<String, DataSetTypePE> dataSetTypes = new HashMap<>();

    private VocabularyPE storageFormatVocabulary;

    private HashMap<String, LocatorTypePE> locatorTypeByCode = new HashMap<>();

    private HashMap<String, FileFormatTypePE> fileFormatTypeByCode = new HashMap<>();

    private HashMap<String, DataStorePE> dataStores = new HashMap<>();

    private Map<EntityTypePE, List<EntityTypePropertyTypePE>> entityTypePropertyTypes = new HashMap<>();

    private Map<String, DataPE> dataSets = new HashMap<>();

    public Map<String, DataPE> getDataSets()
    {
        return dataSets;
    }

    public Map<EntityTypePE, List<EntityTypePropertyTypePE>> getEntityTypePropertyTypes()
    {
        return entityTypePropertyTypes;
    }

    public HashMap<String, DataStorePE> getDataStores()
    {
        return dataStores;
    }

    public HashMap<String, FileFormatTypePE> getFileFormatTypes()
    {
        return fileFormatTypeByCode;
    }

    public HashMap<String, LocatorTypePE> getLocatorTypes()
    {
        return locatorTypeByCode;
    }

    public VocabularyPE getStorageFormatVocabulary()
    {
        return storageFormatVocabulary;
    }

    public void setStorageFormatVocabulary(VocabularyPE storageFormatVocabulary)
    {
        this.storageFormatVocabulary = storageFormatVocabulary;
    }

    private IDataBO dataBO;

    public IDataBO getDataBO()
    {
        return dataBO;
    }

    public void setDataBO(IDataBO dataBO)
    {
        this.dataBO = dataBO;
    }

    public HashMap<ExperimentIdentifier, ExperimentPE> getExperiments()
    {
        return experiments;
    }

    public HashMap<SampleIdentifier, SamplePE> getSamples()
    {
        return samples;
    }

    public HashMap<String, DataSetTypePE> getDataSetTypes()
    {
        return dataSetTypes;
    }

}
