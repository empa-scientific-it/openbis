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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.ObjectToOneRelationTranslator;

/**
 * @author pkupczyk
 */
public abstract class ObjectToProjectTranslator extends ObjectToOneRelationTranslator<Project, ProjectFetchOptions> implements
        IObjectToProjectTranslator
{

    @Autowired
    private IProjectTranslator projectTranslator;

    @Override
    protected Map<Long, Project> translateRelated(TranslationContext context, Collection<Long> relatedIds, ProjectFetchOptions relatedFetchOptions)
    {
        return projectTranslator.translate(context, relatedIds, relatedFetchOptions);
    }

}
