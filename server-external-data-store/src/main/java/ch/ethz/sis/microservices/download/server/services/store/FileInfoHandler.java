package ch.ethz.sis.microservices.download.server.services.store;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.sis.microservices.download.server.json.jackson.JacksonObjectMapper;
import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;

public class FileInfoHandler extends AbstractFileServiceHandler
{
    private static Logger logger = LogManager.getLogger(FileInfoHandler.class);

    protected void writeOutput(HttpServletResponse response, int httpResponseCode, boolean isFileAccessible) throws ServletException, IOException
    {
        byte[] resultAsBytes = null;
        try
        {
            Map<String, String> result = new HashMap<>();
            result.put("isFileAccessible", Boolean.toString(isFileAccessible));
            resultAsBytes = JacksonObjectMapper.getInstance().writeValue(result);
        } catch (Exception ex)
        {
            logger.catching(ex);
        }

        response.setContentType("application/json; charset=utf-8");
        response.getOutputStream().write(resultAsBytes);
        response.setStatus(httpResponseCode);
    }

    @Override
    protected void success(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        writeOutput(response, HttpServletResponse.SC_OK, true);
    }

    @Override
    protected void failure(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        writeOutput(response, HttpServletResponse.SC_OK, false);
    }
}