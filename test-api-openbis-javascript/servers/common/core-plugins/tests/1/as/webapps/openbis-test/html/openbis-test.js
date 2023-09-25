/*
 * These tests should be run against openBIS instance 
 * with screening sprint server database version
 */

var testProtocol = window.location.protocol;
var testHost = window.location.hostname;
var testPort = window.location.port;
var testUrl = testProtocol + "//" + testHost + ":" + testPort;

var testUserId = "openbis_test_js";
var testUserPassword = "password";

var createFacadeAndLogin = function(action, urlOrNull, timeoutOrNull) {
	var url = typeof urlOrNull == "undefined" ? testUrl : urlOrNull;
	createFacadeAndLoginForUserAndPassword(testUserId, testUserPassword, action, url, timeoutOrNull);
}

var createMaterialIdentifier = function(identifierString) {
	var parts = identifierString.split("/");

	return {
		"@type" : "MaterialIdentifierGeneric",
		"materialTypeIdentifier" : {
			"@type" : "MaterialTypeIdentifierGeneric",
			"materialTypeCode" : parts[1]
		},
		"materialCode" : parts[2]
	};
}

var createMetaprojectIdentifierId = function(identifierString) {
	return {
		"@type" : "MetaprojectIdentifierId",
		"identifier" : identifierString
	};
}

var createProjectIdentifierId = function(identifierString) {
	return {
		"@type" : "ProjectIdentifierId",
		"identifier" : identifierString
	};
}

var createExperimentIdentifierId = function(identifierString) {
	return {
		"@type" : "ExperimentIdentifierId",
		"identifier" : identifierString
	};
}

var createSampleIdentifierId = function(identifierString) {
	return {
		"@type" : "SampleIdentifierId",
		"identifier" : identifierString
	};
}

var createNewVocabularyTerm = function(code, previousTermOrdinal) {
	return {
		"@type" : "NewVocabularyTerm",
		"code" : code,
		"previousTermOrdinal" : previousTermOrdinal
	};
}

var createWebAppSettings = function(webAppId, settings) {
	return {
		"@type" : "WebAppSettings",
		"webAppId" : webAppId,
		"settings" : settings
	};
}

var createDataSetFileDTO = function(dataSetCode, path, isRecursive) {
	return {
		"@type" : "DataSetFileDTO",
		"dataSetCode" : dataSetCode,
		"path" : path,
		"isRecursive" : isRecursive
	};
}

var createNewMetaproject = function(facade, identifierString, action) {
	var parts = identifierString.split("/");
	var ownerId = parts[1];
	var name = parts[2];

	facade.listMetaprojects(function(response) {
		var metaproject = findMetaproject(response.result, identifierString);

		if (metaproject) {
			var id = createMetaprojectIdentifierId(identifierString);

			facade.deleteMetaproject(id, function(response) {
				facade.createMetaproject(name, null, function(response) {
					action(response);
				});
			});
		} else {
			facade.createMetaproject(name, null, function(response) {
				action(response);
			});
		}
	});
}

var findMetaproject = function(metaprojects, identifierString) {
	var parts = identifierString.split("/");
	var ownerId = parts[1];
	var name = parts[2];

	return metaprojects.filter(function(metaproject) {
		return metaproject.ownerId == ownerId && metaproject.name == name;
	})[0];
}

var findVocabulary = function(vocabularies, code) {
	return vocabularies.filter(function(vocabulary) {
		return vocabulary.code == code;
	})[0];
};

var findVocabularyTerm = function(vocabulary, code) {
	return vocabulary.terms.filter(function(term) {
		return term.code == code;
	})[0];
};

var findQuery = function(queries, name) {
	return queries.filter(function(query) {
		return query.name == name;
	})[0];
}

var findVocabularyMaxOrdinal = function(vocabulary) {
	var max = 0;
	vocabulary.terms.forEach(function(term) {
		max = Math.max(max, term.ordinal);
	});
	return max;
};

var downloadFile = function(url, action) {
	$.ajax({
		url : url,
		cache : false,
		dataType : "text",
		success : function(data) {
			action(data);
		},
		error : function() {
			action(null);
		}
	});
}

var isHtml = function(page) {
	return page && page.indexOf("<html>") != -1;
}

var uploadFileToSessionWorkspace = function(facade, fileName, fileContent, dataStoreCode, action) {
	facade._internal.getDataStoreUrlForDataStoreCode(dataStoreCode, function(dataStoreUrl) {
		var uploadUrl = dataStoreUrl + "/session_workspace_file_upload" + "?filename=" + fileName + "&id=0" + "&startByte=0" + "&endByte=0" + "&sessionID=" + facade.getSession();

		$.ajax({
			url : uploadUrl,
			type : "POST",
			data : fileContent,
			contentType : "multipart/form-data"
		}).done(function(response) {
			action(response);
		});
	});
}

var uploadGraphToSessionWorkspace = function(facade, dataStoreCodeOrNull, action) {
	var fileName = generateRandomString();
	var fileContent = "" +

	"row	col	col1	col2	col3\n\
1	A	0	1	2\n\
1	B	3	4	5\n\
2	A	6	7	8\n\
2	B	9	10	11\n\
3	A	12	13	14\n\
3	B	15	16	17\n\
4	A	18	19	20\n\
4	B	21	22	23\n\
5	A	23	25	26\n\
5	B	27	28	29"

	uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCodeOrNull, function() {
		var graphType = "HISTOGRAM";
		var graphTitle = "Test graph";

		var graphConfig = new openbisGraphConfig(fileName, graphType, graphTitle);
		graphConfig["col-z"] = "<col1>Bins";
		graphConfig["bins"] = "13";
		graphConfig["delimiter"] = "\t";

		action(graphConfig);
	});
}

var generateRandomString = function() {
	return Math.random().toString();
}

test("new openbis()", function() {
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server without url");
		facade.close();
	}, null);
});

