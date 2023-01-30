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
public class OrAuthorizationRule implements AuthorizationRule
{
    private final AuthorizationRule rule1;

    private final AuthorizationRule rule2;

    public OrAuthorizationRule(AuthorizationRule rule1, AuthorizationRule rule2)
    {
        this.rule1 = rule1;
        this.rule2 = rule2;
    }

    @Override
    public boolean accepts(Map<GuardedDomain, RoleWithHierarchy> roles)
    {
        return this.rule1.accepts(roles) || this.rule2.accepts(roles);
    }
}
