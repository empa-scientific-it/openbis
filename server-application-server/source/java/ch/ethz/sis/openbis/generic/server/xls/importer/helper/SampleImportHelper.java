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
package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

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
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.IdentifierVariable;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.ImportUtils;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.VARIABLE_PREFIX;
import static ch.ethz.sis.openbis.generic.server.xls.importer.utils.PropertyTypeSearcher.getPropertyValue;

public class SampleImportHelper extends BasicImportHelper
{
    private static final String SAMPLE_TYPE_FIELD = "Sample type";

    public enum Attribute implements IAttribute {
        $("$", false),
        Identifier("Identifier", false),
        Code("Code", false),
        Space("Space", false),
        Project("Project", false),
        Experiment("Experiment", false),
        AutoGenerateCode("Auto generate code", false),
        Parents("Parents", false),
        Children("Children", false);

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

    private EntityTypePermId sampleType;

    private final DelayedExecutionDecorator delayedExecutor;

    private PropertyTypeSearcher propertyTypeSearcher;

    private final AttributeValidator<Attribute> attributeValidator;

    public SampleImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
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
            AttributeValidator.validateHeader(SAMPLE_TYPE_FIELD, header);
            lineIndex++;

            sampleType = new EntityTypePermId(getValueByColumnName(header, page.get(lineIndex), SAMPLE_TYPE_FIELD));
            if(sampleType.getPermId() == null || sampleType.getPermId().isEmpty()) {
                throw new UserFailureException("Mandatory field missing or empty: " + SAMPLE_TYPE_FIELD);
            }

            // first check that sample type exist.
            SampleTypeFetchOptions fetchTypeOptions = new SampleTypeFetchOptions();
            fetchTypeOptions.withPropertyAssignments().withPropertyType().withVocabulary().withTerms();
            SampleType type = delayedExecutor.getSampleType(sampleType, fetchTypeOptions);
            if (type == null)
            {
                throw new UserFailureException("Sample type " + sampleType + " not found.");
            }
            this.propertyTypeSearcher = new PropertyTypeSearcher(type.getPropertyAssignments());

            lineIndex++;
        } catch (Exception e)
        {
            throw new UserFailureException("sheet: " + (pageIndex + 1) + " line: " + (lineIndex + 1) + " message: " + e.getMessage());
        }

        // and then import samples
        super.importBlock(page, pageIndex, start + 2, end);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.SAMPLE;
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withType();
        fetchOptions.withChildren();
        fetchOptions.withParents();
        fetchOptions.withProperties();

        String identifier = getValueByColumnName(header, values, Attribute.Identifier); // Only used for updates
        String code = getValueByColumnName(header, values, Attribute.Code);
        String space = getValueByColumnName(header, values, Attribute.Space);
        String project = getValueByColumnName(header, values, Attribute.Project);

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

        String variable = getValueByColumnName(header, values, Attribute.$);
        String code = getValueByColumnName(header, values, Attribute.Code);
        String autoGenerateCode = getValueByColumnName(header, values, Attribute.AutoGenerateCode);
        String space = getValueByColumnName(header, values, Attribute.Space);
        String project = getValueByColumnName(header, values, Attribute.Project);
        String experiment = getValueByColumnName(header, values, Attribute.Experiment);
        String parents = getValueByColumnName(header, values, Attribute.Parents);
        String children = getValueByColumnName(header, values, Attribute.Children);

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
            if (!attributeValidator.isHeader(key))
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
        String variable = getValueByColumnName(header, values, Attribute.$);
        String identifier = getValueByColumnName(header, values, Attribute.Identifier);
        String space = getValueByColumnName(header, values, Attribute.Space);
        String project = getValueByColumnName(header, values, Attribute.Project);
        String experiment = getValueByColumnName(header, values, Attribute.Experiment);
        String parents = getValueByColumnName(header, values, Attribute.Parents);
        String children = getValueByColumnName(header, values, Attribute.Children);

        if (variable != null && !variable.isEmpty() && !variable.startsWith(VARIABLE_PREFIX))
        {
            throw new UserFailureException("Variables should start with " + VARIABLE_PREFIX);
        }

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

        if (parents == null || parents.isEmpty())
        {
            // Skip empty values to avoid deleting by mistake
        } else
        {
            Set<SampleIdentifier> parentIds = new HashSet<>();
            if (parents.equals("--DELETE--") || parents.equals("__DELETE__"))
            {
                // Delete missing = all
            } else // Delete missing
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
        }

        if (children == null || children.isEmpty())
        {
            // Skip empty values to avoid deleting by mistake
        } else
        {
            Set<SampleIdentifier> childrenIds = new HashSet<>();
            if (children.equals("--DELETE--") || children.equals("__DELETE__"))
            {
                // Delete missing = all
            } else // Delete missing
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
        }

        // End - Special case -> Remove parents / children & Special case -> Sample Variables

        for (String key : header.keySet())
        {
            if (!attributeValidator.isHeader(key))
            {
                String value = getValueByColumnName(header, values, key);
                if (value == null || value.isEmpty()) { // Skip empty values to avoid deleting by mistake
                    continue;
                } else if (value.equals("--DELETE--") || value.equals("__DELETE__")) // Do explicit delete
                {
                    value = null;
                } else { // Normal behaviour, set value
                }
                PropertyType propertyType = propertyTypeSearcher.findPropertyType(key);
                update.setProperty(propertyType.getCode(), getPropertyValue(propertyType, value));
            }
        }

        delayedExecutor.updateSample(variable, update, page, line);
    }

    @Override protected void validateHeader(Map<String, Integer> header)
    {
        attributeValidator.validateHeaders(Attribute.values(), propertyTypeSearcher, header);
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
