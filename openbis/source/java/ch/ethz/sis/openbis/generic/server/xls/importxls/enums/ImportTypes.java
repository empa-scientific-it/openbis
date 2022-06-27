package ch.ethz.sis.openbis.generic.server.xls.importxls.enums;

public enum ImportTypes
{
    VOCABULARY_TYPE("VOCABULARY"),
    VOCABULARY_TERM("VOCABULARYTERM"),
    SAMPLE_TYPE("SAMPLETYPE"),
    EXPERIMENT_TYPE("EXPERIMENTTYPE"),
    DATASET_TYPE("DATASETTYPE"),
    PROPERTY_TYPE("PROPERTYTYPE"),
    SPACE(null),
    PROJECT(null),
    EXPERIMENT(null),
    SAMPLE(null),
    SCRIPT(null);

    private String type;

    public String getType()
    {
        return type;
    }

    private ImportTypes(String type)
    {
        this.type = type;
    }

}
