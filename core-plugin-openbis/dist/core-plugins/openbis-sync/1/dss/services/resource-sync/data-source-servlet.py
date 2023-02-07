''' TO-DO look at why the sample with the same perm_id as project or experiment is not appearing'''
from java.text import SimpleDateFormat
from java.util import Locale, TimeZone
from java.io import PrintWriter

from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset import DataSetKind
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project import Project

from java.lang import UnsupportedOperationException
from java.util import Date

from xml.etree import ElementTree as ET
from xml.etree.ElementTree import Element, SubElement
#===============================================================================
import os.path
import sys
import errno
import time

def package_folder(module):
    return os.path.dirname(os.path.abspath(module))

def inject_local_package(name):
    """patches sys.path to prefer / find packages provided next to this script"""
    here = package_folder(__file__)
    sys.path.insert(0, os.path.join(here, name))


def import_and_check_location(package_name):
    """checks if given package was imported from right place"""

    print "import %s: " % package_name,
    package = __import__(package_name)
    print "succeeded" 

    print "check location: ",
    found_location = package_folder(package.__file__)
    here = package_folder(__file__)
    if found_location.startswith(here):
        print "ok"
    else:
        print "failed: package is imported from %s" % found_location


inject_local_package("six")
inject_local_package("dateutil-master")
inject_local_package("requests-master")
inject_local_package("resync-master")

print sys.path
print "jython version" + str(sys.version)

import_and_check_location("six")
import_and_check_location("dateutil")
import_and_check_location("requests")
import_and_check_location("resync")


'''this is where the actual logic starts'''

import resync
from resync import Resource,ResourceList, CapabilityList
from resync.source_description import SourceDescription
from resync.resource_list import ResourceList, ResourceListDupeError

#===============================================================================
# from resync.resource import Resource
# from resync.resource_list import ResourceList
# from resync.change_list import ChangeList
# from resync.capability_list import CapabilityList
#===============================================================================

DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
DATE_FORMAT_WITH_TIME = SimpleDateFormat("yyyy-MM-dd HH:mm")
TIME_ZONE = "0"
ALLOWED_PARAMS = ["verb", "black_list", "mode"]
DATA_SET_LINES = []

def getURIPrefix():
    return getDownloadUrl() + getServletPath()

def getSourceDescURI():
    return getURIPrefix() + "?verb=about.xml"

def getCapabilityListURI():
    return getURIPrefix() + "?verb=capabilitylist.xml"

def getResourceListURI() :
    return getURIPrefix() + "?verb=resourcelist.xml"

def createResourceListTimestamp():
    format = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
    format.setTimeZone(TimeZone.getTimeZone("GMT"))
    return format.format(Date())

def getResourceListPartDir() :
    return os.path.join(getTempDir())

def maintainResourceListPartDir() :
    partDir = getResourceListPartDir()
    
    if not os.path.exists(partDir):
        os.makedirs(partDir)
        
    timeThreshold = int(time.time()) - (3600 * 24) 
        
    for fileName in os.listdir(partDir):
        if isResourceListPartFileName(fileName):
            filePath = os.path.join(partDir, fileName)
            if os.path.getmtime(filePath) < timeThreshold:
                print "Removed old resource list part " + filePath
                os.remove(filePath)

def getResourceListPartURIBase(timestamp) :
    return getURIPrefix() + "?verb=resourcelistpart_{timestamp}_.xml".format(timestamp=timestamp)

def getResourceListPartFileName(timestamp, part_number) :
    return "resourcelistpart_{timestamp}_{part_number:05d}.xml".format(timestamp=timestamp, part_number=part_number)

def isResourceListPartFileName(str) :
    return str.startswith("resourcelistpart") and str.endswith("xml")

def getSourceDescription():
    #Describe source
    rsd = SourceDescription()
    rsd.describedby = getSourceDescURI()
    rsd.md_at = None
    rsd.add_capability_list(getCapabilityListURI())
    return rsd.as_xml()

def getCapabilityList(rl):
    # Read Capability List and show supported capabilities
    cl = CapabilityList()
    cl.add_capability(rl, getResourceListURI())
    cl.md['from'] = "2013-02-07T22:39:00"
#    cl.up = getSourceDescURI
    cl.link_set(rel="up",href=getSourceDescURI())
    return cl

