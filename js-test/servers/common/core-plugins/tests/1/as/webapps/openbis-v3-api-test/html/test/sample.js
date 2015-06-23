define([ 'jquery', 'openbis', 'test/common', 'dto/entity/sample/SampleCreation', 'dto/id/entitytype/EntityTypePermId', 'dto/id/space/SpacePermId', 'dto/id/tag/TagCode' ], function($, openbis, common,
		SampleCreation, EntityTypePermId, SpacePermId, TagCode) {
	return function() {
		QUnit.module("Sample tests");

		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createSamplePermId("20130415095748527-404"), c.createSampleFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapSamples([ permId ], fetchOptions).done(function() {
					facade.logout()
				})
			}).done(function(samples) {
				c.assertObjectsCount(Object.keys(samples), 1);
				var sample = samples["20130415095748527-404"];
				c.assertEqual(sample.code, "TEST-SAMPLE-2-PARENT", "Sample code");
				c.assertEqual(sample.type.code, "UNKNOWN", "Type code");
				c.assertEqual(sample.experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(sample.experiment.project.code, "TEST-PROJECT", "Project code");
				c.assertEqual(sample.space.code, "TEST", "Space code");
				c.assertNotEqual(sample.children, null, "Children expected");
				if (sample.children !== null) {
					console.log("Children %s", sample.children);
					var child = sample.children[0];
					c.assertEqual(sample.children.length, 1, "Number of children");
					c.assertEqual(child.code, "TEST-SAMPLE-2", "Child sample code");
					c.assertEqual(child.type.code, "UNKNOWN", "Child type code");
					c.assertEqual(child.experiment.code, "TEST-EXPERIMENT-2", "Child experiment code");
					c.assertNotEqual(child.children, null, "Grand children expected");
					if (child.children !== null) {
						c.assertEqual(child.children.length, 2, "Number of grand children");
					}
				}
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("searchSamples()", function(assert) {
			var c = new common(assert);
			var done = assert.async();
			
			$.when(c.createFacadeAndLogin(), c.createSampleSearchCriterion(), c.createSampleFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("PLATE-1");

				return facade.searchSamples(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(samples) {
				c.assertObjectsCount(samples, 1);

				var sample = samples[0];
				c.assertEqual(sample.getCode(), "PLATE-1", "Sample code");
				c.assertEqual(sample.getType().getCode(), "PLATE", "Type code");
				c.assertEqual(sample.getExperiment().getCode(), "EXP-1", "Experiment code");
				c.assertEqual(sample.getExperiment().getProject().getCode(), "SCREENING-EXAMPLES", "Project  code");
				c.assertEqual(sample.getSpace().getCode(), "PLATONIC", "Space code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("createSamples()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var creation = new SampleCreation();
			creation.setTypeId(new EntityTypePermId("UNKNOWN"));
			creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
			creation.setSpaceId(new SpacePermId("TEST"));
			creation.setTagIds([ new TagCode("CREATE_JSON_TAG") ]);

			$.when(c.createFacadeAndLogin(), c.createSampleFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSamples([ creation ]).then(function(permIds) {
					return facade.mapSamples(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				c.assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				c.assertEqual(sample.getCode(), creation.getCode(), "Sample code");
				c.assertEqual(sample.getType().getCode(), creation.getTypeId().getPermId(), "Type code");
				c.assertEqual(sample.getSpace().getCode(), creation.getSpaceId().getPermId(), "Space code");
				c.assertEqual(sample.getTags()[0].getCode(), creation.getTagIds()[0].getCode(), "Tag code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		/*
		 * test("updateSamples()", function() { var code = "UPDATE_JSON_SAMPLE_" +
		 * (new Date().getTime());
		 * 
		 * createFacadeAndLogin().then(function(facade) { var creations = [ {
		 * "@type" : "SampleCreation",
		 * 
		 * "typeId" : { "@type" : "EntityTypePermId", "permId" : "UNKNOWN" },
		 * 
		 * "code" : code,
		 * 
		 * "spaceId" : { "@type" : "SpacePermId", "permId" : "PLATONIC" } } ];
		 * 
		 * var ids = facade.createSamples(creations).then(function(permIds) {
		 * var updates = [ { "@type" : "SampleUpdate",
		 * 
		 * "sampleId" : permIds[0],
		 * 
		 * "spaceId" : { "@type" : "SpacePermId", "permId" : "TEST" } } ];
		 * 
		 * return facade.updateSamples(updates).then(function() { return
		 * permIds; }); });
		 * 
		 * return $.when(ids, createSampleFetchOptions()).then(function(permIds,
		 * fetchOptions) { return facade.mapSamples(permIds,
		 * fetchOptions).done(function() { facade.logout(); }) })
		 * }).done(function(samples) { var keys = Object.keys(samples);
		 * assertObjectsCount(keys, 1);
		 * 
		 * var sample = samples[keys[0]]; equal(sample.code, code, "Sample
		 * code"); equal(sample.type.code, "UNKNOWN", "Type code");
		 * equal(sample.space.code, "TEST", "Space code"); start();
		 * }).fail(function(error) { ok(false, error.message); start(); }); });
		 */

	}
});
