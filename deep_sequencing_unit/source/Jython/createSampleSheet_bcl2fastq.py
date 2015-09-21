'''
@copyright:
Copyright 2015 ETH Zuerich, SIS
 
@license:
Licensed under the Apache License, Version 2.0 (the 'License');
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author:
Manuel Kohler

@description:
Creates the SampleSheet.csv out of values from openBIS for Demultiplexing 
used in the Illumina pipeline (bcl2fastq) 

@attention:
Runs under Jython

@note:
Takes into account to replace special characters with an underscore so that the Illumina script
does not fail

HiSeq Header Description
========================
Column Header  Description
FCID  Flow cell ID
Lane  Positive integer, indicating the lane number (1-8)
SampleID  ID of the sample
SampleRef  The reference used for alignment for the sample
Index  Index sequences. Multiple index reads are separated by a hyphen (for example, ACCAGTAA-GGACATGA).
Description  Description of the sample
Control  Y indicates this lane is a control lane, N means sample
Recipe Recipe used during sequencing
Operator Name or ID of the operator
SampleProject  The project the sample belongs to
'''

import os
import logging
import re
import sys
import string
import smtplib
import argparse
from ConfigParser import SafeConfigParser
from datetime import *
from collections import OrderedDict

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email.Utils import COMMASPACE, formatdate
from email import Encoders

from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

lineending = {'win32':'\r\n', 'linux':'\n', 'mac':'\r'}
COMMA = ','
CSV = ".csv"


class Sequencers:
    HISEQ_4000, HISEQ_3000, HISEQ_2500, HISEQ_2000, HISEQ_X, NEXTSEQ_500, MISEQ , UNIDENTIFIED= \
        ('Illumina HiSeq 4000','Illumina HiSeq 3000','Illumina HiSeq 2500','Illumina HiSeq 2000',
         'Illumina HiSeq X', 'Illumina NextSeq 500', 'Illumina MiSeq', 'Unidentified')
HISEQ_LIST = [Sequencers.HISEQ_2000, Sequencers.HISEQ_2500, Sequencers.HISEQ_3000, Sequencers.HISEQ_4000, Sequencers.HISEQ_X]


def login(logger, configMap):
    logger.info('Logging into ' + configMap['openbisServer'])
    service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                    configMap['openbisPassword'],
                                                    configMap['openbisServer'],
                                                    configMap['connectionTimeout'])
    return service


def logout (service, logger):
    service.logout()
    logger.info('Logged out')


def setUpLogger(logPath, logLevel=logging.INFO):
    logFileName = 'create_sample_sheet_dict'
    d = datetime.now()
    logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
    logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
    logger = logging.getLogger(logFileName)
    return logger


def parseOptions(logger):
    logger.info('Parsing command line parameters')
    parser = argparse.ArgumentParser(version='%prog 1.0', description='Process some integers.')
    parser.add_argument('-f', '--flowcell',
                  dest='flowcell',
                  help='The flowcell which is used to create the SampleSheet.csv',
                  metavar='<flowcell>')
    parser.add_argument('-m', '--mailist',
                  dest='maillist',
                  default=False,
                  action='store_true',
                  help='Generated Sample Sheet will be addtionally sent as email to the defined list of recipients')
    parser.add_argument('-l', '--lineending',
                  dest='lineending',
                  action='store',
                  choices=['win32', 'linux', 'mac'],
                  default='win32',
                  help='Specify end of line separator: win32, linux, mac. Default: win32' ,
                  metavar='<lineending>')
    parser.add_argument('-o', '--outdir',
                  dest='outdir',
                  default='./',
                  help='Specify the ouput directory. Default: ./' ,
                  metavar='<outdir>')
    parser.add_argument('-s', '--singlelane',
                  dest='singlelane',
                  default=False,
                  action='store_true',
                  help='Creates a single Sample Sheet for each lane. Default: False')
    parser.add_argument('-d', '--debug',
                  dest='debug',
                  default=False,
                  action='store_true',
                  help='Verbose debug logging. Default: False')
    parser.add_argument('--verbose',
                  dest='verbose',
                  default=False,
                  action='store_true',
                  help='Write Sample Sheet to stout. Default: False')

    args = parser.parse_args()
    
    print(type(args))

    if args.outdir[-1] <> '/':
        args.outdir = args.outdir + '/'

    if args.flowcell is None:
        parser.print_help()
        exit(-1)
    return args


