package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSSpaceExportHelper extends AbstractXLSExportHelper
{

    public XLSSpaceExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Space> spaces = getSpaces(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        warnings.addAll(addRow(rowNumber++, true, ExportableKind.SPACE, null, "SPACE"));
        warnings.addAll(addRow(rowNumber++, true, ExportableKind.SPACE, null, "Code", "Description"));

        for (final Space space : spaces)
        {
            warnings.addAll(addRow(rowNumber++, false, ExportableKind.SPACE, space.getPermId().getPermId(),
                    space.getCode(), space.getDescription()));
        }

        return new AdditionResult(rowNumber + 1, warnings);
    }

    private Collection<Space> getSpaces(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<SpacePermId> spacePermIds = permIds.stream().map(SpacePermId::new).collect(Collectors.toList());
        return api.getSpaces(sessionToken, spacePermIds, new SpaceFetchOptions()).values();
    }

}
