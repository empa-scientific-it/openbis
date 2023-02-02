/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

/**
 * Allows to define feature vectors of one image analysis dataset.
 * 
 * @author Tomasz Pylak
 */
public interface IFeaturesBuilder
{
    /** Defines a container to which values of the feature for each well can be added. */
    public IFeatureDefinition defineFeature(String featureCode);

}
