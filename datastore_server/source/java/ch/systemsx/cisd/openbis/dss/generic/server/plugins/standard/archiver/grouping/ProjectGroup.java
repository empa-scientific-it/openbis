package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.grouping;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

public class ProjectGroup extends Grouper<Project, ExperimentGroup>
{
    private static final long serialVersionUID = -1655936590485472113L;

    public ProjectGroup()
    {
        super(ExperimentGroup.class);
    }
}