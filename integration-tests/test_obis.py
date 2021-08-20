#!/usr/bin/python
# encoding=utf8
#!/usr/bin/python
#
# Requirement:
#   The pybis module must be available.

import json
import settings
import socket
import subprocess
import systemtest.testcase
import systemtest.util as util
import os
from contextlib import contextmanager
from random import randrange
from subprocess import PIPE


# The output buffer and decurator are used to only print detailed output
# when something fails. Otherwise there would be way too much.
output_buffer = ""


def decorator_print(func):
    def wrapper(tmpdir, *args, **kwargs):
        try:
            func(tmpdir, *args, **kwargs)
        except Exception:
            util.printAndFlush(output_buffer)
            raise

    return wrapper


class TestCase(systemtest.testcase.TestCase):

    # setup

    def execute(self):

        self.OPENBIS_URL = "https://localhost:8443/openbis"

        self.installOpenbis()
        self.installPybis()
        self.installObis()
        self.openbisController = self.createOpenbisController()
        self.openbisController.createTestDatabase("openbis")
        self.openbisController.asProperties["max-number-of-sessions-per-user"] = "0"
        self.openbisController.dssProperties["host-address"] = "https://localhost"

        self.openbisController.allUp()

        tmpdir = os.path.abspath("obis_data_" + str(randrange(100000)))
        os.mkdir(tmpdir)
        self._test_obis(tmpdir)

        tmpdir = os.path.abspath("obis_data_" + str(randrange(100000)))
        os.mkdir(tmpdir)
        self._test_obis_with_metadata_folder(tmpdir)

    def _get_openbis(self):
        # pybis can only be imported after installPybis is called
        import pybis

        o = pybis.Openbis(url="https://localhost:8443", verify_certificates=False)
        o.login("admin", "admin", save_token=True)
        return o

    def _setup_masterdata(self, o):
        spaces = o.get_spaces().df.code.values
        if "OBIS_TEST_1" not in spaces:
            o.new_space(code="OBIS_TEST_1").save()
        if "OBIS_TEST_2" not in spaces:
            o.new_space(code="OBIS_TEST_2").save()
        if (
            "/OBIS_TEST_1/SAMPLE_1"
            not in o.get_samples(code="SAMPLE_1").df.identifier.values
        ):
            o.new_sample(type="UNKNOWN", code="SAMPLE_1", space="OBIS_TEST_1").save()
        if (
            "/OBIS_TEST_1/SAMPLE_2"
            not in o.get_samples(code="SAMPLE_2").df.identifier.values
        ):
            o.new_sample(type="UNKNOWN", code="SAMPLE_2", space="OBIS_TEST_1").save()
        if "/OBIS_TEST_1/PROJECT_1" not in o.get_projects().df.identifier.values:
            o.new_project(space="OBIS_TEST_1", code="PROJECT_1").save()
        if (
            "/OBIS_TEST_1/PROJECT_1/COLLECTION_1"
            not in o.get_experiments(code="COLLECTION_1").df.identifier.values
        ):
            o.new_experiment(
                type="UNKNOWN", code="COLLECTION_1", project="PROJECT_1"
            ).save()

    # actual tests

    def _init_global_settings(self):
        global output_buffer
        output_buffer = "=================== 1. Global settings ===================\n"
        cmd("obis config -g clear")
        cmd("obis data_set -g clear")
        cmd(
            "obis config -g set openbis_url="
            + self.OPENBIS_URL
            + ", user=admin, verify_certificates=false, hostname="
            + socket.gethostname()
        )
        cmd("obis data_set -g set type=UNKNOWN")
        settings = get_settings_global()
        assert settings["config"]["openbis_url"] == self.OPENBIS_URL
        assert settings["config"]["user"] == "admin"
        assert settings["config"]["verify_certificates"] == False
        assert settings["config"]["hostname"] == socket.gethostname()
        assert settings["data_set"]["type"] == "UNKNOWN"

    @decorator_print
    def _test_obis(self, tmpdir):
        util.printWhoAmI()
        o = self._get_openbis()
        self._setup_masterdata(o)
        self._init_global_settings()
        self._run_tests(tmpdir, o)

    @decorator_print
    def _test_obis_with_metadata_folder(self, tmpdir):
        util.printWhoAmI()
        o = self._get_openbis()
        self._setup_masterdata(o)
        self._init_global_settings()

        obis_metadata_folder = os.path.abspath(os.path.join(tmpdir, "obis_metadata"))
        os.makedirs(obis_metadata_folder)
        cmd("obis config -g set obis_metadata_folder=" + obis_metadata_folder)
        settings = get_settings_global()
        assert settings["config"]["obis_metadata_folder"] == obis_metadata_folder

        self._run_tests(tmpdir, o, skip=["clone", "addref", "removeref", "sync"])

    def _run_tests(self, tmpdir, o, skip=[]):

        with cd(tmpdir):
            cmd("mkdir obis_data")
        with cd(tmpdir + "/obis_data"):

            output_buffer = (
                "=================== 2. First commit =================== skip: "
                + str(skip)
                + "\n"
            )
            cmd("obis init data1")
            with cd("data1"):
                cmd("touch file")
                result = cmd("obis status")
                assert "? file" in result
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert settings["repository"]["external_dms_id"].startswith(
                    "ADMIN-" + socket.gethostname().upper()
                )
                assert len(settings["repository"]["id"]) == 36
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data1")

            output_buffer = "=================== 3. Second commit ===================\n"
            with cd("data1"):
                settings_before = get_settings()
                cmd("dd if=/dev/zero of=big_file bs=1000000 count=1")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert (
                    settings["repository"]["data_set_id"]
                    != settings_before["repository"]["data_set_id"]
                )
                assert settings["repository"]["external_dms_id"].startswith(
                    "ADMIN-" + socket.gethostname().upper()
                )
                assert (
                    settings["repository"]["external_dms_id"]
                    == settings_before["repository"]["external_dms_id"]
                )
                assert (
                    settings["repository"]["id"] == settings_before["repository"]["id"]
                )
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                result = cmd_git(
                    "annex info big_file", settings, tmpdir, "obis_data/data1"
                )
                assert "file: big_file" in result
                assert (
                    "key: SHA256E-s1000000--d29751f2649b32ff572b5e0a9f541ea660a50f94ff0beedfb0b692b924cc8025"
                    in result
                )
                assert "present: true" in result
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data1")
                assert (
                    data_set["parents"][0]["code"]
                    == settings_before["repository"]["data_set_id"]
                )

            output_buffer = (
                "=================== 4. Second repository ===================\n"
            )
            cmd("obis init data2")
            with cd("data2"):
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                cmd("touch file")
                result = cmd("obis commit -m 'commit-message'")
                with cd("../data1"):
                    settings_data1 = get_settings()
                settings = get_settings()
                assert settings["repository"]["external_dms_id"].startswith(
                    "ADMIN-" + socket.gethostname().upper()
                )
                assert (
                    settings["repository"]["external_dms_id"]
                    == settings_data1["repository"]["external_dms_id"]
                )
                assert len(settings["repository"]["id"]) == 36
                assert (
                    settings["repository"]["id"] != settings_data1["repository"]["id"]
                )
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data2")

        output_buffer = (
            "=================== 5. Second external dms ===================\n"
        )
        with cd(tmpdir):
            cmd("mkdir obis_data_b")
        with cd(tmpdir + "/obis_data_b"):
            cmd("obis init data3")
            with cd("data3"):
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                cmd("touch file")
                result = cmd("obis commit -m 'commit-message'")
                with cd("../../obis_data/data1"):
                    settings_data1 = get_settings()
                settings = get_settings()
                assert settings["repository"]["external_dms_id"].startswith(
                    "ADMIN-" + socket.gethostname().upper()
                )
                assert (
                    settings["repository"]["external_dms_id"]
                    != settings_data1["repository"]["external_dms_id"]
                )
                assert len(settings["repository"]["id"]) == 36
                assert (
                    settings["repository"]["id"] != settings_data1["repository"]["id"]
                )
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data_b/data3")

        output_buffer = (
            "=================== 6. Error on first commit ===================\n"
        )
        with cd(tmpdir + "/obis_data"):
            cmd("obis init data4")
            with cd("data4"):
                cmd("touch file")
                result = cmd("obis commit -m 'commit-message'")
                assert (
                    "Missing configuration settings for ['object id or collection id']."
                    in result
                )
                result = cmd("obis status")
                assert "? file" in result
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data4")

            output_buffer = "=================== 7. Attach data set to a collection ===================\n"
            cmd("obis init data5")
            with cd("data5"):
                cmd("touch file")
                cmd("obis collection set id=/OBIS_TEST_1/PROJECT_1/COLLECTION_1")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert settings["repository"]["external_dms_id"].startswith(
                    "ADMIN-" + socket.gethostname().upper()
                )
                assert len(settings["repository"]["id"]) == 36
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data5")

            if "addref" not in skip:
                output_buffer = "=================== 8. Addref ===================\n"
                cmd("cp -r data1 data6")
                cmd("obis addref data6")
                with cd("data1"):
                    settings_data1 = get_settings()
                with cd("data6"):
                    settings_data6 = get_settings()
                assert settings_data6 == settings_data1
                result = cmd("obis addref data6")
                assert "DataSet already exists in the database" in result
                result = cmd("obis addref data7")
                assert "Invalid value" in result
                data_set = o.get_dataset(
                    settings_data6["repository"]["data_set_id"]
                ).data
                with cd("data6"):
                    assert_matching(settings_data6, data_set, tmpdir, "obis_data/data6")

            if "clone" not in skip:
                output_buffer = (
                    "=================== 9. Local clone ===================\n"
                )
                with cd("data2"):
                    settings_data2 = get_settings()
                with cd("../obis_data_b"):
                    cmd("obis clone " + settings_data2["repository"]["data_set_id"])
                    with cd("data2"):
                        settings_data2_clone = get_settings()
                        assert settings_data2_clone["repository"][
                            "external_dms_id"
                        ].startswith("ADMIN-" + socket.gethostname().upper())
                        assert (
                            settings_data2_clone["repository"]["external_dms_id"]
                            != settings_data2["repository"]["external_dms_id"]
                        )
                        data_set = o.get_dataset(
                            settings_data2_clone["repository"]["data_set_id"]
                        ).data
                        assert_matching(
                            settings_data2_clone, data_set, tmpdir, "obis_data_b/data2"
                        )
                        del settings_data2["repository"]["external_dms_id"]
                        del settings_data2_clone["repository"]["external_dms_id"]
                        assert settings_data2_clone == settings_data2

            output_buffer = (
                "=================== 11. Init analysis ===================\n"
            )
            cmd("obis init_analysis -p data1 analysis1")
            with cd("analysis1"):
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                cmd("touch file")
                result = cmd("obis commit -m 'commit-message'")
            with cd("data1"):
                settings_data1 = get_settings()
            with cd("analysis1"):
                settings_analysis1 = get_settings()
                assert (
                    "Created data set {}.".format(
                        settings_analysis1["repository"]["data_set_id"]
                    )
                    in result
                )
                assert len(settings_analysis1["repository"]["id"]) == 36
                assert (
                    settings_analysis1["repository"]["id"]
                    != settings_data1["repository"]["id"]
                )
                assert (
                    settings_analysis1["repository"]["data_set_id"]
                    != settings_data1["repository"]["data_set_id"]
                )
                data_set = o.get_dataset(
                    settings_analysis1["repository"]["data_set_id"]
                ).data
                assert_matching(
                    settings_analysis1, data_set, tmpdir, "obis_data/analysis1"
                )
                assert (
                    data_set["parents"][0]["code"]
                    == settings_data1["repository"]["data_set_id"]
                )
            with cd("data1"):
                cmd("obis init_analysis analysis2")
                with cd("analysis2"):
                    cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                    cmd("touch file")
                    result = cmd("obis commit -m 'commit-message'")
                    settings_analysis2 = get_settings()
                    assert (
                        "Created data set {}.".format(
                            settings_analysis2["repository"]["data_set_id"]
                        )
                        in result
                    )
                    assert len(settings_analysis2["repository"]["id"]) == 36
                    assert (
                        settings_analysis2["repository"]["id"]
                        != settings_data1["repository"]["id"]
                    )
                    assert (
                        settings_analysis2["repository"]["data_set_id"]
                        != settings_data1["repository"]["data_set_id"]
                    )
                    data_set = o.get_dataset(
                        settings_analysis2["repository"]["data_set_id"]
                    ).data
                    assert_matching(
                        settings_analysis2,
                        data_set,
                        tmpdir,
                        "obis_data/data1/analysis2",
                    )
                    assert (
                        data_set["parents"][0]["code"]
                        == settings_data1["repository"]["data_set_id"]
                    )
                result = cmd_git(
                    "check-ignore analysis2", settings_data1, tmpdir, "obis_data/data1"
                )
                assert "analysis2" in result

            output_buffer = (
                "=================== 12. Metadata only commit ===================\n"
            )
            cmd("obis init data7")
            with cd("data7"):
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                cmd("touch file")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data7")
                cmd("obis object clear id")
                cmd("obis collection set id=/OBIS_TEST_1/PROJECT_1/COLLECTION_1")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                assert (
                    "Created data set {}.".format(settings["repository"]["data_set_id"])
                    in result
                )
                data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                assert_matching(settings, data_set, tmpdir, "obis_data/data7")

            if "sync" not in skip:
                output_buffer = (
                    "=================== 13. obis sync ===================\n"
                )
                with cd("data7"):
                    cmd("touch file2")
                    cmd("git add file2")
                    cmd("git commit -m 'msg'")
                    result = cmd("obis sync")
                    settings = get_settings()
                    assert (
                        "Created data set {}.".format(
                            settings["repository"]["data_set_id"]
                        )
                        in result
                    )
                    data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
                    assert_matching(settings, data_set, tmpdir, "obis_data/data7")
                    result = cmd("obis sync")
                    assert "Nothing to sync" in result

            output_buffer = (
                "=================== 14. Set data set properties ===================\n"
            )
            cmd("obis init data8")
            with cd("data8"):
                result = cmd("obis data_set -p set a=0")
                settings = get_settings()
                assert settings["data_set"]["properties"] == {"A": "0"}
                cmd('obis data_set set properties={"a":"0","b":"1","c":"2"}')
                cmd("obis data_set -p set c=3")
                settings = get_settings()
                assert settings["data_set"]["properties"] == {
                    "A": "0",
                    "B": "1",
                    "C": "3",
                }
                result = cmd('obis data_set set properties={"a":"0","A":"1"}')
                assert "Duplicate key after capitalizing JSON config: A" in result

            if "removeref" not in skip:
                output_buffer = (
                    "=================== 15. Removeref ===================\n"
                )
                with cd("data6"):
                    settings = get_settings()
                content_copies = get_data_set(o, settings)["linkedData"][
                    "contentCopies"
                ]
                assert len(content_copies) == 2
                cmd("obis removeref data6")
                content_copies = get_data_set(o, settings)["linkedData"][
                    "contentCopies"
                ]
                assert len(content_copies) == 1
                assert content_copies[0]["path"].endswith("data1")
                cmd("obis addref data6")
                cmd("obis removeref data1")
                content_copies = get_data_set(o, settings)["linkedData"][
                    "contentCopies"
                ]
                assert len(content_copies) == 1
                assert content_copies[0]["path"].endswith("data6")
                result = cmd("obis removeref data1")
                assert "Matching content copy not fount in data set" in result
                cmd("obis addref data1")

            output_buffer = "=================== 18. Use git-annex hashes as checksums ===================\n"
            cmd("obis init data10")
            with cd("data10"):
                cmd("dd if=/dev/zero of=big_file bs=2000000 count=1")
                cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
                # use SHA256 form git annex by default
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                search_result = o.search_files(settings["repository"]["data_set_id"])
                files = list(
                    filter(
                        lambda file: file["fileLength"] > 0, search_result["objects"]
                    )
                )
                assert_file_paths(files, ["big_file"])
                for file in files:
                    assert file["checksumType"] == "SHA256"
                    assert len(file["checksum"]) == 64
                # don't use git annex hash - use default CRC32
                cmd("obis config set git_annex_hash_as_checksum=false")
                result = cmd("obis commit -m 'commit-message'")
                settings = get_settings()
                search_result = o.search_files(settings["repository"]["data_set_id"])
                files = list(
                    filter(
                        lambda file: file["fileLength"] > 0, search_result["objects"]
                    )
                )
                assert_file_paths(files, ["big_file"])
                for file in files:
                    assert file["checksumType"] is None
                    assert file["checksum"] is None
                    assert file["checksumCRC32"] != 0

            output_buffer = (
                "=================== 19. Clearing settings ===================\n"
            )
            cmd("obis init data11")
            with cd("data11"):
                assert get_settings()["repository"] == {
                    "id": None,
                    "external_dms_id": None,
                    "data_set_id": None,
                }
                cmd("obis repository set id=0, external_dms_id=1, data_set_id=2")
                assert get_settings()["repository"] == {
                    "id": "0",
                    "external_dms_id": "1",
                    "data_set_id": "2",
                }
                cmd("obis repository clear external_dms_id, data_set_id")
                assert get_settings()["repository"] == {
                    "id": "0",
                    "external_dms_id": None,
                    "data_set_id": None,
                }
                cmd("obis repository clear")
                assert get_settings()["repository"] == {
                    "id": None,
                    "external_dms_id": None,
                    "data_set_id": None,
                }

            output_buffer = (
                "=================== 22. changing identifier ===================\n"
            )
            settings = create_repository_and_commit(
                tmpdir, o, "data14", "/OBIS_TEST_1/SAMPLE_2"
            )
            move_sample(o, settings["object"]["permId"], "OBIS_TEST_2")
            try:
                settings = commit_new_change(tmpdir, o, "data14")
                assert settings["object"]["id"] == "/OBIS_TEST_2/SAMPLE_2"
            finally:
                move_sample(o, settings["object"]["permId"], "OBIS_TEST_1")
            with cd("data14"):
                assert get_settings()["object"]["permId"] is not None
            cmd("obis object set id=/OBIS_TEST_1/SAMPLE_1")
            with cd("data14"):
                assert get_settings()["object"]["permId"] is not None


