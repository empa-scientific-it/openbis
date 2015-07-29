define([ 'jquery', 'openbis', 'dto/entity/space/SpaceCreation', 'dto/entity/project/ProjectCreation', 'dto/entity/experiment/ExperimentCreation', 'dto/entity/sample/SampleCreation',
		'dto/entity/material/MaterialCreation', 'dto/entity/attachment/AttachmentCreation', 'dto/entity/space/SpaceUpdate', 'dto/entity/project/ProjectUpdate',
		'dto/entity/experiment/ExperimentUpdate', 'dto/entity/sample/SampleUpdate', 'dto/entity/dataset/DataSetUpdate', 'dto/entity/dataset/ExternalDataUpdate', 'dto/entity/material/MaterialUpdate',
		'dto/deletion/space/SpaceDeletionOptions', 'dto/deletion/project/ProjectDeletionOptions', 'dto/deletion/experiment/ExperimentDeletionOptions', 'dto/deletion/sample/SampleDeletionOptions',
		'dto/deletion/dataset/DataSetDeletionOptions', 'dto/deletion/material/MaterialDeletionOptions', 'dto/id/entitytype/EntityTypePermId', 'dto/id/space/SpacePermId',
		'dto/id/project/ProjectPermId', 'dto/id/project/ProjectIdentifier', 'dto/id/experiment/ExperimentPermId', 'dto/id/experiment/ExperimentIdentifier', 'dto/id/sample/SamplePermId',
		'dto/id/dataset/DataSetPermId', 'dto/id/dataset/FileFormatTypePermId', 'dto/id/material/MaterialPermId', 'dto/id/tag/TagCode', 'dto/search/SpaceSearchCriterion',
		'dto/search/ProjectSearchCriterion', 'dto/search/ExperimentSearchCriterion', 'dto/search/SampleSearchCriterion', 'dto/search/DataSetSearchCriterion', 'dto/search/MaterialSearchCriterion',
		'dto/fetchoptions/space/SpaceFetchOptions', 'dto/fetchoptions/project/ProjectFetchOptions', 'dto/fetchoptions/experiment/ExperimentFetchOptions', 'dto/fetchoptions/sample/SampleFetchOptions',
		'dto/fetchoptions/dataset/DataSetFetchOptions', 'dto/fetchoptions/material/MaterialFetchOptions', 'dto/fetchoptions/deletion/DeletionFetchOptions' ], function($, openbis, SpaceCreation,
		ProjectCreation, ExperimentCreation, SampleCreation, MaterialCreation, AttachmentCreation, SpaceUpdate, ProjectUpdate, ExperimentUpdate, SampleUpdate, DataSetUpdate, ExternalDataUpdate,
		MaterialUpdate, SpaceDeletionOptions, ProjectDeletionOptions, ExperimentDeletionOptions, SampleDeletionOptions, DataSetDeletionOptions, MaterialDeletionOptions, EntityTypePermId, SpacePermId,
		ProjectPermId, ProjectIdentifier, ExperimentPermId, ExperimentIdentifier, SamplePermId, DataSetPermId, FileFormatTypePermId, MaterialPermId, TagCode, SpaceSearchCriterion,
		ProjectSearchCriterion, ExperimentSearchCriterion, SampleSearchCriterion, DataSetSearchCriterion, MaterialSearchCriterion, SpaceFetchOptions, ProjectFetchOptions, ExperimentFetchOptions,
		SampleFetchOptions, DataSetFetchOptions, MaterialFetchOptions, DeletionFetchOptions) {

	/*
	 * These tests should be run against openBIS instance with screening sprint
	 * server database version
	 */

	var testProtocol = window.location.protocol;
	var testHost = window.location.hostname;
	var testPort = window.location.port;
	var testUrl = testProtocol + "//" + testHost + ":" + testPort;
	var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

	var testUserId = "openbis_test_js";
	var testUserPassword = "password";

	var Common = function(assert) {
		this.assert = assert;

		this.SpaceCreation = SpaceCreation;
		this.ProjectCreation = ProjectCreation;
		this.ExperimentCreation = ExperimentCreation;
		this.SampleCreation = SampleCreation;
		this.MaterialCreation = MaterialCreation;
		this.AttachmentCreation = AttachmentCreation;
		this.SpaceUpdate = SpaceUpdate;
		this.ProjectUpdate = ProjectUpdate;
		this.ExperimentUpdate = ExperimentUpdate;
		this.SampleUpdate = SampleUpdate;
		this.DataSetUpdate = DataSetUpdate;
		this.ExternalDataUpdate = ExternalDataUpdate;
		this.MaterialUpdate = MaterialUpdate;
		this.SpaceDeletionOptions = SpaceDeletionOptions;
		this.ProjectDeletionOptions = ProjectDeletionOptions;
		this.ExperimentDeletionOptions = ExperimentDeletionOptions;
		this.SampleDeletionOptions = SampleDeletionOptions;
		this.DataSetDeletionOptions = DataSetDeletionOptions;
		this.MaterialDeletionOptions = MaterialDeletionOptions;
		this.EntityTypePermId = EntityTypePermId;
		this.SpacePermId = SpacePermId;
		this.ProjectPermId = ProjectPermId;
		this.ProjectIdentifier = ProjectIdentifier;
		this.ExperimentPermId = ExperimentPermId;
		this.ExperimentIdentifier = ExperimentIdentifier;
		this.SamplePermId = SamplePermId;
		this.DataSetPermId = DataSetPermId;
		this.FileFormatTypePermId = FileFormatTypePermId;
		this.MaterialPermId = MaterialPermId;
		this.TagCode = TagCode;
		this.SpaceSearchCriterion = SpaceSearchCriterion;
		this.ProjectSearchCriterion = ProjectSearchCriterion;
		this.ExperimentSearchCriterion = ExperimentSearchCriterion;
		this.SampleSearchCriterion = SampleSearchCriterion;
		this.DataSetSearchCriterion = DataSetSearchCriterion;
		this.MaterialSearchCriterion = MaterialSearchCriterion;
		this.SpaceFetchOptions = SpaceFetchOptions;
		this.ProjectFetchOptions = ProjectFetchOptions;
		this.ExperimentFetchOptions = ExperimentFetchOptions;
		this.SampleFetchOptions = SampleFetchOptions;
		this.DataSetFetchOptions = DataSetFetchOptions;
		this.MaterialFetchOptions = MaterialFetchOptions;
		this.DeletionFetchOptions = DeletionFetchOptions;

		this.generateId = function(base) {
			var date = new Date();
			var parts = [ "V3", base, date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), Math.random() ];
			return parts.join("_");
		},

		this.createSpace = function(facade) {
			var c = this;
			var creation = new SpaceCreation();
			creation.setCode(c.generateId("SPACE"));
			return facade.createSpaces([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createProject = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new ProjectCreation();
				creation.setCode(c.generateId("PROJECT"));
				creation.setSpaceId(spacePermId);
				return facade.createProjects([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createExperiment = function(facade) {
			var c = this;
			return c.createProject(facade).then(function(projectPermId) {
				var creation = new ExperimentCreation();
				creation.setCode(c.generateId("EXPERIMENT"));
				creation.setTypeId(new EntityTypePermId("UNKNOWN"));
				creation.setProjectId(projectPermId);
				return facade.createExperiments([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createSample = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new SampleCreation();
				creation.setCode(c.generateId("SAMPLE"));
				creation.setTypeId(new EntityTypePermId("UNKNOWN"));
				creation.setSpaceId(spacePermId);
				return facade.createSamples([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createDataSet = function(facade) {
			var c = this;
			return $.ajax({
				"url" : "http://localhost:20001/datastore_server/rmi-dss-api-v1.json",
				"type" : "POST",
				"processData" : false,
				"dataType" : "json",
				"data" : JSON.stringify({
					"method" : "createReportFromAggregationService",
					"params" : [ facade._private.sessionToken, "js-test", {} ],
					"id" : "1",
					"jsonrpc" : "2.0"
				})
			}).then(function(response) {
				return new DataSetPermId(response.result.rows[0][0].value);
			});
		}.bind(this);

		this.createMaterial = function(facade) {
			var c = this;
			var creation = new MaterialCreation();
			creation.setCode(c.generateId("MATERIAL"));
			creation.setTypeId(new EntityTypePermId("COMPOUND"));
			return facade.createMaterials([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.findSpace = function(facade, id) {
			var c = this;
			return facade.mapSpaces([ id ], c.createSpaceFetchOptions()).then(function(spaces) {
				return spaces[id];
			});
		}.bind(this);

		this.findProject = function(facade, id) {
			var c = this;
			return facade.mapProjects([ id ], c.createProjectFetchOptions()).then(function(projects) {
				return projects[id];
			});
		}.bind(this);

		this.findExperiment = function(facade, id) {
			var c = this;
			return facade.mapExperiments([ id ], c.createExperimentFetchOptions()).then(function(experiments) {
				return experiments[id];
			});
		}.bind(this);

		this.findSample = function(facade, id) {
			var c = this;
			return facade.mapSamples([ id ], c.createSampleFetchOptions()).then(function(samples) {
				return samples[id];
			});
		}.bind(this);

		this.findDataSet = function(facade, id) {
			var c = this;
			return facade.mapDataSets([ id ], c.createDataSetFetchOptions()).then(function(dataSets) {
				return dataSets[id];
			});
		}.bind(this);

		this.findMaterial = function(facade, id) {
			var c = this;
			return facade.mapMaterials([ id ], c.createMaterialFetchOptions()).then(function(materials) {
				return materials[id];
			});
		}.bind(this);

		this.deleteSpace = function(facade, id) {
			var c = this;
			var options = new SpaceDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSpaces([ id ], options);
		}.bind(this);

		this.deleteProject = function(facade, id) {
			var c = this;
			var options = new ProjectDeletionOptions();
			options.setReason("test reason");
			return facade.deleteProjects([ id ], options);
		}.bind(this);

		this.deleteExperiment = function(facade, id) {
			var c = this;
			var options = new ExperimentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperiments([ id ], options);
		}.bind(this);

		this.deleteSample = function(facade, id) {
			var c = this;
			var options = new SampleDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSamples([ id ], options);
		}.bind(this);

		this.deleteDataSet = function(facade, id) {
			var c = this;
			var options = new DataSetDeletionOptions();
			options.setReason("test reason");
			return facade.deleteDataSets([ id ], options);
		}.bind(this);

		this.deleteMaterial = function(facade, id) {
			var c = this;
			var options = new MaterialDeletionOptions();
			options.setReason("test reason");
			return facade.deleteMaterials([ id ], options);
		}.bind(this);

		this.getObjectProperty = function(object, propertyName) {
			var propertyNames = propertyName.split('.');
			for ( var pn in propertyNames) {
				object = object[propertyNames[pn]];
			}
			return object;
		};

		this.createFacade = function() {
			var dfd = $.Deferred();
			dfd.resolve(new openbis(testApiUrl));
			return dfd.promise();
		};

		this.createFacadeAndLogin = function() {
			var dfd = $.Deferred();

			this.createFacade().then(function(facade) {
				facade.login(testUserId, testUserPassword).done(function() {
					dfd.resolve(facade);
				}).fail(function() {
					dfd.reject(arguments);
				});
			});

			return dfd.promise();
		};

		this.createSpaceFetchOptions = function() {
			var fo = new SpaceFetchOptions();
			fo.withProjects();
			fo.withSamples();
			fo.withRegistrator();
			return fo;
		};

		this.createProjectFetchOptions = function() {
			var fo = new ProjectFetchOptions();
			fo.withSpace();
			fo.withExperiments();
			fo.withRegistrator();
			fo.withModifier();
			fo.withLeader();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createExperimentFetchOptions = function() {
			var fo = new ExperimentFetchOptions();
			fo.withType();
			fo.withProject().withSpace();
			fo.withDataSets();
			fo.withSamples();
			fo.withHistory();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createSampleFetchOptions = function() {
			var fo = new SampleFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainer();
			fo.withContained();
			fo.withDataSets();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments().withContent();
			fo.withChildrenUsing(fo);
			return fo;
		};

		this.createDataSetFetchOptions = function() {
			var fo = new DataSetFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSample();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainers();
			fo.withContained();
			fo.withExternalData().withFileFormatType();
			fo.withExternalData().withLocatorType();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			return fo;
		};

		this.createMaterialFetchOptions = function() {
			var fo = new MaterialFetchOptions();
			fo.withType();
			fo.withHistory();
			fo.withRegistrator();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			return fo;
		};

		this.assertNull = function(actual, msg) {
			this.assertEqual(actual, null, msg)
		};

		this.assertNotNull = function(actual, msg) {
			this.assertNotEqual(actual, null, msg);
		};

		this.assertTrue = function(actual, msg) {
			this.assertEqual(actual, true, msg);
		};

		this.assertFalse = function(actual, msg) {
			this.assertEqual(actual, false, msg);
		};

		this.assertEqual = function(actual, expected, msg) {
			this.assert.equal(actual, expected, msg);
		};

		this.assertNotEqual = function(actual, expected, msg) {
			this.assert.notEqual(actual, expected, msg);
		};

		this.assertDate = function(millis, msg, year, month, day, hour, minute) {
			var date = new Date(millis);
			var actual = "";
			var expected = "";

			if (year) {
				actual += date.getUTCFullYear();
				expected += year;
			}
			if (month) {
				actual += "-" + (date.getUTCMonth() + 1);
				expected += "-" + month;
			}
			if (day) {
				actual += "-" + date.getUTCDate();
				expected += "-" + day;
			}
			if (hour) {
				actual += " " + date.getUTCHours();
				expected += " " + hour;
			}
			if (minute) {
				actual += ":" + date.getUTCMinutes();
				expected += ":" + minute;
			}

			this.assertEqual(actual, expected, msg);
		};

		this.assertToday = function(millis, msg) {
			var today = new Date();
			this.assertDate(millis, msg, today.getUTCFullYear(), today.getUTCMonth() + 1, today.getUTCDate());
		};

		this.assertObjectsCount = function(objects, count) {
			this.assertEqual(objects.length, count, 'Got ' + count + ' object(s)');
		};

		this.assertObjectsWithValues = function(objects, propertyName, propertyValues) {
			var thisCommon = this;
			var values = {};

			$.each(objects, function(index, object) {
				var value = thisCommon.getObjectProperty(object, propertyName);
				if (value in values == false) {
					values[value] = true;
				}
			});

			this.assert.deepEqual(Object.keys(values).sort(), propertyValues.sort(), 'Objects have correct ' + propertyName + ' values')
		};

		this.assertObjectsWithOrWithoutCollections = function(objects, accessor, checker) {
			var theObjects = null;

			if ($.isArray(objects)) {
				theObjects = objects;
			} else {
				theObjects = [ objects ];
			}

			var theAccessor = null;

			if ($.isFunction(accessor)) {
				theAccessor = accessor;
			} else {
				theAccessor = function(object) {
					return object[accessor];
				}
			}

			checker(theObjects, theAccessor);
		};

		this.assertObjectsWithCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.some(function(object) {
					var value = accessor(object);
					return value && Object.keys(value).length > 0;
				}), 'Objects have non-empty collections accessed via: ' + accessor);
			});
		};

		this.assertObjectsWithoutCollections = function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.every(function(object) {
					var value = accessor(object);
					return !value || Object.keys(value).length == 0;
				}), 'Objects have empty collections accessed via: ' + accessor);
			});
		};

		this.start = function() {
			this.done = this.assert.async();
		};

		this.finish = function() {
			if (this.done) {
				this.done();
			}
		};

		this.ok = function(msg) {
			this.assert.ok(true, msg);
		};

		this.fail = function(msg) {
			this.assert.ok(false, msg);
		};

	};

	return Common;
})
