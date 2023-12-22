/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.screenshot;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * @author anttil
 */
public class FileScreenShotter implements ScreenShotter
{
    private String directory;

    private TakesScreenshot driver;

    private int counter;
    
    private long previousFileSize;
    private long previousTimestamp;
    private static final long TEN_MINUTES = 10 * 60 * 1000;

    public FileScreenShotter(TakesScreenshot driver, String directory)
    {
        this.driver = driver;
        this.directory = directory;
        this.counter = 1;
    }

    @Override
    public void screenshot()
    {
        try
        {
            File file = driver.getScreenshotAs(OutputType.FILE);
            File target = new File(directory, "screenshot_" + String.format("%04d", counter) + ".png");
            FileUtils.copyFile(file, target);
            long fileSize = target.length();
            long timestamp = target.lastModified();
            System.out.println("SCREENSHOT: " + file.getAbsolutePath() + " -> " + target.getAbsolutePath()
                    + " (" + fileSize + ")");
            if (fileSize == previousFileSize && timestamp > previousTimestamp + TEN_MINUTES)
            {
                throw new RuntimeException("Unchanged screenshot after 10 minutes");
            }
            previousFileSize = fileSize;
            previousTimestamp = timestamp;
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        counter++;
    }

}
