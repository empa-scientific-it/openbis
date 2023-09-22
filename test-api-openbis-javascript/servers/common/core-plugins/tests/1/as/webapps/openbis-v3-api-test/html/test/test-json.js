define([ 'jquery', 'underscore', 'test/common', 'test/dtos' ], function($, _, common, dtos) {
	var executeModule = function(moduleName, dtos) {
		QUnit.module(moduleName);

		QUnit.test("fromJSON with object id before object definition (in top level maps)", function(assert) {
			var c = new common(assert, dtos);
			c.start();

			var json = {
				"23" : {
					"@type" : "as.dto.query.Query",
					"@id" : 1,
					"fetchOptions" : {
						"@type" : "as.dto.query.fetchoptions.QueryFetchOptions",
						"@id" : 2,
						"registrator" : {
							"@type" : "as.dto.person.fetchoptions.PersonFetchOptions",
							"@id" : 3
						}
					},
					"permId" : {
						"@type" : "as.dto.query.id.QueryTechId",
						"@id" : 4,
						"techId" : 23,
					},
					"name" : "V3_QUERY_2018_5_4_21_26_476162",
					"databaseId" : {
						"@type" : "as.dto.query.id.QueryDatabaseName",
						"@id" : 5,
						"name" : "openbisDB"
					},
					"queryType" : "GENERIC",
					"sql" : "select code from spaces",
					"publicFlag" : false,
					"registrationDate" : 1525462005857,
					"registrator" : {
						"@type" : "as.dto.person.Person",
						"@id" : 6,
						"fetchOptions" : {
							"@type" : "as.dto.person.fetchoptions.PersonFetchOptions",
							"@id" : 7
						},
						"permId" : {
							"@type" : "as.dto.person.id.PersonPermId",
							"@id" : 8,
							"permId" : "openbis_test_js"
						},
						"userId" : "openbis_test_js",
						"firstName" : "Karel",
						"lastName" : "Mallarmé",
						"email" : "franz-josef.elmer@systemsx.ch",
						"registrationDate" : 1365770153704,
						"active" : true
					},
					"modificationDate" : 1525462005857
				},
				"22" : {
					"@type" : "as.dto.query.Query",
					"@id" : 9,
					"fetchOptions" : 2,
					"permId" : {
						"@type" : "as.dto.query.id.QueryTechId",
						"@id" : 11,
						"techId" : 22
					},
					"name" : "V3_QUERY_2018_5_4_21_26_374293",
					"databaseId" : 5,
					"queryType" : "GENERIC",
					"sql" : "select code from spaces",
					"registrationDate" : 1525462005857,
					"registrator" : 6,
					"modificationDate" : 1525462005857
				}
			};

			var returnType = {
				name : "Map",
				arguments : [ "IQueryId", "Query" ]
			};

			try {
				dtos.Json.fromJson(returnType, json).done(function(result) {
					c.assertEqual(result[22].getRegistrator().getUserId(), "openbis_test_js", "22.Registrator.userId");
					c.assertEqual(result[23].getRegistrator().getUserId(), "openbis_test_js", "23.Registrator.userId");
					c.finish();
				}).fail(function(error) {
					c.fail(error);
					c.finish();
				});
			} catch (error) {
				c.fail(error);
				c.finish();
			}
		});

		QUnit.test("fromJSON with object id before object definition (in nested maps)", function(assert) {
			var c = new common(assert, dtos);
			c.start();

			var json = {
				"23" : {
					"@id" : 1,
					"@type" : "as.dto.sample.Sample",
					"code" : "sample_1",
					"materialProperties" : {
						"230" : {
							"@id" : 2,
							"@type" : "as.dto.material.Material",
							"code" : "material_1",
							"registrator" : {
								"@id" : 3,
								"@type" : "as.dto.person.Person",
								"userId" : "person_2"
							},
							"fetchOptions" : {
								"@id" : 4,
								"@type" : "as.dto.material.fetchoptions.MaterialFetchOptions",
								"registrator" : {
									"@id" : 5,
									"@type" : "as.dto.person.fetchoptions.PersonFetchOptions"
								}
							}
						},
						"220" : {
							"@id" : 6,
							"@type" : "as.dto.material.Material",
							"code" : "material_2",
							"registrator" : 3,
							"fetchOptions" : 4
						}
					},
					"registrator" : {
						"@id" : 7,
						"@type" : "as.dto.person.Person",
						"userId" : "person_1"
					},
					"fetchOptions" : {
						"@id" : 8,
						"@type" : "as.dto.sample.fetchoptions.SampleFetchOptions",
						"materialProperties" : {
							"@id" : 9,
							"@type" : "as.dto.material.fetchoptions.MaterialFetchOptions"
						},
						"registrator" : {
							"@id" : 10,
							"@type" : "as.dto.person.fetchoptions.PersonFetchOptions"
						}
					}
				},
				"22" : {
					"@id" : 11,
					"@type" : "as.dto.sample.Sample",
					"code" : "sample_2",
					"materialProperties" : {
						"230" : 2,
						"220" : 6
					},
					"registrator" : 7,
					"fetchOptions" : 8
				}
			};

			var returnType = {
				name : "Map",
				arguments : [ null, "Sample" ]
			};

			try {
				dtos.Json.fromJson(returnType, json).done(function(result) {
					c.assertEqual(result[23].getCode(), "sample_1", "23.code");
					c.assertEqual(result[23].getMaterialProperties()[230].getCode(), "material_1", "23.materialProperties.230.code");
					c.assertEqual(result[23].getMaterialProperties()[230].getRegistrator().getUserId(), "person_2", "23.materialProperties.230.registrator.userId");
					c.assertEqual(result[23].getMaterialProperties()[220].getCode(), "material_2", "23.materialProperties.220.code");
					c.assertEqual(result[23].getMaterialProperties()[220].getRegistrator().getUserId(), "person_2", "23.materialProperties.220.registrator.userId");
					c.assertEqual(result[23].getRegistrator().getUserId(), "person_1", "23.registrator.userId");
					c.assertEqual(result[22].getCode(), "sample_2", "22.code");
					c.assertEqual(result[22].getMaterialProperties()[230].getCode(), "material_1", "22.materialProperties.230.code");
					c.assertEqual(result[22].getMaterialProperties()[220].getCode(), "material_2", "22.materialProperties.220.code");
					c.assertEqual(result[22].getRegistrator().getUserId(), "person_1", "22.registrator.userId");
					c.finish();
				}).fail(function(error) {
					c.fail(error);
					c.finish();
				});
			} catch (error) {
				c.fail(error);
				c.finish();
			}
		});

	}

	return function() {
		executeModule("Json tests (RequireJS)", dtos);
		executeModule("Json tests (module VAR)", window.openbis);
		executeModule("Json tests (module ESM)", window.openbisESM);
	}
});
