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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipException;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationErrorType;

import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;

/**
 * Verifies integrity of a zip file by comparing its header checksums with real checksums of extracted files, like command "unzip -t".
 * 
 * @author anttil
 */
public class ZipFileIntegrityVerifier extends AbstractZipFileVerifier
{

    @Override
    public List<VerificationError> verify(ZipFile zip)
    {
        List<VerificationError> errors = new ArrayList<VerificationError>();

        Enumeration<?> entries = zip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            VerificationError error = checkZipEntry(zip, entry);
            if (error != null)
            {
                errors.add(error);
            }
        }
        return errors;
    }

    private VerificationError checkZipEntry(ZipFile zip, ZipEntry entry)
    {
        InputStream input = null;
        try
        {
            input = zip.getInputStream(entry);
            long crc = calculateCRC32(input);
            if (crc != entry.getCrc())
            {
                return new VerificationError(VerificationErrorType.ERROR, entry.getName() + ": CRC failure (calculated: " + crc32ToString((int) crc)
                        + ", zip file header: "
                        + crc32ToString((int) entry.getCrc()) + ")");
            }

        } catch (ZipException ex)
        {
            return new VerificationError(VerificationErrorType.ERROR, "Corrupted zip file entry, reason: " + ex.getMessage());
        } catch (IOException ex)
        {
            return new VerificationError(VerificationErrorType.ERROR, entry.getName() + ": " + ex.getMessage());
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException ex)
                {
                }
            }
        }
        return null;
    }
}
