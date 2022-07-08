package ch.ethz.sis.openbis.generic.server.xls.importer.enums;

public enum ImportTypes
{
    VOCABULARY_TYPE("VOCABULARY", false),
    VOCABULARY_TERM("VOCABULARYTERM", false),
    SAMPLE_TYPE("SAMPLETYPE", false),
    EXPERIMENT_TYPE("EXPERIMENTTYPE", false),
    DATASET_TYPE("DATASETTYPE", false),
    PROPERTY_TYPE("PROPERTYTYPE", false),
    SPACE(null, true),
    PROJECT(null, true),
    EXPERIMENT(null, true),
    SAMPLE(null, true),
    SCRIPT(null, false);

    private String type;

    private boolean metadata;

    public String getType()
    {
        return type;
    }

    public boolean isMetadata() {
        return metadata;
    }

    private ImportTypes(String type, boolean metadata)
    {
        this.type = type;
        this.metadata = metadata;
    }

}
