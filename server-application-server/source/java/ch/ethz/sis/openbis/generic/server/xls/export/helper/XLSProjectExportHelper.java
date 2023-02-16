/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ethz.sis.openbis.generic.server.xls.export.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind;
import ch.ethz.sis.openbis.generic.server.xls.export.XLSExport;

public class XLSProjectExportHelper extends AbstractXLSExportHelper
{

    public XLSProjectExportHelper(final Workbook wb)
    {
        super(wb);
    }

    @Override
    public AdditionResult add(final IApplicationServerApi api, final String sessionToken, final Workbook wb,
            final Collection<String> permIds, int rowNumber,
            final Map<String, Collection<String>> entityTypeExportPropertiesMap,
            final XLSExport.TextFormatting textFormatting)
    {
        final Collection<Project> projects = getProjects(api, sessionToken, permIds);
        final Collection<String> warnings = new ArrayList<>();

        warnings.addAll(addRow(rowNumber++, true, ExportableKind.PROJECT, null, "PROJECT"));
        warnings.addAll(addRow(rowNumber++, true, ExportableKind.PROJECT, null, "Identifier", "Code", "Description",
                "Space"));

        for (final Project project : projects)
        {
            warnings.addAll(addRow(rowNumber++, false, ExportableKind.PROJECT, project.getIdentifier().getIdentifier(),
                    project.getIdentifier().getIdentifier(), project.getCode(), project.getDescription(),
                    project.getSpace().getCode()));
        }

        return new AdditionResult(rowNumber + 1, warnings);
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

}
