/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic;

/**
 * Some constants needed to create attachment download URLs.
 * 
 * @author Franz-Josef Elmer
 */
public class AttachmentDownloadConstants
{
    /** Name of the servlet to download an experiment attachment. */
    public static final String ATTACHMENT_DOWNLOAD_SERVLET_NAME = "attachment-download";

    /** The HTTP URL parameter used to specify the technical id. */
    public static final String TECH_ID_PARAMETER = "id";

    /** The HTTP URL parameter used to specify the version. */
    public static final String VERSION_PARAMETER = "version";

    /** The HTTP URL parameter used to specify the file name. */
    public static final String FILE_NAME_PARAMETER = "fileName";

    /** The HTTP URL parameter used to specify the attachment holder. */
    public static final String ATTACHMENT_HOLDER_PARAMETER = "attachmentHolder";

}
