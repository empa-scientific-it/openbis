/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractServlet;


/**
 * Servlet for uploading and downloading files.
 *
 * @author Franz-Josef Elmer
 */
@Controller
public class FileServiceServlet extends AbstractServlet
{
    public static final String FILE_SERVICE_PATH = "file-service";
    public static final String FILE_SERVICE_PATH_MAPPING = FILE_SERVICE_PATH + "/**/*";

    private static final String KEY_PREFIX = "file-server.";
    public static final String REPO_PATH_KEY = KEY_PREFIX + "repository-path";

    public static final String DEFAULT_REPO_PATH = "../../../data/file-server";
    private static final String APP_PREFIX = "/" + FILE_SERVICE_PATH + "/";

    private static final String MAX_SIZE_KEY = KEY_PREFIX + "maximum-file-size-in-MB";
    private static final int DEFAULT_MAX_SIZE = 10;
    
    private static final String DOWNLOAD_CHECK_KEY = KEY_PREFIX + "download-check";
    private static final boolean DEFAULT_DOWNLOAD_CHECK = true;
    
    private static final String DOWNLOAD_URL_PLACE_HOLDER = "download-url";
    private static final String ERROR_MESSAGE_PLACE_HOLDER = "error-message";
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, FileServiceServlet.class);
    
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Override
    @RequestMapping({ "/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING,
        "/openbis/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING,
        "/openbis/openbis/" + FileServiceServlet.FILE_SERVICE_PATH_MAPPING})
    protected void respondToRequest(HttpServletRequest request, HttpServletResponse response) 
            throws Exception, IOException
    {
        String fullPath = request.getPathInfo();
        int indexOfPrefix = fullPath.indexOf(APP_PREFIX);
        if (indexOfPrefix < 0)
        {
            return;
        }
        
        PathInfo pathInfo = new PathInfo(fullPath.substring(indexOfPrefix + APP_PREFIX.length()));
        File filesRepository = getFilesRepository();
        operationLog.info(fullPath);
        if (request instanceof MultipartHttpServletRequest)
        {
            handleUpload((MultipartHttpServletRequest) request, response, filesRepository, pathInfo);
        } else
        {
            handleDownload(request, response, filesRepository, pathInfo);
        }
        
    }

    private void handleDownload(HttpServletRequest request, HttpServletResponse response, File filesRepository, 
            PathInfo pathInfo) throws IOException
    {
        if (canDownload(request) == false)
        {
            operationLog.warn("Download not authorized: " + pathInfo);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File file = new File(filesRepository, pathInfo.getSectionAndPath());
        if (file.isFile() == false)
        {
            operationLog.warn("Unknown file: " + pathInfo);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentLength((int) file.length());
        String contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file.getName());
        response.setContentType(contentType);
        ServletOutputStream outputStream = null;
        FileInputStream inputStream = null;
        try
        {
            outputStream = response.getOutputStream();
            inputStream = new FileInputStream(file);
            IOUtils.copy(inputStream, outputStream);
            operationLog.info(file.length() + " bytes of file '" + pathInfo + "' have been deliverd.");
        } catch (IOException ex)
        {
            operationLog.error("Delivering file '" + pathInfo + "' failed.", ex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
    
    private boolean canDownload(HttpServletRequest request)
    {
        Properties props = configurer.getResolvedProps();
        if (PropertyUtils.getBoolean(props, DOWNLOAD_CHECK_KEY, DEFAULT_DOWNLOAD_CHECK) == false)
        {
            return true;
        }
        return isSessionActive(request);
    }

    private void handleUpload(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, 
            File filesRepository, PathInfo pathInfo) throws IOException
    {
        if (isSessionActive(multipartRequest) == false)
        {
            writeError(multipartRequest, response, pathInfo, "Session time out");
            return;
        }
        Iterator<String> fileNamesIterator = multipartRequest.getFileNames();
        if (fileNamesIterator.hasNext() == false)
        {
            writeError(multipartRequest, response, pathInfo, "No file.");
            return;
        }
        String name = fileNamesIterator.next();
        MultipartFile multipartFile = multipartRequest.getFile(name);
        String originalFilename = multipartFile.getOriginalFilename();
        // The original filename is obtained incorrectly encoded, and even swapping its encoding can lead to other issues
        // String originalFilenameCorrectEncoding = new String((originalFilename).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        int maxSizeInMB = PropertyUtils.getInt(configurer.getResolvedProps(), MAX_SIZE_KEY, DEFAULT_MAX_SIZE);
        long maxSize = maxSizeInMB * FileUtils.ONE_MB;
        if (multipartFile.getSize() > maxSize)
        {
            writeError(multipartRequest, response, pathInfo, "File " + originalFilename 
                    + " is to large. Maximum allowed size is  " + maxSizeInMB + "MB.");
            return;
        }
        String uuid = UUID.randomUUID().toString();
        String nameUUID = UUID.randomUUID() + "." + FilenameUtils.getExtension(originalFilename);
        String filePath = pathInfo.getSection() + "/" + uuid.substring(0, 2) + "/" + uuid.substring(2, 4)
                + "/" + uuid.substring(4, 6) + "/" + uuid + "/" + nameUUID;
        File file = new File(filesRepository, filePath);
        file.getParentFile().mkdirs();
        multipartFile.transferTo(file);
        operationLog.info(multipartFile.getSize() + " bytes have been uploaded for file '"
                + originalFilename + "' and stored in '" + filePath + "'.");

        response.setStatus(HttpServletResponse.SC_OK);
        String imageURL = "/openbis/openbis" + APP_PREFIX + filePath;
        writeDownloadUrl(multipartRequest, response, pathInfo, imageURL);
    }

    private boolean isSessionActive(HttpServletRequest request)
    {
        return service.isSessionActive(getSessionToken(request));
    }
    
    private void writeDownloadUrl(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, 
            PathInfo pathInfo, String downloadUrl) throws IOException
    {
        writeResponse(multipartRequest, response, pathInfo, "download-url-template", 
                DOWNLOAD_URL_PLACE_HOLDER, downloadUrl);
    }

    private void writeError(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, 
            PathInfo pathInfo, String errorMessage) throws IOException
    {
        operationLog.warn("Return the following error message for '" + pathInfo + "': " + errorMessage);
        writeResponse(multipartRequest, response, pathInfo, "error-message-template", 
                ERROR_MESSAGE_PLACE_HOLDER, errorMessage);
    }
    
    private void writeResponse(MultipartHttpServletRequest multipartRequest, HttpServletResponse response, 
            PathInfo pathInfo, String type, String placeHolder, String placeHolderValue) throws IOException
    {
        Template template = getTemplate(pathInfo, type, "${" + placeHolder + "}");
        bindPlaceholdersToRequestParameters(template, multipartRequest);
        template.attemptToBind(placeHolder, placeHolderValue);
        writeResponse(response, template.createText(false));
    }

    private void bindPlaceholdersToRequestParameters(Template template, MultipartHttpServletRequest multipartRequest)
    {
        for (String placeholderName : template.getPlaceholderNames())
        {
            String value = multipartRequest.getParameter(placeholderName);
            if (value != null)
            {
                template.attemptToBind(placeholderName, value);
            }
        }
    }
    
    private Template getTemplate(PathInfo pathInfo, String type, String defaultValue)
    {
        String key = KEY_PREFIX + "section_" + pathInfo.getSection() + "." + type;
        String template = configurer.getResolvedProps().getProperty(key);
        if (template == null)
        {
            operationLog.warn("No template configured for '" + key + "'. Using default template: " + defaultValue);
            template = defaultValue;
        }
        return new Template(template);
    }
    
    private File getFilesRepository()
    {
        return new File(configurer.getResolvedProps().getProperty(REPO_PATH_KEY, DEFAULT_REPO_PATH));
    }

    private static final class PathInfo
    {
        private final String sectionAndPath;
        private final String section;

        PathInfo(String sectionAndPath)
        {
            this.sectionAndPath = sectionAndPath;
            String[] splittedString = sectionAndPath.split("/", 2);
            section = splittedString[0];
        }

        public String getSectionAndPath()
        {
            return sectionAndPath;
        }

        public String getSection()
        {
            return section;
        }

        @Override
        public String toString()
        {
            return sectionAndPath;
        }
    }
}
