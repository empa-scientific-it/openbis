/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class ProjectTranslator extends AbstractCachingTranslator<Long, Project, ProjectFetchOptions> implements IProjectTranslator
{

    @Autowired
    private IProjectAuthorizationValidator authorizationValidator;

    @Autowired
    private IProjectBaseTranslator baseTranslator;

    @Autowired
    private IProjectSpaceTranslator spaceTranslator;

    @Autowired
    private IProjectExperimentTranslator experimentTranslator;

    @Autowired
    private IProjectSampleTranslator sampleTranslator;

    @Autowired
    private IProjectRegistratorTranslator registratorTranslator;

    @Autowired
    private IProjectModifierTranslator modifierTranslator;

    @Autowired
    private IProjectLeaderTranslator leaderTranslator;

    @Autowired
    private IProjectAttachmentTranslator attachmentTranslator;

    @Autowired
    private IProjectHistoryTranslator historyTranslator;

    @Autowired
    private ProjectSpaceRelationshipHistoryTranslator spaceRelationshipHistoryTranslator;

    @Autowired
    private ProjectExperimentRelationshipHistoryTranslator experimentRelationshipHistoryTranslator;

    @Autowired
    private ProjectSampleRelationshipHistoryTranslator sampleRelationshipHistoryTranslator;

    @Autowired
    private ProjectUnknownRelationshipHistoryTranslator unknownRelationshipHistoryTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> projectIds, ProjectFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), projectIds);
    }

    @Override
    protected Project createObject(TranslationContext context, Long projectId, ProjectFetchOptions fetchOptions)
    {
        Project project = new Project();
        project.setFetchOptions(new ProjectFetchOptions());
        return project;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> projectIds, ProjectFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IProjectBaseTranslator.class, baseTranslator.translate(context, projectIds, null));

        if (fetchOptions.hasSpace())
        {
            relations.put(IProjectSpaceTranslator.class, spaceTranslator.translate(context, projectIds, fetchOptions.withSpace()));
        }

        if (fetchOptions.hasExperiments())
        {
            relations.put(IProjectExperimentTranslator.class, experimentTranslator.translate(context, projectIds, fetchOptions.withExperiments()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(IProjectSampleTranslator.class, sampleTranslator.translate(context, projectIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IProjectRegistratorTranslator.class,
                    registratorTranslator.translate(context, projectIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IProjectModifierTranslator.class, modifierTranslator.translate(context, projectIds, fetchOptions.withModifier()));
        }

        if (fetchOptions.hasLeader())
        {
            relations.put(IProjectLeaderTranslator.class, leaderTranslator.translate(context, projectIds, fetchOptions.withLeader()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(IProjectAttachmentTranslator.class, attachmentTranslator.translate(context, projectIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IProjectHistoryTranslator.class, historyTranslator.translate(context, projectIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasSpaceHistory())
        {
            relations.put(ProjectSpaceRelationshipHistoryTranslator.class,
                    spaceRelationshipHistoryTranslator.translate(context, projectIds, fetchOptions.withSpaceHistory()));
        }

        if (fetchOptions.hasExperimentsHistory())
        {
            relations.put(ProjectExperimentRelationshipHistoryTranslator.class,
                    experimentRelationshipHistoryTranslator.translate(context, projectIds, fetchOptions.withExperimentsHistory()));
        }

        if (fetchOptions.hasSamplesHistory())
        {
            relations.put(ProjectSampleRelationshipHistoryTranslator.class,
                    sampleRelationshipHistoryTranslator.translate(context, projectIds, fetchOptions.withSamplesHistory()));
        }

        if (fetchOptions.hasUnknownHistory())
        {
            relations.put(ProjectUnknownRelationshipHistoryTranslator.class,
                    unknownRelationshipHistoryTranslator.translate(context, projectIds, fetchOptions.withUnknownHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long projectId, Project result, Object objectRelations, ProjectFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ProjectBaseRecord baseRecord = relations.get(IProjectBaseTranslator.class, projectId);

        result.setCode(baseRecord.code);
        result.setPermId(new ProjectPermId(baseRecord.permId));
        result.setIdentifier(new ProjectIdentifier(baseRecord.spaceCode, baseRecord.code));
        result.setDescription(baseRecord.description);
        result.setFrozen(baseRecord.frozen);
        result.setFrozenForExperiments(baseRecord.frozenForExperiments);
        result.setFrozenForSamples(baseRecord.frozenForSamples);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);

        if (fetchOptions.hasSpace())
        {
            result.setSpace(relations.get(IProjectSpaceTranslator.class, projectId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasExperiments())
        {
            result.setExperiments((List<Experiment>) relations.get(IProjectExperimentTranslator.class, projectId));
            result.getFetchOptions().withExperimentsUsing(fetchOptions.withExperiments());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples(SampleUtils.extractSamples(relations.get(IProjectSampleTranslator.class, projectId)));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IProjectRegistratorTranslator.class, projectId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IProjectModifierTranslator.class, projectId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasLeader())
        {
            result.setLeader(relations.get(IProjectLeaderTranslator.class, projectId));
            result.getFetchOptions().withLeaderUsing(fetchOptions.withLeader());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(IProjectAttachmentTranslator.class, projectId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IProjectHistoryTranslator.class, projectId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasSpaceHistory())
        {
            result.setSpaceHistory(relations.get(ProjectSpaceRelationshipHistoryTranslator.class, projectId));
            result.getFetchOptions().withSpaceHistoryUsing(fetchOptions.withSpaceHistory());
        }

        if (fetchOptions.hasExperimentsHistory())
        {
            result.setExperimentsHistory(relations.get(ProjectExperimentRelationshipHistoryTranslator.class, projectId));
            result.getFetchOptions().withExperimentsHistoryUsing(fetchOptions.withExperimentsHistory());
        }

        if (fetchOptions.hasSamplesHistory())
        {
            result.setSamplesHistory(relations.get(ProjectSampleRelationshipHistoryTranslator.class, projectId));
            result.getFetchOptions().withSamplesHistoryUsing(fetchOptions.withSamplesHistory());
        }

        if (fetchOptions.hasUnknownHistory())
        {
            result.setUnknownHistory(relations.get(ProjectUnknownRelationshipHistoryTranslator.class, projectId));
            result.getFetchOptions().withUnknownHistoryUsing(fetchOptions.withUnknownHistory());
        }
    }

}
