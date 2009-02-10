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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;

/**
 * An interface that contains all data access operations on {@link ExternalDataPE}s.
 * 
 * @author Christian Ribeaud
 */
public interface IExternalDataDAO
{
    /**
     * List the {@link ExternalDataPE} for given <var>sample</var>.
     * 
     * @param sourceType the source type represented by given <var>sample</var>.
     * @returns list of {@link ExternalDataPE}s that are related to given {@link SamplePE}.
     */
    public List<ExternalDataPE> listExternalData(final SamplePE sample, final SourceType sourceType)
            throws DataAccessException;
    
    /**
     * Tries to get the data set for the specified code.
     */
    public ExternalDataPE tryToFindDataSetByCode(String dataSetCode);

    /**
     * Creates a unique data set code.
     */
    public String createDataSetCode();

    /**
     * Persists the specified data set.
     */
    public void createDataSet(ExternalDataPE dataset);

    /**
     * Updates the specified data set.
     */
    public void updateDataSet(ExternalDataPE dataset);
    
}
