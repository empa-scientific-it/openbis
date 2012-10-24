from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

#
# BEGIN Infrastructure
#

def json_encoded_value(coll):
	"""Utility function for converting a list into a json-encoded list"""
	return ObjectMapper().writeValueAsString(coll)

class RequestHandler:
	"""Abstract superclass for the handlers for concrete requests like ROOT.

	This superclass defines behavior common to all requests.

	Subclasses need to implement the method optional_headers(), which returns
	a list of the optional headers they fill out.

	Subclasses should implement retrieve_data to get the data they provide.

	Subclasses should implement add_data_rows. In this method, they should call add_row.
	The method add_row takes a dictionary as an argument. The keys of the dictionary match the
	headers in the result columns. The dictionary should include data for the required columns
	and optional ones they fill.

	"""

	def __init__(self, parameters, builder):
		self.parameters = parameters
		self.builder = builder
		global searchService
		self.searchService = searchService
		self.headers = ['PERM_ID', 'REFCON'] + self.optional_headers()


	def optional_headers(self):
		"""Return a list of optional headers supported by this request. Sublass responsibility.

		See add_headers() for the list of supported headers
		"""
		return []

	def retrieve_data(self):
		"""Get the data for the request. Subclass responsibility"""
		pass

	def add_data_rows(self):
		"""Take the information from the data and put it into the table.
		Subclass responsibility.
		"""
		pass

	def add_headers(self):
		"""Configure the headers for this request.

		The possible headers come from the following list:
			PERM_ID : A stable identifier for the object. (required)
			REFCON : Data that is passed unchanged back to the server when a row is modified.
				This can be used by the server to encode whatever it needs in order to
				modify the row. (required)
			CATEGORY : A category identifier for grouping entities.
			SUMMARY_HEADER : A short summary of the entity.
			SUMMARY : A potentially longer summary of the entity.
			CHILDREN : The permIds of the children of this entity. Transmitted as JSON.
			IDENTIFIER : An identifier for the object.
			IMAGE_URL : A url for an image associated with this entity. If None or empty, no
				image is shown.
			PROPERTIES : Properties (metadata) that should be displayed for this entity. Transmitted as JSON.
			ROOT_LEVEL : True if the entity should be shown on the root level.

		The relevant headers are determined by the request.
		"""
		for header in self.headers:
			self.builder.addHeader(header)

	def add_row(self, entry):
		"""Append a row of data to the table"""
		row = self.builder.addRow()
		for header in self.headers:
			row.setCell(header, str(entry.get(header)))

	def add_rows(self, entities):
		"""Take a collection of dictionaries and add a row for each one"""
		for entry in entities:
			self.add_row(entry)

	def process_request(self):
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.retrieve_data()
		self.add_data_rows()

class AllDataRequestHandler(RequestHandler):
	"""Abstract Handler for the ALLDATA request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "IDENTIFIER", "IMAGE_URL", "PROPERTIES"]

class EmptyDataRequestHandler(RequestHandler):
	"""Return nothing to the caller."""

	def add_data_rows(self):
		pass

class RootRequestHandler(RequestHandler):
	"""Abstract Handler for the ROOT request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "ROOT_LEVEL"]

class DrillRequestHandler(RequestHandler):
	"""Abstract Handler for the DRILL request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN"]

class DetailRequestHandler(RequestHandler):
	"""Abstract Handler for the DETAIL request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "IDENTIFIER", "IMAGE_URL", "PROPERTIES"]

#
# END Infrastructure
#

#
# Helper Methods
# 
def image_url_for_compound(material):
	"""Given a material (compound) return the image url"""
	chemblId =  material.getCode()
	return 'https://www.ebi.ac.uk/chemblws/compounds/%s/image' % chemblId

def material_to_dict(material):
	material_dict = {}
	material_dict['SUMMARY_HEADER'] = material.getCode()
	material_dict['IDENTIFIER'] = material.getMaterialIdentifier()
	material_dict['PERM_ID'] = material.getMaterialIdentifier()
	refcon = {}
	refcon['code'] =  material.getCode()
	refcon['entityKind'] = 'MATERIAL'
	refcon['entityType'] = material.getMaterialType()
	material_dict['REFCON'] = json_encoded_value(refcon)
	material_dict['CATEGORY'] = material.getMaterialType()
	if material.getMaterialType() == '5HT_COMPOUND':
		material_dict['SUMMARY'] = material.getPropertyValue("FORMULA")
		material_dict['IMAGE_URL'] = image_url_for_compound(material)
	else:
		material_dict['SUMMARY'] = material.getPropertyValue("DESC")
		material_dict['IMAGE_URL'] = ""
		material_dict['ROOT_LEVEL'] = True

	material_dict['CHILDREN'] = json_encoded_value([])

	prop_names = ["NAME", "PROT_NAME", "GENE_NAME", "LENGTH", "CHEMBL", "DESC", "FORMULA", "WEIGHT", "SMILES"]
	properties = dict((name, material.getPropertyValue(name)) for name in prop_names if material.getPropertyValue(name) is not None)
	material_dict['PROPERTIES'] = json_encoded_value(properties)
	return material_dict

