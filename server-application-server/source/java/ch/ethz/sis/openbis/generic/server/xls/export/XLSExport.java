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
package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROJECT;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.SPACE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY_TYPE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;

public class XLSExport
{

    public static final String XLSX_EXTENSION = ".xlsx";

    public static final String ZIP_EXTENSION = ".zip";

    public static final String SCRIPTS_DIRECTORY = "scripts";

    public static final String VALUES_DIRECTORY = "values";

    private static final String TYPE_EXPORT_FIELD_KEY = "TYPE";

    private XLSExport()
    {
        throw new UnsupportedOperationException("Instantiation of a utility class.");
    }

    private static File createDirectory(final File parentDirectory, final String directoryName) throws IOException
    {
        final File scriptsDirectory = new File(parentDirectory, directoryName);
        final boolean directoryCreated = scriptsDirectory.mkdir();

        if (!directoryCreated)
        {
            throw new IOException(String.format("Failed create directory %s.", scriptsDirectory.getAbsolutePath()));
        }

        return scriptsDirectory;
    }

    public static ExportResult export(final String filePrefix, final IApplicationServerApi api,
            final String sessionToken, final List<ExportablePermId> exportablePermIds,
            final boolean exportReferredMasterData,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final TextFormatting textFormatting, final boolean compatibleWithImport) throws IOException
    {
        final PrepareWorkbookResult exportResult = prepareWorkbook(api, sessionToken, exportablePermIds,
                exportReferredMasterData, exportFields, textFormatting, compatibleWithImport);
        final ISessionWorkspaceProvider sessionWorkspaceProvider = CommonServiceProvider.getSessionWorkspaceProvider();
        final Map<String, String> scripts = exportResult.getScripts();

        final String fullFileName = filePrefix + "." +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date()) +
                (scripts.isEmpty() ? XLSX_EXTENSION : ZIP_EXTENSION);
        try (final FileOutputStream os = sessionWorkspaceProvider.getFileOutputStream(sessionToken, fullFileName))
        {
            writeToOutputStream(os, filePrefix, exportResult);
        }
        return new ExportResult(fullFileName, exportResult.getWarnings());
    }

    private static void writeToOutputStream(final FileOutputStream os, final String filePrefix,
            final PrepareWorkbookResult exportResult) throws IOException
    {
        final Map<String, String> scripts = exportResult.getScripts();
        if (scripts.isEmpty())
        {
            try
            (
                    final Workbook wb = exportResult.getWorkbook();
                    final BufferedOutputStream bos = new BufferedOutputStream(os)
            )
            {
                wb.write(bos);
            }
        } else
        {
            try
            (
                    final Workbook wb = exportResult.getWorkbook();
                    final ZipOutputStream zos = new ZipOutputStream(os);
                    final BufferedOutputStream bos = new BufferedOutputStream(zos)
            )
            {
                for (final Map.Entry<String, String> script : scripts.entrySet())
                {
                    zos.putNextEntry(new ZipEntry(String.format("%s/%s.py", SCRIPTS_DIRECTORY, script.getKey())));
                    bos.write(script.getValue().getBytes());
                    bos.flush();
                    zos.closeEntry();
                }

                zos.putNextEntry(new ZipEntry(filePrefix + XLSX_EXTENSION));
                wb.write(bos);
            }
        }
    }

    public static PrepareWorkbookResult prepareWorkbook(final IApplicationServerApi api, final String sessionToken,
            List<ExportablePermId> exportablePermIds, final boolean exportReferredMasterData,
            final Map<String, Map<String, List<Map<String, String>>>> exportFields,
            final TextFormatting textFormatting, final boolean compatibleWithImport)
    {
        if (!isValid(exportablePermIds))
        {
            throw new IllegalArgumentException();
        }

        final Workbook wb = new XSSFWorkbook();
        wb.createSheet();

        final ExportHelperFactory exportHelperFactory = new ExportHelperFactory(wb);

        if (exportReferredMasterData)
        {
            exportablePermIds = expandReference(api, sessionToken, exportablePermIds, exportHelperFactory);
        }

        final Collection<Collection<ExportablePermId>> groupedExportablePermIds =
                putVocabulariesFirst(group(exportablePermIds));

        int rowNumber = 0;
        final Map<String, String> scripts = new HashMap<>();
        final Collection<String> warnings = new ArrayList<>();
        final Map<String, String> valueFiles = new HashMap<>();

        for (final Collection<ExportablePermId> exportablePermIdGroup : groupedExportablePermIds)
        {
            final ExportablePermId exportablePermId = exportablePermIdGroup.iterator().next();
            final ExportableKind exportableKind = exportablePermId.getExportableKind();
            final IXLSExportHelper<? extends IEntityType> helper = exportHelperFactory.getHelper(exportableKind);
            final List<String> permIds = exportablePermIdGroup.stream()
                    .map(permId -> permId.getPermId().getPermId()).collect(Collectors.toList());

            final Map<String, List<Map<String, String>>> entityTypeExportFieldsMap = getEntityTypeExportFieldsMap(exportFields, exportableKind);
            final IXLSExportHelper.AdditionResult additionResult = helper.add(api, sessionToken, wb, permIds, rowNumber,
                    entityTypeExportFieldsMap, textFormatting, compatibleWithImport);
            rowNumber = additionResult.getRowNumber();
            valueFiles.putAll(additionResult.getValueFiles());
            warnings.addAll(additionResult.getWarnings());

            final IEntityType entityType = helper.getEntityType(api, sessionToken,
                    exportablePermId.getPermId().getPermId());

            if (exportReferredMasterData && entityType != null)
            {
                final Plugin validationPlugin = entityType.getValidationPlugin();
                if (validationPlugin != null && validationPlugin.getScript() != null)
                {
                    scripts.put(validationPlugin.getName(), validationPlugin.getScript());
                }

                final Map<String, String> propertyAssignmentPluginScripts = entityType.getPropertyAssignments().stream()
                        .filter(propertyAssignment -> propertyAssignment.getPlugin() != null
                                && propertyAssignment.getPlugin().getScript() != null)
                        .map(PropertyAssignment::getPlugin)
                        .collect(Collectors.toMap(Plugin::getName, Plugin::getScript));

                scripts.putAll(propertyAssignmentPluginScripts);
            }
        }

        return new PrepareWorkbookResult(wb, scripts, warnings, valueFiles);
    }

    private static Map<String, List<Map<String, String>>> getEntityTypeExportFieldsMap(
            final Map<String, Map<String, List<Map<String, String>>>> exportFields, final ExportableKind exportableKind)
    {
        return exportFields == null
                ? null
                : exportFields.get(MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind) || exportableKind == SPACE || exportableKind == PROJECT
                ? TYPE_EXPORT_FIELD_KEY : exportableKind.toString());
    }

    private static List<ExportablePermId> expandReference(final IApplicationServerApi api,
            final String sessionToken, final List<ExportablePermId> exportablePermIds,
            final ExportHelperFactory exportHelperFactory)
    {
        return exportablePermIds.stream().flatMap(exportablePermId ->
        {
            final Stream<ExportablePermId> expandedExportablePermIds = getExpandedExportablePermIds(api, sessionToken,
                    exportablePermId, new HashSet<>(Collections.singletonList(exportablePermId)), exportHelperFactory);
            return Stream.concat(expandedExportablePermIds, Stream.of(exportablePermId));
        }).distinct().collect(Collectors.toList());
    }

    private static Stream<ExportablePermId> getExpandedExportablePermIds(final IApplicationServerApi api,
            final String sessionToken, final ExportablePermId exportablePermId,
            final Set<ExportablePermId> processedIds, final ExportHelperFactory exportHelperFactory)
    {
        final IXLSExportHelper<? extends IEntityType> helper = exportHelperFactory.getHelper(exportablePermId.getExportableKind());
        if (helper != null)
        {
            final IPropertyAssignmentsHolder propertyAssignmentsHolder = helper
                    .getEntityType(api, sessionToken, exportablePermId.getPermId().getPermId());

            if (propertyAssignmentsHolder != null)
            {
                return propertyAssignmentsHolder.getPropertyAssignments().stream().flatMap(propertyAssignment ->
                        {
                            final PropertyType propertyType = propertyAssignment.getPropertyType();
                            switch (propertyType.getDataType())
                            {
                                case CONTROLLEDVOCABULARY:
                                {
                                    return Stream.of(new ExportablePermId(ExportableKind.VOCABULARY_TYPE,
                                            propertyType.getVocabulary().getPermId()));
                                }
                                case SAMPLE:
                                {

                                    return getExportablePermIdStreamForEntityType(api, sessionToken, processedIds,
                                            exportHelperFactory, propertyType.getSampleType(),
                                            ExportableKind.SAMPLE_TYPE, SAMPLE);
                                }
                                default:
                                {
                                    return Stream.empty();
                                }
                            }
                        });
            }
        }

        return Stream.empty();
    }

    private static Stream<ExportablePermId> getExportablePermIdStreamForEntityType(final IApplicationServerApi api,
            final String sessionToken, final Set<ExportablePermId> processedIds,
            final ExportHelperFactory exportHelperFactory, final ICodeHolder codeHolder,
            final ExportableKind exportableKind, final EntityKind entityKind)
    {
        if (codeHolder != null)
        {
            final ExportablePermId entityPropertyExportablePermId =
                    new ExportablePermId(exportableKind, new EntityTypePermId(codeHolder.getCode(), entityKind));

            if (processedIds.contains(entityPropertyExportablePermId))
            {
                return Stream.empty();
            } else
            {
                processedIds.add(entityPropertyExportablePermId);

                final Stream<ExportablePermId> entityPropertyExpandedExportablePermIds =
                        getExpandedExportablePermIds(api, sessionToken,
                                entityPropertyExportablePermId, processedIds,
                                exportHelperFactory);

                return Stream.concat(entityPropertyExpandedExportablePermIds,
                        Stream.of(entityPropertyExportablePermId));
            }
        } else
        {
            return Stream.empty();
        }
    }

    static Collection<Collection<ExportablePermId>> group(final Collection<ExportablePermId> exportablePermIds)
    {
        final Map<ExportableKind, Collection<ExportablePermId>> groupMap = new EnumMap<>(ExportableKind.class);
        final Collection<Collection<ExportablePermId>> result = new ArrayList<>(exportablePermIds.size());
        for (final ExportablePermId permId : exportablePermIds)
        {
            final ExportableKind exportableKind = permId.getExportableKind();
            if (MASTER_DATA_EXPORTABLE_KINDS.contains(exportableKind))
            {
                result.add(Collections.singletonList(permId));
            } else
            {
                final Collection<ExportablePermId> foundGroup = groupMap.get(exportableKind);
                final Collection<ExportablePermId> group;

                if (foundGroup == null)
                {
                    group = new ArrayList<>();
                    groupMap.put(exportableKind, group);
                } else
                {
                    group = foundGroup;
                }

                group.add(permId);
            }
        }

        result.addAll(groupMap.values());

        return result;
    }

    static Collection<Collection<ExportablePermId>> putVocabulariesFirst(
            final Collection<Collection<ExportablePermId>> exportablePermIds)
    {
        final List<Collection<ExportablePermId>> result = new ArrayList<>(exportablePermIds.size());

        // Adding vocabularies first
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() == VOCABULARY_TYPE)
            {
                result.add(group);
            }
        }

        // Adding other items
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() != VOCABULARY_TYPE)
            {
                result.add(group);
            }
        }

        return result;
    }

    private static boolean isValid(final Collection<ExportablePermId> exportablePermIds)
    {
        boolean isValid = true;
        for (final ExportablePermId exportablePermId : exportablePermIds)
        {
            switch (exportablePermId.getExportableKind())
            {
                case SAMPLE_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == SAMPLE;
                    break;
                }
                case EXPERIMENT_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == EXPERIMENT;
                    break;
                }
                case DATASET_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == DATA_SET;
                    break;
                }
                case VOCABULARY_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof VocabularyPermId;
                    break;
                }
                case SPACE:
                {
                    isValid = exportablePermId.getPermId() instanceof SpacePermId;
                    break;
                }
            }

            if (isValid == false)
            {
                break;
            }
        }

        return isValid;
    }

    public static class PrepareWorkbookResult
    {

        private final Workbook workbook;

        private final Map<String, String> scripts;

        private final Collection<String> warnings;

        private final Map<String, String> valueFiles;

        public PrepareWorkbookResult(final Workbook workbook, final Map<String, String> scripts,
                final Collection<String> warnings, final Map<String, String> valueFiles)
        {
            this.workbook = workbook;
            this.scripts = scripts;
            this.warnings = warnings;
            this.valueFiles = valueFiles;
        }

        public Workbook getWorkbook()
        {
            return workbook;
        }

        public Map<String, String> getScripts()
        {
            return scripts;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

        public Map<String, String> getValueFiles()
        {
            return valueFiles;
        }

    }

    public enum TextFormatting
    {
        PLAIN, RICH
    }

    public static class ExportResult
    {

        final String fileName;

        final Collection<String> warnings;

        public ExportResult(final String fileName, final Collection<String> warnings)
        {
            this.fileName = fileName;
            this.warnings = warnings;
        }

        public String getFileName()
        {
            return fileName;
        }

        public Collection<String> getWarnings()
        {
            return warnings;
        }

    }

}
