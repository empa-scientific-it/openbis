package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public interface IXLSExportHelper
{

    int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, final int rowNumber, final XLSExport.TextFormatting textFormatting);

    IEntityType getEntityType(final IApplicationServerApi api, final String sessionToken, final String permId);

}
