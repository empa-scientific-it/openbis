/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * An interface that contains all data access operations on {@link SampleTypePE}s.
 * 
 * @author Christian Ribeaud
 */
public interface ISampleTypeDAO extends IGenericDAO<SampleTypePE>
{
    /**
     * Returns a list of {@link SampleTypePE} which have their property types and corresponding vocabulary terms eagerly fetched (overriding default
     * behavior which lazily loads the property types).
     * 
     * @return The list of {@link SampleTypePE}s registered in the database.
     */
    public List<SampleTypePE> listSampleTypes() throws DataAccessException;

    /**
     * For given <var>code</var> returns corresponding {@link SampleTypePE}.
     * 
     * @return <code>null</code> if no type found for given <var>code</var>.
     */
    public SampleTypePE tryFindSampleTypeByCode(final String code) throws DataAccessException;

    /**
     * For given example <var>sampleType</var> returns corresponding {@link SampleTypePE}.
     * 
     * @return <code>null</code> if no type found for given <var>sampleType</var>.
     */
    public SampleTypePE tryFindSampleTypeByExample(final SampleTypePE sampleType)
            throws DataAccessException;
}
