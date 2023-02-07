#
# Copyright 2016 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the 'License');
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import json
import os
import traceback
import xml.etree.ElementTree as ET
from urlparse import urlsplit

import datetime
import time
from ch.systemsx.cisd.common.logging import LogCategory
from java.io import File
from java.io import FileOutputStream
from java.net import URI
from java.nio.file import Paths
from java.text import SimpleDateFormat
from java.util import UUID
from java.util.zip import ZipOutputStream, Deflater
from org.apache.commons.io import FileUtils
from org.apache.log4j import Logger
from org.eclipse.jetty.client import HttpClient
from org.eclipse.jetty.client.util import BasicAuthentication
from org.eclipse.jetty.http import HttpMethod
from org.eclipse.jetty.util.ssl import SslContextFactory

from exportsApi import displayResult, findEntitiesToExport, validateDataSize, getConfigurationProperty, addToZipFile, generateZipFile, \
    checkResponseStatus, cleanUp

operationLog = Logger.getLogger(str(LogCategory.OPERATION) + '.rcExports.py')


def process(tr, params, tableBuilder):
    method = params.get('method')

    # Set user using the service
    tr.setUserId(userId)

    if method == 'exportAll':
        resultUrl = expandAndExport(tr, params)
        displayResult(resultUrl is not None, tableBuilder, '{"url": "' + resultUrl + '"}' if resultUrl is not None else None,
                      errorMessage=None if resultUrl is not None else 'Archives are not allowed if indefinite retention period is selected.')


def getBaseUrl(url):
    splitUrl = urlsplit(url)
    return splitUrl.scheme + '://' + splitUrl.netloc


def expandAndExport(tr, params):
    entitiesToExport = findEntitiesToExport(params)
    validateDataSize(entitiesToExport, tr)

    if params.get('retentionPeriod') == 'indefinite' and containsArchives(entitiesToExport):
        return None

    userInformation = {
        'firstName': params.get('userFirstName'),
        'lastName': params.get('userLastName'),
        'email': params.get('userEmail'),
    }

    operationLog.info('Found ' + str(len(entitiesToExport)) + ' entities to export')
    return export(entities=entitiesToExport, tr=tr, params=params, userInformation=userInformation)


def containsArchives(entitiesToExport):
    for entityToExport in entitiesToExport:
        print('containsArchives() entityToExport=%s' % str(entityToExport))
        if entityToExport['type'] == 'FILE' and not entityToExport['isDirectory'] and isArchive(entityToExport['path']):
            print('Archive found. entityToExport=%s' % str(entityToExport))
            return True
    return False


