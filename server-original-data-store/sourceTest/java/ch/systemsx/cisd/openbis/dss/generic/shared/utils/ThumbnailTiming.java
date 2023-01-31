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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.util.StopWatch;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Friend(toClasses = ImageUtil.class)
public class ThumbnailTiming
{

    public static void main(String[] args)
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Loading");
        File file =
                new File(
                        "/Users/cramakri/Documents/_streams/Local/Live/20100615-Screening/small-plate-examples/lmc/H055-1A/H055-1A_A02_02_dapi1.jpg");
        BufferedImage image = ImageUtil.loadImage(file);
        stopWatch.stop();
        stopWatch.start("Convert");
        BufferedImage thumbnail = ImageUtil.createThumbnailForDisplay(image, 120, 60, null);
        stopWatch.stop();
        stopWatch.start("Write");
        try
        {
            FileOutputStream output = new FileOutputStream("foo.png");
            ImageIO.write(thumbnail, "png", output);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }
}
