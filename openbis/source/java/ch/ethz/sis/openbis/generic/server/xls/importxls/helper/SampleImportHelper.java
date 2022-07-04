package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.xls.importxls.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.IdentifierVariable;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.PropertyTypeSearcher;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.xls.importxls.utils.PropertyTypeSearcher.VARIABLE_PREFIX;
import static ch.ethz.sis.openbis.generic.server.xls.importxls.utils.PropertyTypeSearcher.getPropertyValue;

public class SampleImportHelper extends BasicImportHelper
{
    private EntityTypePermId sampleType;

    private final ImportOptions options;

    private final DelayedExecutionDecorator delayedExecutor;

    private PropertyTypeSearcher propertyTypeSearcher;

    private static final Set<String> sampleAttributes = new HashSet<>(Arrays.asList("$", "Identifier", "Code", "Space", "Project",
            "Experiment", "Auto generate code", "Parents", "Children"));

    public SampleImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
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

            sampleType = new EntityTypePermId(getValueByColumnName(header, page.get(lineIndex), "Sample type"));

            // first check that sample type exist.
            SampleTypeFetchOptions fetchTypeOptions = new SampleTypeFetchOptions();
            fetchTypeOptions.withPropertyAssignments().withPropertyType().withVocabulary().withTerms();
            SampleType type = delayedExecutor.getSampleType(sampleType, fetchTypeOptions);
            if (type == null)
            {
                throw new UserFailureException("Sample type " + sampleType + " is not exist.");
            }
            this.propertyTypeSearcher = new PropertyTypeSearcher(type.getPropertyAssignments());

