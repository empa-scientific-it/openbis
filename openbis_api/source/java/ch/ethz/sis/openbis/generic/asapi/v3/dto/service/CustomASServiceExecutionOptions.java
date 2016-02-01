/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.CustomASServiceExecutionOptions")
public class CustomASServiceExecutionOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final Map<String, Object> parameters = new HashMap<String, Object>();

    public CustomASServiceExecutionOptions withParameter(String parameterName, Object value)
    {
        parameters.put(parameterName, value);
        return this;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
