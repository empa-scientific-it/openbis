#!/usr/bin/python

from string import zfill, split
from functools import partial
import os, time

id_counter = 277723
def int():
        global id_counter
        id_counter = id_counter + 1
        return id_counter

def code():
	return zfill(str(int()), 16)
	

def null():
	return "\\N"

def now():
	return time.strftime("%Y-%m-%d %H:%M:%S.00000+01", time.localtime())

def false():
	return "f"

def true():
	return "t"

def association(entity):
	return partial(lambda : entity.id)

def url():
	return "http://foo.bar/"

def token():
	return "ABC123"

random_string = code

def rndstring(amount):
	return zfill(str(int()), amount)

load_order = []
files = dict()

def print_to_files():
	global all_entities, load_order, files
	for entity in all_entities:
		if files.has_key(entity.tablename):
			file = files[entity.tablename]
		else:
			file = open("./"+entity.tablename + ".load", "w")
			files[entity.tablename] = file
			load_order.append((entity.tablename, file))

		file.write(str(entity)+"\n")
		all_entities = []

def end_print():
	global load_order
	script = open("load.sh", "w")
	for tablename, file in load_order:
		file.close()
		path = os.path.abspath("./"+tablename+".load")
		script.write("psql -U postgres -d openbis_dev -c \"COPY "+tablename+" FROM '"+path+"' (DELIMITER '|');\"\n")

	script.close()
	

all_entities = []
class Entity(object):
	def __init__(self, *fields):
		global all_entities
		self.fields = fields
		for field in self.fields:
			self.__dict__[field[0]] = field[1]()
		all_entities.append(self)

	def __str__(self):
		line = ""
		for field in self.fields:
			line = line + "|" +  str(self.__dict__[field[0]]) 

		# Test here that the entity does not contain other fields than names of fields and "field". If it does => exception!
		return line[1:]


class DatabaseInstance:
	def __init__(self):
		self.id = 1

class FileFormatType:
	def __init__(self):
		self.id = 2

class LocatorType:
	def __init__(self):
		self.id = 1

class ControlledVocabularyTerm:
	def __init__(self):
		self.id = 1

class DataType:
	def __init__(self):
		self.id = 2

class PropertyType(Entity):
	def __init__(self):
		super(PropertyType, self).__init__(
			("id", int),
			("code", code),
			("description", lambda : "description" + str(int())),
			("label", lambda : "label" + str(int())),
			("daty_id", association(data_type)),
			("registration_timestamp", now),
			("pers_id_registerer", association(person)),
			("covo_id", null),
			("is_managed_internally", true),
			("is_internal_namespace", true),
			("dbin_id", association(database_instance)),
			("maty_prop_id", null),
			("schema", null),
			("transformation", null))
		self.tablename = "property_types"


class Person(Entity):
	def __init__(self):
		super(Person, self).__init__(
			("id", int),
			("first_name", random_string),
			("last_name", random_string),
			("user_id", lambda : "admin"),
			("email", random_string),
			("dbin_id", association(database_instance)),
			("space_id", null),
			("registration_timestamp", now),
			("per_id_registerer", null),
			("display_settings", null))
		self.tablename = "persons"
		RoleAssignment(self)
		
class RoleAssignment(Entity):
	def __init__(self, person):
		super(RoleAssignment, self).__init__(
			("id", int),
			("role_code", lambda : "ADMIN"),
			("space_id", null),
			("dbin_id", association(database_instance)),
			("pers_id_grantee", association(person)),
			("ag_id_grantee", null),
			("pers_id_registerer", association(person)),
			("registration_timestamp", now))
		self.tablename = "role_assignments"


class Space(Entity):
	def __init__(self):
		super(Space, self).__init__(
			("id", int),
			("code", code),
			("dbin_id", association(database_instance)),
			("description", null),
			("registration_timestamp", now),
			("pers_id_registerer", association(person)))
		self.tablename = "spaces"

class DataSetType(Entity):
	def __init__(self):
		super(DataSetType, self).__init__(
			("id", int), 
			("code", code),
			("description", null), 
			("dbin_id", association(database_instance)),
			("modification_timestamp", now),
			("main_db_pattern", null),
			("main_ds_path", null),
			("is_container,", false))
		self.tablename = "data_set_types"

class DataStore(Entity):
	def __init__(self):
		super(DataStore, self).__init__(
			("id", int),
			("dbin_id", association(database_instance)),
			("code", code),
			("download_url", url),
			("remote_url", url),
			("session_token", token),
			("registration_timestamp", now),
			("modification_timestamp", now),
			("is_archiver_configured", false))
		self.tablename = "data_stores"

