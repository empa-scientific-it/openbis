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

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author anttil
 */
public class BasicAuthorizationRule implements AuthorizationRule
{

    private final RoleWithHierarchy limit;

    private final GuardedDomain domain;

    public BasicAuthorizationRule(GuardedDomain domain, RoleWithHierarchy limit)
    {
        switch (domain.getType())
        {
            case SPACE:
                if (!limit.isSpaceLevel())
                {
                    throw new IllegalArgumentException("Invalid limit for space: " + limit);
                }
                break;
            case INSTANCE:
                if (!limit.isInstanceLevel())
                {
                    throw new IllegalArgumentException("Invalid limit for instance: " + limit);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown domain type " + domain.getType());
        }
        this.limit = limit;
        this.domain = domain;
    }

    @Override
    public boolean accepts(Map<GuardedDomain, RoleWithHierarchy> permutation)
    {
        GuardedDomain current = this.domain;
        while (current != null)
        {
            if (this.limit.getRoles().contains(permutation.get(current)))
            {
                return true;
            }
            current = current.getSuperDomain();
        }
        return false;
    }
}
