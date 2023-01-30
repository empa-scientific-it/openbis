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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.tag;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class TagTranslator extends AbstractCachingTranslator<Long, Tag, TagFetchOptions> implements ITagTranslator
{

    @Autowired
    private ITagAuthorizationValidator authorizationValidator;

    @Autowired
    private ITagBaseTranslator baseTranslator;

    @Autowired
    private ITagOwnerTranslator ownerTranslator;

    @Autowired
    private ITagExperimentTranslator experimentTranslator;

    @Autowired
    private ITagSampleTranslator sampleTranslator;

    @Autowired
    private ITagDataSetTranslator dataSetTranslator;

    @Autowired
    private ITagMaterialTranslator materialTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> tagIds, TagFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), tagIds);
    }

    @Override
    protected Tag createObject(TranslationContext context, Long tagId, TagFetchOptions fetchOptions)
    {
        Tag result = new Tag();
        result.setFetchOptions(new TagFetchOptions());
        return result;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> tagIds, TagFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ITagBaseTranslator.class, baseTranslator.translate(context, tagIds, null));

        if (fetchOptions.hasOwner())
        {
            relations.put(ITagOwnerTranslator.class, ownerTranslator.translate(context, tagIds, fetchOptions.withOwner()));
        }

        if (fetchOptions.hasExperiments())
        {
            relations.put(ITagExperimentTranslator.class, experimentTranslator.translate(context, tagIds, fetchOptions.withExperiments()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(ITagSampleTranslator.class, sampleTranslator.translate(context, tagIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasDataSets())
        {
            relations.put(ITagDataSetTranslator.class, dataSetTranslator.translate(context, tagIds, fetchOptions.withDataSets()));
        }

        if (fetchOptions.hasMaterials())
        {
            relations.put(ITagMaterialTranslator.class, materialTranslator.translate(context, tagIds, fetchOptions.withMaterials()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long tagId, Tag result, Object objectRelations, TagFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        TagBaseRecord baseRecord = relations.get(ITagBaseTranslator.class, tagId);

        result.setPermId(new TagPermId(baseRecord.owner, baseRecord.name));
        result.setCode(baseRecord.name);
        result.setDescription(baseRecord.description);
        result.setPrivate(baseRecord.isPrivate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasOwner())
        {
            result.setOwner(relations.get(ITagOwnerTranslator.class, tagId));
            result.getFetchOptions().withOwnerUsing(fetchOptions.withOwner());
        }

        if (fetchOptions.hasExperiments())
        {
            result.setExperiments((List<Experiment>) relations.get(ITagExperimentTranslator.class, tagId));
            result.getFetchOptions().withExperimentsUsing(fetchOptions.withExperiments());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples(SampleUtils.extractSamples(relations.get(ITagSampleTranslator.class, tagId)));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasDataSets())
        {
            result.setDataSets((List<DataSet>) relations.get(ITagDataSetTranslator.class, tagId));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasMaterials())
        {
            result.setMaterials((List<Material>) relations.get(ITagMaterialTranslator.class, tagId));
            result.getFetchOptions().withMaterialsUsing(fetchOptions.withMaterials());
        }
    }

}
