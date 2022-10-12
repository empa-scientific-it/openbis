package ethz.ch;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ethz.ch.dataset.DatasetCreationHelper;
import ethz.ch.ssl.SslCertificateHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainShufle {
    private static final String OPENBIS_LOCAL_DEV = "http://localhost:8888";
    private static final String DSS_LOCAL_DEV = "http://localhost:8889";

    private static final int TIMEOUT = Integer.MAX_VALUE;

    public static void main(String[] args) throws Exception
    {
        if(args.length == 4) {
            String AS_URL = args[0] + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
            String DSS_URL = args[1] + "/datastore_server" + IDataStoreServerApi.SERVICE_URL;
            DatasetCreationHelper.setDssURL(args[1]);
            String user = args[2];
            String pass = args[3];

            System.out.println("AS_URL : [" + AS_URL +"]");
            System.out.println("DSS_URL : [" + DSS_URL +"]");
            System.out.println("user : [" + user +"]");
            System.out.println("pass : [" + pass +"]");

            doTheWork(true, AS_URL, DSS_URL, user, pass);
        } else {
// Used for development
            String AS_URL = OPENBIS_LOCAL_DEV + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
            String DSS_URL = DSS_LOCAL_DEV + "/datastore_server" + IDataStoreServerApi.SERVICE_URL;
            doTheWork(false, AS_URL, DSS_URL, "migration", "");
//            System.out.println("Example: java -jar microscopy_migration_tool.jar https://openbis-as-domain.ethz.ch https://openbis-dss-domain.ethz.ch user password");
        }
    }

    private static void doTheWork(boolean COMMIT_CHANGES_TO_OPENBIS, String AS_URL, String DSS_URL, String userId, String pass) throws Exception {
        System.out.println("Migration Started");
        SslCertificateHelper.trustAnyCertificate(AS_URL);
        SslCertificateHelper.trustAnyCertificate(DSS_URL);
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, AS_URL, TIMEOUT);
        IDataStoreServerApi v3dss = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, DSS_URL, TIMEOUT);
        String sessionToken = v3.login(userId, pass);
        System.out.println("userId, pass, sessionToken : [" + userId +"][" + pass +"][" + sessionToken +"]");
        Map<String, String> serverInfo = v3.getServerInformation(sessionToken);

        if(serverInfo.containsKey("project-samples-enabled") && serverInfo.get("project-samples-enabled").equals("true")) {
            System.out.println("Project samples enabled.");
        } else {
            System.out.println("Enable project samples before running the migration.");
            return;
        }

        final List<String> PREFIXES = List.of("FACS_ARIA_",
                "INFLUX_",
                "LSR_FORTESSA_",
                "MOFLO_XDP_",
                "S3E_",
                "CYTOFLEX_S_",
                "SONY_SH800S_",
                "SONY_MA900_");

        final String EXPERIMENT = "EXPERIMENT";
        final String TUBE = "TUBE";
        final String WELL = "WELL";

        final String TUBESET = "TUBESET";
        //
        // 1. Updating samples types
        //
        List<SampleTypeUpdate> sampleTypeUpdates = new ArrayList<>();
        System.out.println("2. Update sample types");
        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        sampleTypeSearchCriteria.withOrOperator();
        for (String PREFIX:PREFIXES) {
            sampleTypeSearchCriteria.withCode().thatEquals(PREFIX + TUBESET);
        }

        SearchResult<SampleType> sampleTypes = v3.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, new SampleTypeFetchOptions());
        for (SampleType sampleType:sampleTypes.getObjects()) {
            System.out.println(sampleType.getCode());
            SampleTypeUpdate sampleTypeUpdate = new SampleTypeUpdate();
            sampleTypeUpdate.setTypeId(sampleType.getPermId());
            PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
            propertyAssignmentCreation.setPropertyTypeId(new PropertyTypePermId("$NAME"));
            propertyAssignmentCreation.setInitialValueForExistingEntities("Tubes");
            propertyAssignmentCreation.setOrdinal(1);
            propertyAssignmentCreation.setMandatory(true);
            sampleTypeUpdate.getPropertyAssignments().add(propertyAssignmentCreation);
            sampleTypeUpdates.add(sampleTypeUpdate);
        }

        //
        // 2. Updating samples
        //
        List<SampleUpdate> sampleUpdates = new ArrayList<>();
        System.out.println("2. Find Samples attached to migrated types");

        for(String prefix:PREFIXES) {
            SampleSearchCriteria criteria = new SampleSearchCriteria();
            criteria.withType().withCode().thatEquals(prefix + EXPERIMENT);

            SampleFetchOptions options = new SampleFetchOptions();
            options.withChildren().withType();
            options.withChildren().withParents();

            SearchResult<Sample> sampleSearchResult = v3.searchSamples(sessionToken, criteria, options);
            System.out.println("3. Found: " + prefix + EXPERIMENT + " " + sampleSearchResult.getObjects().size());
            for (Sample experiment:sampleSearchResult.getObjects()) {
                List<Sample> tubes = getSamplesOfType(experiment.getChildren(), prefix + TUBE);
                List<Sample> wells = getSamplesOfType(experiment.getChildren(), prefix + WELL);

                List<Sample> itemsToFix = new ArrayList<>();
                itemsToFix.addAll(tubes);
                itemsToFix.addAll(wells);

                for (Sample itemToFix:itemsToFix) {
                    SampleUpdate sampleUpdate = new SampleUpdate();
                    sampleUpdate.setSampleId(itemToFix.getPermId());
                    if(itemToFix.getParents().size() < 2) {
                        throw new RuntimeException("Found with less than 2 parents: " + itemToFix.getIdentifier());
                    }
                    sampleUpdate.getParentIds().remove(experiment.getPermId());
                    System.out.println("[UPDATE] " + itemToFix.getPermId() + " [REMOVE PARENT] " + experiment.getPermId());
                    sampleUpdates.add(sampleUpdate);
                }

                System.out.println(experiment.getIdentifier() + "\t" +
                        TUBE + ": " + tubes.size() + "\t" +
                        WELL + ": " + wells.size());

            }
        }

        System.out.println("Do Updates? " + COMMIT_CHANGES_TO_OPENBIS);
        System.out.println("SAMPLE TYPE UPDATES TOTAL: " + sampleTypeUpdates.size());
        System.out.println("SAMPLE UPDATES TOTAL: " + sampleUpdates.size());

        if (COMMIT_CHANGES_TO_OPENBIS) {
            System.out.println("updateSampleTypes start");
            long start = System.currentTimeMillis();
            v3.updateSampleTypes(sessionToken, sampleTypeUpdates);
            System.out.println("updateSampleTypes: " + (System.currentTimeMillis() - start));
            System.out.println("updateSamples start");
            long start2 = System.currentTimeMillis();
            v3.updateSamples(sessionToken, sampleUpdates);
            System.out.println("updateSamples: " + (System.currentTimeMillis() - start2));

        }
        v3.logout(sessionToken);
    }

    public static List<Sample> getSamplesOfType(List<Sample> samples, String typeCode) {
        return samples.stream().filter(sample -> sample.getType().getCode().equals(typeCode)).collect(Collectors.toList());
    }
}
