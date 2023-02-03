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

package ch.systemsx.cisd.imagereaders;

/**
 * Encapsulates various parameters for reading.
 * <p>
 * NOTE : Readers are not obliged to support all enumerated parameter fields. Consult the javadoc of
 * the field of interest to find out if your reader supports it.
 * 
 * @author Kaloyan Enimanev
 */
public class ReadParams implements IReadParams
{

    /**
     * When non-null value specified, the image reader will try to perform intensity rescaling for
     * this channel.
     * <p>
     * Only supported by BioFormat readers.
     */
    private Integer intensityRescalingChannel;

    public Integer getIntensityRescalingChannel()
    {
        return intensityRescalingChannel;
    }

    public void setIntensityRescalingChannel(Integer intensityRescalingChannel)
    {
        this.intensityRescalingChannel = intensityRescalingChannel;
    }

}
