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
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VALIDATION_SCRIPT;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.VERSION;
import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSEntityExportHelper.isFieldAcceptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSampleTypeExportHelper extends AbstractXLSExportHelper
{

    public XLSSampleTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        assert permIds.size() == 1;
        final SampleType sampleType = getSampleType(api, sessionToken, permIds.iterator().next());
        final Collection<String> warnings = new ArrayList<>();

        if (sampleType != null)
        {
            final String permId = sampleType.getPermId().getPermId();
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, "SAMPLE_TYPE"));

            final Attribute[] possibleAttributes = getAttributes(sampleType);
            final Attribute[] importableAttributes = Arrays.stream(possibleAttributes)
                    .filter(Attribute::isImportable)
                    .toArray(Attribute[]::new);
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(ExportableKind.SAMPLE_TYPE.toString()) ||
                    entityTypeExportFieldsMap.get(ExportableKind.SAMPLE_TYPE.toString()).isEmpty())
            {
                // Export all fields in any order
                // Names
                final Attribute[] attributes = compatibleWithImport ? importableAttributes : possibleAttributes;
                final String[] attributeHeaders = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, attributeHeaders));

                // Values
                final String[] values = Arrays.stream(attributes)
                        .map(attribute -> getAttributeValue(sampleType, attribute)).toArray(String[]::new);
                warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE_TYPE, permId, values));
            } else
            {
                // Export selected fields in predefined order
                // Names
                final Set<Attribute> attributeNameSet = Stream.of(possibleAttributes)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final List<Map<String, String>> selectedExportFields = entityTypeExportFieldsMap.get(ExportableKind.SAMPLE_TYPE.toString());

                final String[] selectedFieldNames = selectedExportFields.stream()
                        .filter(field -> AbstractXLSEntityExportHelper.isFieldAcceptable(attributeNameSet, field))
                        .map(field ->
                        {
                            if (FieldType.valueOf(field.get(FIELD_TYPE_KEY)) == FieldType.ATTRIBUTE)
                            {
                                return Attribute.valueOf(field.get(FIELD_ID_KEY)).getName();
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);
                final Attribute[] requiredAttributes = Arrays.stream(possibleAttributes)
                        .filter(Attribute::isRequiredForImport)
                        .toArray(Attribute[]::new);
                final Set<Attribute> selectedAttributes = selectedExportFields.stream()
                        .filter(map -> map.get(FIELD_TYPE_KEY).equals(FieldType.ATTRIBUTE.toString()))
                        .map(map -> Attribute.valueOf(map.get(FIELD_ID_KEY)))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final Stream<String> importAttributeNamesStream = Arrays.stream(requiredAttributes)
                        .filter(attribute -> !selectedAttributes.contains(attribute))
                        .map(Attribute::getName);
                final String[] allFieldNames = Stream.concat(Arrays.stream(selectedFieldNames), importAttributeNamesStream)
                        .toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, allFieldNames));

                // Values
                final Set<Map<String, String>> selectedExportFieldSet = new HashSet<>(selectedExportFields);
                final List<Map<String, String>> extraExportFields = Arrays.stream(requiredAttributes)
                        .map(attribute -> Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(),
                                FIELD_ID_KEY, attribute.toString()))
                        .filter(map -> !selectedExportFieldSet.contains(map))
                        .collect(Collectors.toList());
                final String[] entityValues = Stream.concat(selectedExportFields.stream(),
                                extraExportFields.stream())
                        .filter(field -> isFieldAcceptable(attributeNameSet, field))
                        .map(field ->
                        {
                            if (FieldType.valueOf(field.get(FIELD_TYPE_KEY)) == FieldType.ATTRIBUTE)
                            {
                                return getAttributeValue(sampleType, Attribute.valueOf(field.get(FIELD_ID_KEY)));
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE_TYPE, permId, entityValues));
            }

            final AdditionResult additionResult = addEntityTypePropertyAssignments(rowNumber,
                    sampleType.getPropertyAssignments(), ExportableKind.SAMPLE_TYPE, permId,
                    entityTypeExportFieldsMap, compatibleWithImport);
            warnings.addAll(additionResult.getWarnings());
            rowNumber = additionResult.getRowNumber();

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    private SampleType getSampleType(final IApplicationServerApi api, final String sessionToken, final String permId)
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

    public IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken,
            final String permId)
    {
        return getSampleType(api, sessionToken, permId);
    }

    protected Attribute[] getAttributes(final SampleType sampleType)
    {
        return new Attribute[] { VERSION, CODE, DESCRIPTION, AUTO_GENERATE_CODES, VALIDATION_SCRIPT,
                GENERATED_CODE_PREFIX };
    }

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
                // TODO: implement
                return "1";
            }
            case AUTO_GENERATE_CODES:
            {
                return sampleType.isAutoGeneratedCode().toString().toUpperCase();
            }
            default:
            {
                return null;
            }
        }
    }

}
