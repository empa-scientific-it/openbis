/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.fasta.FastaUtilities;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.etlserver.plugins.GenericFastaFileBuilder.EntryType;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.BlastUtils;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistrationAndModificationDate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * This maintenance task creates a BLAST database for all files with defined file types of data set with matching data set types.
 *
 * @author Franz-Josef Elmer
 */
public class BlastDatabaseCreationMaintenanceTask implements IMaintenanceTask
{
    static final String DATASET_TYPES_PROPERTY = "dataset-types";

    static final String BLAST_TEMP_FOLDER_PROPERTY = "blast-temp-folder";

    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    static final String FILE_TYPES_PROPERTY = "file-types";

    static final String ENTITY_SEQUENCE_PROPERTIES_PROPERTY = "entity-sequence-properties";

    private static final String DEFAULT_LAST_SEEN_DATA_SET_FILE = "last-seen-data-set-for-BLAST-database-creation";

    private static final String DEFAULT_FILE_TYPES = ".fasta .fa .fsa .fastq";

    private static final String ID_DELIM = "+";

    private static final String DB_NAME_DELIM = "+";

    private static final Template ID_TEMPLATE = new Template("${entityKind}" + ID_DELIM + "${permId}"
            + ID_DELIM + "${propertyType}" + ID_DELIM + "${timestamp}");

