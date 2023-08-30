define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ], function($, _, openbis, openbisExecuteOperations, common, dtos) {
	var executeModule = function(moduleName, openbis, dtos) {
		QUnit.module(moduleName);

		var testGet = function(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig) {
			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length > 0, "Entities were created");
					return fGet(facade, permIds).then(function(map) {
						c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
						permIds.forEach(function(permId) {
							var entity = map[permId];
							testFetchOptionsResults(c, fechOptionsTestConfig, true, entity);
							c.assertEqual(c.getId(entity).toString(), permId.toString(), "Entity perm id matches");
						});
						return fGetEmptyFetchOptions(facade, permIds).then(function(map) {
							c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
							permIds.forEach(function(permId) {
								var entity = map[permId];
								testFetchOptionsResults(c, fechOptionsTestConfig, false, entity);
								c.assertEqual(c.getId(entity).toString(), permId.toString(), "Entity perm id matches");
							});
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testGetManual = function(c, fCreate, fGet, fCheck) {
			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					return fGet(facade, permIds).then(function(persons) {
						fCheck(permIds, persons);
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testFetchOptionsAssignation = function(c, fo, toTest) {
			for (component in toTest) {
				if (component === "SortBy") {
					fo.sortBy().code();
					c.assertEqual(true, ((fo.getSortBy()) ? true : false), "Component " + component + " set on Fetch Options.");
				} else {
					var methodNameWithUsing = "with" + component + "Using";
					if (typeof fo[methodNameWithUsing] === "function") {
						fo[methodNameWithUsing](null);
					} else {
						throw methodNameWithUsing + " should be a method.";
					}

					var methodNameWith = "with" + component;
					if (typeof fo[methodNameWith] === "function") {
						fo[methodNameWith]();
					} else {
						throw methodNameWith + " should be a method.";
					}

					var methodNameHas = "has" + component;
					if (typeof fo[methodNameHas] === "function") {
						c.assertEqual(true, fo[methodNameHas](), "Component " + component + " set on Fetch Options.");
					} else {
						throw methodNameHas + " should be a method.";
					}
				}
			}
		}

		var testFetchOptionsResults = function(c, toTest, expectedShouldSucceed, entity) {
			for (property in toTest) {
				if (property !== "SortBy") {
					var methodName = "get" + property;
					var errorFound = null;
					if (typeof entity[methodName] === "function") {
						try {
							var result = entity[methodName](); // Should not
							// thrown an
							// exception,
							// what it means
							// is right!
						} catch (error) {
							errorFound = error;
						}
						var msg = (expectedShouldSucceed) ? "Succeed" : "Fail";
						c.assertEqual(expectedShouldSucceed, !errorFound, "Calling method " + methodName + " expected to " + msg);
					} else {
						throw methodName + " should be a method.";
					}
				}
			}
		}

		var getMethods = function(obj) {
			var result = [];
			for ( var id in obj) {
				try {
					if (typeof (obj[id]) == "function") {
						result.push(id + ": " + obj[id].toString());
					}
				} catch (err) {
					result.push(id + ": inaccessible");
				}
			}
			return result;
		}

		var getConfigForFetchOptions = function(fo) {
			var components = {};
			var methods = getMethods(fo);
			for (var mIdx = 0; mIdx < methods.length; mIdx++) {
				var method = methods[mIdx];
				if (method.startsWith("has")) {
					var component = method.substring(3, method.indexOf(':'));
					components[component] = null;
				}
			}
			return components;
		}

		QUnit.test("getSpaces()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.SpaceFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSpaces(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSpaces(permIds, new c.SpaceFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getProjects()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.ProjectFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {

				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getProjects(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getProjects(permIds, new c.ProjectFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getExperiments()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.ExperimentFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getExperiments(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getExperiments(permIds, new c.ExperimentFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getSamples()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.SampleFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSamples(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSamples(permIds, new c.SampleFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getDataSets()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.DataSetFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getDataSets(permIds, fo);

				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				var result = facade.getDataSets(permIds, new c.DataSetFetchOptions());

				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getMaterials()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.MaterialFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getMaterials(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getMaterials(permIds, new c.MaterialFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getPropertyTypes()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.PropertyTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createPropertyType(facade), c.createPropertyType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPropertyTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPropertyTypes(permIds, new c.PropertyTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getPlugins()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.PluginFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createPlugin(facade), c.createPlugin(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPlugins(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPlugins(permIds, new c.PluginFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getVocabularies()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.VocabularyFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createVocabulary(facade), c.createVocabulary(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getVocabularies(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getVocabularies(permIds, new c.VocabularyFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.VocabularyTermFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createVocabularyTerm(facade), c.createVocabularyTerm(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getVocabularyTerms(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getVocabularyTerms(permIds, new c.VocabularyTermFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getExternalDms()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.ExternalDmsFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createExternalDms(facade), c.createExternalDms(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getExternalDataManagementSystems(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getExternalDataManagementSystems(permIds, new c.ExternalDmsFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getTags()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.TagFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createTag(facade), c.createTag(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getTags(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getTags(permIds, new c.TagFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getAuthorizationGroups()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.AuthorizationGroupFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createAuthorizationGroup(facade), c.createAuthorizationGroup(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getAuthorizationGroups(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getAuthorizationGroups(permIds, new c.AuthorizationGroupFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getRoleAssignments() with user", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.RoleAssignmentFetchOptions();
			fo.withUser();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createRoleAssignment(facade, true)).then(function(id) {
					return [ id ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getRoleAssignments(permIds, fo);
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.getUser().getUserId(), "power_user", "User");
					});
				});
				return result;
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getRoleAssignments(permIds, new c.RoleAssignmentFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getRoleAssignments() with authorization group", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.RoleAssignmentFetchOptions();
			fo.withAuthorizationGroup();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createRoleAssignment(facade, false)).then(function(id) {
					return [ id ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getRoleAssignments(permIds, fo);
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.getAuthorizationGroup().getCode(), "TEST-GROUP", "Authorization group");
					});
				});
				return result;
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getRoleAssignments(permIds, new c.RoleAssignmentFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getPersons()", function(assert) {
			var c = new common(assert, openbis, dtos);

			var fCreate = function(facade) {
				return $.when(c.createPerson(facade), c.createPerson(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				var fo = new c.PersonFetchOptions();
				return facade.getPersons(permIds, fo);
			}

			var fCheck = function(permIds, persons) {
				c.assertEqual(Object.keys(persons).length, 2);
			}

			testGetManual(c, fCreate, fGet, fCheck);
		});

		QUnit.test("getPersons() with chosen webAppSettings", function(assert) {
			var WEB_APP_1 = "webApp1";
			var WEB_APP_2 = "webApp2";
			var WEB_APP_3 = "webApp3";

			var c = new common(assert, openbis, dtos);

			var fCreate = function(facade) {
				return c.createPerson(facade).then(function(permId) {
					var update = new c.PersonUpdate();
					update.setUserId(permId);

					var webApp1Update = update.getWebAppSettings(WEB_APP_1);
					webApp1Update.add(new c.WebAppSettingCreation("n1a", "v1a"));
					webApp1Update.add(new c.WebAppSettingCreation("n1b", "v1b"));

					var webApp2Update = update.getWebAppSettings(WEB_APP_2);
					webApp2Update.add(new c.WebAppSettingCreation("n2a", "v2a"));
					webApp2Update.add(new c.WebAppSettingCreation("n2b", "v2b"));

					var webApp3Update = update.getWebAppSettings(WEB_APP_3);
					webApp3Update.add(new c.WebAppSettingCreation("n3a", "v3a"));

					return facade.updatePersons([ update ]).then(function() {
						return [ permId ];
					});
				});
			}

			var fGet = function(facade, permIds) {
				var fo = new c.PersonFetchOptions();

				var webApp1Fo = fo.withWebAppSettings(WEB_APP_1);
				webApp1Fo.withAllSettings();

				var webApp2Fo = fo.withWebAppSettings(WEB_APP_2);
				webApp2Fo.withSetting("n2b");

				return facade.getPersons(permIds, fo);
			}

			var fCheck = function(permIds, persons) {
				c.assertEqual(Object.keys(persons).length, 1);

				var person = persons[permIds[0]];
				c.assertEqual(Object.keys(person.getWebAppSettings()).length, 2);

				var webApp1 = person.getWebAppSettings(WEB_APP_1);
				c.assertEqual(Object.keys(webApp1.getSettings()).length, 2);
				c.assertEqual(webApp1.getSetting("n1a").getValue(), "v1a");
				c.assertEqual(webApp1.getSetting("n1b").getValue(), "v1b");

				var webApp2 = person.getWebAppSettings(WEB_APP_2);
				c.assertEqual(Object.keys(webApp2.getSettings()).length, 1);
				c.assertEqual(webApp2.getSetting("n2b").getValue(), "v2b");
			}

			testGetManual(c, fCreate, fGet, fCheck);
		});

		QUnit.test("getPersons() with all webAppSettings", function(assert) {
			var WEB_APP_1 = "webApp1";
			var WEB_APP_2 = "webApp2";
			var WEB_APP_3 = "webApp3";

			var c = new common(assert, openbis, dtos);

			var fCreate = function(facade) {
				return c.createPerson(facade).then(function(permId) {
					var update = new c.PersonUpdate();
					update.setUserId(permId);

					var webApp1Update = update.getWebAppSettings(WEB_APP_1);
					webApp1Update.add(new c.WebAppSettingCreation("n1a", "v1a"));
					webApp1Update.add(new c.WebAppSettingCreation("n1b", "v1b"));

					var webApp2Update = update.getWebAppSettings(WEB_APP_2);
					webApp2Update.add(new c.WebAppSettingCreation("n2a", "v2a"));
					webApp2Update.add(new c.WebAppSettingCreation("n2b", "v2b"));

					return facade.updatePersons([ update ]).then(function() {
						return [ permId ];
					});
				});
			}

			var fGet = function(facade, permIds) {
				var fo = new c.PersonFetchOptions();
				fo.withAllWebAppSettings();

				return facade.getPersons(permIds, fo);
			}

			var fCheck = function(permIds, persons) {
				c.assertEqual(Object.keys(persons).length, 1);

				var person = persons[permIds[0]];
				c.assertEqual(Object.keys(person.getWebAppSettings()).length, 2);

				var webApp1 = person.getWebAppSettings(WEB_APP_1);
				c.assertEqual(Object.keys(webApp1.getSettings()).length, 2);
				c.assertEqual(webApp1.getSetting("n1a").getValue(), "v1a");
				c.assertEqual(webApp1.getSetting("n1b").getValue(), "v1b");

				var webApp2 = person.getWebAppSettings(WEB_APP_2);
				c.assertEqual(Object.keys(webApp2.getSettings()).length, 2);
				c.assertEqual(webApp2.getSetting("n2a").getValue(), "v2a");
				c.assertEqual(webApp2.getSetting("n2b").getValue(), "v2b");
			}

			testGetManual(c, fCreate, fGet, fCheck);
		});

		QUnit.test("getOperationExecutions()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.OperationExecutionFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createOperationExecution(facade), c.createOperationExecution(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getOperationExecutions(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getOperationExecutions(permIds, new c.OperationExecutionFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getSemanticAnnotations()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.SemanticAnnotationFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createSemanticAnnotation(facade), c.createSemanticAnnotation(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSemanticAnnotations(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSemanticAnnotations(permIds, new c.SemanticAnnotationFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getQueries()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.QueryFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createQuery(facade), c.createQuery(facade)).then(function(techId1, techId2) {
					return [ techId1, techId2 ];
				});
			}

			var fGet = function(facade, techIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getQueries(techIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, techIds) {
				return facade.getQueries(techIds, new c.QueryFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getQueryDatabases()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.QueryDatabaseFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				var dfd = $.Deferred();
				dfd.resolve([ new c.QueryDatabaseName("openbisDB"), new c.QueryDatabaseName("test-query-database") ]);
				return dfd.promise();
			}

			var fGet = function(facade, techIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getQueryDatabases(techIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, techIds) {
				return facade.getQueryDatabases(techIds, new c.QueryDatabaseFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getExperimentTypes()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.ExperimentTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createExperimentType(facade), c.createExperimentType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getExperimentTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getExperimentTypes(permIds, new c.ExperimentTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getSampleTypes()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.SampleTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createSampleType(facade), c.createSampleType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSampleTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSampleTypes(permIds, new c.SampleTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getDataSetTypes()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.DataSetTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createDataSetType(facade), c.createDataSetType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getDataSetTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getDataSetTypes(permIds, new c.DataSetTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getMaterialTypes()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.MaterialTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createMaterialType(facade), c.createMaterialType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getMaterialTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getMaterialTypes(permIds, new c.MaterialTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getRights()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var sampleId = new c.SampleIdentifier("/PLATONIC/SCREENING-EXAMPLES/PLATE-2");
			c.start();
			
			c.createFacadeAndLogin().then(function(facade) {
				return facade.getRights([sampleId], new c.RightsFetchOptions()).then(function(rights) {
                    var rights = rights[sampleId].rights;
                    rights.sort();
                    c.assertEqual(rights, "DELETE,UPDATE", "Rights");
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});
		
		QUnit.test("getServerInformation()", function(assert) {
			var c = new common(assert, openbis, dtos);
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return facade.getServerInformation().then(function(serverInformation) {
					c.assertTrue(serverInformation != null);
					c.assertEqual(serverInformation["api-version"], "3.6", "api-version");
					c.assertEqual(serverInformation["project-samples-enabled"], "true", "project-samples-enabled");
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("getServerPublicInformation()", function(assert) {
			var c = new common(assert, openbis, dtos);
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return facade.getServerPublicInformation().then(function(serverInformation) {
					c.assertTrue(serverInformation != null);
					c.assertEqual(Object.keys(serverInformation).length, 4);
					c.assertEqual(serverInformation["authentication-service"], "dummy-authentication-service", "authentication-service");
					c.assertEqual(serverInformation["authentication-service.switch-aai.link"], "testSwitchAaiLink", "authentication-service.switch-aai.link");
					c.assertEqual(serverInformation["authentication-service.switch-aai.label"], "testSwitchAaiLabel", "authentication-service.switch-aai.label");
					c.assertEqual(serverInformation["openbis.support.email"], "cisd.helpdesk@bsse.ethz.ch", "openbis.support.email");
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("getPersonalAccessTokens()", function(assert) {
			var c = new common(assert, openbis, dtos);
			var fo = new c.PersonalAccessTokenFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createPersonalAccessToken(facade), c.createPersonalAccessToken(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPersonalAccessTokens(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPersonalAccessTokens(permIds, new c.PersonalAccessTokenFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

	}

	return function() {
		executeModule("Get tests", openbis, dtos);
		executeModule("Get tests (executeOperations)", openbisExecuteOperations, dtos);
        executeModule("Get tests (module UMD)", window.openbis.openbis, window.openbis);
        executeModule("Get tests (module ESM)", window.openbisESM.openbis, window.openbisESM);
	}
});
