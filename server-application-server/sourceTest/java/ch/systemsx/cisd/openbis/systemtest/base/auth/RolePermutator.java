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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * @author anttil
 */
public class RolePermutator
{

    public static RoleWithHierarchy[][] getAcceptedPermutations(AuthorizationRule rule,
            GuardedDomain... domains)
    {
        Collection<Map<GuardedDomain, RoleWithHierarchy>> accepted =
                new HashSet<Map<GuardedDomain, RoleWithHierarchy>>();

        for (Map<GuardedDomain, RoleWithHierarchy> permutation : getAllPermutations(Arrays
                .asList(domains)))
        {
            if (rule.accepts(permutation) && !containsOnlyNulls(permutation.values()))
            {
                accepted.add(permutation);
            }
        }
        return toNestedArray(accepted, domains);
    }

    public static Collection<Map<GuardedDomain, RoleWithHierarchy>> getAllPermutations(
            List<GuardedDomain> domains)
    {
        Collection<Map<GuardedDomain, RoleWithHierarchy>> result =
                new HashSet<Map<GuardedDomain, RoleWithHierarchy>>();
        if (domains.size() == 0)
        {
            return result;
        }

        GuardedDomain domain = domains.get(0);
        for (RoleWithHierarchy role : getAllRolesFor(domain.getType()))
        {
            Collection<Map<GuardedDomain, RoleWithHierarchy>> subpermutations =
                    getAllPermutations(restOf(domains));
            if (subpermutations.size() > 0)
            {
                for (Map<GuardedDomain, RoleWithHierarchy> rest : subpermutations)
                {
                    Map<GuardedDomain, RoleWithHierarchy> map =
                            new HashMap<GuardedDomain, RoleWithHierarchy>();
                    map.put(domain, role);
                    map.putAll(rest);
                    result.add(map);
                }
            } else
            {
                Map<GuardedDomain, RoleWithHierarchy> map =
                        new HashMap<GuardedDomain, RoleWithHierarchy>();
                map.put(domain, role);
                result.add(map);
            }
        }
        return result;
    }

    public static <T> List<T> restOf(List<T> list)
    {
        if (list.size() == 0)
        {
            throw new IllegalArgumentException("0");
        }

        if (list.size() == 1)
        {
            return new ArrayList<T>();
        }

        return list.subList(1, list.size());
    }

    public static Collection<RoleWithHierarchy> getAllRolesFor(RoleLevel level)
    {
        if (RoleLevel.SPACE.equals(level))
        {
            return allSpaceRoles;
        } else if (RoleLevel.INSTANCE.equals(level))
        {
            return allInstanceRoles;
        } else
        {
            throw new IllegalArgumentException(level.toString());
        }
    }

    public static RoleWithHierarchy[][] toNestedArray(
            Collection<Map<GuardedDomain, RoleWithHierarchy>> input, GuardedDomain[] order)
    {
        RoleWithHierarchy[][] result = new RoleWithHierarchy[input.size()][];
        int index = 0;
        for (Map<GuardedDomain, RoleWithHierarchy> permutation : input)
        {
            RoleWithHierarchy[] roles = new RoleWithHierarchy[permutation.size()];
            int subIndex = 0;
            for (GuardedDomain d : order)
            {
                roles[subIndex] = permutation.get(d);
                subIndex++;
            }
            result[index] = roles;
            index++;
        }
        return result;
    }

    public static boolean containsOnlyNulls(Collection<?> collection)
    {
        for (Object o : collection)
        {
            if (o != null)
            {
                return false;
            }
        }
        return true;
    }

    public static final Collection<RoleWithHierarchy> allInstanceRoles;

    public static final Collection<RoleWithHierarchy> allSpaceRoles;

    static
    {
        allInstanceRoles = new HashSet<RoleWithHierarchy>();
        allSpaceRoles = new HashSet<RoleWithHierarchy>();

        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ADMIN);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ETL_SERVER);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_OBSERVER);
        allInstanceRoles.add(null);

        allSpaceRoles.add(RoleWithHierarchy.SPACE_ADMIN);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_ETL_SERVER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_POWER_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_OBSERVER);
        allSpaceRoles.add(null);
    }
}
