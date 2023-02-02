package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public final class ImageFileInfo extends ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageFileInfo
{

    private static final long serialVersionUID = 1L;

    public ImageFileInfo(String channelCode, int tileRow, int tileColumn, String imageRelativePath)
    {
        super(channelCode, tileRow, tileColumn, imageRelativePath);
    }
}