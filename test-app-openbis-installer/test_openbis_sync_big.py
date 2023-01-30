#!/usr/bin/python
# encoding=utf8
import glob
import os
import re
import shutil
import time
import settings
import systemtest.testcase
import systemtest.util as util
import ssl, base64
import json
from random import randint

from functools import wraps

from systemtest.testcase import TEST_DATA
from systemtest.testcase import OPENBIS_STANDARD_TECHNOLOGIES_PROJECT
from systemtest.util import printAndFlush


# Had to add the ssl wrap thing below because of a problem during the auth call
def sslwrap(func):

    @wraps(func)
    def bar(*args, **kw):
        kw['ssl_version'] = ssl.PROTOCOL_TLSv1
        return func(*args, **kw)

    return bar


ssl.wrap_socket = sslwrap(ssl.wrap_socket)

ALL_TECHNOLOGIES = ['eln-lims', 'illumina-ngs', 'microscopy', 'flow']
DATA_SOURCE_AS_PORT = '9000'
DATA_SOURCE_DSS_PORT = '9001'
HARVESTER_AS_PORT = '9002'
HARVESTER_DSS_PORT = '9003'
TYPE_PREFIX = 'DS1_'


class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        openbis_data_source = self._setupOpenbisDataSource()
        openbis_data_source.allUp()
        self._drop_test_examples(openbis_data_source)
        self._freeze_some_entities(openbis_data_source)

        openbis_harvester = self._setupOpenbisHarvester()
        openbis_harvester.allUp()

        self._waitUntilSyncIsFinished(openbis_harvester)
        
        self._checkData(openbis_data_source, openbis_harvester)

    def executeInDevMode(self):
        openbis_data_source = self.createOpenbisController('data_source', port=DATA_SOURCE_AS_PORT, dropDatabases=False)
#        openbis_data_source.allUp()
        
#        self._drop_test_examples(openbis_data_source)
        openbis_harvester = self.createOpenbisController('harvester', port=HARVESTER_AS_PORT, dropDatabases=False)
