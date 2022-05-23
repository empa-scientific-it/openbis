package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update.IEntityTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialsMigration {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MaterialsMigration.class);
    private static final String PREFIX = "MATERIAL.";
    private static final String SPACE_CODE = PREFIX + "GLOBAL";
    private static final String PROJECT_CODE = SPACE_CODE;
    private static final String EXPERIMENT_TYPE = "COLLECTION";
    private static final String EXPERIMENT_POSTFIX = "_COLLECTION";
    private static final String DESCRIPTION = "Used to hold objects instances from all migrated materials.";

    private static void info(String method, String message) {
        operationLog.info(MaterialsMigration.class.getSimpleName() + " : " + method + " - " + message);
        System.out.println(MaterialsMigration.class.getSimpleName() + " : " + message + " - " + message);
    }

    public static void main(String[] args) throws Exception {
        info("main (doMaterialsMigration development substitute)","");
        final String URL = "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
        final int TIMEOUT = Integer.MAX_VALUE;
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        String sessionToken = v3.login("admin", "a");
        doMaterialsMigrationInsertNew(sessionToken, v3);
        doMaterialsMigrationDeleteOld(sessionToken, v3);
        v3.logout(sessionToken);
    }

    public static void doMaterialsMigration() throws Exception {
        info("doMaterialsMigration","start");
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        doMaterialsMigrationInsertNew(sessionToken, v3);
        doMaterialsMigrationDeleteOld(sessionToken, v3);
        v3.logout(sessionToken);
    }

    private static void doMaterialsMigrationInsertNew(String sessionToken, IApplicationServerApi v3) throws Exception {
        info("doMaterialsMigrationInsertNew","start");
        createSampleSpace(sessionToken, v3);
        createExperimentType(sessionToken, v3);
        createExperiment(sessionToken, v3);
        createSampleTypes(sessionToken, v3);
        List<IEntityTypeUpdate> makeMandatory = createAndAssignPropertyTypesNoMandatoryFields(sessionToken, v3);
        createSamples(sessionToken, v3);
        assignSamplesToProperties(sessionToken, v3);
        updateAssignedPropertyTypesMandatoryFields(sessionToken, v3, makeMandatory);
    }

    private static void createSampleSpace(String sessionToken, IApplicationServerApi v3) {
        info("createSampleSpace","start");
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(SPACE_CODE);
        spaceCreation.setDescription(DESCRIPTION);
        v3.createSpaces(sessionToken, List.of(spaceCreation));
    }

    private static void createExperimentType(String sessionToken, IApplicationServerApi v3) {
        info("createExperimentType","start");
        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode(EXPERIMENT_TYPE);
        experimentTypeCreation.setDescription(DESCRIPTION);
        v3.createExperimentTypes(sessionToken, List.of(experimentTypeCreation));
    }

    private static void createExperiment(String sessionToken, IApplicationServerApi v3) {
        info("createExperiment","start");
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(new SpacePermId(SPACE_CODE));
        projectCreation.setCode(SPACE_CODE);
        projectCreation.setDescription(DESCRIPTION);
        v3.createProjects(sessionToken, List.of(projectCreation));

        List<ExperimentCreation> collectionsToCreate = new ArrayList<>();
        SearchResult<MaterialType> materialTypes = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(), new MaterialTypeFetchOptions());
        for (MaterialType materialType:materialTypes.getObjects()) {
            ExperimentCreation experimentCreation = new ExperimentCreation();
            experimentCreation.setTypeId(new EntityTypePermId(EXPERIMENT_TYPE, EntityKind.EXPERIMENT));
            experimentCreation.setProjectId(new ProjectIdentifier(SPACE_CODE, PROJECT_CODE));
            experimentCreation.setCode(materialType.getCode() + EXPERIMENT_POSTFIX);
            collectionsToCreate.add(experimentCreation);
        }
        v3.createExperiments(sessionToken, collectionsToCreate);
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

    private static void createAndAssignPropertyTypesNoMandatoryFieldsForHolder(String sessionToken, IApplicationServerApi v3, Map<String, PropertyTypeCreation> createPropertyTypes, List<IEntityTypeUpdate> makeMandatoryLater, List<? extends IPropertyAssignmentsHolder> holders) {
        info("createAndAssignPropertyTypesNoMandatoryFieldsForHolder","start");
        for (IPropertyAssignmentsHolder holder: holders) {
            for (PropertyAssignment oldPropertyAssignment: holder.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    PropertyType oldPropertyType = oldPropertyAssignment.getPropertyType();
                    PropertyTypePermId newPropertyTypeId = new PropertyTypePermId(PREFIX + oldPropertyType.getCode());
                    info("createAndAssignPropertyTypesNoMandatoryFieldsForHolder",holder.getClass().getSimpleName() + ": " + ((ICodeHolder)holder).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
                    boolean create = !createPropertyTypes.containsKey(PREFIX + oldPropertyType.getCode());
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
                        createPropertyTypes.put(PREFIX + oldPropertyType.getCode(), propertyTypeCreation);

                        v3.createPropertyTypes(sessionToken, List.of(propertyTypeCreation));
                    }

                    //
                    // Assignment of new property type
                    //
                    boolean assign = true;
                    if (assign) {
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
                        if (holder instanceof SampleType) {
                            update = new SampleTypeUpdate();
                            update.setTypeId(((SampleType) holder).getPermId());
                            update.setPropertyAssignmentActions(List.of(add));
                            v3.updateSampleTypes(sessionToken, List.of((SampleTypeUpdate) update));
                        } else if (holder instanceof ExperimentType) {
                            update = new ExperimentTypeUpdate();
                            update.setTypeId(((ExperimentType) holder).getPermId());
                            update.setPropertyAssignmentActions(List.of(add));
                            v3.updateExperimentTypes(sessionToken, List.of((ExperimentTypeUpdate) update));
                        } else if (holder instanceof DataSetType) {
                            update = new DataSetTypeUpdate();
                            update.setTypeId(((DataSetType) holder).getPermId());
                            update.setPropertyAssignmentActions(List.of(add));
                            v3.updateDataSetTypes(sessionToken, List.of((DataSetTypeUpdate) update));
                        }
                        if (oldPropertyAssignment.isMandatory()) {
                            makeMandatoryLater.add(update);
                        }
                    }
                }
            }
        }
    }

    private static void createSamples(String sessionToken, IApplicationServerApi v3) {
        info("createSamples","start");
        List<SampleCreation> sampleCreations = new ArrayList<>();
        MaterialFetchOptions materialFetchOptions = new MaterialFetchOptions();
        materialFetchOptions.withProperties();
        materialFetchOptions.withTags();
        materialFetchOptions.withType();
        materialFetchOptions.withMaterialProperties();
        SearchResult<Material> materialSearchResult = v3.searchMaterials(sessionToken, new MaterialSearchCriteria(), materialFetchOptions);
        Map<String, Map<String,String>> assignLater = new HashMap<>(); // Objects can't
        for (Material material:materialSearchResult.getObjects()) {
            SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setSpaceId(new SpacePermId(SPACE_CODE));
            sampleCreation.setExperimentId(new ExperimentIdentifier(SPACE_CODE, PROJECT_CODE, material.getType().getCode() + EXPERIMENT_POSTFIX));
            sampleCreation.setTypeId(new EntityTypePermId(PREFIX + material.getType().getCode(), EntityKind.SAMPLE));
            sampleCreation.setCode(material.getCode());
            for (String propertyCode:material.getProperties().keySet()) {
                if (material.getMaterialProperties().keySet().contains(propertyCode)) { // Convert material property to sample property
                    if (!assignLater.containsKey(material.getCode())) {
                        assignLater.put(material.getCode(), new HashMap<>());
                    }
                    assignLater.get(material.getCode()).put(propertyCode, material.getProperty(propertyCode));
                } else {
                    sampleCreation.setProperty(propertyCode, material.getProperty(propertyCode));
                }
            }
            sampleCreations.add(sampleCreation);
        }
        v3.createSamples(sessionToken, sampleCreations);

        // Assign properties that required an existing foreign key
    }

    private static void assignSamplesToProperties(String sessionToken, IApplicationServerApi v3) {
    }

    private static void updateAssignedPropertyTypesMandatoryFields(String sessionToken, IApplicationServerApi v3, List<IEntityTypeUpdate> makeMandatory) {
    }

    private static void doMaterialsMigrationDeleteOld(String sessionToken, IApplicationServerApi v3) throws Exception {
        operationLog.info("doMaterialsMigrationDeleteOld");
        unassignMaterials(sessionToken, v3);
        removeMaterials(sessionToken, v3);
        removeMaterialProperties(sessionToken, v3);
        removeMaterialTypes(sessionToken, v3);
    }

    private static void unassignMaterials(String sessionToken, IApplicationServerApi v3) {
    }

    private static void removeMaterials(String sessionToken, IApplicationServerApi v3) {
    }

    private static void removeMaterialTypes(String sessionToken, IApplicationServerApi v3) {
    }

    private static void removeMaterialProperties(String sessionToken, IApplicationServerApi v3) {
    }

}
