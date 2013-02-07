from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria, SearchSubCriteria

from datetime import datetime

#
# BEGIN Infrastructure
#

def json_encoded_value(coll):
	"""Utility function for converting a list into a json-encoded list"""
	return ObjectMapper().writeValueAsString(coll)

def json_empty_list():
  """Utility function to return an json-encoded empty list"""
  return json_encoded_value([])

def json_empty_dict():
  """Utility function to return an json-encoded empty dictionary"""
  return json_encoded_value({})

class RequestHandler(object):
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
		
	def entities_parameter(self):
	  """A helper method to get the value of the entities parameter. Returns an empty list if no entities were specified"""
	  entities = self.parameters.get('entities')
	  if entities is None:
	    return []
	  return entities


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
			IMAGES : A map with keys coming from the set 'MARQUEE', 'TILED'. The values are image specs or lists of image specs.
				Image specs are maps with the keys: 'URL' (a URL for the iamge) or 'DATA'. The data key contains a map that
				includes the image data and may include some image metadata as well. This format has not yet been specified.
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
			value = entry.get(header)
			if value is not None:
				row.setCell(header, value)
			else:
				row.setCell(header, "")

	def add_rows(self, entities):
		"""Take a collection of dictionaries and add a row for each one"""
		for entry in entities:
			self.add_row(entry)

	def process_request(self):
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.retrieve_data()
		self.add_data_rows()

class ClientPreferencesRequestHandler(object):
	"""Abstract superclass for the handlers for CLIENT_PREFS request.

	This request has a slightly different structure, since it does not return entities.

	Subclasses should override the preferences_dict method to return the preferences dictionary. The superclass
	implements this method with the default values for the standard keys.
	"""

	def __init__(self, parameters, builder):
		self.parameters = parameters
		self.builder = builder
		self.headers = ['KEY', 'VALUE']

	def preferences_dict(self):
		"""The dictionary containing the value for the client preferences. 

		Subclasses may override if they want to change any of the values. The best way to override is to call
		default_preferences_dict then modify/extend the resulting dictionary"""
		return self.default_preferences_dict()

	def default_preferences_dict(self):
		"""The dictionary containing the standard keys and and default values for those keys"""
		prefs = { 
			# The refresh interval is a value in seconds
			'ROOT_SET_REFRESH_INTERVAL' : 60 * 30 
		}
		return prefs

	def add_data_rows(self):
		"""Take the information from the preferences dict and put it into the table."""
		prefs = self.preferences_dict()
		for key in prefs:
			row = self.builder.addRow()
			row.setCell('KEY', key)
			row.setCell('VALUE', prefs[key])

	def add_headers(self):
		"""Configure the headers for this request.

		For preference request, the headers are 
			KEY : The key of the preference.
			VALUE : The value of the preference.
		"""
		for header in self.headers:
			self.builder.addHeader(header)

	def process_request(self):
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.add_data_rows()