#        openbis_harvester = self._setupOpenbisHarvester()
#        openbis_harvester.allUp()
#        self._waitUntilSyncIsFinished(openbis_harvester)
#        self._freeze_some_entities(openbis_data_source)
#        self._checkData(openbis_data_source, openbis_harvester)
        self._checkData(openbis_data_source, openbis_harvester)

    def _freeze_some_entities(self, openbis_data_source):
        util.printAndFlush("Freeze some entities")
        openbis_data_source.queryDatabase("openbis", 
              "update spaces set frozen = 't', frozen_for_proj = 't', frozen_for_samp = 't' where code = 'DEFAULT'")
        openbis_data_source.queryDatabase("openbis", 
              "update projects set frozen = 't', frozen_for_exp = 't', frozen_for_samp = 't' where code = 'DEFAULT'")
        openbis_data_source.queryDatabase("openbis", 
              "update experiments set frozen = 't', frozen_for_data = 't' where code = 'MICROSCOPY-EXP1_181511081915722000'")
        openbis_data_source.queryDatabase("openbis", 
              "update experiments set frozen = 't', frozen_for_samp = 't' where code = 'TEST-EXPERIMENT'")
        openbis_data_source.queryDatabase("openbis", 
              "update samples set frozen = 't', frozen_for_children = 't', frozen_for_comp = 't', frozen_for_data = 't' "
              + "where code = 'BSSE-QGF-RAW-6'")
        openbis_data_source.queryDatabase("openbis", 
              "update samples set frozen = 't', frozen_for_parents = 't', frozen_for_comp = 't', frozen_for_data = 't' "
              + "where code = 'BSSE-QGF-RAW-8'")
        openbis_data_source.queryDatabase("openbis", 
              "update data set frozen = 't', frozen_for_children = 't', frozen_for_parents = 't', "
              + "frozen_for_comps = 't', frozen_for_conts = 't' "
              + "where code in ('20181115081535015-7', '20181115081535299-10', '20181115081918773-62', '20181115081918773-63')")
        
        
    def _drop_test_examples(self, openbis):
        incoming = "%s/data/incoming-test" % openbis.installPath
        if not os.path.exists(incoming):
            os.makedirs(incoming)
        self._mark_and_wait(incoming, openbis, self._drop_big_file(incoming))
        self._mark_and_wait(incoming, openbis, self._drop_big_folder_with_many_files(incoming))

    def _mark_and_wait(self, incoming, openbis, file):
        with open("%s/.MARKER_is_finished_%s" % (incoming, file), 'w') as f:
            pass
        openbis.waitUntilDataSetRegistrationFinished(1, timeOutInMinutes = 20)

    def _drop_big_file(self, incoming):
        big_file = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT, 'openBIS-installation')
        big_file_folder, big_file_name = os.path.split(big_file)
        util.copyFromTo(big_file_folder, incoming, big_file_name)
        return big_file_name

    def _drop_big_folder_with_many_files(self, incoming):
        util.printAndFlush("Create big example with many files")

        next_words = self._get_next_words_dictionary('Pride_and_Prejudice.txt')
        number_of_text_files = 20000
        minimum_size = 50000
        texts_folder = "%s/%s" % (incoming, "texts")
        if not os.path.exists(texts_folder):
            os.makedirs(texts_folder)
        for i in range(number_of_text_files):
            with open("%s/text-%05d.txt" % (texts_folder, i + 1), 'w') as f:
                f.write(self._create_random_text(next_words, minimum_size))
        util.printAndFlush("%s text files (minimum size %s each) created in %s" % (number_of_text_files, minimum_size, texts_folder))
        return "texts"

    def _create_random_text(self, next_words, minimum_size):
        text = ''
        word = self._choose_random_word(next_words)
        line = word
        while len(text) < minimum_size:
            word = self._choose_next_word(next_words, word)
            if len(line) > 60:
                text += '\n' + line
                line = word
            else:
                line += ' ' + word
        return text + '\n'

    def _choose_random_word(self, next_words):
        keys = list(next_words)
        return keys[randint(0, len(keys) - 1)]

    def _choose_next_word(self, next_words, word):
        if word in next_words:
            words = next_words[word]
            return words[randint(0, len(words) - 1)]
        return word

    def _get_next_words_dictionary(self, text_file):
        next_words = {}
        previous_word = None
        with open("%s/%s" % (self.getTemplatesFolder(), text_file), 'r') as f:
            for line in f:
                for word in line.split():
                    if previous_word is not None:
                        words = []
                        if previous_word not in next_words:
                            next_words[previous_word] = words
                        else:
                            words = next_words[previous_word]
                        words.append(word)
                    previous_word = word
        return next_words
    
    def _checkData(self, openbis_data_source, openbis_harvester):
        self._compareDataBases("Internal Vocabularies", openbis_data_source, openbis_harvester, "openbis",
                               "select code, description from controlled_vocabularies "
                               + "where is_managed_internally and pers_id_registerer = 1 order by code")
        self._compareDataBases("Non internal Vocabularies", openbis_data_source, openbis_harvester, "openbis",
                               "select '{0}' || code as code, description, source_uri, is_chosen_from_list "
                               + "from controlled_vocabularies "
                               + "where (not is_managed_internally or pers_id_registerer <> 1) and code like '{1}%' order by code")
        self._compareDataBases("Internal property types", openbis_data_source, openbis_harvester, "openbis", 
                               "select t.code as code, dt.code as data_type, v.code as vocabulary, "
                               + "mt.code as material, t.label, t.description, "
                               + "t.is_managed_internally, t.schema, t.transformation "
                               + "from property_types t join data_types dt on t.daty_id = dt.id "
                               + "left join controlled_vocabularies v on t.covo_id = v.id "
                               + "left join material_types mt on t.maty_prop_id = mt.id "
                               + "where t.is_managed_internally and t.pers_id_registerer = 1 order by t.code, t.is_managed_internally")
        self._compareDataBases("Non internal property types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code,dt.code as data_type, "
                               + "case when v.is_managed_internally then v.code else '{0}' || v.code end as vocabulary, "
                               + "'{0}' || mt.code as material, t.label, t.description, "
                               + "t.schema, t.transformation "
                               + "from property_types t join data_types dt on t.daty_id = dt.id "
                               + "left join controlled_vocabularies v on t.covo_id = v.id "
                               + "left join material_types mt on t.maty_prop_id = mt.id "
                               + "where (not t.is_managed_internally or t.pers_id_registerer <> 1) "
                               + "and t.code like '{1}%' and not v.code like '%PLATE_GEOMETRY'"
                               + "order by t.code, t.is_managed_internally")
        self._compareDataBases("Material types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code, t.description, '{0}' || s.name as validation_script "
                               + "from material_types t left join scripts s on t.validation_script_id = s.id "
                               + "where t.code like '{1}%' order by t.code")
        self._compareDataBases("Material type property assignments", openbis_data_source, openbis_harvester, "openbis",
                                "select '{0}' || et.code as material_type, '{0}' || pt.code as property_type, "
                                + "etpt.is_mandatory, etpt.is_managed_internally, etpt.ordinal, etpt.section, "
                                + "etpt.is_shown_edit, etpt.show_raw_value, '{0}' || s.name as script "
                                + "from material_type_property_types etpt "
                                + "join material_types et on etpt.maty_id = et.id "
                                + "join property_types pt on etpt.prty_id = pt.id "
                                + "left join scripts s on etpt.script_id = s.id "
                                + "where et.code like '{1}%' order by et.code, pt.code, pt.is_managed_internally")
        self._compareDataBases("Experiment types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || t.code as code, t.description, '{0}' || s.name as validation_script "
                               + "from experiment_types t left join scripts s on t.validation_script_id = s.id "
                               + "where t.code like '{1}%' order by t.code")
        self._compareDataBases("Experiment type property assignments", openbis_data_source, openbis_harvester, "openbis",
                                "select '{0}' || et.code as experiment_type, "
                                + "case when pt.is_managed_internally then pt.code else '{0}' || pt.code end as property_type, "
                                + "pt.is_managed_internally, "
                                + "etpt.is_mandatory, etpt.is_managed_internally, etpt.ordinal, etpt.section, "
                                + "etpt.is_shown_edit, etpt.show_raw_value, '{0}' || s.name as script "
                                + "from experiment_type_property_types etpt "
                                + "join experiment_types et on etpt.exty_id = et.id "
                                + "join property_types pt on etpt.prty_id = pt.id "
                                + "left join scripts s on etpt.script_id = s.id "
                                + "where et.code like '{1}%' order by experiment_type, property_type")
        self._compareDataBases("Sample types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || code as code, t.description, is_listable, generated_from_depth, part_of_depth "
                               + "is_auto_generated_code, generated_code_prefix, is_subcode_unique, inherit_properties, "
                               + "show_parent_metadata, '{0}' || s.name as validation_script "
                               + "from sample_types t left join scripts s on t.validation_script_id = s.id "
                               + "where code like '{1}%' order by code")
        self._compareDataBases("Sample type property assignments", openbis_data_source, openbis_harvester, "openbis",
                                "select '{0}' || et.code as sample_type, "
                                + "case when pt.is_managed_internally and pt.code not similar to "
                                + "'(ANALYSIS_PROCEDURE|PLATE_GEOMETRY|CONTROL|GENE|GENE_SYMBOLS|INHIBITOR_OF|LIBRARY_ID|NUCLEOTIDE_SEQUENCE|SIRNA)' "
                                + "then pt.code else '{0}' || pt.code end as property_type, "
                                + "etpt.is_mandatory, etpt. is_managed_internally, etpt.ordinal, etpt.section, "
                                + "etpt.is_shown_edit, etpt.show_raw_value, '{0}' || s.name as script "
                                + "from sample_type_property_types etpt "
                                + "join sample_types et on etpt.saty_id = et.id "
                                + "join property_types pt on etpt.prty_id = pt.id "
                                + "left join scripts s on etpt.script_id = s.id "
                                + "where et.code like '{1}%' order by sample_type, property_type")
        self._compareDataBases("Data set types", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || code as code, t.description, main_ds_pattern, main_ds_path, "
                               + "deletion_disallow, '{0}' || s.name as validation_script "
                               + "from data_set_types t left join scripts s on t.validation_script_id = s.id "
                               + "where code like '{1}%' order by code")
        self._compareDataBases("Data set type property assignments", openbis_data_source, openbis_harvester, "openbis",
                                "select '{0}' || et.code as data_set_type, "
                                + "case when pt.is_managed_internally then pt.code else '{0}' || pt.code end as property_type, "
                                + "pt.is_managed_internally, "
                                + "etpt.is_mandatory, etpt. is_managed_internally, etpt.ordinal, etpt.section, "
                                + "etpt.is_shown_edit, etpt.show_raw_value, '{0}' || s.name as script "
                                + "from data_set_type_property_types etpt "
                                + "join data_set_types et on etpt.dsty_id = et.id "
                                + "join property_types pt on etpt.prty_id = pt.id "
                                + "left join scripts s on etpt.script_id = s.id "
                                + "where et.code like '{1}%' order by data_set_type, property_type")
        self._compareDataBases("Plugins", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || name as name, description, script_type, plugin_type, entity_kind, is_available, "
                               + "length(script) as script_length, md5(script) as script_hash "
                               + "from scripts where name like '{1}%' order by name")
        # Space STORAGE will be ignored because it is empty
        self._compareDataBases("Spaces", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || s.code as code, s.description, "
                               + "ur.user_id as registrator, "
                               + "to_char(s.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "s.frozen, s.frozen_for_proj, s.frozen_for_samp "
                               + "from spaces s join persons ur on s.pers_id_registerer = ur.id "
                               + "where s.code like '{1}%' and not s.code = '{1}STORAGE' order by s.code")
        self._compareDataBases("Projects", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || s.code as space, p.code as project, p.description, "
                               + "ur.user_id as registrator, "
                               + "to_char(p.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "um.user_id as modifier, "
                               + "to_char(p.modification_timestamp, 'YYYY-MM-DD HH24:MI:SS') as modification_timestamp, "
                               + "p.frozen, p.frozen_for_exp, p.frozen_for_samp, p.space_frozen "
                               + "from projects p join spaces s on p.space_id = s.id "
                               + "join persons ur on p.pers_id_registerer = ur.id "
                               + "join persons um on p.pers_id_modifier = um.id "
                               + "where s.code like '{1}%' order by s.code, p.code")
        self._compareDataBases("Material properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select '{0}' || m.code as material, '{0}' || t.code as type, "
                               + "case when pt.is_managed_internally and pt.code not similar to "
                               + "'(ANALYSIS_PROCEDURE|PLATE_GEOMETRY|CONTROL|GENE|GENE_SYMBOLS|INHIBITOR_OF|LIBRARY_ID|NUCLEOTIDE_SEQUENCE|SIRNA)' "
                               + "then pt.code else '{0}' || pt.code end as property, "
                               + "concat(mp.value, cvt.code, '{0}' || m2.code) as value, "
                               + "ur.user_id as registrator, "
                               + "to_char(m.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "to_char(m.modification_timestamp, 'YYYY-MM-DD HH24:MI:SS') as modification_timestamp "
                               + "from materials m join material_properties mp on mp.mate_id = m.id "
                               + "left join controlled_vocabulary_terms cvt on mp.cvte_id = cvt.id "
                               + "left join materials m2 on mp.mate_prop_id = m2.id "
                               + "join material_type_property_types etpt on mp.mtpt_id = etpt.id "
                               + "join material_types t on etpt.maty_id = t.id "
                               + "join property_types pt on etpt.prty_id = pt.id "
                               + "join persons ur on m.pers_id_registerer = ur.id "
                               + "order by m.code, property")
        self._compareDataBases("Experiment properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select '/' || '{0}' || sp.code || '/' || p.code || '/' || e.code as experiment, "
                               + "'{0}' || t.code as type, "
                               + "case when pt.is_managed_internally then pt.code else '{0}' || pt.code end as property, "
                               + "concat(ep.value, cvt.code, m.code) as value, "
                               + "ur.user_id as registrator, "
                               + "to_char(e.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "um.user_id as modifier, "
                               + "to_char(e.modification_timestamp, 'YYYY-MM-DD HH24:MI:SS') as modification_timestamp, "
                               + "e.frozen, e.frozen_for_data, e.frozen_for_samp, e.proj_frozen "
                               + "from experiments e join experiment_properties ep on ep.expe_id = e.id "
                               + "join projects p on e.proj_id = p.id "
                               + "join spaces sp on p.space_id = sp.id "
                               + "left join controlled_vocabulary_terms cvt on ep.cvte_id = cvt.id "
                               + "left join materials m on ep.mate_prop_id = m.id "
                               + "join experiment_type_property_types etpt on ep.etpt_id = etpt.id "
                               + "join experiment_types t on etpt.exty_id = t.id "
                               + "join property_types pt on etpt.prty_id = pt.id "
                               + "join persons ur on e.pers_id_registerer = ur.id "
                               + "join persons um on e.pers_id_modifier = um.id "
                               + "where sp.code like '{1}%' "
                               + "order by experiment, property")
        self._compareDataBases("Number of samples per experiment", openbis_data_source, openbis_harvester, "openbis",
                               "select '/' || '{0}' || sp.code || '/' || p.code || '/' || e.code as experiment, "
                               + "count(*) as number_of_samples "
                               + "from experiments e join projects p on e.proj_id = p.id "
                               + "join spaces sp on p.space_id = sp.id "
                               + "join samples s on s.expe_id = e.id "
                               + "where sp.code like '{1}%' "
                               + "group by experiment order by experiment")
        self._compareDataBases("Attachments", openbis_data_source, openbis_harvester, "openbis",
                               "select e.code as experiment, p.code as project, s.code as sample, "
                               + "a.file_name, a.version, a.title, a.description, length(value) as attachment_length, "
                               + "md5(value) as attachment_hash "
                               + "from attachments a join attachment_contents c on a.exac_id = c.id "
                               + "left join experiments e on a.expe_id = e.id "
                               + "left join projects p on a.proj_id = p.id "
                               + "left join samples s on a.samp_id = s.id order by a.file_name, a.version")
        self._compareDataBases("Samples which are not of type BENCHMARK_OBJECT", openbis_data_source, openbis_harvester, "openbis",
                               "select s.code, '{0}' || t.code as type, s.perm_id, '{0}' || sp.code as space, "
                               + "p.code as project, e.code as experiment, sc.code as container, "
                               + "ur.user_id as registrator, "
                               + "to_char(s.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "um.user_id as modifier, "
                               + "to_char(s.modification_timestamp, 'YYYY-MM-DD HH24:MI:SS') as modification_timestamp "
                               + "from samples s join sample_types t on s.saty_id = t.id "
                               + "left join spaces sp on s.space_id = sp.id "
                               + "left join projects p on s.proj_id = p.id "
                               + "left join experiments e on s.expe_id = e.id "
                               + "left join samples sc on s.samp_id_part_of = sc.id "
                               + "join persons ur on s.pers_id_registerer = ur.id "
                               + "join persons um on s.pers_id_modifier = um.id "
                               + "where sp.code like '{1}%' and t.code <> '{1}BENCHMARK_OBJECT' and s.code != 'DEFAULT' order by s.code, s.perm_id")
        self._compareDataBases("Sample properties", openbis_data_source, openbis_harvester, "openbis",
                               "select s.code as sample, '{0}' || t.code as type, "
                               + "case when pt.is_managed_internally then pt.code else '{0}' || pt.code end as property, "
                               + "concat(sp.value, cvt.code, m.code) as value, "
                               + "s.frozen, s.frozen_for_data, s.frozen_for_children, s.frozen_for_parents, s.frozen_for_comp, "
                               + "s.space_frozen, s.proj_frozen, s.expe_frozen, s.cont_frozen "
                               + "from samples s join sample_properties sp on sp.samp_id = s.id "
                               + "left join controlled_vocabulary_terms cvt on sp.cvte_id = cvt.id "
                               + "left join materials m on sp.mate_prop_id = m.id "
                               + "join sample_type_property_types stpt on sp.stpt_id = stpt.id "
                               + "join sample_types t on stpt.saty_id = t.id "
                               + "join property_types pt on stpt.prty_id = pt.id "
                               + "where expe_id in (select id from experiments where code = 'TEST-EXPERIMENT') "
                               + "order by s.code, property")
        self._compareDataBases("Sample relationships", openbis_data_source, openbis_harvester, "openbis",
                               "select p.code as parent, c.code as child, t.code as relationship, "
                               + "r.parent_frozen, r.child_frozen "
                               + "from sample_relationships r join relationship_types t on r.relationship_id = t.id "
                               + "join samples p on r.sample_id_parent = p.id "
                               + "join samples c on r.sample_id_child = c.id order by p.code, c.code")
        self._compareDataBases("Data sets", openbis_data_source, openbis_harvester, "openbis",
                               "select d.code as data_set, s.code as sample, e.code as experiment " 
                               + "from data d left join samples s on d.samp_id = s.id "
                               + "left join experiments e on d.expe_id=e.id order by d.code")
        self._compareDataBases("Data set properties", openbis_data_source, openbis_harvester, "openbis", 
                               "select d.code as data_set, '{0}' || t.code as type, "
                               + "case when pt.is_managed_internally then pt.code else '{0}' || pt.code end as property, "
                               + "concat(dp.value, cvt.code, m.code) as value, "
                               + "ur.user_id as registrator, "
                               + "to_char(d.registration_timestamp, 'YYYY-MM-DD HH24:MI:SS') as registration_timestamp, "
                               + "um.user_id as modifier, "
                               + "to_char(d.modification_timestamp, 'YYYY-MM-DD HH24:MI:SS') as modification_timestamp, "
                               + "d.frozen, d.frozen_for_comps, d.frozen_for_conts, d.frozen_for_children, d.frozen_for_parents, "
                               + "d.expe_frozen, d.samp_frozen "
                               + "from data d join data_set_properties dp on dp.ds_id = d.id "
                               + "left join controlled_vocabulary_terms cvt on dp.cvte_id = cvt.id "
                               + "left join materials m on dp.mate_prop_id = m.id "
                               + "join data_set_type_property_types dtpt on dp.dstpt_id = dtpt.id "
                               + "join data_set_types t on dtpt.dsty_id = t.id "
                               + "join property_types pt on dtpt.prty_id = pt.id "
                               + "join persons ur on d.pers_id_registerer = ur.id "
                               + "join persons um on d.pers_id_modifier = um.id "
                               + "order by d.code, property")
        self._compareDataBases("Data set sizes", openbis_data_source, openbis_harvester, "pathinfo",
                               "select d.code as data_set, file_name, size_in_bytes "
                               + "from data_set_files f join data_sets d on f.dase_id=d.id where parent_id is null "
                               + "order by d.code")
        self._compareDataBases("h5ar files as folders", openbis_data_source, openbis_harvester, "pathinfo",
                               "select d.code as data_set, relative_path, size_in_bytes "
                               + "from data_set_files f join data_sets d on f.dase_id=d.id where relative_path like '%.h5ar%' "
                               + "order by d.code, relative_path")
        self._compareDataBases("Data set relationships", openbis_data_source, openbis_harvester, "openbis",
                                "select p.code as parent, c.code as child, t.code as relationship_type, "
                                + "r.parent_frozen, r.child_frozen, r.cont_frozen, r.comp_frozen "
                                + "from data_set_relationships r join data p on r.data_id_parent = p.id "
                                + "join data c on r.data_id_child = c.id "
                                + "join relationship_types t on r.relationship_id = t.id order by p.code, c.code")
        data_source_paths = self._gatherFilePaths(openbis_data_source);
        harvester_paths = self._gatherFilePaths(openbis_harvester);
        self.assertEquals("file service paths", data_source_paths, harvester_paths)
        self._compareFiles(openbis_data_source, openbis_harvester, data_source_paths)
        
    def _compareFiles(self, openbis_data_source, openbis_harvester, file_paths):
        template = "%s/data/file-server/%s"
        data_source_install_path = openbis_data_source.installPath
        harvester_install_path = openbis_harvester.installPath
        for file_path in file_paths:
            data_source_path = template % (data_source_install_path, file_path)
            harvester_path = template % (harvester_install_path, file_path)
            self.assertEquals("file size", os.path.getsize(data_source_path), os.path.getsize(harvester_path))
        
    def _gatherFilePaths(self, openbis):
        paths = []
        self._gatherFilePathsOfTable(paths, openbis, "value", "experiment_properties")
        self._gatherFilePathsOfTable(paths, openbis, "value", "sample_properties")
        self._gatherFilePathsOfTable(paths, openbis, "value", "data_set_properties")
        self._gatherFilePathsOfTable(paths, openbis, "value", "material_properties")
        self._gatherFilePathsOfTable(paths, openbis, "description", "projects")
        self._gatherFilePathsOfTable(paths, openbis, "description", "spaces")
        paths.sort()
        return paths
    
    def _gatherFilePathsOfTable(self, paths, openbis, column, table):
        template = "select array_to_string(regexp_matches(%s, 'file-service/([^\"'']*)', 'g'), ',') from %s"
        result = openbis.queryDatabase("openbis", template % (column, table))
        for row in result:
            for cell in row:
                for path in cell.split(","):
                    paths.append(path)

    def _compareDataBases(self, name, openbis_data_source, openbis_harvester, databaseType, sql):
        expectedContent = openbis_data_source.queryDatabase(databaseType, sql.format(TYPE_PREFIX, ""), showHeaders = True)
        synchedContent = openbis_harvester.queryDatabase(databaseType, sql.format("", TYPE_PREFIX), showHeaders = True)
        self.assertEquals(name, expectedContent, synchedContent)

    def _setupOpenbisDataSource(self):
        self.installOpenbis(instanceName='data_source', technologies=ALL_TECHNOLOGIES)
        openbis_data_source = self.createOpenbisController('data_source', port=DATA_SOURCE_AS_PORT)
        openbis_data_source.setDataStoreServerPort(DATA_SOURCE_DSS_PORT)
        openbis_data_source.setOpenbisPortDataStoreServer(DATA_SOURCE_AS_PORT)
        openbis_data_source.enableProjectSamples()
        openbis_data_source.setDummyAuthentication()
        openbis_data_source.setDataStoreServerProperty("host-address", "https://localhost")
        openbis_data_source.asProperties['max-number-of-sessions-per-user'] = '0'
        openbis_data_source.dssProperties['database.kind'] = openbis_data_source.databaseKind
        openbis_data_source.createTestDatabase('openbis')
        openbis_data_source.createTestDatabase('pathinfo')
        openbis_data_source.createTestDatabase('imaging')
        openbis_data_source.enableCorePlugin('openbis-sync')
        return openbis_data_source

    def _setupOpenbisHarvester(self):
        self.installOpenbis(instanceName='harvester', technologies=ALL_TECHNOLOGIES)
        openbis_harvester = self.createOpenbisController('harvester', port=HARVESTER_AS_PORT)
        openbis_harvester.setDataStoreServerPort(HARVESTER_DSS_PORT)
        openbis_harvester.setOpenbisPortDataStoreServer(HARVESTER_AS_PORT)
        openbis_harvester.enableProjectSamples()
        openbis_harvester.setDummyAuthentication()
        openbis_harvester.setDataStoreServerProperty("host-address", "https://localhost")
        openbis_harvester.asProperties['max-number-of-sessions-per-user'] = '0'
        openbis_harvester.dssProperties['database.kind'] = openbis_harvester.databaseKind
        openbis_harvester.createTestDatabase('openbis')
        openbis_harvester.enableCorePlugin("openbis-sync")
        util.copyFromTo(self.getTemplatesFolder(), openbis_harvester.installPath, "harvester-config.txt")
        return openbis_harvester

    def _waitUntilSyncIsFinished(self, openbis_harvester):
        timeoutPeriod = 5 * 60
        timeoutTime = time.time() + timeoutPeriod
        synclogfiles = None

        while time.time() < timeoutTime:
            synclogfiles = sorted(glob.glob("%s/synch*" % openbis_harvester.installPath))
            if synclogfiles:
                break
            else:
                print ('Waiting for the synchronization log file to appear...')
                time.sleep(1)

        if synclogfiles:
            monitor = util.LogMonitor("%s synchronization" % openbis_harvester.instanceName, synclogfiles[-1], timeOutInMinutes=30)
            monitor.addNotificationCondition(util.RegexCondition('OPERATION.EntitySynchronizer'))
            monitor.waitUntilEvent(util.RegexCondition('Saving the timestamp of sync start to file'), delay = 60)
            time.sleep(60)
        else:
            raise Exception('Synchronization log file not found in %s. Waited for %s seconds.' % (openbis_harvester.installPath, str(timeoutPeriod)))

TestCase(settings, __file__).runTest()
