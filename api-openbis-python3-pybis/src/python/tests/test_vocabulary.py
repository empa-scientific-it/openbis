#   Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
#   
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
import random

import time


def test_create_delete_vocabulary(openbis_instance):
    o=openbis_instance 
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    voc_code = 'test_voc_'+timestamp+"_"+str(random.randint(0,1000))
    
    voc = o.new_vocabulary(
        code = voc_code,
        description = 'description of vocabulary',
        urlTemplate = 'https://ethz.ch',
        terms = [
            { "code": 'term_code1', "label": "term_label1", "description": "term_description1"},
            { "code": 'term_code2', "label": "term_label2", "description": "term_description2"},
            { "code": 'term_code3', "label": "term_label3", "description": "term_description3"}
        ],
        chosenFromList = False
    )
    assert voc.registrationDate is None
    voc.save()
    assert voc is not None
    assert voc.registrationDate is not None
    assert voc.chosenFromList is False
    
    voc_exists = o.get_vocabulary(voc_code)
    assert voc_exists is not None
    assert voc_exists.code == voc_code.upper()

    voc.description = 'description changed'
    voc.chosenFromList = True
    voc.save()

    assert voc.description == 'description changed'
    assert voc.chosenFromList is True

    voc.delete('test on '+str(timestamp))


def test_create_delete_vocabulary_term(openbis_instance):
    o = openbis_instance
    timestamp = time.strftime('%a_%y%m%d_%H%M%S').upper()
    voc_code = 'test_voc_' + timestamp + "_" + str(random.randint(0, 1000))

    voc = o.new_vocabulary(
        code=voc_code,
        description='description of vocabulary',
        urlTemplate='https://ethz.ch',
        terms=[
            {"code": 'term_code1', "label": "term_label1", "description": "term_description1"},
            {"code": 'term_code2', "label": "term_label2", "description": "term_description2"},
            {"code": 'term_code3', "label": "term_label3", "description": "term_description3"}
        ],
        chosenFromList=False
    )
    assert voc.registrationDate is None
    voc.save()
    assert voc is not None
    assert voc.registrationDate is not None
    assert voc.chosenFromList is False

    term = o.get_term('term_code3'.upper(), voc_code.upper())
    term.delete('some_reason')

    saved_vocab = o.get_vocabulary(voc_code.upper())
    terms = saved_vocab.get_terms()
    assert list(terms.df['code']) == ['TERM_CODE1', 'TERM_CODE2']



