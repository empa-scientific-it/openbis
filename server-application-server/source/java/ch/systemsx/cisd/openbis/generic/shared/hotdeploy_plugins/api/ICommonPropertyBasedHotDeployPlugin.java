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

package ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api;

import java.io.Serializable;
import java.util.EnumSet;

import ch.ethz.cisd.hotdeploy.Plugin;

/**
 * The base interface for property-based plugins with support for hot-deploy.
 * 
 * @author Bernd Rinn
 */
public interface ICommonPropertyBasedHotDeployPlugin extends Plugin
{
    public enum EntityKind implements Serializable
    {
        EXPERIMENT, SAMPLE, DATA_SET, MATERIAL;
    }

    /**
     * Returns a description for this plugin.
     */
    public String getDescription();

    /**
     * Returns set of supported entities of this property-based plugin.
     */
    public EnumSet<EntityKind> getSupportedEntityKinds();
}
