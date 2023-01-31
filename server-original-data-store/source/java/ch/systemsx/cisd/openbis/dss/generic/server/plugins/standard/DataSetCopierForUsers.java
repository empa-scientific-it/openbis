/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.shared.Constants.USER_PARAMETER;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetCopierForUsers extends DataSetCopier
{
    private static final String USER_PARAMETER_STRING = "${" + USER_PARAMETER + "}";

    private static final long serialVersionUID = 1L;

    public DataSetCopierForUsers(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory(), new SshCommandExecutorFactory(),
                new ImmutableCopierFactory(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    DataSetCopierForUsers(Properties properties, File storeRoot,
            IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory,
            IImmutableCopierFactory immutableCopierFactory, ITimeProvider timeProvider)
    {
        super(properties, storeRoot, new Copier(properties, pathCopierFactory,
                sshCommandExecutorFactory, immutableCopierFactory)
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected String transformHostFile(String originalHostFile,
                        Map<String, String> parameterBindings)
                {
                    return createDestinationFileName(originalHostFile, parameterBindings);
                }
            }, timeProvider);
    }

    private static String createDestinationFileName(String originalHostFile,
            Map<String, String> parameterBindings)
    {
        int indexOfParameter = originalHostFile.indexOf(USER_PARAMETER_STRING);
        String hostFile = originalHostFile;
        if (indexOfParameter >= 0)
        {
            String user = parameterBindings.get(USER_PARAMETER);
            if (user == null)
            {
                throw new UserFailureException("Missing parameter '" + USER_PARAMETER
                        + "'.");
            }
            hostFile =
                    originalHostFile.substring(0, indexOfParameter)
                            + user
                            + originalHostFile.substring(indexOfParameter
                                    + USER_PARAMETER_STRING.length());
        }
        return hostFile;
    }

    @Override
    protected String getProcessingDescription(DatasetDescription dataset,
            DataSetProcessingContext context)
    {
        return "Copy to "
                + createDestinationFileName(properties.getProperty(DESTINATION_KEY),
                        context.getParameterBindings());
    }

}