def convertToW3CDate(date):
    format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    format.setTimeZone(TimeZone.getTimeZone("GMT"))
    result = format.format(date)+"+00:00"
    return result

def getMaterialsAsResources(rl):
    materials = v3EntityRetriever.fetchMaterials()
    for material in materials:
        res = Resource(getMaterialResourceURL(material), lastmod=convertToW3CDate(material.getModificationDate()))
        res.link_set(rel="describedby", href=getMedataDataURIForMaterial(material))
        rl.add(res)
        '''add the meta data record as a resource as well'''
        md_res = Resource(getMedataDataURIForMaterial(material), lastmod=convertToW3CDate(material.getModificationDate()))
        md_res.link_set(rel="describes", href=getMaterialResourceURL(material))
        rl.add(md_res)
    return materials

def getResourceList(entities, rl):
    for entity in entities:
        '''YYYY-MM-DDThh:mm:ssTZD'''
        res = Resource(getResourceUrl(entity), lastmod=convertToW3CDate(entity.getEntity().getModificationDate()))
        res.link_set(rel="describedby",href=getMedataDataURI(entity))
        try:
            rl.add(res)
            '''add the meta data record as a resource as well'''
            md_res = Resource(getMedataDataURI(entity), lastmod=convertToW3CDate(entity.getEntity().getModificationDate()))
            md_res.link_set(rel="describes",href=getResourceUrl(entity))
            rl.add(md_res)
        except ResourceListDupeError,dupex:
            '''the cases where we might have duplicate entities? 
            1. Shared samples: if entity.getEntityKind() == "SAMPLE" and entity.getSpaceOrNull() == None :
            2. Data set in a space that contains another data set which has a child in a different space: Child DS will already be added when the first
            space entities were retrieved as resource.
            TODO  Might log this because this means the resource was probably added before when resources from another space were added
            TODO CHECK IF SPACE VISIBILITY ISSUES COULD CAUSE PROBLEMS!!!!!!'''
            continue
    return rl

def getTypeCode(entity):
    return entity.getTypeCodeOrNull() if entity.getTypeCodeOrNull() else ""

def getModifierId(entity):
    return entity.getEntity().getModifier().getUserId() if entity.getEntity().getModifier() else getRegistratorId(entity)

def getRegistratorId(entity):
    registrator = entity.getEntity().getRegistrator()
    return registrator.getUserId() if registrator else ""
    
def getSpace(entity):
    return entity.getSpaceOrNull() if entity.getSpaceOrNull() else ""

def getProject(entity):
    return entity.getProjectOrNull() if entity.getProjectOrNull() else  ""

def getRegistrationTimestamp(entity):
    return convertToW3CDate(entity.getRegistrationDate())

def createProjectMetaData(entity, url_elm):
    desc = entity.getEntity().getDescription()

    attrs = {"kind": entity.getEntityKind().toString(), 
        "code": entity.getCode(), 
        "registration-timestamp": getRegistrationTimestamp(entity),
        "registrator": getRegistratorId(entity),
        "modifier": getModifierId(entity),
        "space": getSpace(entity), 
        "desc": desc if desc else "" }
    return ET.SubElement(url_elm, "x:xd", attrib = attrs)

def createSampleMetaData(entity, url_elm):
    attrs = {"kind": entity.getEntityKind().toString(), 
        "code": entity.getCode(), 
        "type": getTypeCode(entity), 
        "registration-timestamp": getRegistrationTimestamp(entity),
        "registrator": getRegistratorId(entity),
        "modifier": getModifierId(entity),
        "space": getSpace(entity), 
        "project": getProject(entity), 
        "experiment": entity.getExperimentIdentifierOrNull() if entity.getExperimentIdentifierOrNull() else  ""}
    return ET.SubElement(url_elm, "x:xd", attrib = attrs)

def createExperimentMetaData(entity, url_elm):
    attrs = {"kind": entity.getEntityKind().toString(), 
        "code": entity.getCode(), 
        "type": getTypeCode(entity), 
        "registration-timestamp": getRegistrationTimestamp(entity),
        "registrator": getRegistratorId(entity),
        "modifier": getModifierId(entity),
        "space": getSpace(entity), 
        "project": getProject(entity)}
    return ET.SubElement(url_elm, "x:xd", attrib = attrs)

