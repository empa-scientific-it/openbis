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

package ch.systemsx.cisd.openbis.generic.server.plugin;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * The slave server plug-in for a <i>Sample Type</i>.
 * <p>
 * The implementation will give access to {@link DAOFactory} and appropriate business object factory. Each method specified here must start with
 * {@link Session} parameter.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface ISampleTypeSlaveServerPlugin
{
    /**
     * For given {@link SamplePE} returns the {@link SampleParentWithDerivedDTO}.
     */
    SampleParentWithDerivedDTO getSampleInfo(final Session session, final SamplePE sample)
            throws UserFailureException;

    /**
     * Registers given list of {@link NewSample NewSamples}.
     */
    void registerSamples(final Session session, final List<NewSample> newSamples,
            PersonPE registratorOrNUll) throws UserFailureException;

    /**
     * Updates given list of samples.
     */
    void updateSamples(Session session, List<SampleBatchUpdatesDTO> convertSamples);

}
