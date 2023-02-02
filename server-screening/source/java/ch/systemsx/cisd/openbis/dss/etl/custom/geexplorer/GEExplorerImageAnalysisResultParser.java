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

package ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.geometry.ConversionUtils;

/**
 * XML parser for image analysis results from the GE Explorer image analysis application.
 * 
 * @author Bernd Rinn
 */
public class GEExplorerImageAnalysisResultParser extends DefaultHandler
{
    private static final String MEASURE_TAG = "Measure";

    private static final String WELL_TAG = "Well";

    private static final String DATA_TAG = "Data";

    private static final String ANALYSIS_TAG = "AutoLeadAnalysisProtocol";

    private static final String ASSAY_TAG = "AutoLeadAssay";

    private enum State
    {
        INIT, ANALYSIS, DATA, TABLE, WELL, FINISHED
    }

    private State state = State.INIT;

    private String analysisProtocolWithVersion = StringUtils.EMPTY;

    private String assayWithVersion = StringUtils.EMPTY;

    private String currentWellId;

    private HashSet<String> wells = new HashSet<String>();

    private LinkedHashSet<String> featureNames = new LinkedHashSet<String>();

    // well -> (featureName -> featureValue)
    private HashMap<String, HashMap<String, Number>> features =
            new HashMap<String, HashMap<String, Number>>();

    public GEExplorerImageAnalysisResultParser(String fileName)
            throws ParserConfigurationException, SAXException, IOException
    {
        assert fileName != null;

        parseDocument(fileName);
    }

    private void parseDocument(String fileName) throws ParserConfigurationException, SAXException,
            IOException
    {
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
                if (DATA_TAG.equals(qName))
                {
                    state = State.DATA;
                }
                break;
            case DATA:
                if (ANALYSIS_TAG.equals(qName))
                {
                    analysisProtocolWithVersion = parseNameAndVersion(attributes);
                    state = State.ANALYSIS;
                }
                break;
            case ANALYSIS:
                if (ASSAY_TAG.equals(qName))
                {
                    assayWithVersion = parseNameAndVersion(attributes);
                    state = State.TABLE;
                }
                break;
            case TABLE:
                if (WELL_TAG.equals(qName))
                {
                    final int row = Integer.parseInt(attributes.getValue("row"));
                    final int col = Integer.parseInt(attributes.getValue("col"));
                    currentWellId =
                            String.format("%s%02d",
                                    ConversionUtils.translateRowNumberIntoLetterCode(row), col);
                    wells.add(currentWellId);
                    features.put(currentWellId, new HashMap<String, Number>());
                    state = State.WELL;
                }
                break;
            case WELL:
                if (MEASURE_TAG.equals(qName))
                {
                    final String name = attributes.getValue("name");
                    featureNames.add(name);
                    final String valStr = attributes.getValue("value");
                    try
                    {
                        if (valStr.indexOf('.') >= 0 || valStr.indexOf('E') >= 0
                                || valStr.indexOf('e') >= 0)
                        {
                            final double value = Double.parseDouble(valStr);
                            features.get(currentWellId).put(name, value);
                        } else
                        {
                            final long value = Long.parseLong(valStr);
                            features.get(currentWellId).put(name, value);
                        }
                    } catch (NumberFormatException ex)
                    {
                        features.get(currentWellId).put(name, Double.NaN);
                    }
                }
                break;
            case FINISHED:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (state != State.WELL && state != State.TABLE)
        {
            return;
        }
        if (WELL_TAG.equals(qName))
        {
            state = State.TABLE;
        }
        if (DATA_TAG.equals(qName))
        {
            state = State.FINISHED;
        }
    }

    /**
     * Returns the list with all feature names found in the file.
     */
    public List<String> getFeatureNames()
    {
        return new ArrayList<String>(featureNames);
    }

    /**
     * Returns the list with all well ids found in the file, alphabetically ordered.
     */
    public List<String> getWellIds()
    {
        final ArrayList<String> list = new ArrayList<String>(wells);
        Collections.sort(list);
        return list;
    }

    /**
     * Returns the list with all features for the given <var>wellId</var>.
     */
    public HashMap<String, Number> getFeaturesForWell(String wellId)
    {
        return features.get(wellId);
    }

    /**
     * Writes a CSV file from the image analysis results found.
     */
    public void writeCSV(File resultFile) throws IOExceptionUnchecked
    {
        PrintStream out;
        try
        {
            out = new PrintStream(resultFile);
        } catch (FileNotFoundException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
        out.print("Well");
        final List<String> fNames = getFeatureNames();
        // Header
        for (String feature : fNames)
        {
            out.print("," + feature);
        }
        out.println();
        // Values
        for (String wellId : getWellIds())
        {
            out.print(wellId);
            final HashMap<String, Number> fValues = getFeaturesForWell(wellId);
            for (String name : fNames)
            {
                out.print("," + fValues.get(name));
            }
            out.println();
        }
        out.close();
    }

    public String getAnalysisProcedureName()
    {
        if (StringUtils.isEmpty(analysisProtocolWithVersion))
        {
            return assayWithVersion;
        } else
        {
            return assayWithVersion + "_" + analysisProtocolWithVersion;
        }
    }

    private String parseNameAndVersion(Attributes attributes)
    {
        String name = parseAttribute("name", attributes);
        String version = parseAttribute("version", attributes);
        if (StringUtils.isEmpty(version))
        {
            return name;
        } else
        {
            return name + "_v" + version;
        }
    }

    private String parseAttribute(String attrName, Attributes attributes)
    {
        String attrValue = attributes.getValue(attrName);
        return StringUtils.trimToEmpty(attrValue);
    }

}
