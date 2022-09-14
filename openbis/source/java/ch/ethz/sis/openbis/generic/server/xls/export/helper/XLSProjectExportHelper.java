package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;

public class XLSProjectExportHelper extends AbstractXLSExportHelper
{

    @Override
    public int add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber)
    {
        final Collection<Project> projects = getProjects(api, sessionToken, permIds);

        addRow(wb, rowNumber++, true, "PROJECT");
        addRow(wb, rowNumber++, true, "Identifier", "Code", "Description", "Space");

        for (final Project project : projects)
        {
            addRow(wb, rowNumber++, false, project.getIdentifier().getIdentifier(), project.getCode(),
                    project.getDescription(), project.getSpace().getCode());
        }

        return rowNumber + 1;
    }

    private Collection<Project> getProjects(final IApplicationServerApi api, final String sessionToken,
            final Collection<String> permIds)
    {
        final List<ProjectPermId> projectPermIds = permIds.stream().map(ProjectPermId::new)
                .collect(Collectors.toList());
        final ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();
        return api.getProjects(sessionToken, projectPermIds, fetchOptions).values();
    }

    @Override
    public IPropertyAssignmentsHolder getPropertyAssignmentsHolder(final IApplicationServerApi api,
            final String sessionToken, final String permId)
    {
        return null;
    }

}
