package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.EnumSet;
import java.util.Set;

public enum ExportableKind
{
    SAMPLE_TYPE, EXPERIMENT_TYPE, DATASET_TYPE, VOCABULARY,
    SPACE, PROJECT, SAMPLE, EXPERIMENT, DATASET;
//    TODO:
//    USER, GROUP

    public static final Set<ExportableKind>
            MASTER_DATA_EXPORTABLE_KINDS = EnumSet.of(SAMPLE_TYPE, EXPERIMENT_TYPE, DATASET_TYPE, VOCABULARY);

}
