/**
 * Tests for loading, setting, and getting SearchCriteria beans.
 */
define([ 'underscore', 'test/common' ], function(_, common) {
	return function() {
		QUnit.module("DTO tests");

		QUnit.test("EachSetCorrespondsGet", function(assert) {
			var c = new common(assert);

			var running = 0;

			_.each(c.getDtos(), function(proto) {
				var dto = new proto(++running); // just in case the constructor expects something
				_.chain(_.allKeys(dto))
					.filter(function(el) {
						return el.startsWith('set');
					})
					.each(function(setter) {
						var getter = setter.replace(/^set/, 'get');
						if (_.isFunction(dto[getter])) {

							var value = String(running); // string is more universal
							running++;

							dto[setter](value);
							var result = dto[getter]();
							if (result['@type'] && result['getValue']) {
								result = result['getValue']();
							}
							c.assertEqual(result, value, "Getter " + getter + " of type " + dto['@type'] + " returns same as was put by setter " + setter);
						} 
					});
			});

		});

		var assertAttributes = function(assert, criteria, expectedName, expextedType, expectedValueType, expectedValue) {
			var c = new common(assert);
			c.assertEqual(criteria.getFieldName(), expectedName, "Field  name");
			c.assertEqual(criteria.getFieldType(), expextedType, "Field  type");
			c.assertEqual(criteria.getFieldValue()["@type"], "as.dto.common.search." + expectedValueType, "Field  value type");
			c.assertEqual(criteria.getFieldValue().getValue(), expectedValue, "Field  value");
		}

		var checkStringFieldSearchCriteria = function(assert, criteriaClassName, name, type) {
			var done = assert.async();

			require([ 'as/dto/common/search/' + criteriaClassName ], function(Criteria) {
				var criteria = new Criteria(name, type);
				criteria.thatEquals("ABC");
				assertAttributes(assert, criteria, name, type, "StringEqualToValue", "ABC");
				criteria.thatStartsWith("A");
				assertAttributes(assert, criteria, name, type, "StringStartsWithValue", "A");
				criteria.thatEndsWith("C");
				assertAttributes(assert, criteria, name, type, "StringEndsWithValue", "C");
				criteria.thatContains("B");
				assertAttributes(assert, criteria, name, type, "StringContainsValue", "B");
				done();
			});
		}

		QUnit.test("AnyFieldSearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "AnyFieldSearchCriteria", "any", "ANY_FIELD");
		});

		QUnit.test("AnyPropertySearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "AnyPropertySearchCriteria", "any", "ANY_PROPERTY");
		});

		QUnit.test("CodeSearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "CodeSearchCriteria", "code", "ATTRIBUTE");
		});

		QUnit.test("PermIdSearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "PermIdSearchCriteria", "perm id", "ATTRIBUTE");
		});

		QUnit.test("StringPropertySearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "StringPropertySearchCriteria", "MY-PROPERTY", "PROPERTY");
		});

		QUnit.test("StringFieldSearchCriteria", function(assert) {
			checkStringFieldSearchCriteria(assert, "StringFieldSearchCriteria", "MY-FIELD", "MY-TYPE");
		});

		var checkNumberFieldSearchCriteria = function(assert, criteriaClassName, name, type) {
			var done = assert.async();

			require([ 'as/dto/common/search/' + criteriaClassName ], function(Criteria) {
				var criteria = new Criteria(name, type);
				criteria.thatEquals(42);
				assertAttributes(assert, criteria, name, type, "NumberEqualToValue", 42);
				done();
			});
		}

		QUnit.test("NumberFieldSearchCriteria", function(assert) {
			checkNumberFieldSearchCriteria(assert, "NumberFieldSearchCriteria", "MY-FIELD", "MY-TYPE");
		});

		QUnit.test("NumberPropertySearchCriteria", function(assert) {
			checkNumberFieldSearchCriteria(assert, "NumberPropertySearchCriteria", "MY-FIELD", "PROPERTY");
		});

		var checkDateFieldSearchCriteriaMethod = function(assert, criteria, methodName, basicClassName, methodDescription, name, type) {
			var c = new common(assert);
			criteria[methodName]("2105-02-25 21:23:24");
			assertAttributes(assert, criteria, name, type, "Date" + basicClassName, "2105-02-25 21:23:24");
			criteria[methodName]("2105-02-25");
			assertAttributes(assert, criteria, name, type, "Date" + basicClassName, "2105-02-25");
			var d = new Date("2105-02-25 17:18:19");
			criteria[methodName](d);
			assertAttributes(assert, criteria, name, type, "DateObject" + basicClassName, d);
			try {
				criteria[methodName]("abc");
				c.fail("ex");
			} catch (e) {
				c.assertEqual(e.message, "Date value: " + methodDescription + " to 'abc' does not match any of " + "the supported formats: YYYY-MM-DD,YYYY-MM-DD HH:mm,YYYY-MM-DD HH:mm:ss",
						"Error message");
			}
		}

		var checkDateFieldSearchCriteria = function(assert, criteriaClassName, name, type) {
			var done = assert.async();

			require([ 'as/dto/common/search/' + criteriaClassName ], function(Criteria) {
				var criteria = new Criteria(name, type);
				checkDateFieldSearchCriteriaMethod(assert, criteria, "thatEquals", "EqualToValue", "equal", name, type);
				checkDateFieldSearchCriteriaMethod(assert, criteria, "thatIsLaterThanOrEqualTo", "LaterThanOrEqualToValue", "later than or equal", name, type);
				checkDateFieldSearchCriteriaMethod(assert, criteria, "thatIsEarlierThanOrEqualTo", "EarlierThanOrEqualToValue", "earlier than or equal", name, type);
				checkDateFieldSearchCriteriaMethod(assert, criteria, "thatIsLaterThan", "LaterThanValue", "later than", name, type);
				checkDateFieldSearchCriteriaMethod(assert, criteria, "thatIsEarlierThan", "EarlierThanValue", "earlier than", name, type);
				done();
			});
		}

		QUnit.test("DateFieldSearchCriteria", function(assert) {
			checkDateFieldSearchCriteria(assert, "DateFieldSearchCriteria", "MY_FIELD", "MY_TYPE");
		});

		QUnit.test("DatePropertySearchCriteria", function(assert) {
			checkDateFieldSearchCriteria(assert, "DatePropertySearchCriteria", "MY_PROPERTY", "PROPERTY");
		});

		QUnit.test("ModificationDateSearchCriteria", function(assert) {
			checkDateFieldSearchCriteria(assert, "ModificationDateSearchCriteria", "modification_date", "ATTRIBUTE");
		});

		QUnit.test("RegistrationDateSearchCriteria", function(assert) {
			checkDateFieldSearchCriteria(assert, "RegistrationDateSearchCriteria", "registration_date", "ATTRIBUTE");
		});
	}
});
