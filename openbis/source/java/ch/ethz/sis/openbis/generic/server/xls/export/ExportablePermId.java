package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.EXPERIMENT;
import static ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.SAMPLE;

import java.util.Objects;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;

public class ExportablePermId
{

    private final ExportableKind exportableKind;

    private final ObjectPermId permId;

    public ExportablePermId(final ExportableKind exportableKind, final String permId)
    {
        this(exportableKind, createObjectPermIdByExportableKind(exportableKind, permId));
    }

    public ExportablePermId(final ExportableKind exportableKind, final ObjectPermId permId)
    {
        this.exportableKind = Objects.requireNonNull(exportableKind);
        this.permId = Objects.requireNonNull(permId);
    }

    private static ObjectPermId createObjectPermIdByExportableKind(final ExportableKind exportableKind,
            final String permId)
    {
        switch (exportableKind)
        {
            case SAMPLE_TYPE:
            {
                return new EntityTypePermId(permId, SAMPLE);
            }
            case EXPERIMENT_TYPE:
            {
                return new EntityTypePermId(permId, EXPERIMENT);
            }
            case DATASET_TYPE:
            {
                return new EntityTypePermId(permId, DATA_SET);
            }
            case VOCABULARY:
            {
                return new VocabularyPermId(permId);
            }
            case SPACE:
            {
                return new SpacePermId(permId);
            }
            case PROJECT:
            {
                return new ProjectPermId(permId);
            }
            case SAMPLE:
            {
                return new SamplePermId(permId);
            }
            case EXPERIMENT:
            {
                return new ExperimentPermId(permId);
            }
            case DATASET:
            {
                return new DataSetPermId(permId);
            }
            default:
            {
                throw new IllegalArgumentException(String.format("Unsupported exportable kind %s.", exportableKind));
            }
        }
    }

    public ExportableKind getExportableKind()
    {
        return exportableKind;
    }

    public ObjectPermId getPermId()
    {
        return permId;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ExportablePermId that = (ExportablePermId) o;

        if (exportableKind != that.exportableKind)
            return false;
        return permId.equals(that.permId);
    }

    @Override
    public int hashCode()
    {
        int result = exportableKind.hashCode();
        result = 31 * result + permId.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return exportableKind + " (" + permId + ")";
    }
    
}
