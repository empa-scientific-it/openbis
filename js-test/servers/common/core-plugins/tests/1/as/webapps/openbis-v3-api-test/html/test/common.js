define([ 'jquery', 'openbis', 'underscore', 'test/dtos' ], function($, openbis, _, dtos) {

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

		this.SpaceCreation = dtos.SpaceCreation;
		this.ProjectCreation = dtos.ProjectCreation;
		this.ExperimentCreation = dtos.ExperimentCreation;
		this.SampleCreation = dtos.SampleCreation;
		this.MaterialCreation = dtos.MaterialCreation;
		this.AttachmentCreation = dtos.AttachmentCreation;
		this.SpaceUpdate = dtos.SpaceUpdate;
		this.ProjectUpdate = dtos.ProjectUpdate;
		this.ExperimentUpdate = dtos.ExperimentUpdate;
		this.SampleUpdate = dtos.SampleUpdate;
		this.DataSetUpdate = dtos.DataSetUpdate;
		this.PhysicalDataUpdate = dtos.PhysicalDataUpdate;
		this.MaterialUpdate = dtos.MaterialUpdate;
		this.SpaceDeletionOptions = dtos.SpaceDeletionOptions;
		this.ProjectDeletionOptions = dtos.ProjectDeletionOptions;
		this.ExperimentDeletionOptions = dtos.ExperimentDeletionOptions;
		this.SampleDeletionOptions = dtos.SampleDeletionOptions;
		this.DataSetDeletionOptions = dtos.DataSetDeletionOptions;
		this.MaterialDeletionOptions = dtos.MaterialDeletionOptions;
		this.EntityTypePermId = dtos.EntityTypePermId;
		this.SpacePermId = dtos.SpacePermId;
		this.ProjectPermId = dtos.ProjectPermId;
		this.ProjectIdentifier = dtos.ProjectIdentifier;
		this.ExperimentPermId = dtos.ExperimentPermId;
		this.ExperimentIdentifier = dtos.ExperimentIdentifier;
		this.SamplePermId = dtos.SamplePermId;
		this.DataSetPermId = dtos.DataSetPermId;
		this.FileFormatTypePermId = dtos.FileFormatTypePermId;
		this.MaterialPermId = dtos.MaterialPermId;
		this.TagCode = dtos.TagCode;
		this.SpaceSearchCriteria = dtos.SpaceSearchCriteria;
		this.ProjectSearchCriteria = dtos.ProjectSearchCriteria;
		this.ExperimentSearchCriteria = dtos.ExperimentSearchCriteria;
		this.SampleSearchCriteria = dtos.SampleSearchCriteria;
		this.DataSetSearchCriteria = dtos.DataSetSearchCriteria;
		this.MaterialSearchCriteria = dtos.MaterialSearchCriteria;
		this.SpaceFetchOptions = dtos.SpaceFetchOptions;
		this.ProjectFetchOptions = dtos.ProjectFetchOptions;
		this.ExperimentFetchOptions = dtos.ExperimentFetchOptions;
		this.SampleFetchOptions = dtos.SampleFetchOptions;
		this.DataSetFetchOptions = dtos.DataSetFetchOptions;
		this.MaterialFetchOptions = dtos.MaterialFetchOptions;
		this.DeletionFetchOptions = dtos.DeletionFetchOptions;
		this.DeletionSearchCriteria = dtos.DeletionSearchCriteria;
		this.CustomASServiceSearchCriteria = dtos.CustomASServiceSearchCriteria;
		this.CustomASServiceFetchOptions = dtos.CustomASServiceFetchOptions;
		this.CustomASServiceCode = dtos.CustomASServiceCode;
		this.CustomASServiceExecutionOptions = dtos.CustomASServiceExecutionOptions;
		this.GlobalSearchCriteria = dtos.GlobalSearchCriteria;
		this.GlobalSearchObjectFetchOptions = dtos.GlobalSearchObjectFetchOptions;
		this.ObjectKindModificationSearchCriteria = dtos.ObjectKindModificationSearchCriteria;
		this.ObjectKindModificationFetchOptions = dtos.ObjectKindModificationFetchOptions;

		this.getDtos = function() {
			return dtos;
		}

		this.generateId = function(base) {
			var date = new Date();
			var parts = [ "V3", base, date.getFullYear(), date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), Math.random() ];
			return parts.join("_");
		},

		this.createSpace = function(facade) {
			var c = this;
			var creation = new dtos.SpaceCreation();
			creation.setCode(c.generateId("SPACE"));
			return facade.createSpaces([ creation ]).then(function(permIds) {
				return permIds[0];
			});
		}.bind(this);

		this.createProject = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.ProjectCreation();
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
				var creation = new dtos.ExperimentCreation();
				creation.setCode(c.generateId("EXPERIMENT"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setProjectId(projectPermId);
				return facade.createExperiments([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createSample = function(facade) {
			var c = this;
			return c.createSpace(facade).then(function(spacePermId) {
				var creation = new dtos.SampleCreation();
				creation.setCode(c.generateId("SAMPLE"));
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setSpaceId(spacePermId);
				return facade.createSamples([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		}.bind(this);

		this.createDataSet = function(facade) {
			var c = this;
			return this.getResponseFromJSTestAggregationService(facade, {}, function(response) {
				return new dtos.DataSetPermId(response.result.rows[0][0].value);
			});
		}.bind(this);

		this.getResponseFromJSTestAggregationService = function(facade, params, callback) {
			var c = this;
			return $.ajax({
				"url" : "http://localhost:20001/datastore_server/rmi-dss-api-v1.json",
				"type" : "POST",
				"processData" : false,
				"dataType" : "json",
				"data" : JSON.stringify({
					"method" : "createReportFromAggregationService",
					"params" : [ facade._private.sessionToken, "js-test", params ],
					"id" : "1",
					"jsonrpc" : "2.0"
				})
			}).then(callback);
		}.bind(this);

		this.createMaterial = function(facade) {
			var c = this;
			var creation = new dtos.MaterialCreation();
			creation.setCode(c.generateId("MATERIAL"));
			creation.setTypeId(new dtos.EntityTypePermId("COMPOUND"));
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
			var options = new dtos.SpaceDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSpaces([ id ], options);
		}.bind(this);

		this.deleteProject = function(facade, id) {
			var c = this;
			var options = new dtos.ProjectDeletionOptions();
			options.setReason("test reason");
			return facade.deleteProjects([ id ], options);
		}.bind(this);

		this.deleteExperiment = function(facade, id) {
			var c = this;
			var options = new dtos.ExperimentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperiments([ id ], options);
		}.bind(this);

		this.deleteSample = function(facade, id) {
			var c = this;
			var options = new dtos.SampleDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSamples([ id ], options);
		}.bind(this);

		this.deleteDataSet = function(facade, id) {
			var c = this;
			var options = new dtos.DataSetDeletionOptions();
			options.setReason("test reason");
			return facade.deleteDataSets([ id ], options);
		}.bind(this);

		this.deleteMaterial = function(facade, id) {
			var c = this;
			var options = new dtos.MaterialDeletionOptions();
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
			var fo = new dtos.SpaceFetchOptions();
			fo.withProjects();
			fo.withSamples();
			fo.withRegistrator();
			return fo;
		};

		this.createProjectFetchOptions = function() {
			var fo = new dtos.ProjectFetchOptions();
			fo.withSpace();
			fo.withExperiments();
			fo.withRegistrator();
			fo.withModifier();
			fo.withLeader();
			fo.withAttachments().withContent();
			return fo;
		};

		this.createExperimentFetchOptions = function() {
			var fo = new dtos.ExperimentFetchOptions();
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
			var fo = new dtos.SampleFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainer();
			fo.withComponents();
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
			var fo = new dtos.DataSetFetchOptions();
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSample();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withParents();
			fo.withChildren();
			fo.withContainers();
			fo.withComponents();
			fo.withPhysicalData().withFileFormatType();
			fo.withPhysicalData().withLocatorType();
			fo.withPhysicalData().withStorageFormat();
			fo.withHistory();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			return fo;
		};

		this.createMaterialFetchOptions = function() {
			var fo = new dtos.MaterialFetchOptions();
			fo.withType();
			fo.withHistory();
			fo.withRegistrator();
			fo.withProperties();
			fo.withMaterialProperties();
			fo.withTags();
			return fo;
		};

		this.createGlobalSearchObjectFetchOptions = function() {
			var fo = new dtos.GlobalSearchObjectFetchOptions();
			fo.withExperiment();
			fo.withSample();
			fo.withDataSet();
			fo.withMaterial();
			return fo;
		};
		
		this.createObjectKindModificationFetchOptions = function() {
			var fo = new dtos.ObjectKindModificationFetchOptions();
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
