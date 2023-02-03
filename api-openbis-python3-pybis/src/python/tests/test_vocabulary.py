import json
import random
import re

import pytest
import time


def test_create_delete_vocabulay_terms(openbis_instance):
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
