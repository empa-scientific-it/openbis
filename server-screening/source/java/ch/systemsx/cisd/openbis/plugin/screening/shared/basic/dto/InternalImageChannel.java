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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A channel in which the image has been acquired.
 * <p>
 * Each channel has its <code>code</code> which uniquely identifies it in one experiment or dataset.
 * 
 * @author Tomasz Pylak
 */
public class InternalImageChannel implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String code;

    private String label;

    private String description;

    private Integer wavelength;

    private List<InternalImageTransformationInfo> availableImageTransformations;

    // GWT only
    @SuppressWarnings("unused")
    private InternalImageChannel()
    {
    }

    public InternalImageChannel(String code, String label, String description, Integer wavelength,
            List<InternalImageTransformationInfo> availableImageTransformations)
    {
        assert code != null;
        // assert availableImageTransformations != null;

        this.code = code;
        this.label = label;
        this.description = description;
        this.wavelength = wavelength;
        this.availableImageTransformations = availableImageTransformations;
    }

    public String getCode()
    {
        return code;
    }

    public String tryGetDescription()
    {
        return description;
    }

    public Integer tryGetWavelength()
    {
        return wavelength;
    }

    public String getLabel()
    {
        return label;
    }

    public List<InternalImageTransformationInfo> getAvailableImageTransformations()
    {
        return availableImageTransformations;
    }

}
