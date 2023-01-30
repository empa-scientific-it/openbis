/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.util.*;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.SearchPropertyAssignmentsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.EntityKindConverter;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.IPropertyAssignmentTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentRecord;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchPropertyAssignmentsOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<PropertyAssignment, EntityTypePropertyTypePE, PropertyAssignmentSearchCriteria, PropertyAssignmentFetchOptions>
        implements ISearchPropertyAssignmentsOperationExecutor
{

    @Autowired
    private ISearchPropertyAssignmentExecutor searchExecutor;

    @Autowired
    private IPropertyAssignmentTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<PropertyAssignmentSearchCriteria, PropertyAssignmentFetchOptions>> getOperationClass()
    {
        return SearchPropertyAssignmentsOperation.class;
    }

    @Override
    protected List<EntityTypePropertyTypePE> doSearch(IOperationContext context, PropertyAssignmentSearchCriteria criteria,
            PropertyAssignmentFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected Map<EntityTypePropertyTypePE, PropertyAssignment> doTranslate(TranslationContext translationContext,
            Collection<EntityTypePropertyTypePE> assignments,
            PropertyAssignmentFetchOptions fetchOptions)
    {
        Map<PropertyAssignmentKey, EntityTypePropertyTypePE> keyToPeMap = new HashMap<PropertyAssignmentKey, EntityTypePropertyTypePE>();
        List<PropertyAssignmentRecord> assignmentRecords = new ArrayList<PropertyAssignmentRecord>();

        for (EntityTypePropertyTypePE assignment : assignments)
        {
            PropertyAssignmentKey key =
                    new PropertyAssignmentKey(assignment.getId(), EntityKindConverter.convert(assignment.getEntityType().getEntityKind()));
            keyToPeMap.put(key, assignment);

            PropertyAssignmentRecord assignmentRecord = new PropertyAssignmentRecord();
            assignmentRecord.id = assignment.getId();
            assignmentRecord.is_mandatory = assignment.isMandatory();
            assignmentRecord.is_shown_edit = assignment.isShownInEditView();
            assignmentRecord.kind_code = assignment.getEntityType().getEntityKind().name();
            assignmentRecord.ordinal = assignment.getOrdinal().intValue();
            assignmentRecord.pers_id_registerer = assignment.getRegistrator().getId();
            assignmentRecord.prty_code = assignment.getPropertyType().getCode();
            assignmentRecord.prty_id = assignment.getPropertyType().getId();
            assignmentRecord.registration_timestamp = assignment.getRegistrationDate();
            assignmentRecord.section = assignment.getSection();
            assignmentRecord.show_raw_value = assignment.getShowRawValue();
            assignmentRecord.type_code = assignment.getEntityType().getCode();
            assignmentRecord.type_id = assignment.getEntityType().getId();
            ScriptPE script = assignment.getScript();
            if (script != null)
            {
                assignmentRecord.script_id = script.getId();
            }
            assignmentRecords.add(assignmentRecord);
        }

        Map<PropertyAssignmentKey, PropertyAssignment> keyToAssignmentMap =
                translator.getKeyToAssignmentMap(translationContext, assignmentRecords, fetchOptions);
        Map<EntityTypePropertyTypePE, PropertyAssignment> peToAssignmentMap = new HashMap<EntityTypePropertyTypePE, PropertyAssignment>();

        for (Map.Entry<PropertyAssignmentKey, PropertyAssignment> entry : keyToAssignmentMap.entrySet())
        {
            PropertyAssignmentKey key = entry.getKey();
            PropertyAssignment assignment = entry.getValue();
            EntityTypePropertyTypePE pe = keyToPeMap.get(key);
            peToAssignmentMap.put(pe, assignment);
        }

        return peToAssignmentMap;
    }

    @Override
    protected SearchObjectsOperationResult<PropertyAssignment> getOperationResult(SearchResult<PropertyAssignment> searchResult)
    {
        return new SearchPropertyAssignmentsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<PropertyAssignmentSearchCriteria, PropertyAssignment, EntityTypePropertyTypePE> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
