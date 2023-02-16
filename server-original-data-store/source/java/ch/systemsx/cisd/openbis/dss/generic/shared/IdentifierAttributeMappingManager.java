/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class to load and provided from mapping of space/project/experiment identifier to attributes. If a mappings exist on experiment, project and
 * space level the experiment mapping will be used if the experiment of the data set fits. Otherwise the project mapping is tried.
 * 
 * @author Franz-Josef Elmer
 */
public class IdentifierAttributeMappingManager
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, IdentifierAttributeMappingManager.class);

    private List<Attributes> attributesList = new ArrayList<>();

    private final boolean createArchives;

    private final File mappingFileOrNull;

    private long mappingFileLastModified;

    private Long smallDataSetsSizeLimit;

    public IdentifierAttributeMappingManager(String mappingFilePathOrNull, boolean createArchives, Long smallDataSetsSizeLimit)
    {
        this.createArchives = createArchives;
        this.smallDataSetsSizeLimit = smallDataSetsSizeLimit;

        if (StringUtils.isBlank(mappingFilePathOrNull))
        {
            mappingFileOrNull = null;
        } else
        {
            mappingFileOrNull = new File(mappingFilePathOrNull);
            if (mappingFileOrNull.exists() == false)
            {
                throw new IllegalArgumentException("Mapping file '" + mappingFileOrNull + "' does not exist.");
            }
            getAttributesList(); // loads and validates mapping data
        }
    }

    private List<Attributes> getAttributesList()
    {
        if (mappingFileOrNull != null)
        {
            long lastModified = mappingFileOrNull.lastModified();
            if (lastModified != mappingFileLastModified)
            {
                attributesList = loadMappingFile(mappingFileOrNull);
                mappingFileLastModified = lastModified;
            }
        }
        return attributesList;
    }

    private List<Attributes> loadMappingFile(File mappingFile)
    {
        List<Attributes> newAttributesList = new ArrayList<>();
        CsvReader reader = null;
        try
        {
            reader = new CsvReader(mappingFile.getPath(), '\t');
            reader.setSkipEmptyRecords(true);
            reader.setUseComments(false);
            reader.setComment('#');
            reader.setTrimWhitespace(true);
            boolean success = reader.readHeaders();
            if (success == false)
            {
                throw new IllegalArgumentException("Empty mapping file: " + mappingFile);
            }
            while (reader.readRecord())
            {
                String[] row = reader.getValues();
                if (isEmptyRow(row))
                {
                    continue;
                }
                if (row.length != 3)
                {
                    throw new IllegalArgumentException("Invalid number of row elements in mapping file '"
                            + mappingFile + "': " + Arrays.asList(row));
                }
                Pattern identifierPattern = getIdentifierPattern(row[0]);
                List<String> shareIds = getShareIds(row[1]);
                ArchiveFolders archiveFolders = getArchiveFolder(row[2]);
                Attributes attributes = new Attributes(identifierPattern, shareIds, archiveFolders);
                newAttributesList.add(attributes);
            }
            attributesList = newAttributesList;
            operationLog.info("Mapping file '" + mappingFile + "' successfully loaded.");
            return newAttributesList;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private boolean isEmptyRow(String[] row)
    {
        for (String cell : row)
        {
            if (cell.length() > 0)
            {
                return false;
            }
        }
        return true;
    }

    private Pattern getIdentifierPattern(String regex)
    {
        return Pattern.compile(StringUtils.isBlank(regex) ? ".*" : regex.toUpperCase());
    }

    private ArchiveFolders getArchiveFolder(String folders)
    {
        if (StringUtils.isBlank(folders))
        {
            return null;
        } else
        {
            String[] folderPaths = folders.split(",");
            return ArchiveFolders.create(folderPaths, createArchives, smallDataSetsSizeLimit);
        }
    }

    private List<String> getShareIds(String shareIds)
    {
        List<String> ids = new ArrayList<String>();
        if (StringUtils.isBlank(shareIds))
        {
            return ids;
        }
        String[] splittedIds = shareIds.split(",");
        for (String id : splittedIds)
        {
            ids.add(id.trim());
        }
        return ids;
    }

    public Collection<File> getAllFolders()
    {
        Set<File> folders = new HashSet<File>();
        for (Attributes attributes : getAttributesList())
        {
            ArchiveFolders archiveFolders = attributes.getArchiveFolders();

            if (archiveFolders != null)
            {
                for (File archiveFolder : archiveFolders.getAllFolders())
                {
                    if (archiveFolder != null && archiveFolder.exists())
                    {
                        folders.add(archiveFolder);
                    }
                }
            }
        }
        return folders;
    }

    public File getArchiveFolder(DatasetDescription dataSetDescription, File defaultFolder)
    {
        String spaceCode = dataSetDescription.getSpaceCode();
        String projectCode = dataSetDescription.getProjectCode();
        String experimentCode = dataSetDescription.getExperimentCode();
        Attributes attributes = tryGetExperimentAttributes(spaceCode, projectCode, experimentCode);

        if (hasArchiveFolder(dataSetDescription, attributes))
        {
            return attributes.getArchiveFolders().getFolder(dataSetDescription);
        }
        attributes = tryGetProjectAttributes(spaceCode, projectCode);
        if (hasArchiveFolder(dataSetDescription, attributes))
        {
            return attributes.getArchiveFolders().getFolder(dataSetDescription);
        }
        attributes = tryGetSpaceAttributes(spaceCode);
        if (hasArchiveFolder(dataSetDescription, attributes))
        {
            return attributes.getArchiveFolders().getFolder(dataSetDescription);
        }
        return defaultFolder;
    }

    private boolean hasArchiveFolder(DatasetDescription dataSetDescription, Attributes attributes)
    {
        return attributes != null && attributes.getArchiveFolders() != null && attributes.getArchiveFolders().getFolder(dataSetDescription) != null;
    }

    public List<String> getShareIds(SimpleDataSetInformationDTO dataSet)
    {
        String spaceCode = dataSet.getSpaceCode();
        String projectCode = dataSet.getProjectCode();
        String experimentCode = dataSet.getExperimentCode();
        Attributes attributes = tryGetExperimentAttributes(spaceCode, projectCode, experimentCode);
        if (hasShareIds(attributes))
        {
            return attributes.getShareIds();
        }
        attributes = tryGetProjectAttributes(spaceCode, projectCode);
        if (hasShareIds(attributes))
        {
            return attributes.getShareIds();
        }
        attributes = tryGetSpaceAttributes(spaceCode);
        if (hasShareIds(attributes))
        {
            return attributes.getShareIds();
        }
        return Collections.emptyList();
    }

    private boolean hasShareIds(Attributes attributes)
    {
        if (attributes == null)
        {
            return false;
        }
        List<String> shareIds = attributes.getShareIds();
        return shareIds != null && shareIds.isEmpty() == false;
    }

    private Attributes tryGetExperimentAttributes(String spaceCode, String projectCode, String experimentCode)
    {
        if (experimentCode == null)
        {
            return null;
        }
        String identifier = new ExperimentIdentifier(spaceCode, projectCode, experimentCode).toString();
        return tryGetAttributes(identifier);
    }

    private Attributes tryGetProjectAttributes(String spaceCode, String projectCode)
    {
        if (projectCode == null)
        {
            return null;
        }
        String identifier = new ProjectIdentifier(spaceCode, projectCode).toString();
        return tryGetAttributes(identifier);
    }

    private Attributes tryGetSpaceAttributes(String spaceCode)
    {
        return tryGetAttributes(new SpaceIdentifier(spaceCode).toString());
    }

    private Attributes tryGetAttributes(String identifier)
    {

        for (Attributes attributes : getAttributesList())
        {
            if (attributes.getIdentifierPattern().matcher(identifier).matches())
            {
                return attributes;
            }
        }
        return null;
    }

    private static final class Attributes
    {
        private final Pattern identifierPattern;

        private final ArchiveFolders archiveFolders;

        private final List<String> shareIds;

        Attributes(Pattern identifierPattern, List<String> shareIds, ArchiveFolders archiveFolders)
        {
            this.identifierPattern = identifierPattern;
            this.shareIds = shareIds;
            this.archiveFolders = archiveFolders;
        }

        public Pattern getIdentifierPattern()
        {
            return identifierPattern;
        }

        public List<String> getShareIds()
        {
            return shareIds;
        }

        public ArchiveFolders getArchiveFolders()
        {
            return archiveFolders;
        }

    }

}
