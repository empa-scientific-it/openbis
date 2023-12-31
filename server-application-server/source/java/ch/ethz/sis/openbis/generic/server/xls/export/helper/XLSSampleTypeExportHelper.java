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

import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.AUTO_GENERATE_CODES;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.CODE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.DESCRIPTION;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.GENERATED_CODE_PREFIX;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.MODIFICATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.UNIQUE_SUBCODES;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VERSION;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;

public class XLSSampleTypeExportHelper extends AbstractXLSEntityTypeExportHelper<SampleType>
{

    public XLSSampleTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public SampleType getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withValidationPlugin().withScript();
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        propertyAssignmentFetchOptions.withPropertyType().withVocabulary();
        propertyAssignmentFetchOptions.withPropertyType().withSampleType();
        propertyAssignmentFetchOptions.withPropertyType().withMaterialType();
        propertyAssignmentFetchOptions.withPlugin().withScript();
        final Map<IEntityTypeId, SampleType> sampleTypes = api.getSampleTypes(sessionToken,
                Collections.singletonList(new EntityTypePermId(permId, EntityKind.SAMPLE)), fetchOptions);

        assert sampleTypes.size() <= 1;

        final Iterator<SampleType> iterator = sampleTypes.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    protected Attribute[] getAttributes(final SampleType sampleType)
    {
        return new Attribute[] { VERSION, CODE, DESCRIPTION, AUTO_GENERATE_CODES, VALIDATION_SCRIPT,
                GENERATED_CODE_PREFIX, UNIQUE_SUBCODES, MODIFICATION_DATE };
    }

    @Override
    protected String getAttributeValue(final SampleType sampleType, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return sampleType.getCode();
            }
            case DESCRIPTION:
            {
                return sampleType.getDescription();
            }
            case VALIDATION_SCRIPT:
            {
                final Plugin validationPlugin = sampleType.getValidationPlugin();
                return validationPlugin != null
                        ? (validationPlugin.getName() != null ? validationPlugin.getName() + ".py" : "") : "";

            }
            case GENERATED_CODE_PREFIX:
            {
                return sampleType.getGeneratedCodePrefix();
            }
            case VERSION:
            {
                return String.valueOf(VersionUtils.getStoredVersion(allVersions, ImportTypes.SAMPLE_TYPE, null, sampleType.getCode()));
            }
            case AUTO_GENERATE_CODES:
            {
                return sampleType.isAutoGeneratedCode().toString().toUpperCase();
            }
            case UNIQUE_SUBCODES:
            {
                return sampleType.isSubcodeUnique().toString().toUpperCase();
            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(sampleType.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    @Override
    protected ExportableKind getExportableKind()
    {
        return ExportableKind.SAMPLE_TYPE;
    }

}
