import pytest
import time

from pybis import Openbis

openbis_url = 'https://localhost:8443'
admin_username = 'vermeul'
admin_password = 'blabla'

@pytest.yield_fixture(scope="module")
def openbis_instance():
    instance = Openbis(url=openbis_url, verify_certificates=False)
    print("\nLOGGING IN...")
    instance.login(admin_username, admin_password)
    yield instance
    instance.logout()
    print("LOGGED OUT...")

@pytest.yield_fixture(scope="module")
def role_assignment_person():
    # this user has to be present in the openBIS instance
    yield 'admin'


@pytest.yield_fixture(scope="module")
def space():
    o = Openbis(url=openbis_url, verify_certificates=False)
    o.login(admin_username, admin_password)

    space_exists = o.get_space(code='DEFAULT')
    yield space_exists

    o.logout()
