/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.openbis.uitest.layout.DataSetTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ExperimentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ExperimentTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.layout.ProjectBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.PropertyTypeAssignmentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.PropertyTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ScriptBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SpaceBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.VocabularyBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.Browsable;
import ch.systemsx.cisd.openbis.uitest.page.Browser;

/**
 * @author anttil
 */
public class BrowsableWrapper implements Browsable
{
    private Collection<String> columns;

    private String idColumn;

    private String idValue;

    private Location<? extends Browser> browserLocation;

    public BrowsableWrapper(DataSetType dataSetType)
    {
        idColumn = "Code";
        idValue = dataSetType.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new DataSetTypeBrowserLocation();
    }

    public BrowsableWrapper(Experiment experiment)
    {
        idColumn = "Code";
        idValue = experiment.getCode();
        columns = Arrays.asList("Code", "Project", "Experiment Type");
        browserLocation = new ExperimentBrowserLocation();
    }

    public BrowsableWrapper(ExperimentType experimentType)
    {
        idColumn = "Code";
        idValue = experimentType.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new ExperimentTypeBrowserLocation();
    }

    public BrowsableWrapper(Project project)
    {
        idColumn = "Code";
        idValue = project.getCode();
        columns = Arrays.asList("Code", "Description", "Space");
        browserLocation = new ProjectBrowserLocation();
    }

    public BrowsableWrapper(PropertyType type)
    {
        idColumn = "Code";
        idValue = type.getCode();
        columns = Arrays.asList("Code", "Data Type", "Label", "Description", "Vocabulary");
        browserLocation = new PropertyTypeBrowserLocation();
    }

    public BrowsableWrapper(PropertyTypeAssignment assignment)
    {
        idColumn = "Property Type Code";
        idValue = assignment.getPropertyType().getCode();
        columns = Arrays.asList("Property Type Code", "Entity Type", "Mandatory?");
        browserLocation = new PropertyTypeAssignmentBrowserLocation();
    }

    public BrowsableWrapper(Sample sample)
    {
        idColumn = "Subcode";
        idValue = sample.getCode();
        columns = getColumns(sample);
        browserLocation = new SampleBrowserLocation();
    }

    public BrowsableWrapper(SampleType type)
    {
        idColumn = "Code";
        idValue = type.getCode();
        columns = Arrays.asList("Code", "Description", "Validation Plugin",
                "Listable?", "Show Container?", "Show Parents?", "Unique Subcodes",
                "Generate Codes Automatically", "Show Parent Metadata?", "Generated Code Prefix");
        browserLocation = new SampleTypeBrowserLocation();
    }

    public BrowsableWrapper(Script script)
    {
        idColumn = "Name";
        idValue = script.getName();
        columns = Arrays.asList("Name", "Description", "Entity Kind", "Plugin Type", "Script");
        browserLocation = new ScriptBrowserLocation();
    }

    public BrowsableWrapper(Space space)
    {
        idColumn = "Code";
        idValue = space.getCode();
        columns = Arrays.asList("Code", "Description");
        browserLocation = new SpaceBrowserLocation();
    }

    public BrowsableWrapper(Vocabulary vocabulary)
    {
        idColumn = "Code";
        idValue = vocabulary.getCode();
        columns = Arrays.asList("Code", "Description", "URL Template");
        browserLocation = new VocabularyBrowserLocation();
    }

    @Override
    public Collection<String> getColumns()
    {
        return this.columns;
    }

    @Override
    public String getIdColumn()
    {
        return this.idColumn;
    }

    @Override
    public String getIdValue()
    {
        return this.idValue;
    }

    @Override
    public Location<? extends Browser> getBrowserLocation()
    {
        return this.browserLocation;
    }

    private Collection<String> getColumns(Sample sample)
    {
        Collection<String> cols = new HashSet<String>();
        cols.addAll(Arrays.asList("Code", "Subcode", "Experiment", "Parents", "Space",
                "Sample Type", "Project", "Container"));
        for (PropertyTypeAssignment propertyTypeAssignment : sample.getType()
                .getPropertyTypeAssignments())
        {
            cols.add(propertyTypeAssignment.getPropertyType().getLabel());
        }
        return cols;
    }
}
