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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMetaprojectAssignments;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMetaprojectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;

/**
 * @author Jakub Straszewski
 */
public class MetaprojectAssignments implements IMetaprojectAssignments
{
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments metaprojectAssignments;

    protected IEncapsulatedBasicOpenBISService service;

    public MetaprojectAssignments(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments metaprojectAssignments,
            IEncapsulatedBasicOpenBISService service)
    {
        this.metaprojectAssignments = metaprojectAssignments;
        this.service = service;
    }

    @Override
    public IMetaprojectImmutable getMetaproject()
    {
        return new MetaprojectImmutable(metaprojectAssignments.getMetaproject());
    }

    @Override
    public List<ISampleImmutable> getSamples()
    {
        LinkedList<ISampleImmutable> samples = new LinkedList<ISampleImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample : metaprojectAssignments
                .getSamples())
        {
            samples.add(new SampleImmutable(sample));
        }
        return samples;
    }

    @Override
    public List<IExperimentImmutable> getExperiments()
    {
        LinkedList<IExperimentImmutable> experiments = new LinkedList<IExperimentImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment : metaprojectAssignments
                .getExperiments())
        {
            experiments.add(new ExperimentImmutable(experiment));
        }
        return experiments;
    }

    @Override
    public List<IDataSetImmutable> getDataSets()
    {
        LinkedList<IDataSetImmutable> experiments = new LinkedList<IDataSetImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData dataSet : metaprojectAssignments
                .getDataSets())
        {
            experiments.add(new DataSetImmutable(dataSet, service));
        }
        return experiments;
    }

    @Override
    public List<IMaterialImmutable> getMaterials()
    {
        LinkedList<IMaterialImmutable> materials = new LinkedList<IMaterialImmutable>();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material material : metaprojectAssignments
                .getMaterials())
        {
            materials.add(new MaterialImmutable(material));
        }
        return materials;
    }

}
