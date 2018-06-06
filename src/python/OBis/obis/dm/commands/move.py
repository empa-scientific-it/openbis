import socket
import os
import pybis
from .clone import Clone
from .openbis_command import OpenbisCommand, ContentCopySelector
from ..checksum import validate_checksum
from ..command_result import CommandResult
from ..utils import cd
from ..utils import run_shell
from ..utils import complete_openbis_config
from ..repository_utils import delete_repository, get_repository_location
from ... import dm


class Move(OpenbisCommand):
    """
    Implements the move command. Uses other commands for implementation.
    """

    def __init__(self, dm, data_set_id, ssh_user, content_copy_index, skip_integrity_check):
        self.data_set_id = data_set_id
        self.ssh_user = ssh_user
        self.content_copy_index = content_copy_index
        self.load_global_config(dm)
        self.skip_integrity_check = skip_integrity_check
        super(Move, self).__init__(dm)

    def run(self):
        clone = Clone(self.data_mgmt, self.data_set_id, self.ssh_user, self.content_copy_index, self.skip_integrity_check)
        result = clone.run()
        if result.failure():
            return result

        self.openbis.delete_content_copy(self.data_set_id, clone.content_copy)

        host = clone.content_copy['externalDms']['address'].split(':')[0]
        path = clone.content_copy['path']

        old_repository_location = get_repository_location(self.ssh_user, host, path)

        if self.skip_integrity_check == True:
            return CommandResult(returncode=0, output="Since the integrit check was skipped, please make sure the data was " +
                                                        "copied correctly and delete the old copy manually {}.".format(old_repository_location))

        delete_repository(self.ssh_user, host, path)

        return CommandResult(returncode=0, output="")
