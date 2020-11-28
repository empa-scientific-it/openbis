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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper;

import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

import java.util.HashMap;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class AttributesMapper
{
    public static final String PERM_ID_ATTRIBUTE = "perm id";

    private static final Map<String, String> ATTRIBUTE_ID_TO_COLUMN_NAME = new HashMap<>();

    /** Defines which columns of which entities should be treated as perm_id. It can be PERM_ID_COLUMN or CODE_COLUMN. */
    private static final Map<String, String> ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME = new HashMap<>();

    static
    {
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.CODE, ColumnNames.CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("codes", ColumnNames.CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.ID, ColumnNames.ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.FILE_DESCRIPTION, ColumnNames.DESCRIPTION_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.MODIFICATION_DATE, ColumnNames.MODIFICATION_TIMESTAMP_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put(SearchFieldConstants.REGISTRATION_DATE, ColumnNames.REGISTRATION_TIMESTAMP_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("userIds", ColumnNames.USER_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("externalCode", ColumnNames.EXTERNAL_CODE_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("archivingRequested", ColumnNames.ARCHIVING_REQUESTED);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("presentInArchive", ColumnNames.PRESENT_IN_ARCHIVE);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("shareId", ColumnNames.SHARE_ID_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("speedHint", ColumnNames.SPEED_HINT);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("storageConfirmation", ColumnNames.STORAGE_CONFIRMATION);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("gitCommitHash", ColumnNames.GIT_COMMIT_HASH_COLUMN);
        ATTRIBUTE_ID_TO_COLUMN_NAME.put("gitRepositoryId", ColumnNames.GIT_REPOSITORY_ID_COLUMN);

        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(CONTROLLED_VOCABULARY_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(CONTROLLED_VOCABULARY_TERM_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_SET_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_STORES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_SET.getEntitiesTable(), ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(DATA_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(GRID_CUSTOM_COLUMNS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(EXPERIMENT_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(FILE_FORMAT_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SPACES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(LOCATOR_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(MATERIAL_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(MATERIALS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(PROPERTY_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SAMPLE_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(RELATIONSHIP_TYPES_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(AUTHORIZATION_GROUPS_TABLE, ColumnNames.CODE_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(OPERATION_EXECUTIONS_TABLE, ColumnNames.CODE_COLUMN);

        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(EXPERIMENT.getEntitiesTable(), ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(PROJECTS_TABLE, ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SAMPLE.getEntitiesTable(), ColumnNames.PERM_ID_COLUMN);
        ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.put(SEMANTIC_ANNOTATIONS_TABLE, ColumnNames.PERM_ID_COLUMN);
    }

    private AttributesMapper()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getColumnName(final String attributeId, final String tableName, final String defaultValue)
    {
        return PERM_ID_ATTRIBUTE.equals(attributeId) || "ids".equals(attributeId) ?
                ENTITIES_TABLE_TO_PERM_ID_COLUMN_NAME.getOrDefault(tableName, defaultValue) :
                ATTRIBUTE_ID_TO_COLUMN_NAME.getOrDefault(attributeId, defaultValue);
    }

}
