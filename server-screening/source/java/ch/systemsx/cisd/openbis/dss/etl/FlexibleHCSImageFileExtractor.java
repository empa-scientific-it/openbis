/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A <code>IHCSImageFileExtractor</code> implementation which allows to have optionally time points and depth-scans.
 * <p>
 * Suitable for images processed by <i>iBrain2</i>, but not only.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class FlexibleHCSImageFileExtractor extends
        ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractor
{

    public FlexibleHCSImageFileExtractor(Properties properties)
    {
        super(properties);
    }

    @Override
    protected final ImageFileInfo tryExtractImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample)
    {
        UnparsedImageFileInfo unparsedInfo =
                UnparsedImageFileInfoLexer.tryExtractHCSImageFileInfo(imageFile,
                        incomingDataSetDirectory);
        if (unparsedInfo == null)
        {
            return null;
        }
        return tryExtractHCSImageInfo(unparsedInfo, imageFile, incomingDataSetDirectory);
    }

}
