/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;

/**
 * @author pkupczyk
 */
@Component
public class DataSetHistoryTranslator extends HistoryTranslator implements IDataSetHistoryTranslator
{

    @Autowired
    private DataSetPropertyHistoryTranslator propertyHistoryTranslator;

    @Autowired
    private DataSetExperimentRelationshipHistoryTranslator experimentRelationshipHistoryTranslator;

    @Autowired
    private DataSetSampleRelationshipHistoryTranslator sampleRelationshipHistoryTranslator;

    @Autowired
    private DataSetParentRelationshipHistoryTranslator parentRelationshipHistoryTranslator;

    @Autowired
    private DataSetChildRelationshipHistoryTranslator childRelationshipHistoryTranslator;

    @Autowired
    private DataSetContainerRelationshipHistoryTranslator containerRelationshipHistoryTranslator;

    @Autowired
    private DataSetComponentRelationshipHistoryTranslator componentRelationshipHistoryTranslator;

    @Autowired
    private DataSetUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Autowired
    private DataSetContentCopyHistoryTranslator contentCopyHistoryTranslator;

    @Override protected List<ITranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>> getTranslators()
    {
        return Arrays.asList(propertyHistoryTranslator, experimentRelationshipHistoryTranslator, sampleRelationshipHistoryTranslator,
                parentRelationshipHistoryTranslator, childRelationshipHistoryTranslator, containerRelationshipHistoryTranslator,
                componentRelationshipHistoryTranslator, unknownRelationshipHistoryTranslator, contentCopyHistoryTranslator);
    }
}
