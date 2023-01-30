/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * The slave server plug-in for a Data set type
 * <p>
 * The implementation will give access to {@link DAOFactory} and appropriate business object factory. Each method specified here must start with
 * {@link Session} parameter.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Franz-Josef Elmer
 */
public interface IDataSetTypeSlaveServerPlugin
{
    /**
     * Permanently deletes the specified data sets for the specified reason.
     * 
     * @deprecated this is legacy code and should be removed when we remove the option of disabled trash
     */
    @Deprecated
    public void permanentlyDeleteDataSets(Session session, List<DataPE> dataSets, String reason,
            boolean forceDisallowedTypes);

    /**
     * Updates properties of given data sets.
     */
    public void updateDataSets(Session session, List<DataSetBatchUpdatesDTO> dataSets);

}
