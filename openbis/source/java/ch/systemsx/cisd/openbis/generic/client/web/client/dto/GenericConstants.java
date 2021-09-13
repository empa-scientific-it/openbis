/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;

import ch.systemsx.cisd.openbis.generic.shared.basic.AttachmentDownloadConstants;

/**
 * Some generic constants.
 *
 * @author Franz-Josef Elmer
 */
public final class GenericConstants
{
    public static final String POPUP_BLOCKER_DETECTED =
            "A pop-up blocker has been detected. Please disable all the pop-up blockers for this site.";

    private GenericConstants()
    {
        // Can not be instantiated.
    }

    /**
     * Prefix all widget IDs have to start with.
     */
    public static final String ID_PREFIX = "openbis_";

    private static final String DEPLOYED_APPLICATION_NAME = Dictionary.getDictionary("common").get("deployedAppName");

    private static final String SPRING_APPLICATION_NAME = Dictionary.getDictionary("common").get("springApplicationName");

    public static final String COMMON_SERVER_NAME = createServicePath("common");

    /**
     * Creates for the specified service name the service path.
     */
    public final static String createServicePath(final String serviceName)
    {
        if (GWT.isProdMode()) // Is Production
        {
            return "/" + DEPLOYED_APPLICATION_NAME + "/" + SPRING_APPLICATION_NAME + "/" + serviceName;
        } else
        {
            return "/" + SPRING_APPLICATION_NAME + "/" + serviceName;
        }
    }

    /** Name of the servlet to download an experiment attachment. */
    public static final String ATTACHMENT_DOWNLOAD_SERVLET_NAME =
            createServicePath(AttachmentDownloadConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME);

    /** Name of the servlet to export and download a file. */
    public static final String FILE_EXPORTER_DOWNLOAD_SERVLET_NAME =
            createServicePath("export-file-downloader");

    /** The HTTP URL parameter used to specify the export criteria. */
    public static final String EXPORT_CRITERIA_KEY_PARAMETER = "exportDataKey";

    public static final String EXPORT_FILE_NAME = "fileName";

    public static final String TIMESTAMP_PARAMETER = "timestamp";

    public static final String LABEL_SEPARATOR = ":";

    /** Name of the servlet to download a template. */
    public static final String TEMPLATE_SERVLET_NAME = createServicePath("template-download");

    /** The HTTP URL parameter used to specify the entity kind. */
    public static final String ENTITY_KIND_KEY_PARAMETER = "entityKind";

    /** The HTTP URL parameter used to specify the entity type. */
    public static final String ENTITY_TYPE_KEY_PARAMETER = "entityType";

    /** The HTTP URL parameter used to specify file format. */
    public static final String FILE_FORMAT_PARAMETER = "fileFormat";

    /** The HTTP URL parameter used to specify if codes are automatically generated. */
    public static final String AUTO_GENERATE = "autoGenerate";

    /** Name of the servlet that redirects to the URL for a help page. */
    public static final String HELP_REDIRECT_SERVLET_NAME = createServicePath("help");

    /** The HTTP URL parameter that specifies the title of the help page we want to show. */
    public static final String HELP_REDIRECT_PAGE_TITLE_KEY = "pageTitle";

    /** The HTTP URL parameter that specifies whether we want generic or specific help. */
    public static final String HELP_REDIRECT_SPECIFIC_KEY = "specific";

    public static final int DESCRIPTION_2000 = 2000;

    public static final int DESCRIPTION_500000 = 500000;

    public static final int MAIN_DS_PATH_LENGTH = 1000;

    public static final int MAIN_DS_PATTERN_LENGTH = 300;

    public static final int COLUMN_LABEL = 128;

    public static final String WITH_EXPERIMENTS = "with_experiments";

    public static final String WITH_SPACE = "with_space";

    public static final String BATCH_OPERATION_KIND = "batch_operation_kind";

    public static final String ITEMS_TEXTAREA_REGEX = "\n|\r\n|, *";

    public static final String ITEMS_TEXTAREA_DEFAULT_SEPARATOR = ", ";

    /** A regular expression that match email addresses. */
    public static final String EMAIL_REGEX =
            "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";

    public static final String ALL_ENTITY_KINDS = "(All)";

}