def sample_to_dict(five_ht_sample, material_by_perm_id):
	sample_dict = {}
	sample_dict['SUMMARY_HEADER'] = five_ht_sample.getCode()
	sample_dict['SUMMARY'] = five_ht_sample.getPropertyValue("DESC")
	sample_dict['IDENTIFIER'] = five_ht_sample.getSampleIdentifier()
	sample_dict['PERM_ID'] = five_ht_sample.getPermId()
	refcon = {}
	refcon['code'] =  five_ht_sample.getCode()
	refcon['entityKind'] = 'SAMPLE'
	refcon['entityType'] = five_ht_sample.getSampleType()
	sample_dict['REFCON'] = json_encoded_value(refcon)
	sample_dict['CATEGORY'] = five_ht_sample.getSampleType()
	compound = material_by_perm_id[five_ht_sample.getPropertyValue("COMPOUND")]
	sample_dict['IMAGE_URL'] = image_url_for_compound(compound)

	children = [five_ht_sample.getPropertyValue("TARGET"), five_ht_sample.getPropertyValue("COMPOUND")]
	sample_dict['CHILDREN'] = json_encoded_value(children)

	prop_names = ["DESC"]
	properties = dict((name, five_ht_sample.getPropertyValue(name)) for name in prop_names if five_ht_sample.getPropertyValue(name) is not None)
	sample_dict['PROPERTIES'] = json_encoded_value(properties)
	sample_dict['ROOT_LEVEL'] = True
	# Need to handle the material links as entity links: "TARGET", "COMPOUND"
	return sample_dict


def add_material_to_collection(code, collection):
	material_id = MaterialIdentifier.tryParseIdentifier(code)
	collection.addIdentifier(material_id.getTypeCode(), material_id.getCode())

def gather_materials(five_ht_samples):
	material_identifiers = MaterialIdentifierCollection()
	for sample in five_ht_samples:
		add_material_to_collection(sample.getPropertyValue("TARGET"), material_identifiers)
		add_material_to_collection(sample.getPropertyValue("COMPOUND"), material_identifiers)
	return material_identifiers

def materials_to_dict(materials):
	result = [material_to_dict(material) for material in materials]
	return result

def samples_to_dict(samples, material_by_perm_id):
	result = [sample_to_dict(sample, material_by_perm_id) for sample in samples]
	return result

def retrieve_samples(sample_perm_ids_and_ref_cons):
	sc = SearchCriteria()
	sc.setOperator(sc.SearchOperator.MATCH_ANY_CLAUSES)
	for sample in sample_perm_ids_and_ref_cons:
		code = sample['REFCON']['code']	
		sc.addMatchClause(sc.MatchClause.createAttributeMatch(sc.MatchClauseAttribute.CODE, code))
	return searchService.searchForSamples(sc)

class ExampleAllDataRequestHandler(AllDataRequestHandler):
	"""Handler for the ALLDATA request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
		self.samples = self.searchService.searchForSamples("DESC", "*", "5HT_PROBE")
		material_identifiers = gather_materials(self.samples)
		materials = self.searchService.listMaterials(material_identifiers)
		self.material_dict_array = materials_to_dict(materials)
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id))

class ExampleRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
		self.samples = self.searchService.searchForSamples("DESC", "*", "5HT_PROBE")
		material_identifiers = gather_materials(self.samples)
		materials = self.searchService.listMaterials(material_identifiers)
		self.material_dict_array = materials_to_dict(materials)
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id))

class ExampleDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

	def retrieve_data(self):
		# Drill only happens on samples
		drill_samples = self.parameters['entities']

		self.samples = retrieve_samples(drill_samples)
		material_identifiers = gather_materials(self.samples)
		materials = self.searchService.listMaterials(material_identifiers)
		self.material_dict_array = materials_to_dict(materials)
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id))

class ExampleDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
		entities = self.parameters['entities']
		detail_samples = [entity for entity in entities if 'SAMPLE' == entity['REFCON']['entityKind']]
		detail_materials = [entity for entity in entities if 'MATERIAL' == entity['REFCON']['entityKind']]

		self.samples = retrieve_samples(detail_samples)

		# We need to get data for materials explicitly requested as well as those associated
		# with the samples we have retrieved
		material_identifiers = gather_materials(self.samples)
		for detail_material in detail_materials:
			add_material_to_collection(detail_material, material_identifiers)

		materials = self.searchService.listMaterials(material_identifiers)
		materials_to_return = [material for material in materials if material.getMaterialIdentifier() in detail_materials]
		# We internally need more materials, but only return those explicitly asked for
		self.material_dict_array = materials_to_dict(materials_to_return)
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id))

def aggregate(parameters, builder):
	request_key = parameters.get('requestKey')
	if 'ROOT' == request_key:
		handler = ExampleRootRequestHandler(parameters, builder)
	elif 'DRILL' == request_key:
		handler = ExampleDrillRequestHandler(parameters, builder)
	elif 'DETAIL' == request_key:
		handler = ExampleDetailRequestHandler(parameters, builder)
	else:		
		handler = EmptyDataRequestHandler(parameters, builder)
	handler.process_request()		
