/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import java.util.Properties;

public interface IOperationListener<OPERATION extends IOperation, RESULT extends IOperationResult>
{
    public static final String LISTENER_PROPERTY_KEY = "operation-listener";
    public static final String LISTENER_CLASS_KEY = LISTENER_PROPERTY_KEY + ".class";
    public abstract void setup(Properties properties);
    public abstract void beforeOperation(IApplicationServerApi api, String sessionToken, OPERATION operation);
    public abstract void afterOperation(IApplicationServerApi api, String sessionToken, OPERATION operation,
                                        RESULT result, RuntimeException runtimeException);
}
