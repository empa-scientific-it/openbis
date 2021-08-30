import os
import time

import pytest
from pybis import Openbis

openbis_url = "https://localhost:8443"
admin_username = "admin"
admin_password = "changeit"

@pytest.fixture(scope="module")
def openbis_instance():
    instance = Openbis(
        url=openbis_url,
        verify_certificates=False,
    )
    print("\nLOGGING IN...")
    instance.login(admin_username, admin_password)
    yield instance
    instance.logout()
    print("LOGGED OUT...")


@pytest.fixture(scope="module")
def other_openbis_instance():
    instance = Openbis(
        url=openbis_url,
        verify_certificates=False,

    )
    print("\nLOGGING IN...")
    instance.login(admin_username, admin_password)
    yield instance
    instance.logout()
    print("LOGGED OUT...")


@pytest.fixture(scope="session")
def space():
    o = Openbis(
        url=openbis_url,
        verify_certificates=False,

    )
    o.login(admin_username, admin_password)

    # create a space
    timestamp = time.strftime("%a_%y%m%d_%H%M%S").upper()
    space_name = "test_space_" + timestamp
    space = o.new_space(code=space_name)
    space.save()
    yield space

    # teardown
    o.logout()