class AllDataRequestHandler(RequestHandler):
	"""Abstract Handler for the ALLDATA request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "CHILDREN", "IDENTIFIER", "IMAGES", "PROPERTIES"]

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
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "IDENTIFIER", "IMAGES", "PROPERTIES"]

class NavigationRequestHandler(RequestHandler):
	"""Abstract Handler for the NAVIGATION request."""

	def optional_headers(self):
		return ["CATEGORY", "SUMMARY_HEADER", "SUMMARY", "ROOT_LEVEL"]

#
# END Infrastructure
#

DSS_DOWNLOAD_URL = 'https://localhost:8444/datastore_server/'

#
# Helper Methods
# 
def image_url_for_compound(material):
	"""Given a material (compound) return the image url"""
	chemblId =  material.getCode()
	return 'https://www.ebi.ac.uk/chemblws/compounds/%s/image' % chemblId

def properties_for_entity(entity, property_definitions, prop_names_set):
	"""Extract the properties, in the correct order, for the entity. Restricting them to those in the prop_names_set, if it is non-empty"""
	properties = []
	check_prop_names_set = len(prop_names_set) > 0
	for propdef in property_definitions:
		propcode = propdef.getPropertyTypeCode()
		# Only include the properties we explicitly specify
		if check_prop_names_set and propcode not in prop_names_set:
			continue
		value = entity.getPropertyValue(propcode)
		prop = {'key' : propcode, 'label' : propdef.getPropertyTypeLabel(), 'value' : value }
		properties.append(prop)
	return properties

def marquee_image_spec_for_url(image_url):
	return { 'MARQUEE' : { 'URL' : image_url } }

def navigation_dict(name, children):
	"""Create a navigational entity"""
	navigation_dict = {}
	navigation_dict['SUMMARY_HEADER'] = name
	navigation_dict['SUMMARY'] = None
	navigation_dict['IDENTIFIER'] = None
	navigation_dict['PERM_ID'] = name.upper()
	refcon = {}
	refcon['code'] =  name.upper()
	refcon['entityKind'] = 'NAVIGATION'
	refcon['entityType'] = 'NAVIGATION'
	navigation_dict['REFCON'] = json_encoded_value(refcon)
	navigation_dict['CATEGORY'] = 'Navigation'

	navigation_dict['CHILDREN'] = json_encoded_value(children)

	properties = dict()
	navigation_dict['PROPERTIES'] = json_encoded_value(properties)
	navigation_dict['ROOT_LEVEL'] = True
	# Need to handle the material links as entity links: "TARGET", "COMPOUND"
	return navigation_dict

def material_to_dict(material, material_type_properties_definitions):
	material_dict = {}
	material_dict['SUMMARY_HEADER'] = material.getCode()
	material_dict['IDENTIFIER'] = material.getMaterialIdentifier()
	material_dict['PERM_ID'] = material.getMaterialIdentifier()
	refcon = {}
	refcon['code'] =  material.getCode()
	refcon['identifier'] = material.getMaterialIdentifier()
	refcon['entityKind'] = 'MATERIAL'
	refcon['entityType'] = material.getMaterialType()
	material_dict['REFCON'] = json_encoded_value(refcon)
	material_dict['CATEGORY'] = material.getMaterialType()
	if material.getMaterialType() == '5HT_COMPOUND':
		material_dict['SUMMARY'] = material.getPropertyValue("FORMULA")
		material_dict['IMAGES'] = json_encoded_value(marquee_image_spec_for_url(image_url_for_compound(material)))
	else:
		material_dict['SUMMARY'] = material.getPropertyValue("DESC")
		material_dict['IMAGES'] = json_empty_dict()
		material_dict['ROOT_LEVEL'] = None

	material_dict['CHILDREN'] = json_empty_list()

	prop_names = set(["NAME", "PROT_NAME", "GENE_NAME", "LENGTH", "CHEMBL", "DESC", "FORMULA", "WEIGHT", "SMILES"])
	property_definitions = material_type_properties_definitions.get(material.getMaterialType(), [])
	properties = properties_for_entity(material, property_definitions, prop_names)	
	properties.append({'key' : 'VERY_LONG_PROPERTY_NAME', 'label' : 'Very Long Property Name', 'value' : "This is a very long text that should span multiple lines to see if this thing works, you know, the thing that causes the other thing to place text on multiple lines and stuff like that, etc., etc., so on and so forth."})
	properties.append({'key' : 'UNICODE_PROPERTY', 'label' : 'Unicode Property', 'value' : u'A pr\u00F6perty w\u00EFth accents.'})
	material_dict['PROPERTIES'] = json_encoded_value(properties)
	return material_dict

def sample_to_dict(five_ht_sample, material_by_perm_id, data_sets, sample_type_properties_definitions):
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
	sample_dict['IMAGES'] = json_encoded_value(marquee_image_spec_for_url(image_url_for_sample(five_ht_sample, data_sets, compound)))

	children = [five_ht_sample.getPropertyValue("TARGET"), five_ht_sample.getPropertyValue("COMPOUND")]
	sample_dict['CHILDREN'] = json_encoded_value(children)

	prop_names = set(["DESC"])
	property_definitions = sample_type_properties_definitions.get(five_ht_sample.getSampleType(), [])
	properties = properties_for_entity(five_ht_sample, property_definitions, prop_names)
	properties.append({'key' : 'TIMESTAMP', 'label' : 'Timestamp', 'value' : datetime.today().strftime('%Y-%m-%d %H:%M:%S')})
	sample_dict['PROPERTIES'] = json_encoded_value(properties)
	sample_dict['ROOT_LEVEL'] = None
	# Need to handle the material links as entity links: "TARGET", "COMPOUND"
	return sample_dict

def image_url_for_sample(five_ht_sample, data_sets, compound):
	image_data_set = None
	for data_set in data_sets:
	    if data_set.getExperiment().getExperimentIdentifier() == five_ht_sample.getExperiment().getExperimentIdentifier():
	        image_data_set = data_set
	        break
	if image_data_set is None:
		return image_url_for_compound(compound)
	image_url = DSS_DOWNLOAD_URL + image_data_set.getDataSetCode() + '/original/images/'
	image_url = image_url + five_ht_sample.getCode() + '.jpg'
	return image_url


def add_material_to_collection(code, collection):
	material_id = MaterialIdentifier.tryParseIdentifier(code)
	collection.addIdentifier(material_id.getTypeCode(), material_id.getCode())

def gather_materials(five_ht_samples):
	material_identifiers = MaterialIdentifierCollection()
	for sample in five_ht_samples:
		add_material_to_collection(sample.getPropertyValue("TARGET"), material_identifiers)
		add_material_to_collection(sample.getPropertyValue("COMPOUND"), material_identifiers)
	return material_identifiers

def materials_to_dict(materials, material_type_properties_definitions):
	result = [material_to_dict(material, material_type_properties_definitions) for material in materials]
	return result

def samples_to_dict(samples, material_by_perm_id, sample_type_properties_definitions):
	data_sets = retrieve_data_sets_for_samples(samples)
	result = [sample_to_dict(sample, material_by_perm_id, data_sets, sample_type_properties_definitions) for sample in samples]
	return result

def retrieve_samples(sample_perm_ids_and_ref_cons):
	if not sample_perm_ids_and_ref_cons:
		return []
	sc = SearchCriteria()
	sc.setOperator(sc.SearchOperator.MATCH_ANY_CLAUSES)
	for sample in sample_perm_ids_and_ref_cons:
		code = sample['REFCON']['code']	
		sc.addMatchClause(sc.MatchClause.createAttributeMatch(sc.MatchClauseAttribute.CODE, code))
	return searchService.searchForSamples(sc)

def gather_entity_types(entity_perm_ids_and_ref_cons):
	"""Return a set containing all specified entity types."""
	entity_types = set()
	for entity in entity_perm_ids_and_ref_cons:
		entity_type = entity['REFCON']['entityType']
		entity_types.add(entity_type)
	return entity_types

def retrieve_entity_type_properties_definitions(entity_perm_ids_and_ref_cons, search_func):
	"""Return the property definitions for each of the referenced entity types.

	The definitions are sorted according to display order.
	"""
	if not entity_perm_ids_and_ref_cons:
		return []
	entityTypes = gather_entity_types(entity_perm_ids_and_ref_cons)
	definitionsByType = {}
	for entityType in entityTypes:
		definitions = list(search_func(entityType))
		definitions.sort(lambda x, y: cmp(x.getPositionInForms(), y.getPositionInForms()))
		definitionsByType[entityType] = definitions
	return definitionsByType

def retrieve_sample_type_properties_definitions(sample_perm_ids_and_ref_cons):
	"""Return the property definitions for each of the referenced sample types.

	The definitions are sorted according to display order.
	"""
	return retrieve_entity_type_properties_definitions(sample_perm_ids_and_ref_cons, searchService.listPropertiesDefinitionsForSampleType)

def retrieve_material_type_properties_definitions(material_perm_ids_and_ref_cons):
	"""Return the property definitions for each of the referenced material types.

	The definitions are sorted according to display order.
	"""
	return retrieve_entity_type_properties_definitions(material_perm_ids_and_ref_cons, searchService.listPropertiesDefinitionsForMaterialType)

def retrieve_data_sets_for_samples(samples):
	experiment_codes = set()
	for sample in samples:
		if sample.getExperiment() is None:
			continue
		tokens = sample.getExperiment().getExperimentIdentifier().split('/')
		experiment_code = tokens[len(tokens) - 1]
		experiment_codes.add(experiment_code)
	sc = SearchCriteria()
	sc.setOperator(sc.SearchOperator.MATCH_ANY_CLAUSES)
	for code in experiment_codes:
		sc.addMatchClause(sc.MatchClause.createAttributeMatch(sc.MatchClauseAttribute.CODE, code))
	data_set_sc = SearchCriteria()
	data_set_sc.addMatchClause(data_set_sc.MatchClause.createAttributeMatch(data_set_sc.MatchClauseAttribute.TYPE, "5HT_IMAGE"))
	data_set_sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(sc))
	return searchService.searchForDataSets(data_set_sc)

class ExampleClientPreferencesRequestHandler(ClientPreferencesRequestHandler):
	"""Handler for the CLIENT_PREFS request."""


class ExampleRootRequestHandler(RootRequestHandler):
	"""Handler for the ROOT request."""

	def entities_parameter(self):
		entities = super(ExampleRootRequestHandler, self).entities_parameter()
		if len(entities) == 0:
			materials_nav = navigation_dict('Targets and Compounds', [])
			probe_nav = navigation_dict('Probes', [])
			return [materials_nav, probe_nav]
		return entities

	def retrieve_data(self):
		# Check which navigational entities are being requested here
		nav_entities = self.entities_parameter()
		nav_perm_ids = [entity['PERM_ID'] for entity in nav_entities]

		# Get the data and add a row for each data item
		self.samples = self.searchService.searchForSamples("DESC", "*", "5HT_PROBE")
		material_identifiers = gather_materials(self.samples)
		materials = self.searchService.listMaterials(material_identifiers)
		self.material_dict_array = materials_to_dict(materials, {})
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		nav_entities = self.entities_parameter()
		nav_perm_ids = [entity['PERM_ID'] for entity in nav_entities]

		if 'TARGETS AND COMPOUNDS' in nav_perm_ids:
			children = [material_dict['PERM_ID'] for material_dict in self.material_dict_array]
			materials_nav = navigation_dict('Targets and Compounds', children)
			self.add_rows([materials_nav])
			self.add_rows(self.material_dict_array)

		if 'PROBES' in nav_perm_ids:
			children = [sample.getPermId() for sample in self.samples]
			probe_nav = navigation_dict('Probes', children)
			self.add_rows([probe_nav])
			self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id, {}))

class ExampleDrillRequestHandler(DrillRequestHandler):
	"""Handler for the DRILL request."""

	def retrieve_data(self):
		# Drill only happens on samples
		drill_samples = self.entities_parameter()

		self.samples = retrieve_samples(drill_samples)
		material_identifiers = gather_materials(self.samples)
		materials = self.searchService.listMaterials(material_identifiers)
		self.material_dict_array = materials_to_dict(materials, {})
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id, {}))

class ExampleDetailRequestHandler(DetailRequestHandler):
	"""Handler for the DETAIL request."""

	def retrieve_data(self):
		# Get the data and add a row for each data item
		entities = self.entities_parameter()
		detail_samples = [entity for entity in entities if 'SAMPLE' == entity['REFCON']['entityKind']]
		detail_materials = [entity for entity in entities if 'MATERIAL' == entity['REFCON']['entityKind']]

		self.samples = retrieve_samples(detail_samples)
		self.sample_type_properties_definitions = retrieve_sample_type_properties_definitions(detail_samples)

		# We need to get data for materials explicitly requested as well as those associated
		# with the samples we have retrieved
		detail_material_identifiers = set([detail_material['REFCON']['identifier'] for detail_material in detail_materials])
		material_identifiers = gather_materials(self.samples)
		for identifier in detail_material_identifiers:
			add_material_to_collection(identifier, material_identifiers)

		materials = self.searchService.listMaterials(material_identifiers)
		materials_to_return = [material for material in materials if material.getMaterialIdentifier() in detail_material_identifiers]
		self.material_type_properties_definitions = retrieve_material_type_properties_definitions(detail_materials)
		# We internally need more materials, but only return those explicitly asked for
		self.material_dict_array = materials_to_dict(materials_to_return, self.material_type_properties_definitions)
		self.material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])

	def add_data_rows(self):
		self.add_rows(self.material_dict_array)
		self.add_rows(samples_to_dict(self.samples, self.material_by_perm_id, self.sample_type_properties_definitions))

class ExampleNavigationRequestHandler(NavigationRequestHandler):
	"""Handler for the NAVIGATION request"""
	def add_data_rows(self):
		materials_nav = navigation_dict('Targets and Compounds', [])
		probe_nav = navigation_dict('Probes', [])
		self.add_rows([materials_nav, probe_nav])

class TestingNavigationRequestHandler(ExampleNavigationRequestHandler):
	"""A version of the NAVIGATION request handler designed for testing"""

	def add_data_rows(self):
		hidden_entities = self.parameters.get("HIDE")
		if hidden_entities is None:
			hidden_entities = []
		hidden_perm_ids = set([entity["PERM_ID"] for entity in hidden_entities])

		if 'TARGETS AND COMPOUNDS' not in hidden_perm_ids:
			materials_nav = navigation_dict('Targets and Compounds', [])
			self.add_rows([materials_nav])
		if 'PROBES' not in hidden_perm_ids:
			probe_nav = navigation_dict('Probes', [])
			self.add_rows([probe_nav])

def aggregate(parameters, builder):
	request_key = parameters.get('requestKey')
	if 'CLIENT_PREFS' == request_key:
		handler = ExampleClientPreferencesRequestHandler(parameters, builder)
	elif 'NAVIGATION' == request_key:
		handler = TestingNavigationRequestHandler(parameters, builder)
	elif 'ROOT' == request_key:
		handler = ExampleRootRequestHandler(parameters, builder)
	elif 'DRILL' == request_key:
		handler = ExampleDrillRequestHandler(parameters, builder)
	elif 'DETAIL' == request_key:
		handler = ExampleDetailRequestHandler(parameters, builder)
	else:
		handler = EmptyDataRequestHandler(parameters, builder)
	handler.process_request()		
