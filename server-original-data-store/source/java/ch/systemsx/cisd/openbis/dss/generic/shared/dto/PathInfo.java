/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Immutable class with path informations about a {@link IHierarchicalContentNode} and its descendants.
 * 
 * @author Franz-Josef Elmer
 */
public final class PathInfo
{
    private static final List<PathInfo> NO_CHILDREN = Collections.emptyList();

    public static PathInfo createPathInfo(IHierarchicalContentNode node, boolean computeChecksum, String checksumType)
    {
        if (node.exists() == false)
        {
            throw new IllegalArgumentException("File does not exist: " + node.getRelativePath());
        }
        PathInfo pathInfo = new PathInfo();
        pathInfo.fileName = node.getName();
        pathInfo.lastModifiedDate = new Date(node.getLastModified());
        pathInfo.directory = node.isDirectory();
        pathInfo.file = node.tryGetFile();
        if (pathInfo.directory)
        {
            pathInfo.children = createPathInfos(node, computeChecksum, checksumType);
            long sum = 0;
            for (PathInfo childInfo : pathInfo.children)
            {
                childInfo.parent = pathInfo;
                sum += childInfo.sizeInBytes;
            }
            pathInfo.sizeInBytes = sum;
        } else
        {
            pathInfo.sizeInBytes = node.getFileLength();
            setChecksum(pathInfo, node, computeChecksum, checksumType);
        }
        return pathInfo;
    }

    private static void setChecksum(PathInfo pathInfo, IHierarchicalContentNode node, 
            boolean computeChecksum, String checksumType)
    {
        if (computeChecksum == false)
        {
            return;
        }
        if (checksumType == null)
        {
            pathInfo.checksumCRC32 = node.getChecksumCRC32();
            return;
        } 
        setChecksum(pathInfo, node.getInputStream(), computeChecksum, checksumType);
    }
    
    public static void setChecksum(PathInfo pathInfo, InputStream inputStream, boolean computeChecksum, String checksumType)
    {
        if (computeChecksum)
        {
            if (checksumType == null)
            {
                pathInfo.checksumCRC32 = IOUtilities.getChecksumCRC32(inputStream);
            } else
            {
                MessageDigest messageDigest = PathInfo.getMessageDigest(checksumType);
                CRC32 crc = new CRC32();
                PathInfo.feedChecksumCalculators(inputStream, messageDigest, crc);
                pathInfo.checksumCRC32 = (int) crc.getValue();
                pathInfo.checksum = renderChecksum(checksumType, messageDigest);
            }
        }

    }

    private static String renderChecksum(String checksumType, MessageDigest messageDigest)
    {
        StringBuilder builder = new StringBuilder(checksumType).append(':');
        for (byte b : messageDigest.digest())
        {
            int v = b & 0xff;
            builder.append(Integer.toHexString(v >> 4)).append(Integer.toHexString(v & 0xf));
        }
        return builder.toString();
    }

    private static void feedChecksumCalculators(InputStream inputStream, MessageDigest messageDigest, CRC32 crc)
    {
        try
        {
            byte[] buffer = new byte[4096];
            int n = 0;
            while ((n = inputStream.read(buffer)) > 0) 
            {
                messageDigest.update(buffer, 0, n);
                crc.update(buffer, 0, n);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    static MessageDigest getMessageDigest(String checksumType)
    {
        try
        {
            return MessageDigest.getInstance(checksumType);
        } catch (NoSuchAlgorithmException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        
    }

    private static List<PathInfo> createPathInfos(IHierarchicalContentNode node, boolean computeChecksum, String checksumType)
    {
        if (node.isDirectory() == false)
        {
            throw new IllegalArgumentException("Not a folder: " + node.getRelativePath());
        }
        List<PathInfo> childInfos = new ArrayList<PathInfo>();
        List<IHierarchicalContentNode> childNodes = node.getChildNodes();
        for (IHierarchicalContentNode child : childNodes)
        {
            childInfos.add(createPathInfo(child, computeChecksum, checksumType));
        }
        Collections.sort(childInfos, new Comparator<PathInfo>()
            {
                @Override
                public int compare(PathInfo p1, PathInfo p2)
                {
                    return p1.getFileName().compareTo(p2.getFileName());
                }
            });
        return childInfos;
    }
    
    private File file;

    private String fileName;

    private long sizeInBytes;

    private Integer checksumCRC32;
    
    private String checksum;

    private PathInfo parent;

    private boolean directory;

    private List<PathInfo> children;

    private Date lastModifiedDate;

    public File getFile()
    {
        return file;
    }

    public String getFileName()
    {
        return fileName;
    }

    public long getSizeInBytes()
    {
        return sizeInBytes;
    }

    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public PathInfo getParent()
    {
        return parent;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public Date getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public List<PathInfo> getChildren()
    {
        return children == null ? NO_CHILDREN : Collections.unmodifiableList(children);
    }

}