def isArchive(path):
    archiveExtensions = ['7Z', 'A00', 'A01', 'A02', 'ACE', 'AGG', 'AIN', 'ALZ', 'APEX', 'APZ', 'AR', 'ARC', 'ARH', 'ARI', 'ARJ', 'ARK', 'ASR', 'B1', 'B64', 'B6Z', 'BA', 'BDOC', 'BH', 'BNDL', 'BOO',
                         'BUNDLE', 'BZ', 'BZ2', 'BZA', 'BZIP', 'BZIP2', 'C00', 'C01', 'C02', 'C10', 'CAR', 'CB7', 'CBA', 'CBR', 'CBT', 'CBZ', 'CDZ', 'CP9', 'CPGZ', 'CPT', 'CTX', 'CTZ', 'CXARCHIVE',
                         'CZIP', 'DAF', 'DAR', 'DD', 'DEB', 'DGC', 'DIST', 'DL_', 'DZ', 'ECS', 'ECSBX', 'EDZ', 'EFW', 'EGG', 'EPI', 'F', 'F3Z', 'FDP', 'FP8', 'FZBZ', 'FZPZ', 'GCA', 'GMZ', 'GZ', 'GZ2',
                         'GZA', 'GZI', 'GZIP', 'HA', 'HBC', 'HBC2', 'HBE', 'HKI', 'HKI1', 'HKI2', 'HKI3', 'HPK', 'HPKG', 'HYP', 'IADPROJ', 'ICE', 'IPG', 'IPK', 'ISH', 'ISX', 'ITA', 'IZE', 'J', 'JGZ',
                         'JIC', 'JSONLZ4', 'KGB', 'KZ', 'LAYOUT', 'LBR', 'LEMON', 'LHA', 'LHZD', 'LIBZIP', 'LNX', 'LPKG', 'LQR', 'LZ', 'LZH', 'LZM', 'LZMA', 'LZO', 'LZX', 'MBZ', 'MD', 'MINT', 'MOU',
                         'MPKG', 'MZP', 'MZP', 'NEX', 'NPK', 'NZ', 'OAR', 'OPK', 'OZ', 'P01', 'P19', 'P7Z', 'PA', 'PACKAGE', 'PAE', 'PAK', 'PAQ6', 'PAQ7', 'PAQ8', 'PAQ8F', 'PAQ8L', 'PAQ8P', 'PAR',
                         'PAR2', 'PAX', 'PBI', 'PCV', 'PEA', 'PET', 'PF', 'PIM', 'PIT', 'PIZ', 'PKG', 'PRS', 'PSZ', 'PUP', 'PUP', 'PUZ', 'PVMZ', 'PWA', 'QDA', 'R0', 'R00', 'R01', 'R02', 'R03', 'R04',
                         'R1', 'R2', 'R21', 'R30', 'RAR', 'REV', 'RK', 'RNC', 'RP9', 'RPM', 'RSS', 'RTE', 'RZ', 'S00', 'S01', 'S02', 'S09', 'S7Z', 'SAR', 'SBX', 'SBX', 'SDC', 'SDN', 'SEA', 'SEN',
                         'SFG', 'SFS', 'SFX', 'SH', 'SHAR', 'SHK', 'SHR', 'SI', 'SIFZ', 'SIT', 'SITX', 'SMPF', 'SNAPPY', 'SNB', 'SPD', 'SPL', 'SPM', 'SPT', 'SQX', 'SQZ', 'SREP', 'STPROJ', 'SY_',
                         'TAZ', 'TBZ', 'TBZ2', 'TCX', 'TG', 'TGZ', 'TLZ', 'TLZMA', 'TRS', 'TX_', 'TXZ', 'TZ', 'TZST', 'UC2', 'UHA', 'UZIP', 'VEM', 'VFS', 'VIP', 'VMCZ', 'VOCA', 'VPK', 'VSI', 'WA',
                         'WAFF', 'WAR', 'WARC', 'WASTICKERS', 'WDZ', 'WHL', 'WLB', 'WOT', 'WUX', 'XAPK', 'XAR', 'XEF', 'XEZ', 'XIP', 'XMCDZ', 'XX', 'XZ', 'XZM', 'Y', 'YZ', 'YZ1', 'Z', 'Z01', 'Z02',
                         'Z03', 'Z04', 'ZAP', 'ZI', 'ZI_', 'ZIP', 'ZIPX', 'ZIX', 'ZL', 'ZOO', 'ZPI', 'ZSPLIT', 'ZST', 'ZW', 'ZZ']
    splitPath = path.split('.')
    splitItemsCount = len(splitPath)
    extension = splitPath[splitItemsCount - 1].upper()
    print('Checking isArchive(). path=%s, splitItemsCount=%i, extension=%s' % (path, splitItemsCount, extension))
    return splitItemsCount > 1 and extension in archiveExtensions


def export(entities, tr, params, userInformation):
    #Create temporal folder
    timeNow = time.time()

    exportDirName = 'export_' + str(timeNow)
    exportDir = File.createTempFile(exportDirName, None)
    exportDirPath = exportDir.getCanonicalPath()
    exportDir.delete()
    exportDir.mkdir()

    contentZipFileName = 'content.zip'
    contentDirName = 'content_' + str(timeNow)
    contentDir = File.createTempFile(contentDirName, None, exportDir)
    contentDirPath = contentDir.getCanonicalPath()
    contentDir.delete()
    contentDir.mkdir()

    contentZipFilePath = exportDirPath + '/' + contentZipFileName

    exportZipFilePath = exportDirPath + '.zip'
    exportZipFileName = exportDirName + '.zip'

    generateZipFile(entities, params, contentDirPath, contentZipFilePath, deflated=False)
    FileUtils.forceDelete(File(contentDirPath))

    generateExternalZipFile(params=params, exportDirPath=exportDirPath, contentZipFilePath=contentZipFilePath, contentZipFileName=contentZipFileName,
                            exportZipFileName=exportZipFilePath, userInformation=userInformation, entities=entities)
    resultUrl = sendToDSpace(params=params, tr=tr, tempZipFileName=exportZipFileName, tempZipFilePath=exportZipFilePath)
    cleanUp(exportDirPath, exportZipFilePath)
    return resultUrl