test("new openbis(/openbis)", function() {
	var url = "/openbis";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(/openbis/openbis)", function() {
	var url = "/openbis/openbis";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort;
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort + "/openbis";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort + "/openbis/";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/openbis)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort + "/openbis/openbis";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/openbis/)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort + "/openbis/openbis/";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /someRandomPath/)", function() {
	var url = testProtocol + "//" + testHost + ":" + testPort + "/someRandomPath/";
	createFacadeAndLogin(function(facade) {
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("logout", function() {
	createFacade(function(facade) {
		facade.logout(function() {
			equal(facade.getSession(), null, 'Session is empty after logout');

			facade.restoreSession();
			equal(facade.getSession(), null, 'Restored session is empty after logout');

			facade.isSessionActive(function(response) {
				equal(response.result, false, 'Session is inactive after logout');
				facade.close();
			});
		});
	}, testUrl);
});

test("login", function() {
	createFacade(function(facade) {
		facade.login(testUserId, testUserPassword, function(response) {
			ok(response.result, 'Session from server is not empty after login');
			ok(facade.getSession(), 'Session from facade is not empty after login');

			facade.isSessionActive(function(response) {
				equal(response.result, true, 'Session is active after login');
				facade.close();
			});
		});
	}, testUrl);
});

test("cookies", function() {
	createFacade(function(facade) {
		facade.useSession('session-1');
		facade.rememberSession();
		equal(facade.getSession(), 'session-1', 'Session 1 used')

		facade.useSession('session-2');
		equal(facade.getSession(), 'session-2', 'Session 2 used')

		facade.restoreSession();
		equal(facade.getSession(), 'session-1', 'Session 1 restored')
		facade.close();
	}, testUrl);
});

test("deleteProjects()", function() {
	createFacadeAndLogin(function(facade) {
		facade.deleteProjects([ -1 ], "some reason", function(response) {
			equal(response.error.message, "Project with ID -1 does not exist. Maybe someone has just deleted it.");
			facade.close();
		});
	});
});

test("deleteExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		facade.deleteExperiments([ 2 ], "some reason", "PERMANENT", function(response) {
			ok(response.error.message.indexOf("Experiment is being used") == 0, "Cannot delete an experiment with dependencies.");
			facade.close();
		});
	});
});

test("deleteSamples()", function() {
	createFacadeAndLogin(function(facade) {
		facade.deleteSamples([ 2 ], "some reason", "PERMANENT", function(response) {
			ok(response.error.message.indexOf("Sample is being used") == 0, "Cannot delete a sample with dependencies.");
			facade.close();
		});
	});
});

test("deleteDataSets()", function() {
	createFacadeAndLogin(function(facade) {
		facade.deleteDataSets([ "20130415093804724-403" ], "some reason", "PERMANENT", function(response) {
			ok(response.error.message.indexOf("Deletion failed because the following data sets have 'Disallow deletion' flag set to true in their type") == 0,
					"Cannot delete a data set with deletion_disallow flag set to true.");
			facade.close();
		});
	});
});

test("deleteDataSetsForced()", function() {
	createFacadeAndLogin(function(facade) {
		facade.deleteDataSetsForced([ "20130415093804724-403" ], "some reason", "PERMANENT", function(response) {
			ok(response.error.message.indexOf("Authorization failure") == 0, "Don't have enough privileges to use a forced version of deleting data sets.");
			facade.close();
		});
	});
});

test("listNamedRoleSets()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listNamedRoleSets(function(response) {
			ok(response.result, 'Got results');
			facade.close();
		});
	});
});

test("listSpacesWithProjectsAndRoleAssignments()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listSpacesWithProjectsAndRoleAssignments(null, function(response) {
			assertObjectsCount(response.result, 10);
			facade.close();
		});
	});
});

test("searchForSamples()", function() {
	createFacadeAndLogin(function(facade) {

		var sampleCodes = [ 'PLATE-1', 'PLATE-1A' ];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);

		facade.searchForSamples(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, sampleCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("searchForSamples() withRegistratorUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "REGISTRATOR_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForSamples(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "TEST-SAMPLE-1", "TEST-SAMPLE-2" ]);
			facade.close();
		});
	});
});

test("searchForSamples() withModifierUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "MODIFIER_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForSamples(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "PLATE-1A", "SERIES-1" ]);
			facade.close();
		});
	});
});

test("searchForSamplesWithFetchOptions()", function() {
	createFacadeAndLogin(function(facade) {

		var sampleCodes = [ 'PLATE-1', 'PLATE-1A' ];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);
		var fetchOptions = [ 'BASIC' ];

		facade.searchForSamplesWithFetchOptions(searchCriteria, fetchOptions, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, sampleCodes);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("searchForSamplesOnBehalfOfUser()", function() {
	createFacadeAndLogin(function(facade) {

		var sampleCodes = [ 'PLATE-1', 'PLATE-1A' ]
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes)
		var fetchOptions = [ 'BASIC' ];
		var userId = 'test_space_admin';

		facade.searchForSamplesOnBehalfOfUser(searchCriteria, fetchOptions, userId, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ 'PLATE-1A' ]);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("filterSamplesVisibleToUser()", function() {
	createFacadeAndLogin(function(facade) {

		var sampleCodes = [ 'PLATE-1', 'PLATE-1A' ];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var userId = 'test_space_admin';

			facade.filterSamplesVisibleToUser(samples, userId, function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'PLATE-1A' ]);
				assertObjectsWithProperties(response.result);
				facade.close();
			});
		});
	});
});

test("listSamplesForExperiment()", function() {
	createFacadeAndLogin(function(facade) {
		var experimentIdentifier = '/PLATONIC/SCREENING-EXAMPLES/EXP-1';

		facade.listSamplesForExperiment(experimentIdentifier, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ 'PLATE-1', 'PLATE-2' ]);
			facade.close();
		});
	});
});

test("listDataSetsForSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'PLATE-1' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;

			facade.listDataSetsForSamples(samples, function(response) {
				assertObjectsCount(response.result, 11);
				facade.close();
			});
		});
	});
});

test("listExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listProjects(function(response) {
			var projects = response.result.filter(function(project) {
				return project.code == 'SCREENING-EXAMPLES';
			});
			var experimentType = 'MICROSCOPY_PLATONIC';

			facade.listExperiments(projects, experimentType, function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'EXP-2' ]);
				facade.close();
			});
		});
	});
});

test("listExperimentsHavingSamples()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listProjects(function(response) {
			var projects = response.result.filter(function(project) {
				return project.code == 'SCREENING-EXAMPLES';
			});
			var experimentType = 'MICROSCOPY_PLATONIC';

			facade.listExperimentsHavingSamples(projects, experimentType, function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'EXP-2' ]);
				facade.close();
			});
		});
	});
});

