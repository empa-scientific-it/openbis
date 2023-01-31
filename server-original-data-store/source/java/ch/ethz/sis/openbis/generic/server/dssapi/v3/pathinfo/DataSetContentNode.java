package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

interface DataSetContentNode
{
    long getLength();

    Integer getChecksumCRC32();

    String getFullPath();

    boolean isDirectory();
    
    String getChecksum();
}