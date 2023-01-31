/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;

/**
 * Utility functions for session workspace.
 * 
 * @author Franz-Josef Elmer
 */
public class SessionWorkspaceUtil
{
    public static final String SESSION_WORKSPACE_ROOT_DIR_KEY = "session-workspace-root-dir";

    public static final String SESSION_WORKSPACE_ROOT_DIR_DEFAULT = "data/sessionWorkspace";

    public static File getSessionWorkspace(Properties properties)
    {
        return new File(properties.getProperty(SESSION_WORKSPACE_ROOT_DIR_KEY,
                SESSION_WORKSPACE_ROOT_DIR_DEFAULT));
    }
}
