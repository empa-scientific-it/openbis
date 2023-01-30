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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;

/**
 * Stores Web Client configuration.
 * 
 * @author Izabela Adamczyk
 */
public class WebClientConfiguration implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /**
     * The key is here the pattern of the display id of the section.<br>
     * Warning: this requires changing ids of the sections with a lot of care.
     */
    private Map<String, DetailViewConfiguration> views =
            new HashMap<String, DetailViewConfiguration>();

    private Map<String, Map<String, String>> technologyProperties =
            new HashMap<String, Map<String, String>>();

    private Set<String> dataSetTypePatternsWithImageOverview = new HashSet<String>();

    private ViewMode defaultViewMode;

    private boolean defaultAnonymousLogin;

    private int maxVisibleColumns;

    private int maxEntityVisits;

    private boolean enableTrash;

    private boolean allowAddingUnofficielTerms;
    
    private String sampleText;
    
    private String experimentText;

    private List<String> creatableDataSetTypePatternsWhitelist;

    private List<String> creatableDataSetTypePatternsBlacklist;

    public String getPropertyOrNull(String technology, String key)
    {
        Map<String, String> properties = technologyProperties.get(technology);
        return properties == null ? null : properties.get(key);
    }

    public void addPropertiesForTechnology(String technology, Map<String, String> properties)
    {
        technologyProperties.put(technology, properties);
    }

    public Map<String, DetailViewConfiguration> getViews()
    {
        return views;
    }

    public void setViews(Map<String, DetailViewConfiguration> views)
    {
        this.views = views;
    }

    public ViewMode getDefaultViewMode()
    {
        return defaultViewMode;
    }

    public void setDefaultViewMode(ViewMode defaultViewMode)
    {
        this.defaultViewMode = defaultViewMode;
    }

    public void setDefaultAnonymousLogin(boolean defaultAnonymousLogin)
    {
        this.defaultAnonymousLogin = defaultAnonymousLogin;
    }

    public boolean isDefaultAnonymousLogin()
    {
        return defaultAnonymousLogin;
    }

    public Set<String> getDataSetTypePatternsWithImageOverview()
    {
        return dataSetTypePatternsWithImageOverview;
    }

    public void setDataSetTypesWithImageOverview(Set<String> dataSetTypesWithImageOverview)
    {
        this.dataSetTypePatternsWithImageOverview = dataSetTypesWithImageOverview;
    }

    public int getMaxVisibleColumns()
    {
        return maxVisibleColumns;
    }

    public void setMaxVisibleColumns(int maxVisibleColumns)
    {
        this.maxVisibleColumns = maxVisibleColumns;
    }

    public int getMaxEntityVisits()
    {
        return maxEntityVisits;
    }

    public void setMaxEntityVisits(int maxEntityVisits)
    {
        this.maxEntityVisits = maxEntityVisits;
    }

    public boolean getAllowAddingUnofficialTerms()
    {
        return allowAddingUnofficielTerms;
    }

    public void setAllowAddingUnofficialTerms(boolean allowAddingUnofficialTerms)
    {
        this.allowAddingUnofficielTerms = allowAddingUnofficialTerms;
    }

    public String getSampleText()
    {
        return sampleText;
    }

    public void setSampleText(String sampleText)
    {
        this.sampleText = sampleText;
    }

    public String getExperimentText()
    {
        return experimentText;
    }

    public void setExperimentText(String experimentText)
    {
        this.experimentText = experimentText;
    }

    public boolean getEnableTrash()
    {
        return enableTrash;
    }

    public void setEnableTrash(boolean enableTrash)
    {
        this.enableTrash = enableTrash;
    }

    public List<String> tryCreatableDataSetTypePatternsWhitelist()
    {
        return creatableDataSetTypePatternsWhitelist;
    }

    public void setCreatableDataSetTypePatternsWhitelist(List<String> aList)
    {
        creatableDataSetTypePatternsWhitelist = aList;
    }

    public List<String> tryCreatableDataSetTypePatternsBlacklist()
    {
        return creatableDataSetTypePatternsBlacklist;
    }

    public void setCreatableDataSetTypePatternsBlacklist(List<String> aList)
    {
        creatableDataSetTypePatternsBlacklist = aList;
    }

    public WebClientConfiguration()
    {
    }

}
