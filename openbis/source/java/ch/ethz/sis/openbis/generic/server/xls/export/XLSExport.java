package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.MASTER_DATA_EXPORTABLE_KINDS;
import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.VOCABULARY;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSDataSetExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSDataSetTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSExperimentExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSExperimentTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSProjectExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSpaceExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSVocabularyExportHelper;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceV3Factory;

public class XLSExport
{

    private static final IXLSExportHelper SAMPLE_TYPE_EXPORT_HELPER = new XLSSampleTypeExportHelper();

    private static final IXLSExportHelper EXPERIMENT_TYPE_EXPORT_HELPER = new XLSExperimentTypeExportHelper();

    private static final IXLSExportHelper DATA_SET_TYPE_EXPORT_HELPER = new XLSDataSetTypeExportHelper();

    private static final IXLSExportHelper VOCABULARY_EXPORT_HELPER = new XLSVocabularyExportHelper();

    private static final IXLSExportHelper SPACE_EXPORT_HELPER = new XLSSpaceExportHelper();

    private static final IXLSExportHelper PROJECT_EXPORT_HELPER = new XLSProjectExportHelper();

    private static final IXLSExportHelper EXPERIMENT_EXPORT_HELPER = new XLSExperimentExportHelper();

    private static final IXLSExportHelper SAMPLE_EXPORT_HELPER = new XLSSampleExportHelper();

    private static final IXLSExportHelper DATA_SET_EXPORT_HELPER = new XLSDataSetExportHelper();

