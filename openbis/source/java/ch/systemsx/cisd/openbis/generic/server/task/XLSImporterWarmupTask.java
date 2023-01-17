package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.XLSImport;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

import java.util.Map;
import java.util.Properties;

public class XLSImporterWarmupTask extends AbstractGroupMaintenanceTask {

    public static String DEFAULT_MAINTENANCE_TASK_NAME = "xls-importer-warmup-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = 24 * 60 * 60;

    XLSImporterWarmupTask(boolean configMandatory) {
        super(configMandatory);
    }


    @Override
    public void execute() {
        ImportOptions options = new ImportOptions();
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        XLSImport importXls = new XLSImport(null, api, Map.of(), ImportModes.UPDATE_IF_EXISTS, options, "DEFAULT");
    }

    @Override
    protected void setUpSpecific(Properties properties) {

    }
}
