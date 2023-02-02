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

package ch.systemsx.cisd.openbis.dss.etl.custom.incell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML parser for image metadata (xdce files) from the Incell 2000 microscopes.
 * 
 * @author Tomasz Pylak
 */
public class IncellImageMetadataParser extends DefaultHandler
{
    private static final String LEVEL1_TAG = "ImageStack";

    private static final String LEVEL2_TAG = "AutoLeadAcquisitionProtocol";

    private static final String WAVELENGTHS_TAG = "Wavelengths";

    private static final String WAVELENGTH_TAG = "Wavelength";

    private static final String EMISSION_FILTER_TAG = "EmissionFilter";

    private static final String CHANNEL_CODE_ATTR = "name";

    private static final String CHANNEL_WAVELENGTH_ATTR = "wavelength";

    private enum State
    {
        INIT, LEVEL1, LEVEL2, WAVELENGTHS, WAVELENGTH, EMISSION_FILTER, FINISHED
    }

    private State state = State.INIT;

    private List<String> channelCodes = new ArrayList<String>();

    private List<String> channelWavelengths = new ArrayList<String>();

    public IncellImageMetadataParser(String fileName) throws ParserConfigurationException,
            SAXException, IOException
    {
        assert fileName != null;

        final SAXParserFactory spf = SAXParserFactory.newInstance();
        final SAXParser sp = spf.newSAXParser();
        sp.parse(fileName, this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
        switch (state)
        {
            case INIT:
                if (LEVEL1_TAG.equals(qName))
                {
                    state = State.LEVEL1;
                }
                break;
            case LEVEL1:
                if (LEVEL2_TAG.equals(qName))
                {
                    state = State.LEVEL2;
                }
                break;
            case LEVEL2:
                if (WAVELENGTHS_TAG.equals(qName))
                {
                    state = State.WAVELENGTHS;
                }
                break;
            case WAVELENGTHS:
                if (WAVELENGTH_TAG.equals(qName))
                {
                    state = State.WAVELENGTH;
                }
                break;
            case WAVELENGTH:
                if (EMISSION_FILTER_TAG.equals(qName))
                {
                    channelCodes.add(parseChannelCode(attributes));
                    channelWavelengths.add(parseChannelWavelength(attributes));
                    state = State.EMISSION_FILTER;
                }
                break;
            case EMISSION_FILTER:
                break;
            case FINISHED:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (EMISSION_FILTER_TAG.equals(qName) && state == State.EMISSION_FILTER)
        {
            state = State.WAVELENGTH;
        }
        if (WAVELENGTH_TAG.equals(qName) && state == State.WAVELENGTH)
        {
            state = State.WAVELENGTHS;
        }
        if (WAVELENGTHS_TAG.equals(qName) && state == State.WAVELENGTHS)
        {
            state = State.FINISHED;
        }
    }

    /**
     * Returns the list with all channel codes.
     */
    public String[] getChannelCodes()
    {
        return channelCodes.toArray(new String[0]);
    }

    /**
     * Returns the list with all channel wavelengths.
     */
    public int[] getChannelWavelengths()
    {
        int[] wavelengths = new int[channelWavelengths.size()];
        for (int i = 0; i < wavelengths.length; i++)
        {
            wavelengths[i] = Integer.parseInt(channelWavelengths.get(i));
        }
        return wavelengths;
    }

    private String parseChannelCode(Attributes attributes)
    {
        return parseAttribute(CHANNEL_CODE_ATTR, attributes);
    }

    private String parseChannelWavelength(Attributes attributes)
    {
        return parseAttribute(CHANNEL_WAVELENGTH_ATTR, attributes);
    }

    private String parseAttribute(String attrName, Attributes attributes)
    {
        String attrValue = attributes.getValue(attrName);
        return StringUtils.trimToEmpty(attrValue);
    }

}
