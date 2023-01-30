#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import sys

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import SearchOperator
from ch.systemsx.cisd.openbis.jstest.report import V3APIReport

reload(sys)
sys.setdefaultencoding('UTF8')

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	if method is None:
		sample = findSample(tr)

		dataSetType = parameters.get("dataSetType")
		if dataSetType is None:
			dataSetType = "ALIGNMENT"

		dataSet = createDataSet(tr, sample, dataSetType)
		
		tableBuilder.addHeader("DATA_SET_CODE")
		row = tableBuilder.addRow()
		row.setCell("DATA_SET_CODE", dataSet.getDataSetCode())
	elif method == "getV3APIReport":
		tableBuilder.addHeader("STATUS")
		tableBuilder.addHeader("RESULT")
		row = tableBuilder.addRow()
		report = V3APIReport().getReport()
		if report is None:
			report = "";
			row.setCell("STATUS", "FAILED")
		else:
			row.setCell("STATUS", "SUCCESS")
		row.setCell("RESULT", report)
	elif method == "test":
		tableBuilder.addHeader("key")
		tableBuilder.addHeader("value")
		for entry in parameters.entrySet():
			row = tableBuilder.addRow()
			row.setCell("key", entry.key)
			row.setCell("value", entry.value)
	elif method == "getEmailsWith":
		tableBuilder.addHeader("to")
		tableBuilder.addHeader("subject")
		tableBuilder.addHeader("content")
		tableBuilder.addHeader("full-content")
		textSnippet = parameters.get("text-snippet")
		emails = V3APIReport().getEmailsWith(textSnippet if textSnippet is not None else "")
		for email in emails:
			row = tableBuilder.addRow()
			row.setCell("to", email.getTo())
			row.setCell("subject", email.getSubject())
			row.setCell("content", email.getContent())
			row.setCell("full-content", email.getFullContent())

def findSample(tr):
	criteria = SearchCriteria()
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.PERM_ID, "20130412140147736-21")) # /PLATONIC/PLATE-2
	samples = tr.getSearchService().searchForSamples(criteria)
	return samples[0]
	
def createDataSet(tr, sample, dataSetType):
	dataSet = tr.createNewDataSet(dataSetType)
	dataSet.setSample(sample)
	tr.createNewFile(dataSet, "test")
	return dataSet
