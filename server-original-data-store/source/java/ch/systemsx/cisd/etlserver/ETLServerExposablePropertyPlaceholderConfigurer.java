/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

public class ETLServerExposablePropertyPlaceholderConfigurer extends ExposablePropertyPlaceholderConfigurer
{
    @Override
    public Map<String, String> getDefaultValuesForMissingProperties()
    {
        Map<String, String> defaultValues = new HashMap<String, String>();
        defaultValues.put("download-url", "");
        return defaultValues;
    }
}
