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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
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

    public static void main(String[] args) throws Exception {
        operationLog.info("main");
        final String URL = "http://localhost:8888/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
        final int TIMEOUT = 10000;
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        String sessionToken = v3.login("admin", "a");
        doMaterialsMigrationInsertNew(sessionToken, v3);
        doMaterialsMigrationDeleteOld(sessionToken, v3);
        v3.logout(sessionToken);
    }

    public static void doMaterialsMigration() throws Exception {
        operationLog.info("doMaterialsMigration");
        IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = v3.loginAsSystem();
        doMaterialsMigrationInsertNew(sessionToken, v3);
        doMaterialsMigrationDeleteOld(sessionToken, v3);
        v3.logout(sessionToken);
    }

    public static void doMaterialsMigrationInsertNew(String sessionToken, IApplicationServerApi v3) throws Exception {
        operationLog.info("doMaterialsMigrationInsertNew");
        createSampleSpace(sessionToken, v3);
        createSampleTypes(sessionToken, v3);
        List<IEntityTypeUpdate> makeMandatory = createAndAssignPropertyTypesNoMandatoryFields(sessionToken, v3);
        createSamples(sessionToken, v3);
        assignSamplesToProperties(sessionToken, v3);
        updateAssignedPropertyTypesMandatoryFields(sessionToken, v3, makeMandatory);
    }

    private static void createSampleSpace(String sessionToken, IApplicationServerApi v3) {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(PREFIX + SPACE_CODE);
        spaceCreation.setDescription("Space used to hold objects instances from all migrated materials.");
        v3.createSpaces(sessionToken, List.of(spaceCreation));
    }

    private static void createSampleTypes(String sessionToken, IApplicationServerApi v3) {
        operationLog.info("createSampleTypes");
        List<SampleTypeCreation> sampleTypeCreations = new ArrayList<>();
        MaterialTypeFetchOptions materialTypeFetchOptions = new MaterialTypeFetchOptions();
        PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = materialTypeFetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType();
        propertyAssignmentFetchOptions.withPlugin();
        materialTypeFetchOptions.withValidationPlugin();
        SearchResult<MaterialType> materialTypes = v3.searchMaterialTypes(sessionToken, new MaterialTypeSearchCriteria(), materialTypeFetchOptions);
        for (MaterialType materialType:materialTypes.getObjects()) {
            operationLog.info("Found Material Type: " + materialType.getCode());
            System.out.println("Found Material Type: " + materialType.getCode());

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
            operationLog.info("Creating Material Type: " + sampleTypeCreation.getCode());
            System.out.println("Creating Material Type: " + sampleTypeCreation.getCode());
            sampleTypeCreations.add(sampleTypeCreation);
        }

        v3.createSampleTypes(sessionToken, sampleTypeCreations);
    }

    private static List<IEntityTypeUpdate> createAndAssignPropertyTypesNoMandatoryFields(String sessionToken, IApplicationServerApi v3) {
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
        for (IPropertyAssignmentsHolder holder: holders) {
            for (PropertyAssignment oldPropertyAssignment: holder.getPropertyAssignments()) {
                if (oldPropertyAssignment.getPropertyType().getDataType() == DataType.MATERIAL) {
                    PropertyType oldPropertyType = oldPropertyAssignment.getPropertyType();
                    PropertyTypePermId newPropertyTypeId = new PropertyTypePermId(PREFIX + oldPropertyType.getCode());
                    System.out.println(holder.getClass().getSimpleName() + ": " + ((ICodeHolder)holder).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
                    operationLog.info(holder.getClass().getSimpleName() + ": " + ((ICodeHolder)holder).getCode() + " found Material Type: " + oldPropertyAssignment.getPropertyType().getCode());
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
