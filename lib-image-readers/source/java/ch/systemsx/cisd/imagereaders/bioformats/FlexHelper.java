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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import loci.common.RandomAccessInputStream;
import loci.formats.tiff.IFDList;
import loci.formats.tiff.TiffParser;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.geometry.SpatialPoint;

/**
 * A helper class with utility methods that parse metadata from FLEX files.
 * <p>
 * Clients can implement their own utility methods on the top of FlexHelper, by parsing the metadata
 * XML as returned by {@link #getMetadata()} or by directly executing XPath query via
 * {@link #selectByXpathQuery(String)}.
 * 
 * @author Kaloyan Enimanev
 */
public class FlexHelper
{

    /** Custom IFD entry for Flex XML. */
    private static final int FLEX = 65200;

    private static final String SELECT_CHANNELS = "//Arrays/Array/@Name";

    private static final String SELECT_TILE_FOR_IMAGE =
            "//Images/Image[@BufferNo='%s']/Sublayout/text()";

    private static final String SELECT_FILTER_REF_FOR_IMAGE =
            "//Images/Image[@BufferNo='%s']/FilterCombinationRef/text()";

    private static final String SELECT_LIGHTS_REF_FOR_IMAGE =
            "//Images/Image[@BufferNo='%s']/LightSourceCombinationRef/text()";

    private static final String SELECT_EMISSION_FOR_IMAGE =
            "//FilterCombinations/FilterCombination[@ID='%s']/SliderRef[@ID='%s']";

    private static final String SELECT_LASER_FOR_IMAGE =
            "//LightSourceCombinations/LightSourceCombination[@ID='%s']/LightSourceRef/@ID";

    private static final String SELECT_EXCITATION_FOR_IMAGE =
            "//LightSources/LightSource[@ID='%s']/Wavelength/text()";

    private static final String SELECT_TILE_XCOORDS = "//Sublayouts/Sublayout/Field/OffsetX/text()";

    private static final String SELECT_TILE_YCOORDS = "//Sublayouts/Sublayout/Field/OffsetY/text()";

    private final String file;

    private final String metadataXML;

    private final Document metadata;

    public FlexHelper(String file)
    {
        this.file = file;
        try
        {
            this.metadataXML = readMetadata();
            this.metadata = parseXmlDocument(metadataXML);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public int getTileNumber(int imageIdx)
    {
        String query = String.format(SELECT_TILE_FOR_IMAGE, imageIdx);
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String tile = nodeList.item(0).getNodeValue();
        return Integer.parseInt(tile.trim());
    }

    public String getChannelCode(int imageIdx)
    {
        NodeList nodeList = selectByXpathQuery(SELECT_CHANNELS);
        if (imageIdx < 0 || nodeList.getLength() <= imageIdx)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        return nodeList.item(imageIdx).getNodeValue();
    }

    public String getChannelDescription(int imageIdx)
    {
        String out =
                getChannelCode(imageIdx) + " - Ex:" + getExcitationTag(imageIdx) + "nm Em:"
                        + getEmissionTag(imageIdx) + "nm";
        return out;
    }

    public String getExpCam(int imageIdx)
    {
        NodeList nodeList = selectByXpathQuery(SELECT_CHANNELS);
        if (imageIdx < 0 || nodeList.getLength() <= imageIdx)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        return nodeList.item(imageIdx).getNodeValue();
    }

    public String getFilterRef(int imageIdx)
    {
        String query = String.format(SELECT_FILTER_REF_FOR_IMAGE, imageIdx);
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String filterRef = nodeList.item(0).getNodeValue();
        return filterRef.trim();
    }

    public String getLightSRef(int imageIdx)
    {
        String query = String.format(SELECT_LIGHTS_REF_FOR_IMAGE, imageIdx);
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String filterRef = nodeList.item(0).getNodeValue();
        return filterRef.trim();
    }

    public String getCamera(int imageIdx)
    {
        String expCam = getExpCam(imageIdx);
        String cam = "Camera" + expCam.split("Cam")[1];
        return cam.trim();
    }

    public String getEmissionTag(int imageIdx)
    {
        String query =
                String.format(SELECT_EMISSION_FOR_IMAGE, getFilterRef(imageIdx),
                        getCamera(imageIdx));
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String emissionTag = nodeList.item(0).getAttributes().getNamedItem("Filter").getNodeValue();
        return emissionTag.trim();

    }

    public String getExcitationTag(int imageIdx)
    {
        String query = String.format(SELECT_LASER_FOR_IMAGE, getLightSRef(imageIdx));
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + imageIdx);
        }
        String out = "";
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            out = out + getWaveLength(nodeList.item(i).getNodeValue()) + ",";
        }
        out = out.substring(0, out.length() - 1);
        return out;

    }

    public String getWaveLength(String laser)
    {
        String query = String.format(SELECT_EXCITATION_FOR_IMAGE, laser);
        NodeList nodeList = selectByXpathQuery(query);
        if (nodeList.getLength() == 0)
        {
            throw new IllegalArgumentException("No image can be matched to idx=" + laser);
        }
        String waveLength = nodeList.item(0).getNodeValue();
        return waveLength.trim();
    }

    public Map<Integer, SpatialPoint> getTileCoordinates()
    {
        Map<Integer, SpatialPoint> points = new HashMap<Integer, SpatialPoint>();
        List<Double> xCoords = selectDoubleList(SELECT_TILE_XCOORDS);
        List<Double> yCoords = selectDoubleList(SELECT_TILE_YCOORDS);
        for (int i = 0; i < xCoords.size(); i++)
        {
            int tile = i + 1;
            SpatialPoint point = new SpatialPoint(xCoords.get(i), yCoords.get(i));
            points.put(tile, point);
        }
        return points;
    }

    /**
     * Return the raw metadata as String, so that users can extend the API of the helper if needed.
     */
    public String getMetadata()
    {
        return metadataXML;
    }

    /**
     * Execute an XPath query directly on the metadata DOM.
     */
    public NodeList selectByXpathQuery(String xpathQuery)
    {
        try
        {
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(xpathQuery);
            return (NodeList) expr.evaluate(metadata, XPathConstants.NODESET);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private List<Double> selectDoubleList(String query)
    {
        NodeList nodeList = selectByXpathQuery(query);
        List<Double> result = new ArrayList<Double>();
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            String stringValue = nodeList.item(i).getNodeValue();
            Double doubleValue = Double.parseDouble(stringValue.trim());
            result.add(doubleValue);
        }
        return result;
    }

    private String readMetadata() throws Exception
    {
        RandomAccessInputStream in = new RandomAccessInputStream(file);
        try
        {
            TiffParser tiffParser = new TiffParser(in);
            final IFDList ifds = tiffParser.getIFDs();
            if (ifds != null && ifds.get(0) != null)
            {
                return ifds.get(0).getIFDStringValue(FLEX);
            } else
            {
                throw new IllegalArgumentException("Cannot parse Flex XML metadata from file "
                        + file);
            }
        } finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Parse given string as XML {@link Document}.
     * 
     * @throws UserFailureException if provided value is not a well-formed XML document
     */
    private Document parseXmlDocument(String value)
    {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setNamespaceAware(true);
        InputSource is = new InputSource(new StringReader(value));
        try
        {
            return dBF.newDocumentBuilder().parse(is);
        } catch (Exception e)
        {
            final String errorMessage =
                    String.format(
                            "Failed to parse FLEX metadata :\n\n%s\n\nisn't a well formed XML document. %s",
                            value, e.getMessage());
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
