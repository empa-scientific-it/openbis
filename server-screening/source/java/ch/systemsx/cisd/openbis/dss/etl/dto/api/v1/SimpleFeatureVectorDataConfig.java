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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;

/**
 * Configuration for creating a feature vector data set.
 *
 * @author Franz-Josef Elmer
 */
public class SimpleFeatureVectorDataConfig
{
    private Properties properties;

    private IFeaturesBuilder featuresBuilder;

    /**
     * Creates an instance with undefined properties assuming that the {@link FeaturesBuilder} is used to create the feature vectors.
     */
    public SimpleFeatureVectorDataConfig()
    {
    }

    /**
     * Creates an instance for the specified properties object which is used to configure parsing of a feature vector file.
     */
    public SimpleFeatureVectorDataConfig(Properties properties)
    {
        this.properties = properties;
    }

    public IFeaturesBuilder getFeaturesBuilder()
    {
        if (featuresBuilder == null)
        {
            featuresBuilder = new FeaturesBuilder();
        }
        return featuresBuilder;
    }

    public Properties getProperties()
    {
        return properties;
    }

}