    private static final String TIMESTAMP_TEMPLATE = "yyyyMMddHHmmss";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BlastDatabaseCreationMaintenanceTask.class);

    private File lastSeenDataSetFile;

    private List<Pattern> dataSetTypePatterns;

    private List<String> fileTypes;

    private File blastDatabasesFolder;

    private File tmpFolder;

    private String makeblastdb;

    private String makembindex;

    private List<Loader> loaders;

    protected BlastUtils blaster;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        blaster = getBlaster(properties);
        dataSetTypePatterns = PropertyUtils.getPatterns(properties, DATASET_TYPES_PROPERTY);
        fileTypes = Arrays.asList(properties.getProperty(FILE_TYPES_PROPERTY, DEFAULT_FILE_TYPES).split(" +"));
        operationLog.info("File types: " + fileTypes);
        lastSeenDataSetFile = getFile(properties, LAST_SEEN_DATA_SET_FILE_PROPERTY, DEFAULT_LAST_SEEN_DATA_SET_FILE);
        loaders = createLoaders(properties);
        if (dataSetTypePatterns.isEmpty() && loaders.isEmpty())
        {
            throw new ConfigurationFailureException("At least one of the two properties have to be defined: "
                    + DATASET_TYPES_PROPERTY + ", " + ENTITY_SEQUENCE_PROPERTIES_PROPERTY);
        }
        setUpBlastDatabasesFolder(properties);
        setUpBlastTempFolder(properties);
        String blastToolDirectory = blaster.getBLASTToolDirectory(properties);
        makeblastdb = blastToolDirectory + "makeblastdb";
        if (false == blaster.available())
        {
            makeblastdb = null;
        }
        makembindex = blastToolDirectory + "makembindex";

    }

    protected BlastUtils getBlaster(Properties properties)
    {
        return new BlastUtils(properties, getConfigProvider().getStoreRoot());
    }

    private List<Loader> createLoaders(Properties properties)
    {
        String property = properties.getProperty(ENTITY_SEQUENCE_PROPERTIES_PROPERTY);
        if (property == null)
        {
            return Collections.emptyList();
        }
        List<Loader> result = new ArrayList<Loader>();
        String[] definitions = property.split("[, ] *");
        for (int i = 0; i < definitions.length; i++)
        {
            String definition = definitions[i];
            try
            {
                result.add(new Loader(definition));
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException((i + 1) + " definition ("
                        + definition + ") in property '" + ENTITY_SEQUENCE_PROPERTIES_PROPERTY
                        + "' is invalid: " + ex.getMessage());
            }
        }
        return result;
    }

    private void setUpBlastDatabasesFolder(Properties properties)
    {
        blastDatabasesFolder = blaster.getBlastDatabaseFolder(properties, getConfigProvider().getStoreRoot());
        operationLog.info("BLAST databases folder: " + blastDatabasesFolder);
        if (blastDatabasesFolder.exists())
        {
            if (blastDatabasesFolder.isFile())
            {
                throw new ConfigurationFailureException("BLAST databases folder '" + blastDatabasesFolder
                        + "' is an existing file.");
            }
        } else
        {
            if (blastDatabasesFolder.mkdirs() == false)
            {
                throw new ConfigurationFailureException("Couldn't create BLAST databases folder '"
                        + blastDatabasesFolder + "'.");
            }
        }
    }

    private void setUpBlastTempFolder(Properties properties)
    {
        String tempFolderProperty = properties.getProperty(BLAST_TEMP_FOLDER_PROPERTY);
        if (tempFolderProperty == null)
        {
            tmpFolder = new File(blastDatabasesFolder, "tmp");
        } else
        {
            tmpFolder = new File(tempFolderProperty);
        }
        if (tmpFolder.exists())
        {
            boolean success = FileUtilities.deleteRecursively(tmpFolder);
            if (success == false)
            {
                operationLog.warn("Couldn't delete temp folder '" + tmpFolder + "'.");
            }
        }
        if (tmpFolder.mkdirs() == false)
        {
            throw new ConfigurationFailureException("Couldn't create temp folder '" + tmpFolder + "'.");
        }
        operationLog.info("Temp folder '" + tmpFolder + "' created.");
    }

    private File getFile(Properties properties, String pathProperty, String defaultPath)
    {
        return blaster.getFile(properties, pathProperty, defaultPath, getConfigProvider().getStoreRoot());
    }

    @Override
    public void execute()
    {
        if (makeblastdb == null)
        {
            return;
        }
        IEncapsulatedOpenBISService service = getOpenBISService();
        Map<SequenceType, VirtualDatabase> virtualDatabases = loadVirtualDatabases(service);

        createBlastDatabasesForDataSets(service, virtualDatabases);
        createBlastDatabasesForEntities(service, virtualDatabases);
        Collection<VirtualDatabase> values = virtualDatabases.values();
        for (VirtualDatabase virtualDatabase : values)
        {
            virtualDatabase.save();
        }
    }

    private void createBlastDatabasesForEntities(IEncapsulatedOpenBISService service,
            Map<SequenceType, VirtualDatabase> virtualDatabases)
    {
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_TEMPLATE);
        for (Loader loader : loaders)
        {
            Map<SequenceType, Sequences> map = loader.load(service);
            for (Entry<SequenceType, Sequences> entry : map.entrySet())
            {
                SequenceType sequenceType = entry.getKey();
                VirtualDatabase virtualDatabase = virtualDatabases.get(sequenceType);
                Sequences sequences = entry.getValue();
                String baseName = loader.getDefinition() + DB_NAME_DELIM
                        + dateFormat.format(sequences.getLatestModificationDate());
                if (databaseExist(baseName, sequenceType))
                {
                    virtualDatabase.keepDatabase(baseName);
                    continue;
                }
                GenericFastaFileBuilder builder = new GenericFastaFileBuilder(tmpFolder, baseName);
                for (Sequence sequence : sequences.getSequences())
                {
                    Template template = ID_TEMPLATE.createFreshCopy();
                    template.bind("entityKind", loader.getEntityKind());
                    template.bind("permId", sequence.getPermId());
                    template.bind("propertyType", sequence.getPropertyType());
                    template.bind("timestamp", dateFormat.format(sequence.getModificationDate()));
                    String id = template.createText();
                    builder.startEntry(EntryType.FASTA, id, sequenceType);
                    builder.appendToSequence(sequence.getSequence());
                }
                createBlastDatabases(builder, virtualDatabases);
            }
        }
    }

    private boolean databaseExist(String baseName, SequenceType sequenceType)
    {
        String[] fileNames = blastDatabasesFolder.list();
        if (fileNames != null)
        {
            for (String fileName : fileNames)
            {
                if (fileName.startsWith(BlastUtils.createDatabaseName(baseName, sequenceType)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void createBlastDatabasesForDataSets(IEncapsulatedOpenBISService service,
            Map<SequenceType, VirtualDatabase> virtualDatabases)
    {
        if (dataSetTypePatterns.isEmpty())
        {
            return;
        }
        IHierarchicalContentProvider contentProvider = getContentProvider();
        List<AbstractExternalData> dataSets = getDataSets(service);
        if (dataSets.isEmpty() == false)
        {
            operationLog.info("Scan " + dataSets.size() + " data sets for creating BLAST databases.");
        }
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.tryGetAsDataSet() != null && dataSet.isAvailable() && dataSetTypeMatches(dataSet))
            {
                try
                {
                    createBlastDatabase(dataSet, virtualDatabases, contentProvider);
                } catch (Exception ex)
                {
                    operationLog.error("Error caused by creating BLAST database for data set " + dataSet.getCode()
                            + ": " + ex.getMessage(), ex);
                }
            }
            updateLastSeenEventId(dataSet.getId());
        }
    }

    private boolean dataSetTypeMatches(AbstractExternalData dataSet)
    {
        String dataSetType = dataSet.getDataSetType().getCode();
        for (Pattern pattern : dataSetTypePatterns)
        {
            if (pattern.matcher(dataSetType).matches())
            {
                return true;
            }
        }
        return false;
    }

    private Map<SequenceType, VirtualDatabase> loadVirtualDatabases(IEncapsulatedOpenBISService service)
    {
        Map<SequenceType, VirtualDatabase> virtualDatabases = new EnumMap<SequenceType, VirtualDatabase>(SequenceType.class);
        for (SequenceType sequenceType : SequenceType.values())
        {
            VirtualDatabase virtualDatabase = new VirtualDatabase(blastDatabasesFolder, sequenceType);
            virtualDatabases.put(sequenceType, virtualDatabase);
        }
        for (DeletedDataSet deletedDataSet : service.listDeletedDataSets(null, null))
        {
            for (VirtualDatabase virtualDatabase : virtualDatabases.values())
            {
                virtualDatabase.deleteDatabase(deletedDataSet.getCode());
            }
        }
        return virtualDatabases;
    }

    private void createBlastDatabase(AbstractExternalData dataSet, Map<SequenceType, VirtualDatabase> virtualDatabases,
            IHierarchicalContentProvider contentProvider)
    {
        String dataSetCode = dataSet.getCode();
        FastaFileBuilderForDataSetFiles builder = new FastaFileBuilderForDataSetFiles(tmpFolder, dataSetCode);
        IHierarchicalContent content = contentProvider.asContent(dataSet);
        IHierarchicalContentNode rootNode = content.getRootNode();
        handle(rootNode, builder);
        createBlastDatabases(builder, virtualDatabases);
    }

    private void createBlastDatabases(GenericFastaFileBuilder builder, Map<SequenceType, VirtualDatabase> virtualDatabases)
    {
        builder.finish();
        String baseName = builder.getBaseName();
        SequenceType[] values = SequenceType.values();
        for (SequenceType sequenceType : values)
        {
            File fastaFile = builder.getTemporaryFastaFileOrNull(sequenceType);
            if (fastaFile == null)
            {
                continue;
            }
            String fastaFilePath = fastaFile.getAbsolutePath();
            String databaseName = FilenameUtils.removeExtension(fastaFile.getName());
            String databaseFile = new File(blastDatabasesFolder, databaseName).getAbsolutePath();
            String dbtype = sequenceType.toString().toLowerCase();
            boolean success = blaster.process(makeblastdb, "-in", fastaFilePath, "-dbtype", dbtype,
                    "-title", databaseName, "-out", databaseFile);
            if (success == false)
            {
                operationLog.error("Creation of BLAST database '" + databaseName
                        + "' failed. Temporary fasta file: " + fastaFile);
                break;
            }
            File databaseSeqFile = new File(databaseFile + ".nsq");
            if (databaseSeqFile.exists() && databaseSeqFile.length() > 1000000)
            {
                blaster.process(makembindex, "-iformat", "blastdb", "-input", databaseFile, "-old_style_index", "false");
            }
            VirtualDatabase virtualDatabase = virtualDatabases.get(sequenceType);
            virtualDatabase.addDatabase(baseName);
        }
        builder.cleanUp();
    }

    private void handle(IHierarchicalContentNode node, FastaFileBuilderForDataSetFiles builder)
    {
        if (node.isDirectory())
        {
            for (IHierarchicalContentNode childNode : node.getChildNodes())
            {
                handle(childNode, builder);
            }
        } else
        {
            String nodeName = node.getName();
            for (String fileType : fileTypes)
            {
                if (nodeName.endsWith(fileType))
                {
                    appendTo(builder, node);
                    break;
                }
            }
        }
    }

    private void appendTo(FastaFileBuilderForDataSetFiles builder, IHierarchicalContentNode node)
    {
        InputStream inputStream = node.getInputStream();
        BufferedReader bufferedReader = null;
        String relativePath = node.getRelativePath();
        builder.setFilePath(relativePath);
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                builder.handle(line);
            }
        } catch (IOException e)
        {
            throw new EnvironmentFailureException("Error while reading data from '" + relativePath
                    + "': " + e.getMessage(), e);
        } finally
        {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    private List<AbstractExternalData> getDataSets(IEncapsulatedOpenBISService service)
    {
        Long lastSeenEventId = getLastSeenEventId();
        if (lastSeenEventId == null)
        {
            lastSeenEventId = 0L;
        }
        TrackingDataSetCriteria criteria = new TrackingDataSetCriteria(lastSeenEventId);
        List<AbstractExternalData> dataSets = service.listNewerDataSets(criteria);
        Collections.sort(dataSets, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData d0, AbstractExternalData d1)
                {
                    long id0 = d0.getId();
                    long id1 = d1.getId();
                    return id0 > id1 ? 1 : (id0 < id1 ? -1 : 0);
                }
            });
        return dataSets;
    }

    private Long getLastSeenEventId()
    {
        Long result = null;
        if (lastSeenDataSetFile.exists())
        {
            try
            {
                result = Long.parseLong(FileUtilities.loadToString(lastSeenDataSetFile).trim());
            } catch (Exception ex)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Cannot load last seen event id from file :"
                            + lastSeenDataSetFile, ex);
                }
            }
        }
        return result;
    }

    private void updateLastSeenEventId(Long eventId)
    {
        FileUtilities.writeToFile(lastSeenDataSetFile, String.valueOf(eventId) + "\n");
    }

    IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    IEncapsulatedOpenBISService getOpenBISService()
    {
        return ServiceProvider.getOpenBISService();
    }

    IHierarchicalContentProvider getContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }

    private static final class Loader
    {
        private final String definition;

        private final String entityKind;

        private final EntityLoader entityLoader;

        private final String entityType;

        private final String propertyType;

        Loader(String definition)
        {
            this.definition = definition;
            String[] items = StringUtils.splitByWholeSeparator(definition, DB_NAME_DELIM);
            if (items.length != 3)
            {
                throw new IllegalArgumentException("Definition not in form <entity kind>+<entity type>+<property type>");
            }
            entityKind = items[0];
            entityLoader = EntityLoader.valueOf(entityKind);
            this.entityType = items[1];
            this.propertyType = items[2];
        }

        String getDefinition()
        {
            return definition;
        }

        String getEntityKind()
        {
            return entityKind;
        }

        Map<SequenceType, Sequences> load(IEncapsulatedOpenBISService service)
        {
            Map<SequenceType, Sequences> map = new EnumMap<SequenceType, Sequences>(SequenceType.class);
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, entityType));
            for (IEntityInformationHolderWithProperties entity : entityLoader.listEntities(service, searchCriteria))
            {
                List<IEntityProperty> properties = entity.getProperties();
                for (IEntityProperty property : properties)
                {
                    if (property.getPropertyType().getCode().equals(propertyType))
                    {
                        String sequence = normalize(property.tryGetAsString());
                        if (sequence != null)
                        {
                            SequenceType sequenceType = FastaUtilities.determineSequenceTypeOrNull(sequence);
                            if (sequenceType != null)
                            {
                                Sequence seq = new Sequence(entity, propertyType, sequenceType, sequence);
                                Sequences sequences = map.get(seq.getSequenceType());
                                if (sequences == null)
                                {
                                    sequences = new Sequences();
                                    map.put(seq.getSequenceType(), sequences);
                                }
                                sequences.addSequence(seq);
                            }
                        }
                        break;
                    }
                }
            }
            return map;
        }
    }

    private static final class Sequences
    {
        private final List<Sequence> sequences = new ArrayList<Sequence>();

        private Date latestModificationDate = new Date(0);

        void addSequence(Sequence sequence)
        {
            sequences.add(sequence);
            if (latestModificationDate.compareTo(sequence.getModificationDate()) < 0)
            {
                latestModificationDate = sequence.getModificationDate();
            }
        }

        List<Sequence> getSequences()
        {
            return sequences;
        }

        Date getLatestModificationDate()
        {
            return latestModificationDate;
        }
    }
    
    private static String normalize(String sequence)
    {
        if (sequence == null)
        {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sequence.length(); i++)
        {
            char c = sequence.charAt(i);
            if (Character.isWhitespace(c) == false)
            {
                builder.append(Character.toUpperCase(c));
            }
        }
        return builder.toString();
    }

    private static final class Sequence
    {
        private final String permId;

        private final Date modificationDate;

        private final String propertyType;

        private final String sequence;

        private final SequenceType sequenceType;

        @SuppressWarnings("rawtypes")
        Sequence(IEntityInformationHolderWithProperties entity, String propertyType, 
                SequenceType sequenceType, String sequence)
        {
            this.propertyType = propertyType;
            this.sequenceType = sequenceType;
            this.sequence = sequence;
            permId = entity.getPermId();
            Date date = null;
            if (entity instanceof CodeWithRegistrationAndModificationDate)
            {
                date = ((CodeWithRegistrationAndModificationDate) entity).getModificationDate();
            }
            modificationDate = date == null ? new Date() : date;
        }

        String getPermId()
        {
            return permId;
        }

        Date getModificationDate()
        {
            return modificationDate;
        }

        String getPropertyType()
        {
            return propertyType;
        }

        String getSequence()
        {
            return sequence;
        }

        SequenceType getSequenceType()
        {
            return sequenceType;
        }

    }

    private static enum EntityLoader
    {
        DATA_SET()
        {
            @Override
            List<? extends IEntityInformationHolderWithProperties> listEntities(IEncapsulatedOpenBISService service,
                    SearchCriteria searchCriteria)
            {
                return service.searchForDataSets(searchCriteria);
            }
        },
        EXPERIMENT()
        {

            @Override
            List<? extends IEntityInformationHolderWithProperties> listEntities(IEncapsulatedOpenBISService service,
                    SearchCriteria searchCriteria)
            {
                return service.searchForExperiments(searchCriteria);
            }

        },
        SAMPLE()
        {
            @Override
            List<? extends IEntityInformationHolderWithProperties> listEntities(IEncapsulatedOpenBISService service,
                    SearchCriteria searchCriteria)
            {
                return service.searchForSamples(searchCriteria);
            }
        };

        abstract List<? extends IEntityInformationHolderWithProperties> listEntities(IEncapsulatedOpenBISService service,
                SearchCriteria searchCriteria);

    }

    private static class VirtualDatabase
    {
        private final File databaseFolder;

        private final String dbtype;

        private final String postfix;

        private final File databaseFile;

        private final Set<String> databasesToBeDeleted = new TreeSet<String>();

        private final Set<String> databasesToBeAdded = new TreeSet<String>();

        private final String virtualDatabaseFileType;

        VirtualDatabase(File databaseFolder, SequenceType sequenceType)
        {
            this.databaseFolder = databaseFolder;
            dbtype = sequenceType.toString().toLowerCase();
            postfix = BlastUtils.createDatabaseName("", sequenceType);
            virtualDatabaseFileType = sequenceType == SequenceType.NUCL ? ".nal" : ".pal";
            databaseFile = new File(databaseFolder, "all-" + dbtype + virtualDatabaseFileType);
            if (databaseFile.isFile())
            {
                List<String> lines = FileUtilities.loadToStringList(databaseFile);
                if (lines.size() == 2)
                {
                    StringTokenizer tokenizer = new StringTokenizer(lines.get(1));
                    tokenizer.nextToken(); // drop 'DBLIST' token
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        if (token.endsWith(postfix))
                        {
                            String name = token.substring(0, token.length() - postfix.length());
                            if (isEntityPropertyDatabase(name))
                            {
                                databasesToBeDeleted.add(name);
                            } else
                            {
                                databasesToBeAdded.add(name);
                            }
                        }
                    }
                }
            }
        }

        void keepDatabase(String baseName)
        {
            databasesToBeAdded.add(baseName);
            databasesToBeDeleted.remove(baseName);
        }

        void deleteDatabase(String baseName)
        {
            databasesToBeDeleted.add(baseName);
            databasesToBeAdded.remove(baseName);
        }

        void addDatabase(String name)
        {
            databasesToBeAdded.add(name);
        }

        private boolean isEntityPropertyDatabase(String name)
        {
            return name.contains(DB_NAME_DELIM);
        }

        void save()
        {
            File allDatabaseFile = new File(databaseFolder, "all-" + dbtype + virtualDatabaseFileType);
            File newAllDatabaseFile = new File(databaseFolder, "all-" + dbtype + virtualDatabaseFileType + ".new");
            if (databasesToBeAdded.isEmpty())
            {
                if (allDatabaseFile.exists())
                {
                    if (FileUtilities.delete(allDatabaseFile))
                    {
                        operationLog.info("Virtual BLAST database file " + allDatabaseFile
                                + " deleted because it was empty.");
                    } else
                    {
                        operationLog.warn("File deletion failed: " + allDatabaseFile);
                    }
                }
            } else
            {
                StringBuilder builder = new StringBuilder();
                builder.append("TITLE all-" + dbtype + "\nDBLIST");
                for (String database : databasesToBeAdded)
                {
                    builder.append(' ').append(database).append(postfix);
                }
                FileUtilities.writeToFile(newAllDatabaseFile, builder.toString());
                newAllDatabaseFile.renameTo(allDatabaseFile);
            }
            deleteDatabasesToBeDeleted();
        }

        private void deleteDatabasesToBeDeleted()
        {
            for (String database : databasesToBeDeleted)
            {
                final String databaseName = database + postfix;
                File[] files = databaseFolder.listFiles(new FileFilter()
                    {
                        @Override
                        public boolean accept(File file)
                        {
                            return file.getName().startsWith(databaseName);
                        }
                    });
                if (files != null && files.length > 0)
                {
                    boolean success = true;
                    for (File file : files)
                    {
                        if (FileUtilities.delete(file) == false)
                        {
                            operationLog.warn("File deletion failed: " + file);
                            success = false;
                        }
                    }
                    if (success)
                    {
                        operationLog.info("BLAST database " + databaseName + " successfully deleted.");
                    }
                }
            }
        }
    }
}
