package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleTypeExportHelper;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceV3Factory;

public class XLSExport
{

    private final IXLSExportHelper sampleTypeExportHelper = new XLSSampleTypeExportHelper();

    Workbook prepareWorkbook(final IApplicationServerApi api, final String sessionToken,
            Collection<ExportablePermId> exportablePermIds, final boolean exportReferred) throws IOException
    {
        if (isValid(exportablePermIds) == false)
        {
            throw new IllegalArgumentException();
        }

        if (exportReferred)
        {
            exportablePermIds = expandReference(api, exportablePermIds);
        }

        exportablePermIds = sort(exportablePermIds);

        final Workbook wb = new XSSFWorkbook();
        wb.createSheet();
        int rowNumber = 0;
        for (final ExportablePermId exportablePermId : exportablePermIds)
        {
            switch (exportablePermId.getExportableKind())
            {
                case SAMPLE_TYPE:
                    rowNumber = sampleTypeExportHelper.add(api, sessionToken, wb, exportablePermId, rowNumber);
                    break;
                case EXPERIMENT_TYPE:
                    break;
                case DATASET_TYPE:
                    break;
                case VOCABULARY:
                    break;
                case PROPERTY_TYPE:
                    break;
            }
            rowNumber++;
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
            final Collection<ExportablePermId> exportablePermIds)
    {
        // TODO: implement.
        return exportablePermIds;
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
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == SAMPLE;
                    break;
                case EXPERIMENT_TYPE:
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == EXPERIMENT;
                    break;
                case DATASET_TYPE:
                    isValid = exportablePermId.getPermId() instanceof EntityTypePermId &&
                            ((EntityTypePermId) exportablePermId.getPermId()).getEntityKind() == DATA_SET;
                    break;
                case VOCABULARY:
                    isValid = exportablePermId.getPermId() instanceof VocabularyPermId;
                    break;
                case PROPERTY_TYPE:
                    isValid = exportablePermId.getPermId() instanceof PropertyTypePermId;
                    break;
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
//        final IApplicationServerApi applicationServerApi = new ApplicationServerApi();

        final OpenBisServiceV3Factory openBisServiceV3Factory =
                new OpenBisServiceV3Factory("http://localhost:8888/");
        final IApplicationServerApi applicationServerApi = openBisServiceV3Factory.createService();
        final String sessionToken = applicationServerApi.login("admin", "changeit");

        final Collection<ExportablePermId> permIds = Stream.of("UNKNOWN", "STORAGE", "ENTRY", "PUBLICATION", "STORAGE_POSITION",
                        "SUPPLIER", "ORDER", "REQUEST", "PRODUCT", "GENERAL_ELN_SETTINGS", "GENERAL_PROTOCOL",
                        "EXPERIMENTAL_STEP", "BACTERIA", "PCR_PROTOCOL", "RNA", "PLASMID", "OLIGO", "ANTIBODY",
                        "WESTERN_BLOTTING_PROTOCOL", "SOLUTION_BUFFER", "YEAST", "PLANT_SPECIES", "FLY", "CELL_LINE",
                        "ENZYME", "MEDIA", "CHEMICAL")
                .map(code -> new ExportablePermId(ExportableKind.SAMPLE_TYPE, new EntityTypePermId(code, SAMPLE)))
                .collect(Collectors.toList());
        final ByteArrayOutputStream os = xlsExport.export(applicationServerApi, sessionToken,
                permIds, false);

        try (final OutputStream fileOutputStream = new FileOutputStream("test.xlsx"))
        {
            os.writeTo(fileOutputStream);
        }
    }

}
