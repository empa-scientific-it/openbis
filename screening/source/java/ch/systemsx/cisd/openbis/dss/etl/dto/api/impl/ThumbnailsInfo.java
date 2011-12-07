package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;

/**
 * Stores the mapping between image and their thumbnails paths. Thread-safe class.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailsInfo
{
    public static class PhysicalDatasetInfo
    {
        private final String rootPath;

        private int thumbnailsWidth;

        private int thumbnailsHeight;

        public PhysicalDatasetInfo(String rootPath)
        {
            this.rootPath = rootPath;
        }
    }

    private final Map<RelativeImageFile, String> imageToThumbnailPathMap;

    private final Map<String, PhysicalDatasetInfo> datasetInfos;

    public ThumbnailsInfo()
    {
        this.imageToThumbnailPathMap = new HashMap<RelativeImageFile, String>();
        this.datasetInfos = new HashMap<String, ThumbnailsInfo.PhysicalDatasetInfo>();
    }

    public void putDataSet(String permId, String rootPath)
    {
        datasetInfos.put(permId, new PhysicalDatasetInfo(rootPath));
    }

    /**
     * Saves the path to the thumbnail for the specified image. The thumbnail path is relative and
     * starts with the name of the thumbnail file.
     */
    public synchronized void saveThumbnailPath(String permId, RelativeImageFile image,
            String thumbnailRelativePath, int width, int height)
    {
        imageToThumbnailPathMap.put(image, thumbnailRelativePath);

        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            datasetInfo.thumbnailsWidth = Math.max(datasetInfo.thumbnailsWidth, width);
            datasetInfo.thumbnailsHeight = Math.max(datasetInfo.thumbnailsHeight, height);
        }
    }

    public synchronized String getThumbnailPath(RelativeImageFile image)
    {
        return imageToThumbnailPathMap.get(image);
    }

    public Set<String> getThumbnailPhysicalDatasetsPermIds()
    {
        return datasetInfos.keySet();
    }

    public Size tryGetDimension(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            if (datasetInfo.thumbnailsWidth > 0 && datasetInfo.thumbnailsHeight > 0)
            {
                return new Size(datasetInfo.thumbnailsWidth, datasetInfo.thumbnailsHeight);
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder();
        ToStringUtil.appendNameAndObject(buffer, "number of thumbnails",
                imageToThumbnailPathMap.size());
        ToStringUtil.appendNameAndObject(buffer, "dataset perm ids: ", printPermIds());
        return buffer.toString();
    }

    private String printPermIds()
    {
        StringBuilder sb = new StringBuilder("[");
        boolean notFirst = false;
        for (String permId : datasetInfos.keySet())
        {
            if (notFirst)
            {
                sb.append(";");
                notFirst = true;
            }
            sb.append(" ").append(permId);
        }
        return sb.append(" ]").toString();
    }

    public String getRootPath(String permId)
    {
        return datasetInfos.get(permId).rootPath;
    }
}