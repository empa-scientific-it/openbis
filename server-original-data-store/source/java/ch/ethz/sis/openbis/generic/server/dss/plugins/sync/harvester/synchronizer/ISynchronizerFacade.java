/* Copyright 2016 ETH Zuerich, SIS
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

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Ganime Betul Akin
 */

public interface ISynchronizerFacade
{
    public void updateFileFormatType(AbstractType type);

    public void registerFileFormatType(FileFormatType type);

    public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment, String diff);

    public void assignPropertyType(NewETPTAssignment newETPTAssignment);

    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode);

    public void updatePropertyType(PropertyType propertyType, String diff);

    public void registerPropertyType(PropertyType propertyType);

    public void updateValidationPlugin(Script script, String diff);

    public void registerValidationPlugin(Script script);

    public void registerVocabulary(NewVocabulary vocab);

    public void updateVocabulary(Vocabulary vocab, String diff);

    public void registerSampleType(SampleType sampleType);

    public void registerDataSetType(DataSetType dataSetType);

    public void registerExperimentType(ExperimentType experimentType);

    public void registerMaterialType(MaterialType materialType);

    public void updateVocabularyTerm(String vocabularyCode, VocabularyTerm term, String diff);

    public void updateSampleType(EntityType incomingEntityType, String diff);

    public void updateDataSetType(EntityType incomingEntityType, String diff);

    public void updateExperimentType(EntityType incomingEntityType, String diff);

    public void updateMaterialType(EntityType incomingEntityType, String diff);

    public void addVocabularyTerms(String code, TechId techId, List<VocabularyTerm> termsToBeAdded);

    public void createExternalDataManagementSystems(List<ExternalDmsCreation> creations);

    public void updateExternalDataManagementSystems(List<ExternalDmsUpdate> updates);

    public void printSummary();
}
