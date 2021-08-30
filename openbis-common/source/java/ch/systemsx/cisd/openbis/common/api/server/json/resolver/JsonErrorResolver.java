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
