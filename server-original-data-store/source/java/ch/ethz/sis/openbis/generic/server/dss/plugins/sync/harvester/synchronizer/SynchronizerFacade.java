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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.SummaryUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
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

public class SynchronizerFacade implements ISynchronizerFacade
{
    private final String sessionToken;

    private final ICommonServer commonServer;

    private final IApplicationServerApi v3api;
    
    private final Logger operationLog;

    private Map<String, String> fileformatTypesToUpdate = new TreeMap<String, String>();

    private Set<String> fileformatTypesToAdd = new TreeSet<String>();

    private Map<String, String> propertyTypesToUpdate = new TreeMap<String, String>();

    private Set<String> propertyTypesToAdd = new TreeSet<String>();

    private Map<String, String> validationPluginsToUpdate = new TreeMap<String, String>();

    private Set<String> validationPluginsToAdd = new TreeSet<String>();

    private final boolean dryRun;

    private final boolean verbose;

    private Set<String> vocabulariesToAdd = new TreeSet<String>();

    private Map<String, UpdateSummary> vocabulariesToUpdate = new TreeMap<String, UpdateSummary>();

    // private Map<String, String> vocabularyTermsToUpdate = new TreeMap<String, String>();

    // private Map<String, List<VocabularyTerm>> vocabularyTermsToAdd = new TreeMap<String, List<VocabularyTerm>>();

    private Set<String> sampleTypesToAdd = new TreeSet<String>();

    private Set<String> experimentTypesToAdd = new TreeSet<String>();

    private Set<String> dataSetTypesToAdd = new TreeSet<String>();

    private Set<String> materialTypesToAdd = new TreeSet<String>();

    private Map<String, UpdateSummary> sampleTypesToUpdate = new TreeMap<>();

    private Map<String, UpdateSummary> experimentTypesToUpdate = new TreeMap<>();

    private Map<String, UpdateSummary> dataSetTypesToUpdate = new TreeMap<>();

    private Map<String, UpdateSummary> materialTypesToUpdate = new HashMap<>();

    public SynchronizerFacade(String openBisServerUrl, String harvesterUser, String harvesterPassword, boolean dryRun, boolean verbose,
            Logger operationLog)
    {
        this.commonServer = ServiceFinderUtils.getCommonServer(openBisServerUrl);
        this.v3api = ServiceProvider.getV3ApplicationService();
        this.sessionToken = ServiceFinderUtils.login(commonServer, harvesterUser, harvesterPassword);
        this.dryRun = dryRun;
        this.verbose = verbose || dryRun;
        this.operationLog = operationLog;
    }

