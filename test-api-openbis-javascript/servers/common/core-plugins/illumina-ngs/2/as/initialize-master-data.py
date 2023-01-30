# -*- coding: utf-8 -*-
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

print ("Importing Master Data...")

tr = service.transaction()


file_type_FASTQ_PHRED_64 = tr.getOrCreateNewFileFormatType('FASTQ_PHRED_64')
file_type_FASTQ_PHRED_64.setDescription('FastQ Format with PHRED+64 quality values (as deliverd by Illumina GA Pipeline >= 1.3)')
   
print "Imported 7 File Formats"     
vocabulary_AGILENT_KIT = tr.getOrCreateNewVocabulary('AGILENT_KIT')
vocabulary_AGILENT_KIT.setDescription(None)
vocabulary_AGILENT_KIT.setUrlTemplate(None)
vocabulary_AGILENT_KIT.setManagedInternally(False)
vocabulary_AGILENT_KIT.setInternalNamespace(False)
vocabulary_AGILENT_KIT.setChosenFromList(True)
          
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000 = tr.createNewVocabularyTerm('AGILENT_DNA_KIT_1000')
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setDescription(None)
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setLabel(None)
vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000.setOrdinal(1)
vocabulary_AGILENT_KIT.addTerm(vocabulary_term_AGILENT_KIT_AGILENT_DNA_KIT_1000)
          
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT = tr.createNewVocabularyTerm('AGILENT_HIGH_SENSITIVITY_DNA_KIT')
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setDescription(None)
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setLabel(None)
vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT.setOrdinal(2)
vocabulary_AGILENT_KIT.addTerm(vocabulary_term_AGILENT_KIT_AGILENT_HIGH_SENSITIVITY_DNA_KIT)
    
vocabulary_ALIGNMENT_SOFTWARE = tr.getOrCreateNewVocabulary('ALIGNMENT_SOFTWARE')
vocabulary_ALIGNMENT_SOFTWARE.setDescription('If an alignment is requested, which software package should be use?')
vocabulary_ALIGNMENT_SOFTWARE.setUrlTemplate(None)
vocabulary_ALIGNMENT_SOFTWARE.setManagedInternally(False)
vocabulary_ALIGNMENT_SOFTWARE.setInternalNamespace(False)
vocabulary_ALIGNMENT_SOFTWARE.setChosenFromList(True)
          
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED = tr.createNewVocabularyTerm('NOT_NEEDED')
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED.setOrdinal(1)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_NOT_NEEDED)
          
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND = tr.createNewVocabularyTerm('ELAND')
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_ELAND.setOrdinal(2)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_ELAND)
          
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ = tr.createNewVocabularyTerm('MAQ')
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_MAQ.setOrdinal(3)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_MAQ)
          
vocabulary_term_ALIGNMENT_SOFTWARE_BWA = tr.createNewVocabularyTerm('BWA')
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BWA.setOrdinal(4)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_BWA)
          
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN = tr.createNewVocabularyTerm('NOVOALIGN')
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN.setOrdinal(6)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_NOVOALIGN)
          
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE = tr.createNewVocabularyTerm('BOWTIE')
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setDescription(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setLabel(None)
vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE.setOrdinal(7)
vocabulary_ALIGNMENT_SOFTWARE.addTerm(vocabulary_term_ALIGNMENT_SOFTWARE_BOWTIE)
    
vocabulary_CASAVA_VERSION = tr.getOrCreateNewVocabulary('CASAVA_VERSION')
vocabulary_CASAVA_VERSION.setDescription('Post analyzing software')
vocabulary_CASAVA_VERSION.setUrlTemplate(None)
vocabulary_CASAVA_VERSION.setManagedInternally(False)
vocabulary_CASAVA_VERSION.setInternalNamespace(False)
vocabulary_CASAVA_VERSION.setChosenFromList(True)
          
vocabulary_term_CASAVA_VERSION_18 = tr.createNewVocabularyTerm('1.8')
vocabulary_term_CASAVA_VERSION_18.setDescription(None)
vocabulary_term_CASAVA_VERSION_18.setLabel(None)
vocabulary_term_CASAVA_VERSION_18.setOrdinal(1)
vocabulary_CASAVA_VERSION.addTerm(vocabulary_term_CASAVA_VERSION_18)
          
vocabulary_term_CASAVA_VERSION_17 = tr.createNewVocabularyTerm('1.7')
vocabulary_term_CASAVA_VERSION_17.setDescription(None)
vocabulary_term_CASAVA_VERSION_17.setLabel(None)
vocabulary_term_CASAVA_VERSION_17.setOrdinal(2)
vocabulary_CASAVA_VERSION.addTerm(vocabulary_term_CASAVA_VERSION_17)
    
vocabulary_CLUSTER_GENERATION_KIT_VERSION = tr.getOrCreateNewVocabulary('CLUSTER_GENERATION_KIT_VERSION')
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setDescription('Version of the Cluster Generation Kit')
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setUrlTemplate(None)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setManagedInternally(False)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setInternalNamespace(False)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.setChosenFromList(True)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_HS_V3')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setLabel('TruSeq cBot-HS v3')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3.setOrdinal(1)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V3)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_HS_V2.5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setLabel('TruSeq cBot-HS v2.5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25.setOrdinal(2)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_HS_V25)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2 = tr.createNewVocabularyTerm('TRUSEQ_CBOT_GA_V2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setLabel('TrueSeq cBot-GA v2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2.setOrdinal(3)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CBOT_GA_V2)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5 = tr.createNewVocabularyTerm('TRUSEQ_CS_GA_V5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setLabel('TrueSeq CS-GA v5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5.setOrdinal(4)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_TRUSEQ_CS_GA_V5)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5 = tr.createNewVocabularyTerm('V5')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5.setOrdinal(5)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V5)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4 = tr.createNewVocabularyTerm('V4')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4.setOrdinal(6)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V4)
          
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2 = tr.createNewVocabularyTerm('V2')
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setDescription(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setLabel(None)
vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2.setOrdinal(7)
vocabulary_CLUSTER_GENERATION_KIT_VERSION.addTerm(vocabulary_term_CLUSTER_GENERATION_KIT_VERSION_V2)
    
vocabulary_CLUSTER_STATION = tr.getOrCreateNewVocabulary('CLUSTER_STATION')
vocabulary_CLUSTER_STATION.setDescription('Cluster Station')
vocabulary_CLUSTER_STATION.setUrlTemplate(None)
vocabulary_CLUSTER_STATION.setManagedInternally(False)
vocabulary_CLUSTER_STATION.setInternalNamespace(False)
vocabulary_CLUSTER_STATION.setChosenFromList(True)
          
vocabulary_term_CLUSTER_STATION_CBOT = tr.createNewVocabularyTerm('CBOT')
vocabulary_term_CLUSTER_STATION_CBOT.setDescription(None)
vocabulary_term_CLUSTER_STATION_CBOT.setLabel('cBot')
vocabulary_term_CLUSTER_STATION_CBOT.setOrdinal(1)
vocabulary_CLUSTER_STATION.addTerm(vocabulary_term_CLUSTER_STATION_CBOT)
          
vocabulary_term_CLUSTER_STATION_CBOT2 = tr.createNewVocabularyTerm('CBOT2')
vocabulary_term_CLUSTER_STATION_CBOT2.setDescription('Second cBot')
vocabulary_term_CLUSTER_STATION_CBOT2.setLabel('cBot2')
vocabulary_term_CLUSTER_STATION_CBOT2.setOrdinal(2)
vocabulary_CLUSTER_STATION.addTerm(vocabulary_term_CLUSTER_STATION_CBOT2)
    
vocabulary_CONTROL_LANE = tr.getOrCreateNewVocabulary('CONTROL_LANE')
vocabulary_CONTROL_LANE.setDescription(None)
vocabulary_CONTROL_LANE.setUrlTemplate(None)
vocabulary_CONTROL_LANE.setManagedInternally(False)
vocabulary_CONTROL_LANE.setInternalNamespace(False)
vocabulary_CONTROL_LANE.setChosenFromList(True)
          
vocabulary_term_CONTROL_LANE_1 = tr.createNewVocabularyTerm('1')
vocabulary_term_CONTROL_LANE_1.setDescription(None)
vocabulary_term_CONTROL_LANE_1.setLabel(None)
vocabulary_term_CONTROL_LANE_1.setOrdinal(1)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_1)
          
vocabulary_term_CONTROL_LANE_2 = tr.createNewVocabularyTerm('2')
vocabulary_term_CONTROL_LANE_2.setDescription(None)
vocabulary_term_CONTROL_LANE_2.setLabel(None)
vocabulary_term_CONTROL_LANE_2.setOrdinal(2)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_2)
          
vocabulary_term_CONTROL_LANE_3 = tr.createNewVocabularyTerm('3')
vocabulary_term_CONTROL_LANE_3.setDescription(None)
vocabulary_term_CONTROL_LANE_3.setLabel(None)
vocabulary_term_CONTROL_LANE_3.setOrdinal(3)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_3)
          
vocabulary_term_CONTROL_LANE_4 = tr.createNewVocabularyTerm('4')
vocabulary_term_CONTROL_LANE_4.setDescription(None)
vocabulary_term_CONTROL_LANE_4.setLabel(None)
vocabulary_term_CONTROL_LANE_4.setOrdinal(4)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_4)
          
vocabulary_term_CONTROL_LANE_5 = tr.createNewVocabularyTerm('5')
vocabulary_term_CONTROL_LANE_5.setDescription(None)
vocabulary_term_CONTROL_LANE_5.setLabel(None)
vocabulary_term_CONTROL_LANE_5.setOrdinal(5)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_5)
          
vocabulary_term_CONTROL_LANE_6 = tr.createNewVocabularyTerm('6')
vocabulary_term_CONTROL_LANE_6.setDescription(None)
vocabulary_term_CONTROL_LANE_6.setLabel(None)
vocabulary_term_CONTROL_LANE_6.setOrdinal(6)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_6)
          
vocabulary_term_CONTROL_LANE_7 = tr.createNewVocabularyTerm('7')
vocabulary_term_CONTROL_LANE_7.setDescription(None)
vocabulary_term_CONTROL_LANE_7.setLabel(None)
vocabulary_term_CONTROL_LANE_7.setOrdinal(7)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_7)
          
vocabulary_term_CONTROL_LANE_8 = tr.createNewVocabularyTerm('8')
vocabulary_term_CONTROL_LANE_8.setDescription(None)
vocabulary_term_CONTROL_LANE_8.setLabel(None)
vocabulary_term_CONTROL_LANE_8.setOrdinal(8)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_8)
          
vocabulary_term_CONTROL_LANE_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_CONTROL_LANE_NONE.setDescription(None)
vocabulary_term_CONTROL_LANE_NONE.setLabel(None)
vocabulary_term_CONTROL_LANE_NONE.setOrdinal(9)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_NONE)
          
vocabulary_term_CONTROL_LANE_0 = tr.createNewVocabularyTerm('0')
vocabulary_term_CONTROL_LANE_0.setDescription(None)
vocabulary_term_CONTROL_LANE_0.setLabel(None)
vocabulary_term_CONTROL_LANE_0.setOrdinal(10)
vocabulary_CONTROL_LANE.addTerm(vocabulary_term_CONTROL_LANE_0)
    
vocabulary_END_TYPE = tr.getOrCreateNewVocabulary('END_TYPE')
vocabulary_END_TYPE.setDescription('Sequencing method')
vocabulary_END_TYPE.setUrlTemplate(None)
vocabulary_END_TYPE.setManagedInternally(False)
vocabulary_END_TYPE.setInternalNamespace(False)
vocabulary_END_TYPE.setChosenFromList(True)
          
vocabulary_term_END_TYPE_SINGLE_READ = tr.createNewVocabularyTerm('SINGLE_READ')
vocabulary_term_END_TYPE_SINGLE_READ.setDescription(None)
vocabulary_term_END_TYPE_SINGLE_READ.setLabel(None)
vocabulary_term_END_TYPE_SINGLE_READ.setOrdinal(1)
vocabulary_END_TYPE.addTerm(vocabulary_term_END_TYPE_SINGLE_READ)
          
vocabulary_term_END_TYPE_PAIRED_END = tr.createNewVocabularyTerm('PAIRED_END')
vocabulary_term_END_TYPE_PAIRED_END.setDescription(None)
vocabulary_term_END_TYPE_PAIRED_END.setLabel(None)
vocabulary_term_END_TYPE_PAIRED_END.setOrdinal(2)
vocabulary_END_TYPE.addTerm(vocabulary_term_END_TYPE_PAIRED_END)
    
vocabulary_EXPERIMENT_DESIGN = tr.getOrCreateNewVocabulary('EXPERIMENT_DESIGN')
vocabulary_EXPERIMENT_DESIGN.setDescription('General Intent')
vocabulary_EXPERIMENT_DESIGN.setUrlTemplate(None)
vocabulary_EXPERIMENT_DESIGN.setManagedInternally(False)
vocabulary_EXPERIMENT_DESIGN.setInternalNamespace(False)
vocabulary_EXPERIMENT_DESIGN.setChosenFromList(True)
          
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION = tr.createNewVocabularyTerm('BINDING_SITE_IDENTIFICATION')
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setLabel('Binding Site Identification')
vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION.setOrdinal(1)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_BINDING_SITE_IDENTIFICATION)
          
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS = tr.createNewVocabularyTerm('CHROMATIN_MARKS')
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setLabel('Chromatin Marks')
vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS.setOrdinal(2)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_CHROMATIN_MARKS)
          
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION = tr.createNewVocabularyTerm('COMPARATIVE_GENOMIC_HYBRIDIZATION')
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setLabel('Comparative Genomic Hybridization')
vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION.setOrdinal(3)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_COMPARATIVE_GENOMIC_HYBRIDIZATION)
          
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION = tr.createNewVocabularyTerm('DIFFERENTIAL_EXPRESSION')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setLabel('Differential Expression')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION.setOrdinal(4)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_EXPRESSION)
          
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING = tr.createNewVocabularyTerm('DIFFERENTIAL_SPLICING')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setLabel('Differential Splicing')
vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING.setOrdinal(5)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_DIFFERENTIAL_SPLICING)
          
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION = tr.createNewVocabularyTerm('EXPRESSION')
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setLabel('Expression')
vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION.setOrdinal(6)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_EXPRESSION)
          
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT = tr.createNewVocabularyTerm('SEQUENCE_ENRICHMENT')
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setLabel('Sequence Enrichment')
vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT.setOrdinal(7)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_SEQUENCE_ENRICHMENT)
          
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION = tr.createNewVocabularyTerm('TRANSCRIPT_IDENTIFICATION')
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setLabel('Transcript Identification')
vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION.setOrdinal(8)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_TRANSCRIPT_IDENTIFICATION)
          
vocabulary_term_EXPERIMENT_DESIGN_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setDescription(None)
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setLabel('Other')
vocabulary_term_EXPERIMENT_DESIGN_OTHER.setOrdinal(9)
vocabulary_EXPERIMENT_DESIGN.addTerm(vocabulary_term_EXPERIMENT_DESIGN_OTHER)
    
vocabulary_INDEX1 = tr.getOrCreateNewVocabulary('INDEX1')
vocabulary_INDEX1.setDescription('Index 1 for Illumina Indexing')
vocabulary_INDEX1.setUrlTemplate(None)
vocabulary_INDEX1.setManagedInternally(False)
vocabulary_INDEX1.setInternalNamespace(False)
vocabulary_INDEX1.setChosenFromList(True)
          
vocabulary_term_INDEX1_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_INDEX1_NONE.setDescription('No Index')
vocabulary_term_INDEX1_NONE.setLabel(' None')
vocabulary_term_INDEX1_NONE.setOrdinal(1)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_NONE)
          
vocabulary_term_INDEX1_ATCACGA = tr.createNewVocabularyTerm('ATCACGA')
vocabulary_term_INDEX1_ATCACGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ATCACGA.setLabel('Index1 ATCACGA')
vocabulary_term_INDEX1_ATCACGA.setOrdinal(2)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATCACGA)
          
vocabulary_term_INDEX1_CGATGTA = tr.createNewVocabularyTerm('CGATGTA')
vocabulary_term_INDEX1_CGATGTA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_CGATGTA.setLabel('Index2 CGATGTA')
vocabulary_term_INDEX1_CGATGTA.setOrdinal(3)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGATGTA)
          
vocabulary_term_INDEX1_TTAGGCA = tr.createNewVocabularyTerm('TTAGGCA')
vocabulary_term_INDEX1_TTAGGCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_TTAGGCA.setLabel('Index3 TTAGGCA')
vocabulary_term_INDEX1_TTAGGCA.setOrdinal(4)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TTAGGCA)
          
vocabulary_term_INDEX1_TGACCAA = tr.createNewVocabularyTerm('TGACCAA')
vocabulary_term_INDEX1_TGACCAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_TGACCAA.setLabel('Index4 TGACCAA')
vocabulary_term_INDEX1_TGACCAA.setOrdinal(5)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TGACCAA)
          
vocabulary_term_INDEX1_ACAGTGA = tr.createNewVocabularyTerm('ACAGTGA')
vocabulary_term_INDEX1_ACAGTGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ACAGTGA.setLabel('Index5 ACAGTGA')
vocabulary_term_INDEX1_ACAGTGA.setOrdinal(6)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ACAGTGA)
          
vocabulary_term_INDEX1_GCCAATA = tr.createNewVocabularyTerm('GCCAATA')
vocabulary_term_INDEX1_GCCAATA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GCCAATA.setLabel('Index6 GCCAATA')
vocabulary_term_INDEX1_GCCAATA.setOrdinal(7)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GCCAATA)
          
vocabulary_term_INDEX1_CAGATCA = tr.createNewVocabularyTerm('CAGATCA')
vocabulary_term_INDEX1_CAGATCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_CAGATCA.setLabel('Index7 CAGATCA')
vocabulary_term_INDEX1_CAGATCA.setOrdinal(8)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAGATCA)
          
vocabulary_term_INDEX1_ACTTGAA = tr.createNewVocabularyTerm('ACTTGAA')
vocabulary_term_INDEX1_ACTTGAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ACTTGAA.setLabel('Index8 ACTTGAA')
vocabulary_term_INDEX1_ACTTGAA.setOrdinal(9)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ACTTGAA)
          
vocabulary_term_INDEX1_GATCAGA = tr.createNewVocabularyTerm('GATCAGA')
vocabulary_term_INDEX1_GATCAGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GATCAGA.setLabel('Index9 GATCAGA')
vocabulary_term_INDEX1_GATCAGA.setOrdinal(10)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GATCAGA)
          
vocabulary_term_INDEX1_TAGCTTA = tr.createNewVocabularyTerm('TAGCTTA')
vocabulary_term_INDEX1_TAGCTTA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_TAGCTTA.setLabel('Index10 TAGCTTA')
vocabulary_term_INDEX1_TAGCTTA.setOrdinal(11)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TAGCTTA)
          
vocabulary_term_INDEX1_GGCTACA = tr.createNewVocabularyTerm('GGCTACA')
vocabulary_term_INDEX1_GGCTACA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GGCTACA.setLabel('Index11 GGCTACA')
vocabulary_term_INDEX1_GGCTACA.setOrdinal(12)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GGCTACA)
          
vocabulary_term_INDEX1_CTTGTAA = tr.createNewVocabularyTerm('CTTGTAA')
vocabulary_term_INDEX1_CTTGTAA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_CTTGTAA.setLabel('Index12 CTTGTAA')
vocabulary_term_INDEX1_CTTGTAA.setOrdinal(13)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTTGTAA)
          
vocabulary_term_INDEX1_AGATACA = tr.createNewVocabularyTerm('AGATACA')
vocabulary_term_INDEX1_AGATACA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_AGATACA.setLabel('Index13 AGATAC')
vocabulary_term_INDEX1_AGATACA.setOrdinal(14)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGATACA)
          
vocabulary_term_INDEX1_AGTTCCG = tr.createNewVocabularyTerm('AGTTCCG')
vocabulary_term_INDEX1_AGTTCCG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_AGTTCCG.setLabel('Index14 AGTTCCG')
vocabulary_term_INDEX1_AGTTCCG.setOrdinal(15)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGTTCCG)
          
vocabulary_term_INDEX1_ATGTCAG = tr.createNewVocabularyTerm('ATGTCAG')
vocabulary_term_INDEX1_ATGTCAG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ATGTCAG.setLabel('Index15 ATGTCAG')
vocabulary_term_INDEX1_ATGTCAG.setOrdinal(16)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATGTCAG)
          
vocabulary_term_INDEX1_CCGTCCC = tr.createNewVocabularyTerm('CCGTCCC')
vocabulary_term_INDEX1_CCGTCCC.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_CCGTCCC.setLabel('Index16 CCGTCCC')
vocabulary_term_INDEX1_CCGTCCC.setOrdinal(17)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CCGTCCC)
          
vocabulary_term_INDEX1_GTCCGCA = tr.createNewVocabularyTerm('GTCCGCA')
vocabulary_term_INDEX1_GTCCGCA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GTCCGCA.setLabel('Index18 GTCCGCA')
vocabulary_term_INDEX1_GTCCGCA.setOrdinal(18)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTCCGCA)
          
