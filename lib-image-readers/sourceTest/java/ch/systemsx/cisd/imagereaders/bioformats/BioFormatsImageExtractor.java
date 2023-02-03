/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import loci.formats.IFormatReader;
import loci.formats.gui.BufferedImageReader;

/**
 * Stand-alone application which extracts all images from any image format understood by BioFormats.
 * The images are stored in a folder as PNG images. Each argument is an image file. In case of no
 * arguments a file dialog pops up.
 * 
 * @author Franz-Josef Elmer
 */
public class BioFormatsImageExtractor
{
    public static void main(String[] args) throws Exception
    {
        String[] fileNames = args;
        if (args.length == 0)
        {
            FileDialog fileDialog = new FileDialog((Frame) null);
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.setVisible(true);
            String dir = fileDialog.getDirectory();
            fileNames = new String[] {dir + "/" + fileDialog.getFile()};
        }
        for (String fileName : fileNames)
        {
            IFormatReader reader = BioFormatsImageUtils.tryToCreateReaderForFile(fileName);
            if (reader == null)
            {
                System.out.println("No reader found: " + fileName);
                continue;
            }
            reader.setId(fileName);
            int seriesCount = reader.getSeriesCount();
            System.out.println("=========== file " + fileName + " has " + seriesCount + " series.");
            File imageFolder = new File(fileName + ".extracted-images");
            imageFolder.mkdirs();
            for (int s = 0; s < seriesCount; s++)
            {
                reader.setSeries(s);
                File serieFolder = new File(imageFolder, "s" + s);
                serieFolder.mkdirs();
                int effectiveSizeC = reader.getEffectiveSizeC();
                int sizeT = reader.getSizeT();
                int sizeZ = reader.getSizeZ();
                System.out.println(" Serie " + s + " has " + sizeT + " time points, "
                        + sizeZ + " focal planes and " + effectiveSizeC + " color channels.");
                for (int t = 0; t < sizeT; t++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        for (int c = 0; c < effectiveSizeC; c++)
                        {
                            int index = reader.getIndex(z, c, t);
                            BufferedImageReader biReader =
                                    BufferedImageReader.makeBufferedImageReader(reader);
                            BufferedImage image = biReader.openImage(index);
                            String imageName = "t" + t + "-z" + z + "-c" + c;
                            File imageFile = new File(serieFolder, imageName + ".png");
                            ImageIO.write(image, "PNG", imageFile);
                            System.out.println("Image extracted into " + imageFile + " (" + index + ")");
                        }
                    }
                }
            }
            reader.close();
        }
    }
}
