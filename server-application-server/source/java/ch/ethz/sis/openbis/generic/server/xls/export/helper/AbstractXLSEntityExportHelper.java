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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public abstract class AbstractXLSEntityExportHelper<ENTITY extends IPermIdHolder & IPropertiesHolder,
        ENTITY_TYPE extends IEntityType> extends AbstractXLSExportHelper<ENTITY_TYPE>
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
        final Map<String, String> valueFiles = new HashMap<>();

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
            addRow(rowNumber++, true, typeExportableKind, typePermId, warnings, valueFiles, exportableKind.toString());
            addRow(rowNumber++, true, typeExportableKind, typePermId, warnings, valueFiles, entityTypeName);
            addRow(rowNumber++, false, typeExportableKind, typePermId, warnings, valueFiles, typePermId);

            final Attribute[] possibleAttributes = getAttributes(entry.getValue());
            final List<PropertyType> propertyTypes = entry.getKey().getPropertyAssignments().stream().map(PropertyAssignment::getPropertyType)
                    .collect(Collectors.toList());
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(typePermId) ||
                    entityTypeExportFieldsMap.get(typePermId).isEmpty())
            {
                // Export all fields in any order
                // Headers
                final Attribute[] importableAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isImportable)
                        .toArray(Attribute[]::new);
                final Attribute[] defaultPossibleAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isIncludeInDefaultList)
                        .toArray(Attribute[]::new);
                final Attribute[] attributes = compatibleWithImport ? importableAttributes : defaultPossibleAttributes;

                final String[] fieldHeaders = Stream.concat(
                        Arrays.stream(attributes).map(Attribute::getName),
                        propertyTypes.stream().map(PropertyType::getLabel)
                ).toArray(String[]::new);

                addRow(rowNumber++, true, typeExportableKind, typePermId, warnings, valueFiles, fieldHeaders);

                // Values
                for (final ENTITY entity : entry.getValue())
                {
                    final String[] values = Stream.concat(
                            Arrays.stream(attributes).map(attribute -> getAttributeValue(entity, attribute)),
                            propertyTypes.stream().map(getPropertiesMappingFunction(textFormatting, entity.getProperties()))
                    ).toArray(String[]::new);

                    addRow(rowNumber++, true, exportableKind, getIdentifier(entity), warnings, valueFiles, values);
                }
            } else
            {
                // Export selected fields in predefined order
                // Headers
                final Set<Attribute> possibleAttributeNameSet = Stream.of(possibleAttributes)
                        .filter(attribute -> !compatibleWithImport || attribute.isImportable())
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final List<Map<String, String>> selectedExportFields = entityTypeExportFieldsMap.get(typePermId);

                final String[] selectedFieldHeaders = selectedExportFields.stream()
                        .filter(field -> isFieldAcceptable(possibleAttributeNameSet, field))
                        .flatMap(field ->
                        {
                            final String fieldId = field.get(FIELD_ID_KEY);
                            switch (FieldType.valueOf(field.get(FIELD_TYPE_KEY)))
                            {
                                case ATTRIBUTE:
                                {
                                    return Stream.of(Attribute.valueOf(fieldId).getName());
                                }
                                case PROPERTY:
                                {
                                    return propertyTypes.stream()
                                            .filter(propertyType -> Objects.equals(propertyType.getCode(), fieldId))
                                            .findFirst()
                                            .stream()
                                            .map(PropertyType::getLabel);
                                }
                                default:
                                {
                                    throw new IllegalArgumentException();
                                }
                            }
                        }).toArray(String[]::new);
                final Attribute[] requiredForImportAttributes = Arrays.stream(possibleAttributes)
                        .filter(Attribute::isRequiredForImport)
                        .toArray(Attribute[]::new);
                final Set<Attribute> selectedAttributes = selectedExportFields.stream()
                        .filter(map -> map.get(FIELD_TYPE_KEY).equals(FieldType.ATTRIBUTE.toString()))
                        .map(map -> Attribute.valueOf(map.get(FIELD_ID_KEY)))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final Stream<String> requiredForImportAttributeNameStream = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes).filter(attribute -> !selectedAttributes.contains(attribute))
                        .map(Attribute::getName)
                        : Stream.empty();
                final String[] allFieldHeaders = Stream.concat(Arrays.stream(selectedFieldHeaders), requiredForImportAttributeNameStream)
                        .toArray(String[]::new);

                addRow(rowNumber++, true, typeExportableKind, typePermId, warnings, valueFiles, allFieldHeaders);

                // Values
                final Set<Map<String, String>> selectedExportFieldSet = new HashSet<>(selectedExportFields);
                final Map<String, PropertyType> codeToPropertyTypeMap = propertyTypes.stream()
                        .collect(Collectors.toMap(PropertyType::getCode, propertyType -> propertyType, (o1, o2) -> o2));
                final List<Map<String, String>> extraExportFields = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes)
                        .map(attribute -> Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, attribute.toString()))
                        .filter(map -> !selectedExportFieldSet.contains(map))
                        .collect(Collectors.toList())
                        : List.of();

                for (final ENTITY entity : entry.getValue())
                {
                    final String[] entityValues = Stream.concat(selectedExportFields.stream(),
                                    extraExportFields.stream())
                            .filter(field -> isFieldAcceptable(possibleAttributeNameSet, field))
                            .flatMap(field ->
                            {
                                final String fieldId = field.get(FIELD_ID_KEY);
                                switch (FieldType.valueOf(field.get(FIELD_TYPE_KEY)))
                                {
                                    case ATTRIBUTE:
                                    {
                                        return Stream.of(getAttributeValue(entity, Attribute.valueOf(fieldId)));
                                    }
                                    case PROPERTY:
                                    {
                                        final PropertyType propertyType = codeToPropertyTypeMap.get(fieldId);
                                        return propertyType != null
                                                ? Stream.of(getPropertiesMappingFunction(textFormatting, entity.getProperties()).apply(propertyType))
                                                : Stream.of();
                                    }
                                    default:
                                    {
                                        throw new IllegalArgumentException();
                                    }
                                }
                            }).toArray(String[]::new);

                    addRow(rowNumber++, false, exportableKind, getIdentifier(entity), warnings, valueFiles, entityValues);
                }
            }

            rowNumber++;
        }

        return new AdditionResult(rowNumber, warnings, valueFiles);
    }

    protected abstract ExportableKind getExportableKind();

    protected abstract ExportableKind getTypeExportableKind();

    protected abstract String getEntityTypeName();

    protected abstract String getIdentifier(final ENTITY entity);

    protected abstract Function<ENTITY, ENTITY_TYPE> getTypeFunction();

    protected abstract Attribute[] getAttributes(final Collection<ENTITY> entities);

    protected abstract String getAttributeValue(final ENTITY entity, final Attribute attribute);

    protected abstract Collection<ENTITY> getEntities(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds);

    protected abstract String typePermIdToString(final ENTITY_TYPE entityType);

}