vocabulary_term_INDEX1_GTGAAAC = tr.createNewVocabularyTerm('GTGAAAC')
vocabulary_term_INDEX1_GTGAAAC.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GTGAAAC.setLabel('Index19 GTGAAAC')
vocabulary_term_INDEX1_GTGAAAC.setOrdinal(19)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTGAAAC)
          
vocabulary_term_INDEX1_GTGGCCT = tr.createNewVocabularyTerm('GTGGCCT')
vocabulary_term_INDEX1_GTGGCCT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GTGGCCT.setLabel('Index20 GTGGCCT')
vocabulary_term_INDEX1_GTGGCCT.setOrdinal(20)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTGGCCT)
          
vocabulary_term_INDEX1_GTTTCGG = tr.createNewVocabularyTerm('GTTTCGG')
vocabulary_term_INDEX1_GTTTCGG.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GTTTCGG.setLabel('Index21 GTTTCGG')
vocabulary_term_INDEX1_GTTTCGG.setOrdinal(21)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTTTCGG)
          
vocabulary_term_INDEX1_CGTACGT = tr.createNewVocabularyTerm('CGTACGT')
vocabulary_term_INDEX1_CGTACGT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_CGTACGT.setLabel('Index22 CGTACGT')
vocabulary_term_INDEX1_CGTACGT.setOrdinal(22)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGTACGT)
          
vocabulary_term_INDEX1_GAGTGGA = tr.createNewVocabularyTerm('GAGTGGA')
vocabulary_term_INDEX1_GAGTGGA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_GAGTGGA.setLabel('Index23 GAGTGGA')
vocabulary_term_INDEX1_GAGTGGA.setOrdinal(23)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GAGTGGA)
          
vocabulary_term_INDEX1_ACTGATA = tr.createNewVocabularyTerm('ACTGATA')
vocabulary_term_INDEX1_ACTGATA.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ACTGATA.setLabel('Index25 ACTGATA')
vocabulary_term_INDEX1_ACTGATA.setOrdinal(24)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ACTGATA)
          
vocabulary_term_INDEX1_ATTCCTT = tr.createNewVocabularyTerm('ATTCCTT')
vocabulary_term_INDEX1_ATTCCTT.setDescription('Illumina, Nextera or Scriptseq')
vocabulary_term_INDEX1_ATTCCTT.setLabel('Index27 ATTCCTT')
vocabulary_term_INDEX1_ATTCCTT.setOrdinal(25)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATTCCTT)
          
vocabulary_term_INDEX1_TAAGGCGA = tr.createNewVocabularyTerm('TAAGGCGA')
vocabulary_term_INDEX1_TAAGGCGA.setDescription('Nextera DNA')
vocabulary_term_INDEX1_TAAGGCGA.setLabel('Index1 (i7) N701 TAAGGCGA')
vocabulary_term_INDEX1_TAAGGCGA.setOrdinal(26)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TAAGGCGA)
          
vocabulary_term_INDEX1_CGTACTAG = tr.createNewVocabularyTerm('CGTACTAG')
vocabulary_term_INDEX1_CGTACTAG.setDescription('Nextera DNA')
vocabulary_term_INDEX1_CGTACTAG.setLabel('Index1 (i7) N702 CGTACTAG')
vocabulary_term_INDEX1_CGTACTAG.setOrdinal(27)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGTACTAG)
          
vocabulary_term_INDEX1_AGGCAGAA = tr.createNewVocabularyTerm('AGGCAGAA')
vocabulary_term_INDEX1_AGGCAGAA.setDescription('Nextera DNA')
vocabulary_term_INDEX1_AGGCAGAA.setLabel('Index1 (i7) N703 AGGCAGAA')
vocabulary_term_INDEX1_AGGCAGAA.setOrdinal(28)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGGCAGAA)
          
vocabulary_term_INDEX1_TCCTGAGC = tr.createNewVocabularyTerm('TCCTGAGC')
vocabulary_term_INDEX1_TCCTGAGC.setDescription('Nextera DNA')
vocabulary_term_INDEX1_TCCTGAGC.setLabel('Index1 (i7) N704 TCCTGAGC')
vocabulary_term_INDEX1_TCCTGAGC.setOrdinal(29)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCCTGAGC)
          
vocabulary_term_INDEX1_GGACTCCT = tr.createNewVocabularyTerm('GGACTCCT')
vocabulary_term_INDEX1_GGACTCCT.setDescription('Nextera DNA')
vocabulary_term_INDEX1_GGACTCCT.setLabel('Index1 (i7) N705 GGACTCCT')
vocabulary_term_INDEX1_GGACTCCT.setOrdinal(30)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GGACTCCT)
          
vocabulary_term_INDEX1_TAGGCATG = tr.createNewVocabularyTerm('TAGGCATG')
vocabulary_term_INDEX1_TAGGCATG.setDescription('Nextera DNA')
vocabulary_term_INDEX1_TAGGCATG.setLabel('Index1 (i7) N706 TAGGCATG')
vocabulary_term_INDEX1_TAGGCATG.setOrdinal(31)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TAGGCATG)
          
vocabulary_term_INDEX1_CTCTCTAC = tr.createNewVocabularyTerm('CTCTCTAC')
vocabulary_term_INDEX1_CTCTCTAC.setDescription('Nextera DNA')
vocabulary_term_INDEX1_CTCTCTAC.setLabel('Index1 (i7) N707 CTCTCTAC')
vocabulary_term_INDEX1_CTCTCTAC.setOrdinal(32)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTCTCTAC)
          
vocabulary_term_INDEX1_CAGAGAGG = tr.createNewVocabularyTerm('CAGAGAGG')
vocabulary_term_INDEX1_CAGAGAGG.setDescription('Nextera DNA')
vocabulary_term_INDEX1_CAGAGAGG.setLabel('Index1 (i7) N708 CAGAGAGG')
vocabulary_term_INDEX1_CAGAGAGG.setOrdinal(33)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAGAGAGG)
          
vocabulary_term_INDEX1_GCTACGCT = tr.createNewVocabularyTerm('GCTACGCT')
vocabulary_term_INDEX1_GCTACGCT.setDescription('Nextera DNA')
vocabulary_term_INDEX1_GCTACGCT.setLabel('Index1 (i7) N709 GCTACGCT')
vocabulary_term_INDEX1_GCTACGCT.setOrdinal(34)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GCTACGCT)
          
vocabulary_term_INDEX1_CGAGGCTG = tr.createNewVocabularyTerm('CGAGGCTG')
vocabulary_term_INDEX1_CGAGGCTG.setDescription('Nextera DNA')
vocabulary_term_INDEX1_CGAGGCTG.setLabel('Index1 (i7) N710 CGAGGCTG')
vocabulary_term_INDEX1_CGAGGCTG.setOrdinal(35)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGAGGCTG)
          
vocabulary_term_INDEX1_AAGAGGCA = tr.createNewVocabularyTerm('AAGAGGCA')
vocabulary_term_INDEX1_AAGAGGCA.setDescription('Nextera DNA')
vocabulary_term_INDEX1_AAGAGGCA.setLabel('Index1 (i7) N711 AAGAGGCA')
vocabulary_term_INDEX1_AAGAGGCA.setOrdinal(36)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AAGAGGCA)
          
vocabulary_term_INDEX1_GTAGAGGA = tr.createNewVocabularyTerm('GTAGAGGA')
vocabulary_term_INDEX1_GTAGAGGA.setDescription('Nextera DNA')
vocabulary_term_INDEX1_GTAGAGGA.setLabel('Index1 (i7) N712 GTAGAGGA')
vocabulary_term_INDEX1_GTAGAGGA.setOrdinal(37)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTAGAGGA)
          
vocabulary_term_INDEX1_AAGACTA = tr.createNewVocabularyTerm('AAGACTA')
vocabulary_term_INDEX1_AAGACTA.setDescription(None)
vocabulary_term_INDEX1_AAGACTA.setLabel('02 AAGACT')
vocabulary_term_INDEX1_AAGACTA.setOrdinal(38)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AAGACTA)
          
vocabulary_term_INDEX1_ACTTCAA = tr.createNewVocabularyTerm('ACTTCAA')
vocabulary_term_INDEX1_ACTTCAA.setDescription(None)
vocabulary_term_INDEX1_ACTTCAA.setLabel('10 ACTTCA')
vocabulary_term_INDEX1_ACTTCAA.setOrdinal(39)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ACTTCAA)
          
vocabulary_term_INDEX1_AGGTTGA = tr.createNewVocabularyTerm('AGGTTGA')
vocabulary_term_INDEX1_AGGTTGA.setDescription(None)
vocabulary_term_INDEX1_AGGTTGA.setLabel('17 AGGTTG')
vocabulary_term_INDEX1_AGGTTGA.setOrdinal(40)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGGTTGA)
          
vocabulary_term_INDEX1_TTCGTCA = tr.createNewVocabularyTerm('TTCGTCA')
vocabulary_term_INDEX1_TTCGTCA.setDescription(None)
vocabulary_term_INDEX1_TTCGTCA.setLabel('17 TTCGTCA')
vocabulary_term_INDEX1_TTCGTCA.setOrdinal(41)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TTCGTCA)
          
vocabulary_term_INDEX1_AGCATAA = tr.createNewVocabularyTerm('AGCATAA')
vocabulary_term_INDEX1_AGCATAA.setDescription(None)
vocabulary_term_INDEX1_AGCATAA.setLabel('20 AGCATAA')
vocabulary_term_INDEX1_AGCATAA.setOrdinal(42)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGCATAA)
          
vocabulary_term_INDEX1_ATACGCA = tr.createNewVocabularyTerm('ATACGCA')
vocabulary_term_INDEX1_ATACGCA.setDescription(None)
vocabulary_term_INDEX1_ATACGCA.setLabel('20 ATACGC')
vocabulary_term_INDEX1_ATACGCA.setOrdinal(43)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATACGCA)
          
vocabulary_term_INDEX1_CAGATGA = tr.createNewVocabularyTerm('CAGATGA')
vocabulary_term_INDEX1_CAGATGA.setDescription(None)
vocabulary_term_INDEX1_CAGATGA.setLabel('28 CAGATG')
vocabulary_term_INDEX1_CAGATGA.setOrdinal(44)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAGATGA)
          
vocabulary_term_INDEX1_CCGAATA = tr.createNewVocabularyTerm('CCGAATA')
vocabulary_term_INDEX1_CCGAATA.setDescription(None)
vocabulary_term_INDEX1_CCGAATA.setLabel('34 CCGAAT')
vocabulary_term_INDEX1_CCGAATA.setOrdinal(45)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CCGAATA)
          
vocabulary_term_INDEX1_CGCTCGA = tr.createNewVocabularyTerm('CGCTCGA')
vocabulary_term_INDEX1_CGCTCGA.setDescription(None)
vocabulary_term_INDEX1_CGCTCGA.setLabel('43 CGCTCG')
vocabulary_term_INDEX1_CGCTCGA.setOrdinal(46)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGCTCGA)
          
vocabulary_term_INDEX1_CGTATTA = tr.createNewVocabularyTerm('CGTATTA')
vocabulary_term_INDEX1_CGTATTA.setDescription(None)
vocabulary_term_INDEX1_CGTATTA.setLabel('45 CGTATT')
vocabulary_term_INDEX1_CGTATTA.setOrdinal(47)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGTATTA)
          
vocabulary_term_INDEX1_CCATGAA = tr.createNewVocabularyTerm('CCATGAA')
vocabulary_term_INDEX1_CCATGAA.setDescription(None)
vocabulary_term_INDEX1_CCATGAA.setLabel('50 CCATGAA')
vocabulary_term_INDEX1_CCATGAA.setOrdinal(48)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CCATGAA)
          
vocabulary_term_INDEX1_CTGACCA = tr.createNewVocabularyTerm('CTGACCA')
vocabulary_term_INDEX1_CTGACCA.setDescription(None)
vocabulary_term_INDEX1_CTGACCA.setLabel('50 CTGACC')
vocabulary_term_INDEX1_CTGACCA.setOrdinal(49)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTGACCA)
          
vocabulary_term_INDEX1_GAAGCCA = tr.createNewVocabularyTerm('GAAGCCA')
vocabulary_term_INDEX1_GAAGCCA.setDescription(None)
vocabulary_term_INDEX1_GAAGCCA.setLabel('52 GAAGCC')
vocabulary_term_INDEX1_GAAGCCA.setOrdinal(50)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GAAGCCA)
          
vocabulary_term_INDEX1_NOINDEX = tr.createNewVocabularyTerm('NOINDEX')
vocabulary_term_INDEX1_NOINDEX.setDescription(None)
vocabulary_term_INDEX1_NOINDEX.setLabel(None)
vocabulary_term_INDEX1_NOINDEX.setOrdinal(51)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_NOINDEX)
          
vocabulary_term_INDEX1_GCTGAAA = tr.createNewVocabularyTerm('GCTGAAA')
vocabulary_term_INDEX1_GCTGAAA.setDescription(None)
vocabulary_term_INDEX1_GCTGAAA.setLabel('59 GCTGAA')
vocabulary_term_INDEX1_GCTGAAA.setOrdinal(52)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GCTGAAA)
          
vocabulary_term_INDEX1_GTCGCGA = tr.createNewVocabularyTerm('GTCGCGA')
vocabulary_term_INDEX1_GTCGCGA.setDescription(None)
vocabulary_term_INDEX1_GTCGCGA.setLabel('64 GTCGCG')
vocabulary_term_INDEX1_GTCGCGA.setOrdinal(53)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTCGCGA)
          
vocabulary_term_INDEX1_TAAGATA = tr.createNewVocabularyTerm('TAAGATA')
vocabulary_term_INDEX1_TAAGATA.setDescription(None)
vocabulary_term_INDEX1_TAAGATA.setLabel('65 TAAGAT')
vocabulary_term_INDEX1_TAAGATA.setOrdinal(54)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TAAGATA)
          
vocabulary_term_INDEX1_GCGCTGA = tr.createNewVocabularyTerm('GCGCTGA')
vocabulary_term_INDEX1_GCGCTGA.setDescription(None)
vocabulary_term_INDEX1_GCGCTGA.setLabel('66 GCGCTGA')
vocabulary_term_INDEX1_GCGCTGA.setOrdinal(55)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GCGCTGA)
          
vocabulary_term_INDEX1_TATCGTA = tr.createNewVocabularyTerm('TATCGTA')
vocabulary_term_INDEX1_TATCGTA.setDescription(None)
vocabulary_term_INDEX1_TATCGTA.setLabel('66 TATCGT')
vocabulary_term_INDEX1_TATCGTA.setOrdinal(56)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TATCGTA)
          
vocabulary_term_INDEX1_TCCTACA = tr.createNewVocabularyTerm('TCCTACA')
vocabulary_term_INDEX1_TCCTACA.setDescription(None)
vocabulary_term_INDEX1_TCCTACA.setLabel('68 TCCTAC')
vocabulary_term_INDEX1_TCCTACA.setOrdinal(57)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCCTACA)
          
vocabulary_term_INDEX1_TCTATAA = tr.createNewVocabularyTerm('TCTATAA')
vocabulary_term_INDEX1_TCTATAA.setDescription(None)
vocabulary_term_INDEX1_TCTATAA.setLabel('70 TCTATA')
vocabulary_term_INDEX1_TCTATAA.setOrdinal(58)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCTATAA)
          
vocabulary_term_INDEX1_TTACTTA = tr.createNewVocabularyTerm('TTACTTA')
vocabulary_term_INDEX1_TTACTTA.setDescription(None)
vocabulary_term_INDEX1_TTACTTA.setLabel('74 TTACTT')
vocabulary_term_INDEX1_TTACTTA.setOrdinal(59)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TTACTTA)
          
vocabulary_term_INDEX1_TTCCGAA = tr.createNewVocabularyTerm('TTCCGAA')
vocabulary_term_INDEX1_TTCCGAA.setDescription(None)
vocabulary_term_INDEX1_TTCCGAA.setLabel('75 TTCCGA')
vocabulary_term_INDEX1_TTCCGAA.setOrdinal(60)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TTCCGAA)
          
vocabulary_term_INDEX1_ATGTCAA = tr.createNewVocabularyTerm('ATGTCAA')
vocabulary_term_INDEX1_ATGTCAA.setDescription(None)
vocabulary_term_INDEX1_ATGTCAA.setLabel('DNA Adapter 15')
vocabulary_term_INDEX1_ATGTCAA.setOrdinal(61)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATGTCAA)
          
vocabulary_term_INDEX1_CCGTCCA = tr.createNewVocabularyTerm('CCGTCCA')
vocabulary_term_INDEX1_CCGTCCA.setDescription(None)
vocabulary_term_INDEX1_CCGTCCA.setLabel('DNA Adapter 16')
vocabulary_term_INDEX1_CCGTCCA.setOrdinal(62)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CCGTCCA)
          
vocabulary_term_INDEX1_GTAGAGA = tr.createNewVocabularyTerm('GTAGAGA')
vocabulary_term_INDEX1_GTAGAGA.setDescription(None)
vocabulary_term_INDEX1_GTAGAGA.setLabel('DNA Adapter 17')
vocabulary_term_INDEX1_GTAGAGA.setOrdinal(63)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTAGAGA)
          
vocabulary_term_INDEX1_GTGAAAA = tr.createNewVocabularyTerm('GTGAAAA')
vocabulary_term_INDEX1_GTGAAAA.setDescription(None)
vocabulary_term_INDEX1_GTGAAAA.setLabel('DNA Adapter 19')
vocabulary_term_INDEX1_GTGAAAA.setOrdinal(64)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTGAAAA)
          
vocabulary_term_INDEX1_GTGGCCA = tr.createNewVocabularyTerm('GTGGCCA')
vocabulary_term_INDEX1_GTGGCCA.setDescription(None)
vocabulary_term_INDEX1_GTGGCCA.setLabel('DNA Adapter 20')
vocabulary_term_INDEX1_GTGGCCA.setOrdinal(65)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTGGCCA)
          
vocabulary_term_INDEX1_GTTTCGA = tr.createNewVocabularyTerm('GTTTCGA')
vocabulary_term_INDEX1_GTTTCGA.setDescription(None)
vocabulary_term_INDEX1_GTTTCGA.setLabel('DNA Adapter 21')
vocabulary_term_INDEX1_GTTTCGA.setOrdinal(66)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GTTTCGA)
          
vocabulary_term_INDEX1_CGTACGA = tr.createNewVocabularyTerm('CGTACGA')
vocabulary_term_INDEX1_CGTACGA.setDescription(None)
vocabulary_term_INDEX1_CGTACGA.setLabel('DNA Adapter 22')
vocabulary_term_INDEX1_CGTACGA.setOrdinal(67)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGTACGA)
          
vocabulary_term_INDEX1_GGTAGCA = tr.createNewVocabularyTerm('GGTAGCA')
vocabulary_term_INDEX1_GGTAGCA.setDescription(None)
vocabulary_term_INDEX1_GGTAGCA.setLabel('DNA Adapter 24')
vocabulary_term_INDEX1_GGTAGCA.setOrdinal(68)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GGTAGCA)
          
vocabulary_term_INDEX1_ATGAGCA = tr.createNewVocabularyTerm('ATGAGCA')
vocabulary_term_INDEX1_ATGAGCA.setDescription(None)
vocabulary_term_INDEX1_ATGAGCA.setLabel('DNA Adapter 26')
vocabulary_term_INDEX1_ATGAGCA.setOrdinal(69)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATGAGCA)
          
vocabulary_term_INDEX1_ATTCCTA = tr.createNewVocabularyTerm('ATTCCTA')
vocabulary_term_INDEX1_ATTCCTA.setDescription(None)
vocabulary_term_INDEX1_ATTCCTA.setLabel('DNA Adapter 27')
vocabulary_term_INDEX1_ATTCCTA.setOrdinal(70)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_ATTCCTA)
          
vocabulary_term_INDEX1_CAAAAGA = tr.createNewVocabularyTerm('CAAAAGA')
vocabulary_term_INDEX1_CAAAAGA.setDescription(None)
vocabulary_term_INDEX1_CAAAAGA.setLabel('DNA Adapter 28')
vocabulary_term_INDEX1_CAAAAGA.setOrdinal(71)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAAAAGA)
          
vocabulary_term_INDEX1_CAACTAA = tr.createNewVocabularyTerm('CAACTAA')
vocabulary_term_INDEX1_CAACTAA.setDescription(None)
vocabulary_term_INDEX1_CAACTAA.setLabel('DNA Adapter 29')
vocabulary_term_INDEX1_CAACTAA.setOrdinal(72)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAACTAA)
          
vocabulary_term_INDEX1_CACCGGA = tr.createNewVocabularyTerm('CACCGGA')
vocabulary_term_INDEX1_CACCGGA.setDescription(None)
vocabulary_term_INDEX1_CACCGGA.setLabel('DNA Adapter 30')
vocabulary_term_INDEX1_CACCGGA.setOrdinal(73)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CACCGGA)
          
