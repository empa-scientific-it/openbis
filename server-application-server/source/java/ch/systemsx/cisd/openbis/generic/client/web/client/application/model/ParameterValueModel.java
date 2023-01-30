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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;

/**
 * A {@link ModelData} implementation for {@link ParameterValue}.
 * 
 * @author Piotr Buczek
 */
public class ParameterValueModel extends SimplifiedBaseModel
{

    private static final long serialVersionUID = 1L;

    public ParameterValueModel(ParameterValue parameterValue)
    {
        set(ModelDataPropertyNames.CODE, parameterValue.getValue());
        set(ModelDataPropertyNames.TOOLTIP, parameterValue.getDescription());
        set(ModelDataPropertyNames.OBJECT, parameterValue);
    }

    public static final List<ParameterValueModel> convert(List<ParameterValue> parameterValues)
    {
        final ArrayList<ParameterValueModel> list = new ArrayList<ParameterValueModel>();
        for (ParameterValue v : parameterValues)
        {
            list.add(new ParameterValueModel(v));
        }
        return list;
    }

    public ParameterValue getParameterValue()
    {
        return (ParameterValue) get(ModelDataPropertyNames.OBJECT);
    }

}
