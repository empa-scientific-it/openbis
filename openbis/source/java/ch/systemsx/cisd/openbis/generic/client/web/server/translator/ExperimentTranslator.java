/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Experiment} &lt;---&gt; {@link ExperimentPE} translator.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentTranslator
{

    public enum LoadableFields
    {
        ATTACHMENTS, PROPERTIES
    }

    private ExperimentTranslator()
    {
        // Can not be instantiated.
    }

    public final static Experiment translate(final ExperimentPE experiment, String baseIndexURL,
            final LoadableFields... withFields)
    {
        if (experiment == null)
        {
            return null;
        }
        final Experiment result = new Experiment();
        result.setId(HibernateUtils.getId(experiment));
        result.setModificationDate(experiment.getModificationDate());
        result.setCode(StringEscapeUtils.escapeHtml(experiment.getCode()));
        result.setPermId(StringEscapeUtils.escapeHtml(experiment.getPermId()));
        result.setPermlink(PermlinkUtilities.createPermlinkURL(baseIndexURL, EntityKind.EXPERIMENT,
                experiment.getPermId()));
        result.setExperimentType(translate(experiment.getExperimentType()));
        result.setIdentifier(StringEscapeUtils.escapeHtml(experiment.getIdentifier()));
        result.setProject(ProjectTranslator.translate(experiment.getProject()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(experiment.getRegistrator()));
        result.setInvalidation(InvalidationTranslator.translate(experiment.getInvalidation()));
        for (final LoadableFields field : withFields)
        {
            switch (field)
            {
                case PROPERTIES:
                    result.setProperties(ExperimentPropertyTranslator.translate(experiment
                            .getProperties()));
                    break;
                case ATTACHMENTS:
                    result.setAttachments(AttachmentTranslator.translate(experiment
                            .getAttachments()));
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public final static ExperimentType translate(final ExperimentTypePE experimentType)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(StringEscapeUtils.escapeHtml(experimentType.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(experimentType.getDescription()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                experimentType.getExperimentTypePropertyTypes(), result));
        return result;
    }

    public final static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(StringEscapeUtils.escapeHtml(experimentType.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(experimentType.getDescription()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        return result;
    }

}
