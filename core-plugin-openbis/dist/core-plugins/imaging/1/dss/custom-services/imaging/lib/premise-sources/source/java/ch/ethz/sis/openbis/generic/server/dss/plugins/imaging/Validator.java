/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.io.Serializable;
import java.util.Map;

class Validator
{
    private Validator() {}


    static void validateInputParams(Map<String, Object> params)
    {
//        if (!params.containsKey("permId"))
//        {
//            throw new UserFailureException("Missing dataset permId!");
//        }
        if (!params.containsKey("type"))
        {
            throw new UserFailureException("Missing type!");
        }
//        if (!params.containsKey("index"))
//        {
//            throw new UserFailureException("Missing index!");
//        }
    }


    static void validateExportConfig(Map<String, Serializable> exportConfig) {
        if(exportConfig == null) {
            throw new UserFailureException("Export config can not be empty!");
        }
        validateTag(exportConfig, "include", true);
        validateTag(exportConfig, "image-format");
        validateTag(exportConfig, "archive-format");
        validateTag(exportConfig, "resolution");
    }

    private static void validateTag(Map<String, Serializable> config, String tagName)
    {
        validateTag(config, tagName, false);
    }

    private static void validateTag(Map<String, Serializable> config, String tagName, boolean isMultiValue)
    {
        if(!config.containsKey(tagName)){
            throw new UserFailureException("Missing '"+tagName+"' in export config!");
        }
        Serializable include = config.get(tagName);
        if(include == null){
            throw new UserFailureException("'"+tagName+"' tag in export config can not be null!");
        }
        if(isMultiValue)
        {
            if (!include.getClass().isArray())
            {
                throw new UserFailureException("'include' tag in export config must be an array!");
            }
            if (((Serializable[]) include).length == 0)
            {
                throw new UserFailureException("'include' tag in export config can not be empty!");
            }
        }
    }


}
