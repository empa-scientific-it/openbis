package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProjectImportHelper extends BasicImportHelper
{
    private final ImportOptions options;

    private final DelayedExecutionDecorator delayedExecutor;

    public ProjectImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode);
        this.options = options;
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected String getTypeName()
    {
        return "Project";
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "Code");
        String space = getValueByColumnName(header, values, "Space");

        String normalizedCode = ImportUtils.valueNormalizer("Code", code, false);
        String normalizedSpace = ImportUtils.valueNormalizer("Space", space, false);

        final ProjectIdentifier projectIdentifier = new ProjectIdentifier(normalizedSpace, normalizedCode);
        return delayedExecutor.getProject(projectIdentifier, new ProjectFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, "Code");
        String space = getValueByColumnName(header, values, "Space");
        String description = getValueByColumnName(header, values, "Description");

        String normalizedCode = ImportUtils.valueNormalizer("Code", code, false);
        String normalizedSpace = ImportUtils.valueNormalizer("Space", space, false);

        if (options.getDisallowEntityCreations())
        {
            throw new UserFailureException("Entity creations disallowed but found at line: " + line + " [" + getTypeName() + "]");
        }

        ProjectCreation creation = new ProjectCreation();
        creation.setCode(normalizedCode);
        creation.setDescription(description);
        creation.setSpaceId(new SpacePermId(normalizedSpace));

        delayedExecutor.createProject(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String identifier = getValueByColumnName(header, values, "Identifier");
        String space = getValueByColumnName(header, values, "Space");
        String description = getValueByColumnName(header, values, "Description");

        if (identifier == null || identifier.isEmpty())
        {
            throw new UserFailureException("'Identifier' is missing, is mandatory since is needed to select a project to update.");
        }

        ImportUtils.projectIdentifierNormalizer(identifier);
        final ProjectIdentifier projectIdentifier = new ProjectIdentifier(identifier);

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectIdentifier);
        if (description != null)
        {
            update.setDescription(description);
        }

        // Space is only needed to "MOVE" the project
        if (space != null && !space.isEmpty())
        {
            String normalizedSpace = ImportUtils.valueNormalizer("Space", space, false);
            update.setSpaceId(new SpacePermId(normalizedSpace));
        }

        delayedExecutor.updateProject(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Code");
        checkKeyExistence(header, "Space");
    }
}
