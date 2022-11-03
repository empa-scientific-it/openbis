package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSpaceExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber, final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Space> spaces = getSpaces(api, sessionToken, permIds);

        addRow(wb, rowNumber++, true, "SPACE");
        addRow(wb, rowNumber++, true, "Code", "Description");

        for (final Space space : spaces)
        {
            addRow(wb, rowNumber++, false, space.getCode(), space.getDescription());
        }

        return rowNumber + 1;
    }

    private Collection<Space> getSpaces(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        return api.getSpaces(sessionToken, spacePermIds, new SpaceFetchOptions()).values();
    }

}
