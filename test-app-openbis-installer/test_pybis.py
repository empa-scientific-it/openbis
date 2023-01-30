#!/usr/bin/python
# encoding=utf8
#!/usr/bin/python
#
# Requirement:
#   The pybis module must be available.

import os
from random import randrange

import settings
import systemtest.testcase
import systemtest.util as util



class TestCase(systemtest.testcase.TestCase):


    def execute(self):

        self.SPACE = 'TEST_SPACE_' + str(randrange(100000))
        self.PROJECT = 'TEST_PROJECT_' + str(randrange(100000))
        self.USER_ID = 'SunWukong_' + str(randrange(100000))
        self.SAMPLE_CODE_1 = 'SAMPLE_' + str(randrange(100000))
        self.SAMPLE_CODE_2 = 'SAMPLE_' + str(randrange(100000))
        self.OBJECT_CODE_1 = 'OBJECT_' + str(randrange(100000))
        self.OBJECT_CODE_2 = 'OBJECT_' + str(randrange(100000))
        self.EXPERIMENT_CODE = "EXP_" + str(randrange(100000))
        self.COLLECTION_CODE = "COL_" + str(randrange(100000))
        self.FILE = 'TEST_FILE_' + str(randrange(100000))

        self.installOpenbis()
        self.installPybis()
        self.openbisController = self.createOpenbisController()
        self.openbisController.createTestDatabase("openbis")
        self.openbisController.allUp()

        openbis = self._get_openbis()
        self._test_login(openbis)
        self._test_server_information(openbis)
        self._test_datastores(openbis)
        self._test_spaces(openbis)
        self._test_id_generation(openbis)
        self._test_persons(openbis)
        self._test_groups(openbis)
        self._test_role_assignments(openbis)
        self._test_samples(openbis)
        self._test_objects(openbis)
        self._test_experiments(openbis)
        self._test_collections(openbis)
        self._test_datasets(openbis)
        self._test_entity_types(openbis)
        # self._test_material_types(openbis)
        self._test_semantic_annotations(openbis)
        self._test_tags(openbis)
        self._test_vocabularies(openbis)
        # self._test_plugins(openbis)
        self._test_logout(openbis)


    def _get_openbis(self):
        # pybis can only be imported after installPybis is called
        import pybis
        return pybis.Openbis(url="https://localhost:8443", verify_certificates=False)


    def _test_login(self, openbis):
        util.printWhoAmI()
        openbis.login('admin', 'admin', save_token=True)
        self.assertEquals('openBIS session is active', True, openbis.is_session_active())


    def _test_server_information(self, openbis):
        util.printWhoAmI()
        server_information = openbis.get_server_information()
        self.assertType('major_version', int, server_information.get_major_version())
        self.assertType('minor_version', int, server_information.get_major_version())


    def _test_datastores(self, openbis):
        util.printWhoAmI()
        self.assertIn('datastores', openbis.get_datastores().code.values, 'DSS1')


    def _test_spaces(self, openbis):
        util.printWhoAmI()
        space = openbis.new_space(code=self.SPACE)
        self.assertLength('space.permId', 0, space.permId)
        space.save()
        self.assertEquals('space.permId', self.SPACE, space.permId)
        self.assertIn('spaces', openbis.get_spaces().df.code.values, self.SPACE)
        self.assertLength('spaces', 1, openbis.get_spaces(code=self.SPACE))
        self.assertNotNone('space', openbis.get_space(code=self.SPACE))


    def _test_projects(self, openbis):
        util.printWhoAmI()
        project = openbis.new_project(self.SPACE, self.PROJECT)
        self.assertNone('project.permId', project.permId)
        project.save()
        self.assertNotNone('project.permId', project.permId)
        self.assertEquals('project.permId', project.permId, openbis.get_project(self.SPACE + '/' + self.PROJECT).permId)
        self.assertIn('projects', openbis.get_projects(space=self.SPACE).df.permId.values, project.permId)
        self.assertIn('projects', openbis.projects.df.permId.values, project.permId)


    def _test_id_generation(self, openbis):
        util.printWhoAmI()
        # should create incremental ids
        permId1 = openbis.create_permId()
        permId2 = openbis.create_permId()
        permId1_number = int(permId1.split("-")[1])
        permId2_number = int(permId2.split("-")[1])
        self.assertEquals('permId number', permId1_number + 1, permId2_number)
        object_code_1 = openbis.gen_code("OBJECT")
        object_code_2 = openbis.gen_code("OBJECT")
        self.assertEquals('object code', int(object_code_1) + 1, int(object_code_2))


    def _test_persons(self, openbis):
        util.printWhoAmI()
        # should return a person if person already exists
        person = openbis.new_person("admin")
        self.assertEquals('person.permId', 'admin', person.permId)
        # should create new person
        person = openbis.new_person(self.USER_ID)
        self.assertEquals('person.userId', self.USER_ID, person.userId)
        self.assertLength('person.permId', 0, person.permId)
        self.openbisController.addUser(self.USER_ID, 'password')
        person.save()
        person = openbis.get_person(self.USER_ID)
        self.assertEquals('person.userId', self.USER_ID, person.userId)
        self.assertNotNone('person.permId', person.permId)
        self.assertIn('', openbis.get_persons().df.userId.values, person.userId)
        persons = openbis.get_persons(roleLevel="INSTANCE")
        self.assertIn('userIds', persons.df.userId.values, 'admin')


    def _test_groups(self, openbis):
        util.printWhoAmI()
        group_id = "test_group"
        group = openbis.new_group(group_id)
        self.assertEquals('group.code', group_id, group.code)
        self.assertLength('group.permId', 0, group.permId)
        group = group.save()
        self.assertNotNone('group.permId', group.permId)
        # TODO: next lines cause the test _test_role_assignments() to fail.
        # self.assertNotNone('group', openbis.get_group(code=group.permId))
        # self.assertNotEmpty('groups', openbis.get_groups(code=group.permId))


    def _test_role_assignments(self, openbis):
        util.printWhoAmI()
        # should find instance admin
        found = False
        for role_assignment in openbis.get_role_assignments(user='admin'):
            found = True
            self.assertEquals('role', 'ADMIN', role_assignment.role)
            self.assertEquals('roleLevel', 'INSTANCE', role_assignment.roleLevel)
        self.assertTrue('found admin role', found)


    def _test_samples(self, openbis):
        util.printWhoAmI()
        samples = openbis.get_samples(code=self.SAMPLE_CODE_1)
        self.assertLength('samples with code ' + self.SAMPLE_CODE_1, 0, samples)
        sample = openbis.new_sample(type="UNKNOWN", code=self.SAMPLE_CODE_1, space=self.SPACE)
        self.assertLength('sample.permId', 0, sample.permId)
        sample.save()
        self.assertNotNone('sample.permId', sample.permId)
        sample.delete("reason")
        samples = openbis.get_samples(code=self.SAMPLE_CODE_1)
        self.assertLength('samples with code ' + self.SAMPLE_CODE_1, 0, samples)
        self.assertIn('deletions ids', openbis.get_deletions().permId.values, sample.permId)

    def _test_objects(self, openbis):
        util.printWhoAmI()
        objects = openbis.get_objects(code=self.OBJECT_CODE_1)
        self.assertLength('objects with code ' + self.OBJECT_CODE_1, 0, objects)
        object = openbis.new_object(type="UNKNOWN", code=self.OBJECT_CODE_1, space=self.SPACE)
        self.assertLength('object.permId', 0, object.permId)
        object.save()
        self.assertNotNone('object.permId', object.permId)
        object.delete("reason")
        objects = openbis.get_objects(code=self.OBJECT_CODE_1)
        self.assertLength('objects with code ' + self.OBJECT_CODE_1, 0, objects)
        self.assertIn('deletions ids', openbis.get_deletions().permId.values, object.permId)


    def _test_experiments(self, openbis):
        util.printWhoAmI()
        # should check existing experiment
        self.assertNotEmpty('experiments', openbis.get_experiments())
        self.assertLength('experiments code=DEFAULT', 1, openbis.get_experiments(code='DEFAULT'))
        self.assertNotEmpty('experiments type=UNKNOWN', openbis.get_experiments(type='UNKNOWN'))
        self.assertNotEmpty('experiments project=DEFAULT', openbis.get_experiments(project='DEFAULT'))
        experiment = openbis.get_experiment('/DEFAULT/DEFAULT/DEFAULT')
        self.assertEquals('experiment.project.identifier', '/DEFAULT/DEFAULT', experiment.project.identifier)
        # should create new experiment
        experiment = openbis.new_experiment(type="UNKNOWN", code=self.EXPERIMENT_CODE, project="DEFAULT")
        self.assertLength('experiment.permId', 0, experiment.permId)
        experiment.save()
        self.assertNotNone('experiment.permId', experiment.permId)

    def _test_collections(self, openbis):
        util.printWhoAmI()
        # should check existing collection
        self.assertNotEmpty('collections', openbis.get_collections())
        self.assertLength('collections code=DEFAULT', 1, openbis.get_collections(code='DEFAULT'))
        self.assertNotEmpty('collections type=UNKNOWN', openbis.get_collections(type='UNKNOWN'))
        self.assertNotEmpty('collections project=DEFAULT', openbis.get_collections(project='DEFAULT'))
        collection = openbis.get_collection('/DEFAULT/DEFAULT/DEFAULT')
        self.assertEquals('collection.project.identifier', '/DEFAULT/DEFAULT', collection.project.identifier)
        # should create new collection
        collection = openbis.new_collection(type="UNKNOWN", code=self.COLLECTION_CODE, project="DEFAULT")
        self.assertLength('collection.permId', 0, collection.permId)
        collection.save()
        self.assertNotNone('collection.permId', collection.permId)


    def _test_datasets(self, openbis):
        util.printWhoAmI()
        sample = openbis.new_sample(type="UNKNOWN", code=self.SAMPLE_CODE_2, space=self.SPACE)
        sample.save()
        obj = openbis.new_object(type="UNKNOWN", code=self.OBJECT_CODE_2, space=self.SPACE)
        obj.save()
        with open(self.FILE, "w") as file:
            file.write("content")
        dataset = openbis.new_dataset(files=[self.FILE], type="UNKNOWN")
        dataset.sample = openbis.get_sample('/' + self.SPACE + '/' + self.SAMPLE_CODE_2)
        dataset.object = openbis.get_object('/' + self.SPACE + '/' + self.OBJECT_CODE_2)
        self.assertLength('dataset.permId', 0, dataset.permId)
        dataset.save()
        self.assertNotNone('dataset.permId', dataset.permId)
        self.assertIn('dataset.file_list', dataset.file_list, "original/" + self.FILE)
        host_and_permid = dataset.download()
        self.assertTrue('downloaded file exists', os.path.exists(os.path.join(host_and_permid, "original", self.FILE)))
        self.assertNotEmpty('datasets', openbis.get_datasets())
        fetched_dataset = openbis.get_dataset(dataset.permId)
        self.assertNotNone('dataset', fetched_dataset)
        self.assertEquals('dataset.permId', dataset.permId, fetched_dataset.permId)
        os.remove(self.FILE)


    def _test_entity_types(self, openbis):
        util.printWhoAmI()
        for entity in ['dataset', 'experiment', 'sample', 'collection', 'object']:
            method_all = getattr(openbis, 'get_' + entity + '_types')
            method_one = getattr(openbis, 'get_' + entity + '_type')
            self.assertNotEmpty(entity + ' types', method_all())
            entity_type = method_one('UNKNOWN')
            self.assertEquals('entity_type.code', 'UNKNOWN', entity_type.code)


    # TODO: method openbis.new_material_type() does not exist.
    # def _test_material_types(self, openbis):
    #     util.printWhoAmI()
    #     materialType = openbis.new_material_type(code=self.SPACE)
    #     self.assertLength('materialType.permId', 0, materialType.permId)
    #     materialType.save()
    #
    #     material_types = openbis.get_material_types()
    #     print('material_types: %s' % (material_types))
    #     self.assertNotEmpty('material types', material_types)
    #     found_material_type = openbis.get_material_type('UNKNOWN')
    #     self.assertEquals('found_material_type.code', 'UNKNOWN', found_material_type.code)


    def _test_semantic_annotations(self, openbis):
        util.printWhoAmI()
        sa = openbis.new_semantic_annotation(
            entityType = 'UNKNOWN',
            predicateOntologyId = 'po_id', predicateOntologyVersion = 'po_version', predicateAccessionId = 'pa_id', 
            descriptorOntologyId = 'do_id',    descriptorOntologyVersion = 'do_version', descriptorAccessionId = 'da_id')
        self.assertNone('semantic annotation permId', sa.permId)
        sa.save()
        self.assertNotNone('semantic annotation permId', sa.permId)
        sas = openbis.get_semantic_annotations()
        self.assertIn('semantic annotation permIds', sas.df.permId.values, sa.permId)
        saved_sa = openbis.get_semantic_annotation(sa.permId)
        self.assertNotNone('semantic annotation', saved_sa)


    def _test_tags(self, openbis):
        util.printWhoAmI()
        tag = openbis.new_tag('TAG1')
        self.assertLength('tag.permId', 0, tag.permId)
        tag.save()
        self.assertNotNone('tag.permId', tag.permId)
        self.assertIn('tag permIds', openbis.get_tags().df.permId.values, tag.permId)
        self.assertNotNone('tag', openbis.get_tag(tag.permId))


    def _test_vocabularies(self, openbis):
        util.printWhoAmI()
        # should create vocabulary
        vocabulary = openbis.new_vocabulary(
            code = 'VOCABULARY_1',
            description = 'description',
            terms = [
                { "code": "TERM1", "label": "label1", "description": "description1" },
                { "code": "TERM2", "label": "label2", "description": "description2" },
            ]
        )
        self.assertNone('vocabulary.registrationDate', vocabulary.registrationDate)
        vocabulary.save()
        self.assertNotNone('vocabulary.registrationDate', vocabulary.registrationDate)
        # should get terms
        terms = openbis.get_terms(vocabulary='VOCABULARY_1')
        self.assertNotEmpty('terms', terms)
        self.assertNotNone('term', openbis.get_term(code='TERM1', vocabularyCode='VOCABULARY_1'))
        self.assertIn('VOCABULARY_1 terms', terms.df.code.values, 'TERM1')
        self.assertIn('VOCABULARY_1 terms', terms.df.code.values, 'TERM2')
        self.assertNotNone('vocabulary', openbis.get_vocabulary(code=vocabulary.code))
        self.assertNotEmpty('vocabularies', openbis.get_vocabularies(code=vocabulary.code))


    # TODO: plugin.save() is not working properly, it requires a tag to be present in the system.
    # def _test_plugins(self, openbis):
    #     util.printWhoAmI()
    #     plugin = openbis.new_plugin()
    #     plugin.script = 'print(\'Hello world\')'
    #     self.assertNone('plugin.permId', plugin.permId)
    #     plugin.save()
    #     self.assertNotNone('plugin.permId', plugin.permId)
    #     plugins = openbis.get_plugins()
    #     self.assertNotEmpty('plugins', plugins)
    #     self.assertIn('plugins', plugins, plugin)
    #     plugin2 = openbis.get_plugin(plugin.permId)
    #     self.assertNotNone('plugin', plugin2)


    def _test_logout(self, openbis):
        util.printWhoAmI()
        openbis.logout()
        self.assertFalse('openBIS session is active', openbis.is_session_active())

TestCase(settings, __file__).runTest()