            lineIndex++;
        } catch (Exception e)
        {
            throw new UserFailureException("Exception at page " + pageIndex + " and line " + lineIndex + " with message: " + e.getMessage());
        }

        // and then import samples
        super.importBlock(page, pageIndex, start + 2, end);
    }

    @Override protected String getTypeName()
    {
        return "sample";
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        fetchOptions.withProperties();

        String code = getValueByColumnName(header, values, "Code");
        String space = getValueByColumnName(header, values, "Space");
        String project = getValueByColumnName(header, values, "Project");
        String identifier = getValueByColumnName(header, values, "Identifier"); // Only used for updates

        ISampleId sampleId;
        if (identifier != null && !identifier.isEmpty()) {
            sampleId = ImportUtils.buildSampleIdentifier(identifier);
        } else {
            sampleId = ImportUtils.buildSampleIdentifier(code, space, project);
        }

        if (sampleId == null) // If sample codes are autogenerated they can't be known at the time of the creation. Is then a creation.
        {
            return false;
        } else
        {
            return delayedExecutor.getSample(sampleId, fetchOptions) != null;
        }
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        SampleCreation creation = new SampleCreation();
        creation.setTypeId(sampleType);

        String code = getValueByColumnName(header, values, "Code");
        String variable = getValueByColumnName(header, values, "$");
        String autoGenerateCode = getValueByColumnName(header, values, "Auto generate code");
        String space = getValueByColumnName(header, values, "Space");
        String project = getValueByColumnName(header, values, "Project");
        String experiment = getValueByColumnName(header, values, "Experiment");
        String parents = getValueByColumnName(header, values, "Parents");
        String children = getValueByColumnName(header, values, "Children");

        if (variable != null && !variable.isEmpty() && !variable.startsWith(VARIABLE_PREFIX))
        {
            throw new UserFailureException("Variables should start with " + VARIABLE_PREFIX);
        }
        if (code != null && !code.isEmpty())
        {
            creation.setCode(code);
        }
        if (autoGenerateCode != null && !autoGenerateCode.isEmpty())
        {
            creation.setAutoGeneratedCode(Boolean.parseBoolean(autoGenerateCode));
        }
        if (space != null && !space.isEmpty())
        {
            creation.setSpaceId(new SpacePermId(space));
        }
        if (project != null && !project.isEmpty() && options.getAllowProjectSamples()) // Projects can only be set in project samples are enabled
        {
            creation.setProjectId(new ProjectIdentifier(project));
        }
        if (experiment != null && !experiment.isEmpty())
        {
            creation.setExperimentId(new ExperimentIdentifier(experiment));
        }
        injectOwner(creation);

        // Start - Special case - Sample Variables
        if (parents != null && !parents.isEmpty())
        {
            List<ISampleId> parentIds = new ArrayList<>();
            for (String parent : parents.split("\n"))
            {
                if (parent.startsWith(VARIABLE_PREFIX))
                {
                    parentIds.add(new IdentifierVariable(parent));
                } else
                {
                    parentIds.add(new SampleIdentifier(parent));
                }
            }
            creation.setParentIds(parentIds);
        }
        if (children != null && !children.isEmpty())
        {
            List<ISampleId> childrenIds = new ArrayList<>();
            for (String child : children.split("\n"))
            {
                if (child.startsWith(VARIABLE_PREFIX))
                {
                    childrenIds.add(new IdentifierVariable(child));
                } else
                {
                    childrenIds.add(new SampleIdentifier(child));
                }
            }
            creation.setChildIds(childrenIds);
        }
        // End - Special case - Sample Variables

        for (String key : header.keySet())
        {
            if (!sampleAttributes.contains(key))
            {
                String value = getValueByColumnName(header, values, key);
                PropertyType propertyType = propertyTypeSearcher.findPropertyType(key);
                creation.setProperty(propertyType.getCode(), getPropertyValue(propertyType, value));
            }
        }

        delayedExecutor.createSample(variable, creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String identifier = getValueByColumnName(header, values, "Identifier");
        String variable = getValueByColumnName(header, values, "$");
        String space = getValueByColumnName(header, values, "Space");
        String project = getValueByColumnName(header, values, "Project");
        String experiment = getValueByColumnName(header, values, "Experiment");
        String parents = getValueByColumnName(header, values, "Parents");
        String children = getValueByColumnName(header, values, "Children");

        if (identifier == null || identifier.isEmpty()) {
            throw new UserFailureException("'Identifier' is missing, is mandatory since is needed to select a sample to update.");
        }

        ISampleId sampleId = ImportUtils.buildSampleIdentifier(identifier);

        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        fetchOptions.withParents();

        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sampleId);

        // Space, project and experiment are used to "MOVE", only set if present since all values can't be null
        if (space != null && !space.isEmpty())
        {
            update.setSpaceId(new SpacePermId(space));
        }
        if (project != null && !project.isEmpty())
        {
            update.setProjectId(new ProjectIdentifier(project));
        }
        if (experiment != null && !experiment.isEmpty())
        {
            update.setExperimentId(new ExperimentIdentifier(experiment));
        }

        // Start - Special case -> Remove parents / children & Special case -> Sample Variables
        Sample originSample = delayedExecutor.getSample(sampleId, fetchOptions);
        Set<SampleIdentifier> parentIds = new HashSet<>();
        if (parents != null && !parents.isEmpty())
        {
            for (String parent : parents.split("\n"))
            {
                if (parent.startsWith(VARIABLE_PREFIX))
                {
                    update.getParentIds().add(new IdentifierVariable(parent));
                } else
                {
                    SampleIdentifier parentId = new SampleIdentifier(parent);
                    update.getParentIds().add(parentId);
                    parentIds.add(parentId);
                }
            }
        }
        for (Sample parent : originSample.getParents())
        {
            if (!parentIds.contains(parent.getIdentifier()))
            {
                update.getParentIds().remove(parent.getIdentifier());
            }
        }

        Set<SampleIdentifier> childrenIds = new HashSet<>();
        if (children != null && !children.isEmpty())
        {
            for (String child : children.split("\n"))
            {
                if (child.startsWith(VARIABLE_PREFIX))
                {
                    update.getChildIds().add(new IdentifierVariable(child));
                } else
                {
                    SampleIdentifier childId = new SampleIdentifier(child);
                    update.getChildIds().add(childId);
                    childrenIds.add(childId);
                }
            }
        }
        for (Sample child : originSample.getChildren())
        {
            if (!childrenIds.contains(child.getIdentifier()))
            {
                update.getChildIds().remove(child.getIdentifier());
            }
        }
        // End - Special case -> Remove parents / children & Special case -> Sample Variables

        for (String key : header.keySet())
        {
            if (!sampleAttributes.contains(key))
            {
                String value = getValueByColumnName(header, values, key);
                if (value != null && (value.isEmpty() || value.equals("--DELETE--") || value.equals("__DELETE__")))
                {
                    value = null;
                }
                PropertyType propertyType = propertyTypeSearcher.findPropertyType(key);
                update.setProperty(propertyType.getCode(), getPropertyValue(propertyType, value));
            }
        }

        delayedExecutor.updateSample(variable, update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        // nothing to validate
    }

    private void injectOwner(SampleCreation creation)
    {
        String typePermId = ((EntityTypePermId) creation.getTypeId()).getPermId();
        if (options.getExperimentsByType() != null &&
                options.getExperimentsByType().containsKey(typePermId) &&
                options.getExperimentsByType().get(typePermId) != null)
        {
            String experimentIdentifier = options.getExperimentsByType().get(typePermId);
            creation.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
        }
        if (options.getSpacesByType() != null && options.getSpacesByType().containsKey(typePermId))
        {
            creation.setSpaceId(new SpacePermId(options.getSpacesByType().get(typePermId)));
        }
        if (creation.getExperimentId() != null && creation.getSpaceId() == null)
        {
            ExperimentIdentifier experimentIdentifier = (ExperimentIdentifier) creation.getExperimentId();
            creation.setSpaceId(new SpacePermId(experimentIdentifier.getIdentifier().split("/")[1]));
        }
        if (creation.getProjectId() != null && creation.getSpaceId() == null)
        {
            ProjectIdentifier projectIdentifier = (ProjectIdentifier) creation.getProjectId();
            creation.setSpaceId(new SpacePermId(projectIdentifier.getIdentifier().split("/")[1]));
        }
    }
}