    @Override
    public void updateFileFormatType(AbstractType type)
    {
        fileformatTypesToUpdate.put(type.getCode(), "Description: " + type.getDescription());
        if (dryRun == false)
        {
            commonServer.updateFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void registerFileFormatType(FileFormatType type)
    {
        fileformatTypesToAdd.add(type.getCode());
        if (dryRun == false)
        {
            commonServer.registerFileFormatType(sessionToken, type);
        }
    }

    @Override
    public void updatePropertyTypeAssignment(NewETPTAssignment newETPTAssignment, String diff)
    {
        Map<String, UpdateSummary> summaryMap = getEntityTypeSummaryMap(newETPTAssignment.getEntityKind());
        getEntityTypeSummary(summaryMap, newETPTAssignment.getEntityTypeCode())
                .update(newETPTAssignment.getPropertyTypeCode(), diff);
        if (dryRun == false)
        {
            commonServer.updatePropertyTypeAssignment(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void assignPropertyType(NewETPTAssignment newETPTAssignment)
    {
        Map<String, UpdateSummary> summaryMap = getEntityTypeSummaryMap(newETPTAssignment.getEntityKind());
        getEntityTypeSummary(summaryMap, newETPTAssignment.getEntityTypeCode())
                .add(newETPTAssignment.getPropertyTypeCode());
        if (dryRun == false)
        {
            commonServer.assignPropertyType(sessionToken, newETPTAssignment);
        }
    }

    @Override
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode, String entityTypeCode)
    {
        getEntityTypeSummary(getEntityTypeSummaryMap(entityKind), entityTypeCode).remove(propertyTypeCode);
        if (dryRun == false)
        {
            commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode, entityTypeCode);
        }
    }

    private Map<String, UpdateSummary> getEntityTypeSummaryMap(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case SAMPLE:
                return sampleTypesToUpdate;
            case EXPERIMENT:
                return experimentTypesToUpdate;
            case DATA_SET:
                return dataSetTypesToUpdate;
            case MATERIAL:
                return materialTypesToUpdate;
            default:
                throw new RuntimeException("Unknown entity kind: " + entityKind);
        }
    }

    @Override
    public void updatePropertyType(PropertyType propertyType, String diff)
    {
        propertyTypesToUpdate.put(propertyType.getCode(), diff);
        if (dryRun == false)
        {
            commonServer.updatePropertyType(sessionToken, propertyType);
        }
    }

    @Override
    public void registerPropertyType(PropertyType propertyType)
    {
        propertyTypesToAdd.add(propertyType.getCode());
        if (dryRun == false)
        {
            commonServer.registerPropertyType(sessionToken, propertyType);
        }
    }

    @Override
    public void updateValidationPlugin(Script script, String diff)
    {
        validationPluginsToUpdate.put(script.getName(), diff);
        if (dryRun == false)
        {
            commonServer.updateScript(sessionToken, script);
        }
    }

    @Override
    public void registerValidationPlugin(Script script)
    {
        validationPluginsToAdd.add(script.getName());
        if (dryRun == false)
        {
            commonServer.registerScript(sessionToken, script);
        }
    }

    @Override
    public void registerVocabulary(NewVocabulary vocab)
    {
        String vocabCode = CodeConverter.tryToBusinessLayer(vocab.getCode(), vocab.isManagedInternally());
        vocabulariesToAdd.add(vocabCode);
        if (dryRun == false)
        {
            commonServer.registerVocabulary(sessionToken, vocab);
        }
    }

    @Override
    public void updateVocabulary(Vocabulary vocab, String diff)
    {
        String vocabCode = CodeConverter.tryToBusinessLayer(vocab.getCode(), vocab.isManagedInternally());
        getVocabularySummary(vocabCode).update(diff);
        if (dryRun == false)
        {
            commonServer.updateVocabulary(sessionToken, vocab);
        }
    }

    @Override
    public void updateVocabularyTerm(String vocabularyCode, VocabularyTerm term, String diff)
    {
        getVocabularySummary(vocabularyCode).update(term.getCode(), diff);
        if (dryRun == false)
        {
            commonServer.updateVocabularyTerm(sessionToken, term);
        }
    }

    @Override
    public void registerSampleType(SampleType sampleType)
    {
        sampleTypesToAdd.add(sampleType.getCode());
        if (dryRun == false)
        {
            commonServer.registerSampleType(sessionToken, sampleType);
        }
    }

    @Override
    public void registerDataSetType(DataSetType dataSetType)
    {
        dataSetTypesToAdd.add(dataSetType.getCode());
        if (dryRun == false)
        {
            commonServer.registerDataSetType(sessionToken, dataSetType);
        }
    }

    @Override
    public void registerExperimentType(ExperimentType experimentType)
    {
        experimentTypesToAdd.add(experimentType.getCode());
        if (dryRun == false)
        {
            commonServer.registerExperimentType(sessionToken, experimentType);
        }
    }

    @Override
    public void registerMaterialType(MaterialType materialType)
    {
        materialTypesToAdd.add(materialType.getCode());
        if (dryRun == false)
        {
            commonServer.registerMaterialType(sessionToken, materialType);
        }
    }

    @Override
    public void updateSampleType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(sampleTypesToUpdate, entityType.getCode()).update(diff);
        if (dryRun == false)
        {
            commonServer.updateSampleType(sessionToken, entityType);
        }
    }

    @Override
    public void updateDataSetType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(dataSetTypesToUpdate, entityType.getCode()).update(diff);
        if (dryRun == false)
        {
            commonServer.updateDataSetType(sessionToken, entityType);
        }
    }

