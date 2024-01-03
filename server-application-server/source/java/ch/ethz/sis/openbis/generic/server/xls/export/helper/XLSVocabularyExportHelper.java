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
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATION_DATE;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.REGISTRATOR;
import static ch.ethz.sis.openbis.generic.server.xls.export.Attribute.valueOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.Attribute;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.FieldType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSVocabularyExportHelper extends AbstractXLSExportHelper<IEntityType>
{

    protected static final String[] VOCABULARY_ASSIGNMENT_COLUMNS = new String[] { "Code", "Label", "Description" };

    public XLSVocabularyExportHelper(final Workbook wb)
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
            throw new IllegalArgumentException("For entity type export number of permIds should be equal to 1.");
        }
        final Vocabulary vocabulary = getVocabulary(api, sessionToken, permIds.get(0));
        final Collection<String> warnings = new ArrayList<>();
        final Map<String, String> valueFiles = new HashMap<>();

        if (vocabulary != null)
        {
            final String permId = vocabulary.getPermId().toString();
            final ExportableKind exportableKind = getExportableKind();
            addRow(rowNumber++, true, exportableKind, permId, warnings, valueFiles, exportableKind.name());

            final Attribute[] possibleAttributes = getAttributes();
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

                addRow(rowNumber++, true, exportableKind, permId, warnings, valueFiles, attributeHeaders);

                // Values
                final String[] values = Arrays.stream(attributes).map(attribute -> getAttributeValue(vocabulary, attribute)).toArray(String[]::new);
                addRow(rowNumber++, false, exportableKind, permId, warnings, valueFiles, values);
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
                                return valueOf(attribute.get(FIELD_ID_KEY)).getName();
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
                        .map(map -> valueOf(map.get(FIELD_ID_KEY)))
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Attribute.class)));
                final Stream<String> requiredForImportAttributeNameStream = compatibleWithImport
                        ? Arrays.stream(requiredForImportAttributes)
                        .filter(attribute -> !selectedAttributes.contains(attribute))
                        .map(Attribute::getName)
                        : Stream.empty();
                final String[] allAttributeNames = Stream.concat(Arrays.stream(selectedAttributeHeaders), requiredForImportAttributeNameStream)
                        .toArray(String[]::new);

                addRow(rowNumber++, true, exportableKind, permId, warnings, valueFiles, allAttributeNames);

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
                                return getAttributeValue(vocabulary, valueOf(field.get(FIELD_ID_KEY)));
                            } else
                            {
                                throw new IllegalArgumentException();
                            }
                        }).toArray(String[]::new);

                addRow(rowNumber++, false, exportableKind, permId, warnings, valueFiles, entityValues);
            }


            addRow(rowNumber++, true, ExportableKind.VOCABULARY_TYPE, permId, warnings, valueFiles, VOCABULARY_ASSIGNMENT_COLUMNS);

            for (final VocabularyTerm vocabularyTerm : vocabulary.getTerms())
            {
                final String[] values = {
                        vocabularyTerm.getCode(),
                        vocabularyTerm.getLabel(),
                        vocabularyTerm.getDescription() };
                addRow(rowNumber++, false, ExportableKind.VOCABULARY_TYPE, permId, warnings, valueFiles, values);
            }

            return new AdditionResult(rowNumber + 1, warnings, valueFiles);
        } else
        {
            return new AdditionResult(rowNumber, warnings, valueFiles);
        }
    }

    protected Attribute[] getAttributes()
    {
        return new Attribute[] { CODE, DESCRIPTION, REGISTRATOR, REGISTRATION_DATE, MODIFICATION_DATE };
    }

    protected String getAttributeValue(final Vocabulary vocabulary, final Attribute attribute)
    {
        switch (attribute)
        {
            case CODE:
            {
                return vocabulary.getCode();
            }
            case DESCRIPTION:
            {
                return vocabulary.getDescription();
            }
            case REGISTRATOR:
            {
                return vocabulary.getRegistrator().getUserId();
            }
            case REGISTRATION_DATE:
            {
                return DATE_FORMAT.format(vocabulary.getRegistrationDate());
            }
            case MODIFICATION_DATE:
            {
                return DATE_FORMAT.format(vocabulary.getModificationDate());
            }
            default:
            {
                return null;
            }
        }
    }

    protected ExportableKind getExportableKind()
    {
        return ExportableKind.VOCABULARY_TYPE;
    }

    private Vocabulary getVocabulary(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
        fetchOptions.withTerms();
        fetchOptions.withRegistrator();
        final Map<IVocabularyId, Vocabulary> vocabularies = api.getVocabularies(sessionToken,
                Collections.singletonList(new VocabularyPermId(permId)), fetchOptions);

        assert vocabularies.size() <= 1;

        final Iterator<Vocabulary> iterator = vocabularies.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

}
