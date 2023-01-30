--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.0
-- Dumped by pg_dump version 9.5.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: boolean_char; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN boolean_char AS boolean DEFAULT false;


--
-- Name: code; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN code AS character varying(60);


--
-- Name: file_path; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN file_path AS character varying(1000);


--
-- Name: tech_id; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN tech_id AS bigint;


--
-- Name: time_stamp; Type: DOMAIN; Schema: public; Owner: -
--

CREATE DOMAIN time_stamp AS timestamp with time zone;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: data_set_files; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE data_set_files (
    id bigint NOT NULL,
    dase_id tech_id NOT NULL,
    parent_id tech_id,
    relative_path file_path NOT NULL,
    file_name file_path NOT NULL,
    size_in_bytes bigint NOT NULL,
    checksum_crc32 integer,
    is_directory boolean_char NOT NULL,
    last_modified time_stamp DEFAULT now() NOT NULL
);


--
-- Name: data_set_files_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_set_files_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_set_files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE data_set_files_id_seq OWNED BY data_set_files.id;


--
-- Name: data_sets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE data_sets (
    id bigint NOT NULL,
    code code NOT NULL,
    location file_path NOT NULL
);


--
-- Name: data_sets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE data_sets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: data_sets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE data_sets_id_seq OWNED BY data_sets.id;


--
-- Name: database_version_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE database_version_logs (
    db_version character varying(4) NOT NULL,
    module_name character varying(250),
    run_status character varying(10),
    run_status_timestamp timestamp without time zone,
    module_code bytea,
    run_exception bytea
);


--
-- Name: events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE events (
    last_seen_deletion_event_id tech_id NOT NULL
);


