/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.path;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MockPathsInfoDAO implements IPathsInfoDAO
{
    private final StringWriter writer = new StringWriter();
    private final PrintWriter out = new PrintWriter(writer, true);
    
    private Long dataSetId;
    private Date registrationTimestampOfLastFeedingEvent;
    private List<PathEntryDTO> dataSetFilesWithUnkownChecksum;
    private Map<Long, RuntimeException> exceptions = new HashMap<>();
    
    private long nextId;

    public void setDataSetId(long dataSetId)
    {
        this.dataSetId = dataSetId;
    }

    public void setRegistrationTimestampOfLastFeedingEvent(Date registrationTimestampOfLastFeedingEvent)
    {
        this.registrationTimestampOfLastFeedingEvent = registrationTimestampOfLastFeedingEvent;
    }

    public void setDataSetFilesWithUnkownChecksum(List<PathEntryDTO> dataSetFilesWithUnkownChecksum)
    {
        this.dataSetFilesWithUnkownChecksum = dataSetFilesWithUnkownChecksum;
    }
    
    public void addException(Long parentFolderId, RuntimeException exception)
    {
        exceptions.put(parentFolderId, exception);
    }
    
    public String getLog()
    {
        return writer.toString();
    }

    @Override
    public void close(boolean commit)
    {
        out.println("close(" + commit + ")");
    }

    @Override
    public void close()
    {
        out.println("close()");
    }
    
    @Override
    public boolean isClosed()
    {
        return false;
    }

    @Override
    public void commit()
    {
        out.println("commit()");
    }

    @Override
    public void rollback()
    {
        out.println("rollback()");
    }

    @Override
    public void rollback(Savepoint savepoint)
    {
        out.println("rollback(" + savepoint + ")");
    }

    @Override
    public Savepoint setSavepoint()
    {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name)
    {
        return null;
    }

    @Override
    public Long tryGetDataSetId(String code)
    {
        return dataSetId;
    }

    @Override
    public long createDataSet(String code, String location)
    {
        out.println("createDataSet(code=" + code + ", location=" + location + ")");
        return nextId++;
    }

    @Override
    public void deleteDataSet(String code)
    {
        out.println("deleteDataSet(" + code + ")");
    }

    @Override
    public long createDataSetFile(long id, Long parentId, String relativePath, String fileName, 
            long sizeInBytes, boolean directory, Integer checksumCRC32, String checksum, Date lastModifiedDate)
    {
        out.println("createDataSetFile(" + render(id, parentId, relativePath, fileName, sizeInBytes, 
                directory, checksumCRC32, checksum) + ")");
        return nextId++;
    }

    @Override
    public void createDataSetFiles(Collection<PathEntryDTO> filePaths)
    {
        out.println("createDataSetFiles:");
        for (PathEntryDTO filePath : filePaths)
        {
            out.println("  " + render(filePath.getDataSetId(), filePath.getParentId(), 
                    filePath.getRelativePath(), filePath.getFileName(), filePath.getSizeInBytes(),
                    filePath.isDirectory(), filePath.getChecksumCRC32(), filePath.getChecksum()));
        }
    }
    
    private String render(long id, Long parentId, String relativePath, String fileName, long sizeInBytes, 
            boolean directory, Integer checksumCRC32, String checksum)
    {
        RuntimeException exception = exceptions.get(parentId);
        if (exception != null)
        {
            out.println("ERROR:" + exception);
            throw exception;
        }
        return id + ", parent=" + parentId + ", " + fileName + " (" + relativePath + ", " + sizeInBytes + ", "
                + (directory ? "d" : "f") + (checksumCRC32 == null ? "" : ", checksumCRC32=" + Integer.toHexString(checksumCRC32))
                + (checksum == null ? "" : ", checksum=" + checksum) + ")";
    }

    @Override
    public Date getRegistrationTimestampOfLastFeedingEvent()
    {
        return registrationTimestampOfLastFeedingEvent;
    }

    @Override
    public void deleteLastFeedingEvent()
    {
        out.println("deleteLastFeedingEvent()");
    }

    @Override
    public void createLastFeedingEvent(Date registrationTimestamp)
    {
        out.println("createLastFeedingEvent(" + registrationTimestamp + ")");
        System.out.println("RTS:"+registrationTimestamp);
        registrationTimestampOfLastFeedingEvent = registrationTimestamp;
    }

    @Override
    public List<PathEntryDTO> listDataSetFilesWithUnkownChecksum()
    {
        return dataSetFilesWithUnkownChecksum;
    }

    @Override
    public void updateChecksum(long id, int checksumCRC32, String checksum)
    {
        out.println("updateChecksum(" + id + ", " + checksumCRC32 + ", " + checksum + ")");
    }

    @Override
    public List<PathEntryDTO> listDataSetsSize(String[] dataSetCodes)
    {
        return null;
    }
    
}