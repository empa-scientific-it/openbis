package ch.ethz.sis.openbis.generic.server.xls.export;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.ExportResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.AllFields;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.ExportOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.options.XlsTextFormat;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportablePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.exporter.data.ExportableKind;
import java.io.Serializable;
import java.util.*;

public class XLSExportExtendedService
{

    public static String export(String sessionToken, Map<String, Serializable> parameters) {
        System.out.println("sessionToken: " + sessionToken);
        System.out.println("parameters: " + parameters);

        // Root
        String kind = ((Map<String, String>) parameters.get("entity")).get("kind");
        String permId = ((Map<String, String>) parameters.get("entity")).get("permId");
        // Options
        boolean withEmail = (boolean) parameters.get("withEmail");
        boolean withImportCompatibility = (boolean) parameters.get("withImportCompatibility");
        // Formats
        boolean pdf = ((Map<String, Boolean>) parameters.get("formats")).get("pdf");
        boolean xlsx = ((Map<String, Boolean>) parameters.get("formats")).get("xlsx");
        boolean data = ((Map<String, Boolean>) parameters.get("formats")).get("data");
        // Inclusions
        boolean withLevelsBelow = (boolean) parameters.get("withLevelsBelow");
        boolean withObjectsAndDataSetsParents = (boolean) parameters.get("withObjectsAndDataSetsParents");
        boolean withObjectsAndDataSetsOtherSpaces = (boolean) parameters.get("withObjectsAndDataSetsOtherSpaces");

        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        ExportData exportData = new ExportData();
        ExportableKind rootKind = ExportableKind.valueOf(kind);
        ExportablePermId root = new ExportablePermId(rootKind, new ObjectPermId(permId));
        HashSet<ExportablePermId> collection = new HashSet<>();
        collectEntities(api, sessionToken, collection, root, withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
        exportData.setPermIds(new ArrayList<ExportablePermId>(collection));
        exportData.setFields(new AllFields());
        ExportOptions exportOptions = new ExportOptions();
        Set<ExportFormat> formats = new HashSet<>();
        if (pdf) {
            formats.add(ExportFormat.PDF);
        }
        if (xlsx) {
            formats.add(ExportFormat.XLSX);
        }
        if (data) {
            formats.add(ExportFormat.DATA);
        }
        exportOptions.setFormats(formats);
        exportOptions.setXlsTextFormat(XlsTextFormat.RICH);
        exportOptions.setWithReferredTypes(Boolean.TRUE);
        exportOptions.setWithImportCompatibility(withImportCompatibility);
        exportOptions.setZipSingleFiles(Boolean.TRUE);

        ExportThread exportThread = new ExportThread(api, sessionToken, exportData, exportOptions, withEmail);
        if (withEmail) {
            Thread thread = new Thread(exportThread);
            thread.start();
            return Boolean.TRUE.toString();
        } else {
            exportThread.run();
            return exportThread.getExportResult().getDownloadURL();
        }
    }

    private static class ExportThread implements Runnable {

        private final IApplicationServerInternalApi api;
        private final String sessionToken;
        private final ExportData exportData;
        private final ExportOptions exportOptions;
        private final boolean withEmail;
        private ExportResult exportResult = null;

        public ExportThread(IApplicationServerInternalApi api,
                String sessionToken,
                ExportData exportData,
                ExportOptions exportOptions,
                boolean withEmail)
        {
            this.api = api;
            this.sessionToken = sessionToken;
            this.exportData = exportData;
            this.exportOptions = exportOptions;
            this.withEmail = withEmail;
        }

        @Override
        public void run()
        {
            exportResult = api.executeExport(sessionToken, exportData, exportOptions);
            if (withEmail) {
                sentEmail();
            }
        }

        private void sentEmail() {
            String downloadURL = exportResult.getDownloadURL();
            SessionInformation sessionInformation = api.getSessionInformation(sessionToken);
            EMailAddress eMailAddress = new EMailAddress(sessionInformation.getPerson().getEmail());
            IMailClient eMailClient = CommonServiceProvider.createEMailClient();
            String subject = "openBIS Export Download Ready";
            eMailClient.sendEmailMessage(subject, downloadURL, null, null, eMailAddress);
        }

        public ExportResult getExportResult() {
            return exportResult;
        }
    }

    private static void collectEntities(
            IApplicationServerInternalApi api,
            String sessionToken,
            HashSet<ExportablePermId> collection,
            ExportablePermId current,
            boolean withLevelsBelow,
            boolean withObjectsAndDataSetsParents,
            boolean withObjectsAndDataSetsOtherSpaces)
    {
        if (collection.contains(current)) { // Check to avoid loops
            return;
        }

        collection.add(current);

        if (withLevelsBelow)
        {
            switch (current.getExportableKind())
            {
                case SPACE:
                    SpaceFetchOptions spaceFetchOptions = new SpaceFetchOptions();
                    spaceFetchOptions.withProjects();
                    Map<ISpaceId, Space> spaces = api.getSpaces(sessionToken,
                            List.of(new SpacePermId(current.getPermId().getPermId())),
                            spaceFetchOptions);
                    for (Space space:spaces.values())
                    {
                        for (Project project:space.getProjects())
                        {
                            collectEntities(api, sessionToken, collection,
                                    new ExportablePermId(ExportableKind.PROJECT,
                                            new ObjectPermId(project.getPermId().getPermId())),
                                    withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                        }
                    }
                    break;
                case PROJECT:
                    ProjectFetchOptions projectFetchOptions = new ProjectFetchOptions();
                    projectFetchOptions.withExperiments();
                    Map<IProjectId, Project> projects = api.getProjects(sessionToken,
                            List.of(new ProjectPermId(current.getPermId().getPermId())),
                            projectFetchOptions);
                    for (Project project:projects.values())
                    {
                        for (Experiment experiment : project.getExperiments())
                        {
                            collectEntities(api, sessionToken, collection,
                                    new ExportablePermId(ExportableKind.EXPERIMENT,
                                            new ObjectPermId(experiment.getPermId().getPermId())),
                                        withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                        }
                    }
                    break;
                case EXPERIMENT:
                    ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
                    experimentFetchOptions.withSamples();
                    experimentFetchOptions.withDataSets();
                    Map<IExperimentId, Experiment> experiments = api.getExperiments(sessionToken,
                            List.of(new ExperimentPermId(current.getPermId().getPermId())),
                            experimentFetchOptions);
                    for (Experiment experiment:experiments.values()) {
                        String experimentSpaceCode = experiment.getIdentifier().getIdentifier().split("/")[1];
                        for (Sample sample:experiment.getSamples()) {
                            collectEntities(api, sessionToken, collection,
                                    new ExportablePermId(ExportableKind.SAMPLE,
                                            new ObjectPermId(sample.getPermId().getPermId())),
                                    withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                        }
                        for (DataSet dataSet:experiment.getDataSets()) {
                            collectEntities(api, sessionToken, collection,
                                    new ExportablePermId(ExportableKind.DATASET,
                                            new ObjectPermId(dataSet.getPermId().getPermId())),
                                    withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                        }
                    }
                    break;
                case SAMPLE:
                    SampleFetchOptions sampleFetchOptions = new SampleFetchOptions();
                    sampleFetchOptions.withChildren();
                    sampleFetchOptions.withDataSets();
                    if (withObjectsAndDataSetsParents) {
                        sampleFetchOptions.withParents();
                    }

                    Map<ISampleId, Sample> samples = api.getSamples(sessionToken,
                            List.of(new SamplePermId(current.getPermId().getPermId())),
                            sampleFetchOptions);

                    for (Sample sample:samples.values()) {
                        String sampleSpaceCode = sample.getIdentifier().getIdentifier().split("/")[1];
                        if (withObjectsAndDataSetsParents) {
                            for (Sample parent:sample.getParents()) {
                                String parentSpaceCode = parent.getIdentifier().getIdentifier().split("/")[1];
                                if (sampleSpaceCode.equals(parentSpaceCode) || withObjectsAndDataSetsOtherSpaces)
                                {
                                    collectEntities(api, sessionToken, collection,
                                            new ExportablePermId(ExportableKind.SAMPLE,
                                                    new ObjectPermId(parent.getPermId().getPermId())),
                                            withLevelsBelow, withObjectsAndDataSetsParents,
                                            withObjectsAndDataSetsOtherSpaces);
                                }
                            }
                        }
                        for (Sample child:sample.getChildren()) {
                            String childSpaceCode = child.getIdentifier().getIdentifier().split("/")[1];
                            if (sampleSpaceCode.equals(childSpaceCode) || withObjectsAndDataSetsOtherSpaces)
                            {
                                collectEntities(api, sessionToken, collection,
                                        new ExportablePermId(ExportableKind.SAMPLE,
                                                new ObjectPermId(child.getPermId().getPermId())),
                                        withLevelsBelow, withObjectsAndDataSetsParents,
                                        withObjectsAndDataSetsOtherSpaces);
                            }
                        }
                        for (DataSet dataSet:sample.getDataSets()) {
                            collectEntities(api, sessionToken, collection,
                                    new ExportablePermId(ExportableKind.DATASET,
                                            new ObjectPermId(dataSet.getPermId().getPermId())),
                                    withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                        }
                    }
                    break;
                case DATASET:
                    DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
                    dataSetFetchOptions.withChildren();
                    dataSetFetchOptions.withExperiment();
                    if (withObjectsAndDataSetsParents) {
                        dataSetFetchOptions.withParents().withExperiment();
                    }
                    Map<IDataSetId, DataSet> dataSets = api.getDataSets(sessionToken,
                            List.of(new DataSetPermId(current.getPermId().getPermId())),
                            dataSetFetchOptions);
                    for (DataSet dataset:dataSets.values()) {
                        String datasetSpaceCode = dataset.getExperiment().getIdentifier().getIdentifier().split("/")[1];

                        if (withObjectsAndDataSetsParents) {
                            for (DataSet parent:dataset.getParents()) {
                                String parentDatasetSpaceCode = parent.getExperiment().getIdentifier().getIdentifier().split("/")[1];
                                if (datasetSpaceCode.equals(parentDatasetSpaceCode) || withObjectsAndDataSetsOtherSpaces)
                                {
                                    collectEntities(api, sessionToken, collection,
                                            new ExportablePermId(ExportableKind.DATASET,
                                                    new ObjectPermId(parent.getPermId().getPermId())),
                                            withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                                }
                            }
                        }

                        for (DataSet child:dataset.getChildren()) {
                            String childDatasetSpaceCode = child.getExperiment().getIdentifier().getIdentifier().split("/")[1];
                            if (datasetSpaceCode.equals(childDatasetSpaceCode) || withObjectsAndDataSetsOtherSpaces)
                            {
                                collectEntities(api, sessionToken, collection,
                                        new ExportablePermId(ExportableKind.DATASET,
                                                new ObjectPermId(child.getPermId().getPermId())),
                                        withLevelsBelow, withObjectsAndDataSetsParents, withObjectsAndDataSetsOtherSpaces);
                            }
                        }

                    }
                    break;
            }
        }
    }
}