def createDataSetMetaData(entity, url_elm):
    dsKind = entity.getEntity().getKind().toString() 
    #TO-DO , 
    attrs = {"kind": entity.getEntityKind().toString(), 
        "code": entity.getCode(), 
        "dsKind": dsKind, 
        "type": getTypeCode(entity), 
        "registration-timestamp": getRegistrationTimestamp(entity),
        "registrator":  getRegistratorId(entity),
        "modifier": getModifierId(entity),
        "sample": entity.getSampleIdentifierOrNull() if entity.getSampleIdentifierOrNull() else "", 
        "experiment": entity.getExperimentIdentifierOrNull() if entity.getExperimentIdentifierOrNull() else ""} 
    return ET.SubElement(url_elm, "x:xd", attrib = attrs)

def createMaterialMetaData(material, url_elm):
    attrs = {"kind": "MATERIAL", "code": material.getCode(), "type": material.getType().getCode(), 
             "registration-timestamp": getRegistrationTimestamp(material),
             "registrator":  material.getRegistrator().getUserId()}
    return ET.SubElement(url_elm, "x:xd", attrib = attrs)

def attachProperties(entity, xd_elm):
    properties = entity.getPropertiesOrNull()
    if properties:
        propertiesNode = ET.SubElement(xd_elm, "x:properties")
        for key in properties.keySet():
            propertyNode = ET.SubElement(propertiesNode, "x:property")
            codeNode = ET.SubElement(propertyNode, "x:code")
            codeNode.text = key
            valueNode = ET.SubElement(propertyNode, "x:value")
            valueNode.text = properties.get(key)
            
def attachPropertiesForMaterials(material, xd_elm):
    properties = material.getProperties()
    if properties:
        propertiesNode = ET.SubElement(xd_elm, "x:properties")
        for key in properties.keySet():
            propertyNode = ET.SubElement(propertiesNode, "x:property")
            codeNode = ET.SubElement(propertyNode, "x:code")
            codeNode.text = key
            valueNode = ET.SubElement(propertyNode, "x:value")
            valueNode.text = properties.get(key)
            
def attachConnections(entity, xd_elm):
    connections = entity.getConnections()
    if connections.size() > 0:
        connectionsNode = ET.SubElement(xd_elm, "x:connections")
        for edgeNodePair in connections:
            connectionNode = ET.SubElement(connectionsNode, "x:connection", attrib = {"to": edgeNodePair.getNode().getPermId(), "type": edgeNodePair.getEdge().getType()})
         
            #===================================================================
            # connToNode = ET.SubElement(connectionNode, "x:to")
            # connToNode.text = edgeNodePair.getNode().getPermId()
            #  
            # connTypeNode = ET.SubElement(connectionNode, "x:type")
            # connTypeNode.text =  edgeNodePair.getEdge().getType()
            #===================================================================

def attachBinaryData(entity, xd_elm):
    entityKind = entity.getEntityKind().toString()
    if entityKind == "DATA_SET":
        dataSetCode = entity.getCode()
        if entity.getEntity().getKind().equals(DataSetKind.PHYSICAL):
            binaryDataNode = ET.SubElement(xd_elm, "x:binaryData")
            _addFileNodes(binaryDataNode, dataSetCode)
        if entity.getEntity().getKind().equals(DataSetKind.LINK):
            binaryDataNode = ET.SubElement(xd_elm, "x:binaryData")
            for cc in entity.getEntity().getLinkedData().getContentCopies():
                attributes = {}
                _addIfSpecified(attributes, "id", cc.getId().getPermId())
                _addIfSpecified(attributes, "externalDMS", cc.getExternalDms().getCode())
                _addIfSpecified(attributes, "externalCode", cc.getExternalCode())
                _addIfSpecified(attributes, "gitCommitHash", cc.getGitCommitHash())
                _addIfSpecified(attributes, "gitRepositoryId", cc.getGitRepositoryId())
                _addIfSpecified(attributes, "path", cc.getPath())
                ET.SubElement(binaryDataNode, "x:contentCopy", attrib = attributes)
            _addFileNodes(binaryDataNode, dataSetCode)
    else:
        attachments = entity.getAttachmentsOrNull()
        if not attachments:
            return
        binaryDataNode = ET.SubElement(xd_elm, "x:binaryData")
        for attachment in attachments:
            title = attachment.getTitle()
            desc = attachment.getDescription()
            linkNode = ET.SubElement(binaryDataNode, "x:attachment", attrib = {"fileName": attachment.getFileName(), "title":  title if  title else "", "latestVersion" : str(attachment.getVersion()), "description": desc if desc else "", "permink": attachment.getPermlink()})

