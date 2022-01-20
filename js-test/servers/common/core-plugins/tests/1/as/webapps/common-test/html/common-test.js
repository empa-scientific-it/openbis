QUnit.jUnitReport = function(report) {
	$("#qunit-junit-report").text(report.xml);
	console.log(report.xml);
}

var createFacade = function(action, url, timeoutOrNull) {
	stop();

	var facade = new openbis(url);

	facade.close = function() {
		facade.logout(function() {
			facade.closed = true;
		});
	};

	action(facade);

	var timeout = timeoutOrNull ? timeoutOrNull : 30000;
	var checkInterval = 100;
	var intervalTotal = 0;

	var startWhenClosed = function() {
		if (facade.closed) {
			start();
		} else {
			intervalTotal += checkInterval;

			if (intervalTotal < timeout) {
				setTimeout(startWhenClosed, checkInterval);
			} else {
				start();
			}
		}
	};

	startWhenClosed();
}

var createFacadeAndLoginForUserAndPassword = function(user, password, action, url, timeoutOrNull) {
	createFacade(function(facade) {
		facade.login(user, password, function(response) {
			if (!response.error) {
				action(facade);
			}
		});
	}, url, timeoutOrNull);
}

var createSearchCriteriaForCodes = function(codes) {
	var clauses = [];

	$.each(codes, function(index, code) {
		clauses.push({
			"@type" : "AttributeMatchClause",
			attribute : "CODE",
			fieldType : "ATTRIBUTE",
			desiredValue : code
		});
	});

	var searchCriteria = {
		"@type" : "SearchCriteria",
		matchClauses : clauses,
		operator : "MATCH_ANY_CLAUSES"
	};

	return searchCriteria;
}

var assertArrays = function(array1, array2, msg) {
	deepEqual(array1.sort(), array2.sort(), msg);
}

var assertObjectsCount = function(objects, count) {
	equal(objects.length, count, 'Got ' + objects.length + ' object(s) instead of ' + count + '.');
}

var assertDate = function(millis, msg, year, month, day, hour, minute) {
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

	equal(actual, expected, msg);
}

var assertToday = function(millis, msg) {
	var today = new Date();
	assertDate(millis, msg, today.getUTCFullYear(), today.getUTCMonth() + 1, today.getUTCDate());
}

var accessProperty = function(object, propertyName) {
	var propertyNames = propertyName.split('.');
	for ( var pn in propertyNames) {
		object = object[propertyNames[pn]];
	}
	return object;
}

var assertObjectsWithValues = function(objects, propertyName, propertyValues) {
	var values = {};

	$.each(objects, function(index, object) {
		var value = accessProperty(object, propertyName);
		if (value in values == false) {
			values[value] = true;
		}
	});

    var expected = propertyValues.sort();
    var actual = Object.keys(values).sort();
	deepEqual(actual, expected, 'Objects have correct ' + propertyName + ' values. Expected: [' + expected + '] - Actual: [' + actual + ']')
}

var assertObjectsWithValuesFunction = function(objects, propertyName, propertyFunction, propertyValues) {
	var values = {};

	$.each(objects, function(index, object) {
		var value = propertyFunction(object);
		if (value in values == false) {
			values[value] = true;
		}
	});

	deepEqual(Object.keys(values).sort(), propertyValues.sort(), 'Objects have correct ' + propertyName + ' values')
}

var assertObjectsWithOrWithoutCollections = function(objects, accessor, checker) {
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
}

var assertObjectsWithCollections = function(objects, accessor) {
	assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
		ok(objects.some(function(object) {
			var value = accessor(object);
			return value && Object.keys(value).length > 0;
		}), 'Objects have non-empty collections accessed via: ' + accessor);
	});
}

var assertObjectsWithoutCollections = function(objects, accessor) {
	assertObjectsWithOrWithoutCollections(objects, accessor, function(objects, accessor) {
		ok(objects.every(function(object) {
			var value = accessor(object);
			return !value || Object.keys(value).length == 0;
		}), 'Objects have empty collections accessed via: ' + accessor);
	});
}

var assertObjectsWithCodes = function(objects, codes) {
	assertObjectsWithValues(objects, 'code', codes);
}

var assertObjectsWithProperties = function(objects) {
	assertObjectsWithCollections(objects, 'properties');
}

var assertObjectsWithoutProperties = function(objects) {
	assertObjectsWithoutCollections(objects, 'properties');
}

var assertObjectsWithParentCodes = function(objects) {
	assertObjectsWithCollections(objects, 'parentCodes');
}

var assertObjectsWithoutParentCodes = function(objects) {
	assertObjectsWithoutCollections(objects, 'parentCodes');
}

var assertObjectsWithChildrenCodes = function(objects) {
	assertObjectsWithCollections(objects, 'childrenCodes');
}

var assertObjectsWithoutChildrenCodes = function(objects) {
	assertObjectsWithoutCollections(objects, 'childrenCodes');
}