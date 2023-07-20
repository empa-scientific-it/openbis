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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.VersionUtils;

public abstract class AbstractXLSEntityTypeExportHelper<ENTITY_TYPE extends IEntityType> extends AbstractXLSExportHelper<ENTITY_TYPE>
{

    protected static Map<String, Integer> allVersions = VersionUtils.loadAllVersions();

    public AbstractXLSEntityTypeExportHelper(final Workbook wb)
    {
        super(wb);
    }

    public static void setAllVersions(final Map<String, Integer> allVersions)
    {
        AbstractXLSEntityTypeExportHelper.allVersions = allVersions;
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        if (permIds.size() != 1)
        {
            throw new IllegalArgumentException("For entity type export number of permIds should be equal to 1.");
        }
        final ENTITY_TYPE entityType = getEntityType(api, sessionToken, permIds.get(0));
        final Collection<String> warnings = new ArrayList<>();

        if (entityType != null)
        {
            final String permId = entityType.getPermId().toString();
            final ExportableKind exportableKind = getExportableKind();
            warnings.addAll(addRow(rowNumber++, true, exportableKind, permId, exportableKind.name()));

            final Attribute[] possibleAttributes = getAttributes(entityType);
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(exportableKind.toString()) ||
                    entityTypeExportFieldsMap.get(exportableKind.toString()).isEmpty())
            {
                // Export all attributes in any order
                // Headers
                final Attribute[] importableAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isImportable)
                        .toArray(Attribute[]::new);
                final Attribute[] defaultPossibleAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isIncludeInDefaultList)
                        .toArray(Attribute[]::new);
                final Attribute[] attributes = compatibleWithImport ? importableAttributes : defaultPossibleAttributes;
                final String[] attributeHeaders = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, exportableKind, permId, attributeHeaders));

                // Values
                final String[] values = Arrays.stream(attributes).map(attribute -> getAttributeValue(entityType, attribute)).toArray(String[]::new);
                warnings.addAll(addRow(rowNumber++, false, exportableKind, permId, values));
            } else
            {
                // Export selected attributes in predefined order
                // Headers
                final Set<Attribute> possibleAttributeNameSet = Stream.of(possibleAttributes)
                        .filter(attribute -> !compatibleWithImport || attribute.isImportable())
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final List<Map<String, String>> selectedExportAttributes = entityTypeExportFieldsMap.get(exportableKind.toString());

                final String[] selectedAttributeHeaders = selectedExportAttributes.stream()
                        .filter(attribute -> AbstractXLSExportHelper.isFieldAcceptable(possibleAttributeNameSet, attribute))
                        .map(attribute ->
                        {
                            if (FieldType.valueOf(attribute.get(FIELD_TYPE_KEY)) == FieldType.ATTRIBUTE)
                            {
                                return Attribute.valueOf(attribute.get(FIELD_ID_KEY)).getName();
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
                final Stream<String> requiredForImportAttributeNameStream = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes)
                        .filter(attribute -> !selectedAttributes.contains(attribute))
                        .map(Attribute::getName)
                        : Stream.empty();
                final String[] allAttributeNames = Stream.concat(Arrays.stream(selectedAttributeHeaders), requiredForImportAttributeNameStream)
                        .toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, exportableKind, permId, allAttributeNames));

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

                warnings.addAll(addRow(rowNumber++, false, exportableKind, permId, entityValues));
            }

            final AdditionResult additionResult = addEntityTypePropertyAssignments(rowNumber,
                    entityType.getPropertyAssignments(), exportableKind, permId,
                    compatibleWithImport);
            warnings.addAll(additionResult.getWarnings());
            rowNumber = additionResult.getRowNumber();

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    protected AdditionResult addEntityTypePropertyAssignments(int rowNumber,
            final Collection<PropertyAssignment> propertyAssignments, final ExportableKind exportableKind,
            final String permId, final boolean compatibleWithImport)
    {
        final Collection<String> warnings = new ArrayList<>(
                addRow(rowNumber++, true, exportableKind, permId, compatibleWithImport
                        ? ENTITY_ASSIGNMENT_COLUMNS
                        : Arrays.copyOfRange(ENTITY_ASSIGNMENT_COLUMNS, 1, ENTITY_ASSIGNMENT_COLUMNS.length)));
        for (final PropertyAssignment propertyAssignment : propertyAssignments)
        {
            final PropertyType propertyType = propertyAssignment.getPropertyType();
            final Plugin plugin = propertyAssignment.getPlugin();
            final Vocabulary vocabulary = propertyType.getVocabulary();

            final String code = propertyType.getCode();
            final String[] values = {
                    String.valueOf(VersionUtils.getStoredVersion(allVersions, ImportTypes.PROPERTY_TYPE, null, code)),
                    code,
                    String.valueOf(propertyAssignment.isMandatory()).toUpperCase(),
                    String.valueOf(propertyAssignment.isShowInEditView()).toUpperCase(),
                    propertyAssignment.getSection(),
                    propertyType.getLabel(),
                    getFullDataTypeString(propertyType),
                    String.valueOf(vocabulary != null ? vocabulary.getCode() : ""),
                    propertyType.getDescription(),
                    mapToJSON(propertyType.getMetaData()),
                    plugin != null ? (plugin.getName() != null ? plugin.getName() + ".py" : "") : "",
                    String.valueOf(propertyType.isMultiValue() != null && propertyType.isMultiValue()).toUpperCase() };
            warnings.addAll(addRow(rowNumber++, false, exportableKind, permId,
                    compatibleWithImport ? values : Arrays.copyOfRange(values, 1, values.length)));
        }
        return new AdditionResult(rowNumber, warnings);
    }

    private String getFullDataTypeString(final PropertyType propertyType)
    {
        final String dataTypeString = String.valueOf(propertyType.getDataType());
        switch (propertyType.getDataType())
        {
            case SAMPLE:
            {
                return dataTypeString +
                        ((propertyType.getSampleType() != null) ? ':' + propertyType.getSampleType().getCode() : "");
            }
            case MATERIAL:
            {
                return dataTypeString +
                        ((propertyType.getMaterialType() != null)
                                ? ':' + propertyType.getMaterialType().getCode() : "");
            }
            default:
            {
                return dataTypeString;
            }
        }
    }

    protected abstract Attribute[] getAttributes(final ENTITY_TYPE entityType);

    protected abstract String getAttributeValue(final ENTITY_TYPE entityType, final Attribute attribute);

    protected abstract ExportableKind getExportableKind();

}
