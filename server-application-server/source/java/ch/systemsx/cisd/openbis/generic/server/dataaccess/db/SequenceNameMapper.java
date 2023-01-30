/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.db.StandardSequenceNameMapper;
import ch.systemsx.cisd.dbmigration.DatabaseVersionLogDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SequenceNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * A small <code>StandardSequenceNameMapper</code> which can handle sequencers that are not constructed the standard way.
 * 
 * @author Franz-Josef Elmer
 */
public final class SequenceNameMapper extends StandardSequenceNameMapper
{
    private static final Map<String, String> createMap()
    {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put(TableNames.DATA_VIEW, SequenceNames.DATA_SEQUENCE); // needed for old migrations
        map.put(TableNames.DATA_ALL_TABLE, SequenceNames.DATA_SEQUENCE);
        map.put(TableNames.SAMPLES_ALL_TABLE, SequenceNames.SAMPLE_SEQUENCE);
        map.put(TableNames.SAMPLE_RELATIONSHIPS_VIEW, SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE);
        map.put(TableNames.SAMPLE_RELATIONSHIPS_ALL_TABLE,
                SequenceNames.SAMPLE_RELATIONSHIPS_SEQUENCE);
        map.put(TableNames.EXPERIMENTS_ALL_TABLE, SequenceNames.EXPERIMENT_SEQUENCE);
        map.put(TableNames.MATERIAL_BATCHES_TABLE, SequenceNames.MATERIAL_BATCH_SEQUENCE);
        map.put(TableNames.DATA_STORES_TABLE, SequenceNames.DATA_STORE_SEQUENCE);
        map.put(TableNames.MATERIAL_PROPERTIES_TABLE, SequenceNames.MATERIAL_PROPERTY_SEQUENCE);
        map.put(TableNames.MATERIAL_PROPERTIES_HISTORY_TABLE,
                SequenceNames.MATERIAL_PROPERTY_SEQUENCE);
        map.put(TableNames.MATERIAL_TYPE_PROPERTY_TYPE_TABLE,
                SequenceNames.MATERIAL_TYPE_PROPERTY_TYPE_SEQUENCE);
        map.put(TableNames.EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE,
                SequenceNames.EXPERIMENT_TYPE_PROPERTY_TYPE_SEQUENCE);
        map.put(TableNames.EXPERIMENT_PROPERTIES_TABLE, SequenceNames.EXPERIMENT_PROPERTY_SEQUENCE);
        map.put(TableNames.EXPERIMENT_PROPERTIES_HISTORY_TABLE,
                SequenceNames.EXPERIMENT_PROPERTY_SEQUENCE);
        map.put(TableNames.SAMPLE_TYPE_PROPERTY_TYPE_TABLE,
                SequenceNames.SAMPLE_TYPE_PROPERTY_TYPE_SEQUENCE);
        map.put(TableNames.DATA_SET_TYPE_PROPERTY_TYPE_TABLE,
                SequenceNames.DATA_SET_TYPE_PROPERTY_TYPE_SEQUENCE);
        map.put(TableNames.SAMPLE_MATERIAL_BATCHES_TABLE,
                SequenceNames.SAMPLE_MATERIAL_BATCH_SEQUENCE);
        map.put(TableNames.SAMPLE_PROPERTIES_TABLE, SequenceNames.SAMPLE_PROPERTY_SEQUENCE);
        map.put(TableNames.SAMPLE_PROPERTIES_HISTORY_TABLE, SequenceNames.SAMPLE_PROPERTY_SEQUENCE);
        map.put(TableNames.CONTROLLED_VOCABULARY_TABLE,
                SequenceNames.CONTROLLED_VOCABULARY_SEQUENCE);
        map.put(TableNames.CONTROLLED_VOCABULARY_TERM_TABLE,
                SequenceNames.CONTROLLED_VOCABULARY_TERM_SEQUENCE);
        map.put(TableNames.DATA_SET_PROPERTIES_TABLE, SequenceNames.DATA_SET_PROPERTY_SEQUENCE);
        map.put(TableNames.DATA_SET_PROPERTIES_HISTORY_TABLE,
                SequenceNames.DATA_SET_PROPERTY_SEQUENCE);
        map.put(TableNames.DATA_STORE_SERVICES_TABLE, SequenceNames.DATA_STORE_SERVICE_SEQUENCE);
        map.put(TableNames.METAPROJECT_ASSIGNMENTS_ALL_TABLE,
                SequenceNames.METAPROJECT_ASSIGNMENTS_SEQUENCE);
        map.put(TableNames.DATA_SET_RELATIONSHIPS_HISTORY_TABLE,
                SequenceNames.DATA_SET_RELATIONSHIPS_HISTORY_SEQUENCE);
        map.put(TableNames.POST_REGISTRATION_DATASET_QUEUE_TABLE,
                SequenceNames.POST_REGISTRATION_DATASET_QUEUE_SEQUENCE);
        // legacy code to make migration tests from db version 018 work
        map.put("sample_component_materials", "SCMA_ID_SEQ");
        return Collections.unmodifiableMap(map);
    }

    private static final Set<String> createTableSetWithoutSequencers()
    {
        final HashSet<String> set = new HashSet<String>();
        add(set, DatabaseVersionLogDAO.DB_VERSION_LOG);
        add(set, TableNames.EXTERNAL_DATA_TABLE);
        add(set, TableNames.SAMPLE_INPUTS_TABLE);
        add(set, TableNames.SAMPLE_MATERIAL_BATCHES_TABLE);
        add(set, TableNames.DATA_SET_RELATIONSHIPS_VIEW);
        add(set, TableNames.DATA_SET_RELATIONSHIPS_ALL_TABLE);
        add(set, TableNames.DATA_STORE_SERVICES_DATASET_TYPES_TABLE);
        add(set, TableNames.LINK_DATA_TABLE);
        add(set, TableNames.AUTHORIZATION_GROUP_PERSONS_TABLE);
        add(set, TableNames.CONTENT_COPIES_TABLE);
        return Collections.unmodifiableSet(set);
    }

    private static void add(HashSet<String> set, String tableName)
    {
        set.add(tableName.toLowerCase());
    }

    public SequenceNameMapper()
    {
        super(createMap(), createTableSetWithoutSequencers());
    }

}
