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
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.util.JsonPropertyUtil;

/**
 * An image representation format is made up of its dimensions, bit depth, and file format.
 * 
 * @since 1.10
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("ImageRepresentationFormat")
public class ImageRepresentationFormat implements Serializable
{
    @JsonObject("ImageRepresentationTransformation")
    public static class ImageRepresentationTransformation implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private long transformationId;

        private String transformationCode;

        private String channelCode;

        public ImageRepresentationTransformation(long transformationId, String transformationCode,
                String channelCode)
        {
            this.transformationId = transformationId;
            this.transformationCode = transformationCode;
            this.channelCode = channelCode;
        }

        @JsonIgnore
        public long getTransformationId()
        {
            return transformationId;
        }

        public String getTransformationCode()
        {
            return transformationCode;
        }

        public String getChannelCode()
        {
            return channelCode;
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

            ImageRepresentationTransformation other = (ImageRepresentationTransformation) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(channelCode, other.channelCode);
            builder.append(transformationId, other.transformationId);
            builder.append(transformationCode, other.transformationCode);
            return builder.isEquals();
        }

        @Override
        public String toString()
        {
            ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append(channelCode);
            builder.append(transformationId);
            builder.append(transformationCode);

            return builder.toString();
        }

        private ImageRepresentationTransformation()
        {
        }

        @JsonIgnore
        private void setTransformationId(long transformationId)
        {
            this.transformationId = transformationId;
        }

        @JsonProperty("transformationId")
        private String getTransformationIdAsString()
        {
            return JsonPropertyUtil.toStringOrNull(transformationId);
        }

        private void setTransformationIdAsString(String transformationId)
        {
            this.transformationId = JsonPropertyUtil.toLongOrNull(transformationId);
        }

        private void setTransformationCode(String transformationCode)
        {
            this.transformationCode = transformationCode;
        }

        private void setChannelCode(String channelCode)
        {
            this.channelCode = channelCode;
        }
    }

    private static final long serialVersionUID = 1L;

    private String dataSetCode;

    private long id;

    private boolean original;

    private Integer width;

    private Integer height;

    private Integer colorDepth;

    private String fileType;

    private List<ImageRepresentationTransformation> transformations;

    /**
     * Constructor.
     * 
     * @param dataSetCode
     * @param id
     * @param original
     * @param width
     * @param height
     * @param colorDepth
     * @param fileType
     * @param transformations
     */
    public ImageRepresentationFormat(String dataSetCode, long id, boolean original, Integer width,
            Integer height, Integer colorDepth, String fileType,
            List<ImageRepresentationTransformation> transformations)
    {
        super();
        this.dataSetCode = dataSetCode;
        this.id = id;
        this.original = original;
        this.width = width;
        this.height = height;
        this.colorDepth = colorDepth;
        this.fileType = fileType;
        this.transformations = new ArrayList<ImageRepresentationTransformation>();

        if (transformations != null)
        {
            this.transformations.addAll(transformations);
        }
    }

    /**
     * Returns the data set code to which this image representation format belongs.
     */
    public String getDataSetCode()
    {
        return dataSetCode;
    }

    /**
     * Return the ID of this image representation format.
     */
    @JsonIgnore
    public long getId()
    {
        return id;
    }

    /**
     * @return True if the format is the original format of the underlying image.
     */
    public boolean isOriginal()
    {
        return original;
    }

    /**
     * @return The width of the image.
     */
    public Integer getWidth()
    {
        return width;
    }

    /**
     * @return The height of the image.
     */
    public Integer getHeight()
    {
        return height;
    }

    /**
     * @return The number of bits of color of the image.
     */
    public Integer getColorDepth()
    {
        return colorDepth;
    }

    /**
     * @return The file type of the image.
     */
    public String getFileType()
    {
        return fileType;
    }

    public List<ImageRepresentationTransformation> getTransformations()
    {
        return transformations;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(original);
        builder.append(width);
        builder.append(height);
        builder.append(colorDepth);
        builder.append(fileType);
        return builder.toHashCode();
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

        ImageRepresentationFormat other = (ImageRepresentationFormat) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(original, other.original);
        builder.append(width, other.width);
        builder.append(height, other.height);
        builder.append(colorDepth, other.colorDepth);
        builder.append(fileType, other.fileType);
        builder.append(transformations, other.transformations);
        return builder.isEquals();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(original);
        builder.append(width);
        builder.append(height);
        builder.append(colorDepth);
        builder.append(fileType);
        builder.append(transformations);

        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private ImageRepresentationFormat()
    {
    }

    private void setDataSetCode(String dataSetCode)
    {
        this.dataSetCode = dataSetCode;
    }

    @JsonIgnore
    private void setId(long id)
    {
        this.id = id;
    }

    @JsonProperty("id")
    private String getIdAsString()
    {
        return JsonPropertyUtil.toStringOrNull(id);
    }

    private void setIdAsString(String id)
    {
        this.id = JsonPropertyUtil.toLongOrNull(id);
    }

    private void setOriginal(boolean original)
    {
        this.original = original;
    }

    private void setWidth(Integer width)
    {
        this.width = width;
    }

    private void setHeight(Integer height)
    {
        this.height = height;
    }

    private void setColorDepth(Integer colorDepth)
    {
        this.colorDepth = colorDepth;
    }

    private void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    private void setTransformations(List<ImageRepresentationTransformation> transformations)
    {
        this.transformations = transformations;
    }

}
