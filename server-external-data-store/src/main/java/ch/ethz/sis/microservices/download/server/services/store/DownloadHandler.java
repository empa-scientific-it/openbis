package ch.ethz.sis.microservices.download.server.services.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;

public class DownloadHandler extends AbstractFileServiceHandler
{
    private static Logger logger = LogManager.getLogger(DownloadHandler.class);

    @Override
    protected void success(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    		// Request parameters
    		String offsetP = request.getParameter("offset");
    		long offset = 0;
    		if(offsetP != null) {
    			offset = Long.parseLong(offsetP);
    		}
    		if(offset < 0) {
                throw new IllegalArgumentException("offset can't be negative");
        }
    		long size = Files.size(pathToFile) - offset;
    		if(size < 0) {
    			throw new IllegalArgumentException("offset to read starts beyond end of file");
    		}
    		
    		// Response
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + pathToFile.getFileName().toString());
        response.setHeader("Content-Length", Long.toString(size));
        
		InputStream is = Files.newInputStream(pathToFile);
		is.skip(offset);
		copy(is, response.getOutputStream(), size);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private static final int BUFFER_SIZE = 1024;
    private static void copy(InputStream source, OutputStream destination, long sourceSize) throws IOException {
    		byte[] buf = new byte[BUFFER_SIZE];
    		int readed;
    		while ((readed = source.read(buf)) > 0) {
    			destination.write(buf, 0, readed);
    		}
    }

    @Override
    protected void failure(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}