package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

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
            final String permId, int rowNumber)
    {
        final Space space = getSpace(api, sessionToken, permId);

        if (space != null)
        {

            addRow(wb, rowNumber++, true, "SPACE");
            addRow(wb, rowNumber++, true, "Code", "Description");

            addRow(wb, rowNumber++, false, "1", space.getCode(), space.getDescription());

            addRow(wb, rowNumber++, true, "Version", "Code", "Label", "Description");

//            for (final SpaceTerm spaceTerm : space.getTerms())
//            {
//                addRow(wb, rowNumber++, false, "1", spaceTerm.getCode(), spaceTerm.getLabel(),
//                        spaceTerm.getDescription());
//            }

            return rowNumber + 1;
        } else
        {
            return rowNumber;
        }

    }

    private Space getSpace(final IApplicationServerApi api, final String sessionToken, final String permId)
    {
        final SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        final Map<ISpaceId, Space> spaces = api.getSpaces(sessionToken,
                Collections.singletonList(new SpacePermId(permId)), fetchOptions);

        assert spaces.size() <= 1;

        final Iterator<Space> iterator = spaces.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return null;
    }

}
