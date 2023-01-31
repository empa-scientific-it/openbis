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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.jython.IJythonFunction;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractProgrammableTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Pawel Glyzewski
 */
/*
 * TODO: gpawel: this class shouldn't extend V2 jython service, it should be the other way around.
 */
public class DataSetRegistrationServiceV2<T extends DataSetInformation> extends
        JythonDataSetRegistrationServiceV2<T>
{
    public DataSetRegistrationServiceV2(AbstractProgrammableTopLevelDataSetHandler<T> registrator,
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        super(registrator, incomingDataSetFile, userProvidedDataSetInformationOrNull,
                globalCleanAfterwardsAction, delegate,
                new IJythonInterpreter()
                    {
                        @Override
                        public IJythonFunction tryJythonFunction(String name)
                        {
                            return null;
                        }

                        @Override
                        public void set(String variableName, Object object)
                        {
                        }

                        @Override
                        public void releaseResources()
                        {
                        }

                        @Override
                        public void exec(String scriptString, String scriptFile)
                        {
                        }

                        @Override
                        public void exec(String scriptString)
                        {
                        }

                        @Override
                        public void addToPath(String... pythonPaths)
                        {
                        }

                        @Override
                        public boolean isNextCommand(String line)
                        {
                            return false;
                        }
                    }, null);
    }

}
