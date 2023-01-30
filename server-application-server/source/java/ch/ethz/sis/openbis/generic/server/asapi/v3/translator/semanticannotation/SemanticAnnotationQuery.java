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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface SemanticAnnotationQuery extends ObjectQuery
{

    @Select(sql = "select sa.id, sa.perm_id as permId"
            + ", sa.predicate_ontology_id as predicateOntologyId"
            + ", sa.predicate_ontology_version as predicateOntologyVersion"
            + ", sa.predicate_accession_id as predicateAccessionId"
            + ", sa.descriptor_ontology_id as descriptorOntologyId"
            + ", sa.descriptor_ontology_version as descriptorOntologyVersion"
            + ", sa.descriptor_accession_id as descriptorAccessionId"
            + ", sa.creation_date as creationDate"
            + " from semantic_annotations sa where sa.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SemanticAnnotationBaseRecord> getSemanticAnnotations(LongSet annotationIds);

    @Select(sql = "select sa.id as objectId, sa.saty_id as relatedId from semantic_annotations sa where sa.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleTypeIds(LongSet annotationIds);

    @Select(sql = "select sa.id as objectId, sa.stpt_id as relatedId from semantic_annotations sa where sa.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleTypePropertyTypeIds(LongSet annotationIds);

    @Select(sql = "select sa.id as objectId, sa.prty_id as relatedId from semantic_annotations sa where sa.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyTypeIds(LongSet annotationIds);

}
