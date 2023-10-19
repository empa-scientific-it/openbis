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

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

public final class ImagingDataSetExampleAdaptor implements IImagingDataSetAdaptor
{
    private final Properties properties;

    public ImagingDataSetExampleAdaptor(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Serializable process(IApplicationServerApi asApi, IDataStoreServerApi dssApi, File rootFile, Map<String, Serializable> previewConfig, Map<String, String> metaData)
    {
        return "SomeDummyResult";
    }
}
