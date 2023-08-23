define(['jquery', 'underscore', 'openbis', 'test/common'], function($, _, openbis, common) {
	return function() {
		QUnit.module("DTO roundtrip test");

		var testAction = function(c, fAction, fCheck) {
			c.start();

			c.createFacadeAndLogin()
				.then(function(facade) {
					c.ok("Login");
					return fAction(facade);
				})
				.then(function(res) {
					c.ok("Sent data. Checking results...");
					return fCheck(res);
				})
				.then(function() {
					c.finish();
				})
				.fail(function(error) {
					c.fail(error.message);
					c.finish();
				});
		}

		QUnit.test("dtosRoundtripTest()", function(assert){
			var c = new common(assert);

			var id = new c.CustomASServiceCode("custom-service-a");
			var actionFacade;

			// those classes cannot be deserialized if the value is an empty string, must be a null
			var nullParamRegexes = [/number.*value/i, /.*Operation/, /.*OperationResult/];

			var instantiate = function(proto) {
				var initWithNull = _.any(nullParamRegexes, function (regex) {
							return proto.prototype["@type"].match(regex) !== null;
						});
				return new proto(initWithNull ? null : "");
			};

			var fAction = function(facade) {
				actionFacade = facade;

				return _.chain(
					c.getDtos()
				)
					.map(function(proto) {
						try {
							return new c.CustomASServiceExecutionOptions()
								.withParameter("object", instantiate(proto));
						} catch (error) {
							c.fail(error.message);
							c.finish();
						}
					})
					.map(function(options) {
							try {
								return facade.executeCustomASService(id, options);
							} catch (error) {
								c.fail(error.message);
								c.finish();
							}
					})
					.value();
			}

			var fCheck = function(promises) {
				return $.when.apply($, promises).then(function(here_we_get_unknown_number_of_resolved_dtos_so_foo){
					c.ok("Got results");

					var dtos = Array.prototype.slice.call(arguments);
					var roundtrips = _.map(dtos, function(dto){

						c.ok("======== Testing " + dto['@type']);
						c.ok('Rountrip ok.');

						var proto = require(dto['@type'].replace(/\./g, '/'));
						if (proto) {
							var subj = instantiate(proto);

							_.chain(_.allKeys(dto))
							.filter(function(key) {
								return !key.startsWith("@") && !key.startsWith("freeze") && !key.startsWith("negate") && !key.startsWith("isNegated") && !_.isFunction(dto[key]);
							})
							.each(function(key){
								var val = dto[key];
								var isSetValue = false;

								if (val != null && _.isFunction(val.getValue)) {
									val = val.getValue();
									isSetValue = true;
								}

								if (val != null && !_.isFunction(val)) {
									if (isSetValue) {
										if (_.isFunction(dto[key].setValue) && subj[key] && _.isFunction(subj[key].setValue)) {
											subj[key].setValue(val);
											c.ok("FIELD: " + key + " = setValue >" + val + "<")
										} else {
											c.ok("Skipping setValue field: " + key);
										}
									} else {
										var regularSetFn = _.find(_.functions(subj), function(fn) {
											return fn.toLowerCase() === "set" + key.toLowerCase();
										});

										var otherSetFn = _.find(_.functions(subj), function(fn) {
											return (fn.toLowerCase() === key.toLowerCase())
												|| (fn.toLowerCase() === "with" + key.toLowerCase());
										});

										// prefer regularSetFn function over otherSetFn
										var setter = regularSetFn || otherSetFn;

										c.ok("Setter: [set/with]" + key);

										if (setter) {
											subj[setter](val);
											c.ok("FIELD: " + key + " = >" + val + "<")
										} else {
											c.ok("Skipping field " + key + " that has no setter.");
										}
									}
								} else {
									c.ok("Skipping field " + key + " as it's empty (i.e. complex).");
								}
							});


							// let's send it back and see if it's acceptable
							var options = new c.CustomASServiceExecutionOptions().withParameter("object", subj).withParameter("echo", "true");
							return actionFacade.executeCustomASService(id, options)
								.then(function(res) {
									// here dto is what was filled by java service, res is what we reconstructed based on it
									// deepEqual(actual, expected)
									c.shallowEqual(JSON.parse(JSON.stringify(res)), JSON.parse(JSON.stringify(dto)), "Checking whether reconstructed " + dto['@type'] + " from Java template has same fields as the one generated and initialized by java.");
									// assert.propEqual(JSON.parse(JSON.stringify(res)), JSON.parse(JSON.stringify(dto)), "Checking whether reconstructed " + dto['@type'] + " from Java template has same fields as the one generated and initialized by java.");
								});

						} else {
							debugger;
							c.fail('Type ' + dto['@type'] + ' is unknown to the common.');

						}
					});
					var applied = $.when.apply($, roundtrips);

					return applied;

				}).fail(function(error) {
					c.fail(error.message);
					c.finish();
				});
			}

			testAction(c, fAction, fCheck);

		});
	}
});