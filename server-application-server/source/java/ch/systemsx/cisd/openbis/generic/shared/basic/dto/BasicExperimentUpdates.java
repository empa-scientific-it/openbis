/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.List;

/**
 * @author Tomasz Pylak
 */
public class BasicExperimentUpdates implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int version;

    // ----- the data which should be changed:

    // new set of properties for the experiment, they will replace the old set
    private List<IEntityProperty> properties;

    // New set of sample codes which will replace the old ones. In this way some
    // samples can be unassigned and some assigned as a result. It will be assumed that
    // all the samples belong to the same group as the experiment.
    // If equals to null nothing should be changed.
    // If a sample currently assigned to a different experiment appears here, the exception is
    // thrown.
    // If some previously assigned sample is missing on this list, it will be unassigned if
    // if it does not have data sets attached yet. Otherwise an exception will be thrown.
    private String[] sampleCodesOrNull;

    // if true sampleCodesOrNull changes semantic - sample codes are the same as in newSamples and
    // all of them will be attached to the experiment.
    private boolean registerSamples;

    private List<NewSamplesWithTypes> newSamples;

    private String[] metaprojectsOrNull;

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    // if null nothing should be changed
    public String[] getSampleCodes()
    {
        return sampleCodesOrNull;
    }

    public void setSampleCodes(String[] sampleCodes)
    {
        this.sampleCodesOrNull = sampleCodes;
    }

    public void setNewSamples(List<NewSamplesWithTypes> list)
    {
        this.newSamples = list;
    }

    public void setRegisterSamples(boolean registerSamples)
    {
        this.registerSamples = registerSamples;
    }

    public List<NewSamplesWithTypes> getNewSamples()
    {
        return newSamples;
    }

    public boolean isRegisterSamples()
    {
        return registerSamples;
    }

    public String[] getMetaprojectsOrNull()
    {
        return metaprojectsOrNull;
    }

    public void setMetaprojectsOrNull(String[] metaprojectsOrNull)
    {
        this.metaprojectsOrNull = metaprojectsOrNull;
    }

}
