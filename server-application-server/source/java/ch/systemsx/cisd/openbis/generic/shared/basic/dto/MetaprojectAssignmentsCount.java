/*
 * Copyright 2012 ETH Zuerich, CISD
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

/**
 * @author pkupczyk
 */
public class MetaprojectAssignmentsCount implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Metaproject metaproject;

    private int experimentCount;

    private int sampleCount;

    private int dataSetCount;

    private int materialCount;

    public Metaproject getMetaproject()
    {
        return metaproject;
    }

    public void setMetaproject(Metaproject metaproject)
    {
        this.metaproject = metaproject;
    }

    public int getExperimentCount()
    {
        return experimentCount;
    }

    public void setExperimentCount(int experimentCount)
    {
        this.experimentCount = experimentCount;
    }

    public int getSampleCount()
    {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount)
    {
        this.sampleCount = sampleCount;
    }

    public int getDataSetCount()
    {
        return dataSetCount;
    }

    public void setDataSetCount(int dataSetCount)
    {
        this.dataSetCount = dataSetCount;
    }

    public int getMaterialCount()
    {
        return materialCount;
    }

    public void setMaterialCount(int materialCount)
    {
        this.materialCount = materialCount;
    }

}
