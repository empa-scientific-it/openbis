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

package ch.systemsx.cisd.openbis.systemtest.base.auth;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * @author anttil
 */
public class SpaceDomain extends GuardedDomain
{

    private GuardedDomain superDomain;

    public SpaceDomain(GuardedDomain instance)
    {
        super(RoleLevel.SPACE);
        this.superDomain = instance;
    }

    @Override
    public GuardedDomain getSuperDomain()
    {
        return this.superDomain;
    }

}
