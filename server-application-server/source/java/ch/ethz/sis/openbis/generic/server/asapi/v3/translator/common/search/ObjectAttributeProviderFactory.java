/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.ContentCopyAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.DataSetAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.FileFormatTypeAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.LinkedDataAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.LocatorTypeAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.PhysicalDataAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search.StorageFormatAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.search.ExperimentAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms.search.ExternalDmsAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.search.MaterialAttributeProvider;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.search.SampleAttributeProvider;

/**
 * @author pkupczyk
 */
public class ObjectAttributeProviderFactory implements IObjectAttributeProviderFactory
{

    @Override
    public IObjectAttributeProvider getProvider(SearchObjectKind objectKind)
    {
        if (SearchObjectKind.SAMPLE.equals(objectKind))
        {
            return new SampleAttributeProvider();
        } else if (SearchObjectKind.EXPERIMENT.equals(objectKind))
        {
            return new ExperimentAttributeProvider();
        } else if (SearchObjectKind.DATA_SET.equals(objectKind))
        {
            return new DataSetAttributeProvider();
        } else if (SearchObjectKind.MATERIAL.equals(objectKind))
        {
            return new MaterialAttributeProvider();
        } else if (SearchObjectKind.PHYSICAL_DATA.equals(objectKind))
        {
            return new PhysicalDataAttributeProvider();
        } else if (SearchObjectKind.LINKED_DATA.equals(objectKind))
        {
            return new LinkedDataAttributeProvider();
        } else if (SearchObjectKind.STORAGE_FORMAT.equals(objectKind))
        {
            return new StorageFormatAttributeProvider();
        } else if (SearchObjectKind.LOCATOR_TYPE.equals(objectKind))
        {
            return new LocatorTypeAttributeProvider();
        } else if (SearchObjectKind.FILE_FORMAT_TYPE.equals(objectKind))
        {
            return new FileFormatTypeAttributeProvider();
        } else if (SearchObjectKind.EXTERNAL_DMS.equals(objectKind))
        {
            return new ExternalDmsAttributeProvider();
        } else if (SearchObjectKind.CONTENT_COPY.equals(objectKind))
        {
            return new ContentCopyAttributeProvider();
        } else
        {
            throw new IllegalArgumentException("Could not create object attribute provider for unknown object kind: " + objectKind);
        }
    }
}
