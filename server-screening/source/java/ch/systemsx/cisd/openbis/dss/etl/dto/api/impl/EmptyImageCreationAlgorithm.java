/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.IImageProvider;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IImageGenerationAlgorithm;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

public class EmptyImageCreationAlgorithm implements IImageGenerationAlgorithm, Serializable
{

    private static final long serialVersionUID = IServer.VERSION;

    @Override
    public String getDataSetTypeCode()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BufferedImage> generateImages(ImageDataSetInformation information,
            List<IDataSet> thumbnailDatasets, IImageProvider imageProvider)
    {
        return Collections.emptyList();
    }

    @Override
    public String getImageFileName(int index)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContent(IHierarchicalContent content)
    {
    }
}
