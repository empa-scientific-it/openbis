/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import static ch.ethz.sis.openbis.generic.server.xls.export.helper.AbstractXLSEntityExportHelper.isFieldAcceptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public abstract class AbstractXLSEntityTypeHelper<ENTITY_TYPE extends IEntityType> extends AbstractXLSExportHelper<ENTITY_TYPE>
{
    public AbstractXLSEntityTypeHelper(final Workbook wb)
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
        final ENTITY_TYPE entityType = getEntityType(api, sessionToken, permIds.iterator().next());
        final Collection<String> warnings = new ArrayList<>();

        if (entityType != null)
        {
            final String permId = entityType.getPermId().toString();
            warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, "SAMPLE_TYPE"));

            final Attribute[] possibleAttributes = getAttributes(entityType);
            final Attribute[] importableAttributes = Arrays.stream(possibleAttributes)
                    .filter(Attribute::isImportable)
                    .toArray(Attribute[]::new);
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(ExportableKind.SAMPLE_TYPE.toString()) ||
                    entityTypeExportFieldsMap.get(ExportableKind.SAMPLE_TYPE.toString()).isEmpty())
            {
                // Export all attributes in any order
                // Names
                final Attribute[] attributes = compatibleWithImport ? importableAttributes : possibleAttributes;
                final String[] attributeHeaders = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, attributeHeaders));

                // Values
                final String[] values = Arrays.stream(attributes)
                        .map(attribute -> getAttributeValue(entityType, attribute)).toArray(String[]::new);
                warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE_TYPE, permId, values));
            } else
            {
                // Export selected attributes in predefined order
                // Names
                final Set<Attribute> possibleAttributeNameSet = Stream.of(possibleAttributes)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final List<Map<String, String>> selectedExportAttributes = entityTypeExportFieldsMap.get(ExportableKind.SAMPLE_TYPE.toString());

                final String[] selectedFieldNames = selectedExportAttributes.stream()
                        .filter(field -> AbstractXLSEntityExportHelper.isFieldAcceptable(possibleAttributeNameSet, field))
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
                final Attribute[] requiredForImportAttributes = Arrays.stream(possibleAttributes)
                        .filter(Attribute::isRequiredForImport)
                        .toArray(Attribute[]::new);
                final Set<Attribute> selectedAttributes = selectedExportAttributes.stream()
                        .filter(map -> map.get(FIELD_TYPE_KEY).equals(FieldType.ATTRIBUTE.toString()))
                        .map(map -> Attribute.valueOf(map.get(FIELD_ID_KEY)))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final Stream<String> requiredForImportAttributeNamesStream = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes)
                        .filter(attribute -> !selectedAttributes.contains(attribute))
                        .map(Attribute::getName)
                        : Stream.empty();
                final String[] allAttributeNames = Stream.concat(Arrays.stream(selectedFieldNames), requiredForImportAttributeNamesStream)
                        .toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, ExportableKind.SAMPLE_TYPE, permId, allAttributeNames));

                // Values
                final Set<Map<String, String>> selectedExportFieldSet = new HashSet<>(selectedExportAttributes);
                final List<Map<String, String>> extraExportFields = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes)
                        .map(attribute -> Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, attribute.toString()))
                        .filter(map -> !selectedExportFieldSet.contains(map))
                        .collect(Collectors.toList())
                        : List.of();
                final String[] entityValues = Stream.concat(selectedExportAttributes.stream(), extraExportFields.stream())
                        .filter(field -> isFieldAcceptable(possibleAttributeNameSet, field))
                        .map(field ->
                        {
                            if (FieldType.valueOf(field.get(FIELD_TYPE_KEY)) == FieldType.ATTRIBUTE)
                            {
                                return getAttributeValue(entityType, Attribute.valueOf(field.get(FIELD_ID_KEY)));
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, false, ExportableKind.SAMPLE_TYPE, permId, entityValues));
            }

            final AdditionResult additionResult = addEntityTypePropertyAssignments(rowNumber,
                    entityType.getPropertyAssignments(), ExportableKind.SAMPLE_TYPE, permId,
                    entityTypeExportFieldsMap, compatibleWithImport);
            warnings.addAll(additionResult.getWarnings());
            rowNumber = additionResult.getRowNumber();

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    protected abstract Attribute[] getAttributes(final ENTITY_TYPE entityType);

    protected abstract String getAttributeValue(final ENTITY_TYPE entityType, final Attribute attribute);

}
