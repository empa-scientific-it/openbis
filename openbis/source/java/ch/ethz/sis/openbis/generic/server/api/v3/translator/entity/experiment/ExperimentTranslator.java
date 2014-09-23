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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment;

import java.util.ArrayList;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.SetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.AttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.ProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author pkupczyk
 */
public class ExperimentTranslator extends AbstractCachingTranslator<ExperimentPE, Experiment, ExperimentFetchOptions>
{

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public ExperimentTranslator(TranslationContext translationContext, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            ExperimentFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    protected boolean shouldTranslate(ExperimentPE input)
    {
        return new ExperimentByIdentiferValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Experiment createObject(ExperimentPE experiment)
    {
        Experiment result = new Experiment();

        result.setCode(experiment.getCode());
        result.setPermId(new ExperimentPermId(experiment.getPermId()));
        result.setIdentifier(new ExperimentIdentifier(experiment.getIdentifier()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setModificationDate(experiment.getModificationDate());
        result.setFetchOptions(new ExperimentFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(ExperimentPE experiment, Experiment result, Relations relations)
    {
        if (getFetchOptions().hasType())
        {
            ExperimentType type =
                    new ExperimentTypeTranslator(getTranslationContext(), getFetchOptions().fetchType()).translate(experiment.getExperimentType());
            result.setType(type);
            result.getFetchOptions().fetchType(getFetchOptions().fetchType());
        }

        if (getFetchOptions().hasProperties())
        {
            Map<String, String> properties =
                    new PropertyTranslator(getTranslationContext(), managedPropertyEvaluatorFactory, getFetchOptions().fetchProperties())
                            .translate(experiment);
            result.setProperties(properties);
            result.getFetchOptions().fetchProperties(getFetchOptions().fetchProperties());
        }

        if (getFetchOptions().hasProject())
        {
            result.setProject(new ProjectTranslator(getTranslationContext(), getFetchOptions().fetchProject()).translate(experiment.getProject()));
            result.getFetchOptions().fetchProject(getFetchOptions().fetchProject());
        }

        if (getFetchOptions().hasRegistrator())
        {
            result.setRegistrator(new PersonTranslator(getTranslationContext(), getFetchOptions().fetchRegistrator()).translate(experiment
                    .getRegistrator()));
            result.getFetchOptions().fetchRegistrator(getFetchOptions().fetchRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            result.setModifier(new PersonTranslator(getTranslationContext(), getFetchOptions().fetchModifier()).translate(experiment
                    .getModifier()));
            result.getFetchOptions().fetchModifier(getFetchOptions().fetchModifier());
        }

        if (getFetchOptions().hasTags())
        {
            result.setTags(new SetTranslator().translate(experiment.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                    .fetchTags())));
            result.getFetchOptions().fetchTags(getFetchOptions().fetchTags());
        }

        if (getFetchOptions().hasAttachments())
        {
            ArrayList<Attachment> attachments =
                    AttachmentTranslator.translate(getTranslationContext(), experiment, getFetchOptions().fetchAttachments());
            result.setAttachments(attachments);
            result.getFetchOptions().fetchAttachments(getFetchOptions().fetchAttachments());
        }

    }
}