def parseConfigurationFile(propertyFile='etc/createSampleSheet.properties'):
    '''
    Parses the given config files and returns the values
    '''
    config = SafeConfigParser()
    config.read(propertyFile)
    config.sections()
    return config


def readConfig(logger):
    GENERAL = 'GENERAL'
    OPENBIS = 'OPENBIS'
    ILLUMINA = 'ILLUMINA'
    
    logger.info('Reading config file')
    configMap = {}
    
    configParameters = parseConfigurationFile()
    configMap['facilityName'] = configParameters.get(GENERAL, 'facilityName')
    configMap['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
    configMap['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
    configMap['mailList'] = configParameters.get(GENERAL, 'mailList')
    configMap['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
    configMap['smptHost'] = configParameters.get(GENERAL, 'smptHost')
    configMap['SampleSheetFileName'] = configParameters.get(GENERAL, 'SampleSheetFileName')
    configMap['separator'] = configParameters.get(GENERAL, 'separator')
    configMap['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')
    
    configMap['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
    configMap['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
    configMap['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
    configMap['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
    configMap['illuminaFlowCellTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowCellTypeName')
    configMap['index1Name'] = configParameters.get(OPENBIS, 'index1Name')
    configMap['index2Name'] = configParameters.get(OPENBIS, 'index2Name')
    configMap['index1Length'] = configParameters.get(OPENBIS, 'index1Length')
    configMap['index2Length'] = configParameters.get(OPENBIS, 'index2Length')
    configMap['endType'] = configParameters.get(OPENBIS, 'endType')
    configMap['cycles'] = configParameters.get(OPENBIS, 'cycles')
    configMap['controlLane'] = configParameters.get(OPENBIS, 'controlLane')
    configMap['ncbi'] = configParameters.get(OPENBIS, 'ncbi')
    configMap['externalSampleName'] = configParameters.get(OPENBIS, 'externalSampleName')
    configMap['laneCount'] = configParameters.get(OPENBIS, 'laneCount')
    configMap['kit'] = configParameters.get(OPENBIS, 'kit')
    
    configMap['headerSection'] = configParameters.get(ILLUMINA, 'headerSection')
    configMap['readsSection'] = configParameters.get(ILLUMINA, 'readsSection')
    configMap['settingsSection'] = configParameters.get(ILLUMINA, 'settingsSection')
    configMap['dataSectionSingleRead'] = configParameters.get(ILLUMINA, 'dataSectionSingleRead')
    configMap['dataSectionDualRead'] = configParameters.get(ILLUMINA, 'dataSectionDualRead')
    configMap['workflow'] = configParameters.get(ILLUMINA, 'workflow')
    configMap['application'] = configParameters.get(ILLUMINA, 'application')
    configMap['chemistry'] = configParameters.get(ILLUMINA, 'chemistry')
    
    configMap['truSeqAdapter1'] = configParameters.get(ILLUMINA, 'truSeqAdapter1')
    configMap['truSeqAdapter2'] = configParameters.get(ILLUMINA, 'truSeqAdapter2')
    configMap['nexteraAdapter'] = configParameters.get(ILLUMINA, 'nexteraAdapter')
    configMap['iemFileVersion'] = configParameters.get(ILLUMINA, 'iemFileVersion')
    
    configMap['configureBclToFastqPath'] = configParameters.get(ILLUMINA, 'configureBclToFastqPath')
    configMap['failedReads'] = configParameters.get(ILLUMINA, 'failedReads')
    configMap['clusterCount'] = configParameters.get(ILLUMINA, 'clusterCount')
    configMap['clusterCountNumber'] = configParameters.get(ILLUMINA, 'clusterCountNumber')
    configMap['outputDir'] = configParameters.get(ILLUMINA, 'outputDir')
    configMap['sampleSheetName'] = configParameters.get(ILLUMINA, 'sampleSheetName')
    configMap['baseMask'] = configParameters.get(ILLUMINA, 'baseMask')
    
    return configMap


def getDate():
    d = datetime.now()
    return d.strftime('%A, %d of %B %Y')


def sanitize_string(myString):
    return re.sub('[^A-Za-z0-9]+', '_', myString)


def get_vocabulary(vocabulary_code, service):
    """
    Returns the vocabulary terms and vocabulary labels of a vocabulary in a dictionary
    specified by the parameter vocabularyCode
    """
    
    terms = []
    vocabularies = service.listVocabularies()
    vocabulary_dict = {}
    for vocabulary in vocabularies:
        if vocabulary.getCode() == vocabulary_code:
            terms = vocabulary.getTerms()
    if terms:
        for term in terms:
            vocabulary_dict[term.getCode()] = term.getLabel()
    else:
        print ('No vocabulary found for ' + vocabulary_code)
    return vocabulary_dict


def send_email(emails, files, flowCellName, configMap, logger):
    """
    Send out an email to the specified recipients
    """
    COMMASPACE = ', '
    emails_list = emails.split()
    
    msg = MIMEMultipart()
    msg['From'] = configMap['mailFrom']
    msg['To'] = COMMASPACE.join(emails_list)
    msg['Date'] = formatdate(localtime=True)
    msg['Subject'] = 'Generated Sample Sheet for flowcell ' + flowCellName
    
    msg.attach(MIMEText('Sample Sheet for ' + flowCellName + ' attached.'))
    
    for f in files:
        part = MIMEBase('application', 'octet-stream')
        part.set_payload(open(f, 'rb').read())
        Encoders.encode_base64(part)
        part.add_header('Content-Disposition', 'attachment; filename="%s"' % os.path.basename(f))
        msg.attach(part)
    
    smtp = smtplib.SMTP(configMap['smptHost'])
    smtp.sendmail(configMap['mailFrom'], emails_list, msg.as_string())
    smtp.close()
    logger.info('Sent email to ' + COMMASPACE.join(emails_list))


def get_flowcell (illuminaFlowCellTypeName, flowCellName, service, logger):
    """
    Getting the the matching FlowCell
    """
    sc = SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, illuminaFlowCellTypeName));
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellName));
    foundSample = service.searchForSamples(sc)
    try:
        assert foundSample.size() == 1
    except AssertionError:
        print (str(foundSample.size()) + ' flow cells found which match.')
        exit(1)

    logger.info('Found ' + foundSample[0].getCode() + ' in openBIS')
    # Search for contained samples
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
    foundContainedSamples = service.searchForSamples(sampleSc)

    return foundSample[0], foundContainedSamples


def get_reverse_complement(sequence):
    lookup_table = {'A': 'T', 'T': 'A', 'G': 'C', 'C': 'G'}
    reverse_complement = ''
    for nucleotide in reversed(sequence):
        reverse_complement += lookup_table[nucleotide]
    return reverse_complement


def get_model(run_id):
    """
    Guesses the sequencer model from the run folder name

    Current Naming schema for Illumina run folders, as far as I know,
    no documentation found on this, Illumina introduced a field called
    <InstrumentID> on the NextSeq runParameters.xml. That might be an
    option for the future. Alternatively a combination of the fields
    <ApplicationName> and <ApplicationVersion>.

    MiSeq: 150130_M01761_0114_000000000-ACUR0
    NextSeq: 150202_NS500318_0047_AH3KLMBGXX
    HiSeq 2000: 130919_SN792_0281_BD2CHRACXX
    HiSeq 2500: 150203_D00535_0052_AC66RWANXX
    HiSeq 3000: 150724_J00121_0017_AH2VYMBBXX
    HiSeq 4000: 150210_K00111_0013_AH2372BBXX
    HiSeq X: 141121_ST-E00107_0356_AH00C3CCXX
    """
    date, machine_id, run_number, fc_string = os.path.basename(run_id).split("_")

    if machine_id.startswith("NS"):
        model = Sequencers.NEXTSEQ_500
    elif machine_id.startswith("M"):
        model = Sequencers.MISEQ
    elif machine_id.startswith("D"):
        model = Sequencers.HISEQ_2500
    elif machine_id.startswith("SN"):
        model = Sequencers.HISEQ_2000
    elif machine_id.startswith("J"):
        model = Sequencers.HISEQ_3000
    elif machine_id.startswith("K"):
        model = Sequencers.HISEQ_4000
    elif machine_id.startswith("ST"):
        model = Sequencers.HISEQ_X
    else:
        model = Sequencers.UNIDENTIFIED
    return model


def get_parents(sampleName, service):
    """
    Returns a list of parents of a sample 
    """
    sc = SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleName));
    foundSample = service.searchForSamples(sc)
    
    try:
        assert foundSample.size() == 1
    except AssertionError:
        print (str(foundSample.size()) + ' flow lanes found which match.')
    
    # set the criteria for getting the parents when providing the child name
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(sc))
    foundParentSamples = service.searchForSamples(sampleSc)
    
    return foundParentSamples


