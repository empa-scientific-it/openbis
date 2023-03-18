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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public abstract class AbstractXLSEntityExportHelper<ENTITY extends IPermIdHolder & IPropertiesHolder,
        ENTITY_TYPE extends IPermIdHolder & IPropertyAssignmentsHolder> extends AbstractXLSExportHelper
{

    public AbstractXLSEntityExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        final Collection<ENTITY> entities = getEntities(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        // Sorting after grouping is needed only to make sure that the tests pass, because entrySet() can have elements
        // in arbitrary order.
        final Collection<Map.Entry<ENTITY_TYPE, List<ENTITY>>> groupedEntities =
                entities.stream().collect(Collectors.groupingBy(getTypeFunction())).entrySet().stream()
                        .sorted(Comparator.comparing(entry -> entry.getKey().getPermId().toString()))
                        .collect(Collectors.toList());

        final ExportableKind exportableKind = getExportableKind();
        final String entityTypeName = getEntityTypeName();
        for (final Map.Entry<ENTITY_TYPE, List<ENTITY>> entry : groupedEntities)
        {
            final String typePermId = typePermIdToString(entry.getKey());

            final ExportableKind typeExportableKind = getTypeExportableKind();
            warnings.addAll(addRow(rowNumber++, true, typeExportableKind, typePermId, exportableKind.toString()));
            warnings.addAll(addRow(rowNumber++, true, typeExportableKind, typePermId, entityTypeName));
            warnings.addAll(addRow(rowNumber++, false, typeExportableKind, typePermId, typePermId));

            final String[] attributeNames = getAttributeNames(entry.getValue().get(0));
            final String[] importAttributeNames = compatibleWithImport ? getImportAttributeNames() : new String[0];
            final Set<String> attributeNameSet = Stream.concat(Stream.of(attributeNames), Stream.of(importAttributeNames))
                    .collect(Collectors.toSet());
            final List<PropertyAssignment> propertyAssignments = entry.getKey().getPropertyAssignments();
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(typePermId) ||
                    entityTypeExportFieldsMap.get(typePermId).isEmpty())
            {
                // Export all fields in any order
                final String[] fieldNames = Stream.concat(Stream.concat(
                        Arrays.stream(attributeNames),
                        propertyAssignments.stream()
                                .map(PropertyAssignment::getPropertyType)
                                .map(PropertyType::getLabel)
                ), Arrays.stream(importAttributeNames)).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, typeExportableKind, typePermId, fieldNames));

                for (final ENTITY entity : entry.getValue())
                {
                    final Stream<String> importAttributeValuesStream = compatibleWithImport
                            ? getImportAttributeValuesStream() : Stream.empty();
                    final List<String> entityValues = Stream.concat(Stream.concat(
                            getAttributeValuesStream(entity),
                            propertyAssignments.stream()
                                    .map(PropertyAssignment::getPropertyType)
                                    .map(getPropertiesMappingFunction(textFormatting, entity.getProperties()))
                    ), importAttributeValuesStream).collect(Collectors.toList());

                    warnings.addAll(addRow(rowNumber++, false, exportableKind, getIdentifier(entity),
                            entityValues.toArray(String[]::new)));
                }
            } else
            {
                // Export selected fields in predefined order
                final List<Map<String, String>> selectedExportFields = entityTypeExportFieldsMap.get(typePermId);
                final List<Map<String, String>> extraExportFieldStream = Arrays.stream(importAttributeNames)
                        .map(fieldName -> Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(),
                                FIELD_ID_KEY, fieldName)).collect(Collectors.toList());

                final String[] selectedFieldNames = Stream.concat(selectedExportFields.stream(),
                                extraExportFieldStream.stream())
                        .filter(field -> isFieldAcceptable(attributeNameSet, field))
                        .map(field ->
                        {
                            final String fieldId = field.get(FIELD_ID_KEY);
                            switch (FieldType.valueOf(field.get(FIELD_TYPE_KEY)))
                            {
                                case ATTRIBUTE:
                                {
                                    return fieldId;
                                }
                                case PROPERTY:
                                {
                                    return propertyAssignments.stream()
                                            .filter(propertyAssignment -> Objects.equals(
                                                    propertyAssignment.getPropertyType().getCode(), fieldId))
                                            .findFirst()
                                            .orElseThrow()
                                            .getPropertyType()
                                            .getLabel();
                                }
                                default:
                                {
                                    throw new IllegalArgumentException();
                                }
                            }
                        }).toArray(String[]::new);
                final Set<String> selectedFieldNameSet = Set.of(selectedFieldNames);
                final Stream<String> importAttributeNamesStream = Arrays.stream(importAttributeNames)
                        .filter(attributeName -> !selectedFieldNameSet.contains(attributeName));

                final String[] allFieldNames = Stream.concat(Arrays.stream(selectedFieldNames), importAttributeNamesStream)
                        .toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, typeExportableKind, typePermId, allFieldNames));

                final Map<String, PropertyType> codeToPropertyTypeMap = propertyAssignments.stream()
                        .map(PropertyAssignment::getPropertyType)
                        .collect(Collectors.toMap(PropertyType::getCode, propertyType -> propertyType, (o1, o2) -> o2));

                for (final ENTITY entity : entry.getValue())
                {
                    final String[] entityValues = Stream.concat(selectedExportFields.stream(),
                                    extraExportFieldStream.stream())
                            .filter(field -> isFieldAcceptable(attributeNameSet, field))
                            .map(field ->
                            {
                                switch (FieldType.valueOf(field.get(FIELD_TYPE_KEY)))
                                {
                                    case ATTRIBUTE:
                                    {
                                        return getAttributeValue(entity, field.get(FIELD_ID_KEY));
                                    }
                                    case PROPERTY:
                                    {
                                        return getPropertiesMappingFunction(textFormatting, entity.getProperties())
                                                .apply(codeToPropertyTypeMap.get(field.get(FIELD_ID_KEY)));
                                    }
                                    default:
                                    {
                                        throw new IllegalArgumentException();
                                    }
                                }
                            }).toArray(String[]::new);

                    warnings.addAll(addRow(rowNumber++, false, exportableKind, getIdentifier(entity), entityValues));
                }
            }

            rowNumber++;
        }

        return new AdditionResult(rowNumber, warnings);
    }

    private static boolean isFieldAcceptable(final Set<String> attributeNameSet, final Map<String, String> field)
    {
        return FieldType.valueOf(field.get(FIELD_TYPE_KEY)) != FieldType.ATTRIBUTE || attributeNameSet.contains(field.get(FIELD_ID_KEY));
    }

    protected abstract ExportableKind getExportableKind();

    protected abstract ExportableKind getTypeExportableKind();

    protected abstract String getEntityTypeName();

    protected abstract String getIdentifier(final ENTITY entity);

    protected abstract Function<ENTITY, ENTITY_TYPE> getTypeFunction();

    protected abstract String[] getAttributeNames(final ENTITY entity);

    protected String[] getImportAttributeNames()
    {
        return new String[0];
    }

    protected abstract String getAttributeValue(final ENTITY entity, final String attributeId);

    protected abstract Stream<String> getAttributeValuesStream(final ENTITY entity);

    protected  Stream<String> getImportAttributeValuesStream()
    {
        return Stream.empty();
    }

    protected abstract Collection<ENTITY> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds);

    protected abstract String typePermIdToString(final ENTITY_TYPE entityType);

}
