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
import java.util.List;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectAssignments implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Metaproject metaproject;

    private List<Experiment> experiments;

    private List<Sample> samples;

    private List<AbstractExternalData> dataSets;

    private List<Material> materials;

    public Metaproject getMetaproject()
    {
        return metaproject;
    }

    public void setMetaproject(Metaproject metaproject)
    {
        this.metaproject = metaproject;
    }

    public List<Experiment> getExperiments()
    {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments)
    {
        this.experiments = experiments;
    }

    public List<Sample> getSamples()
    {
        return samples;
    }

    public void setSamples(List<Sample> samples)
    {
        this.samples = samples;
    }

    public List<AbstractExternalData> getDataSets()
    {
        return dataSets;
    }

    public void setDataSets(List<AbstractExternalData> dataSets)
    {
        this.dataSets = dataSets;
    }

    public List<Material> getMaterials()
    {
        return materials;
    }

    public void setMaterials(List<Material> materials)
    {
        this.materials = materials;
    }
}
