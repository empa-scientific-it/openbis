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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Code, label and description of the image transformation.
 * 
 * @author Tomasz Pylak
 */
public class InternalImageTransformationInfo implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    // can be null
    private String description;

    private String transformationSignature;

    private boolean isDefault;

    // GWT only
    @SuppressWarnings("unused")
    private InternalImageTransformationInfo()
    {
    }

    public InternalImageTransformationInfo(String code, String label, String description,
            String transformationSignature, boolean isDefault)
    {
        assert code != null : "code is null";
        assert label != null : " label is null";

        this.code = code;
        this.label = label;
        this.description = description;
        this.transformationSignature = transformationSignature;
        this.isDefault = isDefault;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public String getDescription()
    {
        return description;
    }

    public String getTransformationSignature()
    {
        return transformationSignature;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof InternalImageTransformationInfo)
        {
            InternalImageTransformationInfo other = (InternalImageTransformationInfo) o;
            return other.code.equals(other.code);
        }

        return false;
    }
}
