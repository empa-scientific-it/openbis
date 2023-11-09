/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;

public class XLSExperimentTypeExportHelper extends AbstractXLSEntityTypeExportHelper<ExperimentType>
{

    public XLSExperimentTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    protected Attribute[] getAttributes(final ExperimentType entityType)
    {
        return new Attribute[] { CODE, DESCRIPTION, VALIDATION_SCRIPT, MODIFICATION_DATE };
    }

    @Override
    protected String getAttributeValue(final ExperimentType experimentType, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return experimentType.getCode();
            }
            case DESCRIPTION:
            {
                return experimentType.getDescription();
            }
            case VALIDATION_SCRIPT:
            {
                final Plugin validationPlugin = experimentType.getValidationPlugin();
                return validationPlugin != null
                        ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(experimentType.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    public ExperimentType getEntityType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        final ExperimentTypeFetchOptions fetchOptions = new ExperimentTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, ExperimentType> experimentTypes = api.getExperimentTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.EXPERIMENT)), fetchOptions);

        assert experimentTypes.size() <= 1;

        final Iterator<ExperimentType> iterator = experimentTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.EXPERIMENT_TYPE;
    }
}
