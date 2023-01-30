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

package ch.systemsx.cisd.openbis.uitest.rmi.eager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.uitest.rmi.lazy.ExperimentLazy;
import ch.systemsx.cisd.openbis.uitest.rmi.lazy.SampleTypeLazy;
import ch.systemsx.cisd.openbis.uitest.rmi.lazy.SpaceLazy;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;

/**
 * @author anttil
 */
public class SampleRmi extends Sample
{

    private ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample;

    private final String session;

    private final ICommonServer commonServer;

    public SampleRmi(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample sample,
            String session, ICommonServer commonServer)
    {
        this.sample = sample;
        this.session = session;
        this.commonServer = commonServer;
    }

    @Override
    public String getCode()
    {
        return sample.getCode();
    }

    @Override
    public SampleType getType()
    {
        return new SampleTypeLazy(sample.getSampleTypeCode(), session, commonServer);
    }

    @Override
    public Experiment getExperiment()
    {
        if (sample.getExperimentIdentifierOrNull() != null)
        {
            return new ExperimentLazy(sample.getExperimentIdentifierOrNull(), session, commonServer);
        } else
        {
            return null;
        }
    }

    @Override
    public Space getSpace()
    {
        return new SpaceLazy(sample.getSpaceCode(), session, commonServer);
    }

    @Override
    public Collection<Sample> getParents()
    {
        Set<Sample> parents = new HashSet<Sample>();
        for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample parent : sample.getParents())
        {
            parents.add(new SampleRmi(parent, session, commonServer));
        }
        return parents;
    }

    @Override
    public Map<PropertyType, Object> getProperties()
    {
        throw new UnsupportedOperationException("to be implemented");
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        Collection<MetaProject> metaProjects = new HashSet<MetaProject>();
        for (Metaproject m : sample.getMetaprojects())
        {
            metaProjects.add(new MetaProjectRmi(m));
        }
        return metaProjects;
    }

    @Override
    public Sample getContainer()
    {
        throw new UnsupportedOperationException("to be implemented");
    }
}
