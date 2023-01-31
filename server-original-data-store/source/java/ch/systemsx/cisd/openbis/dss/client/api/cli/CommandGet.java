/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.io.File;

import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.FileInfoDssDownloader;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Command that retrieves files in a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandGet extends AbstractDssCommand<CommandGet.CommandGetArguments>
{
    static class CommandGetArguments extends DataSetArguments
    {
        @Option(name = "l", longName = "link", usage = "Try to return a link to the data set in the DSS if possible, download the entire data set otherwise.")
        private boolean link = false;

        @Option(name = "e", longName = "store-override", usage = "An alternative path to the DSS Store root (only applicable with the link option)")
        private String storeOverride = "";

        @Option(name = "o", longName = "output", usage = "Path for output")
        private String output = "";

        public String getOutput()
        {
            return output;
        }

        public boolean isLink()
        {
            return link;
        }

        public String getStoreOverride()
        {
            return storeOverride;
        }
    }

    private static class DownloaderListener implements
            FileInfoDssDownloader.FileInfoDssDownloaderListener
    {
        private final File targetDir;

        DownloaderListener(File targetDir)
        {
            this.targetDir = targetDir;
        }

        @Override
        public void willDownload(FileInfoDssDTO fileInfo)
        {
            System.out.println("downloading " + getPathForFileInfo(fileInfo));
        }

        @Override
        public void willCreateDirectory(FileInfoDssDTO fileInfo)
        {
            System.out.println("mkdir " + getPathForFileInfo(fileInfo));
        }

        @Override
        public void didFinish()
        {
            System.out.println("Finished.");
        }

        private String getPathForFileInfo(FileInfoDssDTO fileInfo)
        {
            return targetDir.getPath() + "/" + fileInfo.getPathInListing();
        }
    }

    private static class CommandGetExecutor extends AbstractDataSetExecutor<CommandGetArguments>
    {
        CommandGetExecutor(CommandGetArguments arguments,
                AbstractDssCommand<CommandGetArguments> command)
        {
            super(arguments, command);
        }

        @Override
        protected void handle(FileInfoDssDTO[] fileInfos, IDataSetDss dataSet)
        {
            if (arguments.isLink())
            {
                handleLink(dataSet);
            } else
            {
                handleDownload(fileInfos, dataSet);
            }
        }

        private void handleLink(IDataSetDss dataSet)
        {
            File outputDir = getOutputDir();

            String storeOverride = null;
            if (arguments.getStoreOverride().length() > 0)
            {
                storeOverride = arguments.getStoreOverride();
            }
            File result = dataSet.getLinkOrCopyOfContents(storeOverride, outputDir);
            printResultFile(result);
        }

        private void handleDownload(FileInfoDssDTO[] fileInfos, IDataSetDss dataSet)
        {
            File outputDir = getOutputDir();
            outputDir.mkdirs();

            printResultFile(outputDir);

            FileInfoDssDownloader downloader =
                    new FileInfoDssDownloader(dataSet, fileInfos, outputDir,
                            new DownloaderListener(outputDir));
            downloader.downloadFiles();
        }

        public File getOutputDir()
        {
            File outputDir;
            if (arguments.getOutput().length() > 0)
            {
                // create the directory specified by output
                outputDir = new File(arguments.getOutput());
            } else
            {
                outputDir = new File(".");
            }
            return outputDir;
        }

        private void printResultFile(File result)
        {
            System.out.println("output dir :  " + result.getPath());
        }
    }

    CommandGet()
    {
        super(new CommandGetArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new CommandGetExecutor(arguments, this).execute(args);
    }

    @Override
    public String getName()
    {
        return "get";
    }
}