def get_contained_sample_properties(contained_samples, service):
    """
    Takes a  list of contained samples, retrieves the parents and their properties and returns it
    as a dictionary. The key is the sample name, the value is a list of the properties

    Additionally a dictionary with the lane (key) and the number of samples (value) is returned
    """
    parentDict = {}
    samplesPerLaneDict = {}

    for lane in contained_samples:
        parents = get_parents (lane.getCode(), service)

        try:
            assert parents.size() >= 1
        except AssertionError:
            print (str(parents.size()) + ' parents found for lane ' + lane.getCode())

        samplesPerLaneDict[lane.getCode()[-1]] = len(parents)

        for parent in parents:
            parentCode = parent.getCode()
            parentProperties = parent.getProperties()
            propertyDict = {}
            for property in parentProperties:
                propertyDict[property] = parentProperties.get(property)

            propertyDict['LANE'] = lane.getCode()

            myKey = sanitize_string(parentCode + '_' + lane.getCode())
            parentDict[myKey] = propertyDict

    return parentDict, samplesPerLaneDict



def transform_sample_to_dict(foundFlowCell):
    """
    converts <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
    """
    flowCellDict = {}
    fcProperties = foundFlowCell.getProperties()
    for property in fcProperties:
        flowCellDict[property] = fcProperties.get(property)
    flowCellDict['Project'] = foundFlowCell.getExperimentIdentifierOrNull().split('/')[-1]
    flowCellDict['Name'] = foundFlowCell.getIdentifier().split('/')[-1]
    return flowCellDict


