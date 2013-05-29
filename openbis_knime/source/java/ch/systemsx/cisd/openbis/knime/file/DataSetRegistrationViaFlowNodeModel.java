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

package ch.systemsx.cisd.openbis.knime.file;

import org.knime.core.node.NodeModel;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;

/**
 * {@link NodeModel} for registration of a file as a new data set. The file path is expected as a flow variable.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationViaFlowNodeModel extends DataSetRegistrationNodeModel
{

    protected DataSetRegistrationViaFlowNodeModel(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super(FlowVariablePortObject.class, serviceFacadeFactory);
    }

}
