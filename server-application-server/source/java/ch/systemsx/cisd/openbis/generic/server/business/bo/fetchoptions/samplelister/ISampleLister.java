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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;

/**
 * Lister of {@link Sample} instances based on technical ids.
 * 
 * @author Franz-Josef Elmer
 */
public interface ISampleLister
{
    /**
     * Returns all samples of specified technical ids. Properties and related samples are added in accordance to the fetch options.
     * 
     * @param filter A filter which returns only samples and related samples which pass the filter.
     */
    public List<Sample> getSamples(Collection<Long> sampleIDs,
            EnumSet<SampleFetchOption> fetchOptions, IValidator<IIdentifierHolder> filter);
}
