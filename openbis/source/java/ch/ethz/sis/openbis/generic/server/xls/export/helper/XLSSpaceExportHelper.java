package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class XLSSpaceExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        final Collection<Space> spaces = getSpaces(api, sessionToken, permIds);

        addRow(wb, rowNumber++, true, "SPACE");
        addRow(wb, rowNumber++, true, "Code", "Description");

//            addRow(wb, rowNumber++, false, "1", spaces.getCode(), spaces.getDescription());
//
//            addRow(wb, rowNumber++, true, "Version", "Code", "Label", "Description");

//            for (final SpaceTerm spaceTerm : space.getTerms())
//            {
//                addRow(wb, rowNumber++, false, "1", spaceTerm.getCode(), spaceTerm.getLabel(),
//                        spaceTerm.getDescription());
//            }

        return rowNumber + 1;
    }

    private Collection<Space> getSpaces(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        return api.getSpaces(sessionToken, spacePermIds, new SpaceFetchOptions()).values();
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return null;
    }

}
