/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.verifier;

import static ch.systemsx.cisd.common.io.IOUtilities.crc32ToString;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationErrorType;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * Compares zip file headers to information from an external source, such as pathinfo db.
 * 
 * @author anttil
 */
public class ZipFileHeaderVerifier extends AbstractZipFileVerifier
{

    private final IArchiveFileMetaDataRepository fileMetaDataRepository;

    private final CrcEnabled crcEnabled;

    public ZipFileHeaderVerifier(IArchiveFileMetaDataRepository fileMetaDataRepository, CrcEnabled crcEnabled)
    {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.crcEnabled = crcEnabled;
    }

    @Override
    public List<VerificationError> verify(ZipFile zip)
    {
        List<VerificationError> result = new ArrayList<VerificationError>();

        String filename = zip.getName();
        String dataSetCode = filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
        String repositoryName = fileMetaDataRepository.getDescription();

        IArchiveFileContent metaData = fileMetaDataRepository.getMetaData(dataSetCode);
        if (metaData == null)
        {
            result.add(new VerificationError(VerificationErrorType.WARNING, "Could not find entry for dataset in " + repositoryName));
            return result;
        }

        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = entry.getName();

            if (entry.isDirectory() || entryName.equals(AbstractDataSetPackager.META_DATA_FILE_NAME))
            {
                continue;
            }

            Long externalSize = metaData.getFileSize(entryName);
            if (externalSize == null)
            {
                result.add(new VerificationError(VerificationErrorType.ERROR, "Could not find entry for file " + entryName + " in " + repositoryName));
                continue;
            }

            if (entry.getSize() != externalSize)
            {
                result.add(new VerificationError(VerificationErrorType.ERROR, entryName + ": size in archive file: " + entry.getSize() + ", size in "
                        + repositoryName + ": "
                        + externalSize));
            }

            Long externalCrc = metaData.getFileCrc(entryName);
            if (CrcEnabled.TRUE.equals(crcEnabled))
            {
                if (externalCrc == null)
                {
                    result.add(new VerificationError(VerificationErrorType.ERROR, entryName + ": no CRC32 found in " + repositoryName));

                } else if (externalCrc != entry.getCrc())
                {
                    result.add(new VerificationError(VerificationErrorType.ERROR, entryName + ": CRC32 in archive file: "
                            + crc32ToString((int) entry.getCrc()) + ", CRC32 in "
                            + repositoryName + ": "
                            + crc32ToString((int) externalCrc.longValue())));
                }
            } else if (externalCrc != null)
            {
                result.add(new VerificationError(VerificationErrorType.ERROR, entryName + ": CRC32 found in " + repositoryName
                        + " even it should be disabled. Value was "
                        + crc32ToString((int) externalCrc.longValue())));
            }
        }
        return result;
    }
}
