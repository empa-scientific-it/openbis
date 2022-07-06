package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.List;
import java.util.Map;

public class ProjectImportHelper extends BasicImportHelper
{
    private enum Attribute implements IAttribute {
        Identifier("Identifier", false),
        Code("Code", true),
        Space("Space", true),
        Description("Description", false);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private final DelayedExecutionDecorator delayedExecutor;

    private final AttributeValidator<Attribute> attributeValidator;

    public ProjectImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode, options);
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.PROJECT;
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String identifier = getValueByColumnName(header, values, Attribute.Identifier);
        String code = getValueByColumnName(header, values, Attribute.Code);
        String space = getValueByColumnName(header, values, Attribute.Space);

        ProjectIdentifier projectIdentifier;
        if (identifier != null && !identifier.isEmpty()) {
            projectIdentifier = new ProjectIdentifier(identifier);
        } else {
            projectIdentifier = new ProjectIdentifier(space, code);
        }

        return delayedExecutor.getProject(projectIdentifier, new ProjectFetchOptions()) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String space = getValueByColumnName(header, values, Attribute.Space);
        String description = getValueByColumnName(header, values, Attribute.Description);

        ProjectCreation creation = new ProjectCreation();
        creation.setCode(code);
        creation.setDescription(description);
        creation.setSpaceId(new SpacePermId(space));

        delayedExecutor.createProject(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String identifier = getValueByColumnName(header, values, Attribute.Identifier);
        String space = getValueByColumnName(header, values, Attribute.Space);
        String description = getValueByColumnName(header, values, Attribute.Description);

        if (identifier == null || identifier.isEmpty())
        {
            throw new UserFailureException("'Identifier' is missing, is mandatory since is needed to select a project to update.");
        }

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
            update.setSpaceId(new SpacePermId(space));
        }

        delayedExecutor.updateProject(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        attributeValidator.validateHeaders(Attribute.values(), header);
    }
}
