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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.IPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonAggregationService;

/**
 * A version of the {@link JythonAggregationService} with extra support for screening.
 * 
 * @author Franz-Josef Elmer
 */
public class ScreeningJythonAggregationService extends
        JythonAggregationService
{

    private static final long serialVersionUID = 1L;

    /**
     * Public constructor.
     */
    public ScreeningJythonAggregationService(Properties properties,
            File storeRoot)
    {
        this(properties, storeRoot, new ScreeningPluginScriptRunnerFactory(
                getScriptPathProperty(properties)));
    }

    /**
     * Constructor used in tests.
     */
    protected ScreeningJythonAggregationService(Properties properties,
            File storeRoot, IPluginScriptRunnerFactory scriptRunnerFactory)
    {
        super(properties, storeRoot, scriptRunnerFactory);
    }
}
