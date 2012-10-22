from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import MaterialIdentifier
from com.fasterxml.jackson.databind import ObjectMapper 

class RequestHandler:
	"""Abstract superclass for the handlers for concrete requests like ROOT.

	This superclass defines behavior common to all requests."""

	def __init__(parameters, builder):
		self.parameters = parameters
		self.builder = builder
		global searchService
		self.searchService = searchService

	def retrieve_data():
		"""Get the data for the request. Subclass responsibility"""
		pass

	def add_headers():
		"""Configure the headers for this request.

		The possible headers come from the following list:
			PERM_ID : A stable identifier for the object.
			REFCON : Data that is passed unchanged back to the server when a row is modified.
				This can be used by the server to encode whatever it needs in order to
				modify the row.
			CATEGORY : A category identifier for showing the entity. If empty or None, then the
				the entity in this row is not shown in top level navigation views. Such entities
				may appear as children of other entities.
			SUMMARY_HEADER : A short summary of the entity.
			SUMMARY : A potentially longer summary of the entity.
			CHILDREN : The permIds of the children of this entity. Transmitted as JSON.
			IDENTIFIER : An identifier for the object.
			IMAGE_URL : A url for an image associated with this entity. If None or empty, no
				image is shown.
			PROPERTIES : Properties (metadata) that should be displayed for this entity. Transmitted as JSON.

		The relevant headers are determined by the request.
		"""
		pass

	def add_data_rows():
		"""Take the information from the data and put it into the table.
		Subclass responsibility.
		"""
		pass

	def process_request():
		"""Execute the steps necessary to process the request."""
		self.add_headers()
		self.retrieve_data()
		self.add_data_rows()


def add_headers(builder):
	builder.addHeader("PERM_ID")
	builder.addHeader("REFCON")
	builder.addHeader("CATEGORY")
	builder.addHeader("SUMMARY_HEADER")
	builder.addHeader("SUMMARY")
	builder.addHeader("CHILDREN")
	builder.addHeader("IDENTIFIER")
	builder.addHeader("IMAGE_URL")
	builder.addHeader("PROPERTIES")

def image_url_for_compound(material):
	"""Given a material (compound) return the image url"""
	chemblId =  material.getCode()
	return 'https://www.ebi.ac.uk/chemblws/compounds/%s/image' % chemblId


def add_row(builder, entry):
	"""Append a row of data to the table"""
	row = builder.addRow()
	row.setCell("SUMMARY_HEADER", entry.get("SUMMARY_HEADER"))
	row.setCell("SUMMARY", entry.get("SUMMARY"))
	row.setCell("IDENTIFIER", entry.get("IDENTIFIER"))
	row.setCell("PERM_ID", entry.get("PERM_ID"))
	row.setCell("REFCON", entry.get("REFCON"))
	row.setCell("CATEGORY", entry.get("CATEGORY"))
	row.setCell("IMAGE_URL", entry.get("IMAGE_URL"))
	row.setCell("CHILDREN", entry.get("CHILDREN"))
	row.setCell("PROPERTIES", str(entry.get("PROPERTIES")))

def material_to_dict(material):
	material_dict = {}
	material_dict['SUMMARY_HEADER'] = material.getCode()
	material_dict['IDENTIFIER'] = material.getMaterialIdentifier()
	material_dict['PERM_ID'] = material.getMaterialIdentifier()
	refcon = {}
	refcon['entityKind'] = 'MATERIAL'
	refcon['entityType'] = material.getMaterialType()
	material_dict['REFCON'] = ObjectMapper().writeValueAsString(refcon)
	material_dict['CATEGORY'] = material.getMaterialType()
	if material.getMaterialType() == '5HT_COMPOUND':
		material_dict['SUMMARY'] = material.getPropertyValue("FORMULA")
		material_dict['IMAGE_URL'] = image_url_for_compound(material)
	else:
		material_dict['SUMMARY'] = material.getPropertyValue("DESC")
		material_dict['IMAGE_URL'] = ""

	material_dict['CHILDREN'] = ObjectMapper().writeValueAsString([])

	prop_names = ["NAME", "PROT_NAME", "GENE_NAME", "LENGTH", "CHEMBL", "DESC", "FORMULA", "WEIGHT", "SMILES"]
	properties = dict((name, material.getPropertyValue(name)) for name in prop_names if material.getPropertyValue(name) is not None)
	material_dict['PROPERTIES'] = ObjectMapper().writeValueAsString(properties)
	return material_dict

def sample_to_dict(five_ht_sample, material_by_perm_id):
	sample_dict = {}
	sample_dict['SUMMARY_HEADER'] = five_ht_sample.getCode()
	sample_dict['SUMMARY'] = five_ht_sample.getPropertyValue("DESC")
	sample_dict['IDENTIFIER'] = five_ht_sample.getSampleIdentifier()
	sample_dict['PERM_ID'] = five_ht_sample.getPermId()
	refcon = {}
	refcon['entityKind'] = 'SAMPLE'
	refcon['entityType'] = five_ht_sample.getSampleType()
	sample_dict['REFCON'] = ObjectMapper().writeValueAsString(refcon)
	sample_dict['CATEGORY'] = five_ht_sample.getSampleType()
	compound = material_by_perm_id[five_ht_sample.getPropertyValue("COMPOUND")]
	sample_dict['IMAGE_URL'] = image_url_for_compound(compound)

	children = [five_ht_sample.getPropertyValue("TARGET"), five_ht_sample.getPropertyValue("COMPOUND")]
	sample_dict['CHILDREN'] = ObjectMapper().writeValueAsString(children)

	prop_names = ["DESC"]
	properties = dict((name, five_ht_sample.getPropertyValue(name)) for name in prop_names if five_ht_sample.getPropertyValue(name) is not None)
	sample_dict['PROPERTIES'] = ObjectMapper().writeValueAsString(properties)
	# Need to handle the material links as entity links: "TARGET", "COMPOUND"
	return sample_dict

def add_rows(builder, entities):
	"""Take a collection of dictionaries and add a row for each one"""
	for entry in entities:
		add_row(builder, entry)

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

def aggregate(parameters, builder):
	add_headers(builder)

	# Get the data and add a row for each data item
	samples = searchService.searchForSamples("DESC", "*", "5HT_PROBE")
	material_identifiers = gather_materials(samples)
	materials = searchService.listMaterials(material_identifiers)
	material_dict_array = materials_to_dict(materials)
	material_by_perm_id = dict([(material.getMaterialIdentifier(), material) for material in materials])
	add_rows(builder, material_dict_array)
	add_rows(builder, samples_to_dict(samples, material_by_perm_id))