# utils


def get_cmd_result(completed_process, tmpdir=""):
    result = ""
    if completed_process.stderr:
        result += completed_process.stderr.decode("utf-8").strip()
    if completed_process.stdout:
        result += completed_process.stdout.decode("utf-8").strip()
    return result


def cmd(cmd, timeout=None):
    global output_buffer
    output_buffer += "==== running: " + cmd + "\n"
    completed_process = subprocess.run(
        cmd.split(" "), stdout=PIPE, stderr=PIPE, timeout=timeout
    )
    result = get_cmd_result(completed_process)
    output_buffer += result + "\n"
    return result


def cmd_git(params, settings, tmpdir, path):
    obis_metadata_folder = settings["config"]["obis_metadata_folder"]
    if obis_metadata_folder is None:
        return cmd("git " + params)
    else:
        work_tree = os.path.join(tmpdir, path)
        git_dir = os.path.join(obis_metadata_folder, work_tree[1:], ".git")
        return cmd(
            "git --work-tree=" + work_tree + " --git-dir=" + git_dir + " " + params
        )


@contextmanager
def cd(newdir):
    """Safe cd -- return to original dir after execution, even if an exception is raised."""
    prevdir = os.getcwd()
    os.chdir(os.path.expanduser(newdir))
    try:
        yield
    finally:
        os.chdir(prevdir)