vocabulary_term_INDEX1_CACGATA = tr.createNewVocabularyTerm('CACGATA')
vocabulary_term_INDEX1_CACGATA.setDescription(None)
vocabulary_term_INDEX1_CACGATA.setLabel('DNA Adapter 31')
vocabulary_term_INDEX1_CACGATA.setOrdinal(74)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CACGATA)
          
vocabulary_term_INDEX1_CACTCAA = tr.createNewVocabularyTerm('CACTCAA')
vocabulary_term_INDEX1_CACTCAA.setDescription(None)
vocabulary_term_INDEX1_CACTCAA.setLabel('DNA Adapter 32')
vocabulary_term_INDEX1_CACTCAA.setOrdinal(75)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CACTCAA)
          
vocabulary_term_INDEX1_CAGGCGA = tr.createNewVocabularyTerm('CAGGCGA')
vocabulary_term_INDEX1_CAGGCGA.setDescription(None)
vocabulary_term_INDEX1_CAGGCGA.setLabel('DNA Adapter 33')
vocabulary_term_INDEX1_CAGGCGA.setOrdinal(76)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CAGGCGA)
          
vocabulary_term_INDEX1_CATGGCA = tr.createNewVocabularyTerm('CATGGCA')
vocabulary_term_INDEX1_CATGGCA.setDescription(None)
vocabulary_term_INDEX1_CATGGCA.setLabel('DNA Adapter 34')
vocabulary_term_INDEX1_CATGGCA.setOrdinal(77)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CATGGCA)
          
vocabulary_term_INDEX1_CATTTTA = tr.createNewVocabularyTerm('CATTTTA')
vocabulary_term_INDEX1_CATTTTA.setDescription(None)
vocabulary_term_INDEX1_CATTTTA.setLabel('DNA Adapter 35')
vocabulary_term_INDEX1_CATTTTA.setOrdinal(78)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CATTTTA)
          
vocabulary_term_INDEX1_CCAACAA = tr.createNewVocabularyTerm('CCAACAA')
vocabulary_term_INDEX1_CCAACAA.setDescription(None)
vocabulary_term_INDEX1_CCAACAA.setLabel('DNA Adapter 36')
vocabulary_term_INDEX1_CCAACAA.setOrdinal(79)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CCAACAA)
          
vocabulary_term_INDEX1_CGGAATA = tr.createNewVocabularyTerm('CGGAATA')
vocabulary_term_INDEX1_CGGAATA.setDescription(None)
vocabulary_term_INDEX1_CGGAATA.setLabel('DNA Adapter 37')
vocabulary_term_INDEX1_CGGAATA.setOrdinal(80)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CGGAATA)
          
vocabulary_term_INDEX1_CTAGCTA = tr.createNewVocabularyTerm('CTAGCTA')
vocabulary_term_INDEX1_CTAGCTA.setDescription(None)
vocabulary_term_INDEX1_CTAGCTA.setLabel('DNA Adapter 38')
vocabulary_term_INDEX1_CTAGCTA.setOrdinal(81)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTAGCTA)
          
vocabulary_term_INDEX1_CTATACA = tr.createNewVocabularyTerm('CTATACA')
vocabulary_term_INDEX1_CTATACA.setDescription(None)
vocabulary_term_INDEX1_CTATACA.setLabel('DNA Adapter 39')
vocabulary_term_INDEX1_CTATACA.setOrdinal(82)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTATACA)
          
vocabulary_term_INDEX1_CTCAGAA = tr.createNewVocabularyTerm('CTCAGAA')
vocabulary_term_INDEX1_CTCAGAA.setDescription(None)
vocabulary_term_INDEX1_CTCAGAA.setLabel('DNA Adapter 40')
vocabulary_term_INDEX1_CTCAGAA.setOrdinal(83)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_CTCAGAA)
          
vocabulary_term_INDEX1_GACGACA = tr.createNewVocabularyTerm('GACGACA')
vocabulary_term_INDEX1_GACGACA.setDescription(None)
vocabulary_term_INDEX1_GACGACA.setLabel('DNA Adapter 41')
vocabulary_term_INDEX1_GACGACA.setOrdinal(84)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_GACGACA)
          
vocabulary_term_INDEX1_TAATCGA = tr.createNewVocabularyTerm('TAATCGA')
vocabulary_term_INDEX1_TAATCGA.setDescription(None)
vocabulary_term_INDEX1_TAATCGA.setLabel('DNA Adapter 42')
vocabulary_term_INDEX1_TAATCGA.setOrdinal(85)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TAATCGA)
          
vocabulary_term_INDEX1_TACAGCA = tr.createNewVocabularyTerm('TACAGCA')
vocabulary_term_INDEX1_TACAGCA.setDescription(None)
vocabulary_term_INDEX1_TACAGCA.setLabel('DNA Adapter 43')
vocabulary_term_INDEX1_TACAGCA.setOrdinal(86)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TACAGCA)
          
vocabulary_term_INDEX1_TATAATA = tr.createNewVocabularyTerm('TATAATA')
vocabulary_term_INDEX1_TATAATA.setDescription(None)
vocabulary_term_INDEX1_TATAATA.setLabel('DNA Adapter 44')
vocabulary_term_INDEX1_TATAATA.setOrdinal(87)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TATAATA)
          
vocabulary_term_INDEX1_TCATTCA = tr.createNewVocabularyTerm('TCATTCA')
vocabulary_term_INDEX1_TCATTCA.setDescription(None)
vocabulary_term_INDEX1_TCATTCA.setLabel('DNA Adapter 45')
vocabulary_term_INDEX1_TCATTCA.setOrdinal(88)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCATTCA)
          
vocabulary_term_INDEX1_TCCCGAA = tr.createNewVocabularyTerm('TCCCGAA')
vocabulary_term_INDEX1_TCCCGAA.setDescription(None)
vocabulary_term_INDEX1_TCCCGAA.setLabel('DNA Adapter 46')
vocabulary_term_INDEX1_TCCCGAA.setOrdinal(89)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCCCGAA)
          
vocabulary_term_INDEX1_TCGAAGA = tr.createNewVocabularyTerm('TCGAAGA')
vocabulary_term_INDEX1_TCGAAGA.setDescription(None)
vocabulary_term_INDEX1_TCGAAGA.setLabel('DNA Adapter 47')
vocabulary_term_INDEX1_TCGAAGA.setOrdinal(90)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCGAAGA)
          
vocabulary_term_INDEX1_TCGGCAA = tr.createNewVocabularyTerm('TCGGCAA')
vocabulary_term_INDEX1_TCGGCAA.setDescription(None)
vocabulary_term_INDEX1_TCGGCAA.setLabel('DNA Adapter 48')
vocabulary_term_INDEX1_TCGGCAA.setOrdinal(91)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_TCGGCAA)
          
vocabulary_term_INDEX1_AGTCAAC = tr.createNewVocabularyTerm('AGTCAAC')
vocabulary_term_INDEX1_AGTCAAC.setDescription(None)
vocabulary_term_INDEX1_AGTCAAC.setLabel('Index13 AGTCAAC')
vocabulary_term_INDEX1_AGTCAAC.setOrdinal(92)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AGTCAAC)
          
vocabulary_term_INDEX1_AATGCGA = tr.createNewVocabularyTerm('AATGCGA')
vocabulary_term_INDEX1_AATGCGA.setDescription(None)
vocabulary_term_INDEX1_AATGCGA.setLabel('Lib AATGCGA')
vocabulary_term_INDEX1_AATGCGA.setOrdinal(93)
vocabulary_INDEX1.addTerm(vocabulary_term_INDEX1_AATGCGA)
    
vocabulary_INDEX2 = tr.getOrCreateNewVocabulary('INDEX2')
vocabulary_INDEX2.setDescription('Index 2 for Illumina Dual Indexing')
vocabulary_INDEX2.setUrlTemplate(None)
vocabulary_INDEX2.setManagedInternally(False)
vocabulary_INDEX2.setInternalNamespace(False)
vocabulary_INDEX2.setChosenFromList(True)
          
vocabulary_term_INDEX2_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_INDEX2_NONE.setDescription('No Index')
vocabulary_term_INDEX2_NONE.setLabel('None')
vocabulary_term_INDEX2_NONE.setOrdinal(1)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_NONE)
          
vocabulary_term_INDEX2_TAGATCGC = tr.createNewVocabularyTerm('TAGATCGC')
vocabulary_term_INDEX2_TAGATCGC.setDescription('Nextera DNA')
vocabulary_term_INDEX2_TAGATCGC.setLabel('Index2 (i5) N501 TAGATCGC')
vocabulary_term_INDEX2_TAGATCGC.setOrdinal(2)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_TAGATCGC)
          
vocabulary_term_INDEX2_CTCTCTAT = tr.createNewVocabularyTerm('CTCTCTAT')
vocabulary_term_INDEX2_CTCTCTAT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_CTCTCTAT.setLabel('Index2 (i5) N502 CTCTCTAT')
vocabulary_term_INDEX2_CTCTCTAT.setOrdinal(3)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_CTCTCTAT)
          
vocabulary_term_INDEX2_TATCCTCT = tr.createNewVocabularyTerm('TATCCTCT')
vocabulary_term_INDEX2_TATCCTCT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_TATCCTCT.setLabel('Index2 (i5) N503 TATCCTCT')
vocabulary_term_INDEX2_TATCCTCT.setOrdinal(4)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_TATCCTCT)
          
vocabulary_term_INDEX2_AGAGTAGA = tr.createNewVocabularyTerm('AGAGTAGA')
vocabulary_term_INDEX2_AGAGTAGA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_AGAGTAGA.setLabel('Index2 (i5) N504 AGAGTAGA')
vocabulary_term_INDEX2_AGAGTAGA.setOrdinal(5)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_AGAGTAGA)
          
vocabulary_term_INDEX2_GTAAGGAG = tr.createNewVocabularyTerm('GTAAGGAG')
vocabulary_term_INDEX2_GTAAGGAG.setDescription('Nextera DNA')
vocabulary_term_INDEX2_GTAAGGAG.setLabel('Index2 (i5) N505 GTAAGGAG')
vocabulary_term_INDEX2_GTAAGGAG.setOrdinal(6)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_GTAAGGAG)
          
vocabulary_term_INDEX2_ACTGCATA = tr.createNewVocabularyTerm('ACTGCATA')
vocabulary_term_INDEX2_ACTGCATA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_ACTGCATA.setLabel('Index2 (i5) N506 ACTGCATA')
vocabulary_term_INDEX2_ACTGCATA.setOrdinal(7)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_ACTGCATA)
          
vocabulary_term_INDEX2_AAGGAGTA = tr.createNewVocabularyTerm('AAGGAGTA')
vocabulary_term_INDEX2_AAGGAGTA.setDescription('Nextera DNA')
vocabulary_term_INDEX2_AAGGAGTA.setLabel('Index2 (i5) N507 AAGGAGTA')
vocabulary_term_INDEX2_AAGGAGTA.setOrdinal(8)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_AAGGAGTA)
          
vocabulary_term_INDEX2_CTAAGCCT = tr.createNewVocabularyTerm('CTAAGCCT')
vocabulary_term_INDEX2_CTAAGCCT.setDescription('Nextera DNA')
vocabulary_term_INDEX2_CTAAGCCT.setLabel('Index2 (i5) N508 CTAAGCCT')
vocabulary_term_INDEX2_CTAAGCCT.setOrdinal(9)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_CTAAGCCT)
          
vocabulary_term_INDEX2_NOINDEX = tr.createNewVocabularyTerm('NOINDEX')
vocabulary_term_INDEX2_NOINDEX.setDescription(None)
vocabulary_term_INDEX2_NOINDEX.setLabel(None)
vocabulary_term_INDEX2_NOINDEX.setOrdinal(10)
vocabulary_INDEX2.addTerm(vocabulary_term_INDEX2_NOINDEX)
    
vocabulary_KIT = tr.getOrCreateNewVocabulary('KIT')
vocabulary_KIT.setDescription('Illumina Kit used for preparation')
vocabulary_KIT.setUrlTemplate(None)
vocabulary_KIT.setManagedInternally(False)
vocabulary_KIT.setInternalNamespace(False)
vocabulary_KIT.setChosenFromList(True)
          
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP = tr.createNewVocabularyTerm('CHIP_SEQ_SAMPLE_PREP')
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setLabel('ChIP-Seq Sample Preparation Kit')
vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP.setOrdinal(1)
vocabulary_KIT.addTerm(vocabulary_term_KIT_CHIP_SEQ_SAMPLE_PREP)
          
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1 = tr.createNewVocabularyTerm('NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1')
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setDescription(None)
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setLabel('NEB Genomic DNA Sample Preparation Kit')
vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1.setOrdinal(2)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1)
          
vocabulary_term_KIT_NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT = tr.createNewVocabularyTerm('NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT')
vocabulary_term_KIT_NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT.setDescription(None)
vocabulary_term_KIT_NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT.setLabel('NEB_ChIP_Seq_Sample_Preparation_Kit')
vocabulary_term_KIT_NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT.setOrdinal(3)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEB_CHIP_SEQ_SAMPLE_PREPARATION_KIT)
          
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP = tr.createNewVocabularyTerm('GENOMICDNA_SAMPLE_PREP')
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setLabel('Illumina Genomic DNA Sample Preparation Kit')
vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP.setOrdinal(4)
vocabulary_KIT.addTerm(vocabulary_term_KIT_GENOMICDNA_SAMPLE_PREP)
          
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP = tr.createNewVocabularyTerm('PAIRED_END_DNA_SAMPLE_PREP')
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setLabel('Paired End DNA Sample Prep Oligo Kit')
vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP.setOrdinal(5)
vocabulary_KIT.addTerm(vocabulary_term_KIT_PAIRED_END_DNA_SAMPLE_PREP)
          
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP = tr.createNewVocabularyTerm('MRNA_SEQ_SAMPLE_PREP')
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setDescription(None)
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setLabel('mRNA-Seq Sample Preparation Kit')
vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP.setOrdinal(6)
vocabulary_KIT.addTerm(vocabulary_term_KIT_MRNA_SEQ_SAMPLE_PREP)
          
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT = tr.createNewVocabularyTerm('RIBOZERO_SCRIPTSEQ_MRNA-SEQ_KIT')
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setDescription(None)
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setLabel('RiboZero ScriptSeq mRNA-Seq_Epicentre-kit')
vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT.setOrdinal(7)
vocabulary_KIT.addTerm(vocabulary_term_KIT_RIBOZERO_SCRIPTSEQ_MRNASEQ_KIT)
          
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT = tr.createNewVocabularyTerm('POLYA_SCRIPTSEQ_MRNA-SEQ_KIT')
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setDescription(None)
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setLabel('PolyA(Beads) ScriptSeq mRNA-Seq_Epicentre-kit')
vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT.setOrdinal(8)
vocabulary_KIT.addTerm(vocabulary_term_KIT_POLYA_SCRIPTSEQ_MRNASEQ_KIT)
          
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS = tr.createNewVocabularyTerm('NEXTERA_DNA_SAMPLE_PREP_KITS')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setDescription(None)
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setLabel('Nextera Genomic DNA Sample Preparation Kit BufferLMW(Epicentre)')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS.setOrdinal(9)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KITS)
          
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW = tr.createNewVocabularyTerm('NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setDescription(None)
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setLabel('Nextera Genomic DNA Sample Preparation Kit BufferHMW(Epicentre)')
vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW.setOrdinal(10)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW)
          
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM = tr.createNewVocabularyTerm('AGILENT_SURESELECT_ENRICHMENTSYSTEM')
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setDescription(None)
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setLabel('Agilent_SureSelect_EnrichmentSystem')
vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM.setOrdinal(11)
vocabulary_KIT.addTerm(vocabulary_term_KIT_AGILENT_SURESELECT_ENRICHMENTSYSTEM)
          
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT = tr.createNewVocabularyTerm('TRUSEQRNA_SAMPLE_PREP_KIT')
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setDescription(None)
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setLabel('TruSeq_RNA_SamplePrepKit_Illumina')
vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT.setOrdinal(12)
vocabulary_KIT.addTerm(vocabulary_term_KIT_TRUSEQRNA_SAMPLE_PREP_KIT)
          
vocabulary_term_KIT_TRUESEQ_CHIP_SAMPLE_PREP_KIT = tr.createNewVocabularyTerm('TRUESEQ_CHIP_SAMPLE_PREP_KIT')
vocabulary_term_KIT_TRUESEQ_CHIP_SAMPLE_PREP_KIT.setDescription(None)
vocabulary_term_KIT_TRUESEQ_CHIP_SAMPLE_PREP_KIT.setLabel(None)
vocabulary_term_KIT_TRUESEQ_CHIP_SAMPLE_PREP_KIT.setOrdinal(13)
vocabulary_KIT.addTerm(vocabulary_term_KIT_TRUESEQ_CHIP_SAMPLE_PREP_KIT)
          
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT = tr.createNewVocabularyTerm('TRUSEQ_DNA_SAMPLE_PREP_KIT')
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setDescription(None)
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setLabel('TruSeq_DNA_SamplePrepKit_Illumina')
vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT.setOrdinal(14)
vocabulary_KIT.addTerm(vocabulary_term_KIT_TRUSEQ_DNA_SAMPLE_PREP_KIT)
          
vocabulary_term_KIT_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_KIT_NONE.setDescription(None)
vocabulary_term_KIT_NONE.setLabel('None (Already prepared)')
vocabulary_term_KIT_NONE.setOrdinal(15)
vocabulary_KIT.addTerm(vocabulary_term_KIT_NONE)
    
vocabulary_MACS_VERSION = tr.getOrCreateNewVocabulary('MACS_VERSION')
vocabulary_MACS_VERSION.setDescription('Used MACS version for Peak Calling')
vocabulary_MACS_VERSION.setUrlTemplate(None)
vocabulary_MACS_VERSION.setManagedInternally(False)
vocabulary_MACS_VERSION.setInternalNamespace(False)
vocabulary_MACS_VERSION.setChosenFromList(True)
          
vocabulary_term_MACS_VERSION_140RC2 = tr.createNewVocabularyTerm('1.4.0RC2')
vocabulary_term_MACS_VERSION_140RC2.setDescription(None)
vocabulary_term_MACS_VERSION_140RC2.setLabel('macs14 1.4.0rc2 20110214 (Valentine)')
vocabulary_term_MACS_VERSION_140RC2.setOrdinal(1)
vocabulary_MACS_VERSION.addTerm(vocabulary_term_MACS_VERSION_140RC2)
          
vocabulary_term_MACS_VERSION_1371 = tr.createNewVocabularyTerm('1.3.7.1')
vocabulary_term_MACS_VERSION_1371.setDescription(None)
vocabulary_term_MACS_VERSION_1371.setLabel('macs 1.3.7.1 (Oktoberfest, bug fixed #1)')
vocabulary_term_MACS_VERSION_1371.setOrdinal(2)
vocabulary_MACS_VERSION.addTerm(vocabulary_term_MACS_VERSION_1371)
    
vocabulary_MISMATCH_IN_INDEX = tr.getOrCreateNewVocabulary('MISMATCH_IN_INDEX')
vocabulary_MISMATCH_IN_INDEX.setDescription('Mismatch in Index allowed')
vocabulary_MISMATCH_IN_INDEX.setUrlTemplate(None)
vocabulary_MISMATCH_IN_INDEX.setManagedInternally(False)
vocabulary_MISMATCH_IN_INDEX.setInternalNamespace(False)
vocabulary_MISMATCH_IN_INDEX.setChosenFromList(True)
          
vocabulary_term_MISMATCH_IN_INDEX_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_MISMATCH_IN_INDEX_NONE.setDescription(None)
vocabulary_term_MISMATCH_IN_INDEX_NONE.setLabel(None)
vocabulary_term_MISMATCH_IN_INDEX_NONE.setOrdinal(1)
vocabulary_MISMATCH_IN_INDEX.addTerm(vocabulary_term_MISMATCH_IN_INDEX_NONE)
          
vocabulary_term_MISMATCH_IN_INDEX_ONE = tr.createNewVocabularyTerm('ONE')
vocabulary_term_MISMATCH_IN_INDEX_ONE.setDescription(None)
vocabulary_term_MISMATCH_IN_INDEX_ONE.setLabel(None)
vocabulary_term_MISMATCH_IN_INDEX_ONE.setOrdinal(2)
vocabulary_MISMATCH_IN_INDEX.addTerm(vocabulary_term_MISMATCH_IN_INDEX_ONE)
    
vocabulary_NANO_DROP = tr.getOrCreateNewVocabulary('NANO_DROP')
vocabulary_NANO_DROP.setDescription('Device for measuring the total amount of genetic material contained in the probe')
vocabulary_NANO_DROP.setUrlTemplate(None)
vocabulary_NANO_DROP.setManagedInternally(False)
vocabulary_NANO_DROP.setInternalNamespace(False)
vocabulary_NANO_DROP.setChosenFromList(True)
          
vocabulary_term_NANO_DROP_CONCND3300 = tr.createNewVocabularyTerm('CONCND3300')
vocabulary_term_NANO_DROP_CONCND3300.setDescription(None)
vocabulary_term_NANO_DROP_CONCND3300.setLabel('Conc Nano Drop 3300')
vocabulary_term_NANO_DROP_CONCND3300.setOrdinal(1)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND3300)
          