test("listExperimentsHavingDataSets()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listProjects(function(response) {
			var projects = response.result.filter(function(project) {
				return project.code == 'SCREENING-EXAMPLES';
			});
			var experimentType = 'MICROSCOPY_PLATONIC';

			facade.listExperimentsHavingDataSets(projects, experimentType, function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'EXP-2' ]);
				facade.close();
			});
		});
	});
});

test("filterExperimentsVisibleToUser()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'EXP-1', 'TEST-EXPERIMENT' ]);

		facade.searchForExperiments(searchCriteria, function(response) {
			var experiments = response.result;
			var userId = 'test_space_admin';

			facade.filterExperimentsVisibleToUser(experiments, userId, function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'TEST-EXPERIMENT' ]);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSample()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'PLATE-1' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var sample = response.result[0];
			var restrictToDirectlyConnected = true;

			facade.listDataSetsForSample(sample, restrictToDirectlyConnected, function(response) {
				assertObjectsCount(response.result, 11);
				facade.close();
			});
		});
	});
});

test("listDataStores()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listDataStores(function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ 'DSS1', 'DSS2' ]);
			facade.close();
		});
	});
});

test("getDefaultPutDataStoreBaseURL()", function() {
	createFacadeAndLogin(function(facade) {
		facade.getDefaultPutDataStoreBaseURL(function(response) {
			equal(response.result, "http://localhost:20001", 'URL is correct')
			facade.close();
		});
	});
});

test("tryGetDataStoreBaseURL()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = '20130415093804724-403';

		facade.tryGetDataStoreBaseURL(dataSetCode, function(response) {
			equal(response.result, "http://localhost:20002", 'URL is correct')
			facade.close();
		});
	});
});

test("getDataStoreBaseURLs()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCodes = [ '20130412152036861-380', '20130415093804724-403' ];

		facade.getDataStoreBaseURLs(dataSetCodes, function(response) {
			assertObjectsCount(response.result, 2);

			assertObjectsWithValuesFunction(response.result, "dataStoreURL", function(result) {
				return result.dataStoreURL;
			}, [ "http://localhost:20001", "http://localhost:20002" ]);

			assertObjectsWithValuesFunction(response.result, "dataSetCodes", function(result) {
				return result.dataSetCodes;
			}, [ '20130412152036861-380', '20130415093804724-403' ]);

			facade.close();
		});
	});
});

test("listDataSetTypes()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listDataSetTypes(function(response) {
		    debugger;
			assertObjectsCount(response.result, 38);
			facade.close();
		});
	});
});

test("listSampleTypes()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listSampleTypes(function(response) {
			assertObjectsCount(response.result, 40);
			facade.close();
		});
	});
});

test("listExperimentTypes()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listExperimentTypes(function(response) {
			assertObjectsCount(response.result, 7);
			facade.close();
		});
	});
});

test("listVocabularies()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listVocabularies(function(response) {
			assertObjectsCount(response.result, 61);
			facade.close();
		});
	});
});

