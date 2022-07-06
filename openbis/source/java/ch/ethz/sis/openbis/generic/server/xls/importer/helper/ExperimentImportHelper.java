package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

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
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.getPropertyValue;

public class ExperimentImportHelper extends BasicImportHelper
{
    private static final String EXPERIMENT_TYPE_FIELD = "Experiment type";

    private enum Attribute implements IAttribute {
        Identifier("Identifier", false),
        Code("Code", true),
        Project("Project", true);

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

    private EntityTypePermId entityTypePermId;

    private final DelayedExecutionDecorator delayedExecutor;

    private PropertyTypeSearcher propertyTypeSearcher;

    private final AttributeValidator<Attribute> attributeValidator;

    public ExperimentImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode, options);
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        int lineIndex = start;

        try
        {
            Map<String, Integer> header = parseHeader(page.get(lineIndex), false);
            lineIndex++;

            String experimentType = getValueByColumnName(header, page.get(lineIndex), EXPERIMENT_TYPE_FIELD);
            entityTypePermId = new EntityTypePermId(experimentType);

            // first check that experiment type exist.
            ExperimentTypeFetchOptions fetchTypeOptions = new ExperimentTypeFetchOptions();
            fetchTypeOptions.withPropertyAssignments().withPropertyType().withVocabulary().withTerms();
            ExperimentType type = delayedExecutor.getExperimentType(entityTypePermId, fetchTypeOptions);
            if (type == null)
            {
                throw new UserFailureException("Experiment type " + experimentType + " doesn't exist.");
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

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.EXPERIMENT;
    }

    private ExperimentIdentifier getIdentifier(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String project = getValueByColumnName(header, values, Attribute.Project);
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

        String code = getValueByColumnName(header, values, Attribute.Code);
        String project = getValueByColumnName(header, values, Attribute.Project);

        creation.setTypeId(entityTypePermId);
        creation.setCode(code);
        creation.setProjectId(new ProjectIdentifier(project));

        for (String key : header.keySet())
        {
            if (!attributeValidator.isHeader(key))
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
        String identifier = getValueByColumnName(header, values, Attribute.Identifier);
        String project = getValueByColumnName(header, values, Attribute.Project);

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
            if (!attributeValidator.isHeader(key))
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
        attributeValidator.validateHeaders(Attribute.values(), propertyTypeSearcher, header);
    }
}
