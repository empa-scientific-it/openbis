package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

public interface IXLSExportHelper
{

    int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final String permId, final int rowNumber);

}
