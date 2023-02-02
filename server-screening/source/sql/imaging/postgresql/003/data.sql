-- Note that these data has to be loaded manually into the openBIS Core database.
-- TODO: this sql should be converted to a generic sql without refering to technical ids. 
-- Now the ids should be updated if some new records will be added in the generic data.

--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

--
-- Data for Name: controlled_vocabularies; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabularies (id, code, description, registration_timestamp, pers_id_registerer, is_managed_internally, is_internal_namespace, dbin_id, modification_timestamp, is_chosen_from_list, source_uri) FROM stdin;
2	PLATE_GEOMETRY	The geometry or dimensions of a plate	2008-06-17 16:38:30.723292+02	1	t	t	1	2009-11-27 16:02:26.451046+01	t	\N
3	MICROSCOPE	Microscope used in an experiment.	2009-11-29 23:55:18.978884+01	1	f	f	1	2009-12-17 01:50:54.68+01	t	\N
\.


--
-- Data for Name: controlled_vocabulary_terms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY controlled_vocabulary_terms (id, code, registration_timestamp, covo_id, pers_id_registerer, label, description, ordinal) FROM stdin;
3	384_WELLS_16X24	2008-06-17 16:38:30.723292+02	2	1	384 Wells, 16x24	\N	1
4	96_WELLS_8X12	2008-06-17 16:38:31.101031+02	2	1	96 Wells, 8x12	\N	2
5	1536_WELLS_32X48	2008-06-17 16:38:31.101031+02	2	1	1536 Wells, 32x48	\N	3
6	BD_PATHWAY_855	2009-11-29 23:55:18.978884+01	3	1	\N	\N	1
7	MD_IMAGEXPRESS_MICROLIVE	2009-11-29 23:55:18.978884+01	3	1	\N	\N	2
8	MD_IMAGEXPRESS_MICRO_2	2009-11-29 23:55:18.978884+01	3	1	\N	\N	3
\.


--
-- Data for Name: data_set_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
3	HCS_IMAGE_ANALYSIS_DATA	Data derived from analysis of HCS images	1	2009-11-27 16:02:26.451046+01
2	HCS_IMAGE	HCS raw images acquired by microscopes	1	2009-11-27 16:02:26.451046+01
4	HCS_ANALYSIS_PER_GENE	Image analysis data aggregated per gene.	1	2009-12-16 16:59:50.743029+01
\.


--
-- Data for Name: material_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
1	VIRUS	Virus	1	2009-11-27 16:02:26.451046+01
2	CELL_LINE	Cell Line or Cell Culture. The growing of cells under controlled conditions.	1	2009-11-27 16:02:26.451046+01
3	GENE	Gene	1	2009-11-27 16:02:26.451046+01
4	OLIGO	Oligo nucleotide	1	2009-11-27 16:02:26.451046+01
5	CONTROL	Control of a control layout	1	2009-11-27 16:02:26.451046+01
6	BACTERIUM	Bacterium	1	2009-11-27 16:02:26.451046+01
7	COMPOUND	Compound	1	2009-11-27 16:02:26.451046+01
\.


--
-- Data for Name: property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY property_types (id, code, description, label, daty_id, registration_timestamp, pers_id_registerer, covo_id, is_managed_internally, is_internal_namespace, dbin_id, maty_prop_id) FROM stdin;
3	NUCLEOTIDE_SEQUENCE	A sequence of nucleotides	Nucleotide Sequence	1	2008-02-28 13:03:03.358532+01	1	\N	f	f	1	\N
4	REFSEQ	NCBI Reference Sequence code, applicable to sequences of type: DNA, RNA, protein	RefSeq	1	2008-02-28 13:03:03.358532+01	1	\N	f	f	1	\N
13	MICROSCOPE	 	Microscope	7	2009-11-29 23:57:05.85618+01	1	3	f	f	1	\N
7	INHIBITOR_OF	Inhibitor Of	Inhibitor Of	8	2009-11-27 16:02:45.060699+01	1	\N	f	f	1	3
11	OLIGO	Oligo	Oligo	8	2009-11-29 23:56:19.39967+01	1	\N	f	f	1	4
12	CONTROL	Control	Control	8	2009-11-29 23:56:37.355313+01	1	\N	f	f	1	5
6	PLATE_GEOMETRY	Plate Geometry	Plate Geometry	7	2008-06-17 16:38:30.723292+02	1	2	t	t	1	\N
15	NUMBER_OF_CHANNEL	 	Channels	3	2009-12-17 10:56:17.239319+01	1	\N	f	f	1	\N
16	LIBRARY_ID	Library id	Id from the master plate library	1	2008-02-28 13:03:03.358532+01	1	\N	f	f	1	\N
\.


