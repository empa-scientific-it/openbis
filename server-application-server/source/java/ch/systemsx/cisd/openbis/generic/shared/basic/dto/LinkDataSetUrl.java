/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicURLEncoder;

/**
 * @author pkupczyk
 */
public class LinkDataSetUrl
{

    private String externalCode;

    private String urlTemplate;

    public LinkDataSetUrl(String externalCode, String urlTemplate)
    {
        this.externalCode = externalCode;
        this.urlTemplate = urlTemplate;
    }

    public LinkDataSetUrl(LinkDataSet dataset)
    {
        if (dataset == null)
        {
            throw new IllegalArgumentException("Dataset cannot be null");
        }
        externalCode = dataset.getExternalCode();
        if (dataset.getExternalDataManagementSystem() != null)
        {
            urlTemplate = dataset.getExternalDataManagementSystem().getUrlTemplate();
        }
    }

    protected String maybeUnescape(String str)
    {
        return str;
    }

    @Override
    public String toString()
    {
        if (externalCode != null && urlTemplate != null)
        {
            String anExternalCode = maybeUnescape(externalCode);
            String aUrlTemplate = maybeUnescape(urlTemplate);

            return aUrlTemplate.replaceAll(BasicConstant.EXTERNAL_DMS_URL_TEMPLATE_CODE_PATTERN,
                    BasicURLEncoder.encode(anExternalCode));
        } else
        {
            return null;
        }
    }

}
