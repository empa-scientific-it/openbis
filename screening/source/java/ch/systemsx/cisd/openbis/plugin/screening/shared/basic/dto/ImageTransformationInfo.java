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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Code, label and description of the image transformation.
 * 
 * @author Tomasz Pylak
 */
public class ImageTransformationInfo implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    // can be null
    private String description;

    private boolean isDefault;

    // GWT only
    @SuppressWarnings("unused")
    private ImageTransformationInfo()
    {
    }

    public ImageTransformationInfo(String code, String label, String description, boolean isDefault)
    {
        assert code != null : "code is null";
        assert label != null : " label is null";

        this.code = code;
        this.label = label;
        this.description = description;
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

    public boolean isDefault()
    {
        return isDefault;
    }
}
