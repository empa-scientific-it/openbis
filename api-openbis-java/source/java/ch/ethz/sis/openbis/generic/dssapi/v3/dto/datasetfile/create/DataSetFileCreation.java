package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("dss.dto.datasetfile.create.DataSetFileCreation")
public class DataSetFileCreation implements ICreation
{

    private static final long serialVersionUID = 1L;

    private String path;

    private boolean directory;

    private Long fileLength;

    private Integer checksumCRC32;

    private String checksum;

    private String checksumType;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public void setDirectory(boolean directory)
    {
        this.directory = directory;
    }

    public Long getFileLength()
    {
        return fileLength;
    }

    public void setFileLength(Long fileLength)
    {
        this.fileLength = fileLength;
    }

    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public void setChecksumCRC32(Integer checksumCRC32)
    {
        this.checksumCRC32 = checksumCRC32;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    public String getChecksumType()
    {
        return checksumType;
    }

    public void setChecksumType(String checksumType)
    {
        this.checksumType = checksumType;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("path", path).toString();
    }

}
