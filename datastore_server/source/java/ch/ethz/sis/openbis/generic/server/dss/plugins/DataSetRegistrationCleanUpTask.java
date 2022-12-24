/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationCleanUpTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "data-set-registration-clean-up-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = (int) (DateUtils.MILLIS_PER_DAY / 1000);

    public static final String MINIMUM_AGE_IN_DAYS = "minimum-age-in-days";

    public static final int DEFAULT_MINIMUM_AGE_IN_DAYS = 30;

    private static final int SHARDING_LEVELS = 3;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetRegistrationCleanUpTask.class);

    private long minimumAge;

    private ThreadParameters[] threadParameters;

    private File storeRoot;

    private ITimeProvider timeProvider;

    private IEncapsulatedOpenBISService openBisService;

    private MonitoredDirectories preStagingDirectories;

    private MonitoredDirectories stagingDirectories;

    private MonitoredDirectories preCommitDirectories;

    private Log4jSimpleLogger simpleLogger;

    private SimpleDateFormat dateFormat;

    public DataSetRegistrationCleanUpTask()
    {
    }

    DataSetRegistrationCleanUpTask(ThreadParameters[] threadParameters, File storeRoot,
            ITimeProvider timeProvider, IEncapsulatedOpenBISService openBisService)
    {
        this.threadParameters = threadParameters;
        this.storeRoot = storeRoot;
        this.timeProvider = timeProvider;
        this.openBisService = openBisService;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        minimumAge = PropertyUtils.getInt(properties, MINIMUM_AGE_IN_DAYS, DEFAULT_MINIMUM_AGE_IN_DAYS) * DateUtils.MILLIS_PER_DAY;
        preStagingDirectories = new MonitoredDirectories("pre-staging");
        stagingDirectories = new MonitoredDirectories("staging");
        preCommitDirectories = new MonitoredDirectories("pre-commit");
        File root = getStoreRoot();
        for (ThreadParameters parameters : getThreadParameters())
        {
            String shareId = ETLDaemon.getShareId(parameters, root);
            Properties tp = parameters.getThreadProperties();
            preStagingDirectories.add(TopLevelDataSetRegistratorGlobalState.getPreStagingDir(root, shareId, tp));
            stagingDirectories.add(TopLevelDataSetRegistratorGlobalState.getStagingDir(root, shareId, tp));
            preCommitDirectories.add(TopLevelDataSetRegistratorGlobalState.getPreCommitDir(root, shareId, tp));
        }
        simpleLogger = new Log4jSimpleLogger(operationLog);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }

    @Override
    public void execute()
    {
        long now = getTimeProvider().getTimeInMilliseconds();
        deleteOldFiles(preStagingDirectories, new PreStagingFileFilter(now, minimumAge));
        FileFilter ageBasedFileFilter = new AgeBasedFileFilter(now, minimumAge);
        deleteOldFiles(stagingDirectories, ageBasedFileFilter);
        deleteOldFiles(preCommitDirectories, ageBasedFileFilter);
        deleteEmptyFoldersInStore(ageBasedFileFilter);
    }

    private void deleteOldFiles(MonitoredDirectories dirs, FileFilter filter)
    {
        for (File dir : dirs.getDirectories())
        {
            File[] files = dir.listFiles(filter);
            if (files != null && files.length > 0)
            {
                operationLog.info(dirs.getName() + " directory " + dir.getAbsolutePath()
                        + " has " + files.length + " files");
                for (File file : files)
                {
                    operationLog.info(file.getName() + " (last modified: "
                            + dateFormat.format(new Date(file.lastModified())) + ")");
                    FileUtilities.deleteRecursively(file, simpleLogger);
                    operationLog.info("Stale folder deleted: " + file.getAbsolutePath());
                }
            }
        }
    }

    private void deleteEmptyFoldersInStore(FileFilter fileFilter)
    {
        DatabaseInstance homeDatabaseInstance = getOpenBisService().getHomeDatabaseInstance();
        String instanceHome = homeDatabaseInstance.getUuid();
        File[] shares = SegmentedStoreUtils.getShares(getStoreRoot());
        for (File share : shares)
        {
            Path shareCore = new File(share, instanceHome).toPath();
            int shareCorePathLevels = shareCore.getNameCount();
            EmptyFoldersGatherer gatherer = new EmptyFoldersGatherer(shareCorePathLevels, fileFilter);
            try (Stream<Path> paths = Files.walk(shareCore, SHARDING_LEVELS + 2))
            {
                paths.forEach(gatherer);
            } catch (Exception e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            }
            Set<Path> emptyFolders = gatherer.getEmptyFolders();
            Set<Path> emptyDataSets = gatherer.getEmptyDataSets();
            emptyFolders.removeAll(emptyDataSets);
            handleEmptyDataSets(share.getName(), emptyDataSets);
            handleEmptyFolder(share.getName(), emptyFolders);
        }
    }

    private void handleEmptyDataSets(String shareId, Collection<Path> emptyDataSets)
    {
        if (emptyDataSets.isEmpty())
        {
            return;
        }
        operationLog.info(emptyDataSets.size() + " old empty data set folders found in share " + shareId + ": " + emptyDataSets);
        List<String> dataSetCodes = emptyDataSets.stream().map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        List<AbstractExternalData> dataSets = getOpenBisService().listDataSetsByCode(dataSetCodes);
        Set<String> knownDataSets = collectAsTreeSet(dataSets.stream().map(AbstractExternalData::getCode));
        for (Path path : emptyDataSets)
        {
            if (knownDataSets.contains(path.getFileName().toString()) == false)
            {
                String postfix = "";
                try
                {
                    postfix = " (last modified: "
                            + dateFormat.format(new Date(Files.getLastModifiedTime(path).toMillis())) + ")";
                } catch (IOException e)
                {
                    // can not show last modified date
                }
                FileUtilities.deleteRecursively(path.toFile(), simpleLogger);
                operationLog.info("Stale data set folder deleted: " + path.toAbsolutePath() + postfix);
            }
        }
    }

    private void handleEmptyFolder(String shareId, Collection<Path> emptyFolders)
    {
        if (emptyFolders.isEmpty())
        {
            return;
        }
        operationLog.info(emptyFolders.size() + " empty sharding folders found in share " + shareId + ": " + emptyFolders);
    }

    private ThreadParameters[] getThreadParameters()
    {
        if (threadParameters == null)
        {
            threadParameters = ETLDaemon.getThreadParameters();
        }
        return threadParameters;
    }

    private File getStoreRoot()
    {
        if (storeRoot == null)
        {
            storeRoot = ServiceProvider.getConfigProvider().getStoreRoot();
        }
        return storeRoot;
    }

    private ITimeProvider getTimeProvider()
    {
        if (timeProvider == null)
        {
            timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        }
        return timeProvider;
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        if (openBisService == null)
        {
            openBisService = ServiceProvider.getOpenBISService();
        }
        return openBisService;
    }

    private final class EmptyFoldersGatherer implements Consumer<Path>
    {
        private final int shareCorePathLevels;

        private final FileFilter fileFilter;

        private final Set<Path> emptyFolders = new TreeSet<>();

        private final Set<Path> emptyDataSets = new TreeSet<>();

        private EmptyFoldersGatherer(int shareCorePathLevels, FileFilter fileFilter)
        {
            this.shareCorePathLevels = shareCorePathLevels;
            this.fileFilter = fileFilter;
        }

        @Override
        public void accept(Path path)
        {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
            {
                int nameCount = path.getNameCount();
                if (nameCount == shareCorePathLevels + SHARDING_LEVELS + 1)
                {
                    emptyDataSets.add(path);
                }
                if (nameCount > shareCorePathLevels + SHARDING_LEVELS + 1)
                {
                    emptyDataSets.remove(path.getParent());
                }
                if (nameCount > shareCorePathLevels
                        && nameCount <= shareCorePathLevels + SHARDING_LEVELS + 1)
                {
                    emptyFolders.add(path);
                }
                emptyFolders.remove(path.getParent());
            }
        }

        Set<Path> getEmptyDataSets()
        {
            return getOldFolders(emptyDataSets);
        }

        Set<Path> getEmptyFolders()
        {
            return getOldFolders(emptyFolders);
        }

        private TreeSet<Path> getOldFolders(Set<Path> folders)
        {
            return collectAsTreeSet(folders.stream().filter(path -> fileFilter.accept(path.toFile())));
        }

    }

    private static <T> TreeSet<T> collectAsTreeSet(Stream<T> stream)
    {
        return stream.collect(Collectors.toCollection(TreeSet::new));
    }

    private static final class MonitoredDirectories
    {
        private final String name;

        private final Set<File> directories;

        MonitoredDirectories(String name)
        {
            this.name = name;
            directories = new TreeSet<>();
        }

        String getName()
        {
            return name;
        }

        Set<File> getDirectories()
        {
            return directories;
        }

        void add(File directory)
        {
            directories.add(directory);
        }

        @Override
        public String toString()
        {
            if (directories.isEmpty())
            {
                return "no " + name + " directory";
            }
            if (directories.size() == 1)
            {
                return "1 " + name + " directory: " + directories.iterator().next().getAbsolutePath();
            }
            StringBuilder builder = new StringBuilder();
            builder.append(directories.size()).append(" ").append(name).append(" directories:");
            for (File directory : directories)
            {
                builder.append("\n").append(directory.getAbsolutePath());
            }
            return builder.toString();
        }
    }

    private static class AgeBasedFileFilter implements FileFilter
    {
        private final long now;

        private final long minimumAge;

        AgeBasedFileFilter(long now, long minimumAge)
        {
            this.now = now;
            this.minimumAge = minimumAge;
        }

        @Override
        public boolean accept(File file)
        {
            Date date = getDate(file);
            return date != null ? date.getTime() + minimumAge < now : false;
        }

        protected Date getDate(File file)
        {
            return new Date(file.lastModified());
        }
    }

    private static final class PreStagingFileFilter extends AgeBasedFileFilter
    {
        PreStagingFileFilter(long now, long minimumAge)
        {
            super(now, minimumAge);
        }

        @Override
        protected Date getDate(File file)
        {
            String dateStamp = file.getName().split("_")[0];
            try
            {
                return new SimpleDateFormat("yyyy-MM-dd").parse(dateStamp);
            } catch (ParseException e)
            {
                return null;
            }
        }
    }

}