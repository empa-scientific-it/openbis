package ch.ethz.sis.openbis.generic.server.xls.importer;

import java.util.Map;

public class ImportOptions
{
    private Map<String, String> experimentsByType;

    private Map<String, String> spacesByType;

    private boolean definitionsOnly = false;

    private boolean disallowEntityCreations = false;

    private boolean ignoreVersioning = false;

    private boolean renderResult = true;

    private boolean allowProjectSamples = true;

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