test("listDataSetsForSamplesWithConnections() with parents", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'TEST-SAMPLE-2' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS' ];

			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100238098-408' ]);
				assertObjectsWithParentCodes(response.result);
				assertObjectsWithoutChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() with children", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'TEST-SAMPLE-2' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var connectionsToGet = [ 'CHILDREN' ];

			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100238098-408' ]);
				assertObjectsWithoutParentCodes(response.result);
				assertObjectsWithChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() with parents and children", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'TEST-SAMPLE-2' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS', 'CHILDREN' ];

			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100238098-408' ]);
				assertObjectsWithParentCodes(response.result);
				assertObjectsWithChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() without parents and children", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'TEST-SAMPLE-2' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var connectionsToGet = [];

			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100238098-408' ]);
				assertObjectsWithoutParentCodes(response.result);
				assertObjectsWithoutChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesOnBehalfOfUser()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'PLATE-1', 'TEST-SAMPLE-2' ]);

		facade.searchForSamples(searchCriteria, function(response) {
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			var userId = 'test_space_admin';

			facade.listDataSetsForSamplesOnBehalfOfUser(samples, connectionsToGet, userId, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100238098-408' ]);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'EXP-1', 'TEST-EXPERIMENT-2' ]);

		facade.searchForExperiments(searchCriteria, function(response) {
			var experiments = response.result;
			var connectionsToGet = [ 'PARENTS' ];

			facade.listDataSetsForExperiments(experiments, connectionsToGet, function(response) {
				assertObjectsCount(response.result, 14);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForExperimentsOnBehalfOfUser()", function() {
	createFacadeAndLogin(function(facade) {
		var searchCriteria = createSearchCriteriaForCodes([ 'EXP-1', 'TEST-EXPERIMENT-2' ]);

		facade.searchForExperiments(searchCriteria, function(response) {
			var experiments = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			var userId = 'test_space_admin';

			facade.listDataSetsForExperimentsOnBehalfOfUser(experiments, connectionsToGet, userId, function(response) {
				assertObjectsCount(response.result, 3);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("getDataSetMetaData()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCodes = [ '20130415093804724-403', '20130415100158230-407' ];

		facade.getDataSetMetaData(dataSetCodes, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("getDataSetMetaDataWithFetchOptions()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCodes = [ '20130415093804724-403', '20130415100158230-407' ];
		var fetchOptions = [ 'BASIC' ];

		facade.getDataSetMetaDataWithFetchOptions(dataSetCodes, fetchOptions, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("searchForDataSets()", function() {
	createFacadeAndLogin(function(facade) {

		var dataSetCodes = [ '20130412142205843-196', '20130415093804724-403', '20130415100158230-407' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);

		facade.searchForDataSets(searchCriteria, function(response) {
			assertObjectsCount(response.result, 3);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("searchForDataSets() withRegistratorUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "REGISTRATOR_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "selenium"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForDataSets(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "20130417094936021-428", "20130417094934693-427" ]);
			facade.close();
		});
	});
});

test("searchForDataSets() withModifierUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "MODIFIER_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "selenium"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForDataSets(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "20130412143121081-200", "20130412153119864-385" ]);
			facade.close();
		});
	});
});

test("searchForDataSetsOnBehalfOfUser()", function() {
	createFacadeAndLogin(function(facade) {

		var dataSetCodes = [ '20130412142205843-196', '20130415093804724-403', '20130415100158230-407' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);
		var userId = 'test_space_admin';

		facade.searchForDataSetsOnBehalfOfUser(searchCriteria, userId, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100158230-407' ]);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("searchOnSearchDomain()", function() {
	createFacadeAndLogin(function(facade) {

		var preferredSearchDomainOrNull = "echo-database";
		var optionalParametersOrNull = {
			"SEQ-1" : JSON.stringify({
				"searchDomain" : "Echo database",
				"permId" : "20130412150557128-205",
				"entityKind" : "SAMPLE",
				"entityType" : "PLATE",
				"propertyType" : "PLATE_GEO",
				"code" : "PLATE-1A"
			}),
			"SEQ-2" : JSON.stringify({
				"searchDomain" : "Echo database",
				"dataSetCode" : "20130415093804724-403",
				"pathInDataSet" : "PATH-2",
				"sequenceIdentifier" : "ID-2",
				"positionInSequence" : "2"
			})
		}

		facade.searchOnSearchDomain(preferredSearchDomainOrNull, "SEQ-2", optionalParametersOrNull, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, 'searchDomain.name', [ "echo-database" ]);
			assertObjectsWithValues(response.result, 'searchDomain.label', [ "Echo database" ]);
			assertObjectsWithValues(response.result, 'resultLocation.permId', [ "20130415093804724-403" ]);
			assertObjectsWithValues(response.result, 'resultLocation.code', [ "20130415093804724-403" ]);
			assertObjectsWithValues(response.result, 'resultLocation.entityType', [ "UNKNOWN" ]);
			assertObjectsWithValues(response.result, 'resultLocation.pathInDataSet', [ "PATH-2" ]);
			assertObjectsWithValues(response.result, 'resultLocation.identifier', [ "ID-2" ]);
			assertObjectsWithValues(response.result, 'resultLocation.position', [ "2" ]);
			facade.close();
		});
		facade.searchOnSearchDomain(preferredSearchDomainOrNull, "SEQ-1", optionalParametersOrNull, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, 'searchDomain.name', [ "echo-database" ]);
			assertObjectsWithValues(response.result, 'searchDomain.label', [ "Echo database" ]);
			assertObjectsWithValues(response.result, 'resultLocation.permId', [ "20130412150557128-205" ]);
			assertObjectsWithValues(response.result, 'resultLocation.code', [ "PLATE-1A" ]);
			assertObjectsWithValues(response.result, 'resultLocation.entityKind', [ "SAMPLE" ]);
			assertObjectsWithValues(response.result, 'resultLocation.entityType', [ "PLATE" ]);
			assertObjectsWithValues(response.result, 'resultLocation.propertyType', [ "PLATE_GEO" ]);
			facade.close();
		});
	});
});

test("listAvailableSearchDomains()", function() {
	createFacadeAndLogin(function(facade) {

		facade.listAvailableSearchDomains(function(response) {
			assertObjectsCount(response.result, 4);
			assertObjectsWithValues(response.result, 'name', [ "blastsearch", "echo-database" ]);
			assertObjectsWithValues(response.result, 'label', [ "Blast", "Echo database" ]);
			facade.close();
		});
	});
});

test("filterDataSetsVisibleToUser()", function() {
	createFacadeAndLogin(function(facade) {

		var dataSetCodes = [ '20130412142205843-196', '20130415093804724-403', '20130415100158230-407' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);

		facade.searchForDataSets(searchCriteria, function(response) {
			var dataSets = response.result;
			var userId = "test_space_admin";

			facade.filterDataSetsVisibleToUser(dataSets, userId, function(response) {
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20130415093804724-403', '20130415100158230-407' ]);
				assertObjectsWithProperties(response.result);
				facade.close();
			});
		});
	});
});

test("listExperimentsForIdentifiers()", function() {
	createFacadeAndLogin(function(facade) {
		var experimentIdentifiers = [ "/PLATONIC/SCREENING-EXAMPLES/EXP-1", "/TEST/TEST-PROJECT/TEST-EXPERIMENT" ];

		facade.listExperimentsForIdentifiers(experimentIdentifiers, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, 'identifier', experimentIdentifiers);
			facade.close();
		});
	});
});

test("searchForExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var experimentCodes = [ 'EXP-1' ];
		var searchCriteria = createSearchCriteriaForCodes(experimentCodes);

		facade.searchForExperiments(searchCriteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'identifier', [ "/PLATONIC/SCREENING-EXAMPLES/EXP-1" ]);
			facade.close();
		});
	});
});

test("searchForExperiments() withRegistratorUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "REGISTRATOR_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForExperiments(searchCriteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "TEST-EXPERIMENT" ]);
			facade.close();
		});
	});
});

test("searchForExperiments() withModifierUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "MODIFIER_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForExperiments(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "EXP-2", "TEST-EXPERIMENT-3" ]);
			facade.close();
		});
	});
});

test("listProjects()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listProjects(function(response) {
			assertObjectsCount(response.result, 19);
			assertObjectsWithCodes(response.result, [ "BACTERIA" ,"CELL_LINES" ,"DEFAULT_PROJECT" ,"FLIES" ,"ORDERS"
			,"PLANTS" ,"PLASMIDS" ,"POLYNUCLEOTIDES" ,"PRODUCTS" ,"PROTOCOLS" ,"PUBLIC_REPOSITORIES" ,"REAGENTS"
			,"REQUESTS" ,"SCREENING-EXAMPLES" ,"STORAGES" ,"SUPPLIERS" ,"TEMPLATES" ,"TEST-PROJECT" ,"YEASTS" ]);
			facade.close();
		});
	});
});

test("listProjectsOnBehalfOfUser()", function() {
	createFacadeAndLogin(function(facade) {
		var userId = "test_space_admin";

		facade.listProjectsOnBehalfOfUser(userId, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "TEST-PROJECT" ]);
			facade.close();
		});
	});
});

test("getMaterialByCodes()", function() {
	createFacadeAndLogin(function(facade) {
		var materialIdentifiers = [ createMaterialIdentifier("/GENE/G1") ];

		facade.getMaterialByCodes(materialIdentifiers, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'materialCode', [ "G1" ]);
			facade.close();
		});
	});
});

