package ch.ethz.sis.openbis.generic.server.xls.export;

import org.apache.poi.ss.usermodel.Workbook;

import ch.ethz.sis.openbis.generic.server.xls.export.helper.IXLSExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSDataSetExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSDataSetTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSExperimentExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSExperimentTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSProjectExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSampleTypeExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSSpaceExportHelper;
import ch.ethz.sis.openbis.generic.server.xls.export.helper.XLSVocabularyExportHelper;

public class ExportHelperFactory
{

    private final XLSSampleTypeExportHelper sampleTypeExportHelper;

    private final XLSExperimentTypeExportHelper experimentTypeExportHelper;

    private final XLSDataSetTypeExportHelper dataSetTypeExportHelper;

    private final XLSVocabularyExportHelper vocabularyExportHelper;

    private final XLSSpaceExportHelper spaceExportHelper;

    private final XLSProjectExportHelper projectExportHelper;

    private final XLSExperimentExportHelper experimentExportHelper;

    private final XLSSampleExportHelper sampleExportHelper;

    private final XLSDataSetExportHelper dataSetExportHelper;

    final Workbook wb;

    ExportHelperFactory(final Workbook wb)
    {
        this.wb = wb;

        sampleTypeExportHelper = new XLSSampleTypeExportHelper(wb);
        experimentTypeExportHelper = new XLSExperimentTypeExportHelper(wb);
        dataSetTypeExportHelper = new XLSDataSetTypeExportHelper(wb);
        vocabularyExportHelper = new XLSVocabularyExportHelper(wb);
        spaceExportHelper = new XLSSpaceExportHelper(wb);
        projectExportHelper = new XLSProjectExportHelper(wb);
        experimentExportHelper = new XLSExperimentExportHelper(wb);
        sampleExportHelper = new XLSSampleExportHelper(wb);
        dataSetExportHelper = new XLSDataSetExportHelper(wb);
    }

    IXLSExportHelper getHelper(final ExportableKind exportableKind)
    {
        switch (exportableKind)
        {
            case SAMPLE_TYPE:
            {
                return sampleTypeExportHelper;
            }
            case EXPERIMENT_TYPE:
            {
                return experimentTypeExportHelper;
            }
            case DATASET_TYPE:
            {
                return dataSetTypeExportHelper;
            }
            case VOCABULARY:
            {
                return vocabularyExportHelper;
            }
            case SPACE:
            {
                return spaceExportHelper;
            }
            case PROJECT:
            {
                return projectExportHelper;
            }
            case EXPERIMENT:
            {
                return experimentExportHelper;
            }
            case SAMPLE:
            {
                return sampleExportHelper;
            }
            case DATASET:
            {
                return dataSetExportHelper;
            }
            default:
            {
                throw new IllegalArgumentException(String.format("Not supported exportable kind %s.", exportableKind));
            }
        }
    }

}
