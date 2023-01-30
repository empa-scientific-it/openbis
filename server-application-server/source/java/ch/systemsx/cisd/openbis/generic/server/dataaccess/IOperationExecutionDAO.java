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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;

/**
 * <i>Data Access Object</i> for {@link OperationExecutionPE}.
 * 
 * @author pkupczyk
 */
public interface IOperationExecutionDAO extends IGenericDAO<OperationExecutionPE>
{

    void createOrUpdate(OperationExecutionPE execution);

    OperationExecutionPE tryFindByCode(String code);

    List<OperationExecutionPE> findByCodes(Collection<String> codes);

    List<OperationExecutionPE> findByIds(Collection<Long> ids);

    List<OperationExecutionPE> getExecutionsToBeFailedAfterServerRestart(Date serverStartDate);

    List<OperationExecutionPE> getExecutionsToBeTimeOutPending();

    List<OperationExecutionPE> getExecutionsToBeTimedOut();

    List<OperationExecutionPE> getExecutionsToBeDeleted();

}
