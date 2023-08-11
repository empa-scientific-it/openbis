/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.asapi.v3.dto.importer;

import java.util.Map;

public class ImportOptions
{

    private ImportModes importMode;

    private Map<String, String> experimentsByType;

    private Map<String, String> spacesByType;

    private boolean definitionsOnly = false;

    private boolean disallowEntityCreations = false;

    private boolean ignoreVersioning = false;

    private boolean renderResult = true;

    private boolean allowProjectSamples = true;

    public ImportModes getImportMode()
    {
        return importMode;
    }

    public void setImportMode(final ImportModes importMode)
    {
        this.importMode = importMode;
    }

    public Map<String, String> getExperimentsByType() {
        return experimentsByType;
    }

    public void setExperimentsByType(Map<String, String> experimentsByType) {
        this.experimentsByType = experimentsByType;
    }

    public Map<String, String> getSpacesByType() {
        return spacesByType;
    }

    public void setSpacesByType(Map<String, String> spacesByType) {
        this.spacesByType = spacesByType;
    }

    public boolean getDefinitionsOnly() {
        return definitionsOnly;
    }

    public void setDefinitionsOnly(boolean definitionsOnly) {
        this.definitionsOnly = definitionsOnly;
    }

    public boolean getDisallowEntityCreations() {
        return disallowEntityCreations;
    }

    public void setDisallowEntityCreations(boolean disallowEntityCreations) {
        this.disallowEntityCreations = disallowEntityCreations;
    }

    public boolean getIgnoreVersioning() {
        return ignoreVersioning;
    }

    public void setIgnoreVersioning(boolean ignoreVersioning) {
        this.ignoreVersioning = ignoreVersioning;
    }

    public boolean getRenderResult() {
        return renderResult;
    }

    public void setRenderResult(boolean renderResult) {
        this.renderResult = renderResult;
    }

    public boolean getAllowProjectSamples() {
        return allowProjectSamples;
    }

    public void setAllowProjectSamples(boolean allowProjectSamples) {
        this.allowProjectSamples = allowProjectSamples;
    }

}
