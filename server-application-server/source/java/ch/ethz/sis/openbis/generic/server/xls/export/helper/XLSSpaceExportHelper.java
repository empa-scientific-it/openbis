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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSpaceExportHelper extends AbstractXLSExportHelper<IEntityType>
{

    public XLSSpaceExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        final Collection<Space> spaces = getSpaces(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();
        final Map<String, String> valueFiles = new HashMap<>();

        addRow(rowNumber++, true, ExportableKind.SPACE, null, warnings, valueFiles, ExportableKind.SPACE.toString());

        final Attribute[] possibleAttributes = new Attribute[] { Attribute.CODE, Attribute.DESCRIPTION, Attribute.REGISTRATOR,
                Attribute.REGISTRATION_DATE };
        if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                !entityTypeExportFieldsMap.containsKey(ExportableKind.SPACE.toString()) ||
                entityTypeExportFieldsMap.get(ExportableKind.SPACE.toString()).isEmpty())
        {
            // Export all attributes in any order
            // Headers
            final Attribute[] importableAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isImportable)
                    .toArray(Attribute[]::new);
            final Attribute[] attributes = compatibleWithImport ? importableAttributes : possibleAttributes;
            final String[] attributeHeaders = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);

            addRow(rowNumber++, true, ExportableKind.SPACE, null, warnings, valueFiles, attributeHeaders);

            // Values
            for (final Space space : spaces)
            {
                final String[] values = Arrays.stream(attributes)
                        .map(attribute -> getAttributeValue(space, attribute)).toArray(String[]::new);
                addRow(rowNumber++, false, ExportableKind.SPACE, null, warnings, valueFiles, values);
            }
        } else
        {
            // Export selected attributes in predefined order
            // Headers
            final Set<Attribute> possibleAttributeNameSet = Stream.of(possibleAttributes)
                    .filter(attribute -> !compatibleWithImport || attribute.isImportable())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
            final List<Map<String, String>> selectedExportAttributes = entityTypeExportFieldsMap.get(ExportableKind.SPACE.toString());

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

            addRow(rowNumber++, true, ExportableKind.SPACE, null, warnings, valueFiles, allAttributeNames);

            // Values
            final Set<Map<String, String>> selectedExportFieldSet = new HashSet<>(selectedExportAttributes);
            final List<Map<String, String>> extraExportFields = compatibleWithImport
                    ? Arrays.stream(requiredForImportAttributes)
                    .map(attribute -> Map.of(FIELD_TYPE_KEY, FieldType.ATTRIBUTE.toString(), FIELD_ID_KEY, attribute.toString()))
                    .filter(map -> !selectedExportFieldSet.contains(map))
                    .collect(Collectors.toList())
                    : List.of();
            for (final Space space : spaces)
            {
                final String[] entityValues = Stream.concat(selectedExportAttributes.stream(), extraExportFields.stream())
                        .filter(field -> isFieldAcceptable(possibleAttributeNameSet, field))
                        .map(field ->
                        {
                            if (FieldType.valueOf(field.get(FIELD_TYPE_KEY)) == FieldType.ATTRIBUTE)
                            {
                                return getAttributeValue(space, Attribute.valueOf(field.get(FIELD_ID_KEY)));
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);

                addRow(rowNumber++, false, ExportableKind.SPACE, null, warnings, valueFiles, entityValues);
            }
        }

        return new AdditionResult(rowNumber + 1, warnings, valueFiles);
    }

    protected String getAttributeValue(final Space space, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return space.getCode();
            }
            case DESCRIPTION:
            {
                return space.getDescription();
            }
            case REGISTRATOR:
            {
                return space.getRegistrator().getUserId();
            }
            case REGISTRATION_DATE:
            {
                return DATE_FORMAT.format(space.getRegistrationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    private Collection<Space> getSpaces(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        final SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        fetchOptions.withRegistrator();
        return api.getSpaces(sessionToken, spacePermIds, fetchOptions).values();
    }

}