def get_settings():
    settings = cmd("obis settings get")
    return json.loads(settings)


def get_settings_global():
    settings = cmd("obis settings -g get")
    return json.loads(settings)


def get_data_set(o, settings):
    return o.get_dataset(settings["repository"]["data_set_id"]).data


def move_sample(o, sample_permId, space):
    sample = o.get_sample(sample_permId)
    sample.space = space
    sample.save()


def create_repository_and_commit(tmpdir, o, repo_name, object_id):
    cmd("obis init " + repo_name)
    with cd(repo_name):
        cmd("touch file")
        result = cmd("obis status")
        assert "? file" in result
        cmd("obis object set id=" + object_id)
        result = cmd("obis commit -m 'commit-message'")
        settings = get_settings()
        assert settings["repository"]["external_dms_id"].startswith(
            "ADMIN-" + socket.gethostname().upper()
        )
        assert len(settings["repository"]["id"]) == 36
        assert (
            "Created data set {}.".format(settings["repository"]["data_set_id"])
            in result
        )
        data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
        assert_matching(settings, data_set, tmpdir, "obis_data/" + repo_name)
        return settings


def commit_new_change(tmpdir, o, repo_name):
    with cd(repo_name):
        filename = "file" + str(randrange(100000))
        cmd("touch " + filename)
        result = cmd("obis status")
        assert "? " + filename in result
        result = cmd("obis commit -m 'commit-message'")
        settings = get_settings()
        assert settings["repository"]["external_dms_id"].startswith(
            "ADMIN-" + socket.gethostname().upper()
        )
        assert len(settings["repository"]["id"]) == 36
        assert (
            "Created data set {}.".format(settings["repository"]["data_set_id"])
            in result
        )
        data_set = o.get_dataset(settings["repository"]["data_set_id"]).data
        assert_matching(settings, data_set, tmpdir, "obis_data/" + repo_name)
        return settings