test("searchForMaterials()", function() {
	createFacadeAndLogin(function(facade) {
		var materialCodes = [ "G1" ];
		var searchCriteria = createSearchCriteriaForCodes(materialCodes);

		facade.searchForMaterials(searchCriteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'materialCode', [ "G1" ]);
			facade.close();
		});
	});
});

test("searchForMaterials() withRegistratorUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "REGISTRATOR_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForMaterials(searchCriteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "materialCode", [ "SIRNA-3", "SIRNA-4" ]);
			facade.close();
		});
	});
});

test("searchForMaterials() withModifierUserId", function() {
	createFacadeAndLogin(function(facade) {

		var searchCriteria = {
			"@type" : "SearchCriteria",
			matchClauses : [ {
				"@type" : "AttributeMatchClause",
				attribute : "MODIFIER_USER_ID",
				fieldType : "ATTRIBUTE",
				desiredValue : "etlserver"
			} ],
			operator : "MATCH_ANY_CLAUSES"
		};

		facade.searchForMaterials(searchCriteria, function(response) {
			// search by a modifier not supported yet
			assertObjectsCount(response.result, 0);
			facade.close();
		});
	});
});

test("createMetaproject(), listMetaprojects()", function() {
	createFacadeAndLogin(function(facade) {
		createNewMetaproject(facade, "/" + testUserId + "/JS_TEST_METAPROJECT", function(response) {
			facade.listMetaprojects(function(response) {
				assertObjectsCount(response.result, 1);
				assertObjectsWithValues(response.result, 'name', [ 'JS_TEST_METAPROJECT' ]);
				facade.close();
			});
		});
	});
});

test("createMetaproject(), listMetaprojectsOnBehalfOfUser()", function() {
	var powerUserId = "test_space_admin";
	var powerUserPassword = "password";

	createFacadeAndLoginForUserAndPassword(powerUserId, powerUserPassword, function(facadePowerUser) {
		var powerUserMetaprojectIdentifier = "/" + powerUserId + "/JS_TEST_METAPROJECT_POWER_USER"
		createNewMetaproject(facadePowerUser, powerUserMetaprojectIdentifier, function(response) {

			createFacadeAndLoginForUserAndPassword(testUserId, testUserPassword, function(facadeTestUser) {
				var testUserMetaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
				createNewMetaproject(facadeTestUser, testUserMetaprojectIdentifier, function(response) {

					facadeTestUser.listMetaprojectsOnBehalfOfUser(powerUserId, function(response) {
						assertObjectsCount(response.result, 1);
						assertObjectsWithValues(response.result, 'name', [ 'JS_TEST_METAPROJECT_POWER_USER' ]);
						facadePowerUser.close();
						facadeTestUser.close();
					});
				});
			}, testUrl);
		})
	}, testUrl);
});

test("createMetaproject(), getMetaproject()", function() {
	createFacadeAndLogin(function(facade) {
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";

		createNewMetaproject(facade, metaprojectIdentifier, function(response) {
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);

			facade.getMetaproject(metaprojectId, function(response) {
				equal(response.result.metaproject.identifier, metaprojectIdentifier, 'Metaproject identifier is correct');
				facade.close();
			});
		});
	});
});

test("createMetaproject(), getMetaprojectOnBehalfOfUser()", function() {
	var powerUserId = "test_space_admin";
	var powerUserPassword = "password";

	createFacadeAndLoginForUserAndPassword(powerUserId, powerUserPassword, function(facadePowerUser) {
		var powerUserMetaprojectIdentifier = "/" + powerUserId + "/JS_TEST_METAPROJECT_POWER_USER";
		var powerUserMetaprojectId = createMetaprojectIdentifierId(powerUserMetaprojectIdentifier);
		createNewMetaproject(facadePowerUser, powerUserMetaprojectIdentifier, function(response) {

			createFacadeAndLoginForUserAndPassword(testUserId, testUserPassword, function(facadeTestUser) {
				var testUserMetaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
				createNewMetaproject(facadeTestUser, testUserMetaprojectIdentifier, function(response) {

					facadeTestUser.getMetaprojectOnBehalfOfUser(powerUserMetaprojectId, powerUserId, function(response) {
						equal(response.result.metaproject.identifier, powerUserMetaprojectIdentifier, 'Metaproject identifier is correct');
						facadePowerUser.close();
						facadeTestUser.close();
					});
				});
			}, testUrl);
		})
	}, testUrl);
});

test("createMetaproject(), updateMetaproject()", function() {
	createFacadeAndLogin(function(facade) {
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";

		createNewMetaproject(facade, metaprojectIdentifier, function(response) {
			var metaproject = response.result;
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			var description = generateRandomString();

			ok(!metaproject.description, "Metaproject description was empty");

			facade.updateMetaproject(metaprojectId, metaproject.name, description, function(response) {
				facade.getMetaproject(metaprojectId, function(response) {
					equal(response.result.metaproject.description, description, "Metaproject description properly updated");
					facade.close();
				});
			})
		});
	});
});

test("createMetaproject(), deleteMetaproject()", function() {
	createFacadeAndLogin(function(facade) {
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";

		createNewMetaproject(facade, metaprojectIdentifier, function(response) {
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);

			facade.deleteMetaproject(metaprojectId, function(response) {
				facade.listMetaprojects(function(response) {
					ok(!findMetaproject(response.result, metaprojectIdentifier), "Metaproject has been deleted");
					facade.close();
				});
			})
		});
	});
});

test("createMetaproject(), addToMetaproject(), removeFromMetaproject()", function() {
	createFacadeAndLogin(function(facade) {
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";

		createNewMetaproject(facade, metaprojectIdentifier, function(response) {
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);

			facade.getMetaproject(metaprojectId, function(response) {
				var assignments = response.result;
				assertObjectsCount(response.result.samples, 0);

				var assignmentsIds = {
					"@type" : "MetaprojectAssignmentsIds",
					"samples" : [ createSampleIdentifierId("/PLATONIC/SCREENING-EXAMPLES/PLATE-1") ]
				};

				facade.addToMetaproject(metaprojectId, assignmentsIds, function(response) {
					facade.getMetaproject(metaprojectId, function(response) {
						var assignments = response.result;
						assertObjectsCount(assignments.samples, 1);
						assertObjectsWithCodes(assignments.samples, [ 'PLATE-1' ]);

						facade.removeFromMetaproject(metaprojectId, assignmentsIds, function(response) {
							facade.getMetaproject(metaprojectId, function(response) {
								var assignments = response.result;
								assertObjectsCount(assignments.samples, 0);
								facade.close();
							});
						});
					});
				});
			});
		});
	});
});

