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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Properties;

import javax.servlet.Servlet;

import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;

/**
 * Manager of {@link Servlet} properties.
 *
 * @author Franz-Josef Elmer
 */
public interface IServletPropertiesManager
{
    /**
     * Adds servlets properties from specified section properties.
     * 
     * @param keyPrefix Prefix added to section key for error messaging.
     */
    public void addServletsProperties(String keyPrefix, SectionProperties[] servletsProperties);

    /**
     * Adds specified servlet properties.
     * 
     * @param propertiesName Name of the servlet properties used for error messaging.
     */
    public void addServletProperties(String propertiesName, Properties properties);

}