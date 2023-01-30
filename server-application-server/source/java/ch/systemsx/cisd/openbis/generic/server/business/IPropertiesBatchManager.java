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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * Interface of classes which manage managed properties in batch updates.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPropertiesBatchManager
{
    public void manageProperties(SampleTypePE sampleType, List<NewSample> newSamples,
            PersonPE registrator);

    public void manageProperties(ExperimentTypePE experimentType,
            List<? extends NewBasicExperiment> experiments, PersonPE registrator);

    public void manageProperties(MaterialTypePE materialType, List<NewMaterial> materials,
            PersonPE registrator);

    public void manageProperties(DataSetTypePE dataSetType, List<NewDataSet> dataSets,
            PersonPE tryGetPerson);
}