def sendToDSpace(params, tr, tempZipFileName, tempZipFilePath):
    serviceDocumentUrl = getConfigurationProperty(tr, 'service-document-url')
    depositUrl = getBaseUrl(serviceDocumentUrl) + str(params.get('submissionUrl'))

    headers = {
        'In-Progress': 'true',
        'Content-Disposition': 'filename=' + tempZipFileName,
        'Content-Type': 'application/zip',
        'Content-Length': os.stat(tempZipFilePath).st_size,
        'Content-Transfer-Encoding': 'binary',
        'Packaging': 'http://purl.org/net/sword/package/METSDSpaceSIP',
        'On-Behalf-Of': str(params.get('userId')),
    }

    httpClient = None
    try:
        httpClient = authenticateUserJava(depositUrl, tr)

        httpClient.setFollowRedirects(True)
        httpClient.start()

        request = httpClient.newRequest(depositUrl)
        for key, value in headers.iteritems():
            request.header(key, str(value))
        response = request.method(HttpMethod.POST).file(Paths.get(tempZipFilePath), 'application/zip').send()
        checkResponseStatus(response)

        xmlText = response.getContentAsString().encode('utf-8')
        xmlRoot = ET.fromstring(xmlText)
        linkElement = xmlRoot.find('xmlns:link[@rel="alternate"]', namespaces=dict(xmlns='http://www.w3.org/2005/Atom'))
        if linkElement is None:
            raise ValueError('No redirection URL is found in the response.')

        href = linkElement.attrib['href']

        return href
    except Exception as e:
        operationLog.error('Exception at: ' + traceback.format_exc())
        operationLog.error('Exception: ' + str(e))
        raise e
    finally:
        if httpClient is not None:
            httpClient.stop()


def authenticateUserJava(url, tr):
    sslContextFactory = SslContextFactory()
    httpClient = HttpClient(sslContextFactory)
    uri = URI(url)
    user = getConfigurationProperty(tr, 'user')
    password = getConfigurationProperty(tr, 'password')
    realm = getConfigurationProperty(tr, 'realm')
    auth = httpClient.getAuthenticationStore()
    auth.addAuthentication(BasicAuthentication(uri, realm, user, password))
    return httpClient


def fetchServiceDocument(url, httpClient):
    response = httpClient.newRequest(url).method(HttpMethod.GET).send()
    checkResponseStatus(response)

    xmlText = response.getContentAsString().encode('utf-8')
    xmlRoot = ET.fromstring(xmlText)
    collections = xmlRoot.findall('./xmlns:workspace/xmlns:collection[@href]', namespaces=dict(xmlns='http://www.w3.org/2007/app'))

    def collectionToDictionaryMapper(collection):
        return {
            'title': collection.find('./atom:title', namespaces=dict(atom='http://www.w3.org/2005/Atom')).text,
            'url': collection.attrib['href'],
        }

    return json.dumps(map(collectionToDictionaryMapper, collections))


def generateExternalZipFile(params, exportDirPath, contentZipFilePath, contentZipFileName, exportZipFileName, userInformation, entities,
                            deflated=True):
    # Generates ZIP file which will go to the research collection server

    fileMetadata = [
        {
            'fileName': contentZipFileName,
            'mimeType': 'application/zip'
        }
    ]

    fos = None
    zos = None
    try:
        fos = FileOutputStream(exportZipFileName)
        zos = ZipOutputStream(fos)
        if not deflated:
            zos.setMethod(ZipOutputStream.STORED)
            zos.setLevel(Deflater.NO_COMPRESSION)

        addToZipFile(' ' + contentZipFileName, File(contentZipFilePath), zos, deflated)

        generateXML(zipOutputStream=zos, fileMetadata=fileMetadata, exportDirPath=exportDirPath,
                    userInformation=userInformation, entities=entities, params=params)
    except Exception as e:
        operationLog.error('Exception at: ' + traceback.format_exc())
        operationLog.error('Exception: ' + str(e))
        raise e
    finally:
        if zos is not None:
            zos.close()
        if fos is not None:
            fos.close()


