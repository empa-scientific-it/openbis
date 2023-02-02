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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations;

import java.io.Serializable;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;

/**
 * Describes image transformation, contains user friendly label and description.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformation implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    // can be null
    private String description;

    private boolean isDefault;

    private final IImageTransformerFactory imageTransformerFactory;

    private final boolean isEditable;

    public ImageTransformation(String code, String label, String description,
            IImageTransformerFactory imageTransformerFactory)
    {
        assert code != null : "code is null";
        assert label != null : " label is null";
        assert imageTransformerFactory != null : "imageTransformerFactory is null";

        this.code = CodeNormalizer.normalize(code);
        this.label = label;
        this.description = description;
        this.isDefault = false;
        this.imageTransformerFactory = imageTransformerFactory;
        this.isEditable = false; // will be used later for ImageViewer transformations
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

    public boolean isDefault()
    {
        return isDefault;
    }

    public IImageTransformerFactory getImageTransformerFactory()
    {
        return imageTransformerFactory;
    }

    public boolean isEditable()
    {
        return isEditable;
    }

    // ----------- setters

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Calling with true makes this transformation a default user's choice and makes the 'hard-coded' default unavailable. This transformation will
     * become the first one on the list automatically.
     * <p>
     * Marking more then one transformation as a default for one channel will make it impossible to register a dataset.
     * </p>
     * <p>
     * If no transformation on the list will be marked as default then a 'hard-coded' default transformation will become available.
     * </p>
     */
    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ImageTransformation [code=" + code + ", label=" + label);
        if (description != null)
        {
            sb.append(", description=" + description);
        }
        sb.append(", isDefault=" + isDefault + "]");
        return sb.toString();
    }
}
