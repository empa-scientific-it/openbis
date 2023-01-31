/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Processing plugin which copies data sets to a destination folder by using rsync. The destination can be
 * <ul>
 * <li>on the local file system,
 * <li>a mounted remote folder,
 * <li>a remote folder accessible via SSH,
 * <li>a remote folder accessible via an rsync server.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetCopier extends AbstractDropboxProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    public static final String DESTINATION_KEY = "destination";

    public static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    public static final String RENAME_TO_DATASET_CODE_KEY = "rename-to-dataset-code";

    public static final String HARD_LINK_COPY_KEY = "hard-link-copy";

    @Private
    static final String ALREADY_EXIST_MSG = "already exist";

    @Private
    static final String COPYING_FAILED_MSG = "copying failed";

    public static final String GFIND_EXEC = "find";

    public static final String RSYNC_EXEC = "rsync";

    public static final String LN_EXEC = "ln";

    public static final String SSH_EXEC = "ssh";

    public static final long SSH_TIMEOUT_MILLIS = 15 * 1000; // 15s

    public DataSetCopier(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory(), new SshCommandExecutorFactory(),
                new ImmutableCopierFactory(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    DataSetCopier(Properties properties, File storeRoot, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory,
            IImmutableCopierFactory immutableCopierFactory, ITimeProvider timeProvider)
    {
        super(properties, storeRoot, new Copier(properties, pathCopierFactory,
                sshCommandExecutorFactory, immutableCopierFactory), timeProvider);
    }

    public DataSetCopier(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler, ITimeProvider timeProvider)
    {
        super(properties, storeRoot, dropboxHandler, timeProvider);
    }

    @Override
    protected String getProcessingDescription(DatasetDescription dataset,
            DataSetProcessingContext context)
    {
        return "Copy to " + properties.getProperty(DESTINATION_KEY);
    }

}
