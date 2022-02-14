package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleTypeExportHelper;

public class XLSExport
{

    private final IXLSExportHelper sampleTypeExportHelper = new XLSSampleTypeExportHelper();

    public OutputStream export(final IApplicationServerApi api, Collection<ExportablePermId> exportablePermIds,
            final boolean exportReferred) throws IOException
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
        for (final ExportablePermId exportablePermId : exportablePermIds)
        {
            switch (exportablePermId.getExportableKind())
            {
                case SAMPLE_TYPE:
                    sampleTypeExportHelper.add(api, wb, exportablePermId);
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
        }

        final OutputStream result = new ByteArrayOutputStream();
        wb.write(result);
        return result;
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

}
