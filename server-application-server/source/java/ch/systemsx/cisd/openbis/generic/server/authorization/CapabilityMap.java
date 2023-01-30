/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfigFacade;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * A map that maps capabilities to roles.
 * 
 * @author Bernd Rinn
 */
class CapabilityMap
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CapabilityMap.class);

    private final IAuthorizationConfig authorizationConfig;

    private final Map<String, Collection<RoleWithHierarchy>> capMap =
            new HashMap<String, Collection<RoleWithHierarchy>>();

    @SuppressWarnings("unchecked")
    private final static List<String> readLines(File file)
    {
        if (file.exists() == false)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("No file '%s", file.getPath()));
            }
            return Collections.emptyList();
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Reading capability role map file " + file);
        }
        try
        {
            return FileUtils.readLines(file);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    CapabilityMap(File file, IAuthorizationConfig authorizationConfig)
    {
        this(readLines(file), file.getPath(), authorizationConfig);
    }

    CapabilityMap(List<String> lines, String filePath, IAuthorizationConfig authorizationConfig)
    {
        this.authorizationConfig = authorizationConfig;

        for (String line : lines)
        {
            final String trimmed = line.trim();
            if (trimmed.length() == 0 || trimmed.startsWith("#"))
            {
                continue;
            }
            String[] terms = StringUtils.split(trimmed, ';');
            String firstTerm = terms[0].trim();

            final String[] firstTermSplitted = StringUtils.split(firstTerm, " \t:", 2);
            if (firstTermSplitted.length != 2)
            {
                logWarning(line, filePath, null);
                continue;
            }
            final String capabilityName = firstTermSplitted[0].toUpperCase();
            if (firstTermSplitted[1].contains("="))
            {
                addRolesForParameter(capabilityName, firstTermSplitted[1], trimmed, filePath);
            } else
            {
                final String roleNames = firstTermSplitted[1];
                addRoles(capabilityName, roleNames, trimmed, filePath);
            }
            for (int i = 1; i < terms.length; i++)
            {
                addRolesForParameter(capabilityName, terms[i].trim(), trimmed, filePath);
            }
        }
    }

    private void addRolesForParameter(String capabilityName, String parameterTerm, String line, String filePath)
    {
        int indexOfEqual = parameterTerm.indexOf('=');
        if (indexOfEqual < 0)
        {
            logWarning(line, filePath, "missing '='");
            return;
        }
        String parameterName = parameterTerm.substring(0, indexOfEqual).trim().toUpperCase();
        if (StringUtils.isBlank(parameterName))
        {
            logWarning(line, filePath, "parameter name not specified");
            return;
        }
        String roleNames = parameterTerm.substring(indexOfEqual + 1).trim();
        addRoles(capabilityName + ":" + parameterName, roleNames, line, filePath);
    }

    private void addRoles(String capabilityName, String roleNames, String line, String filePath)
    {
        Collection<RoleWithHierarchy> roles = capMap.get(capabilityName);

        if (roles == null)
        {
            roles = new HashSet<RoleWithHierarchy>();
            capMap.put(capabilityName, roles);
        }

        AuthorizationConfigFacade configFacade = new AuthorizationConfigFacade(authorizationConfig);

        for (String roleName : StringUtils.split(roleNames, ","))
        {
            roleName = roleName.trim().toUpperCase();
            try
            {
                final RoleWithHierarchy role = RoleWithHierarchy.valueOf(roleName);

                if (configFacade.isRoleEnabled(role))
                {
                    roles.add(role);

                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug(String
                                .format("Add to map: '%s' -> %s", capabilityName, role));
                    }
                } else
                {
                    logWarning(line, filePath, "role '" + roleName + "' doesn't exist");
                }
            } catch (IllegalArgumentException ex)
            {
                logWarning(line, filePath, "role '" + roleName + "' doesn't exist");
            }
        }
    }

    private void logWarning(String line, String filePath, String messageOrNull)
    {
        String msg = String.format(
                "Ignoring mal-formed line '%s' in %s",
                line, filePath);
        if (messageOrNull == null)
        {
            msg += ".";
        } else
        {
            msg += " [" + messageOrNull + "].";
        }
        operationLog.warn(msg);

    }

    Collection<RoleWithHierarchy> tryGetRoles(Method m, String argumentNameOrNull)
    {
        final Capability cap = m.getAnnotation(Capability.class);
        if (cap == null)
        {
            return null;
        }
        String capabilityName = cap.value().toUpperCase();
        if (StringUtils.isNotBlank(argumentNameOrNull))
        {
            capabilityName += ":" + argumentNameOrNull.toUpperCase();
        }
        final Collection<RoleWithHierarchy> rolesOrNull = capMap.get(capabilityName);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Request: '%s' -> %s", capabilityName, rolesOrNull));
        }
        return rolesOrNull;
    }
}
