/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.attachment;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author Franz-Josef Elmer
 */
public interface AttachmentQuery extends ObjectQuery
{
    @Select(sql = "select proj_id as objectId, id as relatedId from attachments where proj_id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getProjectAttachments(LongSet projectIds);

    @Select(sql = "select samp_id as objectId, id as relatedId from attachments where samp_id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleAttachments(LongSet sampleIds);

    @Select(sql = "select expe_id as objectId, id as relatedId from attachments where expe_id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperimentAttachments(LongSet experimentIds);

    @Select(sql = "select a.id, a.title, a.file_name as fileName, a.description, "
            + "a.registration_timestamp as registrationDate, a.version, "
            + "p.code as projectCode, sp.code as spaceCode, "
            + "s.perm_id as samplePermId, e.perm_id as experimentPermId "
            + "from attachments a "
            + "left outer join projects p on a.proj_id=p.id left outer join spaces sp on p.space_id=sp.id "
            + "left outer join samples s on a.samp_id=s.id "
            + "left outer join experiments e on a.expe_id=e.id "
            + "where a.id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<AttachmentBaseRecord> getAttachments(LongSet attachmentIds);

    @Select(sql = "select id as objectId, pers_id_registerer as relatedId from attachments where id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet attachmentIds);

    @Select(sql = "select id as objectId, exac_id as relatedId from attachments where id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getContentIds(LongSet attachmentIds);

    @Select(sql = "select id,value as content from attachment_contents where id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<AttachmentContentRecord> getAttachmentContents(LongSet attachmentContentsIds);

}
