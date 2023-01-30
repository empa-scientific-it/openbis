/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.common.parser.BeanProperty;

/**
 * Basic information about data set.
 * 
 * @author Izabela Adamczyk
 */
public class NewDataSet extends Code<NewDataSet> implements Comparable<NewDataSet>, IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SAMPLE = "sample";

    public static final String EXPERIMENT = "experiment";

    public static final String PARENTS = "parents";

    public static final String CONTAINER = "container";

    public static final String FILE_FORMAT = "file_format";

    private String sampleIdentifierOrNull;

    private String experimentIdentifier;

    private String[] parentsIdentifiersOrNull;

    private String containerIdentifierOrNull;

    private String fileFormatOrNull;

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    public NewDataSet()
    {
    }

    public String getSampleIdentifierOrNull()
    {
        return sampleIdentifierOrNull;
    }

    @BeanProperty(label = SAMPLE, optional = true)
    public void setSampleIdentifierOrNull(String sampleIdentifierOrNull)
    {
        this.sampleIdentifierOrNull = sampleIdentifierOrNull;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    @BeanProperty(label = EXPERIMENT, optional = true)
    public void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public String[] getParentsIdentifiersOrNull()
    {
        return parentsIdentifiersOrNull;
    }

    public void setParentsIdentifiersOrNull(String[] parentsIdentifiersOrNull)
    {
        this.parentsIdentifiersOrNull = parentsIdentifiersOrNull;
    }

    @BeanProperty(label = PARENTS, optional = true)
    public void setParentsIdentifiersOrNull(String parentsIdentifiersOrNull)
    {
        if (parentsIdentifiersOrNull != null)
        {
            String[] split = parentsIdentifiersOrNull.split(",");
            setParentsIdentifiersOrNull(split);
        } else
        {
            setParentsIdentifiersOrNull(new String[0]);
        }
    }

    public String getContainerIdentifierOrNull()
    {
        return containerIdentifierOrNull;
    }

    @BeanProperty(label = CONTAINER, optional = true)
    public void setContainerIdentifierOrNull(String containerIdentifierOrNull)
    {
        this.containerIdentifierOrNull = containerIdentifierOrNull;
    }

    public String getFileFormatOrNull()
    {
        return fileFormatOrNull;
    }

    @BeanProperty(label = FILE_FORMAT, optional = true)
    public void setFileFormatOrNull(String fileFormatOrNull)
    {
        this.fileFormatOrNull = fileFormatOrNull;
    }

    @Override
    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    @Override
    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

}
