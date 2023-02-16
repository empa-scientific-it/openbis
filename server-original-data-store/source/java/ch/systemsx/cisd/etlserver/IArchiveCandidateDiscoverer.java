/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;

/**
 * Finds data sets that are possible candidates for archiving The implementing class must have a constructor accepting single parameter of type
 * {@link java.util.Properties}
 * 
 * @author Sascha Fedorenko
 */
public interface IArchiveCandidateDiscoverer
{
    /**
     * Return a list of data sets that can be scheduled for archiving. This will be called periodically so there's no need to return everything in one
     * list. First best subset is sufficient, make sure though that the older data is returned first.
     * 
     * @param openbis an interface to search data sets with
     * @param criteria general time and type criteria to start with
     * @return list of data sets that the auto archiver can process
     */
    List<AbstractExternalData> findDatasetsForArchiving(IEncapsulatedOpenBISService openbis, ArchiverDataSetCriteria criteria);
}