def _addIfSpecified(dict, key, value):
    if value is not None:
        dict[key] = value

def createEntityMetaData(entity, url_elm, entityKind):
    if entityKind == "PROJECT":
        xd_elm = createProjectMetaData(entity, url_elm)
    elif entityKind == "SAMPLE":
        xd_elm = createSampleMetaData(entity, url_elm)
    elif entityKind == "EXPERIMENT":
        xd_elm = createExperimentMetaData(entity, url_elm)
    elif entityKind == "DATA_SET":
        xd_elm = createDataSetMetaData(entity, url_elm)
    return xd_elm


def injectEntityMetaData(entity, url_elm):
    entityKind = entity.getEntityKind().toString()
    xd_elm = createEntityMetaData(entity, url_elm, entityKind)
    if entityKind in ["SAMPLE", "EXPERIMENT", "DATA_SET"]:
        '''properties'''
        attachProperties(entity, xd_elm)
    '''connections'''
    attachConnections(entity, xd_elm)
    '''binary data links'''
    attachBinaryData(entity, xd_elm)

def injectMaterialMetaData(material, url_elm):
    xd_elm = createMaterialMetaData(material, url_elm)
    attachPropertiesForMaterials(material, xd_elm)

def injectMasterData(url_elm):
    masterDataXML = v3EntityRetriever.fetchMasterDataAsXML()
    masterDataTree = ET.fromstring(masterDataXML.encode('utf-8'))
    #print ET.tostring(masterDataTree, None, None)
    url_elm.append(masterDataTree)
#    ET.SubElement(url_elm, masterDataTree)

def findEntityInMaps(projectMap, expMap, sampleMap, dsMap, perm_id):
    if perm_id in projectMap.keys():
        return projectMap[perm_id]
    elif perm_id in expMap.keys():
        return expMap[perm_id]
    elif perm_id in sampleMap.keys():
        return sampleMap[perm_id]
    elif perm_id in dsMap.keys():
        return dsMap[perm_id]
    return None

def injectMdAt(xml, mdDate):
    return xml.replace('capability="resourcelist"', 'capability="resourcelist" at="{mdDate}"'.format(mdDate=mdDate), 1)

def injectMetaDataXML(materials, entities, xml_in):
    materialMap = {}
    for material in materials:
        key = str(material.getPermId().getCode() + ":" + material.getType().getCode())
        materialMap[key] = material
        
    print materialMap.keys()
                
    projectMap = {}
    expMap = {}
    sampleMap = {}
    dsMap = {}

    for entity in entities:
        entityKind = entity.getEntityKind().toString()
        if entityKind == "PROJECT":
            projectMap[str(entity.getPermId())] = entity
        elif entityKind == "SAMPLE":
            sampleMap[str(entity.getPermId())] = entity
        elif entityKind == "EXPERIMENT":
            expMap[str(entity.getPermId())] = entity
        elif entityKind == "DATA_SET":
            dsMap[str(entity.getPermId())] = entity
          
    tree = ET.fromstring(xml_in.encode('utf-8'))
    for url_elm in tree.findall(".//{http://www.sitemaps.org/schemas/sitemap/0.9}url"):
        #print url_elm
        loc_elm = url_elm.findall(".//{http://www.sitemaps.org/schemas/sitemap/0.9}loc")
        if loc_elm[0].text.strip().endswith("/M"):
            href_parts = loc_elm[0].text.split('/')
            perm_id = str(href_parts[len(href_parts)-2])
            ''' MATERIAL uri also contain material type as materials are uniquely identified by code and type'''
            entity_kind_or_material_type = str(href_parts[len(href_parts)-3]).strip()
            key =  str(perm_id + ":"+ entity_kind_or_material_type)
            # print "perm_id:" +perm_id 
            # print "entity_kind:" + entity_kind_or_material_type
            if entity_kind_or_material_type == "MASTER_DATA":
                injectMasterData(url_elm)
            elif entity_kind_or_material_type == "PROJECT":
                entity = projectMap[perm_id]
                injectEntityMetaData(entity, url_elm)
            elif entity_kind_or_material_type == "EXPERIMENT": 
                entity = expMap[perm_id]
                injectEntityMetaData(entity, url_elm)
            elif entity_kind_or_material_type == "SAMPLE": 
                entity = sampleMap[perm_id]
                injectEntityMetaData(entity, url_elm)
            elif entity_kind_or_material_type == "DATA_SET": 
                entity = dsMap[perm_id]
                injectEntityMetaData(entity, url_elm)
            else:
                entity_kind = str(href_parts[len(href_parts)-4]).strip()
                if entity_kind == "MATERIAL" and key in materialMap.keys():
                    material = materialMap[key]
                    injectMaterialMetaData(material, url_elm)
                else:
                    raise Exception("Resource " + perm_id + " not found in any meta data elements in XML: %s" % perm_id)
                
    ET.register_namespace('', 'http://www.sitemaps.org/schemas/sitemap/0.9')
    ET.register_namespace('rs', 'http://www.openarchives.org/rs/terms/')
    ET.register_namespace('x', 'https://sis.id.ethz.ch/software/#openbis/xdterms//')
    ET.register_namespace('xmd', 'https://sis.id.ethz.ch/software/#openbis/xmdterms/')

    tree.set("xmlns:" + "x", "https://sis.id.ethz.ch/software/#openbis/xdterms/")
    tree.set("xmlns:" + "xsi", "http://www.w3.org/2001/XMLSchema-instance")
    tree.set("xsi:schemaLocation", "https://sis.id.ethz.ch/software/#openbis/xdterms/ ./xml/xdterms.xsd https://sis.id.ethz.ch/software/#openbis/xmdterms/")
 
    return ET.tostring(tree, None, None)

