package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

class FileNode implements DataSetContentNode
{
    private final String fullPath;

    private final long length;

    private final Integer checksumCRC32;

    private final String checksum;

    public FileNode(String parentPath, String name, long length, Integer checksumCRC32, String checksum)
    {
        this.checksum = checksum;
        if (parentPath == null)
        {
            this.fullPath = name;
        } else
        {
            this.fullPath = parentPath + "/" + name;
        }
        this.length = length;
        this.checksumCRC32 = checksumCRC32;
    }

    @Override
    public long getLength()
    {
        return length;
    }

    @Override
    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    @Override
    public String getChecksum()
    {
        return checksum;
    }

    @Override
    public String getFullPath()
    {
        return this.fullPath;
    }

    @Override
    public boolean isDirectory()
    {
        return false;
    }

}
