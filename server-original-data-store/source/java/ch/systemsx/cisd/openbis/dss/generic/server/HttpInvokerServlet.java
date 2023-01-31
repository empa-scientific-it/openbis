package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * A more generic version of the DataStoreServlet above.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2010-04-21, Tomasz Pylak: Refactor to make the reference to DataStoreServlet use the HttpInvokerServlet.
public class HttpInvokerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final HttpRequestHandler target;

    private final String description;

    public HttpInvokerServlet(HttpRequestHandler target, String description)
    {
        this.target = target;
        this.description = description;
    }

    @Override
    public void init() throws ServletException
    {
        DataStoreServer.operationLog.info("[http-invoker] RPC service available at " + description);
    }

    // Code copied from org.springframework.web.context.support.HttpRequestHandlerServlet
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        LocaleContextHolder.setLocale(request.getLocale());
        try
        {
            this.target.handleRequest(request, response);
        } catch (HttpRequestMethodNotSupportedException ex)
        {
            String[] supportedMethods = ex.getSupportedMethods();
            if (supportedMethods != null)
            {
                response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods,
                        ", "));
            }
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
        } finally
        {
            LocaleContextHolder.resetLocaleContext();
        }
    }
}