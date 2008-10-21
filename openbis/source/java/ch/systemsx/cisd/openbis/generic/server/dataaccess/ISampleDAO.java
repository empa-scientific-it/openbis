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

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * An interface that contains all data access operations on {@link SamplePE}s.
 * 
 * @author Tomasz Pylak
 */
public interface ISampleDAO
{
    List<SamplePE> listSamplesByTypeAndGroup(final SampleTypePE sampleType, final GroupPE group)
            throws DataAccessException;

    List<SamplePE> listSamplesByTypeAndDatabaseInstance(final SampleTypePE sampleType,
            final DatabaseInstancePE databaseInstance);

    void createSample(final SamplePE sample) throws DataAccessException;

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given <var>databaseInstance</var>.
     */
    SamplePE tryFindByCodeAndDatabaseInstance(final String sampleCode,
            final DatabaseInstancePE databaseInstance);

    /**
     * Returns the sample specified by given <var>sampleCode</var> and given <var>group</var>.
     */
    SamplePE tryFindByCodeAndGroup(final String sampleCode, final GroupPE group);

    /**
     * For given <var>sample</var> returns all {@link SamplePE}s that are generated from it.
     */
    List<SamplePE> listSampleByGeneratedFrom(final SamplePE sample);
}
