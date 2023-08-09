/*
 * Copyright ETH 2020 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.hotfix;

import static ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;

public class ELNFixes {

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, ELNAnnotationsMigration.class);

    public static void beforeUpgrade(String sessionToken) throws Exception {
        operationLog.info("ELNFixes beforeUpgrade START");
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        storageValidationLevelFix(sessionToken, api);
        nameNoRTFFix(sessionToken, api);
        fixProperties("sample_properties", "sample_type_property_types", "stpt_id");
        fixProperties("experiment_properties", "experiment_type_property_types", "etpt_id");
        fixProperties("data_set_properties", "data_set_type_property_types", "dstpt_id");
        operationLog.info("ELNFixes beforeUpgrade FINISH");
    }

    private static final String STORAGE_VALIDATION_LEVEL_PROPERTY_CODE = "$STORAGE.STORAGE_VALIDATION_LEVEL";
    private static final String STORAGE_VALIDATION_LEVEL_DEFAULT_VALUE = "RACK";

    private static void storageValidationLevelFix(String sessionToken, IApplicationServerInternalApi api) {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("STORAGE");

        SampleFetchOptions options = new SampleFetchOptions();
        options.withProperties();

        SearchResult<Sample> storages = api.searchSamples(sessionToken, criteria, options);

        List<SampleUpdate> storageUpdates = new ArrayList<>();
        for (Sample storage:storages.getObjects()) {
            if(storage.getProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE) == null ||
                    storage.getProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE).isEmpty()) {
                SampleUpdate storageUpdate = new SampleUpdate();
                storageUpdate.setSampleId(storage.getPermId());
                storageUpdate.setProperty(STORAGE_VALIDATION_LEVEL_PROPERTY_CODE, STORAGE_VALIDATION_LEVEL_DEFAULT_VALUE);
                storageUpdates.add(storageUpdate);
            }
        }

        if (!storageUpdates.isEmpty()) {
            api.updateSamples(sessionToken, storageUpdates);
        }
        operationLog.info("ELNFixes storageValidationLevelFix: " + storageUpdates.size());
    }

    private static final String NAME_PROPERTY_CODE = "$NAME";

    private static void nameNoRTFFix(String sessionToken, IApplicationServerInternalApi api) {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withStringProperty(NAME_PROPERTY_CODE).thatContains("<");
        criteria.withType().withCode().thatEquals("ENTRY");
        SampleFetchOptions options = new SampleFetchOptions();
        options.withProperties();

        SearchResult<Sample> namesRTF = api.searchSamples(sessionToken, criteria, options);

        List<SampleUpdate> nameUpdates = new ArrayList<>();
        for (Sample nameRTF:namesRTF.getObjects()) {
            SampleUpdate nameUpdate = new SampleUpdate();
            nameUpdate.setSampleId(nameRTF.getPermId());
            nameUpdate.setProperty(NAME_PROPERTY_CODE, nameRTF.getProperty(NAME_PROPERTY_CODE).replaceAll( "(<([^>]+)>)", ""));
            nameUpdates.add(nameUpdate);
        }

        if (!nameUpdates.isEmpty()) {
            api.updateSamples(sessionToken, nameUpdates);
        }
        operationLog.info("ELNFixes nameNoRTFFix: " + nameUpdates.size());
    }

    public static boolean isELNInstalled() {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        List<CorePluginPE> elnLims = daoFactory.getCorePluginDAO().listCorePluginsByName("eln-lims");
        return (elnLims != null && elnLims.size() > 0);
    }

    /*
     * This is a heuristic to determine if an openBIS instance is multi group.
     *
     * Can have false positives if:
     *  - MULTI_GROUP_CONFIG_KEY is left on the service.properties by mistake but plugin not actually configured.
     *  - MULTI_GROUP_SPACES returns spaces with that particular pattern.
     *
     * If positive some spaces will NOT be created by the ELN-LIMS.
     */
    private static final String MULTI_GROUP_CONFIG_KEY = "user-management-config-file-path";
    private static final String MULTI_GROUP_SPACES = "SELECT COUNT(*) > 0 FROM spaces WHERE code LIKE '%_ELN_SETTINGS'";
    public static boolean isMultiGroup() {
        List<Object> isMultiGroupSpaces = ELNCollectionTypeMigration.executeNativeQuery(MULTI_GROUP_SPACES);
        String isMultiGroupProperty = getProperty(MULTI_GROUP_CONFIG_KEY);
        return (Boolean) isMultiGroupSpaces.get(0) || isMultiGroupProperty != null;
    }

    static String getProperty(String key) {
        ExposablePropertyPlaceholderConfigurer configurer = ((ExposablePropertyPlaceholderConfigurer) CommonServiceProvider.tryToGetBean(PROPERTY_CONFIGURER_BEAN_NAME));
        return configurer.getResolvedProps().getProperty(key);
    }

    private static void fixProperties(final String propertiesTable, final String entityTypePropertyTypesTable,
            final String entityTypePropertyTypesColumn) {
        ELNCollectionTypeMigration.executeNativeUpdate(
            String.format("UPDATE %s prop\n"
                    + "SET value = null\n"
                    + "FROM %s etpt\n"
                    + "INNER JOIN property_types prty ON etpt.prty_id = prty.id\n"
                    + "INNER JOIN data_types daty ON prty.daty_id = daty.id\n"
                    + "WHERE prop.%s IS NOT NULL AND prop.%s = etpt.id AND daty.code = 'CONTROLLEDVOCABULARY'",
                    propertiesTable, entityTypePropertyTypesTable, entityTypePropertyTypesColumn, entityTypePropertyTypesColumn));
        ELNCollectionTypeMigration.executeNativeUpdate(
            String.format("UPDATE %s prop\n"
                + "SET cvte_id = null\n"
                + "FROM %s etpt\n"
                + "INNER JOIN property_types prty ON etpt.prty_id = prty.id\n"
                + "INNER JOIN data_types daty ON prty.daty_id = daty.id\n"
                + "WHERE prop.%s IS NOT NULL AND prop.%s = etpt.id AND daty.code != 'CONTROLLEDVOCABULARY'",
                propertiesTable, entityTypePropertyTypesTable, entityTypePropertyTypesColumn, entityTypePropertyTypesColumn));
    }

}