test("listAttachmentsForProject()", function() {
	createFacadeAndLogin(function(facade) {
		var projectId = createProjectIdentifierId("/TEST/TEST-PROJECT");

		facade.listAttachmentsForProject(projectId, false, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'fileName', [ 'projectAttachment' ]);
			facade.close();
		});
	});
});

test("listAttachmentsForExperiment()", function() {
	createFacadeAndLogin(function(facade) {
		var experimentId = createExperimentIdentifierId("/TEST/TEST-PROJECT/TEST-EXPERIMENT");

		facade.listAttachmentsForExperiment(experimentId, false, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "fileName", [ "experimentAttachment" ]);
			facade.close();
		});
	});
});

test("listAttachmentsForSample()", function() {
	createFacadeAndLogin(function(facade) {
		var sampleId = createSampleIdentifierId("/TEST/TEST-PROJECT/TEST-SAMPLE-2");

		facade.listAttachmentsForSample(sampleId, false, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "fileName", [ "sampleAttachment" ]);
			facade.close();
		});
	});
});

test("updateSampleProperties(), searchForSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var sampleCodes = [ 'TEST-SAMPLE-2' ];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);

		facade.searchForSamples(searchCriteria, function(response) {
			var sample = response.result[0];
			var description = generateRandomString();
			var properties = {
				"DESCRIPTION" : description
			};

			facade.updateSampleProperties(sample.id, properties, function(response) {
				facade.searchForSamples(searchCriteria, function(response) {
					var sample = response.result[0];
					equal(sample.properties["DESCRIPTION"], description, "Property value has been changed")
					facade.close();
				});
			});
		});
	});
});

test("addUnofficialVocabularyTerm(), listVocabularies()", function() {
	createFacadeAndLogin(function(facade) {
		var vocabularyCode = "TEST-VOCABULARY";
		var termCode = generateRandomString();

		facade.listVocabularies(function(response) {
			var originalVocabulary = findVocabulary(response.result, vocabularyCode);
			var originalTerm = findVocabularyTerm(originalVocabulary, termCode);

			ok(!originalTerm, 'Term did not exist');

			var term = createNewVocabularyTerm(termCode, findVocabularyMaxOrdinal(originalVocabulary));

			facade.addUnofficialVocabularyTerm(originalVocabulary.id, term, function(response) {
				facade.listVocabularies(function(response) {
					var updatedVocabulary = findVocabulary(response.result, vocabularyCode);
					var updatedTerm = findVocabularyTerm(updatedVocabulary, termCode);

					ok(updatedTerm, 'Term has been added');
					equal(updatedTerm.code, termCode, 'Term has correct code');

					facade.close();
				});
			});
		});
	});
});

test("setWebAppSettings(), getWebAppSettings()", function() {
	createFacadeAndLogin(function(facade) {
		var webAppId = generateRandomString();

		facade.getWebAppSettings(webAppId, function(response) {
			deepEqual(response.result.settings, {}, 'Web app settings are empty');

			var settings = {
				"param1" : "value1",
				"param2" : "value2"
			};
			var webAppSettings = createWebAppSettings(webAppId, settings);

			facade.setWebAppSettings(webAppSettings, function(response) {
				facade.getWebAppSettings(webAppId, function(response) {
					deepEqual(response.result.settings, settings, "Web app settings properly updated");
					facade.close();
				});
			});
		});
	});
});

test("listQueries()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listQueries(function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "name", [ "Test Query" ]);
			facade.close();
		});
	});
});

test("executeQuery()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listQueries(function(response) {
			var queryId = findQuery(response.result, "Test Query").id;
			var parameterBindings = {};

			facade.executeQuery(queryId, parameterBindings, function(response) {
				ok(response.result, "Query has been executed");
				equal(response.result.columns[0].title, "test_column", "Returned column has correct title");
				equal(response.result.rows[0][0].value, "test_value", "Returned row has correct value");
				facade.close();
			});
		});
	});
});

test("listTableReportDescriptions()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listTableReportDescriptions(function(response) {
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});

test("createReportFromDataSets()", function() {
	createFacadeAndLogin(function(facade) {
		var dataStoreCode = "DSS1";
		var serviceKey = "default-plate-image-analysis";
		var dataSetCodes = [ "20130412153659994-391" ];

		facade.createReportFromDataSets(dataStoreCode, serviceKey, dataSetCodes, function(response) {
			ok(response.result, "Report has been created");
			facade.close();
		});
	});
});

test("listAggregationServices()", function() {
	createFacadeAndLogin(function(facade) {
		facade.listAggregationServices(function(response) {
			assertObjectsCount(response.result, 20);
			facade.close();
		});
	});
});

test("createReportFromAggregationService()", function() {
	createFacadeAndLogin(function(facade) {
		var dataStoreCode = "DSS2";
		var serviceKey = "test-aggregation-service";
		var parameters = {};

		facade.createReportFromAggregationService(dataStoreCode, serviceKey, parameters, function(response) {
			ok(response.result, "Report has been created");
			facade.close();
		});
	});
});

test("getSessionTokenFromServer()", function() {
	createFacadeAndLogin(function(facade) {
		facade.getSessionTokenFromServer(function(response) {
			ok(response.result, "Got session token");
			facade.close();
		});
	});
});

test("listFilesForDataSetFile()", function() {
	createFacadeAndLogin(function(facade) {
		var fileOrFolder = createDataSetFileDTO("20130412152036861-380", "/original/PLATE-1A", true);

		facade.listFilesForDataSetFile(fileOrFolder, function(response) {
			assertObjectsCount(response.result, 864);
			facade.close();
		});
	});
});

