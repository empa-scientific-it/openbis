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

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;

/**
 * <i>Data Access Object</i> for {@link AuthorizationGroupPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IAuthorizationGroupDAO extends IGenericDAO<AuthorizationGroupPE>
{
    /**
     * Lists all authorization groups.
     */
    public List<AuthorizationGroupPE> list();
    
    public List<AuthorizationGroupPE> listByIds(Collection<Long> ids);

    public List<AuthorizationGroupPE> listByCodes(Collection<String> codes);

    /**
     * Creates a new authorization group.
     */
    public void create(AuthorizationGroupPE authorizationGroup);

    /**
     * Returns the authorization group with given code located in home database or null if no such group exists.
     */
    public AuthorizationGroupPE tryFindByCode(String code);
}
