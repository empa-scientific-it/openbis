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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A bean that provides information about a feature.
 * 
 * @since 1.9
 * @author Bernd Rinn
 */
@SuppressWarnings("unused")
@JsonObject("FeatureInformation")
public class FeatureInformation implements Serializable, Comparable<FeatureInformation>
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    public FeatureInformation(String code, String label, String description)
    {
        super();
        this.code = code;
        this.label = label;
        this.description = description;
    }

    private String description;

    /**
     * Returns the code of the feature.
     */
    public String getCode()
    {
        return code;
    }

    /**
     * Returns the label of the feature (pretty-print, but not guaranteed to be unique).
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Returns the text description of the feature.
     */
    public String getDescription()
    {
        return description;
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
        FeatureInformation other = (FeatureInformation) obj;
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
    public int compareTo(FeatureInformation o)
    {
        return code.compareTo(o.getCode());
    }

    @Override
    public String toString()
    {
        return "FeatureDescription [code=" + code + ", label=" + label + ", description="
                + description + "]";
    }

    //
    // JSON-RPC
    //

    private FeatureInformation()
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

    private void setDescription(String description)
    {
        this.description = description;
    }

}
