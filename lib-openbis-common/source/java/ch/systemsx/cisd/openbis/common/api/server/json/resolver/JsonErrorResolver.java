/*
 * Copyright ETH 2021 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.common.api.server.json.resolver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.ErrorData;
import com.googlecode.jsonrpc4j.ErrorResolver;

public class JsonErrorResolver implements ErrorResolver
{
    @Override public JsonError resolveError(final Throwable t, final Method method, final List<JsonNode> arguments)
    {
        return new JsonError(0, t.getMessage(), new FullErrorData(t));
    }

    public static class FullErrorData extends ErrorData
    {

        private final String stackTrace;

        public FullErrorData(Throwable t)
        {
            super(t.getClass().getName(), t.getMessage());

            StringWriter buffer = new StringWriter();
            t.printStackTrace(new PrintWriter(buffer));
            stackTrace = buffer.toString();
        }

        public String getStackTrace()
        {
            return stackTrace;
        }
    }
}
