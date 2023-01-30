/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * Interfaces to query basic information about samples and experiments referenced from other objects. Note that this interface is not intended to be
 * used to fetch primary entities.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses =
{ SampleReferenceRecord.class })
public interface ISecondaryEntityListingQuery extends BaseQuery
{
    public static final String SELECT_FROM_EXPERIMENTS =
            "select e.id as id, e.code as e_code, e.perm_id as e_permid, e.del_id as del_id, et.code as et_code, "
                    + "p.code as p_code, p.id as p_id, p.perm_id as p_perm_id, g.code as spc_code from experiments e "
                    + "join experiment_types et on e.exty_id=et.id join projects p on e.proj_id=p.id "
                    + "join spaces g on p.space_id=g.id";

    public static final String SELECT_FROM_PROJECTS =
            "select p.id as p_id, "
                    + "p.code as p_code, p.perm_id as p_perm_id, g.code as spc_code from "
                    + "projects p "
                    + "join spaces g on p.space_id=g.id";

    public static final int FETCH_SIZE = 1000;

    //
    // Experiments
    //

    /**
     * Returns the code of an experiment and its project by the experiment <var>id</var>.
     * 
     * @param experimentId The id of the experiment to get the code for.
     */
    @Select(SELECT_FROM_EXPERIMENTS + " where e.id=?{1}")
    public ExperimentProjectSpaceCodeRecord getExperimentAndProjectAndGroupCodeForId(
            long experimentId);

    @Select(SELECT_FROM_PROJECTS + " where p.id=?{1}")
    public ExperimentProjectSpaceCodeRecord getProjectAndGroupCodeForId(
            long experimentId);

    @Select(sql = SELECT_FROM_EXPERIMENTS + " where e.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<ExperimentProjectSpaceCodeRecord> getExperiments(LongSet experimentIds);

    //
    // Samples
    //

    /**
     * Returns the samples for the given ids.
     */
    @Select(sql = "select s.id as id, s.perm_id as perm_id, s.code as s_code, s.del_id as del_id, "
            + "           st.code as st_code, g.code as spc_code, c.code as c_code,"
            + "           p.id as proj_id, p.code as proj_code, ps.code as proj_space_code"
            + "   from samples s join sample_types st on s.saty_id=st.id"
            + "                  join spaces g on s.space_id=g.id "
            + "                  left join projects p on s.proj_id=p.id "
            + "                  left join spaces ps on p.space_id=ps.id "
            + "                  left join samples c on s.samp_id_part_of=c.id "
            + "        where s.id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleReferenceRecord> getSamples(LongSet sampleIds);

    /**
     * Returns all children sample ids of the specified samples.
     */
    @Select(sql = "SELECT sample_id_child FROM sample_relationships "
            + "    WHERE sample_id_parent = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getChildrenIds(LongSet parentSampleIds);

    //
    // Persons
    //

    /**
     * Returns the person for the given <var>personId</var>
     * 
     * @param personId The id of the Person you want to get.
     */
    @Select("select first_name as firstName, last_name as lastName, email, user_id as userId from persons where id=?{1}")
    public Person getPersonById(long personId);

    /**
     * Returns all spaces of this data base instance.
     */
    @Select("select id, code from spaces")
    public Space[] getAllSpaces();

    /**
     * Returns the technical id of a group for given <var>spaceCode</code>.
     */
    @Select("select id from spaces where code=?{1}")
    public long getGroupIdForCode(String spaceCode);

    /**
     * Returns the technical id of a sample type for given <var>sampleTypeCode</code> or <code>null</code> if such sample type doesn't exist.
     */
    @Select("select id from sample_types where code=?{1}")
    public Long getSampleTypeIdForSampleTypeCode(String sampleTypeCode);

}