def getDOTRepForSpace(space, graph, mode, idx):
    sub_dot = getEdges(graph, mode == "test")
    dot_rep = ""
    dot_rep += '''subgraph cluster_%d {
        style=filled;
        color=lightgrey;
        node [style=filled,color=white];''' % idx
    dot_rep +="\n\n"
    dot_rep += sub_dot
    dot_rep +='label = "%s";}' % space
    dot_rep += "\n\n"
    return dot_rep

#===============================================================================
# def getGraphInDOT(spaces):
#     dot_rep = getDOTRepForClusters(spaces)
#     cluster_str = ''.join(dot_rep)
#     return 'digraph G {\n\n' + cluster_str + '\n}'
#     file_name = "Entity_DAG_" + '_'.join(spaces)
#     file_name += '.dot' #        file_path = os.path.join(os.path.abspath(__file__))
#     '''TODO remove local path!!!!!'''
#     with open(os.path.join('/Users/gakin/Documents', file_name), 'w') as text_file:
#         text_file.write('digraph G {\n\n' + cluster_str + '\n}')
#===============================================================================

def handle(req, resp):
    print("loaded resync version", resync.__version__)

    writer = resp.getWriter()
    resp.setContentType("text/xml")    
    
    params = {}
    for param in req.getParameterNames():
        values = []
        for value in req.getParameterValues(param):
            values.append(str(value))
        params[str(param)] = values
        
    '''TODO fix capability list below''' 
#    rl = ResourceList()
    rl = ResourceList(allow_multifile=False)
    rl.max_sitemap_entries = None
    
    maintainResourceListPartDir()
    
    if 'verb' not in params or "about.xml" in params['verb']:
        writer.write(getSourceDescription());
    elif "capabilitylist.xml" in  params['verb']:        
        writer.write(getCapabilityList(rl).as_xml());
    elif len(params['verb']) > 0 and isResourceListPartFileName(params['verb'][0]):
        part_path = os.path.join(getResourceListPartDir(), params['verb'][0])
        with open(part_path, 'r') as part_file:
            writer.write(part_file.read())
    elif "resourcelist.xml" in  params['verb']:
        deliverResourceList(rl, params, writer)
    else:
        writer.write(getSourceDescription());

    v3EntityRetriever.finish()
    writer.flush();