--
-- Data for Name: data_set_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_type_property_types (id, dsty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, section, ordinal) FROM stdin;
\.


--
-- Data for Name: experiment_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_types (id, code, description, dbin_id, modification_timestamp) FROM stdin;
2	COMPOUND_HCS	Compound High Content Screening	1	2009-11-27 16:02:26.451046+01
1	SIRNA_HCS	Small Interfering RNA High Content Screening	1	2009-11-27 16:02:26.451046+01
\.


--
-- Data for Name: experiment_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY experiment_type_property_types (id, exty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, section, ordinal) FROM stdin;
1	1	1	t	t	1	2008-06-17 16:38:49.023295+02	\N	1
2	2	1	t	t	1	2008-06-17 16:38:49.301922+02	\N	1
5	1	13	f	f	1	2009-12-17 10:54:01.261178+01	\N	2
\.


--
-- Data for Name: file_format_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY file_format_types (id, code, description, dbin_id) FROM stdin;
7	PNG	\N	1
8	CSV	files with values separated by comma or semicolon	1
9	JPG	\N	1
\.


--
-- Data for Name: filters; Type: TABLE DATA; Schema: public; Owner: -
--

COPY filters (id, dbin_id, name, description, registration_timestamp, pers_id_registerer, modification_timestamp, expression, is_public, grid_id) FROM stdin;
9	1	Infection index	\N	2009-12-16 21:19:19.012657+01	1	2009-12-16 22:41:56.863+01	float(row.col('colIndex_5')) > float(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
8	1	Total number of cells	\N	2009-12-16 21:18:05.161964+01	1	2009-12-16 22:42:31.707+01	int(row.col('colIndex_3')) > int(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
7	1	Number of infected cells	\N	2009-12-16 21:17:40.765083+01	1	2009-12-16 22:42:46.052+01	int(row.col('colIndex_4')) > int(${threshold})	t	data-set-reporting-gridplate-image-analysis-merger
\.


--
-- Data for Name: material_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY material_type_property_types (id, maty_id, prty_id, is_mandatory, registration_timestamp, pers_id_registerer, is_managed_internally, section, ordinal) FROM stdin;
10	4	7	t	2009-11-27 16:02:45.060699+01	1	f	\N	4
9	5	1	f	2008-02-28 13:03:03.358532+01	1	f	\N	1
8	4	1	f	2008-02-28 13:03:03.358532+01	1	f	\N	3
6	4	3	t	2008-02-28 13:03:03.358532+01	1	f	\N	1
12	4	16	f	2008-02-28 13:03:03.358532+01	1	f	\N	1
5	3	1	f	2008-02-28 13:03:03.358532+01	1	f	\N	2
11	3	16	f	2008-02-28 13:03:03.358532+01	1	f	\N	3
3	7	1	t	2008-02-28 13:03:03.358532+01	1	f	\N	1
2	6	1	t	2008-02-28 13:03:03.358532+01	1	f	\N	1
1	1	1	t	2008-02-28 13:03:03.358532+01	1	f	\N	1
\.


--
-- Data for Name: sample_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_types (id, code, description, dbin_id, is_listable, generated_from_depth, part_of_depth, modification_timestamp, is_auto_generated_code, generated_code_prefix) FROM stdin;
3	PLATE	Cell Plate	1	t	2	0	2009-11-27 16:02:26.451046+01	f	S
7	OLIGO_WELL	\N	1	f	0	1	2009-11-27 19:42:03.483115+01	f	O
8	CONTROL_WELL	\N	1	f	0	1	2009-11-27 19:42:25.791288+01	f	C
9	LIBRARY	\N	1	f	0	0	2009-11-27 19:42:25.791288+01	f	L
\.


--
-- Data for Name: sample_type_property_types; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sample_type_property_types (id, saty_id, prty_id, is_mandatory, is_managed_internally, pers_id_registerer, registration_timestamp, is_displayed, section, ordinal) FROM stdin;
8	7	11	f	f	1	2009-11-29 23:57:38.268212+01	t	\N	1
9	8	12	f	f	1	2009-11-29 23:57:49.098187+01	t	\N	1
10	3	6	f	f	1	2009-11-30 01:28:20.972263+01	t	\N	1
\.


select setval('controlled_vocabulary_id_seq', 100);
select setval('cvte_id_seq', 100);
select setval('property_type_id_seq', 100);
select setval('file_format_type_id_seq', 100);
select setval('filter_id_seq', 100);
select setval('experiment_type_id_seq', 100);
select setval('sample_type_id_seq', 100);
select setval('data_set_type_id_seq', 100);
select setval('material_type_id_seq', 100);
select setval('etpt_id_seq', 100);
select setval('stpt_id_seq', 100);
select setval('mtpt_id_seq', 100);

--
-- PostgreSQL database dump complete
--

