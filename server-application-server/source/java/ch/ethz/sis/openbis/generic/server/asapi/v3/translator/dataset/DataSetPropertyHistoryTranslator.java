/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.ContentCopyHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryContentCopyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.PropertyHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class DataSetPropertyHistoryTranslator extends PropertyHistoryTranslator
{

    @Override protected List<? extends PropertyRecord> loadProperties(final Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getProperties(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

}