def deliverResourceList(rl, params, writer):
    try:
        '''TODO Maybe keep a list of allowed params and throw error if any other param is sent over'''
        for param in params:
            if param not in ALLOWED_PARAMS:
                raise Exception("Parameter '%s' is not alowed." % param)
        spaces_param = list()
        spaces_black_list = list()
        if "black_list" in params:
            spaces_param =  params['black_list']
        if len(spaces_param) > 1 : #and spaces[0].strip() == "" :
            raise Exception("Specify spaces in a parameter called 'spaces' with comma separated values.")
        if len(spaces_param) == 1 :
            spaces_black_list = spaces_param[0].split(",")
        #=======================================================================
        # for space in spaces:
        #     if v3EntityRetriever.spaceExists(space) == False:
        #         raise Exception("Space %s does not exist." % space)
        #=======================================================================
        rl.up = getCapabilityListURI()
        
        result = queryService.select("openbis-db", "select xact_start FROM pg_stat_activity WHERE xact_start IS NOT NULL ORDER BY xact_start ASC LIMIT 1")
        time  = Date()
        if result:
            time = result[0].get("xact_start")

        queryService.release()
        rl.md_at = convertToW3CDate(time)
        '''Add master data as a single element to the RL'''
        res = Resource(getServerUrl() + "/MASTER_DATA/MASTER_DATA/M", lastmod=convertToW3CDate(Date()))
        rl.add(res)

        v3EntityRetriever.start(writer)

        '''then add the materials to the resource list'''
        materials = getMaterialsAsResources(rl)

        '''then add other openbis entities for each space'''
        spaces =  [str(space.getCode()) for space in v3EntityRetriever.getSpaces()]
        for space in spaces_black_list:
            if space in spaces:
                spaces.remove(space)
            else:
                raise Exception("Black-listed space '%s' does not exist or is not visible to user." % space)
            
        mode = "rl"
        if "mode" in params:
            mode_param = params["mode"][0]
            if mode_param == "test":
                mode = "test"
            elif mode_param == "dot":
                mode = "dot"
            else:
                raise Exception("Values allowed for 'mode' are: 'test' and 'dot'")

        all_entities = list()
        cluster_str = ""
        idx = 0 
        for space in spaces:
            graph = getEntityGraph(space)
            cluster = getDOTRepForSpace(space, graph, mode, idx)
            cluster_str += cluster
            idx = idx + 1
            '''TODO if mode == "rl":"'''
            entities = graph.getNodes()
            rl = getResourceList(entities, rl)
            all_entities.extend(entities)
        
        if mode == "test":
            for entity in all_entities:
                if entity.getEntityKind().toString() == "DATA_SET":
                    fileNodes = getFilesInDataSet(entity.getCode())
                    for path, crc32_checksum, file_length in fileNodes :
                        DATA_SET_LINES.append(entity.getCode() + ":" + path)
            
            cluster_str = cluster_str + '\n'.join([ line for line in DATA_SET_LINES ])
        elif mode == "dot":
            cluster_str = 'digraph G {\n\n' + cluster_str + '\n}'

        '''TO-DO check if it is OK to call getNodes below? What if in between the nodes change'''
        '''Maybe look at creating meta data as we add resources and putting them in a dictionary'''
        #injectMetaDataXML(materials, all_entities, rl_xml)
       # writer.write(injectMetaDataXML(materials, all_entities, rl_xml))
        if mode in ["test", "dot"]:
            resp.setContentType("text/plain");
            v3EntityRetriever.finish()
            writer.write(cluster_str)
        else:
            if rl.requires_multifile():
                timestamp = createResourceListTimestamp()
                
                index_xml = rl.as_xml(allow_multifile=True, basename=getResourceListPartURIBase(timestamp))
                index_xml = injectMdAt(index_xml, rl.md_at)
                writer.write(index_xml)
                
                num_parts = rl.requires_multifile()
                
                for part_number in range(0, num_parts):
                    part_xml = rl.as_xml_part(basename=getResourceListURI(), part_number=part_number)
                    part_xml = injectMetaDataXML(materials, all_entities, part_xml)
                    part_xml = injectMdAt(part_xml, rl.md_at)
                    part_path = os.path.join(getResourceListPartDir(), getResourceListPartFileName(timestamp, part_number))
                    with open(part_path, 'w+') as part_file:
                        part_file.write(part_xml)
                v3EntityRetriever.finish()
            else:
                xml = injectMetaDataXML(materials, all_entities, rl.as_xml())
                v3EntityRetriever.finish()
                writer.write(xml)
    finally:
        v3EntityRetriever.finish()

    #===========================================================================
    # cl.read("https://raw.github.com/resync/resync/0.6/resync/test/testdata/examples_from_spec/resourcesync_ex_2_6.xml")
    # for resource in cl:
    #     print "supports %s (at %s)" % (resource.capability,resource.uri)
    #===========================================================================
