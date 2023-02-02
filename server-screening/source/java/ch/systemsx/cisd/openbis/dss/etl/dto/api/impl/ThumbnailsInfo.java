package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ThumbnailsStorageFormat.FileFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ToStringUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Stores the mapping between image and their thumbnails paths. Thread-safe class.
 * 
 * @author Tomasz Pylak
 */
public class ThumbnailsInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static class PhysicalDatasetInfo implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String rootPath;

        private int thumbnailsWidth;

        private int thumbnailsHeight;

        private Integer colorDepth;

        private FileFormat fileType;

        private Map<String, String> transformations;

        public PhysicalDatasetInfo(String rootPath, FileFormat fileType,
                Map<String, String> transformations)
        {
            this.rootPath = rootPath;
            this.fileType = fileType;
            this.transformations = transformations;
        }
    }

    private final Map<RelativeImageFile, HashMap<ColorComponent, String>> imageToThumbnailPathMap;

    private final Map<String, PhysicalDatasetInfo> datasetInfos;

    public ThumbnailsInfo()
    {
        this.imageToThumbnailPathMap =
                new HashMap<RelativeImageFile, HashMap<ColorComponent, String>>();
        this.datasetInfos = new HashMap<String, ThumbnailsInfo.PhysicalDatasetInfo>();
    }

    public void putDataSet(String permId, String rootPath, FileFormat fileFormat,
            Map<String, String> transformations)
    {
        datasetInfos.put(permId, new PhysicalDatasetInfo(rootPath, fileFormat, transformations));
    }

    /**
     * Saves the path to the thumbnail for the specified image. The thumbnail path is relative and starts with the name of the thumbnail file.
     */
    public synchronized void saveThumbnailPath(String permId, RelativeImageFile image,
            ColorComponent colorComponentOrNull, String thumbnailRelativePath, int width,
            int height, int colorDepth)
    {
        HashMap<ColorComponent, String> imageComponents = imageToThumbnailPathMap.get(image);
        if (imageComponents == null)
        {
            imageComponents = new HashMap<ColorComponent, String>();
            imageToThumbnailPathMap.put(image, imageComponents);
        }
        imageComponents.put(colorComponentOrNull, thumbnailRelativePath);

        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            datasetInfo.thumbnailsWidth = Math.max(datasetInfo.thumbnailsWidth, width);
            datasetInfo.thumbnailsHeight = Math.max(datasetInfo.thumbnailsHeight, height);
            datasetInfo.colorDepth = colorDepth;
        }
    }

    public synchronized String getThumbnailPath(RelativeImageFile image,
            ColorComponent colorComponentOrNull)
    {
        return imageToThumbnailPathMap.get(image).get(colorComponentOrNull);
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

    public Integer tryGetColorDepth(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            return datasetInfo.colorDepth;
        }
        return null;
    }

    public FileFormat getFileType(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        if (datasetInfo != null)
        {
            return datasetInfo.fileType;
        }
        return null;
    }

    public Map<String, String> getTransformations(String permId)
    {
        PhysicalDatasetInfo datasetInfo = datasetInfos.get(permId);
        return datasetInfo != null ? datasetInfo.transformations : null;
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