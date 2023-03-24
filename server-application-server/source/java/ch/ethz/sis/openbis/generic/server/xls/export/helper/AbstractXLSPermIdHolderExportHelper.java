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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

public abstract class AbstractXLSPermIdHolderExportHelper<HOLDER extends IPermIdHolder> extends AbstractXLSExportHelper<IEntityType>
{
    public AbstractXLSPermIdHolderExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final List<String> permIds, int rowNumber,
            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap,
            final XLSExport.TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        if (permIds.size() != 1)
        {
            throw new UserFailureException("For entity type export number of permIds should be equal to 1.");
        }
        final HOLDER permIdHolder = getPermIdHolder(api, sessionToken, permIds.get(0));
        final Collection<String> warnings = new ArrayList<>();

        if (permIdHolder != null)
        {
            final String permId = permIdHolder.getPermId().toString();
            final ExportableKind exportableKind = getExportableKind();
            warnings.addAll(addRow(rowNumber++, true, exportableKind, permId, exportableKind.name()));

            final Attribute[] possibleAttributes = getAttributes(permIdHolder);
            if (entityTypeExportFieldsMap == null || entityTypeExportFieldsMap.isEmpty() ||
                    !entityTypeExportFieldsMap.containsKey(exportableKind.toString()) ||
                    entityTypeExportFieldsMap.get(exportableKind.toString()).isEmpty())
            {
                // Export all attributes in any order
                // Headers
                final Attribute[] importableAttributes = Arrays.stream(possibleAttributes).filter(Attribute::isImportable)
                        .toArray(Attribute[]::new);
                final Attribute[] attributes = compatibleWithImport ? importableAttributes : possibleAttributes;
                final String[] attributeHeaders = Arrays.stream(attributes).map(Attribute::getName).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, true, exportableKind, permId, attributeHeaders));

                // Values
                final String[] values = Arrays.stream(attributes).map(attribute -> getAttributeValue(permIdHolder, attribute)).toArray(String[]::new);
                warnings.addAll(addRow(rowNumber++, false, exportableKind, permId, values));
            } else
            {
                // Export selected attributes in predefined order
                // Headers
                final Set<Attribute> possibleAttributeNameSet = Stream.of(possibleAttributes)
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
                                return getAttributeValue(permIdHolder, Attribute.valueOf(field.get(FIELD_ID_KEY)));
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);

                warnings.addAll(addRow(rowNumber++, false, exportableKind, permId, entityValues));
            }

            return new AdditionResult(rowNumber + 1, warnings);
        } else
        {
            return new AdditionResult(rowNumber, warnings);
        }
    }

    protected abstract Attribute[] getAttributes(final HOLDER entityType);

    protected abstract String getAttributeValue(final HOLDER entityType, final Attribute attribute);
    
    protected abstract ExportableKind getExportableKind();

    protected abstract HOLDER getPermIdHolder(final IApplicationServerApi api, final String sessionToken, final String permId);

}