test("getDownloadUrlForFileForDataSetFile()", function() {
	createFacadeAndLogin(function(facade) {
		var fileOrFolder = createDataSetFileDTO("20130412152036861-380", "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png", false);

		facade.getDownloadUrlForFileForDataSetFile(fileOrFolder, function(response) {
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data) {
				ok(data, "Download url works");
				facade.close();
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetFileWithTimeout()", function() {
	createFacadeAndLogin(function(facade) {
		var fileOrFolder = createDataSetFileDTO("20130412152036861-380", "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png", false);
		var validityDurationInSeconds = 10;

		facade.getDownloadUrlForFileForDataSetFileWithTimeout(fileOrFolder, validityDurationInSeconds, function(response) {
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data) {
				ok(data, "Download url works once");

				downloadFile(response.result, function(data) {
					ok(!data, "Download url does not work anymore");
					facade.close();
				});
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetFileInSession()", function() {
	createFacadeAndLogin(function(facade) {
		var fileOrFolder = createDataSetFileDTO("20130412152036861-380", "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png", false);

		facade.getDownloadUrlForFileForDataSetFileInSession(fileOrFolder, function(response) {
			ok(response, "Got download url");

			downloadFile(response, function(data) {
				ok(!isHtml(data), "Download url works");

				downloadFile(response, function(data) {
					ok(!isHtml(data), "Download url works more than once");

					facade.logout(function() {
						downloadFile(response, function(data) {
							ok(isHtml(data), "Download url does not work after logout");
							facade.close();
						});
					});
				});
			});
		});
	});
});

test("listFilesForDataSet()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var path = "/original";
		var recursive = true;

		facade.listFilesForDataSet(dataSetCode, path, recursive, function(response) {
			assertObjectsCount(response.result, 865);
			facade.close();
		});
	});
});

test("getDownloadUrlForFileForDataSet()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var path = "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png";

		facade.getDownloadUrlForFileForDataSet(dataSetCode, path, function(response) {
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data) {
				ok(data, "Download url works");
				facade.close();
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetInSession()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var path = "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png";

		facade.getDownloadUrlForFileForDataSetInSession(dataSetCode, path, function(response) {
			ok(response, "Got download url");

			downloadFile(response, function(data) {
				ok(!isHtml(data), "Download url works");

				downloadFile(response, function(data) {
					ok(!isHtml(data), "Download url works more than once");

					facade.logout(function() {
						downloadFile(response, function(data) {
							ok(isHtml(data), "Download url does not work after logout");
							facade.close();
						});
					});
				});
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetWithTimeout()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var path = "/original/PLATE-1A/bPLATE_wA10_s1_cRGB.png";
		var validityDurationInSeconds = 10;

		facade.getDownloadUrlForFileForDataSetWithTimeout(dataSetCode, path, validityDurationInSeconds, function(response) {
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data) {
				ok(data, "Download url works once");

				downloadFile(response.result, function(data) {
					ok(!data, "Download url does not work anymore");
					facade.close();
				});
			});
		});
	});
});

test("createSessionWorkspaceDownloadUrl()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			try {
				facade.createSessionWorkspaceDownloadUrl(fileName, function(downloadUrl) {
					downloadFile(downloadUrl, function(response) {
						ok(false);
					});
				});
			} catch (e) {
				ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
				facade.close();
			}
		});
	});
});

test("createSessionWorkspaceDownloadUrlForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			facade.createSessionWorkspaceDownloadUrlForDataStore(fileName, dataStoreCode, function(downloadUrl) {
				downloadFile(downloadUrl, function(response) {
					equal(response, fileContent, "Download url is correct");
					facade.close();
				});
			});
		});
	});
});

test("createSessionWorkspaceDownloadLink()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var linkText = generateRandomString();
		var dataStoreCode = "DSS2";

		facade.createSessionWorkspaceDownloadUrlForDataStore(fileName, dataStoreCode, function(downloadUrl) {
			try {
				facade.createSessionWorkspaceDownloadLink(fileName, linkText, function(link) {
					ok(false);
				});
			} catch (e) {
				ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
				facade.close();
			}
		});
	});
});

test("createSessionWorkspaceDownloadLinkForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var linkText = generateRandomString();
		var dataStoreCode = "DSS2";

		facade.createSessionWorkspaceDownloadUrlForDataStore(fileName, dataStoreCode, function(downloadUrl) {
			facade.createSessionWorkspaceDownloadLinkForDataStore(fileName, linkText, dataStoreCode, function(link) {
				equal($(link).attr("href"), downloadUrl, "Link has correct url");
				equal($(link).text(), linkText, "Link has correct text");
				facade.close();
			});
		});
	});
});

test("downloadSessionWorkspaceFile()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			try {
				facade.downloadSessionWorkspaceFile(fileName, function(response) {
					ok(false);
				});
			} catch (e) {
				ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
				facade.close();
			}
		});
	});
});

test("downloadSessionWorkspaceFileForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			facade.downloadSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function(response) {
				equal(response, fileContent, "File has been downloaded");
				facade.close();
			});
		});
	});
});

test("deleteSessionWorkspaceFile()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			try {
				facade.deleteSessionWorkspaceFile(fileName, function() {
					ok(false);
				});
			} catch (e) {
				ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
				facade.close();
			}
		});
	});
});

test("deleteSessionWorkspaceFileForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS2";

		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function() {
			facade.deleteSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function() {
				facade.downloadSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function(response) {
					ok(response.error.indexOf("No such file or directory"), "File has been deleted");
					facade.close();
				});
			});
		});
	});
});

test("getPathToDataSet()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var overrideStoreRootPathOrNull = "";

		facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response) {
			equal(response.result, "/1/1FD3FF61-1576-4908-AE3D-296E60B4CE06/7e/71/80/20130412152036861-380", "Data set path is correct");
			facade.close();
		});
	});
});

test("tryGetPathToDataSet()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130412152036861-380";
		var overrideStoreRootPathOrNull = "";

		facade.tryGetPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response) {
			equal(response.result, "/1/1FD3FF61-1576-4908-AE3D-296E60B4CE06/7e/71/80/20130412152036861-380", "Data set path is correct");
			facade.close();
		});
	});
});

test("listAllShares()", function() {
	createFacadeAndLogin(function(facade) {
		try {
			facade.listAllShares(function(response) {
				ok(false);
			});
		} catch (e) {
			ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
			facade.close();
		}
	});
});

test("listAllSharesForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var dataStoreCode = "DSS2";

		facade.listAllSharesForDataStore(dataStoreCode, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "shareId", [ "1", "2" ]);
			facade.close();
		});
	});
});

