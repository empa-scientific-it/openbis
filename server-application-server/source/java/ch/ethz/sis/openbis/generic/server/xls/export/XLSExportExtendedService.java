package ch.ethz.sis.openbis.generic.server.xls.export;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XLSExportExtendedService
{

    public static String export(String sessionToken, Map<String, Serializable> parameters) {
        System.out.println("sessionToken: " + sessionToken);
        System.out.println("parameters: " + parameters);

        // Root
        String kind = ((Map<String, String>) parameters.get("entity")).get("kind");
        String permId = ((Map<String, String>) parameters.get("entity")).get("permId");
        // Options
        boolean withEmail = (boolean) parameters.get("withEmail");
        boolean withImportCompatibility = (boolean) parameters.get("withImportCompatibility");
        // Formats
        boolean pdf = ((Map<String, Boolean>) parameters.get("formats")).get("pdf");
        boolean xlsx = ((Map<String, Boolean>) parameters.get("formats")).get("xlsx");
        boolean data = ((Map<String, Boolean>) parameters.get("formats")).get("data");
        // Inclusions
        boolean withLevelsBelow = (boolean) parameters.get("withLevelsBelow");
        boolean withObjectsAndDataSetsParents = (boolean) parameters.get("withObjectsAndDataSetsParents");
        boolean withObjectsAndDataSetsOtherSpaces = (boolean) parameters.get("withObjectsAndDataSetsOtherSpaces");

        IApplicationServerInternalApi applicationServerApi = CommonServiceProvider.getApplicationServerApi();
        ExportData exportData = new ExportData();
        ExportablePermId exportablePermId = new ExportablePermId(ExportableKind.valueOf(kind), new ObjectPermId(permId));
        exportData.setPermIds(List.of(exportablePermId));
        exportData.setFields(new AllFields());
        ExportOptions exportOptions = new ExportOptions();
        Set<ExportFormat> formats = new HashSet<>();
        if (pdf) {
            formats.add(ExportFormat.PDF);
        }
        if (xlsx) {
            formats.add(ExportFormat.XLSX);
        }
        if (data) {
            formats.add(ExportFormat.DATA);
        }
        exportOptions.setFormats(formats);
        exportOptions.setXlsTextFormat(XlsTextFormat.RICH);
        exportOptions.setWithReferredTypes(Boolean.TRUE);
        exportOptions.setWithImportCompatibility(withImportCompatibility);
        exportOptions.setZipSingleFiles(Boolean.TRUE);

        ExportResult exportResult = applicationServerApi.executeExport(sessionToken, exportData, exportOptions);
        return exportResult.getDownloadURL();
    }
}
