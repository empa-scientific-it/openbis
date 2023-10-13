/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.execute;

import ch.systemsx.cisd.base.annotation.JsonObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("dss.dto.service.execute.AbstractExecutionOptionsWithParameters")
public abstract class AbstractExecutionOptionsWithParameters<EO extends AbstractExecutionOptionsWithParameters<EO, V>, V> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, V> parameters = new HashMap<String, V>();

    @SuppressWarnings("unchecked")
    public EO withParameter(String parameterName, V value)
    {
        parameters.put(parameterName, value);
        return (EO) this;
    }

    public Map<String, V> getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + ": parameterKeys=" + (parameters != null ? parameters.keySet() : "[]");
    }

}
