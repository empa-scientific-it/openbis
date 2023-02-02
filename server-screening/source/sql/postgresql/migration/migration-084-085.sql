-- mark screening core plugin as initialized for existing installation
--
insert into CORE_PLUGINS (ID, NAME, VERSION) values (nextval('CORE_PLUGIN_ID_SEQ'), 'screening', 1);

