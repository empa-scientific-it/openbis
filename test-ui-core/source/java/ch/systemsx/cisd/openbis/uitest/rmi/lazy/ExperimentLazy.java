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

package ch.systemsx.cisd.openbis.uitest.rmi.lazy;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public class ExperimentLazy extends Experiment
{
    private final String code;

    @SuppressWarnings("unused")
    private final String session;

    @SuppressWarnings("unused")
    private final ICommonServer commonServer;

    public ExperimentLazy(String code, String session, ICommonServer commonServer)
    {
        this.code = code;
        this.session = session;
        this.commonServer = commonServer;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public ExperimentType getType()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Project getProject()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Sample> getSamples()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<MetaProject> getMetaProjects()
    {
        throw new UnsupportedOperationException();
    }

}
