define([ 'jquery', 'openbis' ], function($, openbis) {
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
	}

	$.extend(Common.prototype, {

		createObject : function() {
			var dfd = $.Deferred();
			var objectPath = arguments[0];
			var objectParameters = [];

			for (var i = 1; i < arguments.length; i++) {
				objectParameters.push(arguments[i]);
			}

			require([ objectPath ], function(objectClass) {
				objectParameters.unshift(objectClass);
				var object = new (objectClass.bind.apply(objectClass, objectParameters))();
				dfd.resolve(object);
			});

			return dfd.promise();
		},

		createFacade : function(action) {
			var facade = new openbis(testApiUrl);
			action(facade);
		},

		createFacadeAndLogin : function() {
			var dfd = $.Deferred();

			this.createFacade(function(facade) {
				facade.login(testUserId, testUserPassword).done(function() {
					dfd.resolve(facade);
				}).fail(function() {
					dfd.reject(arguments);
				});
			});

			return dfd.promise();
		},

		createSpacePermId : function(permId) {
			return this.createObject('dto/id/space/SpacePermId', permId);
		},

		createProjectPermId : function(permId) {
			return this.createObject('dto/id/project/ProjectPermId', permId);
		},

		createProjectIdentifier : function(identifier) {
			return this.createObject('dto/id/project/ProjectIdentifier', identifier);
		},

		createExperimentPermId : function(permId) {
			return this.createObject('dto/id/experiment/ExperimentPermId', permId);
		},

		createExperimentIdentifier : function(identifier) {
			return this.createObject('dto/id/experiment/ExperimentIdentifier', identifier);
		},

		createSamplePermId : function(permId) {
			return this.createObject('dto/id/sample/SamplePermId', permId);
		},

		createMaterialPermId : function(code, typeCode) {
			return this.createObject('dto/id/material/MaterialPermId', code, typeCode);
		},

		createSpaceSearchCriterion : function() {
			return this.createObject('dto/search/SpaceSearchCriterion');
		},

		createProjectSearchCriterion : function() {
			return this.createObject('dto/search/ProjectSearchCriterion');
		},

		createExperimentSearchCriterion : function() {
			return this.createObject('dto/search/ExperimentSearchCriterion');
		},

		createSampleSearchCriterion : function() {
			return this.createObject('dto/search/SampleSearchCriterion');
		},

		createMaterialSearchCriterion : function() {
			return this.createObject('dto/search/MaterialSearchCriterion');
		},

		createSpaceFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/space/SpaceFetchOptions');
			promise.done(function(fo) {
				fo.withProjects();
				fo.withSamples();
				fo.withRegistrator();
			});
			return promise;
		},

		createProjectFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/project/ProjectFetchOptions');
			promise.done(function(fo) {
				fo.withSpace();
				fo.withExperiments();
				fo.withRegistrator();
				fo.withModifier();
				fo.withLeader();
				fo.withAttachments().withContent();
			});
			return promise;
		},

		createExperimentFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/experiment/ExperimentFetchOptions');
			promise.done(function(fo) {
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
			})
			return promise;
		},

		createSampleFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/sample/SampleFetchOptions');
			promise.done(function(fo) {
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
			});
			return promise;
		},

		createMaterialFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/material/MaterialFetchOptions');
			promise.done(function(fo) {
				fo.withType();
				fo.withHistory();
				fo.withRegistrator();
				fo.withProperties();
				fo.withMaterialProperties();
				fo.withTags();
			});
			return promise;
		},

		assertEqual : function(actual, expected, msg) {
			this.assert.equal(actual, expected, msg);
		},

		assertNotEqual : function(actual, expected, msg) {
			this.assert.notEqual(actual, expected, msg);
		},

		assertDate : function(millis, msg, year, month, day, hour, minute) {
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
		},

		assertToday : function(millis, msg) {
			var today = new Date();
			this.assertDate(millis, msg, today.getUTCFullYear(), today.getUTCMonth() + 1, today.getUTCDate());
		},

		assertObjectsCount : function(objects, count) {
			this.assertEqual(objects.length, count, 'Got ' + count + ' object(s)');
		},

		assertObjectsWithOrWithoutCollections : function(objects, accessor, checker) {
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
		},

		assertObjectsWithCollections : function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.some(function(object) {
					var value = accessor(object);
					return value && Object.keys(value).length > 0;
				}), 'Objects have non-empty collections accessed via: ' + accessor);
			});
		},

		assertObjectsWithoutCollections : function(objects, accessor) {
			var thisCommon = this;
			this.assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
				thisCommon.assert.ok(objects.every(function(object) {
					var value = accessor(object);
					return !value || Object.keys(value).length == 0;
				}), 'Objects have empty collections accessed via: ' + accessor);
			});
		},

		fail : function(msg) {
			this.assert.ok(false, msg);
		}

	});

	return Common;
})
