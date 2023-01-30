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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author pkupczyk
 */
public class ListMetaprojectsCriteria extends
        DefaultResultSetConfig<String, TableModelRowWithObject<Metaproject>> implements
        IsSerializable
{

    private Set<String> blacklist;

    private Set<String> whitelist;

    public Set<String> getBlacklist()
    {
        return blacklist;
    }

    public void setBlacklist(Set<String> blacklist)
    {
        this.blacklist = blacklist;
    }

    public Set<String> getWhitelist()
    {
        return whitelist;
    }

    public void setWhitelist(Set<String> whitelist)
    {
        this.whitelist = whitelist;
    }

}