    @Override
    public void updateExperimentType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(experimentTypesToUpdate, entityType.getCode()).update(diff);
        if (dryRun == false)
        {
            commonServer.updateExperimentType(sessionToken, entityType);
        }
    }

    @Override
    public void updateMaterialType(EntityType entityType, String diff)
    {
        getEntityTypeSummary(materialTypesToUpdate, entityType.getCode()).update(diff);
        if (dryRun == false)
        {
            commonServer.updateMaterialType(sessionToken, entityType);
        }
    }

    @Override
    public void addVocabularyTerms(String vocabularyCode, TechId techId, List<VocabularyTerm> termsToBeAdded)
    {
        for (VocabularyTerm vocabularyTerm : termsToBeAdded)
        {
            getVocabularySummary(vocabularyCode).add(vocabularyTerm.getCode());
        }
        if (dryRun == false)
        {
            commonServer.addVocabularyTerms(sessionToken, techId, termsToBeAdded, null);
        }
    }

    @Override
    public void createExternalDataManagementSystems(List<ExternalDmsCreation> creations)
    {
        if (dryRun == false)
        {
            v3api.createExternalDataManagementSystems(sessionToken, creations);
        }
    }

    @Override
    public void updateExternalDataManagementSystems(List<ExternalDmsUpdate> updates)
    {
        if (dryRun == false)
        {
            v3api.updateExternalDataManagementSystems(sessionToken, updates);
        }
    }

    @Override
    public void printSummary()
    {
        if (verbose)
        {
            SummaryUtils.printAddedSummary(operationLog, fileformatTypesToAdd, "file format types");
            printUpdatedSummary(fileformatTypesToUpdate, "file format types");

            SummaryUtils.printAddedSummary(operationLog, validationPluginsToAdd, "validation plugins");
            printUpdatedSummary(validationPluginsToUpdate, "validation plugins");

            SummaryUtils.printAddedSummary(operationLog, vocabulariesToAdd, "vocabularies");
            printUpdateSummary(vocabulariesToAdd, vocabulariesToUpdate, "vocabularies");

            SummaryUtils.printAddedSummary(operationLog, propertyTypesToAdd, "property types");
            printUpdatedSummary(propertyTypesToUpdate, "property types");

            SummaryUtils.printAddedSummary(operationLog, experimentTypesToAdd, "experiment types");
            printUpdateSummary(experimentTypesToAdd, experimentTypesToUpdate, "experiment types");
            SummaryUtils.printAddedSummary(operationLog, sampleTypesToAdd, "sample types");
            printUpdateSummary(sampleTypesToAdd, sampleTypesToUpdate, "sample types");
            SummaryUtils.printAddedSummary(operationLog, dataSetTypesToAdd, "data set types");
            printUpdateSummary(dataSetTypesToAdd, dataSetTypesToUpdate, "data set types");
            SummaryUtils.printAddedSummary(operationLog, materialTypesToAdd, "material types");
            printUpdateSummary(materialTypesToAdd, materialTypesToUpdate, "material types");
        }
        SummaryUtils.printShortSummaryHeader(operationLog);
        SummaryUtils.printShortAddedSummary(operationLog, fileformatTypesToAdd.size(), "file format types");
        SummaryUtils.printShortUpdatedSummary(operationLog, fileformatTypesToUpdate.size(), "file format types");
        SummaryUtils.printShortAddedSummary(operationLog, validationPluginsToAdd.size(), "validation plugins");
        SummaryUtils.printShortUpdatedSummary(operationLog, validationPluginsToUpdate.size(), "validation plugins");
        SummaryUtils.printShortAddedSummary(operationLog, vocabulariesToAdd.size(), "vocabularies");
        printShortSummary(vocabulariesToAdd, vocabulariesToUpdate, "vocabularies", "terms");
        SummaryUtils.printShortAddedSummary(operationLog, propertyTypesToAdd.size(), "property types");
        SummaryUtils.printShortUpdatedSummary(operationLog, propertyTypesToUpdate.size(), "property types");
        SummaryUtils.printShortAddedSummary(operationLog, experimentTypesToAdd.size(), "experiment types");
        printShortSummary(experimentTypesToAdd, experimentTypesToUpdate, "experiment types", "property assignments");
        SummaryUtils.printShortAddedSummary(operationLog, sampleTypesToAdd.size(), "sample types");
        printShortSummary(sampleTypesToAdd, sampleTypesToUpdate, "sample types", "property assignments");
        SummaryUtils.printShortAddedSummary(operationLog, dataSetTypesToAdd.size(), "data set types");
        printShortSummary(dataSetTypesToAdd, dataSetTypesToUpdate, "data set types", "property assignments");
        SummaryUtils.printShortAddedSummary(operationLog, materialTypesToAdd.size(), "material types");
        printShortSummary(materialTypesToAdd, materialTypesToUpdate, "material types", "property assignments");
        SummaryUtils.printShortSummaryFooter(operationLog);
    }

    private void printShortSummary(Set<String> added, Map<String, UpdateSummary> updates, String type, String subType)
    {
        int numberOfUpdates = 0;
        int numberOfAdds = 0;
        int numberOfRemoves = 0;
        int numberOfUpdatedItems = 0;
        for (UpdateSummary updateSummary : updates.values())
        {
            if (added.contains(updateSummary.getItem()))
            {
                continue;
            }
            numberOfUpdatedItems++;
            numberOfUpdates += updateSummary.getNumberOfUpdates();
            numberOfAdds += updateSummary.getNumberOfAdds();
            numberOfRemoves += updateSummary.getNumberOfRemoves();
        }
        SummaryUtils.printShortUpdatedSummary(operationLog, numberOfUpdatedItems, type);
        SummaryUtils.printShortAddedSummaryDetail(operationLog, numberOfAdds, subType);
        SummaryUtils.printShortUpdatedSummaryDetail(operationLog, numberOfUpdates, subType);
        SummaryUtils.printShortRemovedSummaryDetail(operationLog, numberOfRemoves, subType);
    }

    private void printUpdateSummary(Set<String> addedEntityTypes, Map<String, UpdateSummary> summaries, String itemType)
    {
        List<String> details = new LinkedList<>();
        for (Entry<String, UpdateSummary> entry : summaries.entrySet())
        {
            String entityType = entry.getKey();
            if (addedEntityTypes.contains(entityType))
            {
                continue;
            }
            UpdateSummary summary = entry.getValue();
            String diff = summary.getDiff();
            details.add(entityType + " " + (diff == null ? "no basic changes" : diff));
            Map<String, String> assignmentChanges = summary.getChanges();
            for (Entry<String, String> entry2 : assignmentChanges.entrySet())
            {
                details.add("    " + entry2.getKey() + ": " + entry2.getValue());
            }
        }
        SummaryUtils.printUpdatedSummary(operationLog, details, itemType);
    }

    private void printUpdatedSummary(Map<String, String> map, String type)
    {
        List<String> details = new LinkedList<>();
        for (String key : map.keySet())
        {
            details.add(key + " - " + map.get(key));
        }
        SummaryUtils.printUpdatedSummary(operationLog, details, type);
    }

    private UpdateSummary getEntityTypeSummary(Map<String, UpdateSummary> summariesByType, String entityTypeCode)
    {
        UpdateSummary summary = summariesByType.get(entityTypeCode);
        if (summary == null)
        {
            summary = new UpdateSummary(entityTypeCode);
            summariesByType.put(entityTypeCode, summary);
        }
        return summary;
    }

    private UpdateSummary getVocabularySummary(String vocabularyCode)
    {
        UpdateSummary vocabularySummary = vocabulariesToUpdate.get(vocabularyCode);
        if (vocabularySummary == null)
        {
            vocabularySummary = new UpdateSummary(vocabularyCode);
            vocabulariesToUpdate.put(vocabularyCode, vocabularySummary);
        }
        return vocabularySummary;
    }

    private static final class UpdateSummary
    {
        private final String item;

        private String diff;

        private Map<String, String> changes = new TreeMap<>();

        private int numberOfUpdates;

        private int numberOfAdds;

        private int numberOfRemoves;

        public UpdateSummary(String item)
        {
            this.item = item;
        }

        void update(String diff)
        {
            this.diff = diff;
        }

        void update(String item, String diff)
        {
            changes.put(item, diff);
            numberOfUpdates++;
        }

        void add(String item)
        {
            changes.put(item, "ADDED");
            numberOfAdds++;
        }

        void remove(String item)
        {
            changes.put(item, "REMOVED");
            numberOfRemoves++;
        }

        public String getItem()
        {
            return item;
        }

        public String getDiff()
        {
            return diff;
        }

        public Map<String, String> getChanges()
        {
            return changes;
        }

        public int getNumberOfUpdates()
        {
            return numberOfUpdates;
        }

        public int getNumberOfAdds()
        {
            return numberOfAdds;
        }

        public int getNumberOfRemoves()
        {
            return numberOfRemoves;
        }
    }

}
