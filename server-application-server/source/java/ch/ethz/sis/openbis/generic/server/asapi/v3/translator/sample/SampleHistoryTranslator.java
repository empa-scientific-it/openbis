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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.id.UnknownRelatedObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.IDataSetAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.ExperimentDataSetRelationshipHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.ExperimentProjectRelationshipHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.ExperimentPropertyHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.ExperimentSampleRelationshipHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project.IProjectAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.ISpaceAuthorizationValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class SampleHistoryTranslator extends HistoryTranslator implements ISampleHistoryTranslator
{
    @Autowired
    private SamplePropertyHistoryTranslator propertyHistoryTranslator;

    @Autowired
    private SampleSpaceRelationshipHistoryTranslator spaceRelationshipHistoryTranslator;

    @Autowired
    private SampleProjectRelationshipHistoryTranslator projectRelationshipHistoryTranslator;

    @Autowired
    private SampleExperimentRelationshipHistoryTranslator experimentRelationshipHistoryTranslator;

    @Autowired
    private SampleParentRelationshipHistoryTranslator parentRelationshipHistoryTranslator;

    @Autowired
    private SampleChildRelationshipHistoryTranslator childRelationshipHistoryTranslator;

    @Autowired
    private SampleContainerRelationshipHistoryTranslator containerRelationshipHistoryTranslator;

    @Autowired
    private SampleComponentRelationshipHistoryTranslator componentRelationshipHistoryTranslator;

    @Autowired
    private SampleDataSetRelationshipHistoryTranslator dataSetRelationshipHistoryTranslator;

    @Autowired
    private SampleUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Override protected List<ITranslator<Long, ObjectHolder<List<HistoryEntry>>, HistoryEntryFetchOptions>> getTranslators()
    {
        return Arrays.asList(propertyHistoryTranslator, spaceRelationshipHistoryTranslator, projectRelationshipHistoryTranslator,
                experimentRelationshipHistoryTranslator, parentRelationshipHistoryTranslator, childRelationshipHistoryTranslator,
                containerRelationshipHistoryTranslator, componentRelationshipHistoryTranslator, dataSetRelationshipHistoryTranslator,
                unknownRelationshipHistoryTranslator);
    }
}