def getEntityGraph(space):
    graph = v3EntityRetriever.getEntityGraph(space)
    return graph

def getEdges(graph, forTest):
    if forTest == True:
        sub_dot = graph.getEdgesForTest()
    else:
        sub_dot = graph.getEdgesForDOTRepresentation()
    return sub_dot

    
def getResourceUrl(entity):
    entityKind = entity.getEntityKind().toString()
    if entityKind in ["SAMPLE", "EXPERIMENT", "DATA_SET"]:
        return getServerUrl() + "?viewMode=SIMPLE&anonymous=true#entity=" + entityKind + "&permId=" + str(entity.getPermId())
    elif entityKind == "PROJECT":
        return getServerUrl() + "?viewMode=SIMPLE&anonymous=true#entity=PROJECT&code=" + str(entity.getCode()) + "&space=" + str(entity.getSpaceOrNull())

def getMaterialResourceURL(material):
        return getServerUrl() + "#action=VIEW&entity=MATERIAL&code=" + material.getCode() + "&type=" + material.getType().getCode()

def getMedataDataURI(entity):
    return getServerUrl() + "/" + entity.getEntityKind().toString() + "/" + entity.getPermId() + "/M"

def getMedataDataURIForMaterial(material):
    return getServerUrl() + "/" + "MATERIAL" + "/" + material.getType().getCode() + "/" + material.getPermId().getCode() + "/M"    

def getServletPath():
    return properties.get("path").strip()[:-1]
    
def getServerUrl():
    property = properties.get("server-url")
    if property and property.strip():
        return property.strip()
    else:
        raise Exception("No server url has been configured. Please check that 'server-url' property has been properly set in the service plugin plugin.properties.")

def getDownloadUrl():
    property = properties.get("download-url")
    if property and property.strip():
        return property.strip()
    else:
        raise Exception("No download url has been configured. Please check that 'download-url' property has been properly set in the service plugin plugin.properties.")

def getTempDir():
    property = properties.get("temp-dir")
    if property and property.strip():
        return property.strip()
    else:
        raise Exception("No temporary directory has been configured. Please check that 'temp-dir' property has been properly set in the service plugin plugin.properties.")

def _addFileNodes(binaryDataNode, dataSetCode):
    for path, crc32_checksum, checksum, file_length in getDataSetFileInfos(dataSetCode):
        attributes = {"path": path, "length": str(file_length)}
        if crc32_checksum is not None:
            attributes["crc32checksum"] = crc32_checksum
        if checksum is not None:
            attributes["checksum"] = checksum
        linkNode = ET.SubElement(binaryDataNode, "x:fileNode", attrib = attributes)
    
def getDataSetFileInfos(dataSetCode):
    infos = []
    for path, crc32_checksum, checksum, file_length in getFilesInDataSet(dataSetCode) :
        '''TO-DO make below  URL configurable'''
        infos.append((getDownloadUrl() + "/datastore_server/" + dataSetCode + "/" + path + "?", crc32_checksum, checksum, file_length)) # + "?mode=simpleHtml&sessionID=" + userSessionToken 
    return infos 

def getFilesInDataSet(dataSetCode):
    infos = []
    fileNodes = contentProvider.getContent(dataSetCode).listMatchingNodes(".*\.*")
    if fileNodes:
        for  i, fileNode in enumerate(fileNodes):
            try:
                if fileNodes[i].isDirectory() == False:
                    relPath = fileNodes[i].getRelativePath();
                    fileLength = str(fileNode.getFileLength())
                    crc32checksum = str(fileNode.getChecksumCRC32()) if fileNode.isChecksumCRC32Precalculated() else None
                    checksum = fileNode.getChecksum()
                    infos.append((relPath, crc32checksum, checksum, fileLength));
            except Exception, ex:
                print "File  node: %d for data set' %s' no longer exists: %s" % (i, str(dataSetCode), ex.getMessage())
    else:
        print "No fileNode found in data set: '%s'" % str(dataSetCode)   
    return infos
