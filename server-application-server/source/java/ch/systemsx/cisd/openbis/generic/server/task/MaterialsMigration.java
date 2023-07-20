/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;

import java.util.*;

public class MaterialsMigration implements IMaintenanceTask {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MaterialsMigration.class);
    private static final int BATCH_SIZE = 10000;
    private static final String PREFIX = "MATERIAL.";
    private static final String SPACE_CODE = PREFIX + "GLOBAL";
    private static final String PROJECT_CODE = SPACE_CODE;
    private static final String EXPERIMENT_TYPE = "COLLECTION";
    private static final String EXPERIMENT_POSTFIX = "_COLLECTION";
    private static final String DESCRIPTION = "Used to hold objects instances from all migrated materials.";
    private static final String REASON = "Materials Migration.";

    private static Boolean doMaterialsMigrationInsertNew = null;
    private static Boolean doMaterialsMigrationDeleteOld = null;

    public MaterialsMigration() {
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        String doMaterialsMigrationInsertNewValue = PropertyUtils.getMandatoryProperty(properties, "doMaterialsMigrationInsertNew");
        doMaterialsMigrationInsertNew = Boolean.parseBoolean(doMaterialsMigrationInsertNewValue);
        String doMaterialsMigrationDeleteOldValue = PropertyUtils.getMandatoryProperty(properties, "doMaterialsMigrationDeleteOld");
        doMaterialsMigrationDeleteOld = Boolean.parseBoolean(doMaterialsMigrationDeleteOldValue);
    }

    @Override
    public void execute()
    {
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = null;
        try {
            sessionToken = v3.loginAsSystem();
            doMaterialsMigration(v3, sessionToken);
        } catch (Exception exception) {
            operationLog.error(MaterialsMigration.class.getSimpleName(), exception);
            if (sessionToken != null) {
                v3.logout(sessionToken);
            }
            throw exception;
        }
    }

    private static void info(String method, String message) {
        operationLog.info(MaterialsMigration.class.getSimpleName() + " : " + method + " - " + message);
    }

    public static void doMaterialsMigration(IApplicationServerInternalApi v3, String sessionToken) {
        info("doMaterialsMigration","start");
        if (doMaterialsMigrationInsertNew) {
            doMaterialsMigrationInsertNew(sessionToken, v3);
        }
        doMaterialsMigrationValidation(sessionToken, v3);
        if (doMaterialsMigrationDeleteOld) {
            doMaterialsMigrationDeleteOld(sessionToken, v3);
        }
    }

    private static void doMaterialsMigrationInsertNew(String sessionToken, IApplicationServerApi v3) {
        info("doMaterialsMigrationInsertNew","start");
        createSpace(sessionToken, v3);
        createExperimentType(sessionToken, v3);
        createProject(sessionToken, v3);
        createExperiment(sessionToken, v3);
        createSampleTypes(sessionToken, v3);
        List<IEntityTypeUpdate> makeMandatory = createAndAssignPropertyTypesNoMandatoryFields(sessionToken, v3);
        createSamples(sessionToken, v3);
        assignSamplesToProperties(sessionToken, v3);
        updateAssignedPropertyTypesMandatoryFields(sessionToken, v3, makeMandatory);
    }

    private static boolean isSpace(String sessionToken, IApplicationServerApi v3, String spaceCode) {
        return !v3.getSpaces(sessionToken, List.of(new SpacePermId(spaceCode)), new SpaceFetchOptions()).isEmpty();
    }

    private static void createSpace(String sessionToken, IApplicationServerApi v3) {
        info("createSampleSpace","start");
        if (isSpace(sessionToken, v3, SPACE_CODE)) {
            info("createSampleSpace","skip");
            return; // Skip already done migration step
        }
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(SPACE_CODE);
        spaceCreation.setDescription(DESCRIPTION);
        v3.createSpaces(sessionToken, List.of(spaceCreation));
        info("createSampleSpace","done");
    }

    private static boolean isExperimentType(String sessionToken, IApplicationServerApi v3, String experimentTypeCode) {
        return !v3.getExperimentTypes(sessionToken, List.of(new EntityTypePermId(experimentTypeCode, EntityKind.EXPERIMENT)), new ExperimentTypeFetchOptions()).isEmpty();
    }

    private static void createExperimentType(String sessionToken, IApplicationServerApi v3) {
        info("createExperimentType","start");
        if (isExperimentType(sessionToken, v3, EXPERIMENT_TYPE)) {
            info("createExperimentType","skip");
            return; // Skip already done migration step
        }
        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode(EXPERIMENT_TYPE);
        experimentTypeCreation.setDescription(DESCRIPTION);
        v3.createExperimentTypes(sessionToken, List.of(experimentTypeCreation));
        info("createExperimentType","done");
    }

    private static boolean isProject(String sessionToken, IApplicationServerApi v3, String projectIdentifier) {
        return !v3.getProjects(sessionToken, List.of(new ProjectIdentifier(projectIdentifier)), new ProjectFetchOptions()).isEmpty();
    }

    private static void createProject(String sessionToken, IApplicationServerApi v3) {
        info("createProject","start");
        if (isProject(sessionToken, v3, "/" + SPACE_CODE + "/" + PROJECT_CODE)) {
            info("createProject","skip");
            return; // Skip already done migration step
        }
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(SPACE_CODE));
        projectCreation.setCode(PROJECT_CODE);
        projectCreation.setDescription(DESCRIPTION);
        v3.createProjects(sessionToken, List.of(projectCreation));
        info("createProject","done");
    }

    private static boolean isExperiment(String sessionToken, IApplicationServerApi v3, String experimentIdentifier) {
        return !v3.getExperiments(sessionToken, List.of(new ExperimentIdentifier(experimentIdentifier)), new ExperimentFetchOptions()).isEmpty();
    }

    private static void createExperiment(String sessionToken, IApplicationServerApi v3) {
        info("createExperiment","start");
        List<ExperimentCreation> collectionsToCreate = new ArrayList<>();
        SearchResult<MaterialType> materialTypes = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(), new MaterialTypeFetchOptions());
        for (MaterialType materialType:materialTypes.getObjects()) {
            if (isExperiment(sessionToken, v3, "/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + materialType.getCode() + EXPERIMENT_POSTFIX)) {
                info("createExperiment","skip");
                continue; // Skip already done migration step
            }
            ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId(EXPERIMENT_TYPE, EntityKind.EXPERIMENT));
            experimentCreation.setProjectId(new ProjectIdentifier(SPACE_CODE, PROJECT_CODE));
            experimentCreation.setCode(materialType.getCode() + EXPERIMENT_POSTFIX);
            collectionsToCreate.add(experimentCreation);
        }
        v3.createExperiments(sessionToken, collectionsToCreate);
    }

    private static boolean isSampleType(String sessionToken, IApplicationServerApi v3, String sampleTypeCode) {
        return !v3.getSampleTypes(sessionToken, List.of(new EntityTypePermId(sampleTypeCode, EntityKind.SAMPLE)), new SampleTypeFetchOptions()).isEmpty();
    }

    private static void createSampleTypes(String sessionToken, IApplicationServerApi v3) {
        info("createSampleTypes","start");
        List<SampleTypeCreation> sampleTypeCreations = new ArrayList<>();
        MaterialTypeFetchOptions materialTypeFetchOptions = new MaterialTypeFetchOptions();
        PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = materialTypeFetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType();
        propertyAssignmentFetchOptions.withPlugin();
        materialTypeFetchOptions.withValidationPlugin();
        SearchResult<MaterialType> materialTypes = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(), materialTypeFetchOptions);
        for (MaterialType materialType:materialTypes.getObjects()) {
            info("createSampleTypes", "Found Material Type: " + materialType.getCode());
            if (isSampleType(sessionToken, v3, PREFIX + materialType.getCode())) {
                info("createSampleTypes","skip");
                continue; // Skip already done migration step
            }
            SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
            sampleTypeCreation.setCode(PREFIX + materialType.getCode());
            sampleTypeCreation.setDescription(materialType.getDescription());
            if (materialType.getValidationPlugin() != null) {
                sampleTypeCreation.setValidationPluginId(materialType.getValidationPlugin().getPermId());
            }
            sampleTypeCreation.setPropertyAssignments(new ArrayList<>());

            for (PropertyAssignment propertyAssignment:materialType.getPropertyAssignments()) {
                PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
                propertyAssignmentCreation.setPropertyTypeId(propertyAssignment.getPermId().getPropertyTypeId());
                propertyAssignmentCreation.setMandatory(propertyAssignment.isMandatory());
                propertyAssignmentCreation.setOrdinal(propertyAssignment.getOrdinal());
                propertyAssignmentCreation.setSection(propertyAssignment.getSection());
                propertyAssignmentCreation.setShowInEditView(propertyAssignment.isShowInEditView());
                propertyAssignmentCreation.setShowRawValueInForms(propertyAssignment.isShowRawValueInForms());
                if (propertyAssignment.getPlugin() != null) {
                    propertyAssignmentCreation.setPluginId(propertyAssignment.getPlugin().getPermId());
                }
                sampleTypeCreation.getPropertyAssignments().add(propertyAssignmentCreation);
            }
            info("createSampleTypes", "Creating Material Sample Type: " + materialType.getCode());
            sampleTypeCreations.add(sampleTypeCreation);
        }

        v3.createSampleTypes(sessionToken, sampleTypeCreations);
    }

    private static List<IEntityTypeUpdate> createAndAssignPropertyTypesNoMandatoryFields(String sessionToken, IApplicationServerApi v3) {
        info("createAndAssignPropertyTypesNoMandatoryFields","start");
        Map<String, PropertyTypeCreation> createPropertyTypes = new HashMap<>();
        List<IEntityTypeUpdate> makeMandatoryLater = new ArrayList<>();

        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        sampleTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<SampleType> sampleTypeSearchResult = v3.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
        createAndAssignPropertyTypesNoMandatoryFieldsForHolder(sessionToken, v3, createPropertyTypes, makeMandatoryLater, sampleTypeSearchResult.getObjects());

        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
        experimentTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        experimentTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<ExperimentType> experimentTypeSearchResult = v3.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, experimentTypeFetchOptions);
        createAndAssignPropertyTypesNoMandatoryFieldsForHolder(sessionToken, v3, createPropertyTypes, makeMandatoryLater, experimentTypeSearchResult.getObjects());

        DataSetTypeSearchCriteria dataSetTypeSearchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
        dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        dataSetTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<DataSetType> dataTypeSearchResult = v3.searchDataSetTypes(sessionToken, dataSetTypeSearchCriteria, dataSetTypeFetchOptions);
        createAndAssignPropertyTypesNoMandatoryFieldsForHolder(sessionToken, v3, createPropertyTypes, makeMandatoryLater, dataTypeSearchResult.getObjects());

        return makeMandatoryLater;
    }

    private static boolean isPropertyType(String sessionToken, IApplicationServerApi v3, String propertyTypeCode) {
        return !v3.getPropertyTypes(sessionToken, List.of(new PropertyTypePermId(propertyTypeCode)), new PropertyTypeFetchOptions()).isEmpty();
    }

    private static boolean isPropertyTypeAssigned(String propertyTypeCode, IPropertyAssignmentsHolder holder) {
        for (PropertyAssignment oldPropertyAssignment: holder.getPropertyAssignments()) {
            if (oldPropertyAssignment.getPropertyType().getCode().equals(propertyTypeCode)) {
                return true;
            }
        }
        return false;
    }

    private static void createAndAssignPropertyTypesNoMandatoryFieldsForHolder(String sessionToken, IApplicationServerApi v3, Map<String, PropertyTypeCreation> createPropertyTypes, List<IEntityTypeUpdate> makeMandatoryLater, List<? extends IPropertyAssignmentsHolder> holderTypes) {
        info("createAndAssignPropertyTypesNoMandatoryFieldsForHolder","start");
        for (IPropertyAssignmentsHolder holderType: holderTypes) {
            for (PropertyAssignment oldPropertyAssignment: holderType.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    PropertyType oldPropertyType = oldPropertyAssignment.getPropertyType();
                    PropertyTypePermId newPropertyTypeId = new PropertyTypePermId(PREFIX + oldPropertyType.getCode());
                    info("createAndAssignPropertyTypesNoMandatoryFieldsForHolder",holderType.getClass().getSimpleName() + ": " + ((ICodeHolder)holderType).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
                    boolean create = !isPropertyType(sessionToken, v3, PREFIX + oldPropertyType.getCode());
                    if (create) {
                        PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
                        propertyTypeCreation.setDataType(DataType.SAMPLE);
                        propertyTypeCreation.setCode(PREFIX + oldPropertyType.getCode());
                        propertyTypeCreation.setDescription(oldPropertyType.getDescription());
                        propertyTypeCreation.setLabel(oldPropertyType.getLabel());
                        propertyTypeCreation.setManagedInternally(oldPropertyType.isManagedInternally());
                        propertyTypeCreation.setMetaData(oldPropertyType.getMetaData());
                        if (oldPropertyType.getMaterialType() != null) {
                            propertyTypeCreation.setSampleTypeId(new EntityTypePermId(PREFIX + oldPropertyType.getMaterialType().getCode(), EntityKind.SAMPLE));
                        }
                        propertyTypeCreation.setMultiValue(false);
                        createPropertyTypes.put(PREFIX + oldPropertyType.getCode(), propertyTypeCreation);

                        v3.createPropertyTypes(sessionToken, List.of(propertyTypeCreation));
                    }

                    //
                    // Assignment of new property type
                    //
                    boolean assign = !isPropertyTypeAssigned(PREFIX + oldPropertyType.getCode(), holderType);
                    PropertyAssignmentCreation newPropertyAssignment = new PropertyAssignmentCreation();
                    newPropertyAssignment.setPropertyTypeId(newPropertyTypeId);
                    newPropertyAssignment.setOrdinal(oldPropertyAssignment.getOrdinal());
                    newPropertyAssignment.setSection(oldPropertyAssignment.getSection());
                    newPropertyAssignment.setShowInEditView(oldPropertyAssignment.isShowInEditView());
                    newPropertyAssignment.setShowRawValueInForms(oldPropertyAssignment.isShowRawValueInForms());
                    if (oldPropertyAssignment.getPlugin() != null) {
                        newPropertyAssignment.setPluginId(oldPropertyAssignment.getPlugin().getPermId());
                    }
                    ListUpdateValue.ListUpdateActionAdd add = new ListUpdateValue.ListUpdateActionAdd();
                    add.setItems(List.of(newPropertyAssignment));

                    IEntityTypeUpdate update = null;
                    if (holderType instanceof SampleType) {
                        update = new SampleTypeUpdate();
                        update.setTypeId(((SampleType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(add));
                        if (assign) {
                            v3.updateSampleTypes(sessionToken, List.of((SampleTypeUpdate) update));
                        }
                    } else if (holderType instanceof ExperimentType) {
                        update = new ExperimentTypeUpdate();
                        update.setTypeId(((ExperimentType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(add));
                        if (assign) {
                            v3.updateExperimentTypes(sessionToken, List.of((ExperimentTypeUpdate) update));
                        }
                    } else if (holderType instanceof DataSetType) {
                        update = new DataSetTypeUpdate();
                        update.setTypeId(((DataSetType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(add));
                        if (assign) {
                            v3.updateDataSetTypes(sessionToken, List.of((DataSetTypeUpdate) update));
                        }
                    }
                    if (oldPropertyAssignment.isMandatory()) {
                        makeMandatoryLater.add(update);
                    }
                    if (!create && !assign) {
                        info("createAndAssignPropertyTypesNoMandatoryFieldsForHolder","skip");
                    }
                }
            }
        }
    }

    private static Map<ISampleId, Sample> getSample(String sessionToken, IApplicationServerApi v3, List<Material> materials) {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        for (Material material:materials) {
            SampleIdentifier sampleIdentifier = new SampleIdentifier("/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + material.getCode());
            sampleIdentifiers.add(sampleIdentifier);
        }
        return v3.getSamples(sessionToken, sampleIdentifiers, new SampleFetchOptions());
    }

    private static void createSamples(String sessionToken, IApplicationServerApi v3) {
        info("createSamples","start");
        int count = 0;
        int offset = 0;
        int limit = BATCH_SIZE;
        int total = -1;
        List<SampleCreation> sampleCreations = new ArrayList<>();
        while (offset < total || total == -1) {
            info("createSamples", offset + "/" + total);
            MaterialFetchOptions materialFetchOptions = new MaterialFetchOptions();
            materialFetchOptions.withProperties();
            materialFetchOptions.withTags();
            materialFetchOptions.withType();
            materialFetchOptions.withMaterialProperties();
            materialFetchOptions.from(offset).count(limit);
            SearchResult<Material> materialSearchResult = v3.searchMaterials(sessionToken, new MaterialSearchCriteria(), materialFetchOptions);

            total = materialSearchResult.getTotalCount();
            if (offset + limit < total) {
                offset += limit;
            } else {
                offset = total;
            }

            Map<ISampleId, Sample> samples = getSample(sessionToken, v3, materialSearchResult.getObjects());
            for (Material material:materialSearchResult.getObjects()) {
                count++;
                if (samples.containsKey(new SampleIdentifier("/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + material.getCode()))) {
                    info("createSamples","skip: " + count + "/" + total + " : " + "/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + material.getCode());
                    continue; // Skip already done migration step
                }
                SampleCreation sampleCreation = new SampleCreation();
                sampleCreation.setSpaceId(new SpacePermId(SPACE_CODE));
                sampleCreation.setExperimentId(new ExperimentIdentifier(SPACE_CODE, PROJECT_CODE, material.getType().getCode() + EXPERIMENT_POSTFIX));
                sampleCreation.setTypeId(new EntityTypePermId(PREFIX + material.getType().getCode(), EntityKind.SAMPLE));
                sampleCreation.setCode(material.getCode());
                for (String propertyCode:material.getProperties().keySet()) {
                    sampleCreation.setProperty(propertyCode, material.getProperty(propertyCode));
                }
                info("createSamples",count + "/" + total + " : " + "/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + material.getCode());
                sampleCreations.add(sampleCreation);
            }
            if (sampleCreations.size() == BATCH_SIZE || (!sampleCreations.isEmpty() && offset == total)) {
                long start = System.currentTimeMillis();
                info("createSamples","Insert batch of " + BATCH_SIZE);
                v3.createSamples(sessionToken, sampleCreations);
                info("createSamples","Inserted batch of " + BATCH_SIZE + " in " + (System.currentTimeMillis()-start) + " millis");
                sampleCreations.clear();
            }
        }
    }

    private static void assignSamplesToProperties(String sessionToken, IApplicationServerApi v3) {
        info("assignSamplesToProperties","start");
        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        sampleTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<SampleType> sampleTypeSearchResult = v3.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
        assignSamplesToPropertiesForHolders(sessionToken, v3, sampleTypeSearchResult.getObjects());

        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
        experimentTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        experimentTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<ExperimentType> experimentTypeSearchResult = v3.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, experimentTypeFetchOptions);
        assignSamplesToPropertiesForHolders(sessionToken, v3, experimentTypeSearchResult.getObjects());

        DataSetTypeSearchCriteria dataSetTypeSearchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
        dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        dataSetTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<DataSetType> dataTypeSearchResult = v3.searchDataSetTypes(sessionToken, dataSetTypeSearchCriteria, dataSetTypeFetchOptions);
        assignSamplesToPropertiesForHolders(sessionToken, v3, dataTypeSearchResult.getObjects());
    }

    private static boolean isSampleAssigned(IPropertiesHolder holder, String propertyCode, String sampleIdentifier) {
        return holder.getProperty(propertyCode) != null;
    }

    private static void assignSamplesToPropertiesForHolders(String sessionToken, IApplicationServerApi v3, List<? extends IPropertyAssignmentsHolder> holderTypes) {
        info("assignSamplesToPropertiesForHolders","start");
        List updates = new ArrayList<>();
        for (IPropertyAssignmentsHolder holderType: holderTypes) {
            for (PropertyAssignment oldPropertyAssignment : holderType.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    int count = 0;
                    int offset = 0;
                    int limit = BATCH_SIZE;
                    int total = -1;
                    while (offset < total || total == -1) {
                        info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
                        SearchResult<? extends IPropertiesHolder> result = null;
                        List<? extends IPropertiesHolder> holders = null;
                        if (holderType instanceof SampleType) {
                            SampleSearchCriteria criteria = new SampleSearchCriteria();
                            criteria.withType().withCode().equals(((SampleType) holderType).getCode());
                            SampleFetchOptions options = new SampleFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchSamples(sessionToken, criteria, options);
                        } else if (holderType instanceof ExperimentType) {
                            ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
                            criteria.withType().withCode().equals(((ExperimentType) holderType).getCode());
                            ExperimentFetchOptions options = new ExperimentFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchExperiments(sessionToken, criteria, options);
                        } else if (holderType instanceof DataSetType) {
                            DataSetSearchCriteria criteria = new DataSetSearchCriteria();
                            criteria.withType().withCode().equals(((DataSetType) holderType).getCode());
                            DataSetFetchOptions options = new DataSetFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchDataSets(sessionToken, criteria, options);
                        }
                        total = result.getTotalCount();
                        holders = result.getObjects();
                        if (offset + limit < total) {
                            offset += limit;
                        } else {
                            offset = total;
                        }

                        info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " found: " + offset + " / " + total);

                        for (IPropertiesHolder holder : holders) {
                            count++;
                            String oldPropertyMaterialCode = null;
                            if (holder instanceof Sample && ((Sample) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((Sample) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            } else if (holder instanceof Experiment && ((Experiment) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((Experiment) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            } else if (holder instanceof DataSet && ((DataSet) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((DataSet) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            }
                            String newPropertySampleCode = PREFIX + oldPropertyAssignment.getPropertyType().getCode();
                            String sampleIdentifier = null;
                            if (oldPropertyMaterialCode != null) {
                                sampleIdentifier = "/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + oldPropertyMaterialCode;
                            }
                            boolean notUpdated = sampleIdentifier != null && !isSampleAssigned(holder, newPropertySampleCode, sampleIdentifier);
                            if (notUpdated) {
                                if (holder instanceof Sample) {
                                    SampleUpdate update = new SampleUpdate();
                                    update.setSampleId(((Sample) holder).getPermId());
                                    update.setProperty(newPropertySampleCode, sampleIdentifier);
                                    updates.add(update);
                                } else if (holder instanceof Experiment) {
                                    ExperimentUpdate update = new ExperimentUpdate();
                                    update.setExperimentId(((Experiment) holder).getPermId());
                                    update.setProperty(newPropertySampleCode, sampleIdentifier);
                                    updates.add(update);
                                } else if (holder instanceof DataSet) {
                                    DataSetUpdate update = new DataSetUpdate();
                                    update.setDataSetId(((DataSet) holder).getPermId());
                                    update.setProperty(newPropertySampleCode, sampleIdentifier);
                                    updates.add(update);
                                }
                            } else {
                                info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " skip: " + count + " / " + total);
                            }
                        }

                        if (updates.size() == BATCH_SIZE || (!updates.isEmpty() && offset == total)) {
                            if (updates.get(0) instanceof SampleUpdate) {
                                v3.updateSamples(sessionToken, updates);
                                info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " updated: " + updates.size());
                            } else if (updates.get(0) instanceof ExperimentUpdate) {
                                v3.updateExperiments(sessionToken, updates);
                                info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " updated: " + updates.size());
                            } else if (updates.get(0) instanceof DataSetUpdate) {
                                v3.updateDataSets(sessionToken, updates);
                                info("assignSamplesToPropertiesForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " updated: " + updates.size());
                            }
                            updates.clear();
                        }
                    }
                }
            }
        }
    }

    private static void updateAssignedPropertyTypesMandatoryFields(String sessionToken, IApplicationServerApi v3, List<IEntityTypeUpdate> makeMandatory) {
        info("updateAssignedPropertyTypesMandatoryFields","start");
        for (IEntityTypeUpdate entityTypeUpdate:makeMandatory) {

            // Complete Entity Type
            EntityTypePermId entityTypePermId = (EntityTypePermId) entityTypeUpdate.getTypeId();
            IEntityType iEntityType = null;
            switch (entityTypePermId.getEntityKind()) {
                case EXPERIMENT:
                    ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
                    experimentTypeFetchOptions.withPropertyAssignments().withPropertyType();
                    experimentTypeFetchOptions.withPropertyAssignments().withPlugin();
                    iEntityType = v3.getExperimentTypes(sessionToken, List.of(entityTypePermId), experimentTypeFetchOptions).get(entityTypePermId);
                    break;
                case SAMPLE:
                    SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
                    sampleTypeFetchOptions.withPropertyAssignments().withPropertyType();
                    sampleTypeFetchOptions.withPropertyAssignments().withPlugin();
                    iEntityType = v3.getSampleTypes(sessionToken, List.of(entityTypePermId), sampleTypeFetchOptions).get(entityTypePermId);
                    break;
                case DATA_SET:
                    DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
                    dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType();
                    dataSetTypeFetchOptions.withPropertyAssignments().withPlugin();
                    iEntityType = v3.getDataSetTypes(sessionToken, List.of(entityTypePermId), dataSetTypeFetchOptions).get(entityTypePermId);
                    break;
            }

            // Entity Type properties to make mandatory
            Collection<PropertyAssignmentCreation> propertyAssignmentCreations = entityTypeUpdate.getPropertyAssignments().getAdded();
            List<PropertyTypePermId> mandatoryPropertyTypePermIds = new ArrayList<>();
            for (PropertyAssignmentCreation propertyAssignmentCreation:propertyAssignmentCreations) {
                mandatoryPropertyTypePermIds.add((PropertyTypePermId) propertyAssignmentCreation.getPropertyTypeId());
            }

            info("updateAssignedPropertyTypesMandatoryFields", entityTypeUpdate.getClass().getSimpleName() + "-" + entityTypeUpdate.getTypeId());
            IEntityTypeUpdate typeUpdate = null;
            if(entityTypeUpdate instanceof SampleTypeUpdate) {
                typeUpdate = new SampleTypeUpdate();
            } else if(entityTypeUpdate instanceof ExperimentTypeUpdate) {
                typeUpdate = new ExperimentTypeUpdate();
            } else if(entityTypeUpdate instanceof DataSetTypeUpdate) {
                typeUpdate = new DataSetTypeUpdate();
            }
            typeUpdate.setTypeId(entityTypeUpdate.getTypeId());
            typeUpdate.setDescription(entityTypeUpdate.getDescription().getValue());
            if (entityTypeUpdate.getValidationPluginId() != null) {
                typeUpdate.setValidationPluginId(entityTypeUpdate.getValidationPluginId().getValue());
            }
            List<PropertyAssignmentCreation> newPropertyAssignmentCreations = new ArrayList<>();
            for (PropertyAssignment propertyAssignment:iEntityType.getPropertyAssignments()) {
                PropertyAssignmentCreation creation = new PropertyAssignmentCreation();
                creation.setPropertyTypeId(propertyAssignment.getPropertyType().getPermId());
                if (mandatoryPropertyTypePermIds.contains(propertyAssignment.getPropertyType().getPermId()) || propertyAssignment.isMandatory()) {
                    creation.setMandatory(true);
                }
                creation.setOrdinal(propertyAssignment.getOrdinal());
                if (propertyAssignment.getPlugin() != null) {
                    creation.setPluginId(propertyAssignment.getPlugin().getPermId());
                }
                creation.setSection(propertyAssignment.getSection());
                creation.setShowInEditView(propertyAssignment.isShowInEditView());
                newPropertyAssignmentCreations.add(creation);
            }

            ListUpdateValue.ListUpdateActionSet<Object> set = new ListUpdateValue.ListUpdateActionSet<>();
            set.setItems(newPropertyAssignmentCreations);

            typeUpdate.setPropertyAssignmentActions(List.of(set));

            if(typeUpdate instanceof SampleTypeUpdate) {
                v3.updateSampleTypes(sessionToken, List.of((SampleTypeUpdate)typeUpdate));
            } else if(typeUpdate instanceof ExperimentTypeUpdate) {
                v3.updateExperimentTypes(sessionToken, List.of((ExperimentTypeUpdate)typeUpdate));
            } else if(typeUpdate instanceof DataSetTypeUpdate) {
                ((DataSetTypeUpdate)typeUpdate).setMainDataSetPath(((DataSetType)iEntityType).getMainDataSetPath());
                ((DataSetTypeUpdate)typeUpdate).setMainDataSetPattern(((DataSetType)iEntityType).getMainDataSetPattern());
                ((DataSetTypeUpdate)typeUpdate).setDisallowDeletion(((DataSetType)iEntityType).isDisallowDeletion());
                v3.updateDataSetTypes(sessionToken, List.of((DataSetTypeUpdate)typeUpdate));
            }

        }
    }

    private static void doMaterialsMigrationValidation(String sessionToken, IApplicationServerApi v3) {
        info("doMaterialsMigrationValidation","start");
        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        sampleTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<SampleType> sampleTypeSearchResult = v3.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
        doMaterialsMigrationValidationForHolders(sessionToken, v3, sampleTypeSearchResult.getObjects());

        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
        experimentTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        experimentTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<ExperimentType> experimentTypeSearchResult = v3.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, experimentTypeFetchOptions);
        doMaterialsMigrationValidationForHolders(sessionToken, v3, experimentTypeSearchResult.getObjects());

        DataSetTypeSearchCriteria dataSetTypeSearchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
        dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        dataSetTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<DataSetType> dataTypeSearchResult = v3.searchDataSetTypes(sessionToken, dataSetTypeSearchCriteria, dataSetTypeFetchOptions);
        doMaterialsMigrationValidationForHolders(sessionToken, v3, dataTypeSearchResult.getObjects());
    }

    private static void doMaterialsMigrationValidationForHolders(String sessionToken, IApplicationServerApi v3, List<? extends IPropertyAssignmentsHolder> holderTypes) {
        info("doMaterialsMigrationValidationForHolders","start");
        for (IPropertyAssignmentsHolder holderType: holderTypes) {
            for (PropertyAssignment oldPropertyAssignment : holderType.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    int offset = 0;
                    int limit = BATCH_SIZE;
                    int total = -1;
                    while (offset < total || total == -1) {
                        info("doMaterialsMigrationValidationForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
                        SearchResult<? extends IPropertiesHolder> result = null;
                        List<? extends IPropertiesHolder> holders = null;
                        if (holderType instanceof SampleType) {
                            SampleSearchCriteria criteria = new SampleSearchCriteria();
                            criteria.withType().withCode().equals(((SampleType) holderType).getCode());
                            SampleFetchOptions options = new SampleFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchSamples(sessionToken, criteria, options);
                        } else if (holderType instanceof ExperimentType) {
                            ExperimentSearchCriteria criteria = new ExperimentSearchCriteria();
                            criteria.withType().withCode().equals(((ExperimentType) holderType).getCode());
                            ExperimentFetchOptions options = new ExperimentFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchExperiments(sessionToken, criteria, options);
                        } else if (holderType instanceof DataSetType) {
                            DataSetSearchCriteria criteria = new DataSetSearchCriteria();
                            criteria.withType().withCode().equals(((DataSetType) holderType).getCode());
                            DataSetFetchOptions options = new DataSetFetchOptions();
                            options.withMaterialProperties();
                            options.withProperties();
                            options.from(offset).count(limit);
                            result = v3.searchDataSets(sessionToken, criteria, options);
                        }
                        total = result.getTotalCount();
                        holders = result.getObjects();
                        if (offset + limit < total) {
                            offset += limit;
                        } else {
                            offset = total;
                        }

                        info("doMaterialsMigrationValidationForHolders", holderType.getClass().getSimpleName() + ": " + ((ICodeHolder) holderType).getCode() + " found: " + offset + " / " + total);

                        for (IPropertiesHolder holder : holders) {
                            String oldPropertyMaterialCode = null;
                            if (holder instanceof Sample && ((Sample) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((Sample) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            } else if (holder instanceof Experiment && ((Experiment) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((Experiment) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            } else if (holder instanceof DataSet && ((DataSet) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()) != null) {
                                oldPropertyMaterialCode = ((DataSet) holder).getMaterialProperty(oldPropertyAssignment.getPropertyType().getCode()).getCode();
                            }
                            String newPropertySampleCode = PREFIX + oldPropertyAssignment.getPropertyType().getCode();
                            String sampleIdentifier = null;
                            if (oldPropertyMaterialCode != null) {
                                sampleIdentifier = "/" + SPACE_CODE + "/" + PROJECT_CODE + "/" + oldPropertyMaterialCode;
                            }
                            boolean notUpdated = sampleIdentifier != null && !isSampleAssigned(holder, newPropertySampleCode, sampleIdentifier);
                            if (notUpdated) {
                                throw new RuntimeException("Sample not assigned found: " + sampleIdentifier);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void doMaterialsMigrationDeleteOld(String sessionToken, IApplicationServerApi v3) {
        info("doMaterialsMigrationDeleteOld","");
        unassignMaterialProperties(sessionToken, v3);
        removeMaterialProperties(sessionToken, v3);
        removeMaterials(sessionToken, v3);
        removeMaterialTypes(sessionToken, v3);
    }

    private static void unassignMaterialProperties(String sessionToken, IApplicationServerApi v3) {
        info("unassignMaterialProperties","start");
        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();
        sampleTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        sampleTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<SampleType> sampleTypeSearchResult = v3.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, sampleTypeFetchOptions);
        unassignMaterialPropertiesForHolder(sessionToken, v3, sampleTypeSearchResult.getObjects());

        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        ExperimentTypeFetchOptions experimentTypeFetchOptions = new ExperimentTypeFetchOptions();
        experimentTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        experimentTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<ExperimentType> experimentTypeSearchResult = v3.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, experimentTypeFetchOptions);
        unassignMaterialPropertiesForHolder(sessionToken, v3, experimentTypeSearchResult.getObjects());

        DataSetTypeSearchCriteria dataSetTypeSearchCriteria = new DataSetTypeSearchCriteria();
        DataSetTypeFetchOptions dataSetTypeFetchOptions = new DataSetTypeFetchOptions();
        dataSetTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        dataSetTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<DataSetType> dataTypeSearchResult = v3.searchDataSetTypes(sessionToken, dataSetTypeSearchCriteria, dataSetTypeFetchOptions);
        unassignMaterialPropertiesForHolder(sessionToken, v3, dataTypeSearchResult.getObjects());

        MaterialTypeSearchCriteria materialTypeSearchCriteria = new MaterialTypeSearchCriteria();
        MaterialTypeFetchOptions materialTypeFetchOptions = new MaterialTypeFetchOptions();
        materialTypeFetchOptions.withPropertyAssignments().withPropertyType().withMaterialType();
        materialTypeFetchOptions.withPropertyAssignments().withPlugin();
        SearchResult<MaterialType> materialTypeSearchResult = v3.searchMaterialTypes(sessionToken, materialTypeSearchCriteria, materialTypeFetchOptions);
        unassignMaterialPropertiesForHolder(sessionToken, v3, materialTypeSearchResult.getObjects());
    }

    private static void unassignMaterialPropertiesForHolder(String sessionToken, IApplicationServerApi v3, List<? extends IPropertyAssignmentsHolder> holderTypes) {
        info("unassignMaterialPropertiesForHolder","start");
        for (IPropertyAssignmentsHolder holderType: holderTypes) {
            for (PropertyAssignment oldPropertyAssignment : holderType.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    info("unassignMaterialPropertiesForHolder","removing" + oldPropertyAssignment.getPropertyType().getCode());
                    ListUpdateValue.ListUpdateActionRemove remove = new ListUpdateValue.ListUpdateActionRemove<>();
                    remove.setItems(List.of(oldPropertyAssignment.getPermId()));
                    IEntityTypeUpdate update = null;
                    if (holderType instanceof SampleType) {
                        update = new SampleTypeUpdate();
                        update.setTypeId(((SampleType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(remove));
                        update.getPropertyAssignments().setForceRemovingAssignments(true);
                        v3.updateSampleTypes(sessionToken, List.of((SampleTypeUpdate) update));
                    } else if (holderType instanceof ExperimentType) {
                        update = new ExperimentTypeUpdate();
                        update.setTypeId(((ExperimentType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(remove));
                        update.getPropertyAssignments().setForceRemovingAssignments(true);
                        v3.updateExperimentTypes(sessionToken, List.of((ExperimentTypeUpdate) update));
                    } else if (holderType instanceof DataSetType) {
                        update = new DataSetTypeUpdate();
                        update.setTypeId(((DataSetType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(remove));
                        update.getPropertyAssignments().setForceRemovingAssignments(true);
                        v3.updateDataSetTypes(sessionToken, List.of((DataSetTypeUpdate) update));
                    } else if (holderType instanceof MaterialType) {
                        update = new MaterialTypeUpdate();
                        update.setTypeId(((MaterialType) holderType).getPermId());
                        update.setPropertyAssignmentActions(List.of(remove));
                        update.getPropertyAssignments().setForceRemovingAssignments(true);
                        v3.updateMaterialTypes(sessionToken, List.of((MaterialTypeUpdate) update));
                    }
                }
            }
        }
    }

    private static void removeMaterialProperties(String sessionToken, IApplicationServerApi v3) {
        info("removeMaterialProperties","");
        SearchResult<PropertyType> propertyTypeSearchResult = v3.searchPropertyTypes(sessionToken, new PropertyTypeSearchCriteria(), new PropertyTypeFetchOptions());
        List<PropertyTypePermId> materialPropertyTypes = new ArrayList<>();
        for (PropertyType propertyType:propertyTypeSearchResult.getObjects()) {
            if (propertyType.getDataType() == DataType.MATERIAL) {
                materialPropertyTypes.add(propertyType.getPermId());
            }
        }
        info("removeMaterialProperties", "Found material properties " + materialPropertyTypes);
        PropertyTypeDeletionOptions propertyTypeDeletionOptions = new PropertyTypeDeletionOptions();
        propertyTypeDeletionOptions.setReason(REASON);
        v3.deletePropertyTypes(sessionToken, materialPropertyTypes, propertyTypeDeletionOptions);
    }

    private static void removeMaterials(String sessionToken, IApplicationServerApi v3) {
        info("removeMaterials","start");
        SearchResult<MaterialType> materialTypeSearchResult = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(),  new MaterialTypeFetchOptions());
        for (MaterialType materialType:materialTypeSearchResult.getObjects()) {
            int count = 0;
            int offset = 0;
            int limit = BATCH_SIZE/10;
            int total = -1;
            while (offset < total || total == -1) {
                MaterialSearchCriteria criteria = new MaterialSearchCriteria();
                criteria.withType().withCode().equals(materialType.getCode());
                MaterialFetchOptions options = new MaterialFetchOptions();
                options.from(offset).count(limit);
                SearchResult<Material> materialSearchResult = v3.searchMaterials(sessionToken, criteria, options);
                total = materialSearchResult.getTotalCount();
                List<Material> materials = materialSearchResult.getObjects();
                info("removeMaterials", materialType.getCode() + " " + total);
                if (offset + limit < total) {
                    offset += limit;
                } else {
                    offset = total;
                }
                List<MaterialPermId> deletePermIds = new ArrayList<>();
                for (Material material:materials) {
                    count++;
                    deletePermIds.add(material.getPermId());

                    if (deletePermIds.size() == limit || (!deletePermIds.isEmpty() && offset == total)) {
                        MaterialDeletionOptions materialDeletionOptions = new MaterialDeletionOptions();
                        materialDeletionOptions.setReason(REASON);
                        long start = System.currentTimeMillis();
                        info("removeMaterials", " removing " + options.getFrom() + " / " + offset + " of " + total);
                        v3.deleteMaterials(sessionToken, deletePermIds, materialDeletionOptions);
                        deletePermIds.clear();
                        info("removeMaterials", " removed " + options.getFrom() + " / " + offset + " - using " + (System.currentTimeMillis() - start) + " millis");
                    }
                }
            }
        }
     }

    private static void removeMaterialTypes(String sessionToken, IApplicationServerApi v3) {
        info("removeMaterialTypes","start");
        SearchResult<MaterialType> materialTypeSearchResult = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(),  new MaterialTypeFetchOptions());
        List<EntityTypePermId> materialPermIds = new ArrayList<>();
        for (MaterialType materialType:materialTypeSearchResult.getObjects()) {
            materialPermIds.add(materialType.getPermId());
        }
        info("removeMaterialTypes","removing " + materialPermIds);
        MaterialTypeDeletionOptions deletionOptions = new MaterialTypeDeletionOptions();
        deletionOptions.setReason(REASON);
        v3.deleteMaterialTypes(sessionToken, materialPermIds, deletionOptions);
    }

}