vocabulary_term_NANO_DROP_CONCND1000 = tr.createNewVocabularyTerm('CONCND1000')
vocabulary_term_NANO_DROP_CONCND1000.setDescription(None)
vocabulary_term_NANO_DROP_CONCND1000.setLabel('Conc Nano Drop 1000')
vocabulary_term_NANO_DROP_CONCND1000.setOrdinal(2)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND1000)
          
vocabulary_term_NANO_DROP_CONCND2000 = tr.createNewVocabularyTerm('CONCND2000')
vocabulary_term_NANO_DROP_CONCND2000.setDescription(None)
vocabulary_term_NANO_DROP_CONCND2000.setLabel('Conc Nano Drop 2000')
vocabulary_term_NANO_DROP_CONCND2000.setOrdinal(3)
vocabulary_NANO_DROP.addTerm(vocabulary_term_NANO_DROP_CONCND2000)
    
vocabulary_NCBI_TAXONOMY = tr.getOrCreateNewVocabulary('NCBI_TAXONOMY')
vocabulary_NCBI_TAXONOMY.setDescription(None)
vocabulary_NCBI_TAXONOMY.setUrlTemplate('http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=$term$')
vocabulary_NCBI_TAXONOMY.setManagedInternally(False)
vocabulary_NCBI_TAXONOMY.setInternalNamespace(False)
vocabulary_NCBI_TAXONOMY.setChosenFromList(True)
          
vocabulary_term_NCBI_TAXONOMY_10090 = tr.createNewVocabularyTerm('10090')
vocabulary_term_NCBI_TAXONOMY_10090.setDescription('Genbank common name: house mouse\ Inherited blast name: rodents')
vocabulary_term_NCBI_TAXONOMY_10090.setLabel('Mus musculus')
vocabulary_term_NCBI_TAXONOMY_10090.setOrdinal(1)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10090)
          
vocabulary_term_NCBI_TAXONOMY_9606 = tr.createNewVocabularyTerm('9606')
vocabulary_term_NCBI_TAXONOMY_9606.setDescription('Genbank common name: human\ Inherited blast name: primates')
vocabulary_term_NCBI_TAXONOMY_9606.setLabel('Homo sapiens')
vocabulary_term_NCBI_TAXONOMY_9606.setOrdinal(2)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9606)
          
vocabulary_term_NCBI_TAXONOMY_7227 = tr.createNewVocabularyTerm('7227')
vocabulary_term_NCBI_TAXONOMY_7227.setDescription('Genbank common name: fruit fly\ Inherited blast name: flies')
vocabulary_term_NCBI_TAXONOMY_7227.setLabel('Drosophila melanogaster')
vocabulary_term_NCBI_TAXONOMY_7227.setOrdinal(3)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_7227)
          
vocabulary_term_NCBI_TAXONOMY_6239 = tr.createNewVocabularyTerm('6239')
vocabulary_term_NCBI_TAXONOMY_6239.setDescription('Inherited blast name: nematodes')
vocabulary_term_NCBI_TAXONOMY_6239.setLabel('Caenorhabditis elegans')
vocabulary_term_NCBI_TAXONOMY_6239.setOrdinal(4)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_6239)
          
vocabulary_term_NCBI_TAXONOMY_4932 = tr.createNewVocabularyTerm('4932')
vocabulary_term_NCBI_TAXONOMY_4932.setDescription('Genbank common name: baker\'s yeast\ Inherited blast name: ascomycetes')
vocabulary_term_NCBI_TAXONOMY_4932.setLabel('Saccharomyces cerevisiae')
vocabulary_term_NCBI_TAXONOMY_4932.setOrdinal(5)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4932)
          
vocabulary_term_NCBI_TAXONOMY_10116 = tr.createNewVocabularyTerm('10116')
vocabulary_term_NCBI_TAXONOMY_10116.setDescription('Genbank common name: Norway rat\ Inherited blast name: rodents')
vocabulary_term_NCBI_TAXONOMY_10116.setLabel('Rattus norvegicus')
vocabulary_term_NCBI_TAXONOMY_10116.setOrdinal(6)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10116)
          
vocabulary_term_NCBI_TAXONOMY_6669 = tr.createNewVocabularyTerm('6669')
vocabulary_term_NCBI_TAXONOMY_6669.setDescription('Genbank common name: common water flea\ Inherited blast name: crustaceans')
vocabulary_term_NCBI_TAXONOMY_6669.setLabel('Daphnia pulex')
vocabulary_term_NCBI_TAXONOMY_6669.setOrdinal(7)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_6669)
          
vocabulary_term_NCBI_TAXONOMY_562 = tr.createNewVocabularyTerm('562')
vocabulary_term_NCBI_TAXONOMY_562.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_562.setLabel('Escherichia coli')
vocabulary_term_NCBI_TAXONOMY_562.setOrdinal(8)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_562)
          
vocabulary_term_NCBI_TAXONOMY_623 = tr.createNewVocabularyTerm('623')
vocabulary_term_NCBI_TAXONOMY_623.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_623.setLabel('Shigella flexneri')
vocabulary_term_NCBI_TAXONOMY_623.setOrdinal(9)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_623)
          
vocabulary_term_NCBI_TAXONOMY_10847 = tr.createNewVocabularyTerm('10847')
vocabulary_term_NCBI_TAXONOMY_10847.setDescription('Inherited blast name: viruses')
vocabulary_term_NCBI_TAXONOMY_10847.setLabel('Enterobacteria phage phiX174')
vocabulary_term_NCBI_TAXONOMY_10847.setOrdinal(10)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_10847)
          
vocabulary_term_NCBI_TAXONOMY_190650 = tr.createNewVocabularyTerm('190650')
vocabulary_term_NCBI_TAXONOMY_190650.setDescription('Inherited blast name: a-proteobacteria')
vocabulary_term_NCBI_TAXONOMY_190650.setLabel('Caulobacter crescentus CB15')
vocabulary_term_NCBI_TAXONOMY_190650.setOrdinal(11)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_190650)
          
vocabulary_term_NCBI_TAXONOMY_8113 = tr.createNewVocabularyTerm('8113')
vocabulary_term_NCBI_TAXONOMY_8113.setDescription('Genbank common name: cichlids\ Inherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_8113.setLabel('Cichlidae')
vocabulary_term_NCBI_TAXONOMY_8113.setOrdinal(12)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_8113)
          
vocabulary_term_NCBI_TAXONOMY_481459 = tr.createNewVocabularyTerm('481459')
vocabulary_term_NCBI_TAXONOMY_481459.setDescription('Genbank common name: three-spined stickleback\ Inherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_481459.setLabel('Gasterosteus aculeatus aculeatus')
vocabulary_term_NCBI_TAXONOMY_481459.setOrdinal(13)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_481459)
          
vocabulary_term_NCBI_TAXONOMY_282301 = tr.createNewVocabularyTerm('282301')
vocabulary_term_NCBI_TAXONOMY_282301.setDescription('Inherited blast name: flatworms')
vocabulary_term_NCBI_TAXONOMY_282301.setLabel('Macrostomum lignano')
vocabulary_term_NCBI_TAXONOMY_282301.setOrdinal(14)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_282301)
          
vocabulary_term_NCBI_TAXONOMY_99287 = tr.createNewVocabularyTerm('99287')
vocabulary_term_NCBI_TAXONOMY_99287.setDescription('Inherited blast name: enterobacteria')
vocabulary_term_NCBI_TAXONOMY_99287.setLabel('Salmonella enterica subsp. enterica serovar Typhimurium str. LT2')
vocabulary_term_NCBI_TAXONOMY_99287.setOrdinal(15)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_99287)
          
vocabulary_term_NCBI_TAXONOMY_4896 = tr.createNewVocabularyTerm('4896')
vocabulary_term_NCBI_TAXONOMY_4896.setDescription('Genbank common name: fission yeast\ Inherited blast name: ascomycetes')
vocabulary_term_NCBI_TAXONOMY_4896.setLabel('Schizosaccharomyces pombe')
vocabulary_term_NCBI_TAXONOMY_4896.setOrdinal(16)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4896)
          
vocabulary_term_NCBI_TAXONOMY_4897 = tr.createNewVocabularyTerm('4897')
vocabulary_term_NCBI_TAXONOMY_4897.setDescription('Inherited blast name: ascomycetes')
vocabulary_term_NCBI_TAXONOMY_4897.setLabel('Schizosaccharomyces japonicus')
vocabulary_term_NCBI_TAXONOMY_4897.setOrdinal(17)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4897)
          
vocabulary_term_NCBI_TAXONOMY_7955 = tr.createNewVocabularyTerm('7955')
vocabulary_term_NCBI_TAXONOMY_7955.setDescription('Genbank common name: zebrafish\ Inherited blast name: bony fishes')
vocabulary_term_NCBI_TAXONOMY_7955.setLabel('Danio rerio')
vocabulary_term_NCBI_TAXONOMY_7955.setOrdinal(18)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_7955)
          
vocabulary_term_NCBI_TAXONOMY_3702 = tr.createNewVocabularyTerm('3702')
vocabulary_term_NCBI_TAXONOMY_3702.setDescription('Genbank common name: thale cress\ Inherited blast name: eudicots')
vocabulary_term_NCBI_TAXONOMY_3702.setLabel('Arabidopsis thaliana')
vocabulary_term_NCBI_TAXONOMY_3702.setOrdinal(19)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3702)
          
vocabulary_term_NCBI_TAXONOMY_225322 = tr.createNewVocabularyTerm('225322')
vocabulary_term_NCBI_TAXONOMY_225322.setDescription('Bacterium which infects Daphnia\ Pasteuria ramosa Metchnikoff 1888 (Approved Lists 1980) emend. Starr et al. 1986\ Ebert D, Rainey P, Embley TM, Scholz D. Development, life\ cycle, ultrastructure and phylogenetic position of Pasteuria ramosa Metchnikoff 1888: rediscovery of an obligate \ endoparasite of Daphnia magna Straus. Philos Trans R Soc \ Lond Ser B. 1996;351:1689.')
vocabulary_term_NCBI_TAXONOMY_225322.setLabel('Pasteuria ramosa')
vocabulary_term_NCBI_TAXONOMY_225322.setOrdinal(20)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_225322)
          
vocabulary_term_NCBI_TAXONOMY_9913 = tr.createNewVocabularyTerm('9913')
vocabulary_term_NCBI_TAXONOMY_9913.setDescription('Genbank common name: cattle\ Inherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9913.setLabel('Bos taurus')
vocabulary_term_NCBI_TAXONOMY_9913.setOrdinal(21)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9913)
          
vocabulary_term_NCBI_TAXONOMY_9823 = tr.createNewVocabularyTerm('9823')
vocabulary_term_NCBI_TAXONOMY_9823.setDescription('Genbank common name: pig\ Inherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9823.setLabel('Sus scrofa')
vocabulary_term_NCBI_TAXONOMY_9823.setOrdinal(22)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9823)
          
vocabulary_term_NCBI_TAXONOMY_9940 = tr.createNewVocabularyTerm('9940')
vocabulary_term_NCBI_TAXONOMY_9940.setDescription('Genbank common name: sheep\ Inherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9940.setLabel('Ovis aries')
vocabulary_term_NCBI_TAXONOMY_9940.setOrdinal(23)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9940)
          
vocabulary_term_NCBI_TAXONOMY_9925 = tr.createNewVocabularyTerm('9925')
vocabulary_term_NCBI_TAXONOMY_9925.setDescription('Genbank common name: goat\ Inherited blast name: even-toed ungulates')
vocabulary_term_NCBI_TAXONOMY_9925.setLabel('Capra hircus')
vocabulary_term_NCBI_TAXONOMY_9925.setOrdinal(24)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_9925)
          
vocabulary_term_NCBI_TAXONOMY_8153 = tr.createNewVocabularyTerm('8153')
vocabulary_term_NCBI_TAXONOMY_8153.setDescription('Common name: cichlid fish')
vocabulary_term_NCBI_TAXONOMY_8153.setLabel('Haplochromis burtoni')
vocabulary_term_NCBI_TAXONOMY_8153.setOrdinal(25)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_8153)
          
vocabulary_term_NCBI_TAXONOMY_61818 = tr.createNewVocabularyTerm('61818')
vocabulary_term_NCBI_TAXONOMY_61818.setDescription('Common name: bony fish')
vocabulary_term_NCBI_TAXONOMY_61818.setLabel('Amphilophus (nicaraguan)')
vocabulary_term_NCBI_TAXONOMY_61818.setOrdinal(26)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_61818)
          
vocabulary_term_NCBI_TAXONOMY_35525 = tr.createNewVocabularyTerm('35525')
vocabulary_term_NCBI_TAXONOMY_35525.setDescription('Common name: water flea')
vocabulary_term_NCBI_TAXONOMY_35525.setLabel('Daphnia Magna')
vocabulary_term_NCBI_TAXONOMY_35525.setOrdinal(27)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_35525)
          
vocabulary_term_NCBI_TAXONOMY_4081 = tr.createNewVocabularyTerm('4081')
vocabulary_term_NCBI_TAXONOMY_4081.setDescription('Common Name: tomato')
vocabulary_term_NCBI_TAXONOMY_4081.setLabel('Solanum lycopersicum (tomato)')
vocabulary_term_NCBI_TAXONOMY_4081.setOrdinal(28)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_4081)
          
vocabulary_term_NCBI_TAXONOMY_13068 = tr.createNewVocabularyTerm('13068')
vocabulary_term_NCBI_TAXONOMY_13068.setDescription('Common name: earwig')
vocabulary_term_NCBI_TAXONOMY_13068.setLabel('Forficula auricularia (earwig)')
vocabulary_term_NCBI_TAXONOMY_13068.setOrdinal(29)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_13068)
          
vocabulary_term_NCBI_TAXONOMY_3569 = tr.createNewVocabularyTerm('3569')
vocabulary_term_NCBI_TAXONOMY_3569.setDescription('Genbank common name: clove pink')
vocabulary_term_NCBI_TAXONOMY_3569.setLabel('Dianthus')
vocabulary_term_NCBI_TAXONOMY_3569.setOrdinal(30)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3569)
          
vocabulary_term_NCBI_TAXONOMY_3573 = tr.createNewVocabularyTerm('3573')
vocabulary_term_NCBI_TAXONOMY_3573.setDescription('Genbank common name: campions\ Inherited blast name: eudicots')
vocabulary_term_NCBI_TAXONOMY_3573.setLabel('Silene')
vocabulary_term_NCBI_TAXONOMY_3573.setOrdinal(31)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_3573)
          
vocabulary_term_NCBI_TAXONOMY_32644 = tr.createNewVocabularyTerm('32644')
vocabulary_term_NCBI_TAXONOMY_32644.setDescription('Inherited blast name: unclassified')
vocabulary_term_NCBI_TAXONOMY_32644.setLabel('unidentified')
vocabulary_term_NCBI_TAXONOMY_32644.setOrdinal(32)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_32644)
          
vocabulary_term_NCBI_TAXONOMY_0000 = tr.createNewVocabularyTerm('0000')
vocabulary_term_NCBI_TAXONOMY_0000.setDescription(None)
vocabulary_term_NCBI_TAXONOMY_0000.setLabel('OTHER')
vocabulary_term_NCBI_TAXONOMY_0000.setOrdinal(33)
vocabulary_NCBI_TAXONOMY.addTerm(vocabulary_term_NCBI_TAXONOMY_0000)
    
vocabulary_PIPELINE_VERSION = tr.getOrCreateNewVocabulary('PIPELINE_VERSION')
vocabulary_PIPELINE_VERSION.setDescription('With which pipeline version has the data been analyzed?')
vocabulary_PIPELINE_VERSION.setUrlTemplate(None)
vocabulary_PIPELINE_VERSION.setManagedInternally(False)
vocabulary_PIPELINE_VERSION.setInternalNamespace(False)
vocabulary_PIPELINE_VERSION.setChosenFromList(True)
          
vocabulary_term_PIPELINE_VERSION_11348 = tr.createNewVocabularyTerm('1.13.48')
vocabulary_term_PIPELINE_VERSION_11348.setDescription(None)
vocabulary_term_PIPELINE_VERSION_11348.setLabel('RTA 1.13.48')
vocabulary_term_PIPELINE_VERSION_11348.setOrdinal(1)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_11348)
          
vocabulary_term_PIPELINE_VERSION_11242 = tr.createNewVocabularyTerm('1.12.4.2')
vocabulary_term_PIPELINE_VERSION_11242.setDescription('Real Time Analysis in combination with HCS 1.4.8')
vocabulary_term_PIPELINE_VERSION_11242.setLabel('RTA 1.12.4.2 (HiSeq 2000)')
vocabulary_term_PIPELINE_VERSION_11242.setOrdinal(2)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_11242)
          
vocabulary_term_PIPELINE_VERSION_112 = tr.createNewVocabularyTerm('1.12')
vocabulary_term_PIPELINE_VERSION_112.setDescription('Real Time Analysis 1.12 HiSeq 2000')
vocabulary_term_PIPELINE_VERSION_112.setLabel('RTA 1.12')
vocabulary_term_PIPELINE_VERSION_112.setOrdinal(3)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_112)
          
vocabulary_term_PIPELINE_VERSION_19 = tr.createNewVocabularyTerm('1.9')
vocabulary_term_PIPELINE_VERSION_19.setDescription('Real Time Analysis 1.9')
vocabulary_term_PIPELINE_VERSION_19.setLabel('RTA 1.9 (GA IIx)')
vocabulary_term_PIPELINE_VERSION_19.setOrdinal(4)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_19)
          
vocabulary_term_PIPELINE_VERSION_RTA_18 = tr.createNewVocabularyTerm('RTA_1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setDescription('Real Time Analysis 1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setLabel('RTA 1.8')
vocabulary_term_PIPELINE_VERSION_RTA_18.setOrdinal(5)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_18)
          
vocabulary_term_PIPELINE_VERSION_RTA_16 = tr.createNewVocabularyTerm('RTA_1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setDescription('Real Time Analysis 1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setLabel('RTA 1.6')
vocabulary_term_PIPELINE_VERSION_RTA_16.setOrdinal(6)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_16)
          
vocabulary_term_PIPELINE_VERSION_RTA_15 = tr.createNewVocabularyTerm('RTA_1.5')
vocabulary_term_PIPELINE_VERSION_RTA_15.setDescription(None)
vocabulary_term_PIPELINE_VERSION_RTA_15.setLabel('RTA 1.5')
vocabulary_term_PIPELINE_VERSION_RTA_15.setOrdinal(7)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_15)
          
vocabulary_term_PIPELINE_VERSION_RTA_14150 = tr.createNewVocabularyTerm('RTA_1.4.15.0')
vocabulary_term_PIPELINE_VERSION_RTA_14150.setDescription(None)
vocabulary_term_PIPELINE_VERSION_RTA_14150.setLabel('RTA 1.4.15.0')
vocabulary_term_PIPELINE_VERSION_RTA_14150.setOrdinal(8)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_RTA_14150)
          
vocabulary_term_PIPELINE_VERSION_15 = tr.createNewVocabularyTerm('1.5')
vocabulary_term_PIPELINE_VERSION_15.setDescription(None)
vocabulary_term_PIPELINE_VERSION_15.setLabel(None)
vocabulary_term_PIPELINE_VERSION_15.setOrdinal(9)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_15)
          
vocabulary_term_PIPELINE_VERSION_14 = tr.createNewVocabularyTerm('1.4')
vocabulary_term_PIPELINE_VERSION_14.setDescription(None)
vocabulary_term_PIPELINE_VERSION_14.setLabel(None)
vocabulary_term_PIPELINE_VERSION_14.setOrdinal(10)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_14)
          
vocabulary_term_PIPELINE_VERSION_132 = tr.createNewVocabularyTerm('1.3.2')
vocabulary_term_PIPELINE_VERSION_132.setDescription(None)
vocabulary_term_PIPELINE_VERSION_132.setLabel(None)
vocabulary_term_PIPELINE_VERSION_132.setOrdinal(11)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_132)
          
vocabulary_term_PIPELINE_VERSION_10 = tr.createNewVocabularyTerm('1.0')
vocabulary_term_PIPELINE_VERSION_10.setDescription(None)
vocabulary_term_PIPELINE_VERSION_10.setLabel(None)
vocabulary_term_PIPELINE_VERSION_10.setOrdinal(12)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_10)
          
vocabulary_term_PIPELINE_VERSION_PRE_10 = tr.createNewVocabularyTerm('PRE_1.0')
vocabulary_term_PIPELINE_VERSION_PRE_10.setDescription('Before Pipeline Version 1.0')
vocabulary_term_PIPELINE_VERSION_PRE_10.setLabel(None)
vocabulary_term_PIPELINE_VERSION_PRE_10.setOrdinal(13)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_PRE_10)
          
vocabulary_term_PIPELINE_VERSION_NONE = tr.createNewVocabularyTerm('NONE')
vocabulary_term_PIPELINE_VERSION_NONE.setDescription(None)
vocabulary_term_PIPELINE_VERSION_NONE.setLabel(None)
vocabulary_term_PIPELINE_VERSION_NONE.setOrdinal(14)
vocabulary_PIPELINE_VERSION.addTerm(vocabulary_term_PIPELINE_VERSION_NONE)
    
