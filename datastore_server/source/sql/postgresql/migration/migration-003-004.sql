DROP INDEX DATA_SET_FILES_RELATIVE_PATH_IDX;
DROP INDEX DATA_SET_FILES_FILE_NAME_IDX;

CREATE INDEX DATA_SET_FILES_DASE_ID_IDX ON DATA_SET_FILES (DASE_ID);
CREATE INDEX DATA_SET_FILES_DASE_ID_PARENT_ID_IDX ON DATA_SET_FILES (DASE_ID, PARENT_ID);
CREATE INDEX DATA_SET_FILES_DASE_ID_RELATIVE_PATH_IDX ON DATA_SET_FILES (DASE_ID, RELATIVE_PATH);
CREATE INDEX DATA_SET_FILES_DASE_ID_FILE_NAME_IDX ON DATA_SET_FILES (DASE_ID, FILE_NAME);