def generateXML(zipOutputStream, fileMetadata, exportDirPath, userInformation, entities, params):
    originUrl=params.get('originUrl')
    pathNameUrl=params.get('pathNameUrl')
    submissionType = str(params.get('submissionType'))

    ns = {
        'mets': 'http://www.loc.gov/METS/',
        'xlink': 'http://www.w3.org/1999/xlink',
        'dim': 'http://www.dspace.org/xmlns/dspace/dim'
    }

    entityPermIds = map(lambda entity: entity['permId'], entities)
    permIdsStr = reduce(lambda str, permId: str + ',' + permId, entityPermIds)

    withRegistrationDates = filter(lambda entity: 'registrationDate' in entity, entities)
    registrationDates = map(lambda entity: entity['registrationDate'], withRegistrationDates)

    if len(registrationDates) > 0:
        minDateStr = javaDateToStr(min(registrationDates, key=lambda date: date.getTime()))
        maxDateStr = javaDateToStr(max(registrationDates, key=lambda date: date.getTime()))
    else:
        minDateStr = None
        maxDateStr = None

    metsNS = ns['mets']
    xlinkNS = ns['xlink']
    dimNS = ns['dim']
    ET.register_namespace('mets', metsNS)
    ET.register_namespace('xlink', xlinkNS)
    ET.register_namespace('dim', dimNS)

    root = ET.Element(ET.QName(metsNS, 'METS'))
    root.set('LABEL', 'DSpace Item')
    root.set('ID', UUID.randomUUID().toString())

    dmdSec = ET.SubElement(root, ET.QName(metsNS, 'dmdSec'))
    dmdSec.set('GROUPID', 'group_dmd_0')
    dmdSec.set('ID', 'dmd_1')

    mdWrap = ET.SubElement(dmdSec, ET.QName(metsNS, 'mdWrap'))
    mdWrap.set('MDTYPE', 'OTHER')
    mdWrap.set('OTHERMDTYPE', 'DIM')

    xmlData = ET.SubElement(mdWrap, ET.QName(metsNS, 'xmlData'))

    dim = ET.SubElement(xmlData, ET.QName(dimNS, 'dim'))
    dim.set('dspaceType', 'ITEM')

    titleField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    titleField.set('mdschema', 'dc')
    titleField.set('element', 'title')
    titleField.text = ''

    typeField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    typeField.set('mdschema', 'dc')
    typeField.set('element', 'type')
    typeField.text = submissionType

    userIdField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    userIdField.set('mdschema', 'ethz')
    userIdField.set('element', 'identifier')
    userIdField.set('qualifier', 'openbis')
    userIdField.text = permIdsStr

    userInfoField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    userInfoField.set('mdschema', 'dc')
    userInfoField.set('element', 'contributor')
    userInfoField.set('qualifier', 'author')
    userInfoField.text = userInformation['lastName'] + ', ' + userInformation['firstName']

    publicationDateField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    publicationDateField.set('mdschema', 'dc')
    publicationDateField.set('element', 'date')
    publicationDateField.set('qualifier', 'issued')
    publicationDateField.text = datetime.date.today().strftime('%Y-%m-%d')

    notesField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    notesField.set('mdschema', 'ethz')
    notesField.set('element', 'notes')
    notesField.text = 'This export has been made from the openBIS installation %s, if you wish to access the original information please contact the data creator.' \
                      % (originUrl + pathNameUrl)

    openBisApiUrlField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
    openBisApiUrlField.set('mdschema', 'ethz')
    openBisApiUrlField.set('element', 'identifier')
    openBisApiUrlField.set('qualifier', 'openBisApiUrl')
    openBisApiUrlField.text = originUrl + '/openbis/openbis/rmi-application-server-v3.json'

    if minDateStr is not None and maxDateStr is not None:
        creationDateField = ET.SubElement(dim, ET.QName(dimNS, 'field'))
        creationDateField.set('mdschema', 'dc')
        creationDateField.set('element', 'date')
        creationDateField.set('qualifier', 'created')
        creationDateField.text = minDateStr + '/' + maxDateStr if minDateStr != maxDateStr else minDateStr

    fileSec = ET.SubElement(root, ET.QName(metsNS, 'fileSec'))
    fileGrp = ET.SubElement(fileSec, ET.QName(metsNS, 'fileGrp'))
    fileGrp.set('USE', 'CONTENT')

    i = 0
    for fileMetadatum in fileMetadata:
        i += 1
        file = ET.SubElement(fileGrp, ET.QName(metsNS, 'file'))
        file.set('ID', 'file_' + str(i))
        fLocat = ET.SubElement(file, ET.QName(metsNS, 'FLocat'))
        fLocat.set('LOCTYPE', 'URL')
        fLocat.set('MIMETYPE', fileMetadatum.get('mimeType'))
        fLocat.set('RETENTIONPERIOD', params.get('retentionPeriod'))
        fLocat.set(ET.QName(xlinkNS, 'href'), fileMetadatum.get('fileName'))

    structMap = ET.SubElement(root, ET.QName(metsNS, 'structMap'))
    structMap.set('LABEL', 'DSpace')
    structMap.set('TYPE', 'LOGICAL')
    div1 = ET.SubElement(structMap, ET.QName(metsNS, 'div'))
    div1.set('DMDID', 'dmd_1')
    div1.set('TYPE', 'DSpace Item')

    xmlFileName = 'mets.xml'
    xmlFilePath = exportDirPath + '/' + xmlFileName
    ET.ElementTree(root).write(xmlFilePath)

    xmlFile = File(xmlFilePath)
    addToZipFile(' ' + xmlFileName, xmlFile, zipOutputStream)
    # Space is added to the file name because the method chops out the first character


def javaDateToStr(javaDate):
    dateFormat = SimpleDateFormat('yyyy-MM-dd')
    return dateFormat.format(javaDate)
