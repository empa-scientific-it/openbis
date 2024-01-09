define(
		[ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ],
		function($, _, openbis, openbisExecuteOperations, common, dtos) {
			var executeModule = function(moduleName, facade, dtos) {
				QUnit.module(moduleName);

				var testCreate = function(c, fCreate, fFind, fCheck) {
					c.start();

					c.login(facade).then(function() {
						return fCreate(facade).then(function(permIds) {
							c.assertTrue(permIds != null && permIds.length == 1, "Entity was created");
							return fFind(facade, permIds[0]).then(function(entity) {
								c.assertNotNull(entity, "Entity can be found");
								var token = fCheck(entity, facade);
								if (token) {
									token.then(function() {
										c.finish()
									});
								} else {
									c.finish();
								}
							});
						});
					}).fail(function(error) {
						c.fail(error.message);
						c.finish();
					});
				}

				QUnit.test("createPermIdStrings", function(assert) {
					var c = new common(assert, dtos);
					c.start();
					c.login(facade).then(function() {
						return facade.createPermIdStrings(7).then(function(permIds) {
							c.assertEqual(permIds.length, 7, "Number of perm ids");
							c.finish();
						});
					}).fail(function(error) {
						c.fail(error.message);
						c.finish();
					});
				});

				QUnit.test("createCodes", function(assert) {
					var c = new common(assert, dtos);
					c.start();
					c.login(facade).then(function() {
						return facade.createCodes("ABC-", dtos.EntityKind.SAMPLE, 7).then(function(codes) {
							c.assertEqual(codes.length, 7, "Number of codes");
							c.finish();
						});
					}).fail(function(error) {
						c.fail(error.message);
						c.finish();
					});
				});

				QUnit.test("createSpaces()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("SPACE");

					var fCreate = function(facade) {
						var creation = new dtos.SpaceCreation();
						creation.setCode(code);
						creation.setDescription("test description");
						return facade.createSpaces([ creation ]);
					}

					var fCheck = function(space) {
						c.assertEqual(space.getCode(), code, "Code");
						c.assertEqual(space.getDescription(), "test description", "Description");
					}

					testCreate(c, fCreate, c.findSpace, fCheck);
				});

				QUnit.test("createProjects()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("PROJECT");

					var fCreate = function(facade) {
						var projectCreation = new dtos.ProjectCreation();
						projectCreation.setSpaceId(new dtos.SpacePermId("TEST"));
						projectCreation.setCode(code);
						projectCreation.setDescription("JS test project");
						attachmentCreation = new dtos.AttachmentCreation();
						attachmentCreation.setFileName("test_file");
						attachmentCreation.setTitle("test_title");
						attachmentCreation.setDescription("test_description");
						attachmentCreation.setContent(btoa("hello world!"));
						projectCreation.setAttachments([ attachmentCreation ]);
						return facade.createProjects([ projectCreation ]);
					}

					var fCheck = function(project) {
						c.assertEqual(project.getCode(), code, "Project code");
						c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
						c.assertEqual(project.getDescription(), "JS test project", "Description");
						var attachments = project.getAttachments();
						c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
						c.assertEqual(attachments[0].title, "test_title", "Attachment title");
						c.assertEqual(attachments[0].description, "test_description", "Attachment description");
						c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
						c.assertEqual(attachments.length, 1, "Number of attachments");
					}

					testCreate(c, fCreate, c.findProject, fCheck);
				});

				QUnit.test("createExperiments()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("EXPERIMENT");

					var fCreate = function(facade) {
						var experimentCreation = new dtos.ExperimentCreation();
						experimentCreation.setTypeId(new dtos.EntityTypePermId("HT_SEQUENCING"));
						experimentCreation.setCode(code);
						experimentCreation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
						experimentCreation.setTagIds([ new dtos.TagCode("CREATE_JSON_TAG") ]);
						attachmentCreation = new dtos.AttachmentCreation();
						attachmentCreation.setFileName("test_file");
						attachmentCreation.setTitle("test_title");
						attachmentCreation.setDescription("test_description");
						attachmentCreation.setContent(btoa("hello world!"));
						experimentCreation.setAttachments([ attachmentCreation ]);
						experimentCreation.setProperty("EXPERIMENT_DESIGN", "SEQUENCE_ENRICHMENT");
						experimentCreation.setMetaData({"meta_key":"meta_value"})
						return facade.createExperiments([ experimentCreation ]);
					}

					var fCheck = function(experiment) {
						c.assertEqual(experiment.getCode(), code, "Experiment code");
						c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
						c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
						c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
						c.assertEqual(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
						var tags = experiment.getTags();
						c.assertEqual(tags[0].code, 'CREATE_JSON_TAG', "tags");
						c.assertEqual(tags.length, 1, "Number of tags");
						var attachments = experiment.getAttachments();
						c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
						c.assertEqual(attachments[0].title, "test_title", "Attachment title");
						c.assertEqual(attachments[0].description, "test_description", "Attachment description");
						c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
						c.assertEqual(attachments.length, 1, "Number of attachments");
						var properties = experiment.getProperties();
						c.assertEqual(properties["EXPERIMENT_DESIGN"], "SEQUENCE_ENRICHMENT", "Property EXPERIMENT_DESIGN");
						c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
						var metaData = experiment.getMetaData();
						c.assertEqual(metaData["meta_key"], "meta_value", "metadata meta_key");
					}

					testCreate(c, fCreate, c.findExperiment, fCheck);
				});

				QUnit.test("createExperiment() with properties of type SAMPLE and DATE", function(assert) {
					var c = new common(assert, dtos);
					var propertyTypeCodeSample = c.generateId("PROPERTY_TYPE");
					var propertyTypeCodeDate = c.generateId("PROPERTY_TYPE");
					var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
					var code = c.generateId("EXPERIMENT");
					
					var fCreate = function(facade) {
						var propertyTypeCreation1 = new dtos.PropertyTypeCreation();
						propertyTypeCreation1.setCode(propertyTypeCodeSample);
						propertyTypeCreation1.setDescription("hello");
						propertyTypeCreation1.setDataType(dtos.DataType.SAMPLE);
						propertyTypeCreation1.setLabel("Test Property Type");
						var propertyTypeCreation2 = new dtos.PropertyTypeCreation();
						propertyTypeCreation2.setCode(propertyTypeCodeDate);
						propertyTypeCreation2.setDescription("hello");
						propertyTypeCreation2.setDataType(dtos.DataType.DATE);
						propertyTypeCreation2.setLabel("Test Property Type");
						return facade.createPropertyTypes([ propertyTypeCreation1, propertyTypeCreation2 ]).then(function(results) {
							var assignmentCreation1 = new dtos.PropertyAssignmentCreation();
							assignmentCreation1.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCodeSample));
							var assignmentCreation2 = new dtos.PropertyAssignmentCreation();
							assignmentCreation2.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCodeDate));
							var experimentTypeCreation = new dtos.ExperimentTypeCreation();
							experimentTypeCreation.setCode(experimentTypeCode);
							experimentTypeCreation.setPropertyAssignments([ assignmentCreation1, assignmentCreation2 ]);
							return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
								var creation = new dtos.ExperimentCreation();
								creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
								creation.setCode(code);
								creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
								creation.setProperty(propertyTypeCodeSample, "20130412140147735-20");
								creation.setProperty(propertyTypeCodeDate, "2013-04-12");
								return facade.createExperiments([ creation ]);
							});
						});
					}
					
					var fCheck = function(experiment) {
						c.assertEqual(experiment.getCode(), code, "Experiment code");
						c.assertEqual(experiment.getType().getCode(), experimentTypeCode, "Type code");
						c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
						c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
						c.assertEqual(experiment.getProperties()[propertyTypeCodeSample], "20130412140147735-20", "Sample property id");
						c.assertEqual(experiment.getProperties()[propertyTypeCodeDate], "2013-04-12", "Date property");
						c.assertEqual(experiment.getSampleProperties()[propertyTypeCodeSample][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-1", "Sample property");
					}
					
					testCreate(c, fCreate, c.findExperiment, fCheck);
				});

				QUnit.test("createExperiment() with unique property", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCodeUnique = c.generateId("PROPERTY_TYPE");
                    var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
                    var code = c.generateId("EXPERIMENT");
                    var code2 = c.generateId("EXPERIMENT");

                    var fCreate = function(facade) {
                        var propertyTypeCreation1 = new dtos.PropertyTypeCreation();
                        propertyTypeCreation1.setCode(propertyTypeCodeUnique);
                        propertyTypeCreation1.setDescription("unique text property");
                        propertyTypeCreation1.setDataType(dtos.DataType.VARCHAR);
                        propertyTypeCreation1.setLabel("Test Unique Property Type");

                        return facade.createPropertyTypes([ propertyTypeCreation1 ]).then(function(results) {
                            var assignmentCreation1 = new dtos.PropertyAssignmentCreation();
                            assignmentCreation1.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCodeUnique));
                            assignmentCreation1.setUnique(true);
                            var experimentTypeCreation = new dtos.ExperimentTypeCreation();
                            experimentTypeCreation.setCode(experimentTypeCode);
                            experimentTypeCreation.setPropertyAssignments([ assignmentCreation1 ]);
                            return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
                                var creation = new dtos.ExperimentCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
                                creation.setCode(code);
                                creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
                                creation.setProperty(propertyTypeCodeUnique, "my_unique_value");
                                return facade.createExperiments([ creation ]).then(function(results) {
                                     var creation2 = new dtos.ExperimentCreation();
                                     creation2.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
                                     creation2.setCode(code2);
                                     creation2.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
                                     creation2.setProperty(propertyTypeCodeUnique, "my_unique_value");
                                     return facade.createExperiments([ creation2 ]);
                                 });;
                            });

                        });
                    }

                    c.start();
                    c.login(facade).then(function() {
                        return fCreate(facade)
                        .fail(function(error) {
                            c.assertEqual("Insert/Update of experiment failed because property contains value that is not unique! (value:  my_unique_value) (Context: [])", error.message, "Uniqueness error message");
                            c.finish();
                            });
                    });

                });

				QUnit.test("createExperiment() with multi-value property of type SAMPLE", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCodeSample = c.generateId("PROPERTY_TYPE");

                    var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
                    var code = c.generateId("EXPERIMENT");

                    var fCreate = function(facade) {
                        var propertyTypeCreation1 = new dtos.PropertyTypeCreation();
                        propertyTypeCreation1.setCode(propertyTypeCodeSample);
                        propertyTypeCreation1.setDescription("hello");
                        propertyTypeCreation1.setDataType(dtos.DataType.SAMPLE);
                        propertyTypeCreation1.setLabel("Test Property Type");
                        propertyTypeCreation1.setMultiValue(true);
                        return facade.createPropertyTypes([ propertyTypeCreation1 ]).then(function(results) {
                            var assignmentCreation1 = new dtos.PropertyAssignmentCreation();
                            assignmentCreation1.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCodeSample));
                            var experimentTypeCreation = new dtos.ExperimentTypeCreation();
                            experimentTypeCreation.setCode(experimentTypeCode);
                            experimentTypeCreation.setPropertyAssignments([ assignmentCreation1 ]);
                            return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
                                var creation = new dtos.ExperimentCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
                                creation.setCode(code);
                                creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
                                creation.setProperty(propertyTypeCodeSample, ["20130412140147735-20", "20130424134657597-433"]);
                                return facade.createExperiments([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(experiment) {
                        c.assertEqual(experiment.getCode(), code, "Experiment code");
                        c.assertEqual(experiment.getType().getCode(), experimentTypeCode, "Type code");
                        c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
                        c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");

                        var identifiers = experiment.getSampleProperties()[propertyTypeCodeSample].map(x => x.getIdentifier().getIdentifier()).sort();
                        c.assertEqual(identifiers.toString(), ['/PLATONIC/SCREENING-EXAMPLES/PLATE-1', '/TEST/TEST-SAMPLE-1'].toString(), "Sample properties");
                        c.assertEqual(experiment.getProperties()[propertyTypeCodeSample].sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Sample property ids");
                    }

                    testCreate(c, fCreate, c.findExperiment, fCheck);
                });

                QUnit.test("createExperiment() with multi-value property of type CONTROLLEDVOCABULARY", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCodeVocab = c.generateId("PROPERTY_TYPE");

                    var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
                    var code = c.generateId("EXPERIMENT");

                    var fCreate = function(facade) {
                        var propertyTypeCreation1 = new dtos.PropertyTypeCreation();
                        propertyTypeCreation1.setCode(propertyTypeCodeVocab);
                        propertyTypeCreation1.setDescription("hello");
                        propertyTypeCreation1.setDataType(dtos.DataType.CONTROLLEDVOCABULARY);
                        propertyTypeCreation1.setLabel("Test Property Type");
                        propertyTypeCreation1.setMultiValue(true);
                        propertyTypeCreation1.setVocabularyId(new dtos.VocabularyPermId("ANTIBODY.HOST"));
                        return facade.createPropertyTypes([ propertyTypeCreation1 ]).then(function(results) {
                            var assignmentCreation1 = new dtos.PropertyAssignmentCreation();
                            assignmentCreation1.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCodeVocab));
                            var experimentTypeCreation = new dtos.ExperimentTypeCreation();
                            experimentTypeCreation.setCode(experimentTypeCode);
                            experimentTypeCreation.setPropertyAssignments([ assignmentCreation1 ]);
                            return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
                                var creation = new dtos.ExperimentCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
                                creation.setCode(code);
                                creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
                                creation.setProperty(propertyTypeCodeVocab, ["RAT", "MOUSE"]);
                                return facade.createExperiments([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(experiment) {
                        c.assertEqual(experiment.getCode(), code, "Experiment code");
                        c.assertEqual(experiment.getType().getCode(), experimentTypeCode, "Type code");
                        c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
                        c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
                        c.assertEqual(experiment.getProperties()[propertyTypeCodeVocab].sort().toString(), ["MOUSE", "RAT"].toString(), "Vocabulary property ids");
                    }

                    testCreate(c, fCreate, c.findExperiment, fCheck);
                });
				
				QUnit.test("createExperimentTypes()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("EXPERIMENT_TYPE");

					var fCreate = function(facade) {
						var assignmentCreation = new dtos.PropertyAssignmentCreation();
						assignmentCreation.setSection("test section");
						assignmentCreation.setOrdinal(10);
						assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("DESCRIPTION"));
						assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
						assignmentCreation.setMandatory(true);
						assignmentCreation.setInitialValueForExistingEntities("initial value");
						assignmentCreation.setShowInEditView(true);
						assignmentCreation.setShowRawValueInForms(true);

						var creation = new dtos.ExperimentTypeCreation();
						creation.setCode(code);
						creation.setDescription("a new description");
						creation.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
						creation.setPropertyAssignments([ assignmentCreation ]);
						creation.setMetaData({"type_key":"type_value"});

						return facade.createExperimentTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getPermId().toString(), code + " (EXPERIMENT)", "Full type perm id");
						c.assertEqual(type.getDescription(), "a new description", "Description");

						c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

						var assignment = type.getPropertyAssignments()[0];

						c.assertEqual(assignment.getSection(), "test section", "Assignment section");
						c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
						c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
						c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
						c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
						c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
						c.assertEqual(assignment.getPlugin().getName(), "Diff_time", "Assignment Plugin");

						var metaData = type.getMetaData();
						c.assertEqual(metaData["type_key"], "type_value", "Metadata");
					}

					testCreate(c, fCreate, c.findExperimentType, fCheck);
				});

				QUnit.test("createSamples()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("SAMPLE");

					var fCreate = function(facade) {
						var creation = new dtos.SampleCreation();
						creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
						creation.setCode(code);
						creation.setSpaceId(new dtos.SpacePermId("TEST"));
						creation.setTagIds([ new dtos.TagCode("CREATE_JSON_TAG") ]);
						creation.setMetaData({"sample_key":"sample_value"});
						return facade.createSamples([ creation ]);
					}

					var fCheck = function(sample) {
						c.assertEqual(sample.getCode(), code, "Sample code");
						c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
						c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
						c.assertEqual(sample.getTags()[0].getCode(), "CREATE_JSON_TAG", "Tag code");

						var metaData = sample.getMetaData();
                        c.assertEqual(metaData["sample_key"], "sample_value", "Metadata");
					}

					testCreate(c, fCreate, c.findSample, fCheck);
				});

				QUnit.test("createSamples() with annotated child", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("SAMPLE");
					var childId = new dtos.SampleIdentifier("/TEST/TEST-SAMPLE-1");

					var fCreate = function(facade) {
						var creation = new dtos.SampleCreation();
						creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
						creation.setCode(code);
						creation.setSpaceId(new dtos.SpacePermId("TEST"));
						creation.setChildIds([ childId ]);
						creation.relationship(childId)
								.addParentAnnotation("type", "mother").addChildAnnotation("type", "daughter");
						return facade.createSamples([ creation ]);
					}

					var fCheck = function(sample) {
						c.assertEqual(sample.getCode(), code, "Sample code");
						c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
						c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
						var child = sample.getChildren()[0];
						c.assertEqual(child.getIdentifier().getIdentifier(), childId.getIdentifier(), "Child");
						var relationship = sample.getChildRelationship(child.getPermId());
						c.assertEqualDictionary(relationship.getChildAnnotations(), {"type": "daughter"}, "Child annotations");
						c.assertEqualDictionary(relationship.getParentAnnotations(), {"type": "mother"}, "Parent annotations");
					}

					testCreate(c, fCreate, c.findSample, fCheck);
				});

				QUnit.test("createSamples() with annotated parent", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("SAMPLE");
					var parentId = new dtos.SampleIdentifier("/TEST/TEST-SAMPLE-1");

					var fCreate = function(facade) {
						var creation = new dtos.SampleCreation();
						creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
						creation.setCode(code);
						creation.setSpaceId(new dtos.SpacePermId("TEST"));
						creation.setParentIds([ parentId ]);
						creation.relationship(parentId)
							.addParentAnnotation("type", "mother").addChildAnnotation("type", "daughter");
						return facade.createSamples([ creation ]);
					}
					
					var fCheck = function(sample) {
						c.assertEqual(sample.getCode(), code, "Sample code");
						c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
						c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
						var parent = sample.getParents()[0];
						c.assertEqual(parent.getIdentifier().getIdentifier(), parentId.getIdentifier(), "Parent");
						var relationship = sample.getParentRelationship(parent.getPermId());
						c.assertEqualDictionary(relationship.getChildAnnotations(), {"type": "daughter"}, "Child annotations");
						c.assertEqualDictionary(relationship.getParentAnnotations(), {"type": "mother"}, "Parent annotations");
					}
					
					testCreate(c, fCreate, c.findSample, fCheck);
				});

				QUnit.test("createSamples() with property of type SAMPLE", function(assert) {
					var c = new common(assert, dtos);
					var propertyTypeCode = c.generateId("PROPERTY_TYPE");
					var sampleTypeCode = c.generateId("SAMPLE_TYPE");
					var code = c.generateId("SAMPLE");
					
					var fCreate = function(facade) {
						var propertyTypeCreation = new dtos.PropertyTypeCreation();
						propertyTypeCreation.setCode(propertyTypeCode);
						propertyTypeCreation.setDescription("hello");
						propertyTypeCreation.setDataType(dtos.DataType.SAMPLE);
						propertyTypeCreation.setLabel("Test Property Type");
						return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
							var assignmentCreation = new dtos.PropertyAssignmentCreation();
							assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
							var sampleTypeCreation = new dtos.SampleTypeCreation();
							sampleTypeCreation.setCode(sampleTypeCode);
							sampleTypeCreation.setPropertyAssignments([ assignmentCreation ]);
							return facade.createSampleTypes([ sampleTypeCreation ]).then(function(results) {
								var creation = new dtos.SampleCreation();
								creation.setTypeId(new dtos.EntityTypePermId(sampleTypeCode));
								creation.setCode(code);
								creation.setSpaceId(new dtos.SpacePermId("TEST"));
								creation.setProperty(propertyTypeCode, "20130412140147735-20");
								return facade.createSamples([ creation ]);
							});
						});
					}
					
					var fCheck = function(sample) {
						c.assertEqual(sample.getCode(), code, "Sample code");
						c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
						c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
						c.assertEqual(sample.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-1", "Sample property");
						c.assertEqual(sample.getProperties()[propertyTypeCode], "20130412140147735-20", "Sample property id");
					}
					
					testCreate(c, fCreate, c.findSample, fCheck);
				});


				QUnit.test("createSamples() with a unique property", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var sampleTypeCode = c.generateId("SAMPLE_TYPE");
                    var code = c.generateId("SAMPLE");
                    var code2 = c.generateId("SAMPLE");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.VARCHAR);
                        propertyTypeCreation.setLabel("Test Property Type");
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            assignmentCreation.setUnique(true);
                            var sampleTypeCreation = new dtos.SampleTypeCreation();
                            sampleTypeCreation.setCode(sampleTypeCode);
                            sampleTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createSampleTypes([ sampleTypeCreation ]).then(function(results) {
                                var creation = new dtos.SampleCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(sampleTypeCode));
                                creation.setCode(code);
                                creation.setSpaceId(new dtos.SpacePermId("TEST"));
                                creation.setProperty(propertyTypeCode, "my_unique_value");
                                return facade.createSamples([ creation ]).then(function(results) {
                                     var creation2 = new dtos.SampleCreation();
                                     creation2.setTypeId(new dtos.EntityTypePermId(sampleTypeCode));
                                     creation2.setCode(code2);
                                     creation2.setSpaceId(new dtos.SpacePermId("TEST"));
                                     creation2.setProperty(propertyTypeCode, "my_unique_value");
                                     return facade.createSamples([ creation2 ]);
                                 });;
                            });
                        });
                    }

                   c.start();
                   c.login(facade).then(function() {
                       return fCreate(facade)
                       .fail(function(error) {
                           c.assertEqual("Insert/Update of object failed because property contains value that is not unique! (value:  my_unique_value) (Context: [])", error.message, "Uniqueness error message");
                           c.finish();
                           });
                   });
                });


				QUnit.test("createSamples() with multi-value property of type SAMPLE", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var sampleTypeCode = c.generateId("SAMPLE_TYPE");
                    var code = c.generateId("SAMPLE");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.SAMPLE);
                        propertyTypeCreation.setLabel("Test Property Type");
                        propertyTypeCreation.setMultiValue(true);
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            var sampleTypeCreation = new dtos.SampleTypeCreation();
                            sampleTypeCreation.setCode(sampleTypeCode);
                            sampleTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createSampleTypes([ sampleTypeCreation ]).then(function(results) {
                                var creation = new dtos.SampleCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(sampleTypeCode));
                                creation.setCode(code);
                                creation.setSpaceId(new dtos.SpacePermId("TEST"));
                                creation.setProperty(propertyTypeCode, ["20130412140147735-20", "20130424134657597-433"]);
                                return facade.createSamples([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(sample) {
                        c.assertEqual(sample.getCode(), code, "Sample code");
                        c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
                        c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
                        var identifiers = sample.getSampleProperties()[propertyTypeCode].map(x => x.getIdentifier().getIdentifier()).sort();
                        c.assertEqual(identifiers.toString(), ['/PLATONIC/SCREENING-EXAMPLES/PLATE-1', '/TEST/TEST-SAMPLE-1'].toString(), "Sample properties");
                        c.assertEqual(sample.getProperties()[propertyTypeCode].sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Sample property ids");
                    }
                    testCreate(c, fCreate, c.findSample, fCheck);
                });

                QUnit.test("createSamples() with multi-value property of type CONTROLLEDVOCABULARY", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var sampleTypeCode = c.generateId("SAMPLE_TYPE");
                    var code = c.generateId("SAMPLE");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.CONTROLLEDVOCABULARY);
                        propertyTypeCreation.setLabel("Test Property Type");
                        propertyTypeCreation.setMultiValue(true);
                        propertyTypeCreation.setVocabularyId(new dtos.VocabularyPermId("ANTIBODY.HOST"));
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            var sampleTypeCreation = new dtos.SampleTypeCreation();
                            sampleTypeCreation.setCode(sampleTypeCode);
                            sampleTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createSampleTypes([ sampleTypeCreation ]).then(function(results) {
                                var creation = new dtos.SampleCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(sampleTypeCode));
                                creation.setCode(code);
                                creation.setSpaceId(new dtos.SpacePermId("TEST"));
                                creation.setProperty(propertyTypeCode, ["RAT", "MOUSE"]);
                                return facade.createSamples([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(sample) {
                        c.assertEqual(sample.getCode(), code, "Sample code");
                        c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
                        c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
                        c.assertEqual(sample.getProperties()[propertyTypeCode].sort().toString(), ["MOUSE", "RAT"].toString(), "Vocabulary property ids");
                    }
                    testCreate(c, fCreate, c.findSample, fCheck);
                });

				QUnit.test("createSampleTypes()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("SAMPLE_TYPE");

					var fCreate = function(facade) {
						var assignmentCreation = new dtos.PropertyAssignmentCreation();
						assignmentCreation.setSection("test section");
						assignmentCreation.setOrdinal(10);
						assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("DESCRIPTION"));
						assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
						assignmentCreation.setMandatory(true);
						assignmentCreation.setInitialValueForExistingEntities("initial value");
						assignmentCreation.setShowInEditView(true);
						assignmentCreation.setShowRawValueInForms(true);

						var creation = new dtos.SampleTypeCreation();
						creation.setCode(code);
						creation.setDescription("a new description");
						creation.setAutoGeneratedCode(true);
						creation.setSubcodeUnique(true);
						creation.setGeneratedCodePrefix("TEST_PREFIX");
						creation.setListable(true);
						creation.setShowContainer(true);
						creation.setShowParents(true);
						creation.setShowParentMetadata(true);
						creation.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
						creation.setPropertyAssignments([ assignmentCreation ]);
						creation.setMetaData({"sample_type_key":"type_value"});

						return facade.createSampleTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getDescription(), "a new description", "Description");
						c.assertEqual(type.isAutoGeneratedCode(), true, "AutoGeneratedCode");
						c.assertEqual(type.isSubcodeUnique(), true, "SubcodeUnique");
						c.assertEqual(type.getGeneratedCodePrefix(), "TEST_PREFIX", "GeneratedCodePrefix");
						c.assertEqual(type.isListable(), true, "Listable");
						c.assertEqual(type.isShowContainer(), true, "ShowContainer");
						c.assertEqual(type.isShowParents(), true, "ShowParents");
						c.assertEqual(type.isShowParentMetadata(), true, "ShowParentMetadata");

						c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

						var assignment = type.getPropertyAssignments()[0];

						c.assertEqual(assignment.getSection(), "test section", "Assignment section");
						c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
						c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
						c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
						c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
						c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
						c.assertEqual(assignment.getPlugin().getName(), "Diff_time", "Assignment Plugin");

						var metaData = type.getMetaData();
                        c.assertEqual(metaData["sample_type_key"], "type_value", "Metadata");
					}

					testCreate(c, fCreate, c.findSampleType, fCheck);
				});

				QUnit.test("createDataSets() link data set via DSS", function(assert) {
					var c = new common(assert, dtos);
					var emdsId = null;

					var fCreate = function(facade) {
						return c.createExperiment(facade).then(function(experimentPermId) {
							return c.createFileExternalDms(facade).then(function(emdsPermId) {
								emdsId = emdsPermId;
								var creation = new dtos.FullDataSetCreation();
								var dataSet = new dtos.DataSetCreation();
								dataSet.setCode(c.generateId("DATA_SET"));
								dataSet.setTypeId(new dtos.EntityTypePermId("LINK_TYPE"));
								dataSet.setExperimentId(experimentPermId);
								dataSet.setDataStoreId(new dtos.DataStorePermId("DSS1"));
								var linkedData = new dtos.LinkedDataCreation();
								var cc = new dtos.ContentCopyCreation();
								cc.setExternalDmsId(emdsPermId);
								cc.setPath("my/path");
								linkedData.setContentCopies([ cc ]);
								dataSet.setLinkedData(linkedData);
								creation.setMetadataCreation(dataSet);
								var f1 = new dtos.DataSetFileCreation();
								f1.setDirectory(true);
								f1.setPath("root/folder");
								var f2 = new dtos.DataSetFileCreation();
								f2.setDirectory(false);
								f2.setPath("root/my-file-in.txt");
								f2.setFileLength(42);
								f2.setChecksumCRC32(123456);
								creation.setFileMetadata([ f1, f2 ]);
								return facade.getDataStoreFacade("DSS1", "DSS2").createDataSets([ creation ]);
							});
						});
					}

					var fCheck = function(dataSet, facade) {
						c.assertEqual(dataSet.getType().getCode(), "LINK_TYPE", "Data set type");
						var contentCopies = dataSet.getLinkedData().getContentCopies();
						c.assertEqual(contentCopies.length, 1, "Number of content copies");
						var contentCopy = contentCopies[0];
						c.assertEqual(contentCopy.getExternalDms().getPermId().toString(), emdsId.toString(), "External data management system");
						c.assertEqual(contentCopy.getPath(), "/my/path", "Content copy path");
						var dfd = $.Deferred()
						var dataSetCode = dataSet.getCode();
						c.waitUntilIndexed(facade, dataSetCode, 10000).then(function() {
							var criteria = new dtos.DataSetFileSearchCriteria();
							criteria.withDataSet().withCode().thatEquals(dataSet.getCode());
							facade.getDataStoreFacade("DSS1").searchFiles(criteria, c.createDataSetFileFetchOptions()).then(function(result) {
								var files = result.getObjects();
								c.assertEqual(files.length, 4, "Number of files");
								files.sort(function(f1, f2) {
									return f1.getPath().localeCompare(f2.getPath());
								});
								var expectedPaths = [ "", "root", "root/folder", "root/my-file-in.txt" ];
								var expectedDirectoryFlags = [ true, true, true, false ];
								var expectedSizes = [ 42, 42, 0, 42 ];
								var expectedChecksums = [ 0, 0, 0, 123456 ];
								for (i = 0; i < files.length; i++) {
									var file = files[i];
									var postfix = " of file " + (i + 1);
									c.assertEqual(file.getDataSetPermId().toString(), dataSetCode, "Data set" + postfix);
									c.assertEqual(file.getDataStore().getCode(), "DSS1", "Data store" + postfix);
									c.assertEqual(file.getPath(), expectedPaths[i], "Path" + postfix);
									c.assertEqual(file.isDirectory(), expectedDirectoryFlags[i], "Directory flag" + postfix);
									c.assertEqual(file.getChecksumCRC32(), expectedChecksums[i], "Checksum" + postfix);
								}
								dfd.resolve();
							});
						});
						return dfd.promise();
					}

					testCreate(c, fCreate, c.findDataSet, fCheck);
				});

				QUnit.test("createDataSet() with property of type SAMPLE", function(assert) {
					var c = new common(assert, dtos);
					var propertyTypeCode = c.generateId("PROPERTY_TYPE");
					var dataSetTypeCode = c.generateId("DATA_SET_TYPE");
					var code = c.generateId("DATA_SET");
					
					var fCreate = function(facade) {
						var propertyTypeCreation = new dtos.PropertyTypeCreation();
						propertyTypeCreation.setCode(propertyTypeCode);
						propertyTypeCreation.setDescription("hello");
						propertyTypeCreation.setDataType(dtos.DataType.SAMPLE);
						propertyTypeCreation.setLabel("Test Property Type");
						return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
							var assignmentCreation = new dtos.PropertyAssignmentCreation();
							assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
							var dataSetTypeCreation = new dtos.DataSetTypeCreation();
							dataSetTypeCreation.setCode(dataSetTypeCode);
							dataSetTypeCreation.setPropertyAssignments([ assignmentCreation ]);
							return facade.createDataSetTypes([ dataSetTypeCreation ]).then(function(results) {
								var creation = new dtos.DataSetCreation();
								creation.setTypeId(new dtos.EntityTypePermId(dataSetTypeCode));
								creation.setCode(code);
								creation.setDataSetKind(dtos.DataSetKind.CONTAINER);
								creation.setDataStoreId(new dtos.DataStorePermId("DSS1"));
								creation.setExperimentId(new dtos.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
								creation.setProperty(propertyTypeCode, "20130412140147735-20");
								creation.setMetaData({"dataset_key":"dataset_value"});
								return facade.createDataSets([ creation ]);
							});
						});
					}
					
					var fCheck = function(dataSet) {
						c.assertEqual(dataSet.getCode(), code, "Data set code");
						c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");
						c.assertEqual(dataSet.getProperties()[propertyTypeCode], "20130412140147735-20", "Sample property id");
						c.assertEqual(dataSet.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-1", "Sample property");

						var metaData = dataSet.getMetaData();
						c.assertEqual(metaData["dataset_key"], "dataset_value", "Metadata");

					}
					
					testCreate(c, fCreate, c.findDataSet, fCheck);
				});


				QUnit.test("createDataSet() with unique property", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var dataSetTypeCode = c.generateId("DATA_SET_TYPE");
                    var code = c.generateId("DATA_SET");
                    var code2 = c.generateId("DATA_SET");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.VARCHAR);
                        propertyTypeCreation.setLabel("Test Property Type");
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            assignmentCreation.setUnique(true);
                            var dataSetTypeCreation = new dtos.DataSetTypeCreation();
                            dataSetTypeCreation.setCode(dataSetTypeCode);
                            dataSetTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createDataSetTypes([ dataSetTypeCreation ]).then(function(results) {
                                var creation = new dtos.DataSetCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(dataSetTypeCode));
                                creation.setCode(code);
                                creation.setDataSetKind(dtos.DataSetKind.CONTAINER);
                                creation.setDataStoreId(new dtos.DataStorePermId("DSS1"));
                                creation.setExperimentId(new dtos.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
                                creation.setProperty(propertyTypeCode, "my_unique_value");
                                return facade.createDataSets([ creation ]).then(function(results) {
                                    var creation2 = new dtos.DataSetCreation();
                                    creation2.setTypeId(new dtos.EntityTypePermId(dataSetTypeCode));
                                    creation2.setCode(code2);
                                    creation2.setDataSetKind(dtos.DataSetKind.CONTAINER);
                                    creation2.setDataStoreId(new dtos.DataStorePermId("DSS1"));
                                    creation2.setExperimentId(new dtos.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
                                    creation2.setProperty(propertyTypeCode, "my_unique_value");
                                    return facade.createDataSets([ creation2 ]);
                                });
                            });
                        });
                    }

                    c.start();
                    c.login(facade).then(function() {
                        return fCreate(facade)
                        .fail(function(error) {
                            c.assertEqual("Insert/Update of dataset failed because property contains value that is not unique! (value:  my_unique_value) (Context: [])", error.message, "Uniqueness error message");
                            c.finish();
                            });
                    });

                });

				QUnit.test("createDataSet() with multi-value property of type SAMPLE", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var dataSetTypeCode = c.generateId("DATA_SET_TYPE");
                    var code = c.generateId("DATA_SET");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.SAMPLE);
                        propertyTypeCreation.setLabel("Test Property Type");
                        propertyTypeCreation.setMultiValue(true);
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            var dataSetTypeCreation = new dtos.DataSetTypeCreation();
                            dataSetTypeCreation.setCode(dataSetTypeCode);
                            dataSetTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createDataSetTypes([ dataSetTypeCreation ]).then(function(results) {
                                var creation = new dtos.DataSetCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(dataSetTypeCode));
                                creation.setCode(code);
                                creation.setDataSetKind(dtos.DataSetKind.CONTAINER);
                                creation.setDataStoreId(new dtos.DataStorePermId("DSS1"));
                                creation.setExperimentId(new dtos.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
                                creation.setProperty(propertyTypeCode, ["20130412140147735-20", "20130424134657597-433"]);
                                creation.setMetaData({"dataset_key":"dataset_value"});
                                return facade.createDataSets([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(dataSet) {
                        c.assertEqual(dataSet.getCode(), code, "Data set code");
                        c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");

                        var identifiers = dataSet.getSampleProperties()[propertyTypeCode].map(x => x.getIdentifier().getIdentifier()).sort();
                        c.assertEqual(identifiers.toString(), ['/PLATONIC/SCREENING-EXAMPLES/PLATE-1', '/TEST/TEST-SAMPLE-1'].toString(), "Sample properties");
                        c.assertEqual(dataSet.getProperties()[propertyTypeCode].sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Sample property ids");

                        var metaData = dataSet.getMetaData();
                        c.assertEqual(metaData["dataset_key"], "dataset_value", "Metadata");

                    }

                    testCreate(c, fCreate, c.findDataSet, fCheck);
                });

                QUnit.test("createDataSet() with multi-value property of type CONTROLLEDVOCABULARY", function(assert) {
                    var c = new common(assert, dtos);
                    var propertyTypeCode = c.generateId("PROPERTY_TYPE");
                    var dataSetTypeCode = c.generateId("DATA_SET_TYPE");
                    var code = c.generateId("DATA_SET");

                    var fCreate = function(facade) {
                        var propertyTypeCreation = new dtos.PropertyTypeCreation();
                        propertyTypeCreation.setCode(propertyTypeCode);
                        propertyTypeCreation.setDescription("hello");
                        propertyTypeCreation.setDataType(dtos.DataType.CONTROLLEDVOCABULARY);
                        propertyTypeCreation.setLabel("Test Property Type");
                        propertyTypeCreation.setMultiValue(true);
                        propertyTypeCreation.setVocabularyId(new dtos.VocabularyPermId("ANTIBODY.HOST"));
                        return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
                            var assignmentCreation = new dtos.PropertyAssignmentCreation();
                            assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
                            var dataSetTypeCreation = new dtos.DataSetTypeCreation();
                            dataSetTypeCreation.setCode(dataSetTypeCode);
                            dataSetTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                            return facade.createDataSetTypes([ dataSetTypeCreation ]).then(function(results) {
                                var creation = new dtos.DataSetCreation();
                                creation.setTypeId(new dtos.EntityTypePermId(dataSetTypeCode));
                                creation.setCode(code);
                                creation.setDataSetKind(dtos.DataSetKind.CONTAINER);
                                creation.setDataStoreId(new dtos.DataStorePermId("DSS1"));
                                creation.setExperimentId(new dtos.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
                                creation.setProperty(propertyTypeCode, ["RAT", "MOUSE"]);
                                creation.setMetaData({"dataset_key":"dataset_value"});
                                return facade.createDataSets([ creation ]);
                            });
                        });
                    }

                    var fCheck = function(dataSet) {
                        c.assertEqual(dataSet.getCode(), code, "Data set code");
                        c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");
                        c.assertEqual(dataSet.getProperties()[propertyTypeCode].sort().toString(), ["MOUSE", "RAT"].toString(), "Vocabulary property ids");
                        var metaData = dataSet.getMetaData();
                        c.assertEqual(metaData["dataset_key"], "dataset_value", "Metadata");
                    }

                    testCreate(c, fCreate, c.findDataSet, fCheck);
                });

				QUnit.test("createDataSetTypes()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("DATA_SET_TYPE");

					var fCreate = function(facade) {
						var assignmentCreation = new dtos.PropertyAssignmentCreation();
						assignmentCreation.setSection("test section");
						assignmentCreation.setOrdinal(10);
						assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("DESCRIPTION"));
						assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
						assignmentCreation.setMandatory(true);
						assignmentCreation.setInitialValueForExistingEntities("initial value");
						assignmentCreation.setShowInEditView(true);
						assignmentCreation.setShowRawValueInForms(true);

						var creation = new dtos.DataSetTypeCreation();
						creation.setCode(code);
						creation.setDescription("a new description");
						creation.setMainDataSetPattern(".*\\.jpg");
						creation.setMainDataSetPath("original/images/");
						creation.setDisallowDeletion(true);
						creation.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
						creation.setPropertyAssignments([ assignmentCreation ]);
						creation.setMetaData({"dataset_type_key":"dataset_type_value"});

						return facade.createDataSetTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getDescription(), "a new description", "Description");
						c.assertEqual(type.getMainDataSetPattern(), ".*\\.jpg", "Main data set pattern");
						c.assertEqual(type.getMainDataSetPath(), "original/images/", "Main data set path");
						c.assertEqual(type.isDisallowDeletion(), true, "Disallow deletion");

						c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

						var assignment = type.getPropertyAssignments()[0];

						c.assertEqual(assignment.getSection(), "test section", "Assignment section");
						c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
						c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
						c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
						c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
						c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
						c.assertEqual(assignment.getPlugin().getName(), "Diff_time", "Assignment Plugin");

						var metaData = type.getMetaData();
						c.assertEqual(metaData["dataset_type_key"], "dataset_type_value", "Metadata");
					}

					testCreate(c, fCreate, c.findDataSetType, fCheck);
				});

				QUnit.test("createMaterials()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("MATERIAL");

					var fCreate = function(facade) {
						var materialCreation = new dtos.MaterialCreation();
						materialCreation.setTypeId(new dtos.EntityTypePermId("COMPOUND"));
						materialCreation.setCode(code);
						materialCreation.setProperty("DESCRIPTION", "Metal");
						return facade.createMaterials([ materialCreation ]);
					}

					var fCheck = function(material) {
						c.assertEqual(material.getCode(), code, "Material code");
						c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
						var properties = material.getProperties();
						c.assertEqual(properties["DESCRIPTION"], "Metal", "Property DESCRIPTION");
					}

					testCreate(c, fCreate, c.findMaterial, fCheck);
				});

				QUnit.test("createMaterialTypes()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("MATERIAL_TYPE");

					var fCreate = function(facade) {
						var assignmentCreation = new dtos.PropertyAssignmentCreation();
						assignmentCreation.setSection("test section");
						assignmentCreation.setOrdinal(10);
						assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("DESCRIPTION"));
						assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
						assignmentCreation.setMandatory(true);
						assignmentCreation.setInitialValueForExistingEntities("initial value");
						assignmentCreation.setShowInEditView(true);
						assignmentCreation.setShowRawValueInForms(true);

						var creation = new dtos.MaterialTypeCreation();
						creation.setCode(code);
						creation.setDescription("a new description");
						creation.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
						creation.setPropertyAssignments([ assignmentCreation ]);

						return facade.createMaterialTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getDescription(), "a new description", "Description");

						c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

						var assignment = type.getPropertyAssignments()[0];

						c.assertEqual(assignment.getSection(), "test section", "Assignment section");
						c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
						c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
						c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
						c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
						c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
						c.assertEqual(assignment.getPlugin().getName(), "Diff_time", "Assignment Plugin");
					}

					testCreate(c, fCreate, c.findMaterialType, fCheck);
				});

				QUnit.test("createPropertyTypes()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("PROPERTY_TYPE");
					var metaData = {"greetings" : "hello test"};

					var fCreate = function(facade) {
						var creation = new dtos.PropertyTypeCreation();
						creation.setCode(code);
						creation.setDescription("hello");
						creation.setDataType(dtos.DataType.INTEGER);
						creation.setLabel("Test Property Type");
						creation.setMetaData(metaData);
						return facade.createPropertyTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getLabel(), "Test Property Type", "Label");
						c.assertEqual(type.getDescription(), "hello", "Description");
						c.assertEqual(type.getDataType(), dtos.DataType.INTEGER, "Data type");
						c.assertEqual(type.getMetaData().toString(), metaData, "Meta data");

					}

					testCreate(c, fCreate, c.findPropertyType, fCheck);
				});

				QUnit.test("createPropertyType() with data type SAMPLE", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("PROPERTY_TYPE");
					var metaData = {"greetings" : "hello test"};

					var fCreate = function(facade) {
						var creation = new dtos.PropertyTypeCreation();
						creation.setCode(code);
						creation.setDescription("hello");
						creation.setDataType(dtos.DataType.SAMPLE);
						creation.setLabel("Test Property Type");
						creation.setMetaData(metaData);
						creation.setSampleTypeId(new dtos.EntityTypePermId("UNKNOWN", "SAMPLE"));
						return facade.createPropertyTypes([ creation ]);
					}

					var fCheck = function(type) {
						c.assertEqual(type.getCode(), code, "Type code");
						c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
						c.assertEqual(type.getLabel(), "Test Property Type", "Label");
						c.assertEqual(type.getDescription(), "hello", "Description");
						c.assertEqual(type.getDataType(), dtos.DataType.SAMPLE, "Data type");
						c.assertEqual(type.getMetaData().toString(), metaData, "Meta data");
						c.assertEqual(type.getSampleType().toString(), "UNKNOWN", "Sample type");
					}

					testCreate(c, fCreate, c.findPropertyType, fCheck);
				});

				QUnit.test("createPlugins()", function(assert) {
					var c = new common(assert, dtos);
					var name = c.generateId("PLUGIN");

					var fCreate = function(facade) {
						var creation = new dtos.PluginCreation();
						creation.setName(name);
						creation.setDescription("hello");
						creation.setPluginType(dtos.PluginType.ENTITY_VALIDATION);
						creation.setScript("def a():\n  pass");
						return facade.createPlugins([ creation ]);
					}

					var fCheck = function(plugin) {
						c.assertEqual(plugin.getName(), name, "Name");
						c.assertEqual(plugin.getPermId().getPermId(), name, "Perm id");
						c.assertEqual(plugin.getDescription(), "hello", "Description");
						c.assertEqual(plugin.getPluginKind(), dtos.PluginKind.JYTHON, "Plugin kind");
						c.assertEqual(plugin.getPluginType(), dtos.PluginType.ENTITY_VALIDATION, "Plugin type");
						c.assertEqual(plugin.getScript(), "def a():\n  pass", "Script");
						c.assertEqual(plugin.isAvailable(), true, "Available?");
					}

					testCreate(c, fCreate, c.findPlugin, fCheck);
				});

				QUnit.test("createVocabularies()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("VOCABULARY");

					var fCreate = function(facade) {
						var vocabularyCreation = new dtos.VocabularyCreation();
						vocabularyCreation.setCode(code);
						vocabularyCreation.setDescription("test description");
						vocabularyCreation.setManagedInternally(false);
						vocabularyCreation.setChosenFromList(true);
						vocabularyCreation.setUrlTemplate("https://www.ethz.ch");
						var termCreation = new dtos.VocabularyTermCreation();
						termCreation.setCode("alpha");
						vocabularyCreation.setTerms([ termCreation ]);
						return facade.createVocabularies([ vocabularyCreation ]);
					}

					var fCheck = function(vocabulary) {
						c.assertEqual(vocabulary.getCode(), code, "Code");
						c.assertEqual(vocabulary.getDescription(), "test description", "Description");
						c.assertEqual(vocabulary.isManagedInternally(), false, "Managed internally");
						c.assertEqual(vocabulary.isChosenFromList(), true, "Chosen from list");
						c.assertEqual(vocabulary.getUrlTemplate(), "https://www.ethz.ch", "URL template");
					}

					testCreate(c, fCreate, c.findVocabulary, fCheck);
				});

				QUnit.test("createVocabularyTerms()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("VOCABULARY_TERM");

					var fCreate = function(facade) {
						var termCreation = new dtos.VocabularyTermCreation();
						termCreation.setVocabularyId(new dtos.VocabularyPermId("TEST-VOCABULARY"));
						termCreation.setCode(code);
						termCreation.setLabel("test label");
						termCreation.setDescription("test description");
						termCreation.setOfficial(true);
						termCreation.setPreviousTermId(new dtos.VocabularyTermPermId("TEST-TERM-1", "TEST-VOCABULARY"))
						return facade.createVocabularyTerms([ termCreation ]);
					}

					var fCheck = function(term) {
						c.assertEqual(term.getCode(), code, "Term code");
						c.assertEqual(term.getVocabulary().getCode(), "TEST-VOCABULARY", "Term vocabulary code");
						c.assertEqual(term.getLabel(), "test label", "Term label");
						c.assertEqual(term.getDescription(), "test description", "Term description");
						c.assertEqual(term.isOfficial(), true, "Term official");
						c.assertEqual(term.getOrdinal(), 2, "Term ordinal");
					}

					testCreate(c, fCreate, c.findVocabularyTerm, fCheck);
				});

				QUnit.test("createExternalDataManagementSystem()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("EDMS");

					var fCreate = function(facade) {
						var edmsCreation = new dtos.ExternalDmsCreation();
						edmsCreation.setCode(code);
						edmsCreation.setLabel("Test EDMS");
						edmsCreation.setAddressType(dtos.ExternalDmsAddressType.FILE_SYSTEM);
						edmsCreation.setAddress("host:my/path")
						return facade.createExternalDms([ edmsCreation ]);
					}

					var fCheck = function(edms) {
						c.assertEqual(edms.getCode(), code, "EDMS code");
						c.assertEqual(edms.getLabel(), "Test EDMS", "EDMS label");
						c.assertEqual(edms.getAddress(), "host:my/path", "EDMS address");
						c.assertEqual(edms.getUrlTemplate(), "host:my/path", "EDMS URL template");
						c.assertEqual(edms.getAddressType(), "FILE_SYSTEM", "EDMS address type");
						c.assertEqual(edms.isOpenbis(), false, "EDMS is openBIS");
					}

					testCreate(c, fCreate, c.findExternalDms, fCheck);
				});

				QUnit.test("createTags()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("TAG");
					var description = "Description of " + code;

					var fCreate = function(facade) {
						var tagCreation = new dtos.TagCreation();
						tagCreation.setCode(code);
						tagCreation.setDescription(description);
						return facade.createTags([ tagCreation ]);
					}

					var fCheck = function(tag) {
						c.assertEqual(tag.getCode(), code, "Tag code");
						c.assertEqual(tag.getDescription(), description, "Tag description");
					}

					testCreate(c, fCreate, c.findTag, fCheck);
				});

				QUnit.test("createAuthorizationGroups()", function(assert) {
					var c = new common(assert, dtos);
					var code = c.generateId("AUTHORIZATION_GROUP");
					var description = "Description of " + code;

					var fCreate = function(facade) {
						var creation = new dtos.AuthorizationGroupCreation();
						creation.setCode(code);
						creation.setDescription(description);
						creation.setUserIds([ new dtos.PersonPermId("power_user") ]);
						return facade.createAuthorizationGroups([ creation ]);
					}

					var fCheck = function(group) {
						c.assertEqual(group.getCode(), code, "Code");
						c.assertEqual(group.getDescription(), description, "Description");
						var users = $.map(group.getUsers(), function(user) {
							return user.getUserId();
						});
						users.sort();
						c.assertEqual(users.toString(), "power_user", "Users");
					}

					testCreate(c, fCreate, c.findAuthorizationGroup, fCheck);
				});

				QUnit.test("createRoleAssignments() for space user", function(assert) {
					var c = new common(assert, dtos);

					var fCreate = function(facade) {
						return c.createSpace(facade).then(function(spaceId) {
							var creation = new dtos.RoleAssignmentCreation();
							creation.setRole(dtos.Role.POWER_USER);
							creation.setUserId(new dtos.PersonPermId("power_user"));
							creation.setSpaceId(spaceId);
							return facade.createRoleAssignments([ creation ]);
						});
					}

					var fCheck = function(roleAssignment) {
						c.assertEqual(roleAssignment.getUser().getUserId(), "power_user", "User");
						c.assertEqual(roleAssignment.getRole(), dtos.Role.POWER_USER, "Role");
						c.assertEqual(roleAssignment.getRoleLevel(), dtos.RoleLevel.SPACE, "Role level");
						c.assertEqual(roleAssignment.getRegistrator().getUserId(), "openbis_test_js", "Registrator");
					}

					testCreate(c, fCreate, c.findRoleAssignment, fCheck);
				});

				QUnit.test("createPersons()", function(assert) {
					var c = new common(assert, dtos);
					var userId = c.generateId("user");

					var fCreate = function(facade) {
						var personCreation = new dtos.PersonCreation();
						personCreation.setUserId(userId);
						personCreation.setSpaceId(new dtos.SpacePermId("TEST"))
						return facade.createPersons([ personCreation ]);
					}

					var fCheck = function(person) {
						c.assertEqual(person.getUserId(), userId, "User id");
						c.assertEqual(person.getRegistrator().getUserId(), "openbis_test_js", "Registrator");
						c.assertEqual(person.isActive(), true, "User active");
						c.assertEqual(person.getSpace().getCode(), "TEST", "Home space");
					}

					testCreate(c, fCreate, c.findPerson, fCheck);
				});

				var createSemanticAnnotationCreation = function(c) {
					var creation = new dtos.SemanticAnnotationCreation();
					creation.setPredicateOntologyId("jsPredicateOntologyId");
					creation.setPredicateOntologyVersion("jsPredicateOntologyVersion");
					creation.setPredicateAccessionId("jsPredicateAccessionId");
					creation.setDescriptorOntologyId("jsDescriptorOntologyId");
					creation.setDescriptorOntologyVersion("jsDescriptorOntologyVersion");
					creation.setDescriptorAccessionId("jsDescriptorAccessionId");
					return creation;
				}

				var checkCreatedSemanticAnnotation = function(c, annotation) {
					c.assertEqual(annotation.getPredicateOntologyId(), "jsPredicateOntologyId", "Predicate ontology id");
					c.assertEqual(annotation.getPredicateOntologyVersion(), "jsPredicateOntologyVersion", "Predicate ontology version");
					c.assertEqual(annotation.getPredicateAccessionId(), "jsPredicateAccessionId", "Predicate accession id");
					c.assertEqual(annotation.getDescriptorOntologyId(), "jsDescriptorOntologyId", "Descriptor ontology id");
					c.assertEqual(annotation.getDescriptorOntologyVersion(), "jsDescriptorOntologyVersion", "Descriptor ontology version");
					c.assertEqual(annotation.getDescriptorAccessionId(), "jsDescriptorAccessionId", "Descriptor accession id");
				}

				QUnit.test("createSemanticAnnotations() with entity type id", function(assert) {
					var c = new common(assert, dtos);

					var fCreate = function(facade) {
						var creation = createSemanticAnnotationCreation(c);
						creation.setEntityTypeId(new dtos.EntityTypePermId("UNKNOWN", "SAMPLE"));
						return facade.createSemanticAnnotations([ creation ]);
					}

					var fCheck = function(annotation) {
						checkCreatedSemanticAnnotation(c, annotation);
						c.assertEqual(annotation.getEntityType().getCode(), "UNKNOWN", "Entity type code");
					}

					testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
				});

				QUnit.test("createSemanticAnnotations() with property type id", function(assert) {
					var c = new common(assert, dtos);

					var fCreate = function(facade) {
						var creation = createSemanticAnnotationCreation(c);
						creation.setPropertyTypeId(new dtos.PropertyTypePermId("CONCENTRATION"));
						return facade.createSemanticAnnotations([ creation ]);
					}

					var fCheck = function(annotation) {
						checkCreatedSemanticAnnotation(c, annotation);
						c.assertEqual(annotation.getPropertyType().getCode(), "CONCENTRATION", "Property type code");
					}

					testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
				});

				QUnit.test("createSemanticAnnotations() with property assignment id", function(assert) {
					var c = new common(assert, dtos);

					var fCreate = function(facade) {
						var creation = createSemanticAnnotationCreation(c);
						creation.setPropertyAssignmentId(new dtos.PropertyAssignmentPermId(new dtos.EntityTypePermId("ILLUMINA_FLOW_CELL", "SAMPLE"), new dtos.PropertyTypePermId("RUNNINGTIME")));
						return facade.createSemanticAnnotations([ creation ]);
					}

					var fCheck = function(annotation) {
						checkCreatedSemanticAnnotation(c, annotation);
						c.assertEqual(annotation.getPropertyAssignment().getEntityType().getCode(), "ILLUMINA_FLOW_CELL", "Entity type code");
						c.assertEqual(annotation.getPropertyAssignment().getEntityType().getPermId().getEntityKind(), "SAMPLE", "Entity type kind");
						c.assertEqual(annotation.getPropertyAssignment().getPropertyType().getCode(), "RUNNINGTIME", "Property type code");
					}

					testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
				});

				QUnit
						.test(
								"createUploadedDataSet()",
								function(assert) {
									var c = new common(assert, dtos);

									var fCreate = function(facade) {

										// unfortunately old Firefox that is
										// used together with our
										// Selenium tests does not allow to use
										// FormData class which
										// would make constructing form data
										// much easier

										var formData = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file\"; filename=\"filePath/file1.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\ncontent1\r\n"
												+ "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file\"; filename=\"filePath/file2.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\ncontent2\r\n"
												+ "------WebKitFormBoundary7MA4YWxkTrZu0gW--";

										var dataStoreFacade = facade.getDataStoreFacade("DSS1");

										return dataStoreFacade.createDataSetUpload("UNKNOWN").then(function(upload) {
											return $.ajax({
												url : upload.getUrl("testFolder", false),
												type : "POST",
												processData : false,
												contentType : "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW",
												data : formData
											}).then(function() {
												return c.createExperiment(facade).then(function(experimentPermId) {
													var creation = new dtos.UploadedDataSetCreation();
													creation.setUploadId(upload.getId());
													creation.setTypeId(new dtos.EntityTypePermId(upload.getDataSetType()));
													creation.setExperimentId(experimentPermId);
													creation.setProperty("DESCRIPTION", "test description");
													creation.setParentIds([ new dtos.DataSetPermId("20130424111751432-431") ]);
													return dataStoreFacade.createUploadedDataSet(creation).then(function(permId) {
														return [ permId ];
													})
												});
											});
										});
									}

									var fCheck = function(dataSet, facade) {
										return c.waitUntilIndexed(facade, dataSet.getCode(), 10000).then(function() {
											var dataStoreFacade = facade.getDataStoreFacade("DSS1");

											var criteria = new dtos.DataSetFileSearchCriteria();
											criteria.withDataSet().withCode().thatEquals(dataSet.getCode());

											return dataStoreFacade.searchFiles(criteria, c.createDataSetFileFetchOptions()).then(function(result) {
												var files = result.getObjects();
												c.assertEqual(files.length, 6, "Number of files");
												c.assertEqual(files[0].path, "", "Path 0");
												c.assertEqual(files[1].path, "original", "Path 1");
												c.assertEqual(files[2].path, "original/testFolder", "Path 2");
												c.assertEqual(files[3].path, "original/testFolder/filePath", "Path 3");
												c.assertEqual(files[4].path, "original/testFolder/filePath/file1.txt", "Path 4");
												c.assertEqual(files[5].path, "original/testFolder/filePath/file2.txt", "Path 5");

												c.assertEqual(dataSet.getType().getCode(), "UNKNOWN", "Type code");
												c.assertEqual(dataSet.getProperty("DESCRIPTION"), "test description", "'DESCRIPTION' property value");
												c.assertEqual(dataSet.getParents().length, 1, "Number of parents");
												c.assertEqual(dataSet.getParents()[0].getCode(), "20130424111751432-431", "Parent code");
											});
										});
									}

									testCreate(c, fCreate, c.findDataSet, fCheck);
								});

				QUnit.test("createQueries()", function(assert) {
					var c = new common(assert, dtos);

					var queryCreation = new dtos.QueryCreation();
					queryCreation.setName(c.generateId("query"));
					queryCreation.setDescription("test description");
					queryCreation.setDatabaseId(new dtos.QueryDatabaseName("openbisDB"));
					queryCreation.setQueryType(dtos.QueryType.EXPERIMENT);
					queryCreation.setEntityTypeCodePattern("test pattern");
					queryCreation.setSql("select code from spaces");
					queryCreation.setPublic(true);

					var fCreate = function(facade) {
						return facade.createQueries([ queryCreation ]);
					}

					var fCheck = function(query) {
						c.assertNotNull(query.getPermId(), "Perm Id");
						c.assertEqual(query.getName(), queryCreation.getName(), "Name");
						c.assertEqual(query.getDescription(), queryCreation.getDescription(), "Description");
						c.assertEqual(query.getDatabaseId().getName(), queryCreation.getDatabaseId().getName(), "Database id");
						c.assertEqual(query.getDatabaseLabel(), "openBIS meta data", "Database label");
						c.assertEqual(query.getQueryType(), queryCreation.getQueryType(), "Query type");
						c.assertEqual(query.getEntityTypeCodePattern(), queryCreation.getEntityTypeCodePattern(), "Entity type code pattern");
						c.assertEqual(query.getSql(), queryCreation.getSql(), "Sql");
						c.assertEqual(query.isPublic(), queryCreation.isPublic(), "Is public");
						c.assertNotNull(query.getRegistrator().getUserId(), "Registrator user id");
						c.assertToday(query.getRegistrationDate(), "Registration date");
						c.assertToday(query.getModificationDate(), "Modification date");
					}

					testCreate(c, fCreate, c.findQuery, fCheck);
				});

				QUnit.test("createPersonalAccessTokens()", function(assert) {
					var c = new common(assert, dtos);
					var now = new Date();

					var patCreation = new dtos.PersonalAccessTokenCreation();
					patCreation.setSessionName(c.generateId("pat"));
					patCreation.setValidFromDate(now.getTime());
					patCreation.setValidToDate(new Date(now.getTime() + 24 * 3600 * 1000).getTime());

					var fCreate = function(facade) {
						return facade.createPersonalAccessTokens([ patCreation ]);
					}

					var fCheck = function(pat) {
						c.assertNotNull(pat.getPermId(), "Perm Id");
						c.assertNotNull(pat.getHash(), "Hash");
						c.assertEqual(pat.getSessionName(), patCreation.getSessionName(), "Session name");
						c.assertEqual(pat.getValidFromDate(), patCreation.getValidFromDate(), "Valid from date");
						c.assertEqual(pat.getValidToDate(), patCreation.getValidToDate(), "Valid to date");
						c.assertNotNull(pat.getOwner().getUserId(), "Owner user id");
						c.assertNotNull(pat.getRegistrator().getUserId(), "Registrator user id");
						c.assertNotNull(pat.getModifier().getUserId(), "Modifier user id");
						c.assertToday(pat.getRegistrationDate(), "Registration date");
						c.assertToday(pat.getModificationDate(), "Modification date");
						c.assertNull(pat.getAccessDate(), "Access date");
					}

					testCreate(c, fCreate, c.findPersonalAccessToken, fCheck);
				});

			}

			return function() {
				executeModule("Create tests (RequireJS)", new openbis(), dtos);
				executeModule("Create tests (RequireJS - executeOperations)", new openbisExecuteOperations(new openbis(), dtos), dtos);
				executeModule("Create tests (module VAR)", new window.openbis.openbis(), window.openbis);
				executeModule("Create tests (module VAR - executeOperations)", new openbisExecuteOperations(new window.openbis.openbis(), window.openbis), window.openbis);
				executeModule("Create tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
				executeModule("Create tests (module ESM - executeOperations)", new openbisExecuteOperations(new window.openbisESM.openbis(), window.openbisESM), window.openbisESM);
			}
		});
