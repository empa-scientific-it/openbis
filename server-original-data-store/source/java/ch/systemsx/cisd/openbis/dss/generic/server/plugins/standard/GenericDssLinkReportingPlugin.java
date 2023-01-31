/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class GenericDssLinkReportingPlugin extends AbstractDssLinkReportingPlugin
{
    private static final long serialVersionUID = 1L;

    // URL parameters
    private static final String MAIN_DATA_SET_PATH = "mdsPath";

    private static final String MAIN_DATA_SET_PATTERN = "mdsPattern";

    private static final String FORCE_AUTO_RESOLVE = "forceAutoResolve";

    private static final String SESSION_ID = "sessionID";

    // Properties
    private static final String DOWNLOAD_URL = "download-url";

    private static final String DATA_SET_REGEX = "data-set-regex";

    private static final String DATA_SET_PATH = "data-set-path";

    // ivars
    private final String downloadUrl;

    private final String regexOrNull;

    private final String pathOrNull;

    /**
     * @param properties
     * @param storeRoot
     */
    public GenericDssLinkReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.downloadUrl = PropertyUtils.getProperty(properties, DOWNLOAD_URL, "");
        this.regexOrNull = PropertyUtils.getProperty(properties, DATA_SET_REGEX);
        this.pathOrNull = PropertyUtils.getProperty(properties, DATA_SET_PATH);
    }

    /**
     * Get an LinkModel that codes a link to the file.
     */
    public LinkModel getDataSetLinkModel(DatasetDescription dataSet)
    {
        return getDataSetLinkModel(dataSet, null);
    }

    /**
     * Get an HTML string with a link to the file.
     */
    public LinkModel getDataSetLinkModel(DatasetDescription dataSet, String sessionIdOrNull)
    {
        LinkModel linkModel = new LinkModel();
        linkModel.setSchemeAndDomain(downloadUrl);
        linkModel.setPath(GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/"
                + dataSet.getDataSetCode());

        ArrayList<LinkModel.LinkParameter> parameters = new ArrayList<LinkModel.LinkParameter>();
        parameters.add(new LinkModel.LinkParameter(FORCE_AUTO_RESOLVE, Boolean.TRUE.toString()));
        if (regexOrNull != null)
        {
            parameters.add(new LinkModel.LinkParameter(MAIN_DATA_SET_PATTERN, regexOrNull));
        }
        if (pathOrNull != null)
        {
            parameters.add(new LinkModel.LinkParameter(MAIN_DATA_SET_PATH, pathOrNull));
        }

        if (sessionIdOrNull != null)
        {
            parameters.add(new LinkModel.LinkParameter(SESSION_ID, sessionIdOrNull));
        }

        linkModel.setParameters(parameters);
        return linkModel;
    }

    @Override
    public LinkModel createLink(DatasetDescription dataSet)
    {
        return getDataSetLinkModel(dataSet);
    }
}
