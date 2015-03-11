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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.ExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.SpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ProjectTranslator extends AbstractCachingTranslator<ProjectPE, Project, ProjectFetchOptions>
{
    public ProjectTranslator(TranslationContext translationContext, ProjectFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected boolean shouldTranslate(ProjectPE input)
    {
        return new ProjectByIdentiferValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Project createObject(ProjectPE project)
    {
        Project result = new Project();

        result.setCode(project.getCode());
        result.setPermId(new ProjectPermId(project.getPermId()));
        result.setIdentifier(new ProjectIdentifier(project.getIdentifier()));
        result.setDescription(project.getDescription());
        result.setRegistrationDate(project.getRegistrationDate());
        result.setModificationDate(project.getModificationDate());
        result.setFetchOptions(new ProjectFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(ProjectPE project, Project result, Relations relations)
    {
        if (getFetchOptions().hasSpace())
        {
            result.setSpace(new SpaceTranslator(getTranslationContext(), getFetchOptions().withSpace())
                    .translate(project.getSpace()));
            result.getFetchOptions().withSpaceUsing(getFetchOptions().withSpace());
        }

        if (getFetchOptions().hasRegistrator())
        {
            result.setRegistrator(new PersonTranslator(getTranslationContext(), getFetchOptions().withRegistrator()).translate(project
                    .getRegistrator()));
            result.getFetchOptions().withRegistratorUsing(getFetchOptions().withRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            result.setModifier(new PersonTranslator(getTranslationContext(), getFetchOptions().withModifier()).translate(project
                    .getModifier()));
            result.getFetchOptions().withModifierUsing(getFetchOptions().withModifier());
        }

        if (getFetchOptions().hasExperiments())
        {
            List<Experiment> experiments =
                    new ListTranslator().translate(project.getExperiments(), new ExperimentTranslator(getTranslationContext(),
                            getFetchOptions().withExperiments()));
            result.setExperiments(experiments);
            result.getFetchOptions().withExperimentsUsing(getFetchOptions().withExperiments());
        }
    }
}