--
-- Name: last_feeding_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE last_feeding_event (
    registration_timestamp time_stamp NOT NULL
);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_files ALTER COLUMN id SET DEFAULT nextval('data_set_files_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_sets ALTER COLUMN id SET DEFAULT nextval('data_sets_id_seq'::regclass);


--
-- Data for Name: data_set_files; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_set_files (id, dase_id, parent_id, relative_path, file_name, size_in_bytes, checksum_crc32, is_directory, last_modified) FROM stdin;
1	1	\N		20130415093804724-403	0	\N	t	2013-04-15 09:38:06+02
2	1	1	original	original	0	\N	t	2013-04-15 09:38:05+02
3	1	2	original/emptyFile	emptyFile	0	0	f	2013-04-15 09:37:48+02
4	2	\N		20130415100158230-407	0	\N	t	2013-04-15 10:01:59+02
5	2	4	original	original	0	\N	t	2013-04-15 10:01:58+02
6	2	5	original/emptyFile	emptyFile	0	0	f	2013-04-15 10:01:46+02
7	3	\N		20130415100238098-408	0	\N	t	2013-04-15 10:02:39+02
8	3	7	original	original	0	\N	t	2013-04-15 10:02:38+02
9	3	8	original/emptyFile	emptyFile	0	0	f	2013-04-15 10:02:25+02
10	4	\N		20130415100308111-409	0	\N	t	2013-04-15 10:03:09+02
11	4	10	original	original	0	\N	t	2013-04-15 10:03:08+02
12	4	11	original/emptyFile	emptyFile	0	0	f	2013-04-15 10:02:53+02
78	7	\N		20130417094936021-428	500566	\N	t	2013-04-17 09:49:43+02
79	7	78	original	original	500566	\N	t	2013-04-17 09:49:37+02
80	7	79	original/SERIES-1	SERIES-1	500566	\N	t	2013-04-12 15:43:22+02
81	7	80	original/SERIES-1/bPLATE_w_s1_z0_t0_cRGB.png	bPLATE_w_s1_z0_t0_cRGB.png	16842	-377649888	f	2013-04-12 15:43:21+02
82	7	80	original/SERIES-1/bPLATE_w_s1_z0_t1_cRGB.png	bPLATE_w_s1_z0_t1_cRGB.png	15745	-350472329	f	2013-04-12 15:43:21+02
83	7	80	original/SERIES-1/bPLATE_w_s1_z0_t2_cRGB.png	bPLATE_w_s1_z0_t2_cRGB.png	16572	-1535301365	f	2013-04-12 15:43:21+02
84	7	80	original/SERIES-1/bPLATE_w_s1_z0_t3_cRGB.png	bPLATE_w_s1_z0_t3_cRGB.png	16895	284878461	f	2013-04-12 15:43:22+02
85	7	80	original/SERIES-1/bPLATE_w_s1_z0_t4_cRGB.png	bPLATE_w_s1_z0_t4_cRGB.png	16130	531362498	f	2013-04-12 15:43:22+02
86	7	80	original/SERIES-1/bPLATE_w_s1_z0_t5_cRGB.png	bPLATE_w_s1_z0_t5_cRGB.png	16696	1092208703	f	2013-04-12 15:43:22+02
87	7	80	original/SERIES-1/bPLATE_w_s1_z0_t6_cRGB.png	bPLATE_w_s1_z0_t6_cRGB.png	17103	749130407	f	2013-04-12 15:43:22+02
88	7	80	original/SERIES-1/bPLATE_w_s1_z0_t7_cRGB.png	bPLATE_w_s1_z0_t7_cRGB.png	16253	1701746373	f	2013-04-12 15:43:22+02
89	7	80	original/SERIES-1/bPLATE_w_s1_z0_t8_cRGB.png	bPLATE_w_s1_z0_t8_cRGB.png	16940	-578269368	f	2013-04-12 15:43:22+02
90	7	80	original/SERIES-1/bPLATE_w_s1_z0_t9_cRGB.png	bPLATE_w_s1_z0_t9_cRGB.png	16912	90736323	f	2013-04-12 15:43:22+02
91	7	80	original/SERIES-1/bPLATE_w_s1_z3_t0_cRGB.png	bPLATE_w_s1_z3_t0_cRGB.png	16832	1035037583	f	2013-04-12 15:43:21+02
92	7	80	original/SERIES-1/bPLATE_w_s1_z3_t1_cRGB.png	bPLATE_w_s1_z3_t1_cRGB.png	15700	599152194	f	2013-04-12 15:43:21+02
93	7	80	original/SERIES-1/bPLATE_w_s1_z3_t2_cRGB.png	bPLATE_w_s1_z3_t2_cRGB.png	16657	-357270045	f	2013-04-12 15:43:21+02
94	7	80	original/SERIES-1/bPLATE_w_s1_z3_t3_cRGB.png	bPLATE_w_s1_z3_t3_cRGB.png	16962	1019908662	f	2013-04-12 15:43:22+02
95	7	80	original/SERIES-1/bPLATE_w_s1_z3_t4_cRGB.png	bPLATE_w_s1_z3_t4_cRGB.png	16107	1412385612	f	2013-04-12 15:43:22+02
96	7	80	original/SERIES-1/bPLATE_w_s1_z3_t5_cRGB.png	bPLATE_w_s1_z3_t5_cRGB.png	16807	1239258284	f	2013-04-12 15:43:22+02
97	7	80	original/SERIES-1/bPLATE_w_s1_z3_t6_cRGB.png	bPLATE_w_s1_z3_t6_cRGB.png	17200	-60900712	f	2013-04-12 15:43:22+02
98	7	80	original/SERIES-1/bPLATE_w_s1_z3_t7_cRGB.png	bPLATE_w_s1_z3_t7_cRGB.png	16280	213799880	f	2013-04-12 15:43:22+02
99	7	80	original/SERIES-1/bPLATE_w_s1_z3_t8_cRGB.png	bPLATE_w_s1_z3_t8_cRGB.png	17147	-1555737311	f	2013-04-12 15:43:22+02
100	7	80	original/SERIES-1/bPLATE_w_s1_z3_t9_cRGB.png	bPLATE_w_s1_z3_t9_cRGB.png	17051	1300193223	f	2013-04-12 15:43:22+02
101	7	80	original/SERIES-1/bPLATE_w_s1_z6_t0_cRGB.png	bPLATE_w_s1_z6_t0_cRGB.png	16976	-112188799	f	2013-04-12 15:43:21+02
102	7	80	original/SERIES-1/bPLATE_w_s1_z6_t1_cRGB.png	bPLATE_w_s1_z6_t1_cRGB.png	15970	1678263345	f	2013-04-12 15:43:21+02
103	7	80	original/SERIES-1/bPLATE_w_s1_z6_t2_cRGB.png	bPLATE_w_s1_z6_t2_cRGB.png	16825	-160511318	f	2013-04-12 15:43:22+02
104	7	80	original/SERIES-1/bPLATE_w_s1_z6_t3_cRGB.png	bPLATE_w_s1_z6_t3_cRGB.png	17279	1989273511	f	2013-04-12 15:43:22+02
105	7	80	original/SERIES-1/bPLATE_w_s1_z6_t4_cRGB.png	bPLATE_w_s1_z6_t4_cRGB.png	16332	-1689381472	f	2013-04-12 15:43:22+02
106	7	80	original/SERIES-1/bPLATE_w_s1_z6_t5_cRGB.png	bPLATE_w_s1_z6_t5_cRGB.png	17010	125352385	f	2013-04-12 15:43:22+02
107	7	80	original/SERIES-1/bPLATE_w_s1_z6_t6_cRGB.png	bPLATE_w_s1_z6_t6_cRGB.png	16068	-1823748446	f	2013-04-12 15:43:22+02
108	7	80	original/SERIES-1/bPLATE_w_s1_z6_t7_cRGB.png	bPLATE_w_s1_z6_t7_cRGB.png	16472	664186482	f	2013-04-12 15:43:22+02
109	7	80	original/SERIES-1/bPLATE_w_s1_z6_t8_cRGB.png	bPLATE_w_s1_z6_t8_cRGB.png	17424	441007330	f	2013-04-12 15:43:22+02
110	7	80	original/SERIES-1/bPLATE_w_s1_z6_t9_cRGB.png	bPLATE_w_s1_z6_t9_cRGB.png	17379	259679938	f	2013-04-12 15:43:22+02
111	8	\N		20130417094934693-427	94433	\N	t	2013-04-17 09:49:43+02
112	8	111	thumbnails_128x128.h5ar	thumbnails_128x128.h5ar	94433	\N	t	2013-04-17 09:49:36+02
113	8	112	thumbnails_128x128.h5ar/d1-1_t0.0_h0.0_cRGB.png	d1-1_t0.0_h0.0_cRGB.png	3071	545079273	f	2013-04-17 09:49:35+02
114	8	112	thumbnails_128x128.h5ar/d1-1_t0.0_h3.0_cRGB.png	d1-1_t0.0_h3.0_cRGB.png	3170	-767496005	f	2013-04-17 09:49:35+02
115	8	112	thumbnails_128x128.h5ar/d1-1_t0.0_h6.0_cRGB.png	d1-1_t0.0_h6.0_cRGB.png	3163	-1684211790	f	2013-04-17 09:49:35+02
116	8	112	thumbnails_128x128.h5ar/d1-1_t1.0_h0.0_cRGB.png	d1-1_t1.0_h0.0_cRGB.png	2964	1288624720	f	2013-04-17 09:49:35+02
117	8	112	thumbnails_128x128.h5ar/d1-1_t1.0_h3.0_cRGB.png	d1-1_t1.0_h3.0_cRGB.png	3060	-288476992	f	2013-04-17 09:49:35+02
118	8	112	thumbnails_128x128.h5ar/d1-1_t1.0_h6.0_cRGB.png	d1-1_t1.0_h6.0_cRGB.png	3107	-1656486688	f	2013-04-17 09:49:35+02
119	8	112	thumbnails_128x128.h5ar/d1-1_t2.0_h0.0_cRGB.png	d1-1_t2.0_h0.0_cRGB.png	3072	1352683667	f	2013-04-17 09:49:35+02
120	8	112	thumbnails_128x128.h5ar/d1-1_t2.0_h3.0_cRGB.png	d1-1_t2.0_h3.0_cRGB.png	3156	-1262097506	f	2013-04-17 09:49:35+02
121	8	112	thumbnails_128x128.h5ar/d1-1_t2.0_h6.0_cRGB.png	d1-1_t2.0_h6.0_cRGB.png	3199	-1185807041	f	2013-04-17 09:49:35+02
122	8	112	thumbnails_128x128.h5ar/d1-1_t3.0_h0.0_cRGB.png	d1-1_t3.0_h0.0_cRGB.png	3175	871732577	f	2013-04-17 09:49:35+02
123	8	112	thumbnails_128x128.h5ar/d1-1_t3.0_h3.0_cRGB.png	d1-1_t3.0_h3.0_cRGB.png	3210	-881202459	f	2013-04-17 09:49:35+02
124	8	112	thumbnails_128x128.h5ar/d1-1_t3.0_h6.0_cRGB.png	d1-1_t3.0_h6.0_cRGB.png	3180	972454695	f	2013-04-17 09:49:35+02
125	8	112	thumbnails_128x128.h5ar/d1-1_t4.0_h0.0_cRGB.png	d1-1_t4.0_h0.0_cRGB.png	3016	51400674	f	2013-04-17 09:49:35+02
126	8	112	thumbnails_128x128.h5ar/d1-1_t4.0_h3.0_cRGB.png	d1-1_t4.0_h3.0_cRGB.png	3121	1348166183	f	2013-04-17 09:49:35+02
127	8	112	thumbnails_128x128.h5ar/d1-1_t4.0_h6.0_cRGB.png	d1-1_t4.0_h6.0_cRGB.png	3044	2075959991	f	2013-04-17 09:49:35+02
128	8	112	thumbnails_128x128.h5ar/d1-1_t5.0_h0.0_cRGB.png	d1-1_t5.0_h0.0_cRGB.png	3252	1924897907	f	2013-04-17 09:49:35+02
129	8	112	thumbnails_128x128.h5ar/d1-1_t5.0_h3.0_cRGB.png	d1-1_t5.0_h3.0_cRGB.png	3255	-1200609858	f	2013-04-17 09:49:35+02
130	8	112	thumbnails_128x128.h5ar/d1-1_t5.0_h6.0_cRGB.png	d1-1_t5.0_h6.0_cRGB.png	3241	-1341970646	f	2013-04-17 09:49:35+02
131	8	112	thumbnails_128x128.h5ar/d1-1_t6.0_h0.0_cRGB.png	d1-1_t6.0_h0.0_cRGB.png	3199	687835588	f	2013-04-17 09:49:35+02
132	8	112	thumbnails_128x128.h5ar/d1-1_t6.0_h3.0_cRGB.png	d1-1_t6.0_h3.0_cRGB.png	3219	616235488	f	2013-04-17 09:49:35+02
133	8	112	thumbnails_128x128.h5ar/d1-1_t6.0_h6.0_cRGB.png	d1-1_t6.0_h6.0_cRGB.png	3064	-1287298243	f	2013-04-17 09:49:35+02
134	8	112	thumbnails_128x128.h5ar/d1-1_t7.0_h0.0_cRGB.png	d1-1_t7.0_h0.0_cRGB.png	3132	1145773353	f	2013-04-17 09:49:35+02
135	8	112	thumbnails_128x128.h5ar/d1-1_t7.0_h3.0_cRGB.png	d1-1_t7.0_h3.0_cRGB.png	3129	924566430	f	2013-04-17 09:49:35+02
136	8	112	thumbnails_128x128.h5ar/d1-1_t7.0_h6.0_cRGB.png	d1-1_t7.0_h6.0_cRGB.png	3163	1007699882	f	2013-04-17 09:49:35+02
137	8	112	thumbnails_128x128.h5ar/d1-1_t8.0_h0.0_cRGB.png	d1-1_t8.0_h0.0_cRGB.png	3115	1068093633	f	2013-04-17 09:49:35+02
138	8	112	thumbnails_128x128.h5ar/d1-1_t8.0_h3.0_cRGB.png	d1-1_t8.0_h3.0_cRGB.png	3112	88171741	f	2013-04-17 09:49:35+02
139	8	112	thumbnails_128x128.h5ar/d1-1_t8.0_h6.0_cRGB.png	d1-1_t8.0_h6.0_cRGB.png	3256	-435332103	f	2013-04-17 09:49:35+02
140	8	112	thumbnails_128x128.h5ar/d1-1_t9.0_h0.0_cRGB.png	d1-1_t9.0_h0.0_cRGB.png	3184	1390666588	f	2013-04-17 09:49:35+02
141	8	112	thumbnails_128x128.h5ar/d1-1_t9.0_h3.0_cRGB.png	d1-1_t9.0_h3.0_cRGB.png	3126	1672782918	f	2013-04-17 09:49:35+02
142	8	112	thumbnails_128x128.h5ar/d1-1_t9.0_h6.0_cRGB.png	d1-1_t9.0_h6.0_cRGB.png	3278	-372050420	f	2013-04-17 09:49:36+02
\.


--
-- Name: data_set_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_set_files_id_seq', 142, true);


--
-- Data for Name: data_sets; Type: TABLE DATA; Schema: public; Owner: -
--

COPY data_sets (id, code, location) FROM stdin;
1	20130415093804724-403	1FD3FF61-1576-4908-AE3D-296E60B4CE06/06/e5/ad/20130415093804724-403
2	20130415100158230-407	1FD3FF61-1576-4908-AE3D-296E60B4CE06/1c/84/72/20130415100158230-407
3	20130415100238098-408	1FD3FF61-1576-4908-AE3D-296E60B4CE06/91/0a/10/20130415100238098-408
4	20130415100308111-409	1FD3FF61-1576-4908-AE3D-296E60B4CE06/2f/7a/b9/20130415100308111-409
7	20130417094936021-428	1FD3FF61-1576-4908-AE3D-296E60B4CE06/67/85/36/20130417094936021-428
8	20130417094934693-427	1FD3FF61-1576-4908-AE3D-296E60B4CE06/cd/53/dc/20130417094934693-427
\.


--
-- Name: data_sets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('data_sets_id_seq', 8, true);


--
-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: -
--

COPY database_version_logs (db_version, module_name, run_status, run_status_timestamp, module_code, run_exception) FROM stdin;
006	datastore_server/sql/postgresql/006/schema-006.sql	SUCCESS	2013-04-12 16:44:00.278	\\x0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a20446f6d61696e73202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a43524541544520444f4d41494e20544543485f494420415320424947494e543b0a0a43524541544520444f4d41494e20434f44452041532056415243484152283430293b0a0a43524541544520444f4d41494e2046494c455f5041544820415320564152434841522831303030293b0a0a43524541544520444f4d41494e20424f4f4c45414e5f4348415220415320424f4f4c45414e2044454641554c542046414c53453b0a0a43524541544520444f4d41494e2054494d455f5354414d502041532054494d455354414d5020574954482054494d45205a4f4e453b0a0a0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a2f2a205461626c657320202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202a2f0a2f2a202d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d2d202a2f0a0a435245415445205441424c4520444154415f5345545320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020434f444520434f4445204e4f54204e554c4c2c0a20204c4f434154494f4e2046494c455f50415448204e4f54204e554c4c2c0a0a20205052494d415259204b455920284944292c0a2020554e495155452028434f4445290a293b0a0a43524541544520494e44455820444154415f534554535f434f44455f494458204f4e20444154415f534554532028434f4445293b0a0a435245415445205441424c4520444154415f5345545f46494c455320280a202049442042494753455249414c204e4f54204e554c4c2c0a2020444153455f494420544543485f4944204e4f54204e554c4c2c0a2020504152454e545f494420544543485f49442c0a202052454c41544956455f504154482046494c455f50415448204e4f54204e554c4c2c0a202046494c455f4e414d452046494c455f50415448204e4f54204e554c4c2c0a202053495a455f494e5f425954455320424947494e54204e4f54204e554c4c2c0a2020434845434b53554d5f435243333220494e54454745522c0a202049535f4449524543544f525920424f4f4c45414e5f43484152204e4f54204e554c4c2c0a20204c4153545f4d4f4449464945442054494d455f5354414d50204e4f54204e554c4c2044454641554c54204e4f5728292c0a0a20205052494d415259204b455920284944292c0a2020434f4e53545241494e5420464b5f444154415f5345545f46494c45535f444154415f5345545320464f524549474e204b45592028444153455f494429205245464552454e43455320444154415f534554532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144452c0a2020434f4e53545241494e5420464b5f444154415f5345545f46494c45535f444154415f5345545f46494c455320464f524549474e204b45592028504152454e545f494429205245464552454e43455320444154415f5345545f46494c45532028494429204f4e2044454c4554452043415343414445204f4e2055504441544520434153434144450a293b0a0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f494458204f4e20444154415f5345545f46494c45532028444153455f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f504152454e545f49445f494458204f4e20444154415f5345545f46494c45532028444153455f49442c20504152454e545f4944293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f52454c41544956455f504154485f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2052454c41544956455f50415448293b0a43524541544520494e44455820444154415f5345545f46494c45535f444153455f49445f46494c455f4e414d455f494458204f4e20444154415f5345545f46494c45532028444153455f49442c2046494c455f4e414d45293b0a0a435245415445205441424c45204556454e545320280a20204c4153545f5345454e5f44454c4554494f4e5f4556454e545f494420544543485f4944204e4f54204e554c4c0a293b0a0a435245415445205441424c45204c4153545f46454544494e475f4556454e5420280a2020524547495354524154494f4e5f54494d455354414d502054494d455f5354414d50204e4f54204e554c4c0a293b0a0a	\N
007	../../../../datastore_server/source/sql/postgresql/migration/migration-006-007.sql	SUCCESS	2016-06-13 19:47:28.689	\\x2d2d206368616e676520434f444520646f6d61696e20746f2056415243484152283630292c20756e666f7274756e6174656c79206120747970652063616e6e6f7420626520616c746572656420666f7220616e206578697374696e6720646f6d61696e0a0a414c544552205441424c4520444154415f5345545320414c54455220434f4c554d4e20434f444520545950452056415243484152283630293b0a0a44524f5020444f4d41494e20434f44453b0a43524541544520444f4d41494e20434f44452041532056415243484152283630293b0a0a414c544552205441424c4520444154415f5345545320414c54455220434f4c554d4e20434f4445205459504520434f44453b0a	\N
008	../../../../datastore_server/source/sql/postgresql/migration/migration-007-008.sql	SUCCESS	2016-06-13 19:47:28.712	\\x616c746572207461626c6520646174615f7365745f66696c65732064726f7020636f6e73747261696e742069662065786973747320666b5f646174615f7365745f66696c65735f646174615f7365745f66696c65733b0a	\N
\.


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: -
--

COPY events (last_seen_deletion_event_id) FROM stdin;
175
\.


--
-- Data for Name: last_feeding_event; Type: TABLE DATA; Schema: public; Owner: -
--

COPY last_feeding_event (registration_timestamp) FROM stdin;
\.


--
-- Name: data_set_files_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_files
    ADD CONSTRAINT data_set_files_pkey PRIMARY KEY (id);


--
-- Name: data_sets_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_sets
    ADD CONSTRAINT data_sets_code_key UNIQUE (code);


--
-- Name: data_sets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_sets
    ADD CONSTRAINT data_sets_pkey PRIMARY KEY (id);


--
-- Name: data_set_files_dase_id_file_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_set_files_dase_id_file_name_idx ON data_set_files USING btree (dase_id, file_name);


--
-- Name: data_set_files_dase_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_set_files_dase_id_idx ON data_set_files USING btree (dase_id);


--
-- Name: data_set_files_dase_id_parent_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_set_files_dase_id_parent_id_idx ON data_set_files USING btree (dase_id, parent_id);


--
-- Name: data_set_files_dase_id_relative_path_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_set_files_dase_id_relative_path_idx ON data_set_files USING btree (dase_id, relative_path);


--
-- Name: data_sets_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX data_sets_code_idx ON data_sets USING btree (code);


--
-- Name: fk_data_set_files_data_sets; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY data_set_files
    ADD CONSTRAINT fk_data_set_files_data_sets FOREIGN KEY (dase_id) REFERENCES data_sets(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: -
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM pkupczyk;
GRANT ALL ON SCHEMA public TO pkupczyk;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