def assert_matching(settings, data_set, tmpdir, path):
    content_copies = data_set["linkedData"]["contentCopies"]
    content_copy = list(
        filter(lambda cc: cc["path"].endswith(path) == 1, content_copies)
    )[0]
    assert data_set["type"]["code"] == settings["data_set"]["type"]
    assert (
        content_copy["externalDms"]["code"] == settings["repository"]["external_dms_id"]
    )
    assert content_copy["gitCommitHash"] == cmd_git(
        "rev-parse --short HEAD", settings, tmpdir, path
    )
    assert content_copy["gitRepositoryId"] == settings["repository"]["id"]
    if settings["object"]["id"] is not None:
        assert (
            data_set["sample"]["identifier"]["identifier"] == settings["object"]["id"]
        )
        assert data_set["sample"]["permId"]["permId"] == settings["object"]["permId"]
    if settings["collection"]["id"] is not None:
        assert (
            data_set["experiment"]["identifier"]["identifier"]
            == settings["collection"]["id"]
        )
        assert (
            data_set["experiment"]["permId"]["permId"]
            == settings["collection"]["permId"]
        )


def assert_file_paths(files, expected_paths):
    paths = list(map(lambda file: file["path"], files))
    for expected_path in expected_paths:
        assert expected_path in paths


TestCase(settings, __file__).runTest()
