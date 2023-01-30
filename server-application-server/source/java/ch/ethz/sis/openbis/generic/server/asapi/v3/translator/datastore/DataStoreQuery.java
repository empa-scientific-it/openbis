/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.datastore;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface DataStoreQuery extends ObjectQuery
{

    @Select(sql = "select ds.id, ds.code, ds.download_url as downloadUrl, ds.remote_url as remoteUrl, "
            + "ds.modification_timestamp as modificationDate, ds.registration_timestamp as registrationDate "
            + "from data_stores ds where ds.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataStoreBaseRecord> getDataStores(LongSet dataStoreIds);

}