    public byte[] export(final String spreadsheetFileName, final IApplicationServerApi api, final String sessionToken,
            final Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws IOException
    {
        final ExportResult exportResult = prepareWorkbook(api, sessionToken, exportablePermIds, exportReferred);
        final Map<String, String> scripts = exportResult.getScripts();
        if (scripts.isEmpty())
        {
            try (
                    final Workbook wb = exportResult.getWorkbook();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final BufferedOutputStream bos = new BufferedOutputStream(baos)
            )
            {
                wb.write(bos);
                return baos.toByteArray();
            }
        } else
        {
            try (
                    final Workbook wb = exportResult.getWorkbook();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final ZipOutputStream zos = new ZipOutputStream(baos);
                    final BufferedOutputStream bos = new BufferedOutputStream(zos)
            )
            {
                for (final Map.Entry<String, String> script : scripts.entrySet())
                {
                    zos.putNextEntry(new ZipEntry(String.format("scripts/%s.py", script.getKey())));
                    bos.write(script.getValue().getBytes());
                    bos.flush();
                    zos.closeEntry();
                }

                zos.putNextEntry(new ZipEntry(spreadsheetFileName));
                wb.write(bos);

                return baos.toByteArray();
            }
        }
    }

    ExportResult prepareWorkbook(final IApplicationServerApi api, final String sessionToken,
            Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws IOException
    {
        if (!isValid(exportablePermIds))
        {
            throw new IllegalArgumentException();
        }

        if (exportReferred)
        {
            exportablePermIds = expandReference(api, sessionToken, exportablePermIds);
        }

        final Collection<Collection<ExportablePermId>> groupedExportablePermIds =
                putVocabulariesFirst(group(exportablePermIds));

        final Workbook wb = new XSSFWorkbook();
        wb.createSheet();
        int rowNumber = 0;
        final Map<String, String> scripts = new HashMap<>();

        for (final Collection<ExportablePermId> exportablePermIdGroup : groupedExportablePermIds)
        {
            final ExportablePermId exportablePermId = exportablePermIdGroup.iterator().next();
            final IXLSExportHelper helper = getHelper(exportablePermId.getExportableKind());
            final List<String> permIds = exportablePermIdGroup.stream()
                    .map(permId -> permId.getPermId().getPermId()).collect(Collectors.toList());
            rowNumber = helper.add(api, sessionToken, wb, permIds, rowNumber);
            final IEntityType entityType = helper.getEntityType(api, sessionToken,
                    exportablePermId.getPermId().getPermId());

            if (entityType != null)
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

        return new ExportResult(wb, scripts);
    }

    private Collection<ExportablePermId> expandReference(final IApplicationServerApi api,
            final String sessionToken, final Collection<ExportablePermId> exportablePermIds)
    {
        return exportablePermIds.stream().flatMap(exportablePermId ->
        {
            final Stream<ExportablePermId> expandedExportablePermIds = getExpandedExportablePermIds(api, sessionToken,
                    exportablePermId, new HashSet<>(Collections.singletonList(exportablePermId)));
            return Stream.concat(expandedExportablePermIds, Stream.of(exportablePermId));
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<ExportablePermId> getExpandedExportablePermIds(final IApplicationServerApi api,
            final String sessionToken, final ExportablePermId exportablePermId,
            final Set<ExportablePermId> processedIds)
    {
        final IXLSExportHelper helper = getHelper(exportablePermId.getExportableKind());
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
                                    return Stream.of(new ExportablePermId(ExportableKind.VOCABULARY,
                                            propertyType.getVocabulary().getPermId()));
                                }
                                case SAMPLE:
                                {
                                    final ExportablePermId samplePropertyExportablePermId =
                                            new ExportablePermId(ExportableKind.SAMPLE_TYPE,
                                            new EntityTypePermId(propertyType.getSampleType().getCode(), SAMPLE));

                                    if (processedIds.contains(samplePropertyExportablePermId))
                                    {
                                        return Stream.empty();
                                    } else
                                    {
                                        processedIds.add(samplePropertyExportablePermId);

                                        final Stream<ExportablePermId> samplePropertyExpandedExportablePermIds =
                                                getExpandedExportablePermIds(api, sessionToken,
                                                        samplePropertyExportablePermId, processedIds);

                                        return Stream.concat(samplePropertyExpandedExportablePermIds,
                                                Stream.of(samplePropertyExportablePermId));
                                    }
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

    private IXLSExportHelper getHelper(final ExportableKind exportableKind)
    {
        switch (exportableKind)
        {
            case SAMPLE_TYPE:
            {
                return SAMPLE_TYPE_EXPORT_HELPER;
            }
            case EXPERIMENT_TYPE:
            {
                return EXPERIMENT_TYPE_EXPORT_HELPER;
            }
            case DATASET_TYPE:
            {
                return DATA_SET_TYPE_EXPORT_HELPER;
            }
            case VOCABULARY:
            {
                return VOCABULARY_EXPORT_HELPER;
            }
            case SPACE:
            {
                return SPACE_EXPORT_HELPER;
            }
            case PROJECT:
            {
                return PROJECT_EXPORT_HELPER;
            }
            case EXPERIMENT:
            {
                return EXPERIMENT_EXPORT_HELPER;
            }
            case SAMPLE:
            {
                return SAMPLE_EXPORT_HELPER;
            }
            case DATASET:
            {
                return DATA_SET_EXPORT_HELPER;
            }
            default:
            {
                throw new IllegalArgumentException(String.format("Not supported exportable kind %s.", exportableKind));
            }
        }
    }

    Collection<Collection<ExportablePermId>> group(final Collection<ExportablePermId> exportablePermIds)
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

    Collection<Collection<ExportablePermId>> putVocabulariesFirst(
            final Collection<Collection<ExportablePermId>> exportablePermIds)
    {
        final List<Collection<ExportablePermId>> result = new ArrayList<>(exportablePermIds.size());

        // Adding vocabularies first
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() == VOCABULARY)
            {
                result.add(group);
            }
        }

        // Adding other items
        for (final Collection<ExportablePermId> group : exportablePermIds)
        {
            if (group.iterator().next().getExportableKind() != VOCABULARY)
            {
                result.add(group);
            }
        }

        return result;
    }

    private boolean isValid(final Collection<ExportablePermId> exportablePermIds)
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
                case VOCABULARY:
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

    public static void main(String[] args) throws IOException
    {
        final XLSExport xlsExport = new XLSExport();

        final OpenBisServiceV3Factory openBisServiceV3Factory =
                new OpenBisServiceV3Factory("http://localhost:8888/");
        final IApplicationServerApi applicationServerApi = openBisServiceV3Factory.createService();
        final String sessionToken = applicationServerApi.login("admin", "changeit");

        final Collection<ExportablePermId> vocabularies = Stream.of("STORAGE_FORMAT", "DEFAULT_COLLECTION_VIEWS",
                        "SUPPLIER.PREFERRED_ORDER_METHOD", "$STORAGE_POSITION.STORAGE_BOX_SIZE",
                        "ORDER.ORDER_STATUS", "SUPPLIER.LANGUAGE", "WELL.COLOR_ENCODED_ANNOTATIONS",
                        "PRODUCT.CURRENCY", "$STORAGE.STORAGE_VALIDATION_LEVEL", "ANTIBODY.DETECTION",
                        "YEAST.BACKGROUND_SPECIFIC_MARKERS", "MEDIA.ORGANISM", "RNA.RNA_BACKBONE", "OLIGO.DIRECTION",
                        "ANTIBODY.CLONALITY", "YEAST.GENETIC_BACKGROUND", "YEAST.MATING_TYPE", "PLASMID.BACKBONE",
                        "ORIGIN", "ANNOTATION.PLASMID_RELATIONSHIP", "CELL_LINE.YES_NO_CHOICE", "RNA.STRAND",
                        "CELL_LINE.CELL_MEDIUM", "ANTIBODY.HOST", "RNA.RNA_TYPE", "CELL_LINE.SPECIES", "STERILIZATION",
                        "LIFE_SCIENCES_TYPES.VERSION", "PLASMID.MARKER", "YEAST.COMMON_MARKERS",
                        "PCR_PROTOCOL.TEMPLATE", "PLASMID.BACTERIAL_ANTIBIOTIC_RESISTANCE", "$STORAGE_CONDITIONS",
                        "YEAST.ENDOGENOUS_PLASMID", "CHECK", "CELL_LINE.CELL_TYPE",
                        "WESTERN_BLOTTING_PROTOCOL.MEMBRANE")
                .map(code -> new ExportablePermId(ExportableKind.VOCABULARY, new VocabularyPermId(code)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> sampleTypes = Stream.of("UNKNOWN", "STORAGE", "ENTRY", "PUBLICATION",
                        "STORAGE_POSITION", "SUPPLIER", "ORDER", "REQUEST", "PRODUCT", "GENERAL_ELN_SETTINGS",
                        "GENERAL_PROTOCOL", "EXPERIMENTAL_STEP", "BACTERIA", "PCR_PROTOCOL", "RNA", "PLASMID", "OLIGO",
                        "ANTIBODY", "WESTERN_BLOTTING_PROTOCOL", "SOLUTION_BUFFER", "YEAST", "PLANT_SPECIES", "FLY",
                        "CELL_LINE", "ENZYME", "MEDIA", "CHEMICAL")
                .map(code -> new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId(code, SAMPLE)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> experimentTypes = Stream.of("UNKNOWN", "COLLECTION", "DEFAULT_EXPERIMENT")
                .map(code -> new ExportablePermId(ExportableKind.EXPERIMENT_TYPE,
                        new EntityTypePermId(code, EXPERIMENT)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> dataSetTypes = Stream.of("UNKNOWN", "ATTACHMENT", "ANALYSIS_NOTEBOOK",
                        "RAW_DATA", "PUBLICATION_DATA", "OTHER_DATA", "SOURCE_CODE", "PROCESSED_DATA", "ANALYZED_DATA",
                        "ELN_PREVIEW", "SEQ_FILE")
                .map(code -> new ExportablePermId(ExportableKind.DATASET_TYPE,
                        new EntityTypePermId(code, DATA_SET)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> spaces = Stream.of("DEFAULT", "DEFAULT_LAB_NOTEBOOK", "ELN_SETTINGS")
                .map(permId -> new ExportablePermId(ExportableKind.SPACE, new SpacePermId(permId)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> samples = Stream.of("20220921142846885-1", "20220921142853426-4")
                .map(permId -> new ExportablePermId(ExportableKind.SAMPLE, new ObjectPermId(permId)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> projects = Stream.of("20220921142846885-1", "20220921142853426-6")
                .map(permId -> new ExportablePermId(ExportableKind.PROJECT, new ProjectPermId(permId)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> experiments = Stream.of("20220921142846885-1", "20220921142853426-14",
                        "20220921142853426-2")
                .map(permId -> new ExportablePermId(ExportableKind.EXPERIMENT, new ExperimentPermId(permId)))
                .collect(Collectors.toList());

        final Collection<ExportablePermId> exportablePermIds = new ArrayList<>();
        exportablePermIds.addAll(projects);
        exportablePermIds.addAll(samples);
        exportablePermIds.addAll(vocabularies);
        exportablePermIds.addAll(sampleTypes);
        exportablePermIds.addAll(experimentTypes);
        exportablePermIds.addAll(dataSetTypes);
        exportablePermIds.addAll(spaces);
        exportablePermIds.addAll(experiments);

        final byte[] bytes = xlsExport.export("export.xlsx", applicationServerApi, sessionToken,
                exportablePermIds, true);
        try (final FileOutputStream fos = new FileOutputStream("test.zip"))
        {
            fos.write(bytes);
        }
    }

    public static class ExportResult
    {

        private final Workbook workbook;

        private final Map<String, String> scripts;

        public ExportResult(final Workbook workbook, final Map<String, String> scripts)
        {
            this.workbook = workbook;
            this.scripts = scripts;
        }

        public Workbook getWorkbook()
        {
            return workbook;
        }

        public Map<String, String> getScripts()
        {
            return scripts;
        }

    }

}
