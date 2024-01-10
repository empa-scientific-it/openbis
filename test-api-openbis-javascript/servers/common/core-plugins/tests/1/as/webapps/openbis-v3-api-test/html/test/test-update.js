define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/dtos' ], function($, _, openbis, openbisExecuteOperations, common, dtos) {
	var executeModule = function(moduleName, facade, dtos) {
		QUnit.module(moduleName);

		var testUpdate = function(c, fCreate, fUpdate, fFind, fCheck, fCheckError) {
			c.start();

			var ctx = {
				facade : null,
				permIds : null
			};
			c.login(facade).then(function() {
				ctx.facade = facade;
				return fCreate(facade)
			}).then(function(permIds) {
				ctx.permIds = permIds;
				c.assertTrue(permIds != null && permIds.length == 1, "Entity was created");
				return fFind(ctx.facade, permIds[0])
			}).then(function(entity) {
				c.assertNotNull(entity, "Entity can be found");
				return fUpdate(ctx.facade, ctx.permIds[0])
			}).then(function() {
				c.ok("Entity was updated");
				return fFind(ctx.facade, ctx.permIds[0])
			}).then(function(entity) {
				if (fCheck) {
					fCheck(entity);
				}
				c.finish();
			}).fail(function(error) {
				if (fCheckError) {
					fCheckError(error.message, ctx.permIds[0]);
				} else {
					c.fail(error.message);
				}
				c.finish();
			});
		}

		QUnit.test("updateSpaces()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("SPACE");

			var fCreate = function(facade) {
				var creation = new dtos.SpaceCreation();
				creation.setCode(code);
				creation.setDescription("test description");
				return facade.createSpaces([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.SpaceUpdate();
				update.setSpaceId(permId);
				update.setDescription("test description 2");
				return facade.updateSpaces([ update ]);
			}

			var fCheck = function(space) {
				c.assertEqual(space.getCode(), code, "Code");
				c.assertEqual(space.getDescription(), "test description 2", "Description");
			}

			testUpdate(c, fCreate, fUpdate, c.findSpace, fCheck);
		});

		QUnit.test("updateProjects() added attachments", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new dtos.ProjectCreation();
				projectCreation.setSpaceId(new dtos.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new dtos.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setSpaceId(new dtos.SpacePermId("PLATONIC"));
				attachmentCreation = new dtos.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world"));
				projectUpdate.getAttachments().add([ attachmentCreation ]);
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
				c.assertEqual(project.getDescription(), "JS test project", "Description");
				var attachments = project.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, fCheck);
		});

		QUnit.test("updateProjects() changed attributes", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new dtos.ProjectCreation();
				projectCreation.setSpaceId(new dtos.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new dtos.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setDescription("test_description");
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(project.getDescription(), "test_description", "Description");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, fCheck);
		});

		QUnit.test("updateProjects() removed space", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new dtos.ProjectCreation();
				projectCreation.setSpaceId(new dtos.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new dtos.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setSpaceId(null);
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheckError = function(error, permId) {
				c.assertContains(error, "Space id cannot be null", "Error");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, null, fCheckError);
		});

		QUnit.test("updateExperimentTypes()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("EXPERIMENT_TYPE");

			var fCreate = function(facade) {
				var assignmentCreation = new dtos.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("DESCRIPTION"));
				assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
				assignmentCreation.setInitialValueForExistingEntities("initial value");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);

				var creation = new dtos.ExperimentTypeCreation();
				creation.setCode(code);
				creation.setDescription("a new description");
				creation.setPropertyAssignments([ assignmentCreation ]);
				creation.setMetaData({"experiment_type_update":"old_value", "experiment_type_delete":"del_value"});

				return facade.createExperimentTypes([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var assignmentCreation = new dtos.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section 2");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("VERSION"));
				assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("1.0");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);
				var update = new dtos.ExperimentTypeUpdate();
				update.setTypeId(permId);
				update.setDescription("another new description");
				update.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
				update.getPropertyAssignments().set([ assignmentCreation ]);
				update.getMetaData().put("experiment_type_update", "new_value");
				update.getMetaData().add({"experiment_type_add":"add_value"});
				update.getMetaData().remove("experiment_type_delete");
				return facade.updateExperimentTypes([ update ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "another new description", "Description");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section 2", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "VERSION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");

				var metaData = type.getMetaData();
				c.assertEqual(metaData["experiment_type_update"], "new_value", "Metadata update");
				c.assertEqual(metaData["experiment_type_add"], "add_value", "Metadata add");
				c.assertEqual(metaData["experiment_type_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperimentType, fCheck);
		});

		QUnit.test("updateExperiments() changed attributes + added tag + added attachment", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new dtos.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new dtos.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new dtos.TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
				experimentCreation.setMetaData({"experiment_update":"old_value", "experiment_delete":"del_value"});
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new dtos.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProjectId(new dtos.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
				experimentUpdate.getTagIds().add(new dtos.TagCode("CREATE_ANOTHER_JSON_TAG"));
				attachmentCreation = new dtos.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world"));
				experimentUpdate.getAttachments().add([ attachmentCreation ]);
				experimentUpdate.getMetaData().put("experiment_update", "new_value");
                experimentUpdate.getMetaData().add({"experiment_add":"add_value"});
                experimentUpdate.getMetaData().remove("experiment_delete");
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				var tags = _.sortBy(experiment.getTags(), "code");
				c.assertEqual(tags[0].code, 'CREATE_ANOTHER_JSON_TAG', "tags");
				c.assertEqual(tags[1].code, 'CREATE_JSON_TAG', "tags");
				c.assertEqual(tags.length, 2, "Number of tags");
				var attachments = experiment.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");

				var metaData = experiment.getMetaData();
                c.assertEqual(metaData["experiment_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["experiment_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["experiment_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
		});

		QUnit.test("updateExperiments() changed properties + removed tag", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new dtos.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new dtos.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new dtos.TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new dtos.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProperty("EXPERIMENT_DESIGN", "OTHER");
				experimentUpdate.getTagIds().remove([ new dtos.TagCode("UNKNOWN_TAG"), new dtos.TagCode("CREATE_JSON_TAG") ]);
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				var properties = experiment.getProperties();
				c.assertEqual(properties["EXPERIMENT_DESIGN"], "OTHER", "Property EXPERIMENT_DESIGN");
				c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
				c.assertEqual(experiment.getTags().length, 0, "Number of tags");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
		});

		QUnit.test("updateExperiments() removed project", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new dtos.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new dtos.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new dtos.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProjectId(null);
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheckError = function(error, permId) {
				c.assertContains(error, "Project id cannot be null", "Error");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, null, fCheckError);
		});

		QUnit.test("updateExperiment() change property of type SAMPLE", function(assert) {
			var c = new common(assert, dtos);
			var propertyTypeCode = c.generateId("PROPERTY_TYPE");
			var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var propertyTypeCreation = new dtos.PropertyTypeCreation();
				propertyTypeCreation.setCode(propertyTypeCode);
				propertyTypeCreation.setDescription("hello");
				propertyTypeCreation.setDataType(dtos.DataType.SAMPLE);
				propertyTypeCreation.setLabel("Test Property Type");
				return facade.createPropertyTypes([ propertyTypeCreation ]).then(function(results) {
					var assignmentCreation = new dtos.PropertyAssignmentCreation();
					assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
					var experimentTypeCreation = new dtos.ExperimentTypeCreation();
					experimentTypeCreation.setCode(experimentTypeCode);
					experimentTypeCreation.setPropertyAssignments([ assignmentCreation ]);
					return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
						var creation = new dtos.ExperimentCreation();
						creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
						creation.setCode(code);
						creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
						creation.setProperty(propertyTypeCode, "20130412140147735-20");
						return facade.createExperiments([ creation ]);
					});
				});
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.ExperimentUpdate();
				update.setExperimentId(permId);
				update.setProperty(propertyTypeCode, "20130412140147736-21");
				return facade.updateExperiments([ update ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), experimentTypeCode, "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(experiment.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");
				c.assertEqual(experiment.getHistory()[0].getPropertyName(), propertyTypeCode, "Previous sample property name");
				c.assertEqual(experiment.getHistory()[0].getPropertyValue(), "20130412140147735-20", "Previous sample property value");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
		});

		QUnit.test("updateExperiment() change multi-value property of type SAMPLE", function(assert) {
            var c = new common(assert, dtos);
            var propertyTypeCode = c.generateId("PROPERTY_TYPE");
            var experimentTypeCode = c.generateId("EXPERIMENT_TYPE");
            var code = c.generateId("EXPERIMENT");

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
                    var experimentTypeCreation = new dtos.ExperimentTypeCreation();
                    experimentTypeCreation.setCode(experimentTypeCode);
                    experimentTypeCreation.setPropertyAssignments([ assignmentCreation ]);
                    return facade.createExperimentTypes([ experimentTypeCreation ]).then(function(results) {
                        var creation = new dtos.ExperimentCreation();
                        creation.setTypeId(new dtos.EntityTypePermId(experimentTypeCode));
                        creation.setCode(code);
                        creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
                        creation.setProperty(propertyTypeCode, ["20130412140147735-20", "20130424134657597-433"]);
                        return facade.createExperiments([ creation ]);
                    });
                });
            }

            var fUpdate = function(facade, permId) {
                var update = new dtos.ExperimentUpdate();
                update.setExperimentId(permId);
                update.setProperty(propertyTypeCode, "20130412140147736-21");
                return facade.updateExperiments([ update ]);
            }

            var fCheck = function(experiment) {
                c.assertEqual(experiment.getCode(), code, "Experiment code");
                c.assertEqual(experiment.getType().getCode(), experimentTypeCode, "Type code");
                c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
                c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
                c.assertEqual(experiment.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");

                var history = experiment.getHistory().filter(x => x.propertyName == propertyTypeCode && x.validTo);
                c.assertEqual(history.length, 2, "Previous sample property name");
                c.assertEqual(history.map(x => x.propertyValue).sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Previous sample property value");

            }

            testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
        });
		
		QUnit.test("updateSampleTypes()", function(assert) {
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
				creation.setGeneratedCodePrefix("TEST_PREFIX");
				creation.setPropertyAssignments([ assignmentCreation ]);
				creation.setMetaData({"sample_type_update":"old_value", "sample_type_delete":"del_value"});

				return facade.createSampleTypes([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var assignmentCreation = new dtos.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section 2");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("VERSION"));
				assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("1.0");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);
				var update = new dtos.SampleTypeUpdate();
				update.setTypeId(permId);
				update.setAutoGeneratedCode(true);
				update.setSubcodeUnique(true);
				update.setDescription("another new description");
				update.setGeneratedCodePrefix("TEST_PREFIX2");
				update.setListable(true);
				update.setShowContainer(true);
				update.setShowParents(true);
				update.setShowParentMetadata(true);
				update.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
				update.getPropertyAssignments().add([ assignmentCreation ]);
				update.getPropertyAssignments().remove([ new dtos.PropertyAssignmentPermId(permId, new dtos.PropertyTypePermId("DESCRIPTION")) ]);
                update.getMetaData().put("sample_type_update", "new_value");
                update.getMetaData().add({"sample_type_add":"add_value"});
                update.getMetaData().remove("sample_type_delete");
				return facade.updateSampleTypes([ update ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "another new description", "Description");
				c.assertEqual(type.isAutoGeneratedCode(), true, "AutoGeneratedCode");
				c.assertEqual(type.isSubcodeUnique(), true, "SubcodeUnique");
				c.assertEqual(type.getGeneratedCodePrefix(), "TEST_PREFIX2", "GeneratedCodePrefix");
				c.assertEqual(type.isListable(), true, "Listable");
				c.assertEqual(type.isShowContainer(), true, "ShowContainer");
				c.assertEqual(type.isShowParents(), true, "ShowParents");
				c.assertEqual(type.isShowParentMetadata(), true, "ShowParentMetadata");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section 2", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "VERSION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");

				var metaData = type.getMetaData();
                c.assertEqual(metaData["sample_type_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["sample_type_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["sample_type_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findSampleType, fCheck);
		});

		QUnit.test("updateSamples()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("SAMPLE");

			var fCreate = function(facade) {
				var creation = new dtos.SampleCreation();
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new dtos.SpacePermId("TEST"));
				creation.setTagIds([ new dtos.TagCode("CREATE_JSON_TAG") ]);
				creation.setMetaData({"sample_update":"old_value", "sample_delete":"del_value"});
				return facade.createSamples([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				update.getTagIds().remove(new dtos.TagCode("CREATE_JSON_TAG"));
				update.getTagIds().add(new dtos.TagCode("CREATE_JSON_TAG_2"));
				update.getTagIds().add(new dtos.TagCode("CREATE_JSON_TAG_3"));
				update.getMetaData().put("sample_update", "new_value");
                update.getMetaData().add({"sample_add":"add_value"});
                update.getMetaData().remove("sample_delete");
				return facade.updateSamples([ update ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertObjectsCount(sample.getTags(), 2);
				c.assertObjectsWithValues(sample.getTags(), "code", [ "CREATE_JSON_TAG_2", "CREATE_JSON_TAG_3" ]);

				var metaData = sample.getMetaData();
                c.assertEqual(metaData["sample_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["sample_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["sample_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateSamples() turn project sample into a space sample", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("SAMPLE");

			var fCreate = function(facade) {
				var creation = new dtos.SampleCreation();
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new dtos.SpacePermId("TEST"));
				creation.setProjectId(new dtos.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createSamples([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				update.setProjectId(null);
				update.setSpaceId(new dtos.SpacePermId("PLATONIC"));
				return facade.updateSamples([ update ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getIdentifier(), "/PLATONIC/" + code, "Identifier");
				c.assertEqual(sample.getProject(), null, "Project");
				c.assertEqual(sample.getSpace().getCode(), "PLATONIC", "Space code");
			}

			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateSamples() turn space sample into a project sample", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("SAMPLE");

			var fCreate = function(facade) {
				var creation = new dtos.SampleCreation();
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new dtos.SpacePermId("TEST"));
				return facade.createSamples([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				update.setProjectId(new dtos.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
				update.setSpaceId(new dtos.SpacePermId("PLATONIC"));
				return facade.updateSamples([ update ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/" + code, "Identifier");
				c.assertEqual(sample.getProject().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES", "Project");
				c.assertEqual(sample.getSpace().getCode(), "PLATONIC", "Space code");
			}

			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateSamples() with annotated parent and child", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("SAMPLE");
			var parentId = new dtos.SampleIdentifier("/TEST/TEST-SAMPLE-1");
			var childId = new dtos.SampleIdentifier("/TEST/TEST-PROJECT/TEST-SAMPLE-2");
			
			var fCreate = function(facade) {
				var creation = new dtos.SampleCreation();
				creation.setTypeId(new dtos.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new dtos.SpacePermId("TEST"));
				creation.setParentIds([ parentId ]);
				creation.setChildIds([ childId ]);
				creation.relationship(parentId).addParentAnnotation("type", "father").addChildAnnotation("type", "daughter");
				creation.relationship(childId).addParentAnnotation("type", "mother").addChildAnnotation("type", "son")
					.addParentAnnotation("color", "red");
				return facade.createSamples([ creation ]);
			}
			
			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				var parentRelationShip = update.relationship(parentId);
				parentRelationShip.removeChildAnnotations("type").addChildAnnotation("color", "blue")
						.addParentAnnotation("color", "yellow");
				var childRelationShip = update.relationship(childId);
				childRelationShip.addParentAnnotation("color", "green").removeParentAnnotations("type")
						.addChildAnnotation("color", "black");
				return facade.updateSamples([ update ]);
			}
			
			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				var parent = sample.getParents()[0];
				c.assertEqual(parent.getIdentifier().getIdentifier(), parentId.getIdentifier(), "Parent");
				var parentRelationship = sample.getParentRelationship(parent.getPermId());
				c.assertEqualDictionary(parentRelationship.getChildAnnotations(), {"color": "blue"}, "Parent -> child annotations");
				c.assertEqualDictionary(parentRelationship.getParentAnnotations(), {"type": "father", "color": "yellow"}, "Parent -> parent annotations");
				var child = sample.getChildren()[0];
				c.assertEqual(child.getIdentifier().getIdentifier(), childId.getIdentifier(), "Child");
				var childRelationship = sample.getChildRelationship(child.getPermId());
				c.assertEqualDictionary(childRelationship.getChildAnnotations(), {"type": "son", "color": "black"}, "Child -> child annotations");
				c.assertEqualDictionary(childRelationship.getParentAnnotations(), {"color": "green"}, "Child -> parent annotations");

			}
			
			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});
		
		QUnit.test("updateSamples() change property of type SAMPLE", function(assert) {
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

			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				update.setProperty(propertyTypeCode, "20130412140147736-21");
				return facade.updateSamples([ update ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(sample.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");
				c.assertEqual(sample.getHistory()[0].getPropertyName(), propertyTypeCode, "Previous sample property name");
				c.assertEqual(sample.getHistory()[0].getPropertyValue(), "20130412140147735-20", "Previous sample property value");
			}

			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateSamples() change multi-value property of type SAMPLE", function(assert) {
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

            var fUpdate = function(facade, permId) {
                var update = new dtos.SampleUpdate();
                update.setSampleId(permId);
                update.setProperty(propertyTypeCode, "20130412140147736-21");
                return facade.updateSamples([ update ]);
            }

            var fCheck = function(sample) {
                c.assertEqual(sample.getCode(), code, "Sample code");
                c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
                c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
                c.assertEqual(sample.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");
                var history = sample.getHistory().filter(x => x.propertyName == propertyTypeCode && x.validTo);
                c.assertEqual(history.length, 2, "Previous sample property name");
                c.assertEqual(history.map(x => x.propertyValue).sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Previous sample property value");
            }

            testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
        });
		
		QUnit.test("updateSamples() remove property of type SAMPLE", function(assert) {
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
			
			var fUpdate = function(facade, permId) {
				var update = new dtos.SampleUpdate();
				update.setSampleId(permId);
				update.setProperty(propertyTypeCode, null);
				return facade.updateSamples([ update ]);
			}
			
			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), sampleTypeCode, "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertObjectsCount(Object.keys(sample.getSampleProperties()), 0);
				c.assertEqual(sample.getHistory()[0].getPropertyName(), propertyTypeCode, "Previous sample property name");
				c.assertEqual(sample.getHistory()[0].getPropertyValue(), "20130412140147735-20", "Previous sample property value");
			}
			
			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateDataSetTypes()", function(assert) {
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
				creation.setPropertyAssignments([ assignmentCreation ]);
				creation.setMetaData({"dataset_type_update":"old_value", "dataset_type_delete":"del_value"});

				return facade.createDataSetTypes([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var assignmentCreation = new dtos.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section 2");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("VERSION"));
				assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("1.0");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);
				var update = new dtos.DataSetTypeUpdate();
				update.setTypeId(permId);
				update.setDescription("another new description");
				update.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
				update.setMainDataSetPattern(".*\\.jpg");
				update.setMainDataSetPath("original/images/");
				update.setDisallowDeletion(true);
				update.getPropertyAssignments().set([ assignmentCreation ]);
				update.getMetaData().put("dataset_type_update", "new_value");
                update.getMetaData().add({"dataset_type_add":"add_value"});
                update.getMetaData().remove("dataset_type_delete");
				return facade.updateDataSetTypes([ update ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "another new description", "Description");
				c.assertEqual(type.getMainDataSetPattern(), ".*\\.jpg", "Main data set pattern");
				c.assertEqual(type.getMainDataSetPath(), "original/images/", "Main data set path");
				c.assertEqual(type.isDisallowDeletion(), true, "Disallow deletion");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section 2", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "VERSION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");

				var metaData = type.getMetaData();
                c.assertEqual(metaData["dataset_type_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["dataset_type_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["dataset_type_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSetType, fCheck);
		});

		QUnit.test("updateDataSets()", function(assert) {
			var c = new common(assert, dtos);
			var code = null;

			var fCreate = function(facade) {
				return c.createDataSet(facade).then(function(permId) {
					code = permId.getPermId();
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				var physicalUpdate = new dtos.PhysicalDataUpdate();
				physicalUpdate.setFileFormatTypeId(new dtos.FileFormatTypePermId("TIFF"));
				physicalUpdate.setArchivingRequested(true);

				var update = new dtos.DataSetUpdate();
				update.setDataSetId(permId);
				update.setProperty("NOTES", "new 409 description");
				update.setPhysicalData(physicalUpdate);

				return facade.updateDataSets([ update ]);
			}

			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				c.assertEqual(dataSet.getProperties()["NOTES"], "new 409 description", "Property NOTES");
				c.assertEqual(dataSet.getPhysicalData().getFileFormatType().getCode(), "TIFF", "File format type");
				c.assertEqual(dataSet.getPhysicalData().isArchivingRequested(), true, "Archiving requested");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});

		QUnit.test("updateDataSet() change property of type SAMPLE", function(assert) {
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
						creation.setMetaData({"dataset_update":"old_value", "dataset_delete":"del_value"});
						return facade.createDataSets([ creation ]);
					});
				});
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.DataSetUpdate();
				update.setDataSetId(permId);
				update.setProperty(propertyTypeCode, "20130412140147736-21");
				update.getMetaData().put("dataset_update", "new_value");
                update.getMetaData().add({"dataset_add":"add_value"});
                update.getMetaData().remove("dataset_delete");
				return facade.updateDataSets([ update ]);
			}

			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Data set code");
				c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");
				c.assertEqual(dataSet.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");
				c.assertEqual(dataSet.getHistory()[0].getPropertyName(), propertyTypeCode, "Previous sample property name");
				c.assertEqual(dataSet.getHistory()[0].getPropertyValue(), "20130412140147735-20", "Previous sample property value");

				var metaData = dataSet.getMetaData();
                c.assertEqual(metaData["dataset_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["dataset_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["dataset_delete"], undefined, "Metadata delete");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});

		QUnit.test("updateDataSet() change multi-value property of type SAMPLE", function(assert) {
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
                        creation.setMetaData({"dataset_update":"old_value", "dataset_delete":"del_value"});
                        return facade.createDataSets([ creation ]);
                    });
                });
            }

            var fUpdate = function(facade, permId) {
                var update = new dtos.DataSetUpdate();
                update.setDataSetId(permId);
                update.setProperty(propertyTypeCode, "20130412140147736-21");
                update.getMetaData().put("dataset_update", "new_value");
                update.getMetaData().add({"dataset_add":"add_value"});
                update.getMetaData().remove("dataset_delete");
                return facade.updateDataSets([ update ]);
            }

            var fCheck = function(dataSet) {
                c.assertEqual(dataSet.getCode(), code, "Data set code");
                c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");
                c.assertEqual(dataSet.getSampleProperties()[propertyTypeCode][0].getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES/PLATE-2", "Sample property");
                var history = dataSet.getHistory().filter(x => x.propertyName == propertyTypeCode && x.validTo);
                c.assertEqual(history.length, 2, "Previous sample property name");
                c.assertEqual(history.map(x => x.propertyValue).sort().toString(), ["20130412140147735-20", "20130424134657597-433"].toString(), "Previous sample property value");

                var metaData = dataSet.getMetaData();
                c.assertEqual(metaData["dataset_update"], "new_value", "Metadata update");
                c.assertEqual(metaData["dataset_add"], "add_value", "Metadata add");
                c.assertEqual(metaData["dataset_delete"], undefined, "Metadata delete");
            }

            testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
        });
		
		QUnit.test("updateDataSets() link data set", function(assert) {
			var c = new common(assert, dtos);
			var code = "20160613195437233-437";

			var fCreate = function(facade) {
				return [ new dtos.DataSetPermId(code) ];
			}

			var fUpdate = function(facade, permId) {
				var linkUpdate = new dtos.LinkedDataUpdate();
				linkUpdate.setExternalCode("new code");

				var update = new dtos.DataSetUpdate();
				update.setDataSetId(permId);
				update.setLinkedData(linkUpdate);

				return facade.updateDataSets([ update ]);
			}

			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				c.assertEqual(dataSet.getLinkedData().getExternalCode(), "new code", "External code");
				c.assertEqual(dataSet.getLinkedData().getExternalDms().getPermId().toString(), "DMS_1", "External DMS");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});

		QUnit.test("updateDataSets() add content copy to link data set", function(assert) {
			var c = new common(assert, dtos);
			var edmsId = null;

			var fCreate = function(facade) {
				return c.createLinkDataSet(facade, "root/path", "my-hash", "my-repository-id").then(function(permId) {
					code = permId.getPermId();
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				return c.findDataSet(facade, permId).then(function(dataSet) {
					var contentCopy = dataSet.getLinkedData().getContentCopies()[0];
					edmsId = contentCopy.getExternalDms().getPermId();
					var contentCopyListUpdateValue = new dtos.ContentCopyListUpdateValue();
					var contentCopyCreation = new dtos.ContentCopyCreation();
					contentCopyCreation.setExternalDmsId(edmsId);
					contentCopyCreation.setPath("my/data/path");
					contentCopyCreation.setGitCommitHash("my-git-hash");
					contentCopyCreation.setGitRepositoryId("my-git-repository-id");
					contentCopyListUpdateValue.add([ contentCopyCreation ]);
					contentCopyListUpdateValue.remove([ contentCopy.getId() ]);

					var linkUpdate = new dtos.LinkedDataUpdate();
					linkUpdate.setContentCopyActions(contentCopyListUpdateValue.getActions());

					var update = new dtos.DataSetUpdate();
					update.setDataSetId(permId);
					update.setLinkedData(linkUpdate);

					return facade.updateDataSets([ update ]);
				});
			}

			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				var contentCopies = dataSet.getLinkedData().getContentCopies();
				c.assertEqual(contentCopies.length, 1, "Number of content copies");
				c.assertEqual(contentCopies[0].getExternalDms().getPermId().toString(), edmsId.toString(), "External DMS");
				c.assertEqual(contentCopies[0].getExternalCode(), null, "External code");
				c.assertEqual(contentCopies[0].getPath(), "/my/data/path", "Path");
				c.assertEqual(contentCopies[0].getGitCommitHash(), "my-git-hash", "Git commit hash");
				c.assertEqual(contentCopies[0].getGitRepositoryId(), "my-git-repository-id", "Git repository id");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});

		QUnit.test("updateMaterialTypes()", function(assert) {
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
				creation.setPropertyAssignments([ assignmentCreation ]);

				return facade.createMaterialTypes([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var assignmentCreation = new dtos.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section 2");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new dtos.PropertyTypePermId("VERSION"));
				assignmentCreation.setPluginId(new dtos.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("1.0");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);
				var update = new dtos.MaterialTypeUpdate();
				update.setTypeId(permId);
				update.setDescription("another new description");
				update.setValidationPluginId(new dtos.PluginPermId("Has_Parents"));
				update.getPropertyAssignments().add([ assignmentCreation ]);
				update.getPropertyAssignments().remove([ new dtos.PropertyAssignmentPermId(permId, new dtos.PropertyTypePermId("DESCRIPTION")) ]);
				return facade.updateMaterialTypes([ update ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "another new description", "Description");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section 2", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "VERSION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
			}

			testUpdate(c, fCreate, fUpdate, c.findMaterialType, fCheck);
		});

		QUnit.test("updateMaterials()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("MATERIAL");

			var fCreate = function(facade) {
				var materialCreation = new dtos.MaterialCreation();
				materialCreation.setTypeId(new dtos.EntityTypePermId("COMPOUND"));
				materialCreation.setCode(code);
				materialCreation.setProperty("DESCRIPTION", "Metal");
				return facade.createMaterials([ materialCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var materialUpdate = new dtos.MaterialUpdate();
				materialUpdate.setMaterialId(permId);
				materialUpdate.setProperty("DESCRIPTION", "Alloy");
				return facade.updateMaterials([ materialUpdate ]);
			}

			var fCheck = function(material) {
				c.assertEqual(material.getCode(), code, "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(properties["DESCRIPTION"], "Alloy", "Property DESCRIPTION");
			}

			testUpdate(c, fCreate, fUpdate, c.findMaterial, fCheck);
		});

		QUnit.test("updatePropertyTypes()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("PROPERTY_TYPE");
			var description = "Description of " + code;
			var label = "Label of " + code;
			var metaData1 = {"greetings" : "hello test"};

			var fCreate = function(facade) {
				var creation = new dtos.PropertyTypeCreation();
				creation.setCode(code);
				creation.setLabel("Testing");
				creation.setDescription("testing");
				creation.setDataType(dtos.DataType.VARCHAR);
				creation.setMetaData(metaData1);
				return facade.createPropertyTypes([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.PropertyTypeUpdate();
				update.setTypeId(new dtos.PropertyTypePermId(code));
				update.setDescription(description);
				update.setLabel(label);
				var metaData = update.getMetaData();
				metaData.remove("greetings");
				metaData.put("valid", "true");
				return facade.updatePropertyTypes([ update ]);
			}

			var fCheck = function(propertyType) {
				c.assertEqual(propertyType.getCode(), code, "Code");
				c.assertEqual(propertyType.getDescription(), description, "Description");
				c.assertEqual(propertyType.getLabel(), label, "Label");
				c.assertEqualDictionary(propertyType.getMetaData(), {"valid" : "true"}, "Meta data");
			}

			testUpdate(c, fCreate, fUpdate, c.findPropertyType, fCheck);
		});

		QUnit.test("updatePropertyTypes() convert data type ", function(assert) {
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
						return facade.createDataSets([ creation ]);
					});
				});
			}
			
			var fUpdate = function(facade, permId) {
				var update = new dtos.PropertyTypeUpdate();
				update.setTypeId(new dtos.PropertyTypePermId(propertyTypeCode));
				update.convertToDataType(dtos.DataType.VARCHAR);
				return facade.updatePropertyTypes([ update ]);
			}
			
			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Data set code");
				c.assertEqual(dataSet.getType().getCode(), dataSetTypeCode, "Type code");
				c.assertObjectsCount(Object.keys(dataSet.getSampleProperties()), 0);
				c.assertEqual(dataSet.getProperties()[propertyTypeCode], "20130412140147735-20", "Property");
			}
			
			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});
		
		QUnit.test("updatePlugins()", function(assert) {
			var c = new common(assert, dtos);
			var name = c.generateId("PLUGIN");
			var description = "Description of " + name;
			var script = "print 'hello'";

			var fCreate = function(facade) {
				var creation = new dtos.PluginCreation();
				creation.setName(name);
				creation.setScript("pass");
				creation.setDescription("old description");
				creation.setAvailable(false);
				creation.setPluginType(dtos.PluginType.MANAGED_PROPERTY);
				return facade.createPlugins([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.PluginUpdate();
				update.setPluginId(new dtos.PluginPermId(name));
				update.setDescription(description);
				update.setScript(script);
				return facade.updatePlugins([ update ]);
			}

			var fCheck = function(plugin) {
				c.assertEqual(plugin.getName(), name, "Name");
				c.assertEqual(plugin.getDescription(), description, "Description");
				c.assertEqual(plugin.getScript(), script, "Script");
			}

			testUpdate(c, fCreate, fUpdate, c.findPlugin, fCheck);
		});

		QUnit.test("updateVocabularies()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("VOCABULARY");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var creation = new dtos.VocabularyCreation();
				creation.setCode(code);
				return facade.createVocabularies([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.VocabularyUpdate();
				update.setVocabularyId(permId);
				update.setDescription(description);
				update.setChosenFromList(true);
				update.setUrlTemplate("https://www.ethz.ch")
				return facade.updateVocabularies([ update ]);
			}

			var fCheck = function(vocabulary) {
				c.assertEqual(vocabulary.getCode(), code, "Code");
				c.assertEqual(vocabulary.getPermId().getPermId(), code, "Perm id");
				c.assertEqual(vocabulary.getDescription(), description, "Description");
				c.assertEqual(vocabulary.getUrlTemplate(), "https://www.ethz.ch", "URL template");
			}

			testUpdate(c, fCreate, fUpdate, c.findVocabulary, fCheck);
		});

		QUnit.test("updateVocabularyTerms()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("VOCABULARY_TERM");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var termCreation = new dtos.VocabularyTermCreation();
				termCreation.setVocabularyId(new dtos.VocabularyPermId("TEST-VOCABULARY"));
				termCreation.setCode(code);
				return facade.createVocabularyTerms([ termCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var termUpdate = new dtos.VocabularyTermUpdate();
				termUpdate.setVocabularyTermId(permId);
				termUpdate.setDescription(description);
				return facade.updateVocabularyTerms([ termUpdate ]);
			}

			var fCheck = function(term) {
				c.assertEqual(term.getCode(), code, "Term code");
				c.assertEqual(term.getVocabulary().getCode(), "TEST-VOCABULARY", "Term vocabulary code");
				c.assertEqual(term.getDescription(), description, "Term description");
			}

			testUpdate(c, fCreate, fUpdate, c.findVocabularyTerm, fCheck);
		});

		QUnit.test("updateExternalDms()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("EDMS");

			var fCreate = function(facade) {
				var edmsCreation = new dtos.ExternalDmsCreation();
				edmsCreation.setCode(code);
				edmsCreation.setLabel("Test EDMS");
				edmsCreation.setAddressType(dtos.ExternalDmsAddressType.OPENBIS);
				edmsCreation.setAddress("https://my-site/q=${term}")
				return facade.createExternalDms([ edmsCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var edmsUpdate = new dtos.ExternalDmsUpdate();
				edmsUpdate.setExternalDmsId(permId);
				edmsUpdate.setLabel("Test EDMS 2");
				edmsUpdate.setAddress("https://my-second-site/q=${term}");
				return facade.updateExternalDataManagementSystems([ edmsUpdate ]);
			}

			var fCheck = function(edms) {
				c.assertEqual(edms.getCode(), code, "EDMS code");
				c.assertEqual(edms.getLabel(), "Test EDMS 2", "EDMS label");
				c.assertEqual(edms.getAddress(), "https://my-second-site/q=${term}", "EDMS address");
				c.assertEqual(edms.getUrlTemplate(), "https://my-second-site/q=${term}", "EDMS URL template");
				c.assertEqual(edms.getAddressType(), "OPENBIS", "EDMS address type");
				c.assertEqual(edms.isOpenbis(), true, "EDMS is openBIS");
			}

			testUpdate(c, fCreate, fUpdate, c.findExternalDms, fCheck);
		});

		QUnit.test("updateTags()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("TAG");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var tagCreation = new dtos.TagCreation();
				tagCreation.setCode(code);
				return facade.createTags([ tagCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var tagUpdate = new dtos.TagUpdate();
				tagUpdate.setTagId(permId);
				tagUpdate.setDescription(description);
				return facade.updateTags([ tagUpdate ]);
			}

			var fCheck = function(tag) {
				c.assertEqual(tag.getCode(), code, "Tag code");
				c.assertEqual(tag.getDescription(), description, "Tag description");
			}

			testUpdate(c, fCreate, fUpdate, c.findTag, fCheck);
		});

		QUnit.test("updateAuthorizationGroups()", function(assert) {
			var c = new common(assert, dtos);
			var code = c.generateId("AUTHORIZATION_GROUP");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var creation = new dtos.AuthorizationGroupCreation();
				creation.setCode(code);
				creation.setUserIds([ new dtos.PersonPermId("power_user") ]);
				return facade.createAuthorizationGroups([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.AuthorizationGroupUpdate();
				update.setAuthorizationGroupId(permId);
				update.setDescription(description);
				update.getUserIds().remove([ new dtos.PersonPermId("power_user") ]);
				update.getUserIds().add([ new dtos.PersonPermId("admin"), new dtos.Me() ]);
				return facade.updateAuthorizationGroups([ update ]);
			}

			var fCheck = function(group) {
				c.assertEqual(group.getCode(), code, "Code");
				c.assertEqual(group.getDescription(), description, "Description");
				var users = $.map(group.getUsers(), function(user) {
					return user.getUserId();
				});
				users.sort();
				c.assertEqual(users.toString(), "admin,openbis_test_js", "Users");
			}

			testUpdate(c, fCreate, fUpdate, c.findAuthorizationGroup, fCheck);
		});

		QUnit.test("updatePersons()", function(assert) {
			var c = new common(assert, dtos);
			var userId = c.generateId("USER");

			var fCreate = function(facade) {
				var creation = new dtos.PersonCreation();
				creation.setUserId(userId);
				return facade.createPersons([ creation ]).then(function(permIds) {
					var creation = new dtos.RoleAssignmentCreation();
					creation.setUserId(permIds[0]);
					creation.setRole(dtos.Role.ADMIN);
					return facade.createRoleAssignments([ creation ]).then(function(assignmentId) {
						return permIds;
					});
				});
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.PersonUpdate();
				update.setUserId(permId);
				update.setSpaceId(new dtos.SpacePermId("TEST"))
				return facade.updatePersons([ update ]);
			}

			var fCheck = function(person) {
				c.assertEqual(person.getUserId(), userId, "User id");
				c.assertEqual(person.getSpace().getCode(), "TEST", "Home space");
				c.assertEqual(person.isActive(), true, "Active");
			}

			testUpdate(c, fCreate, fUpdate, c.findPerson, fCheck);
		});

		QUnit.test("updatePersons() deactivate", function(assert) {
			var c = new common(assert, dtos);
			var userId = c.generateId("USER");

			var fCreate = function(facade) {
				var creation = new dtos.PersonCreation();
				creation.setUserId(userId);
				return facade.createPersons([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.PersonUpdate();
				update.setUserId(permId);
				update.deactivate();
				return facade.updatePersons([ update ]);
			}

			var fCheck = function(person) {
				c.assertEqual(person.getUserId(), userId, "User id");
				c.assertEqual(person.isActive(), false, "Active");
			}

			testUpdate(c, fCreate, fUpdate, c.findPerson, fCheck);
		});
		
		QUnit.test("updatePersons() deactivate and activate", function(assert) {
			var c = new common(assert, dtos);
			var userId = c.generateId("USER");

			var fCreate = function(facade) {
				var creation = new dtos.PersonCreation();
				creation.setUserId(userId);
				return facade.createPersons([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var deactivateUpdate = new dtos.PersonUpdate();
				deactivateUpdate.setUserId(permId);
				deactivateUpdate.deactivate();
				return facade.updatePersons([ deactivateUpdate ]).then(function(){
					var activateUpdate = new dtos.PersonUpdate();
					activateUpdate.setUserId(permId);
					activateUpdate.activate();
					return facade.updatePersons([ activateUpdate ]);
				})
			}

			var fCheck = function(person) {
				c.assertEqual(person.getUserId(), userId, "User id");
				c.assertEqual(person.isActive(), true, "Active");
			}

			testUpdate(c, fCreate, fUpdate, c.findPerson, fCheck);
		});

		QUnit.test("updatePersons() webAppSettings", function(assert) {
			var c = new common(assert, dtos);
			var userId = c.generateId("USER");

			var WEB_APP_1 = "webApp1";
			var WEB_APP_2 = "webApp2";
			var WEB_APP_3 = "webApp3";
			var WEB_APP_4 = "webApp4";

			var fCreate = function(facade) {
				var creation = new dtos.PersonCreation();
				creation.setUserId(userId);
				return facade.createPersons([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.PersonUpdate();
				update.setUserId(permId);

				var webApp1Update = update.getWebAppSettings(WEB_APP_1);
				webApp1Update.add(new dtos.WebAppSettingCreation("n1a", "v1a"));
				webApp1Update.add(new dtos.WebAppSettingCreation("n1b", "v1b"));

				var webApp2Update = update.getWebAppSettings(WEB_APP_2);
				webApp2Update.add(new dtos.WebAppSettingCreation("n2a", "v2a"));

				var webApp3Update = update.getWebAppSettings(WEB_APP_3);
				webApp3Update.add(new dtos.WebAppSettingCreation("n3a", "v3a"));

				var webApp4Update = update.getWebAppSettings(WEB_APP_4);
				webApp4Update.add(new dtos.WebAppSettingCreation("n4a", "v4a"));

				return facade.updatePersons([ update ]).then(function() {
					var update = new dtos.PersonUpdate();
					update.setUserId(permId);

					var webApp1Update = update.getWebAppSettings(WEB_APP_1);
					webApp1Update.add(new dtos.WebAppSettingCreation("n1c", "v1c"));
					webApp1Update.remove("n1b");

					var webApp2Update = update.getWebAppSettings(WEB_APP_2);
					webApp2Update.set([ new dtos.WebAppSettingCreation("n2a", "v2a updated"), new dtos.WebAppSettingCreation("n2c", "v2c") ]);

					var webApp3Update = update.getWebAppSettings(WEB_APP_3);
					webApp3Update.set();

					var webApp4Update = update.getWebAppSettings(WEB_APP_4);
					webApp4Update.remove("n4a");

					return facade.updatePersons([ update ]);
				});
			}

			var fCheck = function(person) {
				c.assertEqual(person.getUserId(), userId, "User id");
				c.assertEqual(Object.keys(person.getWebAppSettings()).length, 2, "Web apps");

				var webApp1 = person.getWebAppSettings(WEB_APP_1).getSettings();
				c.assertEqual(Object.keys(webApp1).length, 2);
				c.assertEqual(webApp1["n1a"].getValue(), "v1a");
				c.assertEqual(webApp1["n1c"].getValue(), "v1c");

				var webApp2 = person.getWebAppSettings(WEB_APP_2).getSettings();
				c.assertEqual(Object.keys(webApp2).length, 2);
				c.assertEqual(webApp2["n2a"].getValue(), "v2a updated");
				c.assertEqual(webApp2["n2c"].getValue(), "v2c");
			}

			testUpdate(c, fCreate, fUpdate, c.findPerson, fCheck);
		});

		QUnit.test("updateOperationExecutions()", function(assert) {
			var c = new common(assert, dtos);
			var permId = null;

			var fCreate = function(facade) {
				return c.createOperationExecution(facade).then(function(id) {
					permId = id;
					return [ id ];
				});
			}

			var fUpdate = function(facade, permId) {
				var executionUpdate = new dtos.OperationExecutionUpdate();
				executionUpdate.setExecutionId(permId);
				executionUpdate.setDescription("Description " + permId.getPermId());
				return facade.updateOperationExecutions([ executionUpdate ]);
			}

			var fCheck = function(execution) {
				c.assertEqual(execution.getPermId().getPermId(), permId.getPermId(), "PermId");
				c.assertEqual(execution.getDescription(), "Description " + permId.getPermId(), "Execution description");
			}

			testUpdate(c, fCreate, fUpdate, c.findOperationExecution, fCheck);
		});

		QUnit.test("updateSemanticAnnotations()", function(assert) {
			var c = new common(assert, dtos);

			var fCreate = function(facade) {
				return c.createSemanticAnnotation(facade).then(function(permId) {
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				var update = new dtos.SemanticAnnotationUpdate();
				update.setSemanticAnnotationId(permId);
				update.setPredicateOntologyId("updatedPredicateOntologyId");
				update.setPredicateOntologyVersion("updatedPredicateOntologyVersion");
				update.setPredicateAccessionId("updatedPredicateAccessionId");
				update.setDescriptorOntologyId("updatedDescriptorOntologyId");
				update.setDescriptorOntologyVersion("updatedDescriptorOntologyVersion");
				update.setDescriptorAccessionId("updatedDescriptorAccessionId");
				return facade.updateSemanticAnnotations([ update ]);
			}

			var fCheck = function(annotation) {
				c.assertEqual(annotation.getPredicateOntologyId(), "updatedPredicateOntologyId", "Predicate Ontology Id");
				c.assertEqual(annotation.getPredicateOntologyVersion(), "updatedPredicateOntologyVersion", "Predicate Ontology Version");
				c.assertEqual(annotation.getPredicateAccessionId(), "updatedPredicateAccessionId", "Predicate Accession Id");
				c.assertEqual(annotation.getDescriptorOntologyId(), "updatedDescriptorOntologyId", "Descriptor Ontology Id");
				c.assertEqual(annotation.getDescriptorOntologyVersion(), "updatedDescriptorOntologyVersion", "Descriptor Ontology Version");
				c.assertEqual(annotation.getDescriptorAccessionId(), "updatedDescriptorAccessionId", "Descriptor Accession Id");
			}

			testUpdate(c, fCreate, fUpdate, c.findSemanticAnnotation, fCheck);
		});
		
		QUnit.test("updateQueries()", function(assert) {
			var c = new common(assert, dtos);
			
			var update = new dtos.QueryUpdate();
			update.setName(c.generateId("query"));
			update.setDescription("updated description");
			update.setDatabaseId(new dtos.QueryDatabaseName("openbisDB2"));
			update.setQueryType(dtos.QueryType.SAMPLE);
			update.setEntityTypeCodePattern("sample type pattern");
			update.setSql("select * from samples where perm_id = ${key};");
			update.setPublic(true);

			var fCreate = function(facade) {
				return c.createQuery(facade).then(function(techId) {
					return [ techId ];
				});
			}

			var fUpdate = function(facade, techId) {
				update.setQueryId(techId);
				return facade.updateQueries([ update ]);
			}

			var fCheck = function(query) {
				c.assertEqual(query.getName(), update.getName().getValue(), "Name");
				c.assertEqual(query.getDescription(), update.getDescription().getValue(), "Description");
				c.assertEqual(query.getDatabaseId().getName(), update.getDatabaseId().getValue().getName(), "Database");
				c.assertEqual(query.getQueryType(), update.getQueryType().getValue(), "Query type");
				c.assertEqual(query.getEntityTypeCodePattern(), update.getEntityTypeCodePattern().getValue(), "Entity type code pattern");
				c.assertEqual(query.getSql(), update.getSql().getValue(), "Sql");
				c.assertEqual(query.isPublic(), update.isPublic().getValue(), "Is public");
			}

			testUpdate(c, fCreate, fUpdate, c.findQuery, fCheck);
		});

		QUnit.test("updatePersonalAccessTokens()", function(assert) {
			var c = new common(assert, dtos);

			var update = new dtos.PersonalAccessTokenUpdate();
			update.setAccessDate(new Date().getTime());

			var fCreate = function(facade) {
				return c.createPersonalAccessToken(facade).then(function(permId) {
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				update.setPersonalAccessTokenId(permId);
				return facade.updatePersonalAccessTokens([ update ]);
			}

			var fCheck = function(pat) {
				c.assertEqual(pat.getAccessDate(), update.getAccessDate().getValue(), "Access date");
			}

			testUpdate(c, fCreate, fUpdate, c.findPersonalAccessToken, fCheck);
		});
	}

	return function() {
		executeModule("Update tests (RequireJS)", new openbis(), dtos);
		executeModule("Update tests (RequireJS - executeOperations)", new openbisExecuteOperations(new openbis(), dtos), dtos);
		executeModule("Update tests (module VAR)", new window.openbis.openbis(), window.openbis);
		executeModule("Update tests (module VAR - executeOperations)", new openbisExecuteOperations(new window.openbis.openbis(), window.openbis), window.openbis);
		executeModule("Update tests (module ESM)", new window.openbisESM.openbis(), window.openbisESM);
		executeModule("Update tests (module ESM - executeOperations)", new openbisExecuteOperations(new window.openbisESM.openbis(), window.openbisESM), window.openbisESM);
	}
});
