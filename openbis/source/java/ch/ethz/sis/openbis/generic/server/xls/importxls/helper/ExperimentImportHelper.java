package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.server.xls.importxls.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.PropertyTypeSearcher;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.xls.importxls.utils.PropertyTypeSearcher.getPropertyValue;

public class ExperimentImportHelper extends BasicImportHelper
{
    private EntityTypePermId entityTypePermId;

    private final ImportOptions options;

    private final DelayedExecutionDecorator delayedExecutor;

    private PropertyTypeSearcher propertyTypeSearcher;

    private static final Set<String> experimentAttributes = new HashSet<>(Arrays.asList("$", "Identifier", "Code", "Project"));

    public ExperimentImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode);
        this.options = options;
        this.delayedExecutor = delayedExecutor;
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        int lineIndex = start;

        try
        {
            Map<String, Integer> header = parseHeader(page.get(lineIndex), false);
            lineIndex++;

            String experimentType = getValueByColumnName(header, page.get(lineIndex), "Experiment type");
            entityTypePermId = new EntityTypePermId(experimentType);

            // first check that experiment type exist.
            ExperimentTypeFetchOptions fetchTypeOptions = new ExperimentTypeFetchOptions();
            fetchTypeOptions.withPropertyAssignments().withPropertyType().withVocabulary().withTerms();
            ExperimentType type = delayedExecutor.getExperimentType(entityTypePermId, fetchTypeOptions);
            if (type == null)
            {
                throw new UserFailureException("Experiment type " + experimentType + " is not exist.");
            }
            this.propertyTypeSearcher = new PropertyTypeSearcher(type.getPropertyAssignments());

            lineIndex++;
        } catch (Exception e)
        {
            throw new UserFailureException("Exception at page " + pageIndex + " and line " + lineIndex + " with message: " + e.getMessage());
        }

        // and then import experiments
        super.importBlock(page, pageIndex, lineIndex, end);
    }

    @Override protected String getTypeName()
    {
        return "Experiment";
    }

    private ExperimentIdentifier getIdentifier(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, "Code");
        String project = getValueByColumnName(header, values, "Project");
        return new ExperimentIdentifier(project + "/" + code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();

        return delayedExecutor.getExperiment(getIdentifier(header, values), fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        ExperimentCreation creation = new ExperimentCreation();

        String code = getValueByColumnName(header, values, "Code");
        String project = getValueByColumnName(header, values, "Project");

        if (options.getDisallowEntityCreations())
        {
            throw new UserFailureException("Entity creations disallowed but found at line: " + line + " [" + getTypeName() + "]");
        }

        creation.setTypeId(entityTypePermId);
        creation.setCode(code);
        creation.setProjectId(new ProjectIdentifier(project));

        for (String key : header.keySet())
        {
            if (!experimentAttributes.contains(key))
            {
                String value = getValueByColumnName(header, values, key);
                PropertyType propertyType = propertyTypeSearcher.findPropertyType(key);
                creation.setProperty(propertyType.getCode(), getPropertyValue(propertyType, value));
            }
        }

        delayedExecutor.createExperiment(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String identifier = getValueByColumnName(header, values, "Identifier");
        String project = getValueByColumnName(header, values, "Project");

        if (identifier == null || identifier.isEmpty())
        {
            throw new UserFailureException("'Identifier' is missing, is mandatory since is needed to select a experiment to update.");
        }

        ExperimentUpdate update = new ExperimentUpdate();
        IExperimentId experimentId = new ExperimentIdentifier(identifier);
        update.setExperimentId(experimentId);

        // Project is used to "MOVE", only set if present since can't be null
        if (project != null && !project.isEmpty())
        {
            update.setProjectId(new ProjectIdentifier(project));
        }
        Map<String, String> properties = new HashMap<>();
        for (String key : header.keySet())
        {
            if (!experimentAttributes.contains(key))
            {
                String value = getValueByColumnName(header, values, key);
                PropertyType propertyType = propertyTypeSearcher.findPropertyType(key);
                properties.put(propertyType.getCode(), getPropertyValue(propertyType, value));
            }
        }
        update.setProperties(properties);
        delayedExecutor.updateExperiment(update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        checkKeyExistence(header, "Code");
        checkKeyExistence(header, "Project");
    }
}
