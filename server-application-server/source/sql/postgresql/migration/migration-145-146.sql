-- From this version, any sample type assignment that is not a script can be selected to be shown or not on the forms.
-- In the past this value was not used and was set by default to false, from this version the default should be true until the user specifies otherwise.
update sample_type_property_types set is_shown_edit = 't' where script_id is null;