import pybis
from ..command_result import CommandResult
import uuid
import os
from ..git import GitRepoFileInfo
from .openbis_command import OpenbisCommand


class OpenbisSync(OpenbisCommand):
    """A command object for synchronizing with openBIS."""


    def __init__(self, dm, ignore_missing_parent=False):
        self.ignore_missing_parent = ignore_missing_parent
        super(OpenbisSync, self).__init__(dm)


    def check_configuration(self):
        missing_config_settings = []
        if self.openbis is None:
            missing_config_settings.append('openbis_url')
        if self.user() is None:
            missing_config_settings.append('user')
        if self.data_set_type() is None:
            missing_config_settings.append('data_set type')
        if self.object_id() is None and self.collection_id() is None:
            missing_config_settings.append('object id or collection id')
        if len(missing_config_settings) > 0:
            return CommandResult(returncode=-1,
                                 output="Missing configuration settings for {}.".format(missing_config_settings))
        return CommandResult(returncode=0, output="")


    def create_data_set_code(self):
        try:
            data_set_code = self.openbis.create_permId()
            return CommandResult(returncode=0, output=""), data_set_code
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None

    def create_data_set(self, data_set_code, external_dms, repository_id, ignore_parent=False):
        data_set_type = self.data_set_type()
        parent_data_set_id = None if ignore_parent else self.data_set_id()
        properties = self.data_set_properties()
        result = self.git_wrapper.git_top_level_path()
        if result.failure():
            return result
        top_level_path = result.output
        result = self.git_wrapper.git_commit_hash()
        if result.failure():
            return result
        commit_id = result.output
        sample_id = self.object_id()
        experiment_id = self.collection_id()
        contents = GitRepoFileInfo(self.git_wrapper).contents(git_annex_hash_as_checksum=self.git_annex_hash_as_checksum())
        try:
            data_set = self.openbis.new_git_data_set(data_set_type, top_level_path, commit_id, repository_id, external_dms.code,
                                                     sample=sample_id, experiment=experiment_id, properties=properties, parents=parent_data_set_id,
                                                     data_set_code=data_set_code, contents=contents)
            return CommandResult(returncode=0, output="Created data set {}.".format(str(data_set))), data_set
        except ValueError as e:
            return CommandResult(returncode=-1, output=str(e)), None


    def commit_metadata_updates(self, msg_fragment=None):
        return self.data_mgmt.commit_metadata_updates(msg_fragment)


    def prepare_repository_id(self):
        repository_id = self.repository_id()
        if self.repository_id() is None:
            repository_id = str(uuid.uuid4())
            self.settings_resolver.repository.set_value_for_parameter('id', repository_id, 'local')
        return CommandResult(returncode=0, output=repository_id)


    def handle_unsynced_commits(self):
        return CommandResult(returncode=0, output="")


    def handle_missing_data_set(self):
        return CommandResult(returncode=0, output="")


    def git_hash_matches(self, data_set):
        content_copies = data_set.data['linkedData']['contentCopies']
        for content_copy in content_copies:
            cc_commit_hash = content_copy['gitCommitHash']
            result = self.git_wrapper.git_commit_hash()
            if result.failure():
                return result
            git_comit_hash = result.output
            if cc_commit_hash == git_comit_hash:
                return True
        return False

    def continue_without_parent_data_set(self):

        if self.ignore_missing_parent:
            return True

        while True:
            print("The data set {} not found in openBIS".format(self.data_set_id()))
            print("Create new data set without parent? (y/n)")
            continue_without_parent = input("> ")
            if continue_without_parent == "y":
                return True
            elif continue_without_parent == "n":
                return False 


    def run(self, info_only=False):

        ignore_parent = False

        if self.data_set_id() is not None:
            try:
                data_set = self.openbis.get_dataset(self.data_set_id())
                if self.git_hash_matches(data_set):
                    return CommandResult(returncode=0, output="Nothing to sync.")
            except ValueError as e:
                if 'no such dataset' in str(e):
                    if info_only:
                        return CommandResult(returncode=-1, output="Parent data set not found in openBIS.")
                    ignore_parent = self.continue_without_parent_data_set()
                    if not ignore_parent:
                        return CommandResult(returncode=-1, output="Parent data set not found in openBIS.")
                else:
                    raise e

        if info_only:
            if self.data_set_id() is None:
                return CommandResult(returncode=-1, output="Not yet synchronized with openBIS.")
            else:
                return CommandResult(returncode=-1, output="There are git commits which have not been synchronized.")

        # TODO Write mementos in case openBIS is unreachable
        # - write a file to the .git/obis folder containing the commit id. Filename includes a timestamp so they can be sorted.

        result = self.prepare_run()
        if result.failure():
            return result

        result = self.prepare_repository_id()
        if result.failure():
            return result
        repository_id = result.output

        result = self.prepare_external_dms()
        if result.failure():
            return result
        external_dms = result.output

        result, data_set_code = self.create_data_set_code()
        if result.failure():
            return result

        self.commit_metadata_updates()

        # Update data set id as last commit so we can easily revert it on failure
        self.settings_resolver.repository.set_value_for_parameter('data_set_id', data_set_code, 'local')
        self.commit_metadata_updates("data set id")

        # create a data set, using the existing data set as a parent, if there is one
        result, data_set = self.create_data_set(data_set_code, external_dms, repository_id, ignore_parent)
        return result