class Project(Entity):
	def __init__(self):
		super(Project, self).__init__(
			("id", int),
			("code", code),
			("space_id", association(space)),
			("pers_id_leader", null),
			("description", null),
			("pers_id_registerer", association(person)),
			("registration_timestamp", now),
			("modification_timestamp", now))	
		self.tablename = "projects"

class ExperimentType(Entity):
	def __init__(self):
		super(ExperimentType, self).__init__(
			("id", int),
			("code", code),
			("descrption", null),
			("dbin_id", association(database_instance)),
			("modification_timestamp", null))
		self.tablename = "experiment_types"

class Experiment(Entity):
	def __init__(self):
		super(Experiment, self).__init__(
			("id", int),
			("perm_id", code),
			("code", code),
			("exty_id", association(experiment_type)),
			("pers_id_registerer", association(person)),
			("registration_timestamp", now),
			("modification_timestamp", now),
			("proj_id", association(project)),
			("del_id", null),
			("is_public", true))
		self.tablename = "experiments_all"

class Data(Entity):
	def __init__(self):
		number = int()
		idf = lambda : number
		super(Data, self).__init__(
			("id", idf),
			("code", code),
			("dsty_id", association(data_set_type)),
			("dast_id", association(data_store)),
			("expe_id", association(experiment)),
			("data_producer_code", null),
			("production_timestamp", null),
			("samp_id", null),
			("registration_timestamp", now),
			("pers_id_registerer", null),
			("is_placeholder", false),
			("is_valid", true),
			("modification_timestamp", now),
			("is_derived", false),
			("ctnr_order", null),
			("ctnr_id", null),
			("del_id", null))
		self.tablename = "data_all"
		ExternalData(idf)

class ExternalData(Entity):
	def __init__(self, idf):
		super(ExternalData, self).__init__(
			("id", idf),
			("share_id", null),
			("size", null),
			("location", random_string),
			("ffty_id", association(file_format_type)),
			("loty_id", association(locator_type)),
			("cvte_id_stor_format", association(controlled_vocabulary_term)),
			("is_complete", lambda : "T"),
			("cvte_id_store", null),
			("status", lambda : "AVAILABLE"),
			("present_in_archive", true),
			("speed_hint", lambda : -50),
			("storage_confirmation", true))
		self.tablename = "external_data"

class DataSetProperty(Entity):
	def __init__(self):
		super(DataSetProperty, self).__init__(
			("id", int),
			("ds_id", null),
			("dstpt_id", association(data_set_type_property_type)),
			("value", null),
			("cvte_id", null),
			("mate_prop_id", null),
			("pers_id_registerer", association(person)),
			("registration_timestamp", now),
			("pers_id_author", association(person)),
			("modification_timestamp", now))
		self.tablename = "data_set_properties"

class DataSetTypePropertyType(Entity):
	def __init__(self):
		super(DataSetTypePropertyType, self).__init__(
			("id", int),
			("dsty_id", association(data_set_type)),
			("prty_id", association(property_type)),
			("is_mandatory", false),
			("is_managed_internally", true),
			("pers_id_registerer", association(person)),
			("registration_timestamp", now),
			("ordinal", lambda : 1),
			("section", null),
			("script_id", null),
			("is_shown_edit", true))
		self.tablename = "data_set_type_property_types"
			

database_instance = DatabaseInstance()
data_type = DataType()
file_format_type = FileFormatType()
controlled_vocabulary_term = ControlledVocabularyTerm()
locator_type = LocatorType()
person = Person()
property_type = PropertyType()
space = Space()
data_store = DataStore()
data_set_type = DataSetType()
data_set_type_property_type = DataSetTypePropertyType()
project = Project()
experiment_type = ExperimentType()
experiment = Experiment()

dstpt = []
for i in range(100):
	p = PropertyType()
	d = DataSetTypePropertyType()
	d.prty_id = p.id
	dstpt.append(d)


def create_data(experiment_code, entity_count, property_profiles):
	experiment = Experiment()
	experiment.code = experiment_code
	for i in range(entity_count):
		d = Data()
		d.expe_id = experiment.id
		x = 0
		for count, size in property_profiles:
			for j in range(count):
				prop = DataSetProperty()
				prop.ds_id = d.id
				prop.dstpt_id = dstpt[x].id
				prop.value = rndstring(size)
				x = x+1
		print_to_files()

small = [(5,30)]
medium = [(16,30), (4,150)]
large = [(40,30), (6,150), (4,1500)]

create_data("10000_small", 10000, small)
create_data("10000_medium", 10000, medium)
create_data("10000_large", 10000, large)

create_data("100000_small", 100000, small)
create_data("100000_medium", 100000, medium)
create_data("100000_large", 100000, large)

create_data("500000_small", 500000, small)
create_data("500000_medium", 500000, medium)
create_data("500000_large", 500000, large)

end_print()