def write_sample_sheet(sampleSheetDict, headerList, myoptions, logger, fileName):
    """
    Writes the given dictionary to a csv file. The order does not matter. As the
    header is not fixed we first need to write the headerList in the file.
    """
    newline = lineending[myoptions.lineending]
    try:
        with open(fileName, 'wb') as sampleSheetFile:
            for header_element in headerList:
                if myoptions.verbose:
                    print header_element
                sampleSheetFile.write(header_element + newline)
            for sample in sampleSheetDict:
                if myoptions.verbose:
                    print sampleSheetDict[sample][0]
                sampleSheetFile.write(sampleSheetDict[sample][0] + newline)
            logger.info('Writing file ' + fileName)
    except IOError:
        logger.error('File error: ' + str(err))
        print ('File error: ' + str(err))
    return fileName


def write_sample_sheet_single_lane(ordered_sample_sheet_dict, flowCellDict,
                                         parentDict, configMap, myoptions, logger, csv_file):
    
    newline = lineending[myoptions.lineending]
    header_list = create_header_section (configMap, parentDict, flowCellDict)

    for lane in range(1, int(flowCellDict[configMap['laneCount']]) + 1):
        per_lane_dict = [ordered_sample_sheet_dict[key] for key in ordered_sample_sheet_dict.keys() if int(key[0]) == lane]
        csv_file_path = myoptions.outdir + csv_file + "_" + str(lane) + CSV
        try:
            with open(csv_file_path, 'wb') as sample_sheet_file:                
                for header_element in header_list:
                    sample_sheet_file.write(header_element + newline)
                for sample in per_lane_dict:
                    sample_sheet_file.write(str(sample[0]) + newline)
        except IOError:
            logger.error('File error: ' + str(err))
            print ('File error: ' + str(err))


