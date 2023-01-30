/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetUtils
{
    private static final String MAIN_DATA_SET_PATH = "mdsPath";

    private static final String MAIN_DATA_SET_PATTERN = "mdsPattern";

    private static final String AUTO_RESOLVE = "autoResolve";

    private static final String MODE = "mode";

    private static final String DISABLE_LINKS = "disableLinks";

    private static final String IS_LINK_DATASET = "is_link_data";

    public static String createDataViewUrl(AbstractExternalData dataSet, GenericViewModel model,
            String modeOrNull, boolean autoResolve, boolean disableLinks)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(dataSet.getDataStore().getDownloadUrl() + "/"
                        + dataSet.getCode());
        String sessionID = model.getSessionContext().getSessionID();
        methodWithParameters.addParameter(GenericSharedConstants.SESSION_ID_PARAMETER, sessionID);
        methodWithParameters.addParameter(DISABLE_LINKS, disableLinks);
        if (modeOrNull != null)
        {
            methodWithParameters.addParameter(MODE, modeOrNull);
        }
        if (autoResolve)
        {
            methodWithParameters.addParameter(AUTO_RESOLVE, autoResolve);
            String mainDataSetPattern = dataSet.getDataSetType().getMainDataSetPattern();
            if (StringUtils.isBlank(mainDataSetPattern) == false)
            {
                methodWithParameters.addParameter(MAIN_DATA_SET_PATTERN, mainDataSetPattern);
            }
            if (StringUtils.isBlank(dataSet.getDataSetType().getMainDataSetPath()) == false)
            {
                methodWithParameters.addParameter(MAIN_DATA_SET_PATH, dataSet.getDataSetType()
                        .getMainDataSetPath());
            }
        }

        methodWithParameters.addParameter(IS_LINK_DATASET, DataSetKind.LINK.equals(dataSet.getDataSetKind()));

        String url = methodWithParameters.toString();
        return url;
    }

}
