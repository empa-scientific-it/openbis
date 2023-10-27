define(["jquery", "underscore", "openbis", "test/common", "test/dtos"], function ($, _, openbis, common, dtos) {
    var ignored = [
        "as.dto.common.create.CreateObjectsOperation",
        "as.dto.common.create.CreateObjectsOperationResult",
        "as.dto.common.delete.DeleteObjectsOperation",
        "as.dto.common.delete.DeleteObjectsOperationResult",
        "as.dto.common.delete.DeleteObjectsWithTrashOperationResult",
        "as.dto.common.delete.DeleteObjectsWithoutTrashOperationResult",
        "as.dto.common.entity.AbstractEntity",
        "as.dto.common.entity.AbstractEntityPropertyHolder",
        "as.dto.common.entity.AbstractEntityUpdate",
        "as.dto.common.entity.AbstractEntityCreation",
        "as.dto.common.fetchoptions.FetchOptions",
        "as.dto.common.fetchoptions.SortOptions",
        "as.dto.common.fetchoptions.AbstractEntityFetchOptions",
        "as.dto.common.get.GetObjectsOperation",
        "as.dto.common.get.GetObjectsOperationResult",
        "as.dto.common.id.ObjectIdentifier",
        "as.dto.common.search.AbstractCompositeSearchCriteria",
        "as.dto.common.search.AbstractDateObjectValue",
        "as.dto.common.search.AbstractDateValue",
        "as.dto.common.search.AbstractEntitySearchCriteria",
        "as.dto.common.search.AbstractFieldSearchCriteria",
        "as.dto.common.search.AbstractNumberValue",
        "as.dto.common.search.AbstractObjectSearchCriteria",
        "as.dto.common.search.AbstractSearchCriteria",
        "as.dto.common.search.AbstractStringValue",
        "as.dto.common.search.AbstractTimeZoneValue",
        "as.dto.common.search.AbstractValue",
        "as.dto.common.search.BooleanFieldSearchCriteria",
        "as.dto.common.search.CollectionFieldSearchCriteria",
        "as.dto.common.search.DateFieldSearchCriteria",
        "as.dto.common.search.EnumFieldSearchCriteria",
        "as.dto.common.search.NumberFieldSearchCriteria",
        "as.dto.common.search.SearchObjectsOperation",
        "as.dto.common.search.SearchObjectsOperationResult",
        "as.dto.common.search.StringFieldSearchCriteria",
        "as.dto.common.update.UpdateObjectsOperation",
        "as.dto.common.update.UpdateObjectsOperationResult",
        "as.dto.entitytype.search.AbstractEntityTypeSearchCriteria",
        "as.dto.plugin.evaluate.PluginEvaluationOptions",
        "as.dto.plugin.evaluate.PluginEvaluationResult",
        "as.dto.server.ServerInformation",
        "as.dto.service.execute.AbstractExecutionOptionsWithParameters",
    ]

    var instantiators = {
        "as.dto.attachment.id.AttachmentFileName": () => new dtos.AttachmentFileName("test"),
        "as.dto.query.id.QueryDatabaseName": () => new dtos.QueryDatabaseName("test"),
        "as.dto.query.id.QueryName": () => new dtos.QueryName("test"),
        "as.dto.space.id.SpacePermId": () => new dtos.SpacePermId("test"),
        "as.dto.tag.id.TagCode": () => new dtos.TagCode("test"),
        "as.dto.vocabulary.id.VocabularyTermPermId": () => new dtos.VocabularyTermPermId("test"),
    }

    var executeModule = function (moduleName, facade, dtos) {
        QUnit.module(moduleName)

        var testAction = function (c, fAction, fCheck) {
            c.start()

            c.login(facade)
                .then(function () {
                    c.ok("Login")
                    return fAction(facade)
                })
                .then(function (res) {
                    c.ok("Sent data. Checking results...")
                    return fCheck(res)
                })
                .then(function () {
                    c.finish()
                })
                .fail(function (error) {
                    c.fail(error.message)
                    c.finish()
                })
        }

        QUnit.test("dtosRoundtripTest()", function (assert) {
            var c = new common(assert, dtos)

            var id = new dtos.CustomASServiceCode("custom-service-a")
            var actionFacade

            var instantiate = function (proto) {
                var instantiator = instantiators[proto.prototype["@type"]]

                if (instantiator) {
                    return instantiator(proto)
                } else {
                    return new proto()
                }
            }

            var fAction = function (facade) {
                actionFacade = facade

                var dtosArray = Object.entries(dtos)
                    .map(function (dtoEntry) {
                        var name = dtoEntry[0]
                        var proto = dtoEntry[1]

                        if (!_.isString(name) || !name.match(/[A-Z]/)) {
                            return null
                        }

                        var type = proto && proto.prototype && proto.prototype["@type"]

                        if (type === null || type === undefined || ignored.includes(type)) {
                            return null
                        }

                        return proto
                    })
                    .filter(function (proto) {
                        return proto !== null
                    })

                return _.chain(dtosArray)
                    .map(function (proto) {
						try {
							return new dtos.CustomASServiceExecutionOptions()
								.withParameter("object", instantiate(proto));
						} catch (error) {
							c.fail(error.message);
							c.finish();
						}
                    })
                    .map(function (options) {
							try {
								return facade.executeCustomASService(id, options);
							} catch (error) {
								c.fail(error.message);
								c.finish();
							}
                    })
                    .value()
            }

            var fCheck = function (promises) {
                return $.when
                    .apply($, promises)
                    .then(function (here_we_get_unknown_number_of_resolved_dtos_so_foo) {
                        c.ok("Got results")

                        var loadedDtos = Array.prototype.slice.call(arguments)
                        var roundtrips = _.map(loadedDtos, function (dto) {
                            c.ok("======== Testing " + dto["@type"])
                            c.ok("Rountrip ok.")

                            var proto = eval("dtos." + dto["@type"])

                            if (proto) {
                                var subj = instantiate(proto)

                                _.chain(_.allKeys(dto))
                                    .filter(function (key) {
                                        return (
                                            !key.startsWith("@") &&
                                            !key.startsWith("freeze") &&
                                            !key.startsWith("negate") &&
                                            !key.startsWith("isNegated") &&
                                            !_.isFunction(dto[key])
                                        )
                                    })
                                    .each(function (key) {
                                        var val = dto[key]
                                        var isSetValue = false

                                        if (val != null && _.isFunction(val.getValue)) {
                                            val = val.getValue()
                                            isSetValue = true
                                        }

                                        if (val != null && !_.isFunction(val)) {
                                            if (isSetValue) {
                                                if (
                                                    _.isFunction(dto[key].setValue) &&
                                                    subj[key] &&
                                                    _.isFunction(subj[key].setValue)
                                                ) {
                                                    subj[key].setValue(val)
                                                    c.ok("FIELD: " + key + " = setValue >" + val + "<")
                                                } else {
                                                    c.ok("Skipping setValue field: " + key)
                                                }
                                            } else {
                                                var regularSetFn = _.find(_.functions(subj), function (fn) {
                                                    return fn.toLowerCase() === "set" + key.toLowerCase()
                                                })

                                                var otherSetFn = _.find(_.functions(subj), function (fn) {
                                                    return (
                                                        fn.toLowerCase() === key.toLowerCase() ||
                                                        fn.toLowerCase() === "with" + key.toLowerCase()
                                                    )
                                                })

                                                // prefer regularSetFn function over otherSetFn
                                                var setter = regularSetFn || otherSetFn

                                                c.ok("Setter: [set/with]" + key)

                                                if (setter) {
                                                    subj[setter](val)
                                                    c.ok("FIELD: " + key + " = >" + val + "<")
                                                } else {
                                                    c.ok("Skipping field " + key + " that has no setter.")
                                                }
                                            }
                                        } else {
                                            c.ok("Skipping field " + key + " as it's empty (i.e. complex).")
                                        }
                                    })

                                // let's send it back and see if it's acceptable
                                var options = new dtos.CustomASServiceExecutionOptions()
                                    .withParameter("object", subj)
                                    .withParameter("echo", "true")
                                return actionFacade.executeCustomASService(id, options).then(function (res) {
                                    // here dto is what was filled by java service, res is what we reconstructed based on it
                                    // deepEqual(actual, expected)
                                    c.shallowEqual(
                                        JSON.parse(JSON.stringify(res)),
                                        JSON.parse(JSON.stringify(dto)),
                                        "Checking whether reconstructed " +
                                            dto["@type"] +
                                            " from Java template has same fields as the one generated and initialized by java."
                                    )
                                    // assert.propEqual(JSON.parse(JSON.stringify(res)), JSON.parse(JSON.stringify(dto)), "Checking whether reconstructed " + dto['@type'] + " from Java template has same fields as the one generated and initialized by java.");
                                })
                            } else {
                                debugger
                                c.fail("Type " + dto["@type"] + " is unknown to the common.")
                            }
                        })
                        var applied = $.when.apply($, roundtrips)

                        return applied
                    })
                    .fail(function (error) {
                        c.fail(error.message)
                        c.finish()
                    })
            }

            testAction(c, fAction, fCheck)
        })
    }

    return function () {
        executeModule("DTO roundtrip test (RequireJS)", new openbis(), dtos)
        executeModule("DTO roundtrip test (module VAR)", new window.openbis.openbis(), window.openbis)
        executeModule("DTO roundtrip test (module ESM)", new window.openbisESM.openbis(), window.openbisESM)
    }
})