def create_header_section (configMap, parentDict, flowCellDict):

    kitsDict = {"CHIP_SEQ_SAMPLE_PREP" : ["",""],
                "TRUSEQ_RNA_SAMPLEPREPKIT_V2_ILLUMINA" : ["A","TruSeq LT"],
                "NEXTERA_XT_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["S", "Nextera XT"],
                "TRUSEQ_CHIP_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
                "MRNA_SEQ_SAMPLE_PREP" : ["",""],
                "TRUSEQRNA_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
                "NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1" : ["A","TruSeq LT"],
                "NEBNEXT_CHIP-SEQ_LIBRARY_PREP_REAGENT_SET" : ["A","TruSeq LT"],
                "RIBOZERO_SCRIPTSEQ_MRNA-SEQ_KIT" : ["",""],
                "NEXTERA_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["N", "Nextera"],
                "GENOMICDNA_SAMPLE_PREP" : ["",""],
                "AGILENT_SURESELECTXT_AUTOMATEDLIBRARYPREP" : ["",""],
                "TRUSEQ_DNA_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
                "NEXTERA_DNA_SAMPLE_PREP_KITS" : ["N", "Nextera"],
                "AGILENT_SURESELECT_ENRICHMENTSYSTEM" : ["",""],
                "TRUSEQ_DNA_SAMPLE_PREP_KIT_V2" : ["A","TruSeq LT"],
                "AGILENT_SURESELECT_HUMAN_ALL_EXON_V5_UTRS" : ["",""],
                "POLYA_SCRIPTSEQ_MRNA-SEQ_KIT" : ["",""],
                "AGILENT_SURESELECTXT2_MOUSE_ALL_EXON" : ["",""],
                "PAIRED_END_DNA_SAMPLE_PREP" : ["",""],
                "NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW" : ["N", "Nextera"]
    }
    
    separator = configMap['separator']
    header_list = []
  
    # here we take the first sample to determine the Sample Prep Kit 
    try:
        assay = kitsDict [parentDict.itervalues().next()[configMap['kit']]][1]
    except:
        print "No Kit set for sample. Will not set the assay value in the sample sheet"
        assay = ""
    
    header_section = configMap['headerSection'].split(separator)
    header_section.reverse()
    header_list = [header_section.pop().strip()]
    header_list.append(header_section.pop().strip() + separator + configMap['iemFileVersion'])
    header_list.append(header_section.pop().strip() + separator + configMap['facilityInstitution'])
    header_list.append(header_section.pop().strip() + separator + configMap['facilityName'])
    header_list.append(header_section.pop().strip() + separator + flowCellDict['Name'])
    header_list.append(header_section.pop().strip() + separator + datetime.now().strftime('%m/%d/%Y'))
    header_list.append(header_section.pop().strip() + separator + configMap['workflow'])
    header_list.append(header_section.pop().strip() + separator + configMap['application'])
    header_list.append(header_section.pop().strip() + separator + assay) 
    header_list.append(header_section.pop().strip() + separator + flowCellDict[configMap['endType']] + '_' + flowCellDict[configMap['cycles']])
    header_list.append(header_section.pop().strip() + separator + configMap['chemistry'])
    header_list.append('')

    reads_section = configMap['readsSection'].split(separator)
    reads_section.reverse()
    header_list.append(reads_section.pop())
    header_list.append(flowCellDict[configMap['cycles']])
    if (flowCellDict[configMap['endType']] == 'PAIRED_END'):
        header_list.append(flowCellDict[configMap['cycles']])
    header_list.append('')

    settings_section = configMap['settingsSection'].split(separator)
    settings_section.reverse()
    header_list.append(settings_section.pop())
    if ('nextera' in assay.lower()):
        header_list.append(configMap['nexteraAdapter'])
    if ('truseq' in assay.lower()):
        header_list.append(configMap['truSeqAdapter1'])
        header_list.append(configMap['truSeqAdapter2'])
    header_list.append('')

    if int(flowCellDict['INDEXREAD2']) > 0:
        SeqDataSection = configMap['dataSectionDualRead'].split(',')
    else:
        SeqDataSection = configMap['dataSectionSingleRead'].split(',')

    SeqDataSection.reverse()
    header_list.append(SeqDataSection.pop())
    header_list.append(','.join(SeqDataSection.pop().strip().split()))
    
    return header_list
    

