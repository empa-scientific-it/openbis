/*
 * Copyright 2021 ETH Zuerich, SIS
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

import java.util.List;

import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

/**
 * @author Franz-Josef Elmer
 */
public interface IDeletionQuery extends BaseQuery
{
    public static final int FETCH_SIZE = 1000;

    @Select(sql = "select distinct del_id from samples_all where expe_id in "
            + "(select id from experiments_all where del_id = any(?{1}))",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getSampleDeletionsOfExperimentDeletions(LongSet deletionIds);

    @Select(sql = "select distinct del_id from samples_all where samp_id_part_of in "
            + "(select id from samples_all where del_id = any(?{1}))",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getSampleDeletionsOfContainerDeletions(LongSet deletionIds);

    @Select(sql = "select distinct del_id from data_all where expe_id in "
            + "(select id from experiments_all where del_id = any(?{1}))",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getDataSetDeletionsOfExperimentDeletions(LongSet deletionIds);

    @Select(sql = "select distinct del_id from data_all where samp_id in "
            + "(select id from samples_all where del_id = any(?{1}))",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getDataSetDeletionsOfSampleDeletions(LongSet deletionIds);

}
