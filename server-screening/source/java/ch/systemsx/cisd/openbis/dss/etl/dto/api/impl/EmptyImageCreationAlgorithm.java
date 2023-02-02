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