def verify_index_length (parentDict, flowCellDict, configMap, logger):
    
    index_length_dict = {}
    verified_per_lane_dict = []
    
    flowcell_len_index1 = int(flowCellDict['INDEXREAD'])
    flowcell_len_index2 = int(flowCellDict['INDEXREAD2'])
    
    print("Flowcell has index length [" + str(flowcell_len_index1) + ", " + str(flowcell_len_index2) + "]")

    for lane in range(1,int(flowCellDict['LANECOUNT'])+1):
        index1_set = set ()
        index2_set = set ()
        index1_length = 0
        index2_length = 0
        
        logger.info("Lane: " + str(lane))
        per_lane_list = [parentDict[key] for key in parentDict.keys() if int(key[-1]) == lane]
        
        for sample in per_lane_list:
            # If no index then just skip this  sample
            if (configMap['index1Name'] not in sample) or (sample[configMap['index1Name']] == 'NOINDEX'):
                continue
            index1 = sample[configMap['index1Name']]
            index2=""
            if configMap['index2Name'] in sample:
                index2 = sample[configMap['index2Name']]
            
            index1_set.add(len(index1))
            if index2:
                index2_set.add(len(index2))
            else:
                index2_set.add(0)
                
        # adding the index length of the flow cell to make sure that dual-indexed 
        # samples also work on a single-indexed run
        index1_set.add(flowcell_len_index1)
        index2_set.add(flowcell_len_index2)
         
        if index1_set:
            index1_length = min(index1_set)
        if index2_set:
            index2_length = min(index2_set)

        index_length_dict[lane] = [index1_length, index2_length]
        logger.info("Index1 Length Set: " + str(index1_set))
        logger.info("Index2 Length Set: " + str(index2_set))
        logger.info("Final length of index1 " + str(index1_length))
        logger.info("Final length of index2 " + str(index2_length))
        #print("Lane " + str(lane) + " [" + str(index1_length) + "," + str(index2_length) + "]")
                    
    return index_length_dict