test("shuffleDataSet()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetCode = "20130415100308111-409";
		var share1Id = "1";
		var share2Id = "2";
		var overrideStoreRootPathOrNull = "";
		var isInShare = function(dataSetPath, shareId) {
			return dataSetPath.indexOf("/" + shareId + "/") == 0
		}

		facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response) {
			var toShareId = null;

			if (isInShare(response.result, share1Id)) {
				toShareId = share2Id;
			} else {
				toShareId = share1Id;
			}

			facade.shuffleDataSet(dataSetCode, toShareId, function(response) {
				facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response) {
					ok(isInShare(response.result, toShareId), "Data set has been moved to a different share");
					facade.close();
				});
			});
		});
	});
});

test("getValidationScript()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetTypeOrNull = "HCS_IMAGE";

		try {
			facade.getValidationScript(dataSetTypeOrNull, function(response) {
				ok(false);
			});
		} catch (e) {
			ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
			facade.close();
		}
	});
});

test("getValidationScriptForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetTypeOrNull = "HCS_IMAGE";
		var dataStoreCode = "DSS2";

		facade.getValidationScriptForDataStore(dataSetTypeOrNull, dataStoreCode, function(response) {
			ok(response.result, "Got a validation script");
			facade.close();
		});
	});
});

test("getGraphUrl()", function() {
	createFacadeAndLogin(function(facade) {
		var dataStoreCodeOrNull = "DSS2";

		uploadGraphToSessionWorkspace(facade, dataStoreCodeOrNull, function(graphConfig) {
			try {
				facade.getGraphUrl(graphConfig, function(graphUrl) {
					ok(false);
				});
			} catch (e) {
				ok(e == "There is more than one data store configured. Please specify a data store code to get a data store url.");
				facade.close();
			}
		});
	});
});

test("getGraphUrlForDataStore()", function() {
	createFacadeAndLogin(function(facade) {
		var dataStoreCodeOrNull = "DSS2";

		uploadGraphToSessionWorkspace(facade, dataStoreCodeOrNull, function(graphConfig) {
			facade.getGraphUrlForDataStore(graphConfig, dataStoreCodeOrNull, function(graphUrl) {
				downloadFile(graphUrl, function(data) {
					ok(!isHtml(data), "Graph has been generated");
					facade.close();
				})
			});
		});
	});
});

test("openbisWebAppContext()", function() {
	createFacadeAndLogin(function(facade) {
		var context = new openbisWebAppContext();
		equal(context.getWebappCode(), "openbis-test", "Webapp code is correct");
		ok(!context.getEntityKind(), "Entity kind is correct");
		ok(!context.getEntityType(), "Entity type is correct");
		ok(!context.getEntityIdentifier(), "Entity identifier is correct");
		ok(!context.getEntityPermId(), "Entity perm id is correct");

		facade.getSessionTokenFromServer(function(response) {
			equal(context.getSessionId(), response.result, "Session token is correct");
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createPropertyMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createPropertyMatch("$PLATE_GEOMETRY", "96_WELLS_8X12");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 4);
			assertObjectsWithCodes(response.result, [ "PLATE-1", "PLATE-1A", "PLATE-2" ]);
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createPropertyMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createPropertyMatch("$PLATE_GEOMETRY", "96_WELLS_8X12");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 4);
			assertObjectsWithCodes(response.result, [ "PLATE-1", "PLATE-1A", "PLATE-2" ]);
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createAttributeMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createAttributeMatch("CODE", "PLATE-1");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "PLATE-1" ]);
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createTimeAttributeMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createTimeAttributeMatch("REGISTRATION_DATE", "EQUALS", "2013-04-17", "0");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "SERIES-1" ]);
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createAnyPropertyMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createAnyPropertyMatch("96_WELLS_8X12");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 4);
			assertObjectsWithCodes(response.result, [ "PLATE-1", "PLATE-1A", "PLATE-2" ]);
			facade.close();
		});
	});
});

test("SearchCriteriaMatchClause.createAnyFieldMatch()", function() {
	createFacadeAndLogin(function(facade) {
		var clause = SearchCriteriaMatchClause.createAnyFieldMatch("PLATE-1");
		var criteria = new SearchCriteria();
		criteria.addMatchClause(clause);

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "PLATE-1" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createSampleParentCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var parentCriteria = new SearchCriteria();
		parentCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "TEST-SAMPLE-2"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentCriteria));

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "TEST-SAMPLE-2-CHILD-1", "TEST-SAMPLE-2-CHILD-2" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createSampleChildCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var childCriteria = new SearchCriteria();
		childCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "TEST-SAMPLE-2-CHILD-1"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(childCriteria));

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "TEST-SAMPLE-2" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createSampleContainerCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var containerCriteria = new SearchCriteria();
		containerCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "TEST-SAMPLE-1"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(containerCriteria));

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ "TEST-SAMPLE-1:TEST-SAMPLE-1-CONTAINED-1", "TEST-SAMPLE-1:TEST-SAMPLE-1-CONTAINED-2" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createSampleCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var sampleCriteria = new SearchCriteria();
		sampleCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "PLATE-1"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createSampleCriteria(sampleCriteria));

		facade.searchForDataSets(criteria, function(response) {
			assertObjectsCount(response.result, 11);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createExperimentCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var sampleCriteria = new SearchCriteria();
		sampleCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "EXP-2"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(sampleCriteria));

		facade.searchForSamples(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "SERIES-1" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createDataSetContainerCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetContainerCriteria = new SearchCriteria();
		dataSetContainerCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "20130412143121081-200"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createDataSetContainerCriteria(dataSetContainerCriteria));

		facade.searchForDataSets(criteria, function(response) {
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createDataSetParentCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetParentCriteria = new SearchCriteria();
		dataSetParentCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "20130412143121081-200"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(dataSetParentCriteria));

		facade.searchForDataSets(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "20130412153119864-385" ]);
			facade.close();
		});
	});
});

test("SearchSubCriteria.createDataSetChildCriteria()", function() {
	createFacadeAndLogin(function(facade) {
		var dataSetChildCriteria = new SearchCriteria();
		dataSetChildCriteria.addMatchClause(SearchCriteriaMatchClause.createAttributeMatch("CODE", "20130412153119864-385"));

		var criteria = new SearchCriteria();
		criteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(dataSetChildCriteria));

		facade.searchForDataSets(criteria, function(response) {
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "20130412143121081-200" ]);
			facade.close();
		});
	});
});
