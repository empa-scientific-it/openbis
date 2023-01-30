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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;

/**
 * Service interface for the <i>demo</i> <i>GWT</i> client.
 * 
 * @author Christian Ribeaud
 */
public interface IDemoClientServiceAsync extends IClientServiceAsync
{

    /** @see IDemoClientService#getSampleGenerationInfo(TechId, String) */
    public void getSampleGenerationInfo(final TechId sampleId, String baseIndexUrl,
            final AsyncCallback<SampleParentWithDerived> callback);

    /**
     * @see IDemoClientService#registerSample(String, NewSample)
     */
    public void registerSample(final String sessionKey, final NewSample sample,
            final AsyncCallback<Void> asyncCallback) throws UserFailureException;

    /**
     * @see IDemoClientService#getNumberOfExperiments()
     */
    public void getNumberOfExperiments(final AsyncCallback<Integer> asyncCallback)
            throws UserFailureException;
}