vocabulary_SAMPLE_TYPE = tr.getOrCreateNewVocabulary('SAMPLE_TYPE')
vocabulary_SAMPLE_TYPE.setDescription('Type of sample delivered by the customer')
vocabulary_SAMPLE_TYPE.setUrlTemplate(None)
vocabulary_SAMPLE_TYPE.setManagedInternally(False)
vocabulary_SAMPLE_TYPE.setInternalNamespace(False)
vocabulary_SAMPLE_TYPE.setChosenFromList(True)
          
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY = tr.createNewVocabularyTerm('PROCESSED_DNA_LIBRARY')
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setDescription(None)
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setLabel(None)
vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY.setOrdinal(1)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_PROCESSED_DNA_LIBRARY)
          
vocabulary_term_SAMPLE_TYPE_POOLED_SAMPLE = tr.createNewVocabularyTerm('POOLED_SAMPLE')
vocabulary_term_SAMPLE_TYPE_POOLED_SAMPLE.setDescription(None)
vocabulary_term_SAMPLE_TYPE_POOLED_SAMPLE.setLabel(None)
vocabulary_term_SAMPLE_TYPE_POOLED_SAMPLE.setOrdinal(2)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_POOLED_SAMPLE)
          
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA = tr.createNewVocabularyTerm('GENOMIC_DNA')
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA.setOrdinal(3)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_GENOMIC_DNA)
          
vocabulary_term_SAMPLE_TYPE_SMALL_RNA = tr.createNewVocabularyTerm('SMALL_RNA')
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_SMALL_RNA.setOrdinal(4)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_SMALL_RNA)
          
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA = tr.createNewVocabularyTerm('TOTAL_RNA')
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_TOTAL_RNA.setOrdinal(5)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_TOTAL_RNA)
          
vocabulary_term_SAMPLE_TYPE_CHIP = tr.createNewVocabularyTerm('CHIP')
vocabulary_term_SAMPLE_TYPE_CHIP.setDescription(None)
vocabulary_term_SAMPLE_TYPE_CHIP.setLabel(None)
vocabulary_term_SAMPLE_TYPE_CHIP.setOrdinal(6)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_CHIP)
          
vocabulary_term_SAMPLE_TYPE_VIRAL = tr.createNewVocabularyTerm('VIRAL')
vocabulary_term_SAMPLE_TYPE_VIRAL.setDescription(None)
vocabulary_term_SAMPLE_TYPE_VIRAL.setLabel(None)
vocabulary_term_SAMPLE_TYPE_VIRAL.setOrdinal(7)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_VIRAL)
          
vocabulary_term_SAMPLE_TYPE_SYNTHETIC = tr.createNewVocabularyTerm('SYNTHETIC')
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setDescription(None)
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setLabel(None)
vocabulary_term_SAMPLE_TYPE_SYNTHETIC.setOrdinal(8)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_SYNTHETIC)
          
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA = tr.createNewVocabularyTerm('FRAGMENTED_GENOMIC_DNA')
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA.setOrdinal(9)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_FRAGMENTED_GENOMIC_DNA)
          
vocabulary_term_SAMPLE_TYPE_MRNA = tr.createNewVocabularyTerm('MRNA')
vocabulary_term_SAMPLE_TYPE_MRNA.setDescription(None)
vocabulary_term_SAMPLE_TYPE_MRNA.setLabel(None)
vocabulary_term_SAMPLE_TYPE_MRNA.setOrdinal(10)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_MRNA)
          
vocabulary_term_SAMPLE_TYPE_OTHER = tr.createNewVocabularyTerm('OTHER')
vocabulary_term_SAMPLE_TYPE_OTHER.setDescription(None)
vocabulary_term_SAMPLE_TYPE_OTHER.setLabel(None)
vocabulary_term_SAMPLE_TYPE_OTHER.setOrdinal(11)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_OTHER)
          
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP = tr.createNewVocabularyTerm('BISULFITE_CHIP')
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setDescription(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setLabel(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP.setOrdinal(13)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_BISULFITE_CHIP)
          
vocabulary_term_SAMPLE_TYPE_NONGENOMIC = tr.createNewVocabularyTerm('NON-GENOMIC')
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setDescription(None)
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setLabel(None)
vocabulary_term_SAMPLE_TYPE_NONGENOMIC.setOrdinal(14)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_NONGENOMIC)
          
