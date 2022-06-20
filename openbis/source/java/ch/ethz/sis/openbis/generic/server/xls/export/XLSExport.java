package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSDataSetTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSExperimentTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSVocabularyExportHelper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceV3Factory;

public class XLSExport
{

    private final IXLSExportHelper vocabularyExportHelper = new XLSVocabularyExportHelper();

    private final IXLSExportHelper sampleTypeExportHelper = new XLSSampleTypeExportHelper();

    private final IXLSExportHelper experimentTypeExportHelper = new XLSExperimentTypeExportHelper();

    private final IXLSExportHelper dataSetTypeExportHelper = new XLSDataSetTypeExportHelper();

    Workbook prepareWorkbook(final IApplicationServerApi api, final String sessionToken,
            Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws IOException
    {
        if (isValid(exportablePermIds) == false)
        {
            throw new IllegalArgumentException();
        }

        if (exportReferred)
        {
            exportablePermIds = expandReference(api, sessionToken, exportablePermIds);
        }

        exportablePermIds = sort(exportablePermIds);

        final Workbook wb = new XSSFWorkbook();
        wb.createSheet();
        int rowNumber = 0;
        for (final ExportablePermId exportablePermId : exportablePermIds)
        {
            final IXLSExportHelper helper = getHelper(exportablePermId.getExportableKind());
            if (helper != null)
            {
                rowNumber = helper.add(api, sessionToken, wb, exportablePermId.getPermId().getPermId(), rowNumber);
            }
        }

        return wb;
    }

    public ByteArrayOutputStream export(final IApplicationServerApi api, final String sessionToken,
            final Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws IOException
    {
        try (final Workbook wb = prepareWorkbook(api, sessionToken, exportablePermIds, exportReferred))
        {
            final ByteArrayOutputStream result = new ByteArrayOutputStream();
            wb.write(result);
            return result;
        }
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
                    .getPropertyAssignmentsHolder(api, sessionToken, exportablePermId.getPermId().getPermId());

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
                return sampleTypeExportHelper;
            }
            case EXPERIMENT_TYPE:
            {
                return experimentTypeExportHelper;
            }
            case DATASET_TYPE:
            {
                return dataSetTypeExportHelper;
            }
            case VOCABULARY:
            {
                return vocabularyExportHelper;
            }
            default:
            {
                return null;
            }
        }
    }

    private Collection<ExportablePermId> sort(final Collection<ExportablePermId> exportablePermIds)
    {
        // TODO: implement.
        return exportablePermIds;
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
                case PROPERTY_TYPE:
                {
                    isValid = exportablePermId.getPermId() instanceof PropertyTypePermId;
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

        final Collection<ExportablePermId> exportablePermIds = new ArrayList<>();
        exportablePermIds.addAll(vocabularies);
        exportablePermIds.addAll(sampleTypes);
        exportablePermIds.addAll(experimentTypes);
        exportablePermIds.addAll(dataSetTypes);
        final ByteArrayOutputStream os = xlsExport.export(applicationServerApi, sessionToken, exportablePermIds, true);

        try (final OutputStream fileOutputStream = new FileOutputStream("test.xlsx"))
        {
            os.writeTo(fileOutputStream);
        }
    }

}
