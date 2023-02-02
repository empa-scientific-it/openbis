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

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A class that represents an image transformation.
 * 
 * @since 1.9
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("ImageTransformationInfo")
public class ImageTransformationInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    @JsonProperty
    private String description;

    private boolean defaultTransformation;

    public ImageTransformationInfo(String code, String label, String description,
            boolean defaultTransformation)
    {
        this.code = code;
        this.label = label;
        this.description = description;
        this.defaultTransformation = defaultTransformation;
    }

    /**
     * The code of the image transformation.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * The (pretty-printed) label of the image transformation.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * The description of the image transformation, or <code>null</code>, if the image transformation has no description.
     */
    public String tryGetDescription()
    {
        return description;
    }

    /**
     * Returns <code>true</code>, if this transformation is the default transformation for this channel.
     */
    public boolean isDefaultTransformation()
    {
        return defaultTransformation;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
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
        ImageTransformationInfo other = (ImageTransformationInfo) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        } else if (code.equals(other.code) == false)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ImageTransformationInfo [code=" + code + ", label=" + label + ", description="
                + description + ", defaultTransformation=" + defaultTransformation + "]";
    }

    //
    // JSON-RPC
    //

    private ImageTransformationInfo()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setLabel(String label)
    {
        this.label = label;
    }

    private String getDescription()
    {
        return description;
    }

    private void setDescription(String description)
    {
        this.description = description;
    }

    private void setDefaultTransformation(boolean defaultTransformation)
    {
        this.defaultTransformation = defaultTransformation;
    }

}