vocabulary_term_SAMPLE_TYPE_BISULFITE_TREATED = tr.createNewVocabularyTerm('BISULFITE_TREATED')
vocabulary_term_SAMPLE_TYPE_BISULFITE_TREATED.setDescription(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_TREATED.setLabel(None)
vocabulary_term_SAMPLE_TYPE_BISULFITE_TREATED.setOrdinal(15)
vocabulary_SAMPLE_TYPE.addTerm(vocabulary_term_SAMPLE_TYPE_BISULFITE_TREATED)
    
vocabulary_SBS_SEQUENCING_KIT_VERSION = tr.getOrCreateNewVocabulary('SBS_SEQUENCING_KIT_VERSION')
vocabulary_SBS_SEQUENCING_KIT_VERSION.setDescription('Version of the Sequencing by Synthesis (SBS) Kit')
vocabulary_SBS_SEQUENCING_KIT_VERSION.setUrlTemplate(None)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setManagedInternally(False)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setInternalNamespace(False)
vocabulary_SBS_SEQUENCING_KIT_VERSION.setChosenFromList(True)
          
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5 = tr.createNewVocabularyTerm('TRUSEQ_V5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setLabel('TruSeq v5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5.setOrdinal(1)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_TRUSEQ_V5)
          
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5 = tr.createNewVocabularyTerm('V5')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5.setOrdinal(2)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V5)
          
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4 = tr.createNewVocabularyTerm('V4')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4.setOrdinal(3)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V4)
          
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3 = tr.createNewVocabularyTerm('V3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setLabel(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3.setOrdinal(4)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_V3)
          
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3 = tr.createNewVocabularyTerm('SBS_HS_V3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setDescription(None)
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setLabel('TruSeq SBS HS v3')
vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3.setOrdinal(5)
vocabulary_SBS_SEQUENCING_KIT_VERSION.addTerm(vocabulary_term_SBS_SEQUENCING_KIT_VERSION_SBS_HS_V3)
    
vocabulary_SEQUENCER = tr.getOrCreateNewVocabulary('SEQUENCER')
vocabulary_SEQUENCER.setDescription('Which Sequencer was used?')
vocabulary_SEQUENCER.setUrlTemplate(None)
vocabulary_SEQUENCER.setManagedInternally(False)
vocabulary_SEQUENCER.setInternalNamespace(False)
vocabulary_SEQUENCER.setChosenFromList(True)
          
vocabulary_term_SEQUENCER_M00721 = tr.createNewVocabularyTerm('M00721')
vocabulary_term_SEQUENCER_M00721.setDescription(None)
vocabulary_term_SEQUENCER_M00721.setLabel('MiSeq')
vocabulary_term_SEQUENCER_M00721.setOrdinal(6)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_M00721)
          
vocabulary_term_SEQUENCER_SN792 = tr.createNewVocabularyTerm('SN792')
vocabulary_term_SEQUENCER_SN792.setDescription(None)
vocabulary_term_SEQUENCER_SN792.setLabel('HiSeq 2000')
vocabulary_term_SEQUENCER_SN792.setOrdinal(7)
vocabulary_SEQUENCER.addTerm(vocabulary_term_SEQUENCER_SN792)
    
vocabulary_SEQUENCING_APPLICATION = tr.getOrCreateNewVocabulary('SEQUENCING_APPLICATION')
vocabulary_SEQUENCING_APPLICATION.setDescription('Type of experiment of the High Throughput Sequencing applications')
vocabulary_SEQUENCING_APPLICATION.setUrlTemplate(None)
vocabulary_SEQUENCING_APPLICATION.setManagedInternally(False)
vocabulary_SEQUENCING_APPLICATION.setInternalNamespace(False)
vocabulary_SEQUENCING_APPLICATION.setChosenFromList(True)
          
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ = tr.createNewVocabularyTerm('SMALL_RNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ.setOrdinal(1)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_SMALL_RNA_SEQ)
          
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ = tr.createNewVocabularyTerm('RNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ.setOrdinal(2)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_RNA_SEQ)
          
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ = tr.createNewVocabularyTerm('GENOMIC_DNA_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ.setOrdinal(3)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_GENOMIC_DNA_SEQ)
          
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ = tr.createNewVocabularyTerm('CHIP_SEQ')
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setDescription(None)
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setLabel(None)
vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ.setOrdinal(4)
vocabulary_SEQUENCING_APPLICATION.addTerm(vocabulary_term_SEQUENCING_APPLICATION_CHIP_SEQ)
    
vocabulary_YES_NO = tr.getOrCreateNewVocabulary('YES_NO')
vocabulary_YES_NO.setDescription('Just offers YES or NO')
vocabulary_YES_NO.setUrlTemplate(None)
vocabulary_YES_NO.setManagedInternally(False)
vocabulary_YES_NO.setInternalNamespace(False)
vocabulary_YES_NO.setChosenFromList(True)
          
vocabulary_term_YES_NO_YES = tr.createNewVocabularyTerm('YES')
vocabulary_term_YES_NO_YES.setDescription(None)
vocabulary_term_YES_NO_YES.setLabel(None)
vocabulary_term_YES_NO_YES.setOrdinal(1)
vocabulary_YES_NO.addTerm(vocabulary_term_YES_NO_YES)
          
vocabulary_term_YES_NO_NO = tr.createNewVocabularyTerm('NO')
vocabulary_term_YES_NO_NO.setDescription(None)
vocabulary_term_YES_NO_NO.setLabel(None)
vocabulary_term_YES_NO_NO.setOrdinal(2)
vocabulary_YES_NO.addTerm(vocabulary_term_YES_NO_NO)

print "Imported 22 Vocabularies" 
script_Diff_time = tr.getOrCreateNewScript('Diff_time')
script_Diff_time.setName('Diff_time')
script_Diff_time.setDescription('Calculates the difference of two given dates')
script_Diff_time.setScript('''from datetime import datetime

def dateTimeSplitter(openbisDate):
  dateAndTime, tz = openbisDate.rsplit(" ", 1)
  pythonDateTime = datetime.strptime(dateAndTime, "%Y-%m-%d %H:%M:%S")  
  return pythonDateTime

def calculate():
  
  try:
    start = dateTimeSplitter(entity.propertyValue('FLOW_CELL_SEQUENCED_ON'))
    end = dateTimeSplitter(entity.propertyValue('SEQUENCER_FINISHED'))
    diffTime = end-start
    return str(diffTime)
  except:
    return "N/A"
''')
script_Diff_time.setEntityForScript('SAMPLE')
script_Diff_time.setScriptType('DYNAMIC_PROPERTY')

script_Has_Parents = tr.getOrCreateNewScript('Has_Parents')
script_Has_Parents.setName('Has_Parents')
script_Has_Parents.setDescription('Check if the Entity has a parent')
script_Has_Parents.setScript('''def validate(entity, isNew):
  parents = entity.entityPE().parents
  if parents:
    return None
  else:
    return "No Parents have been selected!"
''')
script_Has_Parents.setEntityForScript('SAMPLE')
script_Has_Parents.setScriptType('ENTITY_VALIDATION')

print "Imported 2 Scripts" 
exp_type_HT_SEQUENCING = tr.getOrCreateNewExperimentType('HT_SEQUENCING')
exp_type_HT_SEQUENCING.setDescription('High Throughput Sequencing (e.g. Illumina HiSeq, Illumina GA)')


print "Imported 1 Experiment Types" 
samp_type_ILLUMINA_FLOW_CELL = tr.getOrCreateNewSampleType('ILLUMINA_FLOW_CELL')
samp_type_ILLUMINA_FLOW_CELL.setDescription('Container of ILLUMINA_FLOW_LANES\ Can be created automatically by a drop box and the properties can be filled from the RunInfo.xml and runParameters.xml files')
samp_type_ILLUMINA_FLOW_CELL.setListable(True)
samp_type_ILLUMINA_FLOW_CELL.setShowContainer(False)
samp_type_ILLUMINA_FLOW_CELL.setShowParents(False)
samp_type_ILLUMINA_FLOW_CELL.setSubcodeUnique(False)
samp_type_ILLUMINA_FLOW_CELL.setAutoGeneratedCode(False)
samp_type_ILLUMINA_FLOW_CELL.setShowParentMetadata(False)
samp_type_ILLUMINA_FLOW_CELL.setGeneratedCodePrefix('FLOWCELL-')


samp_type_ILLUMINA_FLOW_LANE = tr.getOrCreateNewSampleType('ILLUMINA_FLOW_LANE')
samp_type_ILLUMINA_FLOW_LANE.setDescription('Child of LIBRARY or POOL')
samp_type_ILLUMINA_FLOW_LANE.setListable(True)
samp_type_ILLUMINA_FLOW_LANE.setShowContainer(True)
samp_type_ILLUMINA_FLOW_LANE.setShowParents(True)
samp_type_ILLUMINA_FLOW_LANE.setSubcodeUnique(False)
samp_type_ILLUMINA_FLOW_LANE.setAutoGeneratedCode(False)
samp_type_ILLUMINA_FLOW_LANE.setShowParentMetadata(True)
samp_type_ILLUMINA_FLOW_LANE.setGeneratedCodePrefix('FLOWLANE-')


samp_type_LIBRARY = tr.getOrCreateNewSampleType('LIBRARY')
samp_type_LIBRARY.setDescription('Child of RAW and potential parent of POOL')
samp_type_LIBRARY.setListable(True)
samp_type_LIBRARY.setShowContainer(False)
samp_type_LIBRARY.setShowParents(True)
samp_type_LIBRARY.setSubcodeUnique(False)
samp_type_LIBRARY.setAutoGeneratedCode(True)
samp_type_LIBRARY.setShowParentMetadata(True)
samp_type_LIBRARY.setGeneratedCodePrefix('BSSE-QGF-LIBRARY-')
samp_type_LIBRARY.setValidationScript(script_Has_Parents)

samp_type_LIBRARY_POOL = tr.getOrCreateNewSampleType('LIBRARY_POOL')
samp_type_LIBRARY_POOL.setDescription('Child of several LIBRARY samples, used for multiplexing')
samp_type_LIBRARY_POOL.setListable(True)
samp_type_LIBRARY_POOL.setShowContainer(False)
samp_type_LIBRARY_POOL.setShowParents(True)
samp_type_LIBRARY_POOL.setSubcodeUnique(False)
samp_type_LIBRARY_POOL.setAutoGeneratedCode(True)
samp_type_LIBRARY_POOL.setShowParentMetadata(True)
samp_type_LIBRARY_POOL.setGeneratedCodePrefix('BSSE-QGF-POOL-')
samp_type_LIBRARY_POOL.setValidationScript(script_Has_Parents)

samp_type_MASTER_SAMPLE = tr.getOrCreateNewSampleType('MASTER_SAMPLE')
samp_type_MASTER_SAMPLE.setDescription('Sample Type holding Master Data and a parent to RAW')
samp_type_MASTER_SAMPLE.setListable(True)
samp_type_MASTER_SAMPLE.setShowContainer(False)
samp_type_MASTER_SAMPLE.setShowParents(False)
samp_type_MASTER_SAMPLE.setSubcodeUnique(False)
samp_type_MASTER_SAMPLE.setAutoGeneratedCode(True)
samp_type_MASTER_SAMPLE.setShowParentMetadata(False)
samp_type_MASTER_SAMPLE.setGeneratedCodePrefix('BSSE-QGF-MASTER-')


samp_type_RAW_SAMPLE = tr.getOrCreateNewSampleType('RAW_SAMPLE')
samp_type_RAW_SAMPLE.setDescription('Child of MASTER')
samp_type_RAW_SAMPLE.setListable(True)
samp_type_RAW_SAMPLE.setShowContainer(False)
samp_type_RAW_SAMPLE.setShowParents(True)
samp_type_RAW_SAMPLE.setSubcodeUnique(False)
samp_type_RAW_SAMPLE.setAutoGeneratedCode(True)
samp_type_RAW_SAMPLE.setShowParentMetadata(True)
samp_type_RAW_SAMPLE.setGeneratedCodePrefix('BSSE-QGF-RAW-')
samp_type_RAW_SAMPLE.setValidationScript(script_Has_Parents)

print "Imported 6 Sample Types" 
data_set_type_ALIGNMENT = tr.getOrCreateNewDataSetType('ALIGNMENT')
data_set_type_ALIGNMENT.setDescription('Aligner ouput, ideally bam/sam')
data_set_type_ALIGNMENT.setMainDataSetPattern(None)
data_set_type_ALIGNMENT.setMainDataSetPath(None)
data_set_type_ALIGNMENT.setDeletionDisallowed(False)


data_set_type_BASECALL_STATS = tr.getOrCreateNewDataSetType('BASECALL_STATS')
data_set_type_BASECALL_STATS.setDescription('Base Call Statistics from the Illumina Pipeline (configureBclToFastq.pl)')
data_set_type_BASECALL_STATS.setMainDataSetPattern('original/.*/.*/Demultiplex_Stats.htm')
data_set_type_BASECALL_STATS.setMainDataSetPath(None)
data_set_type_BASECALL_STATS.setDeletionDisallowed(False)


data_set_type_BIGWIGGLE = tr.getOrCreateNewDataSetType('BIGWIGGLE')
data_set_type_BIGWIGGLE.setDescription('Visualization')
data_set_type_BIGWIGGLE.setMainDataSetPattern(None)
data_set_type_BIGWIGGLE.setMainDataSetPath(None)
data_set_type_BIGWIGGLE.setDeletionDisallowed(False)


data_set_type_ELAND_ALIGNMENT = tr.getOrCreateNewDataSetType('ELAND_ALIGNMENT')
data_set_type_ELAND_ALIGNMENT.setDescription('Illumina Eland Alignment Output')
data_set_type_ELAND_ALIGNMENT.setMainDataSetPattern(None)
data_set_type_ELAND_ALIGNMENT.setMainDataSetPath(None)
data_set_type_ELAND_ALIGNMENT.setDeletionDisallowed(False)


data_set_type_FASTQ_GZ = tr.getOrCreateNewDataSetType('FASTQ_GZ')
data_set_type_FASTQ_GZ.setDescription('Gzipped Fastq file produced by Casava 1.8+')
data_set_type_FASTQ_GZ.setMainDataSetPattern(None)
data_set_type_FASTQ_GZ.setMainDataSetPath(None)
data_set_type_FASTQ_GZ.setDeletionDisallowed(False)


data_set_type_ILLUMINA_HISEQ_OUTPUT = tr.getOrCreateNewDataSetType('ILLUMINA_HISEQ_OUTPUT')
data_set_type_ILLUMINA_HISEQ_OUTPUT.setDescription('HiSeq2000 Output')
data_set_type_ILLUMINA_HISEQ_OUTPUT.setMainDataSetPattern(None)
data_set_type_ILLUMINA_HISEQ_OUTPUT.setMainDataSetPath(None)
data_set_type_ILLUMINA_HISEQ_OUTPUT.setDeletionDisallowed(False)


data_set_type_ILLUMINA_MISEQ_OUTPUT = tr.getOrCreateNewDataSetType('ILLUMINA_MISEQ_OUTPUT')
data_set_type_ILLUMINA_MISEQ_OUTPUT.setDescription('MiSeq Output')
data_set_type_ILLUMINA_MISEQ_OUTPUT.setMainDataSetPattern(None)
data_set_type_ILLUMINA_MISEQ_OUTPUT.setMainDataSetPath(None)
data_set_type_ILLUMINA_MISEQ_OUTPUT.setDeletionDisallowed(False)


data_set_type_MACS_OUTPUT = tr.getOrCreateNewDataSetType('MACS_OUTPUT')
data_set_type_MACS_OUTPUT.setDescription('MACS Peak Caller output')
data_set_type_MACS_OUTPUT.setMainDataSetPattern(None)
data_set_type_MACS_OUTPUT.setMainDataSetPath(None)
data_set_type_MACS_OUTPUT.setDeletionDisallowed(False)


data_set_type_QUALITY_JPGS = tr.getOrCreateNewDataSetType('QUALITY_JPGS')
data_set_type_QUALITY_JPGS.setDescription('R generated Quality plots')
data_set_type_QUALITY_JPGS.setMainDataSetPattern(None)
data_set_type_QUALITY_JPGS.setMainDataSetPath(None)
data_set_type_QUALITY_JPGS.setDeletionDisallowed(False)


data_set_type_QUALITY_PDFS = tr.getOrCreateNewDataSetType('QUALITY_PDFS')
data_set_type_QUALITY_PDFS.setDescription('R generated pdfs showing quality data')
data_set_type_QUALITY_PDFS.setMainDataSetPattern(None)
data_set_type_QUALITY_PDFS.setMainDataSetPath(None)
data_set_type_QUALITY_PDFS.setDeletionDisallowed(False)


data_set_type_QUALITY_SVG = tr.getOrCreateNewDataSetType('QUALITY_SVG')
data_set_type_QUALITY_SVG.setDescription('R generated Quality plots')
data_set_type_QUALITY_SVG.setMainDataSetPattern(None)
data_set_type_QUALITY_SVG.setMainDataSetPath(None)
data_set_type_QUALITY_SVG.setDeletionDisallowed(False)


data_set_type_RUNINFO = tr.getOrCreateNewDataSetType('RUNINFO')
data_set_type_RUNINFO.setDescription('Run statstics: Status.html and Status_Files folder')
data_set_type_RUNINFO.setMainDataSetPattern('original/.*/Data/Status.htm')
data_set_type_RUNINFO.setMainDataSetPath(None)
data_set_type_RUNINFO.setDeletionDisallowed(False)


data_set_type_THUMBNAILS = tr.getOrCreateNewDataSetType('THUMBNAILS')
data_set_type_THUMBNAILS.setDescription('Thumbnails provided by the Illumina software')
data_set_type_THUMBNAILS.setMainDataSetPattern(None)
data_set_type_THUMBNAILS.setMainDataSetPath(None)
data_set_type_THUMBNAILS.setDeletionDisallowed(False)


data_set_type_TSV = tr.getOrCreateNewDataSetType('TSV')
data_set_type_TSV.setDescription('Tab separated files')
data_set_type_TSV.setMainDataSetPattern(None)
data_set_type_TSV.setMainDataSetPath(None)
data_set_type_TSV.setDeletionDisallowed(False)


print "Imported 15 Data Set Types" 
print "Imported 0 Material Types" 
prop_type_AGILENT_KIT = tr.getOrCreateNewPropertyType('AGILENT_KIT', DataType.CONTROLLEDVOCABULARY)
prop_type_AGILENT_KIT.setLabel('Agilent Kit')
prop_type_AGILENT_KIT.setManagedInternally(False)
prop_type_AGILENT_KIT.setInternalNamespace(False)

prop_type_AGILENT_KIT.setVocabulary(vocabulary_AGILENT_KIT)

prop_type_ALIGNMENT_SOFTWARE = tr.getOrCreateNewPropertyType('ALIGNMENT_SOFTWARE', DataType.CONTROLLEDVOCABULARY)
prop_type_ALIGNMENT_SOFTWARE.setLabel('Alignment software')
prop_type_ALIGNMENT_SOFTWARE.setManagedInternally(False)
prop_type_ALIGNMENT_SOFTWARE.setInternalNamespace(False)

prop_type_ALIGNMENT_SOFTWARE.setVocabulary(vocabulary_ALIGNMENT_SOFTWARE)

prop_type_ANALYSIS_FINISHED = tr.getOrCreateNewPropertyType('ANALYSIS_FINISHED', DataType.TIMESTAMP)
prop_type_ANALYSIS_FINISHED.setLabel('Analysis finished')
prop_type_ANALYSIS_FINISHED.setManagedInternally(False)
prop_type_ANALYSIS_FINISHED.setInternalNamespace(False)


prop_type_BARCODE_COMPLEXITY_CHECKER = tr.getOrCreateNewPropertyType('BARCODE_COMPLEXITY_CHECKER', DataType.MULTILINE_VARCHAR)
prop_type_BARCODE_COMPLEXITY_CHECKER.setLabel('Barcode_Complexity_Checker')
prop_type_BARCODE_COMPLEXITY_CHECKER.setManagedInternally(False)
prop_type_BARCODE_COMPLEXITY_CHECKER.setInternalNamespace(False)


prop_type_BASESCOVERED = tr.getOrCreateNewPropertyType('BASESCOVERED', DataType.INTEGER)
prop_type_BASESCOVERED.setLabel('bases Covered')
prop_type_BASESCOVERED.setManagedInternally(False)
prop_type_BASESCOVERED.setInternalNamespace(False)


prop_type_BIOLOGICAL_SAMPLE_ARRIVED = tr.getOrCreateNewPropertyType('BIOLOGICAL_SAMPLE_ARRIVED', DataType.TIMESTAMP)
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setLabel('Arrival Date of Biological Sample')
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setManagedInternally(False)
prop_type_BIOLOGICAL_SAMPLE_ARRIVED.setInternalNamespace(False)


prop_type_CASAVA_VERSION = tr.getOrCreateNewPropertyType('CASAVA_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_CASAVA_VERSION.setLabel('Casava Version')
prop_type_CASAVA_VERSION.setManagedInternally(False)
prop_type_CASAVA_VERSION.setInternalNamespace(False)

prop_type_CASAVA_VERSION.setVocabulary(vocabulary_CASAVA_VERSION)

prop_type_CELL_PLASTICITY_SYSTEMSX = tr.getOrCreateNewPropertyType('CELL_PLASTICITY_SYSTEMSX', DataType.BOOLEAN)
prop_type_CELL_PLASTICITY_SYSTEMSX.setLabel('Cell Plasticity (SystemsX)')
prop_type_CELL_PLASTICITY_SYSTEMSX.setManagedInternally(False)
prop_type_CELL_PLASTICITY_SYSTEMSX.setInternalNamespace(False)


prop_type_CHROMCOUNT = tr.getOrCreateNewPropertyType('CHROMCOUNT', DataType.INTEGER)
prop_type_CHROMCOUNT.setLabel('chrom Count')
prop_type_CHROMCOUNT.setManagedInternally(False)
prop_type_CHROMCOUNT.setInternalNamespace(False)


prop_type_CLUSTER_GENERATION_KIT_VERSION = tr.getOrCreateNewPropertyType('CLUSTER_GENERATION_KIT_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_CLUSTER_GENERATION_KIT_VERSION.setLabel('CS Generation Kit Version')
prop_type_CLUSTER_GENERATION_KIT_VERSION.setManagedInternally(False)
prop_type_CLUSTER_GENERATION_KIT_VERSION.setInternalNamespace(False)

prop_type_CLUSTER_GENERATION_KIT_VERSION.setVocabulary(vocabulary_CLUSTER_GENERATION_KIT_VERSION)

prop_type_CLUSTER_STATION = tr.getOrCreateNewPropertyType('CLUSTER_STATION', DataType.CONTROLLEDVOCABULARY)
prop_type_CLUSTER_STATION.setLabel('Cluster Station')
prop_type_CLUSTER_STATION.setManagedInternally(False)
prop_type_CLUSTER_STATION.setInternalNamespace(False)

prop_type_CLUSTER_STATION.setVocabulary(vocabulary_CLUSTER_STATION)

prop_type_CONCENTRATION = tr.getOrCreateNewPropertyType('CONCENTRATION', DataType.REAL)
prop_type_CONCENTRATION.setLabel(u'Conc. [ng/\u03bcl]')
prop_type_CONCENTRATION.setManagedInternally(False)
prop_type_CONCENTRATION.setInternalNamespace(False)


prop_type_CONCENTRATION_FLOWLANE = tr.getOrCreateNewPropertyType('CONCENTRATION_FLOWLANE', DataType.REAL)
prop_type_CONCENTRATION_FLOWLANE.setLabel('Concentration in flow lane [pM]')
prop_type_CONCENTRATION_FLOWLANE.setManagedInternally(False)
prop_type_CONCENTRATION_FLOWLANE.setInternalNamespace(False)


prop_type_CONCENTRATION_ORIGINAL_ILLUMINA = tr.getOrCreateNewPropertyType('CONCENTRATION_ORIGINAL_ILLUMINA', DataType.REAL)
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setLabel(u'Concentration (original) [ng/\u03bcl]')
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setManagedInternally(False)
prop_type_CONCENTRATION_ORIGINAL_ILLUMINA.setInternalNamespace(False)


prop_type_CONCENTRATION_PREPARED_ILLUMINA = tr.getOrCreateNewPropertyType('CONCENTRATION_PREPARED_ILLUMINA', DataType.REAL)
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setLabel(u'Concentration (prepared) [ng/\u03bcl]')
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setManagedInternally(False)
prop_type_CONCENTRATION_PREPARED_ILLUMINA.setInternalNamespace(False)


prop_type_CONTACT_PERSON_EMAIL = tr.getOrCreateNewPropertyType('CONTACT_PERSON_EMAIL', DataType.VARCHAR)
prop_type_CONTACT_PERSON_EMAIL.setLabel('Email of Contact Person')
prop_type_CONTACT_PERSON_EMAIL.setManagedInternally(False)
prop_type_CONTACT_PERSON_EMAIL.setInternalNamespace(False)


prop_type_CONTACT_PERSON_NAME = tr.getOrCreateNewPropertyType('CONTACT_PERSON_NAME', DataType.VARCHAR)
prop_type_CONTACT_PERSON_NAME.setLabel('Name of Contact Person')
prop_type_CONTACT_PERSON_NAME.setManagedInternally(False)
prop_type_CONTACT_PERSON_NAME.setInternalNamespace(False)


prop_type_CONTROL_LANE = tr.getOrCreateNewPropertyType('CONTROL_LANE', DataType.CONTROLLEDVOCABULARY)
prop_type_CONTROL_LANE.setLabel('Control Lane')
prop_type_CONTROL_LANE.setManagedInternally(False)
prop_type_CONTROL_LANE.setInternalNamespace(False)

prop_type_CONTROL_LANE.setVocabulary(vocabulary_CONTROL_LANE)

prop_type_CREATED_ON_CS = tr.getOrCreateNewPropertyType('CREATED_ON_CS', DataType.TIMESTAMP)
prop_type_CREATED_ON_CS.setLabel('Clustering date')
prop_type_CREATED_ON_CS.setManagedInternally(False)
prop_type_CREATED_ON_CS.setInternalNamespace(False)


prop_type_CS_PROTOCOL_VERSION = tr.getOrCreateNewPropertyType('CS_PROTOCOL_VERSION', DataType.VARCHAR)
prop_type_CS_PROTOCOL_VERSION.setLabel('CS Protocol Version')
prop_type_CS_PROTOCOL_VERSION.setManagedInternally(False)
prop_type_CS_PROTOCOL_VERSION.setInternalNamespace(False)


prop_type_CYCLES = tr.getOrCreateNewPropertyType('CYCLES', DataType.INTEGER)
prop_type_CYCLES.setLabel('Cycles')
prop_type_CYCLES.setManagedInternally(False)
prop_type_CYCLES.setInternalNamespace(False)


prop_type_DATA_TRANSFERRED = tr.getOrCreateNewPropertyType('DATA_TRANSFERRED', DataType.TIMESTAMP)
prop_type_DATA_TRANSFERRED.setLabel('Data transferred')
prop_type_DATA_TRANSFERRED.setManagedInternally(False)
prop_type_DATA_TRANSFERRED.setInternalNamespace(False)


prop_type_DNA_CONCENTRATION_OF_LIBRARY = tr.getOrCreateNewPropertyType('DNA_CONCENTRATION_OF_LIBRARY', DataType.INTEGER)
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setLabel('DNA concentration of library (nM)')
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setManagedInternally(False)
prop_type_DNA_CONCENTRATION_OF_LIBRARY.setInternalNamespace(False)


prop_type_DNA_CONCENTRATION_POOL = tr.getOrCreateNewPropertyType('DNA_CONCENTRATION_POOL', DataType.REAL)
prop_type_DNA_CONCENTRATION_POOL.setLabel('DNA conc. [nM] - customer value')
prop_type_DNA_CONCENTRATION_POOL.setManagedInternally(False)
prop_type_DNA_CONCENTRATION_POOL.setInternalNamespace(False)


prop_type_DNA_CONCENTRATION_QGF = tr.getOrCreateNewPropertyType('DNA_CONCENTRATION_QGF', DataType.REAL)
prop_type_DNA_CONCENTRATION_QGF.setLabel('DNA concentration of Pool [nM]')
prop_type_DNA_CONCENTRATION_QGF.setManagedInternally(False)
prop_type_DNA_CONCENTRATION_QGF.setInternalNamespace(False)


prop_type_END_TYPE = tr.getOrCreateNewPropertyType('END_TYPE', DataType.CONTROLLEDVOCABULARY)
prop_type_END_TYPE.setLabel('Paired End / Single Read')
prop_type_END_TYPE.setManagedInternally(False)
prop_type_END_TYPE.setInternalNamespace(False)

prop_type_END_TYPE.setVocabulary(vocabulary_END_TYPE)

prop_type_EXPERIMENT_DESIGN = tr.getOrCreateNewPropertyType('EXPERIMENT_DESIGN', DataType.CONTROLLEDVOCABULARY)
prop_type_EXPERIMENT_DESIGN.setLabel('Experiment Design')
prop_type_EXPERIMENT_DESIGN.setManagedInternally(False)
prop_type_EXPERIMENT_DESIGN.setInternalNamespace(False)

prop_type_EXPERIMENT_DESIGN.setVocabulary(vocabulary_EXPERIMENT_DESIGN)

prop_type_EXTERNAL_SAMPLE_NAME = tr.getOrCreateNewPropertyType('EXTERNAL_SAMPLE_NAME', DataType.VARCHAR)
prop_type_EXTERNAL_SAMPLE_NAME.setLabel('External Sample Name')
prop_type_EXTERNAL_SAMPLE_NAME.setManagedInternally(False)
prop_type_EXTERNAL_SAMPLE_NAME.setInternalNamespace(False)


prop_type_FLOWCELLTYPE = tr.getOrCreateNewPropertyType('FLOWCELLTYPE', DataType.VARCHAR)
prop_type_FLOWCELLTYPE.setLabel('Flow Cell Type')
prop_type_FLOWCELLTYPE.setManagedInternally(False)
prop_type_FLOWCELLTYPE.setInternalNamespace(False)


prop_type_FLOW_CELL_SEQUENCED_ON = tr.getOrCreateNewPropertyType('FLOW_CELL_SEQUENCED_ON', DataType.TIMESTAMP)
prop_type_FLOW_CELL_SEQUENCED_ON.setLabel('Sequencing started')
prop_type_FLOW_CELL_SEQUENCED_ON.setManagedInternally(False)
prop_type_FLOW_CELL_SEQUENCED_ON.setInternalNamespace(False)


prop_type_FRAGMENT_SIZE_BASE_PAIRS = tr.getOrCreateNewPropertyType('FRAGMENT_SIZE_BASE_PAIRS', DataType.INTEGER)
prop_type_FRAGMENT_SIZE_BASE_PAIRS.setLabel('Fragment Size [base pairs]')
prop_type_FRAGMENT_SIZE_BASE_PAIRS.setManagedInternally(False)
prop_type_FRAGMENT_SIZE_BASE_PAIRS.setInternalNamespace(False)


prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA = tr.getOrCreateNewPropertyType('FRAGMENT_SIZE_PREPARED_ILLUMINA', DataType.INTEGER)
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setLabel('Fragment Size (prepared) [base (pairs)]')
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setManagedInternally(False)
prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA.setInternalNamespace(False)


prop_type_ILLUMINA_PIPELINE_VERSION = tr.getOrCreateNewPropertyType('ILLUMINA_PIPELINE_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_ILLUMINA_PIPELINE_VERSION.setLabel('Pipeline Version')
prop_type_ILLUMINA_PIPELINE_VERSION.setManagedInternally(False)
prop_type_ILLUMINA_PIPELINE_VERSION.setInternalNamespace(False)

prop_type_ILLUMINA_PIPELINE_VERSION.setVocabulary(vocabulary_PIPELINE_VERSION)

prop_type_INDEX1 = tr.getOrCreateNewPropertyType('INDEX1', DataType.CONTROLLEDVOCABULARY)
prop_type_INDEX1.setLabel('Index 1')
prop_type_INDEX1.setManagedInternally(False)
prop_type_INDEX1.setInternalNamespace(False)

prop_type_INDEX1.setVocabulary(vocabulary_INDEX1)

prop_type_INDEX2 = tr.getOrCreateNewPropertyType('INDEX2', DataType.CONTROLLEDVOCABULARY)
prop_type_INDEX2.setLabel('Index 2')
prop_type_INDEX2.setManagedInternally(False)
prop_type_INDEX2.setInternalNamespace(False)

prop_type_INDEX2.setVocabulary(vocabulary_INDEX2)

prop_type_INDEXREAD = tr.getOrCreateNewPropertyType('INDEXREAD', DataType.INTEGER)
prop_type_INDEXREAD.setLabel('Length of Index Read1')
prop_type_INDEXREAD.setManagedInternally(False)
prop_type_INDEXREAD.setInternalNamespace(False)


prop_type_INDEXREAD2 = tr.getOrCreateNewPropertyType('INDEXREAD2', DataType.INTEGER)
prop_type_INDEXREAD2.setLabel('Length of Index Read2')
prop_type_INDEXREAD2.setManagedInternally(False)
prop_type_INDEXREAD2.setInternalNamespace(False)


prop_type_INVOICE = tr.getOrCreateNewPropertyType('INVOICE', DataType.BOOLEAN)
prop_type_INVOICE.setLabel('Invoice sent?')
prop_type_INVOICE.setManagedInternally(False)
prop_type_INVOICE.setInternalNamespace(False)


prop_type_ISCOMPRESSED = tr.getOrCreateNewPropertyType('ISCOMPRESSED', DataType.BOOLEAN)
prop_type_ISCOMPRESSED.setLabel('Is Compressed')
prop_type_ISCOMPRESSED.setManagedInternally(False)
prop_type_ISCOMPRESSED.setInternalNamespace(False)


prop_type_ISSUED_COMMAND = tr.getOrCreateNewPropertyType('ISSUED_COMMAND', DataType.MULTILINE_VARCHAR)
prop_type_ISSUED_COMMAND.setLabel('Issued Command')
prop_type_ISSUED_COMMAND.setManagedInternally(False)
prop_type_ISSUED_COMMAND.setInternalNamespace(False)


prop_type_ISSWAPPED = tr.getOrCreateNewPropertyType('ISSWAPPED', DataType.INTEGER)
prop_type_ISSWAPPED.setLabel('Is byte swapped')
prop_type_ISSWAPPED.setManagedInternally(False)
prop_type_ISSWAPPED.setInternalNamespace(False)


prop_type_KIT = tr.getOrCreateNewPropertyType('KIT', DataType.CONTROLLEDVOCABULARY)
prop_type_KIT.setLabel('Library preparation kit')
prop_type_KIT.setManagedInternally(False)
prop_type_KIT.setInternalNamespace(False)

prop_type_KIT.setVocabulary(vocabulary_KIT)

prop_type_KIT_PREPARED = tr.getOrCreateNewPropertyType('KIT_PREPARED', DataType.TIMESTAMP)
prop_type_KIT_PREPARED.setLabel('Sample processed')
prop_type_KIT_PREPARED.setManagedInternally(False)
prop_type_KIT_PREPARED.setInternalNamespace(False)


prop_type_LANECOUNT = tr.getOrCreateNewPropertyType('LANECOUNT', DataType.INTEGER)
prop_type_LANECOUNT.setLabel('Lane Count')
prop_type_LANECOUNT.setManagedInternally(False)
prop_type_LANECOUNT.setInternalNamespace(False)


prop_type_LIBRARY_PROCESSING_FAILED = tr.getOrCreateNewPropertyType('LIBRARY_PROCESSING_FAILED', DataType.BOOLEAN)
prop_type_LIBRARY_PROCESSING_FAILED.setLabel('Library processing failed')
prop_type_LIBRARY_PROCESSING_FAILED.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_FAILED.setInternalNamespace(False)


prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO = tr.getOrCreateNewPropertyType('LIBRARY_PROCESSING_POSSIBLE_YES_NO', DataType.CONTROLLEDVOCABULARY)
prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setLabel('Library processing possible')
prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setInternalNamespace(False)

prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setVocabulary(vocabulary_YES_NO)

prop_type_LIBRARY_PROCESSING_SUCCESSFUL = tr.getOrCreateNewPropertyType('LIBRARY_PROCESSING_SUCCESSFUL', DataType.BOOLEAN)
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setLabel('Library processing successful')
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setManagedInternally(False)
prop_type_LIBRARY_PROCESSING_SUCCESSFUL.setInternalNamespace(False)


prop_type_LOT = tr.getOrCreateNewPropertyType('LOT', DataType.INTEGER)
prop_type_LOT.setLabel('Kit Lot #')
prop_type_LOT.setManagedInternally(False)
prop_type_LOT.setInternalNamespace(False)


prop_type_MACS_VERSION = tr.getOrCreateNewPropertyType('MACS_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_MACS_VERSION.setLabel('MACS VERSION')
prop_type_MACS_VERSION.setManagedInternally(False)
prop_type_MACS_VERSION.setInternalNamespace(False)

prop_type_MACS_VERSION.setVocabulary(vocabulary_MACS_VERSION)

prop_type_MAPPED_READS = tr.getOrCreateNewPropertyType('MAPPED_READS', DataType.INTEGER)
prop_type_MAPPED_READS.setLabel('Mapped reads')
prop_type_MAPPED_READS.setManagedInternally(False)
prop_type_MAPPED_READS.setInternalNamespace(False)


prop_type_MAX = tr.getOrCreateNewPropertyType('MAX', DataType.REAL)
prop_type_MAX.setLabel('Maximum')
prop_type_MAX.setManagedInternally(False)
prop_type_MAX.setInternalNamespace(False)


prop_type_MEAN = tr.getOrCreateNewPropertyType('MEAN', DataType.REAL)
prop_type_MEAN.setLabel('Mean')
prop_type_MEAN.setManagedInternally(False)
prop_type_MEAN.setInternalNamespace(False)


prop_type_MIN = tr.getOrCreateNewPropertyType('MIN', DataType.REAL)
prop_type_MIN.setLabel('Minimum')
prop_type_MIN.setManagedInternally(False)
prop_type_MIN.setInternalNamespace(False)


prop_type_MISMATCH_IN_INDEX = tr.getOrCreateNewPropertyType('MISMATCH_IN_INDEX', DataType.CONTROLLEDVOCABULARY)
prop_type_MISMATCH_IN_INDEX.setLabel('Mismatch in Index')
prop_type_MISMATCH_IN_INDEX.setManagedInternally(False)
prop_type_MISMATCH_IN_INDEX.setInternalNamespace(False)

prop_type_MISMATCH_IN_INDEX.setVocabulary(vocabulary_MISMATCH_IN_INDEX)

prop_type_NANO_DROP = tr.getOrCreateNewPropertyType('NANO_DROP', DataType.CONTROLLEDVOCABULARY)
prop_type_NANO_DROP.setLabel('Nano Drop')
prop_type_NANO_DROP.setManagedInternally(False)
prop_type_NANO_DROP.setInternalNamespace(False)

prop_type_NANO_DROP.setVocabulary(vocabulary_NANO_DROP)

prop_type_NCBI_ORGANISM_TAXONOMY = tr.getOrCreateNewPropertyType('NCBI_ORGANISM_TAXONOMY', DataType.CONTROLLEDVOCABULARY)
prop_type_NCBI_ORGANISM_TAXONOMY.setLabel('Organism (NCBI Taxonomy)')
prop_type_NCBI_ORGANISM_TAXONOMY.setManagedInternally(False)
prop_type_NCBI_ORGANISM_TAXONOMY.setInternalNamespace(False)

prop_type_NCBI_ORGANISM_TAXONOMY.setVocabulary(vocabulary_NCBI_TAXONOMY)

prop_type_NM_DNA = tr.getOrCreateNewPropertyType('NM_DNA', DataType.REAL)
prop_type_NM_DNA.setLabel('Calculated DNA concentration of library (nM)')
prop_type_NM_DNA.setManagedInternally(False)
prop_type_NM_DNA.setInternalNamespace(False)


prop_type_NOTES = tr.getOrCreateNewPropertyType('NOTES', DataType.MULTILINE_VARCHAR)
prop_type_NOTES.setLabel('Notes')
prop_type_NOTES.setManagedInternally(False)
prop_type_NOTES.setInternalNamespace(False)


prop_type_NUMBER_OF_ATTACHMENTS = tr.getOrCreateNewPropertyType('NUMBER_OF_ATTACHMENTS', DataType.INTEGER)
prop_type_NUMBER_OF_ATTACHMENTS.setLabel('Number of Attachments')
prop_type_NUMBER_OF_ATTACHMENTS.setManagedInternally(False)
prop_type_NUMBER_OF_ATTACHMENTS.setInternalNamespace(False)


prop_type_PAIRED_END_KIT = tr.getOrCreateNewPropertyType('PAIRED_END_KIT', DataType.VARCHAR)
prop_type_PAIRED_END_KIT.setLabel('Paired End Kit')
prop_type_PAIRED_END_KIT.setManagedInternally(False)
prop_type_PAIRED_END_KIT.setInternalNamespace(False)


prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX = tr.getOrCreateNewPropertyType('PERCENTAGE_ONE_MISMATCH_READS_INDEX', DataType.REAL)
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setLabel('% One Mismatch Reads (Index)')
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setManagedInternally(False)
prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setInternalNamespace(False)


prop_type_PERCENTAGE_PASSED_FILTERING = tr.getOrCreateNewPropertyType('PERCENTAGE_PASSED_FILTERING', DataType.REAL)
prop_type_PERCENTAGE_PASSED_FILTERING.setLabel('% Passes Illumina Filtering (PF)')
prop_type_PERCENTAGE_PASSED_FILTERING.setManagedInternally(False)
prop_type_PERCENTAGE_PASSED_FILTERING.setInternalNamespace(False)


prop_type_PERCENTAGE_PERFECT_INDEX_READS = tr.getOrCreateNewPropertyType('PERCENTAGE_PERFECT_INDEX_READS', DataType.REAL)
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setLabel('% Perfect Index Reads')
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setManagedInternally(False)
prop_type_PERCENTAGE_PERFECT_INDEX_READS.setInternalNamespace(False)


prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE = tr.getOrCreateNewPropertyType('PERCENTAGE_RAW_CLUSTERS_PER_LANE', DataType.REAL)
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setLabel('% of raw clusters per lane')
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setManagedInternally(False)
prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setInternalNamespace(False)


prop_type_PREPARED_BY = tr.getOrCreateNewPropertyType('PREPARED_BY', DataType.VARCHAR)
prop_type_PREPARED_BY.setLabel('Prepared by')
prop_type_PREPARED_BY.setManagedInternally(False)
prop_type_PREPARED_BY.setInternalNamespace(False)


prop_type_PRIMARYDATASIZE = tr.getOrCreateNewPropertyType('PRIMARYDATASIZE', DataType.INTEGER)
prop_type_PRIMARYDATASIZE.setLabel('primary Data Size')
prop_type_PRIMARYDATASIZE.setManagedInternally(False)
prop_type_PRIMARYDATASIZE.setInternalNamespace(False)


prop_type_PRIMARYINDEXSIZE = tr.getOrCreateNewPropertyType('PRIMARYINDEXSIZE', DataType.INTEGER)
prop_type_PRIMARYINDEXSIZE.setLabel('primary Index Size')
prop_type_PRIMARYINDEXSIZE.setManagedInternally(False)
prop_type_PRIMARYINDEXSIZE.setInternalNamespace(False)


prop_type_PRINCIPAL_INVESTIGATOR_EMAIL = tr.getOrCreateNewPropertyType('PRINCIPAL_INVESTIGATOR_EMAIL', DataType.VARCHAR)
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setLabel('Email of Principal Investigator')
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setManagedInternally(False)
prop_type_PRINCIPAL_INVESTIGATOR_EMAIL.setInternalNamespace(False)


prop_type_PRINCIPAL_INVESTIGATOR_NAME = tr.getOrCreateNewPropertyType('PRINCIPAL_INVESTIGATOR_NAME', DataType.VARCHAR)
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setLabel('Name of Principal Investigator')
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setManagedInternally(False)
prop_type_PRINCIPAL_INVESTIGATOR_NAME.setInternalNamespace(False)


prop_type_QC_AT_DBSSE = tr.getOrCreateNewPropertyType('QC_AT_DBSSE', DataType.CONTROLLEDVOCABULARY)
prop_type_QC_AT_DBSSE.setLabel('QC at D-BSSE')
prop_type_QC_AT_DBSSE.setManagedInternally(False)
prop_type_QC_AT_DBSSE.setInternalNamespace(False)

prop_type_QC_AT_DBSSE.setVocabulary(vocabulary_YES_NO)

prop_type_QC_REQUIRED = tr.getOrCreateNewPropertyType('QC_REQUIRED', DataType.CONTROLLEDVOCABULARY)
prop_type_QC_REQUIRED.setLabel('QC required')
prop_type_QC_REQUIRED.setManagedInternally(False)
prop_type_QC_REQUIRED.setInternalNamespace(False)

prop_type_QC_REQUIRED.setVocabulary(vocabulary_YES_NO)

prop_type_RUNNINGTIME = tr.getOrCreateNewPropertyType('RUNNINGTIME', DataType.VARCHAR)
prop_type_RUNNINGTIME.setLabel('Running Time')
prop_type_RUNNINGTIME.setManagedInternally(False)
prop_type_RUNNINGTIME.setInternalNamespace(False)


prop_type_RUN_FOLDER_NAME = tr.getOrCreateNewPropertyType('RUN_FOLDER_NAME', DataType.VARCHAR)
prop_type_RUN_FOLDER_NAME.setLabel('Run Folder Name')
prop_type_RUN_FOLDER_NAME.setManagedInternally(False)
prop_type_RUN_FOLDER_NAME.setInternalNamespace(False)


prop_type_SAMPLE_KIND = tr.getOrCreateNewPropertyType('SAMPLE_KIND', DataType.CONTROLLEDVOCABULARY)
prop_type_SAMPLE_KIND.setLabel('Sample Kind')
prop_type_SAMPLE_KIND.setManagedInternally(False)
prop_type_SAMPLE_KIND.setInternalNamespace(False)

prop_type_SAMPLE_KIND.setVocabulary(vocabulary_SAMPLE_TYPE)

prop_type_SAMTOOLS_FLAGSTAT = tr.getOrCreateNewPropertyType('SAMTOOLS_FLAGSTAT', DataType.MULTILINE_VARCHAR)
prop_type_SAMTOOLS_FLAGSTAT.setLabel('Samtools Flagstat Output')
prop_type_SAMTOOLS_FLAGSTAT.setManagedInternally(False)
prop_type_SAMTOOLS_FLAGSTAT.setInternalNamespace(False)


prop_type_SBS_KIT = tr.getOrCreateNewPropertyType('SBS_KIT', DataType.VARCHAR)
prop_type_SBS_KIT.setLabel('SBS Kit')
prop_type_SBS_KIT.setManagedInternally(False)
prop_type_SBS_KIT.setInternalNamespace(False)


prop_type_SBS_SEQUENCING_KIT_VERSION = tr.getOrCreateNewPropertyType('SBS_SEQUENCING_KIT_VERSION', DataType.CONTROLLEDVOCABULARY)
prop_type_SBS_SEQUENCING_KIT_VERSION.setLabel('SBS Sequencing Kit Version')
prop_type_SBS_SEQUENCING_KIT_VERSION.setManagedInternally(False)
prop_type_SBS_SEQUENCING_KIT_VERSION.setInternalNamespace(False)

prop_type_SBS_SEQUENCING_KIT_VERSION.setVocabulary(vocabulary_SBS_SEQUENCING_KIT_VERSION)

prop_type_SEQUENCER = tr.getOrCreateNewPropertyType('SEQUENCER', DataType.CONTROLLEDVOCABULARY)
prop_type_SEQUENCER.setLabel('Sequencer')
prop_type_SEQUENCER.setManagedInternally(False)
prop_type_SEQUENCER.setInternalNamespace(False)

prop_type_SEQUENCER.setVocabulary(vocabulary_SEQUENCER)

prop_type_SEQUENCER_FINISHED = tr.getOrCreateNewPropertyType('SEQUENCER_FINISHED', DataType.TIMESTAMP)
prop_type_SEQUENCER_FINISHED.setLabel('Sequencer finished')
prop_type_SEQUENCER_FINISHED.setManagedInternally(False)
prop_type_SEQUENCER_FINISHED.setInternalNamespace(False)


prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG = tr.getOrCreateNewPropertyType('STARTING_AMOUNT_OF_SAMPLE_IN_NG', DataType.REAL)
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setLabel('Starting amount of sample (ng)')
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setManagedInternally(False)
prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setInternalNamespace(False)


prop_type_STD = tr.getOrCreateNewPropertyType('STD', DataType.REAL)
prop_type_STD.setLabel('Standard deviation')
prop_type_STD.setManagedInternally(False)
prop_type_STD.setInternalNamespace(False)


prop_type_SURFACECOUNT = tr.getOrCreateNewPropertyType('SURFACECOUNT', DataType.INTEGER)
prop_type_SURFACECOUNT.setLabel('Surface Count')
prop_type_SURFACECOUNT.setManagedInternally(False)
prop_type_SURFACECOUNT.setInternalNamespace(False)


prop_type_SWATHCOUNT = tr.getOrCreateNewPropertyType('SWATHCOUNT', DataType.INTEGER)
prop_type_SWATHCOUNT.setLabel('Swath Count')
prop_type_SWATHCOUNT.setManagedInternally(False)
prop_type_SWATHCOUNT.setInternalNamespace(False)


prop_type_TILECOUNT = tr.getOrCreateNewPropertyType('TILECOUNT', DataType.INTEGER)
prop_type_TILECOUNT.setLabel('Tile Count')
prop_type_TILECOUNT.setManagedInternally(False)
prop_type_TILECOUNT.setInternalNamespace(False)


prop_type_TOTAL_READS = tr.getOrCreateNewPropertyType('TOTAL_READS', DataType.INTEGER)
prop_type_TOTAL_READS.setLabel('Total reads')
prop_type_TOTAL_READS.setManagedInternally(False)
prop_type_TOTAL_READS.setInternalNamespace(False)


prop_type_UL_DNA = tr.getOrCreateNewPropertyType('UL_DNA', DataType.REAL)
prop_type_UL_DNA.setLabel('Calculated ul DNA for 2nM stock')
prop_type_UL_DNA.setManagedInternally(False)
prop_type_UL_DNA.setInternalNamespace(False)


prop_type_UL_EB = tr.getOrCreateNewPropertyType('UL_EB', DataType.REAL)
prop_type_UL_EB.setLabel('Calculated ul EB for 2nM stock ')
prop_type_UL_EB.setManagedInternally(False)
prop_type_UL_EB.setInternalNamespace(False)


prop_type_UL_STOCK = tr.getOrCreateNewPropertyType('UL_STOCK', DataType.INTEGER)
prop_type_UL_STOCK.setLabel('ul of 2nM stock')
prop_type_UL_STOCK.setManagedInternally(False)
prop_type_UL_STOCK.setInternalNamespace(False)


prop_type_UNIQUE_BARCODES = tr.getOrCreateNewPropertyType('UNIQUE_BARCODES', DataType.VARCHAR)
prop_type_UNIQUE_BARCODES.setLabel('Unique Barcodes in Pool?')
prop_type_UNIQUE_BARCODES.setManagedInternally(False)
prop_type_UNIQUE_BARCODES.setInternalNamespace(False)


prop_type_VERSION = tr.getOrCreateNewPropertyType('VERSION', DataType.VARCHAR)
prop_type_VERSION.setLabel('Version')
prop_type_VERSION.setManagedInternally(False)
prop_type_VERSION.setInternalNamespace(False)


prop_type_YIELD_MBASES = tr.getOrCreateNewPropertyType('YIELD_MBASES', DataType.INTEGER)
prop_type_YIELD_MBASES.setLabel('Yield(Mbases)')
prop_type_YIELD_MBASES.setManagedInternally(False)
prop_type_YIELD_MBASES.setInternalNamespace(False)


prop_type_ZOOMLEVELS = tr.getOrCreateNewPropertyType('ZOOMLEVELS', DataType.INTEGER)
prop_type_ZOOMLEVELS.setLabel('zoom Levels')
prop_type_ZOOMLEVELS.setManagedInternally(False)
prop_type_ZOOMLEVELS.setInternalNamespace(False)


print "Imported 92 Property Types" 
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_ALIGNMENT_SOFTWARE)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setSection(None)
assignment_DATA_SET_ALIGNMENT_ALIGNMENT_SOFTWARE.setPositionInForms(3)

assignment_DATA_SET_ALIGNMENT_VERSION = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_VERSION)
assignment_DATA_SET_ALIGNMENT_VERSION.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_VERSION.setSection(None)
assignment_DATA_SET_ALIGNMENT_VERSION.setPositionInForms(4)

assignment_DATA_SET_ALIGNMENT_NOTES = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_NOTES)
assignment_DATA_SET_ALIGNMENT_NOTES.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_NOTES.setSection(None)
assignment_DATA_SET_ALIGNMENT_NOTES.setPositionInForms(5)

assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_SAMTOOLS_FLAGSTAT)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setSection(None)
assignment_DATA_SET_ALIGNMENT_SAMTOOLS_FLAGSTAT.setPositionInForms(6)

assignment_DATA_SET_ALIGNMENT_MAPPED_READS = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_MAPPED_READS)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setSection(None)
assignment_DATA_SET_ALIGNMENT_MAPPED_READS.setPositionInForms(7)

assignment_DATA_SET_ALIGNMENT_TOTAL_READS = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_TOTAL_READS)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setSection(None)
assignment_DATA_SET_ALIGNMENT_TOTAL_READS.setPositionInForms(8)

assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND = tr.assignPropertyType(data_set_type_ALIGNMENT, prop_type_ISSUED_COMMAND)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setMandatory(False)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setSection(None)
assignment_DATA_SET_ALIGNMENT_ISSUED_COMMAND.setPositionInForms(9)

assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX = tr.assignPropertyType(data_set_type_BASECALL_STATS, prop_type_MISMATCH_IN_INDEX)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setMandatory(False)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setSection(None)
assignment_DATA_SET_BASECALL_STATS_MISMATCH_IN_INDEX.setPositionInForms(3)

assignment_DATA_SET_BIGWIGGLE_NOTES = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_NOTES)
assignment_DATA_SET_BIGWIGGLE_NOTES.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_NOTES.setSection(None)
assignment_DATA_SET_BIGWIGGLE_NOTES.setPositionInForms(3)

assignment_DATA_SET_BIGWIGGLE_VERSION = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_VERSION)
assignment_DATA_SET_BIGWIGGLE_VERSION.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_VERSION.setSection(None)
assignment_DATA_SET_BIGWIGGLE_VERSION.setPositionInForms(4)

assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ISCOMPRESSED)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ISCOMPRESSED.setPositionInForms(5)

assignment_DATA_SET_BIGWIGGLE_ISSWAPPED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ISSWAPPED)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ISSWAPPED.setPositionInForms(6)

assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_PRIMARYDATASIZE)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setSection(None)
assignment_DATA_SET_BIGWIGGLE_PRIMARYDATASIZE.setPositionInForms(7)

assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_PRIMARYINDEXSIZE)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setSection(None)
assignment_DATA_SET_BIGWIGGLE_PRIMARYINDEXSIZE.setPositionInForms(8)

assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_ZOOMLEVELS)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setSection(None)
assignment_DATA_SET_BIGWIGGLE_ZOOMLEVELS.setPositionInForms(9)

assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_CHROMCOUNT)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setSection(None)
assignment_DATA_SET_BIGWIGGLE_CHROMCOUNT.setPositionInForms(10)

assignment_DATA_SET_BIGWIGGLE_BASESCOVERED = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_BASESCOVERED)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setSection(None)
assignment_DATA_SET_BIGWIGGLE_BASESCOVERED.setPositionInForms(11)

assignment_DATA_SET_BIGWIGGLE_MEAN = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MEAN)
assignment_DATA_SET_BIGWIGGLE_MEAN.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MEAN.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MEAN.setPositionInForms(12)

assignment_DATA_SET_BIGWIGGLE_MIN = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MIN)
assignment_DATA_SET_BIGWIGGLE_MIN.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MIN.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MIN.setPositionInForms(13)

assignment_DATA_SET_BIGWIGGLE_MAX = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_MAX)
assignment_DATA_SET_BIGWIGGLE_MAX.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_MAX.setSection(None)
assignment_DATA_SET_BIGWIGGLE_MAX.setPositionInForms(14)

assignment_DATA_SET_BIGWIGGLE_STD = tr.assignPropertyType(data_set_type_BIGWIGGLE, prop_type_STD)
assignment_DATA_SET_BIGWIGGLE_STD.setMandatory(False)
assignment_DATA_SET_BIGWIGGLE_STD.setSection(None)
assignment_DATA_SET_BIGWIGGLE_STD.setPositionInForms(15)

assignment_DATA_SET_FASTQ_GZ_NOTES = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_NOTES)
assignment_DATA_SET_FASTQ_GZ_NOTES.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_NOTES.setSection(None)
assignment_DATA_SET_FASTQ_GZ_NOTES.setPositionInForms(3)

assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_YIELD_MBASES)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setSection(None)
assignment_DATA_SET_FASTQ_GZ_YIELD_MBASES.setPositionInForms(4)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_PASSED_FILTERING)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PASSED_FILTERING.setPositionInForms(5)

assignment_DATA_SET_FASTQ_GZ_INDEX1 = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_INDEX1)
assignment_DATA_SET_FASTQ_GZ_INDEX1.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_INDEX1.setSection(None)
assignment_DATA_SET_FASTQ_GZ_INDEX1.setPositionInForms(6)

assignment_DATA_SET_FASTQ_GZ_INDEX2 = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_INDEX2)
assignment_DATA_SET_FASTQ_GZ_INDEX2.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_INDEX2.setSection(None)
assignment_DATA_SET_FASTQ_GZ_INDEX2.setPositionInForms(7)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_RAW_CLUSTERS_PER_LANE)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_RAW_CLUSTERS_PER_LANE.setPositionInForms(8)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_PERFECT_INDEX_READS)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_PERFECT_INDEX_READS.setPositionInForms(9)

assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX = tr.assignPropertyType(data_set_type_FASTQ_GZ, prop_type_PERCENTAGE_ONE_MISMATCH_READS_INDEX)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setMandatory(False)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setSection(None)
assignment_DATA_SET_FASTQ_GZ_PERCENTAGE_ONE_MISMATCH_READS_INDEX.setPositionInForms(10)

assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN = tr.assignPropertyType(exp_type_HT_SEQUENCING, prop_type_EXPERIMENT_DESIGN)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setMandatory(False)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setSection(None)
assignment_EXPERIMENT_HT_SEQUENCING_EXPERIMENT_DESIGN.setPositionInForms(3)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUN_FOLDER_NAME = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_RUN_FOLDER_NAME)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUN_FOLDER_NAME.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUN_FOLDER_NAME.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUN_FOLDER_NAME.setPositionInForms(1)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SEQUENCER)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER.setPositionInForms(5)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_END_TYPE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_END_TYPE.setPositionInForms(7)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_FLOW_CELL_SEQUENCED_ON)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOW_CELL_SEQUENCED_ON.setPositionInForms(8)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SEQUENCER_FINISHED)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SEQUENCER_FINISHED.setPositionInForms(9)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_RUNNINGTIME)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setPositionInForms(10)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setScriptName('Diff_time')
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setDynamic(True)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setManaged(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_RUNNINGTIME.setShownEdit(False)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_ILLUMINA_PIPELINE_VERSION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ILLUMINA_PIPELINE_VERSION.setPositionInForms(11)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CYCLES)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CYCLES.setPositionInForms(14)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_INDEXREAD)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD.setPositionInForms(17)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD2 = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_INDEXREAD2)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD2.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD2.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_INDEXREAD2.setPositionInForms(18)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CONTROL_LANE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CONTROL_LANE.setPositionInForms(19)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_FLOWCELLTYPE)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_FLOWCELLTYPE.setPositionInForms(20)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_LANECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_LANECOUNT.setPositionInForms(21)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SURFACECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SURFACECOUNT.setPositionInForms(22)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SWATHCOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SWATHCOUNT.setPositionInForms(23)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_TILECOUNT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_TILECOUNT.setPositionInForms(24)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SBS_KIT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_KIT.setPositionInForms(25)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_PAIRED_END_KIT)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_PAIRED_END_KIT.setPositionInForms(26)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_STATION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CLUSTER_STATION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_STATION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_STATION.setSection('Clustering')
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_STATION.setPositionInForms(27)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CREATED_ON_CS = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CREATED_ON_CS)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CREATED_ON_CS.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CREATED_ON_CS.setSection('Clustering')
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CREATED_ON_CS.setPositionInForms(28)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CS_PROTOCOL_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CS_PROTOCOL_VERSION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CS_PROTOCOL_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CS_PROTOCOL_VERSION.setSection('Clustering')
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CS_PROTOCOL_VERSION.setPositionInForms(29)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_GENERATION_KIT_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_CLUSTER_GENERATION_KIT_VERSION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_GENERATION_KIT_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_GENERATION_KIT_VERSION.setSection('Clustering')
assignment_SAMPLE_ILLUMINA_FLOW_CELL_CLUSTER_GENERATION_KIT_VERSION.setPositionInForms(30)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_SEQUENCING_KIT_VERSION = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_SBS_SEQUENCING_KIT_VERSION)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_SEQUENCING_KIT_VERSION.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_SEQUENCING_KIT_VERSION.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_SBS_SEQUENCING_KIT_VERSION.setPositionInForms(32)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_ANALYSIS_FINISHED)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_ANALYSIS_FINISHED.setPositionInForms(34)

assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_CELL, prop_type_NOTES)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_CELL_NOTES.setPositionInForms(35)

assignment_SAMPLE_ILLUMINA_FLOW_LANE_CONCENTRATION_FLOWLANE = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_LANE, prop_type_CONCENTRATION_FLOWLANE)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_CONCENTRATION_FLOWLANE.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_CONCENTRATION_FLOWLANE.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_CONCENTRATION_FLOWLANE.setPositionInForms(20)

assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES = tr.assignPropertyType(samp_type_ILLUMINA_FLOW_LANE, prop_type_NOTES)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setMandatory(False)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setSection(None)
assignment_SAMPLE_ILLUMINA_FLOW_LANE_NOTES.setPositionInForms(23)

assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION = tr.assignPropertyType(data_set_type_ILLUMINA_HISEQ_OUTPUT, prop_type_CASAVA_VERSION)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setMandatory(False)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setSection(None)
assignment_DATA_SET_ILLUMINA_HISEQ_OUTPUT_CASAVA_VERSION.setPositionInForms(3)

assignment_SAMPLE_LIBRARY_EXTERNAL_SAMPLE_NAME = tr.assignPropertyType(samp_type_LIBRARY, prop_type_EXTERNAL_SAMPLE_NAME)
assignment_SAMPLE_LIBRARY_EXTERNAL_SAMPLE_NAME.setMandatory(False)
assignment_SAMPLE_LIBRARY_EXTERNAL_SAMPLE_NAME.setSection(None)
assignment_SAMPLE_LIBRARY_EXTERNAL_SAMPLE_NAME.setPositionInForms(1)

assignment_SAMPLE_LIBRARY_KIT = tr.assignPropertyType(samp_type_LIBRARY, prop_type_KIT)
assignment_SAMPLE_LIBRARY_KIT.setMandatory(False)
assignment_SAMPLE_LIBRARY_KIT.setSection(None)
assignment_SAMPLE_LIBRARY_KIT.setPositionInForms(2)

assignment_SAMPLE_LIBRARY_PREPARED_BY = tr.assignPropertyType(samp_type_LIBRARY, prop_type_PREPARED_BY)
assignment_SAMPLE_LIBRARY_PREPARED_BY.setMandatory(False)
assignment_SAMPLE_LIBRARY_PREPARED_BY.setSection(None)
assignment_SAMPLE_LIBRARY_PREPARED_BY.setPositionInForms(3)

assignment_SAMPLE_LIBRARY_STARTING_AMOUNT_OF_SAMPLE_IN_NG = tr.assignPropertyType(samp_type_LIBRARY, prop_type_STARTING_AMOUNT_OF_SAMPLE_IN_NG)
assignment_SAMPLE_LIBRARY_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setMandatory(False)
assignment_SAMPLE_LIBRARY_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setSection(None)
assignment_SAMPLE_LIBRARY_STARTING_AMOUNT_OF_SAMPLE_IN_NG.setPositionInForms(4)

assignment_SAMPLE_LIBRARY_INDEX1 = tr.assignPropertyType(samp_type_LIBRARY, prop_type_INDEX1)
assignment_SAMPLE_LIBRARY_INDEX1.setMandatory(False)
assignment_SAMPLE_LIBRARY_INDEX1.setSection(None)
assignment_SAMPLE_LIBRARY_INDEX1.setPositionInForms(7)

assignment_SAMPLE_LIBRARY_INDEX2 = tr.assignPropertyType(samp_type_LIBRARY, prop_type_INDEX2)
assignment_SAMPLE_LIBRARY_INDEX2.setMandatory(False)
assignment_SAMPLE_LIBRARY_INDEX2.setSection(None)
assignment_SAMPLE_LIBRARY_INDEX2.setPositionInForms(8)

assignment_SAMPLE_LIBRARY_LOT = tr.assignPropertyType(samp_type_LIBRARY, prop_type_LOT)
assignment_SAMPLE_LIBRARY_LOT.setMandatory(False)
assignment_SAMPLE_LIBRARY_LOT.setSection(None)
assignment_SAMPLE_LIBRARY_LOT.setPositionInForms(9)

assignment_SAMPLE_LIBRARY_AGILENT_KIT = tr.assignPropertyType(samp_type_LIBRARY, prop_type_AGILENT_KIT)
assignment_SAMPLE_LIBRARY_AGILENT_KIT.setMandatory(False)
assignment_SAMPLE_LIBRARY_AGILENT_KIT.setSection('QC Data')
assignment_SAMPLE_LIBRARY_AGILENT_KIT.setPositionInForms(10)

assignment_SAMPLE_LIBRARY_FRAGMENT_SIZE_PREPARED_ILLUMINA = tr.assignPropertyType(samp_type_LIBRARY, prop_type_FRAGMENT_SIZE_PREPARED_ILLUMINA)
assignment_SAMPLE_LIBRARY_FRAGMENT_SIZE_PREPARED_ILLUMINA.setMandatory(False)
assignment_SAMPLE_LIBRARY_FRAGMENT_SIZE_PREPARED_ILLUMINA.setSection('QC Data')
assignment_SAMPLE_LIBRARY_FRAGMENT_SIZE_PREPARED_ILLUMINA.setPositionInForms(11)

assignment_SAMPLE_LIBRARY_NANO_DROP = tr.assignPropertyType(samp_type_LIBRARY, prop_type_NANO_DROP)
assignment_SAMPLE_LIBRARY_NANO_DROP.setMandatory(False)
assignment_SAMPLE_LIBRARY_NANO_DROP.setSection('QC Data')
assignment_SAMPLE_LIBRARY_NANO_DROP.setPositionInForms(12)

assignment_SAMPLE_LIBRARY_CONCENTRATION_PREPARED_ILLUMINA = tr.assignPropertyType(samp_type_LIBRARY, prop_type_CONCENTRATION_PREPARED_ILLUMINA)
assignment_SAMPLE_LIBRARY_CONCENTRATION_PREPARED_ILLUMINA.setMandatory(False)
assignment_SAMPLE_LIBRARY_CONCENTRATION_PREPARED_ILLUMINA.setSection('QC Data')
assignment_SAMPLE_LIBRARY_CONCENTRATION_PREPARED_ILLUMINA.setPositionInForms(13)

assignment_SAMPLE_LIBRARY_DNA_CONCENTRATION_OF_LIBRARY = tr.assignPropertyType(samp_type_LIBRARY, prop_type_DNA_CONCENTRATION_OF_LIBRARY)
assignment_SAMPLE_LIBRARY_DNA_CONCENTRATION_OF_LIBRARY.setMandatory(False)
assignment_SAMPLE_LIBRARY_DNA_CONCENTRATION_OF_LIBRARY.setSection('QC Data')
assignment_SAMPLE_LIBRARY_DNA_CONCENTRATION_OF_LIBRARY.setPositionInForms(14)

assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_SUCCESSFUL = tr.assignPropertyType(samp_type_LIBRARY, prop_type_LIBRARY_PROCESSING_SUCCESSFUL)
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_SUCCESSFUL.setMandatory(False)
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_SUCCESSFUL.setSection('QC Data')
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_SUCCESSFUL.setPositionInForms(15)

assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_FAILED = tr.assignPropertyType(samp_type_LIBRARY, prop_type_LIBRARY_PROCESSING_FAILED)
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_FAILED.setMandatory(False)
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_FAILED.setSection('QC Data')
assignment_SAMPLE_LIBRARY_LIBRARY_PROCESSING_FAILED.setPositionInForms(16)

assignment_SAMPLE_LIBRARY_QC_AT_DBSSE = tr.assignPropertyType(samp_type_LIBRARY, prop_type_QC_AT_DBSSE)
assignment_SAMPLE_LIBRARY_QC_AT_DBSSE.setMandatory(False)
assignment_SAMPLE_LIBRARY_QC_AT_DBSSE.setSection('QC Data')
assignment_SAMPLE_LIBRARY_QC_AT_DBSSE.setPositionInForms(17)

assignment_SAMPLE_LIBRARY_KIT_PREPARED = tr.assignPropertyType(samp_type_LIBRARY, prop_type_KIT_PREPARED)
assignment_SAMPLE_LIBRARY_KIT_PREPARED.setMandatory(False)
assignment_SAMPLE_LIBRARY_KIT_PREPARED.setSection(None)
assignment_SAMPLE_LIBRARY_KIT_PREPARED.setPositionInForms(18)

assignment_SAMPLE_LIBRARY_DATA_TRANSFERRED = tr.assignPropertyType(samp_type_LIBRARY, prop_type_DATA_TRANSFERRED)
assignment_SAMPLE_LIBRARY_DATA_TRANSFERRED.setMandatory(False)
assignment_SAMPLE_LIBRARY_DATA_TRANSFERRED.setSection(None)
assignment_SAMPLE_LIBRARY_DATA_TRANSFERRED.setPositionInForms(19)

assignment_SAMPLE_LIBRARY_INVOICE = tr.assignPropertyType(samp_type_LIBRARY, prop_type_INVOICE)
assignment_SAMPLE_LIBRARY_INVOICE.setMandatory(False)
assignment_SAMPLE_LIBRARY_INVOICE.setSection(None)
assignment_SAMPLE_LIBRARY_INVOICE.setPositionInForms(20)

assignment_SAMPLE_LIBRARY_NOTES = tr.assignPropertyType(samp_type_LIBRARY, prop_type_NOTES)
assignment_SAMPLE_LIBRARY_NOTES.setMandatory(False)
assignment_SAMPLE_LIBRARY_NOTES.setSection(None)
assignment_SAMPLE_LIBRARY_NOTES.setPositionInForms(33)

assignment_SAMPLE_LIBRARY_POOL_EXTERNAL_SAMPLE_NAME = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_EXTERNAL_SAMPLE_NAME)
assignment_SAMPLE_LIBRARY_POOL_EXTERNAL_SAMPLE_NAME.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_EXTERNAL_SAMPLE_NAME.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_EXTERNAL_SAMPLE_NAME.setPositionInForms(1)

assignment_SAMPLE_LIBRARY_POOL_PREPARED_BY = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_PREPARED_BY)
assignment_SAMPLE_LIBRARY_POOL_PREPARED_BY.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_PREPARED_BY.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_PREPARED_BY.setPositionInForms(2)

assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_POOL = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_DNA_CONCENTRATION_POOL)
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_POOL.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_POOL.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_POOL.setPositionInForms(3)

assignment_SAMPLE_LIBRARY_POOL_QC_REQUIRED = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_QC_REQUIRED)
assignment_SAMPLE_LIBRARY_POOL_QC_REQUIRED.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_QC_REQUIRED.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_QC_REQUIRED.setPositionInForms(4)

assignment_SAMPLE_LIBRARY_POOL_AGILENT_KIT = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_AGILENT_KIT)
assignment_SAMPLE_LIBRARY_POOL_AGILENT_KIT.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_AGILENT_KIT.setSection('QC Data')
assignment_SAMPLE_LIBRARY_POOL_AGILENT_KIT.setPositionInForms(5)

assignment_SAMPLE_LIBRARY_POOL_FRAGMENT_SIZE_BASE_PAIRS = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_FRAGMENT_SIZE_BASE_PAIRS)
assignment_SAMPLE_LIBRARY_POOL_FRAGMENT_SIZE_BASE_PAIRS.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_FRAGMENT_SIZE_BASE_PAIRS.setSection('QC Data')
assignment_SAMPLE_LIBRARY_POOL_FRAGMENT_SIZE_BASE_PAIRS.setPositionInForms(6)

assignment_SAMPLE_LIBRARY_POOL_NANO_DROP = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_NANO_DROP)
assignment_SAMPLE_LIBRARY_POOL_NANO_DROP.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_NANO_DROP.setSection('QC Data')
assignment_SAMPLE_LIBRARY_POOL_NANO_DROP.setPositionInForms(7)

assignment_SAMPLE_LIBRARY_POOL_CONCENTRATION = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_CONCENTRATION)
assignment_SAMPLE_LIBRARY_POOL_CONCENTRATION.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_CONCENTRATION.setSection('QC Data')
assignment_SAMPLE_LIBRARY_POOL_CONCENTRATION.setPositionInForms(8)

assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_QGF = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_DNA_CONCENTRATION_QGF)
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_QGF.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_QGF.setSection('QC Data')
assignment_SAMPLE_LIBRARY_POOL_DNA_CONCENTRATION_QGF.setPositionInForms(9)

assignment_SAMPLE_LIBRARY_POOL_INVOICE = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_INVOICE)
assignment_SAMPLE_LIBRARY_POOL_INVOICE.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_INVOICE.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_INVOICE.setPositionInForms(10)

assignment_SAMPLE_LIBRARY_POOL_NOTES = tr.assignPropertyType(samp_type_LIBRARY_POOL, prop_type_NOTES)
assignment_SAMPLE_LIBRARY_POOL_NOTES.setMandatory(False)
assignment_SAMPLE_LIBRARY_POOL_NOTES.setSection(None)
assignment_SAMPLE_LIBRARY_POOL_NOTES.setPositionInForms(11)

assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION = tr.assignPropertyType(data_set_type_MACS_OUTPUT, prop_type_MACS_VERSION)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setMandatory(False)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setSection(None)
assignment_DATA_SET_MACS_OUTPUT_MACS_VERSION.setPositionInForms(3)

assignment_DATA_SET_MACS_OUTPUT_NOTES = tr.assignPropertyType(data_set_type_MACS_OUTPUT, prop_type_NOTES)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setMandatory(False)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setSection(None)
assignment_DATA_SET_MACS_OUTPUT_NOTES.setPositionInForms(4)

assignment_SAMPLE_MASTER_SAMPLE_BIOLOGICAL_SAMPLE_ARRIVED = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_BIOLOGICAL_SAMPLE_ARRIVED)
assignment_SAMPLE_MASTER_SAMPLE_BIOLOGICAL_SAMPLE_ARRIVED.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_BIOLOGICAL_SAMPLE_ARRIVED.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_BIOLOGICAL_SAMPLE_ARRIVED.setPositionInForms(1)

assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_EMAIL = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_CONTACT_PERSON_EMAIL)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_EMAIL.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_EMAIL.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_EMAIL.setPositionInForms(2)

assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_NAME = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_CONTACT_PERSON_NAME)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_NAME.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_NAME.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_CONTACT_PERSON_NAME.setPositionInForms(3)

assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_EMAIL = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_PRINCIPAL_INVESTIGATOR_EMAIL)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_EMAIL.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_EMAIL.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_EMAIL.setPositionInForms(4)

assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_NAME = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_PRINCIPAL_INVESTIGATOR_NAME)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_NAME.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_NAME.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_PRINCIPAL_INVESTIGATOR_NAME.setPositionInForms(5)

assignment_SAMPLE_MASTER_SAMPLE_NCBI_ORGANISM_TAXONOMY = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_NCBI_ORGANISM_TAXONOMY)
assignment_SAMPLE_MASTER_SAMPLE_NCBI_ORGANISM_TAXONOMY.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_NCBI_ORGANISM_TAXONOMY.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_NCBI_ORGANISM_TAXONOMY.setPositionInForms(7)

assignment_SAMPLE_MASTER_SAMPLE_SAMPLE_KIND = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_SAMPLE_KIND)
assignment_SAMPLE_MASTER_SAMPLE_SAMPLE_KIND.setMandatory(True)
assignment_SAMPLE_MASTER_SAMPLE_SAMPLE_KIND.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_SAMPLE_KIND.setPositionInForms(21)

assignment_SAMPLE_MASTER_SAMPLE_NOTES = tr.assignPropertyType(samp_type_MASTER_SAMPLE, prop_type_NOTES)
assignment_SAMPLE_MASTER_SAMPLE_NOTES.setMandatory(False)
assignment_SAMPLE_MASTER_SAMPLE_NOTES.setSection(None)
assignment_SAMPLE_MASTER_SAMPLE_NOTES.setPositionInForms(22)

assignment_SAMPLE_RAW_SAMPLE_EXTERNAL_SAMPLE_NAME = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_EXTERNAL_SAMPLE_NAME)
assignment_SAMPLE_RAW_SAMPLE_EXTERNAL_SAMPLE_NAME.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_EXTERNAL_SAMPLE_NAME.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_EXTERNAL_SAMPLE_NAME.setPositionInForms(1)

assignment_SAMPLE_RAW_SAMPLE_CONCENTRATION_ORIGINAL_ILLUMINA = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_CONCENTRATION_ORIGINAL_ILLUMINA)
assignment_SAMPLE_RAW_SAMPLE_CONCENTRATION_ORIGINAL_ILLUMINA.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_CONCENTRATION_ORIGINAL_ILLUMINA.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_CONCENTRATION_ORIGINAL_ILLUMINA.setPositionInForms(4)

assignment_SAMPLE_RAW_SAMPLE_PREPARED_BY = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_PREPARED_BY)
assignment_SAMPLE_RAW_SAMPLE_PREPARED_BY.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_PREPARED_BY.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_PREPARED_BY.setPositionInForms(8)

assignment_SAMPLE_RAW_SAMPLE_KIT_PREPARED = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_KIT_PREPARED)
assignment_SAMPLE_RAW_SAMPLE_KIT_PREPARED.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_KIT_PREPARED.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_KIT_PREPARED.setPositionInForms(9)

assignment_SAMPLE_RAW_SAMPLE_LIBRARY_PROCESSING_POSSIBLE_YES_NO = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_LIBRARY_PROCESSING_POSSIBLE_YES_NO)
assignment_SAMPLE_RAW_SAMPLE_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_LIBRARY_PROCESSING_POSSIBLE_YES_NO.setPositionInForms(10)

assignment_SAMPLE_RAW_SAMPLE_NOTES = tr.assignPropertyType(samp_type_RAW_SAMPLE, prop_type_NOTES)
assignment_SAMPLE_RAW_SAMPLE_NOTES.setMandatory(False)
assignment_SAMPLE_RAW_SAMPLE_NOTES.setSection(None)
assignment_SAMPLE_RAW_SAMPLE_NOTES.setPositionInForms(13)

print "Imported 104 Property Assignments" 
print "Imported 0 External DMSs" 
print ("Import of Master Data finished.") 