package ch.ethz.sis.openbis.generic.server.xls.importxls.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.IdentifierVariable;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Locale;
import java.util.UUID;

public class ImportUtils
{
    public static boolean isInternalNamespace(String property)
    {
        return property.startsWith("$");
    }

    public static String valueNormalizer(String key, String value, boolean dollarPrefixAllowed)
    {
        if (value == null || value.isEmpty())
        {
            throw new UserFailureException(String.format("%s should not be empty.", key));
        }
        value = value.toUpperCase();
        if (value.startsWith("$") && !dollarPrefixAllowed)
        {
            throw new UserFailureException(String.format("%s starts with '$': %s", key, value));
        }
        if (!value.matches("\\$?[A-Z0-9_\\-.]+$"))
        {
            String leadingDollarTest = value.startsWith("$") ? " after the leasing $" : "";

            throw new UserFailureException(String.format("%s contains an invalid character. Only digits, letter, '-', '_', and '.' are allowed%s: %s",
                    key, leadingDollarTest, value));
        }
        return value;
    }

    public static String projectIdentifierNormalizer(String projectId)
    {
        if (projectId == null)
        {
            return projectId;
        }

        if (!projectId.matches("(/[A-Z0-9_\\-.]+){2}$"))
        {
            throw new UserFailureException("Invalid project identifier");
        }

        return projectId;
    }

    public static String experimentIdentifierNormalizer(String experimentId)
    {
        if (experimentId == null)
        {
            return experimentId;
        }

        if (!experimentId.matches("(/[A-Z0-9_\\-.]+){3}$"))
        {
            throw new UserFailureException("Invalid experiment identifier");
        }

        return experimentId;
    }

    public static String sampleIdentifierNormalizer(String identifier)
    {
        if (identifier == null)
        {
            return identifier;
        }

        if (!identifier.matches("(/[A-Z0-9_\\-.]+){1,3}(:[A-Z0-9_\\-.]+)?$"))
        {
            throw new UserFailureException("Invalid sample identifier");
        }

        return identifier;
    }

    public static ISampleId buildSampleIdentifier(String identifier)
    {
        if (identifier == null || identifier.isEmpty())
        {
            return null;
        }
        sampleIdentifierNormalizer(identifier); // To validate the sample identifier
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
            projectIdentifierNormalizer(project); // To validate the project identifier
            project = project.split("/")[2];
        } else
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
