package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.IdentifierVariable;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

import java.util.UUID;

public class ImportUtils
{
    private static final boolean isProjectSamplesEnabled;

    static {
        isProjectSamplesEnabled = CommonServiceProvider.getCommonServer().isProjectSamplesEnabled(null);
    }

    public static boolean isInternalNamespace(String property)
    {
        return property.startsWith("$");
    }

    public static ISampleId buildSampleIdentifier(String identifier)
    {
        if (identifier == null || identifier.isEmpty())
        {
            return null;
        }

        if (isProjectSamplesEnabled == false) // If a project code is found => remove it
        {
            String[] identifierParts = identifier.split("/");
            if (identifierParts.length == 4) {
                String spaceCode = identifierParts[1];
                String projectCode = identifierParts[2];
                String sampleCode = identifierParts[3];
                identifier = "/" + spaceCode + "/" + sampleCode;
            }
        }
        return new SampleIdentifier(identifier);

    }

    public static ISampleId buildSampleIdentifier(String code, String space, String project)
    {
        if (code == null || code.isEmpty())
        {
            return null;
        }

        if (project != null && !project.trim().isEmpty())
        {
            project = project.split("/")[2];
        } else
        {
            project = null;
        }

        if (isProjectSamplesEnabled == false) // If a project code is found => remove it
        {
            project = null;
        }
        return new SampleIdentifier(space, project, null, code);
    }

    public static ISampleId buildSampleIdentifier(String variable, SampleCreation sampleCreation)
    {
        if (sampleCreation.getCode() == null)
        {
            if (variable != null)
            {
                return new IdentifierVariable(variable);
            } else
            {
                return new IdentifierVariable(UUID.randomUUID().toString());
            }
        } else
        {
            String space = null;
            ISpaceId spaceId = sampleCreation.getSpaceId();
            if (spaceId != null)
            {
                space = ((SpacePermId) spaceId).getPermId();
            }
            String project = null;
            IProjectId projectId = sampleCreation.getProjectId();
            if (projectId != null)
            {
                project = ((ProjectIdentifier) projectId).getIdentifier().split("/")[1];
            }

            if (isProjectSamplesEnabled == false) // If a project code is found => remove it
            {
                project = null;
            }
            return new SampleIdentifier(space, project, null, sampleCreation.getCode());
        }
    }

    public static String getScriptName(String code, String pathToScript)
    {
        if (pathToScript.contains("/"))
        {
            pathToScript = pathToScript.substring(pathToScript.lastIndexOf("/") + 1);
        }
        if (pathToScript.contains("."))
        {
            pathToScript = pathToScript.substring(0, pathToScript.lastIndexOf("."));
        }
        return code + "." + pathToScript;
    }
}
