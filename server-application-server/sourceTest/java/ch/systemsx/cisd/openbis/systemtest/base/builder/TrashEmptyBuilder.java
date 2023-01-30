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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * A builder that empties the trash.
 * 
 * @author cramakri
 */
public class TrashEmptyBuilder extends UpdateBuilder<String>
{
    public TrashEmptyBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
    }

    @Override
    public String create()
    {
        return "";
    }

    @Override
    public void perform()
    {
        List<Deletion> deletions = commonServer.listDeletions(sessionToken, false);
        commonServer.deletePermanently(sessionToken, TechId.createList(deletions), false);
    }
}