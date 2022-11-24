package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.NativeQuery;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ELNCollectionTypeMigration {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ELNCollectionTypeMigration.class);

    private static final String COLLECTION = "COLLECTION";

    private static final String[] experimentsOfTypeCollection = new String[]{
            // ELN-LIMS TYPES
        "STORAGES_COLLECTION",
        "GENERAL_PROTOCOLS",
        "PRODUCT_COLLECTION",
        "SUPPLIER_COLLECTION",
        "REQUEST_COLLECTION",
        "ORDER_COLLECTION",
        "TEMPLATES_COLLECTION",
        "PUBLICATIONS_COLLECTION",
            // ELN-LIMS LIFE SCIENCES TYPES
        "ANTIBODY_COLLECTION",
        "CHEMICAL_COLLECTION",
        "ENZYME_COLLECTION",
        "MEDIA_COLLECTION",
        "SOLUTION_BUFFER_COLLECTION",
        "BACTERIA_COLLECTION",
        "CELL_LINE_COLLECTION",
        "FLY_COLLECTION",
        "YEAST_COLLECTION",
        "PLASMID_COLLECTION",
        "OLIGO_COLLECTION",
        "RNA_COLLECTION",
        "PCR_PROTOCOLS",
        "WESTERN_BLOTTING_PROTOCOLS",
        "PLANT_COLLECTION"
    };

    private static final Map<String, String> PROPERTY_UPDATES_MAP = Stream.of(
            new AbstractMap.SimpleEntry<>("COMPANY", "PRODUCT.COMPANY"),
            new AbstractMap.SimpleEntry<>("SIZE_OF_ITEM", "PRODUCT.SIZE_OF_ITEM"),
            new AbstractMap.SimpleEntry<>("HAZARD_STATEMENT", "PRODUCT.HAZARD_STATEMENT"),
            new AbstractMap.SimpleEntry<>("CATEGORY", "PRODUCT.CATEGORY"),
            new AbstractMap.SimpleEntry<>("DESCRIPTION", "PRODUCT.DESCRIPTION"),
            new AbstractMap.SimpleEntry<>("PRODUCT_SECONDARY_NAMES", "PRODUCT.PRODUCT_SECONDARY_NAMES"),

            new AbstractMap.SimpleEntry<>("ORDER_NUMBER", "REQUEST.ORDER_NUMBER"),
            new AbstractMap.SimpleEntry<>("DEPARTMENT", "REQUEST.DEPARTMENT"),
            new AbstractMap.SimpleEntry<>("BUYER", "REQUEST.BUYER"),
            new AbstractMap.SimpleEntry<>("PROJECT", "REQUEST.PROJECT"),

            new AbstractMap.SimpleEntry<>("URL", "SUPPLIER.URL"),
            new AbstractMap.SimpleEntry<>("PREFERRED_ORDER_METHOD", "SUPPLIER.PREFERRED_ORDER_METHOD"),
            new AbstractMap.SimpleEntry<>("COMPANY_CONTACT_EMAIL", "SUPPLIER.COMPANY_CONTACT_EMAIL"),
            new AbstractMap.SimpleEntry<>("COMPANY_CONTACT_NAME", "SUPPLIER.COMPANY_CONTACT_NAME"),

            new AbstractMap.SimpleEntry<>("PRICE_PAID", "ORDER.PRICE_PAID"),

            new AbstractMap.SimpleEntry<>("PROTOCOL_TYPE", "GENERAL_PROTOCOL.PROTOCOL_TYPE"),
            new AbstractMap.SimpleEntry<>("PROTOCOL_EVALUATION", "GENERAL_PROTOCOL.PROTOCOL_EVALUATION"),
            new AbstractMap.SimpleEntry<>("TIME_REQUIREMENT", "GENERAL_PROTOCOL.TIME_REQUIREMENT"),
            new AbstractMap.SimpleEntry<>("MATERIALS", "GENERAL_PROTOCOL.MATERIALS"),

            new AbstractMap.SimpleEntry<>("GRANT", "DEFAULT_EXPERIMENT.GRANT"),

            new AbstractMap.SimpleEntry<>("EXPERIMENTAL_RESULTS", "EXPERIMENTAL_STEP.EXPERIMENTAL_RESULTS"),
            new AbstractMap.SimpleEntry<>("EXPERIMENTAL_GOALS", "EXPERIMENTAL_STEP.EXPERIMENTAL_GOALS"),
            new AbstractMap.SimpleEntry<>("EXPERIMENTAL_PROCEDURE", "EXPERIMENTAL_STEP.EXPERIMENTAL_DESCRIPTION"))
            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    private static final String[] WIDGET_POST_UPDATES = new String[]{
        "UPDATE property_types SET meta_data = '{ \"custom_widget\" : \"Word Processor\" }'::jsonb WHERE id IN (SELECT id FROM property_types WHERE daty_id = (SELECT id FROM data_types WHERE code = 'MULTILINE_VARCHAR'));",
        "UPDATE property_types SET meta_data = NULL WHERE id IN (SELECT id FROM property_types WHERE daty_id IN (SELECT id FROM data_types WHERE code NOT IN ('MULTILINE_VARCHAR', 'XML')));",
        "UPDATE property_types SET meta_data = NULL WHERE code IN ('NAME', 'SEQUENCE', 'AUTHORS', 'FREEFORM_TABLE_STATE');"
    };

    private static Set<ExperimentType> getExperimentTypes(String sessionToken, String[] experimentCodes) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();

        ExperimentSearchCriteria esc = new ExperimentSearchCriteria();
        esc.withOrOperator();
        for (String experimentOfTypeCollection:experimentCodes) {
            esc.withCode().thatEquals(experimentOfTypeCollection);
        }

        ExperimentFetchOptions efo = new ExperimentFetchOptions();
        efo.withType().withPropertyAssignments().withPropertyType();

        SearchResult<Experiment> experimentSearchResult = api.searchExperiments(sessionToken, esc, efo);

        Set<ExperimentType> experimentTypes = new HashSet<>();
        for (Experiment experiment:experimentSearchResult.getObjects()) {
            experimentTypes.add(experiment.getType());
        }

        return experimentTypes;
    }

    private static ExperimentType getExperimentType(String sessionToken, String experimentTypeCode) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();

        ExperimentTypeFetchOptions etfo = new ExperimentTypeFetchOptions();
        etfo.withPropertyAssignments().withPropertyType();

        EntityTypePermId COLLECTION = new EntityTypePermId(experimentTypeCode);
        Map<IEntityTypeId, ExperimentType> collectionType = api.getExperimentTypes(sessionToken, Arrays.asList(COLLECTION), etfo);
        ExperimentType experimentTypeCOLLECTION = collectionType.get(COLLECTION);
        return experimentTypeCOLLECTION;
    }

    private static Set<PropertyType> getPropertyTypes(ExperimentType type) {
        Set<PropertyType> propertyTypes = new HashSet<>();
        for (PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            propertyTypes.add(propertyAssignment.getPropertyType());
        }
        return propertyTypes;
    }

    private static Set<String> getPropertyTypesCodes(ExperimentType type) {
        Set<String> propertyTypes = new HashSet<>();
        for (PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            propertyTypes.add(propertyAssignment.getPropertyType().getCode());
        }
        return propertyTypes;
    }

    private static void addMissingProperties(String sessionToken, String experimentTypeCode, Set<PropertyType> missingPropertyTypes) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();

        List<PropertyAssignmentCreation> propertiesToAdd = new ArrayList<>();
        for (PropertyType propertyType:missingPropertyTypes) {
            PropertyAssignmentCreation pac = new PropertyAssignmentCreation();
            pac.setPropertyTypeId(propertyType.getPermId());
            propertiesToAdd.add(pac);
        }

        EntityTypePermId experimentPermId = new EntityTypePermId(experimentTypeCode);
        ExperimentTypeUpdate experimentTypeUpdate = new ExperimentTypeUpdate();
        experimentTypeUpdate.setTypeId(experimentPermId);
        ListUpdateValue.ListUpdateActionAdd luaa = new ListUpdateValue.ListUpdateActionAdd();
        luaa.setItems(propertiesToAdd);

        experimentTypeUpdate.setPropertyAssignmentActions(Arrays.asList(luaa));

        List<ExperimentTypeUpdate> experimentTypeUpdates = new ArrayList<>();
        experimentTypeUpdates.add(experimentTypeUpdate);
        if (!experimentTypeUpdates.isEmpty()) {
            api.updateExperimentTypes(sessionToken, experimentTypeUpdates);
        }
    }

    static List executeNativeQuery(String SQL) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(SQL);
        List<Object> result = nativeQuery.getResultList();
        return result;
    }

    static List executeNativeQuery(String SQL, String key, Object value) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(SQL);
        nativeQuery.setParameter(key, value);
        List<Object[]> result = nativeQuery.getResultList();
        return result;
    }

    static void executeNativeUpdate(String SQL, String ukey, Object uValue, String ckey, Object cValue) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(SQL);
        if (ukey != null) {
            nativeQuery.setParameter(ukey, uValue);
        }
        if (ckey != null) {
            nativeQuery.setParameter(ckey, cValue);
        }
        nativeQuery.executeUpdate();
    }

    static void executeNativeUpdate(String SQL) {
        try {
            DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
            Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
            Connection connection = ((SessionImpl) currentSession).connection();
            PreparedStatement statement = connection.prepareStatement(SQL);
            statement.execute();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Map<BigInteger, BigInteger> getMap(List<Object[]> prty_id_AND_etpt_id) {
        Map<BigInteger, BigInteger> prty_id_2_etpt_id = new HashMap();
        for (Object[] row:prty_id_AND_etpt_id) {
            prty_id_2_etpt_id.put((BigInteger)row[0], (BigInteger)row[1]);
        }
        return prty_id_2_etpt_id;
    }

    public static void beforeUpgrade(String sessionToken) {
        operationLog.info("ELNCollectionTypeMigration beforeUpgrade START");
        removeKindFromScript();
        migrateCollectionTypes(sessionToken);
    }

    private static final String REMOVE_KIND_FROM_SCRIPTS = "UPDATE scripts SET entity_kind = NULL;";
    private static void removeKindFromScript() {
        // This fix is needed on legacy installations
        // On some of this MANAGE_SCRIPTS for one KIND have been assigned to other KIND types violating the rules.
        // All Kinds with properties are potentially compatible making the KIND restriction really pointless.
        executeNativeUpdate(REMOVE_KIND_FROM_SCRIPTS);
    }

    private static void migrateCollectionTypes(String sessionToken) {
        // Obtain property types used by experiments that should be of type COLLECTION
        for (String experimentCode:experimentsOfTypeCollection) {
            Set<ExperimentType> experimentTypes = getExperimentTypes(sessionToken, new String[]{experimentCode});
            if (!experimentTypes.isEmpty()) {
                //operationLog.info("EXPERIMENT_CODE: " + experimentCode + " IS OF TYPE: " + experimentTypes.iterator().next().getCode());
                ExperimentType experimentType = experimentTypes.iterator().next();
                if (!experimentType.getCode().equals(COLLECTION)) {
                    // Property Type used
                    Set<PropertyType> propertyTypes = getPropertyTypes(experimentType);

                    // Property Type used in collection
                    Set<String> collectionPropertyTypes = getPropertyTypesCodes(getExperimentType(sessionToken, COLLECTION));

                    // Property Type NOT used in collection
                    Set<PropertyType> missingInCollection = new HashSet<>();
                    for (PropertyType propertyType:propertyTypes) {
                        if (!collectionPropertyTypes.contains(propertyType.getCode())) {
                            missingInCollection.add(propertyType);
                        }
                    }

                    // Add Property Type missing to collection
                    addMissingProperties(sessionToken, COLLECTION, missingInCollection);

                    //
                    // Swap experiment property types assignments that NOW should also exist in COLLECTION
                    //

                    // Current type id
                    final String COLLECTION_TYPE_ID = "SELECT id FROM experiment_types WHERE code = :code";
                    BigInteger collectionTypeTechId = (BigInteger) executeNativeQuery(COLLECTION_TYPE_ID, "code", COLLECTION).get(0);
                    final String EXPERIMENT_TYPE_ID = "SELECT exty_id FROM experiments_all WHERE code = :code";
                    BigInteger experimentTypeTechId = (BigInteger) executeNativeQuery(EXPERIMENT_TYPE_ID, "code", experimentCode).get(0);

                    // Current type properties
                    final String EXPERIMENT_PROPERTY_TYPE_IDS = "SELECT etpt.prty_id, etpt.id FROM experiment_type_property_types etpt WHERE etpt.exty_id = :exty_id";
                    Map<BigInteger, BigInteger> collection_prty_id_2_etpt_id = getMap(executeNativeQuery(EXPERIMENT_PROPERTY_TYPE_IDS, "exty_id", collectionTypeTechId));
                    Map<BigInteger, BigInteger> experiment_prty_id_2_etpt_id = getMap(executeNativeQuery(EXPERIMENT_PROPERTY_TYPE_IDS, "exty_id", experimentTypeTechId));

                    // Update properties
                    for (BigInteger propertyTechId:experiment_prty_id_2_etpt_id.keySet()) {
                        BigInteger oldAssignment = experiment_prty_id_2_etpt_id.get(propertyTechId);
                        BigInteger newAssignment = collection_prty_id_2_etpt_id.get(propertyTechId);
                        final String UPDATE_PROPERTY_ASSIGNMENT = "UPDATE experiment_properties SET etpt_id = :new_etpt_id WHERE etpt_id = :old_etpt_id";
                        operationLog.info("ELNCollectionTypeMigration - Swap for property tech id : " + propertyTechId + " : " + oldAssignment + " <> " + newAssignment);
                        executeNativeUpdate(UPDATE_PROPERTY_ASSIGNMENT, "old_etpt_id", oldAssignment, "new_etpt_id", newAssignment);
                    }

                    // Update type
                    final String UPDATE_TYPE = "UPDATE experiments_all SET exty_id = :exty_id WHERE code = :code";
                    executeNativeUpdate(UPDATE_TYPE, "exty_id", collectionTypeTechId, "code", experimentCode);
                    operationLog.info("ELNCollectionTypeMigration -  Update for : " + experimentCode + " : exty_id : " + collectionTypeTechId);

                }
            }
        }

        for (Map.Entry<String, String> entry : PROPERTY_UPDATES_MAP.entrySet()) {
            operationLog.info("Going to Execute PROPERTY_UPDATE: " + entry.getKey());

            String query = String.format("UPDATE property_types SET code = '%s' WHERE code = '%s' and " +
                                         "(select count(*) from property_types where code = '%s') = 0 and " +
                                         "(select count(*) from core_plugins where name = 'eln-lims') > 0;",
                                         entry.getValue(), entry.getKey(), entry.getValue());

            executeNativeUpdate(query, null, null, null, null);
            operationLog.info("PROPERTY_UPDATE DONE");
        }
        operationLog.info("ELNCollectionTypeMigration beforeUpgrade END");
    }

    public static void afterUpgrade() {
        operationLog.info("ELNCollectionTypeMigration afterUpgrade START");
        for (String WIDGET_POST_UPDATE:WIDGET_POST_UPDATES) {
            operationLog.info("Going to Execute WIDGET_POST_UPDATES: " + WIDGET_POST_UPDATE);
            executeNativeUpdate(WIDGET_POST_UPDATE);
            operationLog.info("WIDGET_POST_UPDATE DONE");
        }
        operationLog.info("ELNCollectionTypeMigration afterUpgrade END");
    }
}
