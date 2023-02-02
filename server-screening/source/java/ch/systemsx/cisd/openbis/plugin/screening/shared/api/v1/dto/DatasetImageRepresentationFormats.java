/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A bean that lists all image representation formats available for a data set.
 * 
 * @since 1.10
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("DatasetImageRepresentationFormats")
public class DatasetImageRepresentationFormats implements Serializable,
        Comparable<DatasetImageRepresentationFormats>
{
    private static final long serialVersionUID = 1L;

    private IDatasetIdentifier dataset;

    private ArrayList<ImageRepresentationFormat> imageRepresentationFormats;

    /**
     * Constructor.
     * 
     * @param dataSet
     * @param formats
     */
    public DatasetImageRepresentationFormats(IDatasetIdentifier dataSet,
            List<ImageRepresentationFormat> formats)
    {
        super();
        this.dataset = dataSet;
        this.imageRepresentationFormats = new ArrayList<ImageRepresentationFormat>(formats);
    }

    /**
     * The data set the formats relate to.
     */
    public IDatasetIdentifier getDataset()
    {
        return dataset;
    }

    /**
     * @return An immutable list containing the image representation formats for this data set.
     */
    public List<ImageRepresentationFormat> getImageRepresentationFormats()
    {
        return Collections.unmodifiableList(imageRepresentationFormats);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DatasetImageRepresentationFormats other = (DatasetImageRepresentationFormats) obj;
        if (dataset == null)
        {
            if (other.dataset != null)
            {
                return false;
            }
        } else if (dataset.equals(other.dataset) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(DatasetImageRepresentationFormats o)
    {
        return dataset.getDatasetCode().compareTo(o.getDataset().getDatasetCode());
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(dataset);
        builder.append(imageRepresentationFormats);
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private DatasetImageRepresentationFormats()
    {
    }

    private void setDataset(IDatasetIdentifier dataset)
    {
        this.dataset = dataset;
    }

    private void setImageRepresentationFormats(
            ArrayList<ImageRepresentationFormat> imageRepresentationFormats)
    {
        this.imageRepresentationFormats = imageRepresentationFormats;
    }

}