def create_sample_sheet_dict(model, parentDict, flowCellDict, configMap, index1Vocabulary,
                              index2Vocabulary, flowCellName, logger):

    sampleSheetDict = {}
    separator = configMap['separator']

    index_length_dict = verify_index_length(parentDict, flowCellDict, configMap, logger)
    print(index_length_dict)

    for key in parentDict.keys():
        lane = parentDict[key]['LANE'][-1:]
        # If no index then just skip this  sample
        if (configMap['index1Name'] not in parentDict[key]) or (parentDict[key][configMap['index1Name']] == 'NOINDEX'):
            continue
        index1 = parentDict[key][configMap['index1Name']]
        index2=""
        if configMap['index2Name'] in parentDict[key]:
            index2 = parentDict[key][configMap['index2Name']]
            indexNumber = index2Vocabulary[parentDict[key][configMap['index2Name']]].split()[2]
    
        try:
            kit = parentDict[key][configMap['kit']]
            prefix = kitsDict[kit][0]
        except:
    #       print "Missing Kit on " + str(key)
            prefix = ""

        len_index1 = index_length_dict[int(lane)][0]
        len_index2 = index_length_dict[int(lane)][1]

        lane_string =""
        if model in HISEQ_LIST or model in Sequencers.MISEQ:
            lane_string = lane + separator
    
        if int(flowCellDict['INDEXREAD2']) > 0 and len_index2 > 0:
            if model in Sequencers.NEXTSEQ_500:
                index2_processed = get_reverse_complement(index2[0:len_index2])
            else:
                index2_processed = index2
          
            sampleSheetDict[lane + '_' + key] = [
                                lane_string
                                + key + separator
                                + key + '_' + sanitize_string(parentDict[key][configMap['externalSampleName']]) + '_' + index1[0:len_index1] + '_' + index2[0:len_index2] + separator
                                + separator
                                + separator
                                + index1Vocabulary[index1].split()[1] + separator
                                + index1[0:len_index1] + separator
                                + prefix + indexNumber + separator
                                + index2_processed + separator
                                + key + separator
                                ]
        else:
            sampleSheetDict[lane + '_' + key] = [
                                  lane_string
                                + key + separator
                                + key + '_' + sanitize_string(parentDict[key][configMap['externalSampleName']]) + '_' + index1[0:len_index1] + separator
                                + separator
                                + separator
                                + index1Vocabulary[index1].split()[1] + separator
                                + index1[0:len_index1] + separator
                                + key + separator
                                ]
    
    csv_file_name = configMap['SampleSheetFileName'] + '_' + flowCellName
    ordered_sample_sheet_dict = OrderedDict(sorted(sampleSheetDict.items(), key=lambda t: t[0]))

    return ordered_sample_sheet_dict, csv_file_name

'''
Main script
'''

def main ():

    logger = setUpLogger('log/')
    logger.info('Started Creation of Sample Sheet...')

    myoptions = parseOptions(logger)

    if myoptions.debug:
        logger.setLevel(logging.DEBUG)

    flowCellName = myoptions.flowcell
    config_dict = readConfig(logger)
    service = login(logger, config_dict)

    foundFlowCell, containedSamples = get_flowcell(config_dict['illuminaFlowCellTypeName'], flowCellName,
                                                service, logger)
    parentDict, samplesPerLaneDict = get_contained_sample_properties(containedSamples, service)
    logger.info('Found ' + str(len(parentDict)) + ' samples on the flow cell ' + flowCellName)

    flowCellName = foundFlowCell.getCode()
    flowCellDict = transform_sample_to_dict(foundFlowCell)
    model = get_model(flowCellDict['RUN_NAME_FOLDER'])
    print("Auto-detected: " + model)
    logger.info("Auto-detected: " + model)

    index1Vocabulary = get_vocabulary(config_dict['index1Name'], service)
    index2Vocabulary = get_vocabulary(config_dict['index2Name'], service)
    ordered_sample_sheet_dict, csv_file_name = create_sample_sheet_dict(model, parentDict,
                            flowCellDict, config_dict, index1Vocabulary, index2Vocabulary, flowCellName, logger)
    
    if myoptions.singlelane:
        write_sample_sheet_single_lane(ordered_sample_sheet_dict, flowCellDict,
                                          parentDict, config_dict, myoptions, logger, csv_file_name)
    else:
        header_list = create_header_section (config_dict, parentDict, flowCellDict)
        sampleSheetFile = write_sample_sheet(ordered_sample_sheet_dict, header_list, 
                                          myoptions, logger, myoptions.outdir + csv_file_name + CSV)
        if myoptions.maillist:
            sendMail(config_dict['mailList'], [sampleSheetFile], flowCellName, config_dict, logger)

    logout(service, logger)

if __name__ == "__main__":
    main()
