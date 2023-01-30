/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.demo.shared;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;

/**
 * The <i>demo</i> server.
 * 
 * @author Christian Ribeaud
 */
public interface IDemoServer extends IServer
{
    /**
     * Returns number of experiments.
     */
    @Transactional
    public int getNumberOfExperiments(String sessionToken);

    /**
     * For given {@link TechId} returns the {@link Sample} and its derived (child) samples.
     * 
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample uniquely identified by given <var>sampleId</var>
     *             does not exist.
     */
    @Transactional(readOnly = true)
    public SampleParentWithDerived getSampleInfo(final String sessionToken, final TechId sampleId)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments);
}
