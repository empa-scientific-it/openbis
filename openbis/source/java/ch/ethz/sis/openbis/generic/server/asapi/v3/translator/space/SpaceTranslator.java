/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpaceTechId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class SpaceTranslator extends AbstractCachingTranslator<Long, Space, SpaceFetchOptions> implements ISpaceTranslator
{

    @Autowired
    private ISpaceAuthorizationValidator authorizationValidator;

    @Autowired
    private ISpaceRegistratorTranslator registratorTranslator;

    @Autowired
    private ISpaceProjectTranslator projectTranslator;

    @Autowired
    private ISpaceSampleTranslator sampleTranslator;

    @Autowired
    private ISpaceBaseTranslator baseTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> spaceIds, SpaceFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), spaceIds);
    }

    @Override
    protected Space createObject(TranslationContext context, Long spaceId, SpaceFetchOptions fetchOptions)
    {
        Space space = new Space();
        space.setFetchOptions(new SpaceFetchOptions());
        return space;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> spaceIds, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISpaceBaseTranslator.class, baseTranslator.translate(context, spaceIds, null));

        if (fetchOptions.hasProjects())
        {
            relations.put(ISpaceProjectTranslator.class, projectTranslator.translate(context, spaceIds, fetchOptions.withProjects()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(ISpaceSampleTranslator.class, sampleTranslator.translate(context, spaceIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(ISpaceRegistratorTranslator.class, registratorTranslator.translate(context, spaceIds, fetchOptions.withRegistrator()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long spaceId, Space result, Object objectRelations, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SpaceBaseRecord baseRecord = relations.get(ISpaceBaseTranslator.class, spaceId);

        result.setId(new SpaceTechId(baseRecord.id));
        result.setCode(baseRecord.code);
        result.setPermId(new SpacePermId(baseRecord.code));
        result.setDescription(baseRecord.description);
        result.setFrozen(baseRecord.frozen);
        result.setFrozenForProjects(baseRecord.frozenForProjects);
        result.setFrozenForSamples(baseRecord.frozenForSamples);
        // TODO: add modification date to spaces table
        result.setModificationDate(baseRecord.registrationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasProjects())
        {
            result.setProjects((List<Project>) relations.get(ISpaceProjectTranslator.class, spaceId));
            result.getFetchOptions().withProjectsUsing(fetchOptions.withProjects());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples(SampleUtils.extractSamples(relations.get(ISpaceSampleTranslator.class, spaceId)));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(ISpaceRegistratorTranslator.class, spaceId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

    }

}
