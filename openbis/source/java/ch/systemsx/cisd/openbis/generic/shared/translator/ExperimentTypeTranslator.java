/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentTypeTranslator
{
    public static ExperimentType translate(final ExperimentTypePE experimentTypePE, Map<PropertyTypePE, PropertyType> cachedOrNull)
    {
        final ExperimentType result = new ExperimentType();
        result.setCode(StringEscapeUtils.escapeHtml(experimentTypePE.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(experimentTypePE.getDescription()));
        result.setExperimentTypePropertyTypes(EntityType
                .sortedInternally(ExperimentTypePropertyTypeTranslator.translate(experimentTypePE
                        .getExperimentTypePropertyTypes(), result, cachedOrNull)));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentTypePE
                .getDatabaseInstance()));
        return result;

    }

    public static List<ExperimentType> translate(final List<ExperimentTypePE> experimentTypes,
            Map<PropertyTypePE, PropertyType> cachedOrNull)
    {
        final List<ExperimentType> result = new ArrayList<ExperimentType>();
        for (final ExperimentTypePE ExperimentTypePE : experimentTypes)
        {
            result.add(ExperimentTypeTranslator.translate(ExperimentTypePE, cachedOrNull));
        }
        return result;
    }

    public static ExperimentTypePE translate(final ExperimentType experimentType)
    {
        final ExperimentTypePE result = new ExperimentTypePE();
        result.setCode(experimentType.getCode());
        return result;
    }

}
