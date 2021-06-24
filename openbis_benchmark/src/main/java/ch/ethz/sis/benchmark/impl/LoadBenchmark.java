package ch.ethz.sis.benchmark.impl;

import java.rmi.server.UID;
import java.util.*;

import ch.ethz.sis.benchmark.Benchmark;
import ch.ethz.sis.benchmark.impl.jdbc.ApplicationServerApiPostgresWrapper;
import ch.ethz.sis.benchmark.util.RandomValueGenerator;
import ch.ethz.sis.benchmark.util.RandomWord;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;

public class LoadBenchmark extends Benchmark {
	
	private enum Parameters { SPACES_TO_CREATE, SAMPLES_TO_CREATE, PROPERTIES_NUMBER, USE_DATABASE, DATABASE_URL, DATABASE_USER, DATABASE_PASS, SET_SAMPLES_RELATIONSHIPS }
	private enum Prefix { SPACE_, COLLECTION_, PROJECT_, OBJECT_ }
	
	@Override
	public void startInternal() throws Exception {
        login();
        
        String propertyTypeCode = "BENCHMARK_STRING_";
        EntityTypePermId sampleTypeCode = new EntityTypePermId("BENCHMARK_OBJECT");
        EntityTypePermId experimentTypeCode = new EntityTypePermId("BENCHMARK_COLLECTION");
        
        SampleTypeSearchCriteria stsc = new SampleTypeSearchCriteria();
        stsc.withCode().thatEquals(sampleTypeCode.getPermId());
        SampleTypeFetchOptions stfo = new SampleTypeFetchOptions();
        List<SampleType> types = v3.searchSampleTypes(sessionToken, stsc, stfo).getObjects();

        int propertiesNumber = Integer.parseInt(Optional.ofNullable(this.getConfiguration().getParameters().get(Parameters.PROPERTIES_NUMBER.name())).orElse("2"));

        if(types.isEmpty()) {
        	//
            // Setup - Create Property Types
            //
            List<PropertyTypeCreation> propertyTypeCreations = new ArrayList<>(propertiesNumber);
            for(int i = 0; i < propertiesNumber; i++) {
                int propertyNumber = i + 1;
                PropertyTypeCreation propertyTypeCreation = new PropertyTypeCreation();
                propertyTypeCreation.setCode(propertyTypeCode + propertyNumber);
                propertyTypeCreation.setDataType(DataType.MULTILINE_VARCHAR);
                propertyTypeCreation.setLabel("Benchmark String " + propertyNumber + " label");
                propertyTypeCreation.setDescription("Benchmark String " + propertyNumber + " description");
                propertyTypeCreations.add(propertyTypeCreation);
            }
            
            v3.createPropertyTypes(sessionToken, propertyTypeCreations);
            
            //
            // Setup - Create Sample Type
            //
            
            SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
            sampleTypeCreation.setCode(sampleTypeCode.getPermId());

            List<PropertyAssignmentCreation> propertyAssignments = new ArrayList<>(propertiesNumber);
            for(int i = 0; i < propertiesNumber; i++) {
                int propertyNumber = i + 1;
                PropertyAssignmentCreation propertyAssignmentCreation = new PropertyAssignmentCreation();
                propertyAssignmentCreation.setPropertyTypeId(new PropertyTypePermId(propertyTypeCode + propertyNumber));
                propertyAssignments.add(propertyAssignmentCreation);
            }
            
            sampleTypeCreation.setPropertyAssignments(propertyAssignments);
            
            v3.createSampleTypes(sessionToken, Arrays.asList(sampleTypeCreation));
            
            //
            // Setup - Create Experiment Type
            //
            
            ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
            experimentTypeCreation.setCode(experimentTypeCode.getPermId());
            
            v3.createExperimentTypes(sessionToken, Arrays.asList(experimentTypeCreation));
        }
        
        //
        // Setup - Create codes
        //
        Set<String> codes = new HashSet<String>();
        int spacesToCreate = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.SPACES_TO_CREATE.name()));
        for(int i = 0; i < spacesToCreate; i++) {
	        	String code = null;
	    		while(code == null || codes.contains(code)) {
	    			code = RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord();
	    		}
	    		codes.add(code);
        }
        RandomValueGenerator<String> randomValueGenerator = new RandomValueGenerator<>();
        randomValueGenerator.addAll(codes);
        
        //
        // Part 1 - Creating Spaces
        //
        
        List<SpaceCreation> spaceCreations = new ArrayList<SpaceCreation>();
        for(String code:codes) {
        		SpaceCreation creation = new SpaceCreation();
        		creation.setCode(Prefix.SPACE_ + code);
        		spaceCreations.add(creation);
        }
        long start1 = System.currentTimeMillis();
        v3.createSpaces(sessionToken, spaceCreations);
        long end1 = System.currentTimeMillis();
        //logger.info("Create " + spacesToCreate + " Spaces took: " + (end1-start1) + " millis - " + ((end1-start1)/spacesToCreate) + " millis/space");
        
        //
        // Part 2 - Creating Projects
        //
        List<ProjectCreation> projectCreations = new ArrayList<ProjectCreation>();
        for(String code:codes) {
        		ProjectCreation creation = new ProjectCreation();
        		creation.setCode(Prefix.PROJECT_ + code);
        		creation.setSpaceId(new SpacePermId(Prefix.SPACE_ + code));
        		projectCreations.add(creation);
        }
        long start2 = System.currentTimeMillis();
        v3.createProjects(sessionToken, projectCreations);
        long end2 = System.currentTimeMillis();
        //logger.info("Create " + spacesToCreate + " Projects took: " + (end2-start2) + " millis - " + ((end2-start2)/spacesToCreate) + " millis/project");
        
        //
        // Part 3 - Creating Experiments
        //
        List<ExperimentCreation> experimentCreations = new ArrayList<ExperimentCreation>();
        for(String code:codes) {
        		ExperimentCreation creation = new ExperimentCreation();
        		creation.setCode(Prefix.COLLECTION_ + code);
        		creation.setProjectId(new ProjectIdentifier("/" + Prefix.SPACE_ + code + "/" + Prefix.PROJECT_ + code));
        		creation.setTypeId(experimentTypeCode);
        		experimentCreations.add(creation);
        }
        long start3 = System.currentTimeMillis();
        v3.createExperiments(sessionToken, experimentCreations);
        long end3 = System.currentTimeMillis();
        //logger.info("Create " + spacesToCreate + " Collections took: " + (end3-start3) + " millis - " + ((end3-start3)/spacesToCreate) + " millis/collection");
        
        logout();
        //
        // Part 4 - Creating Samples
        //
        long start4 = System.currentTimeMillis();
        
        Set<String> sampleCodes = new HashSet<String>();
        int sampleBatchSize = 5000;
        int samplesToCreate = Integer.parseInt(this.getConfiguration().getParameters().get(Parameters.SAMPLES_TO_CREATE.name()));
        List<SampleCreation> sampleCreations = new ArrayList<SampleCreation>();
        boolean setSamplesRelationships = Boolean.parseBoolean(this.getConfiguration().getParameters().get(
                Parameters.SET_SAMPLES_RELATIONSHIPS.name()));
        int relationshipsIndex = 0;
        for(int i = 0; i < samplesToCreate; i++) {
        		SampleCreation sampleCreation = new SampleCreation();
        		String sampleCode = null;
        		while(sampleCode == null || sampleCodes.contains(sampleCode)) {
        			sampleCode = "SAMPLE_" + RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord() + "_" + RandomWord.getRandomWord();
        		}
                sampleCreation.setCreationId(new CreationId(UUID.randomUUID().toString()));
        		sampleCreation.setTypeId(sampleTypeCode);
        		sampleCreation.setCode(sampleCode);

                for(int j = 0; j < propertiesNumber; j++) {
                    int propertyNumber = j + 1;
                    sampleCreation.setProperty(propertyTypeCode + propertyNumber, RandomWord.getRandomWord() + " " + RandomWord.getRandomWord());
                }

        		String code = randomValueGenerator.getRandom();
        		sampleCreation.setSpaceId(new SpacePermId(Prefix.SPACE_ + code)); // Spaces are distributed randomly
        		sampleCreation.setProjectId(new ProjectIdentifier("/" + Prefix.SPACE_ + code + "/" + Prefix.PROJECT_ + code));
                sampleCreation.setExperimentId(new ExperimentIdentifier("/" + Prefix.SPACE_ + code + "/" + Prefix.PROJECT_ + code + "/" + Prefix.COLLECTION_ + code));
        		sampleCreations.add(sampleCreation);

                if (setSamplesRelationships && relationshipsIndex % 5 == 4)
                {
                    SampleCreation sampleCreation1 = sampleCreations.get(relationshipsIndex - 1);
                    SampleCreation sampleCreation2 = sampleCreations.get(relationshipsIndex - 2);
                    SampleCreation sampleCreation3 = sampleCreations.get(relationshipsIndex - 3);
                    SampleCreation sampleCreation4 = sampleCreations.get(relationshipsIndex - 4);

                    sampleCreation2.setParentIds(Arrays.asList(sampleCreation1.getCreationId()));
                    sampleCreation3.setParentIds(Arrays.asList(sampleCreation1.getCreationId()));
                    sampleCreation4.setParentIds(Arrays.asList(sampleCreation2.getCreationId()));
                }

                relationshipsIndex++;

        		if(samplesToCreate > sampleBatchSize && (i+1) % sampleBatchSize == 0 || // Every 5000, send to openBIS
                    samplesToCreate <= sampleBatchSize && samplesToCreate == sampleCreations.size()) { // If less than 5000, send to openBIS
                    // Use JDBC If requested
                    boolean useDatabase = Boolean.parseBoolean(this.getConfiguration().getParameters().get(Parameters.USE_DATABASE.name()));
                    if (useDatabase) {
                        String databaseURL = this.getConfiguration().getParameters().get(Parameters.DATABASE_URL.name());
                        String databaseUser = this.getConfiguration().getParameters().get(Parameters.DATABASE_USER.name());
                        String databasePass = this.getConfiguration().getParameters().get(Parameters.DATABASE_PASS.name());
                        this.v3Wrapper = new ApplicationServerApiPostgresWrapper(databaseURL, databaseUser, databasePass);
                    }
                    //
                    login();
        			long lapStart4 = System.currentTimeMillis();
        			v3.createSamples(sessionToken, sampleCreations);
        			long lapEnd4 = System.currentTimeMillis();
        			addOperation(lapStart4, lapEnd4, sampleCreations.size());
        			logout();
        			//logger.info("Create " + sampleCreations.size() + " Samples took: " + (lapEnd4 - lapStart4) + " millis - " + ((lapEnd4-lapStart4)/sampleCreations.size()) + " millis/sample");
        			sampleCreations.clear();
                    relationshipsIndex = 0;
        		}
        }
        long end4 = System.currentTimeMillis();
        //logger.info("Create " + samplesToCreate + " Samples took: " + (end4-start4) + " millis - " + ((end4-start4)/samplesToCreate) + " millis/sample");
	}

}
