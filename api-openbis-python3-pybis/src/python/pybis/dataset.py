import os
from functools import partialmethod
from pathlib import Path
from threading import Thread
from queue import Queue
from typing import Set, Optional, List
from tabulate import tabulate
from .openbis_object import OpenBisObject
from .definitions import (
    openbis_definitions,
    get_type_for_entity,
    get_fetchoption_for_entity,
)
from .utils import (
    VERBOSE,
    parse_jackson,
    extract_permid,
    extract_code,
    extract_downloadUrl,
)
from .things import Things
import requests
from requests import Request, Session
import json
from pandas import DataFrame
from urllib.parse import urlparse, urljoin, quote
import urllib.parse
import zipfile
import random
import time

# needed for Data upload
PYBIS_PLUGIN = "dataset-uploader-api"
dataset_definitions = openbis_definitions("dataSet")
dss_endpoint = "/datastore_server/rmi-data-store-server-v3.json"


def signed_to_unsigned(sig_int):
    """openBIS delivers crc32 checksums as signed integers.
    If the number is negative, we just have to add 2**32
    We display the hex number to match with the classic UI
    """
    if sig_int < 0:
        sig_int += 2**32
    return "%x" % (sig_int & 0xFFFFFFFF)


class DataSet(
    OpenBisObject,
    entity="dataSet",
    single_item_method_name="get_dataset",
):
    """DataSet are openBIS objects that contain the actual files."""

    def __init__(
        self,
        openbis_obj,
        type,
        data=None,
        files=None,
        zipfile=None,
        folder=None,
        kind=None,
        props=None,
        **kwargs,
    ):

        if kind == "PHYSICAL":
            if files is None and zipfile is None:
                raise ValueError("please provide at least one file")

            if files and zipfile:
                raise ValueError(
                    "please provide either a list of files or a single zipfile"
                )

            if zipfile:
                files = [zipfile]
                self.__dict__["isZipDirectoryUpload"] = True
            else:
                self.__dict__["isZipDirectoryUpload"] = False

            if files:
                if isinstance(files, str):
                    files = [files]

                for file in files:
                    if not os.path.exists(file):
                        raise ValueError(f"File {file} does not exist")

                self.__dict__["files"] = files

        # initialize the OpenBisObject
        super().__init__(openbis_obj, type=type, data=data, props=props, **kwargs)

        self.__dict__["files_in_wsp"] = []

        # existing DataSet
        if data is not None:
            if data["physicalData"] is None:
                self.__dict__["shareId"] = None
                self.__dict__["location"] = None
            else:
                self.__dict__["shareId"] = data["physicalData"]["shareId"]
                self.__dict__["location"] = data["physicalData"]["location"]

        if kind is not None:
            kind = kind.upper()
            allowed_kinds = ["PHYSICAL", "CONTAINER", "LINK"]
            if kind not in allowed_kinds:
                raise ValueError(
                    f"only these values are allowed for kind: {allowed_kinds}"
                )
            self.a.__dict__["_kind"] = kind

        self.__dict__["folder"] = folder

        if getattr(self, "parents") is None:
            self.a.__dict__["_parents"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_parents_orig"] = self.a.__dict__["_parents"]

        if getattr(self, "children") is None:
            self.a.__dict__["_children"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_children_orig"] = self.a.__dict__["_children"]

        if getattr(self, "container") is None:
            self.a.__dict__["_container"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_container_orig"] = self.a.__dict__["_container"]

        if getattr(self, "component") is None:
            self.a.__dict__["_component"] = []
        else:
            if not self.is_new:
                self.a.__dict__["_component_orig"] = self.a.__dict__["_component"]

    def __str__(self):
        return self.data["code"]

    def __dir__(self):
        return [
            "get_parents()",
            "get_children()",
            "get_components()",
            "get_contained()",
            "get_containers()",
            "add_parents()",
            "add_children()",
            "add_components()",
            "add_contained()",
            "add_containers()",
            "del_parents()",
            "del_children()",
            "del_components()",
            "del_contained()",
            "del_containers()",
            "set_parents()",
            "set_children()",
            "set_components()",
            "set_contained()",
            "set_containers()",
            "set_tags()",
            "add_tags()",
            "del_tags()",
            "add_attachment()",
            "get_attachments()",
            "download_attachments()",
            "get_files()",
            "file_list",
            "file_links",
            "rel_file_links",
            "physicalData",
            "download()",
            "download_path",
            "is_physical()",
            "symlink()",
            "is_symlink()",
            "archive()",
            "unarchive()",
            "save()",
            "delete()",
            "mark_to_be_deleted()",
            "unmark_to_be_deleted()",
            "is_marked_to_be_deleted()",
            "attrs",
            "props",
        ] + super().__dir__()

    def __setattr__(self, name, value):
        if name in ["folder"]:
            self.__dict__[name] = value
        elif name in ["p", "props"]:
            if isinstance(value, dict):
                for p in value:
                    setattr(self.__dict__["p"], p, value[p])
            else:
                raise ValueError("please provide a dictionary for setting properties")
        else:
            super(DataSet, self).__setattr__(name, value)

    @property
    def props(self):
        return self.__dict__["p"]

    @property
    def type(self):
        return self.__dict__["type"]

    @type.setter
    def type(self, type_name):
        dataset_type = self.openbis.get_dataset_type(type_name.upper())
        self.p.__dict__["_type"] = dataset_type
        self.a.__dict__["_type"] = dataset_type

    @property
    def physicalData(self):
        if "physicalData" in self.data:
            return PhysicalData(data=self.data["physicalData"])

    @property
    def linkedData(self):
        if "linkedData" in self.data:
            return LinkedData(data=self.data["linkedData"])

    @property
    def status(self):
        ds = self.openbis.get_dataset(self.permId)
        self.data["physicalData"] = ds.data["physicalData"]
        try:
            return self.data["physicalData"]["status"]
        except Exception:
            return None

    @property
    def download_path(self):
        """after ther physical datasets have been downloaded, this returns the relative path."""
        return self.__dict__.get("download_path", "")

    @property
    def _sftp_source_dir(self):
        """The SFTP directory is structured as follows:
        /SPACE/PROJECT/EXPERIMENT/permId

        For the current dataSet, this method returns the expected path
        """

        return os.path.join(self.experiment.identifier[1:], self.permId)

    def symlink(self, target_dir: str = None, replace_if_symlink_exists: bool = True):
        """replace_if_symlink_exists will replace the the target_dir
        in case it is an existing symlink
        Returns the absolute path of the symlink
        """

        if target_dir is None:
            target_dir = os.path.join(self.openbis.download_prefix, self.permId)

        target_dir_path = Path(target_dir)
        if target_dir_path.is_symlink() and replace_if_symlink_exists:
            target_dir_path.unlink()

        # create data/openbis-hostname
        os.makedirs(os.path.dirname(target_dir_path.absolute()), exist_ok=True)

        # make sure we got a mountpoint
        mountpoint_path = self.openbis.get_mountpoint()
        if mountpoint_path is None:
            try:
                mountpoint_path = self.openbis.mount()
            except ValueError as err:
                if "password" in str(err):
                    raise ValueError(
                        "openBIS instance cannot be mounted, no symlink possible"
                    )

        # construct the absolute path of our sftp source
        sftp_source_path = os.path.join(mountpoint_path, self._sftp_source_dir)

        # make sure our sftp source is really available
        # create symlink
        if os.path.exists(sftp_source_path):
            target_dir_path.symlink_to(sftp_source_path, target_is_directory=True)
            if VERBOSE:
                print(f"Symlink created: {target_dir} --> {sftp_source_path}")

            return str(target_dir_path.absolute())
        else:
            raise ValueError(
                f"Source path {sftp_source_path} does not exist, cannot create symlink"
            )

    @staticmethod
    def _file_set(target_dir: str) -> Set[str]:
        target_dir_path = Path(target_dir)
        return set(
            str(el.relative_to(target_dir_path))
            for el in target_dir_path.glob("**/*")
            if el.is_file()
        )

    def _is_symlink_or_physical(
        self,
        what: str,
        target_dir: str = None,
        expected_file_list: Optional[List[str]] = None,
    ):
        if target_dir is None:
            target_dir = os.path.join(self.openbis.download_prefix, self.permId)
        target_dir_path = Path(target_dir)

        target_file_set = self._file_set(target_dir)

        if expected_file_list is None:
            source_file_set = set(self.file_list)
        else:
            source_file_set = set(expected_file_list)

        res = source_file_set.issubset(target_file_set)
        if not res:
            return res
        elif what == "symlink":
            return target_dir_path.exists() and target_dir_path.is_symlink()
        elif what == "physical":
            return target_dir_path.exists() and not target_dir_path.is_symlink()
        else:
            raise ValueError("Unexpected error")

    is_symlink = partialmethod(
        _is_symlink_or_physical, what="symlink", expected_file_list=None
    )
    is_physical = partialmethod(_is_symlink_or_physical, what="physical")

    def archive(self, remove_from_data_store=True):
        fetchopts = {
            "removeFromDataStore": remove_from_data_store,
            "@type": "as.dto.dataset.archive.DataSetArchiveOptions",
        }
        self.archive_unarchive("archiveDataSets", fetchopts)
        if VERBOSE:
            print(f"DataSet {self.permId} archived")

    def unarchive(self):
        fetchopts = {"@type": "as.dto.dataset.unarchive.DataSetUnarchiveOptions"}
        self.archive_unarchive("unarchiveDataSets", fetchopts)
        if VERBOSE:
            print(f"DataSet {self.permId} unarchived")

    def archive_unarchive(self, method, fetchopts):
        payload = {}

        request = {
            "method": method,
            "params": [
                self.openbis.token,
                [{"permId": self.permId, "@type": "as.dto.dataset.id.DataSetPermId"}],
                dict(fetchopts),
            ],
        }
        resp = self.openbis._post_request(self._openbis.as_v3, request)
        return

    def set_properties(self, properties):
        """expects a dictionary of property names and their values.
        Does not save the dataset.
        """
        for prop in properties.keys():
            setattr(self.p, prop, properties[prop])

    set_props = set_properties

    def get_dataset_files(self, start_with=None, count=None, **properties):

        search_criteria = get_type_for_entity("dataSetFile", "search")
        search_criteria["operator"] = "AND"
        search_criteria["criteria"] = [
            {
                "criteria": [
                    {
                        "fieldName": "code",
                        "fieldType": "ATTRIBUTE",
                        "fieldValue": {
                            "value": self.permId,
                            "@type": "as.dto.common.search.StringEqualToValue",
                        },
                        "@type": "as.dto.common.search.CodeSearchCriteria",
                    }
                ],
                "operator": "OR",
                "@type": "as.dto.dataset.search.DataSetSearchCriteria",
            }
        ]

        fetchopts = get_fetchoption_for_entity("dataSetFile")

        request = {
            "method": "searchFiles",
            "params": [
                self.openbis.token,
                search_criteria,
                fetchopts,
            ],
        }
        full_url = urljoin(self._get_download_url(), dss_endpoint)
        resp = self.openbis._post_request_full_url(full_url, request)

        def create_data_frame(attrs, props, response):
            objects = response["objects"]
            parse_jackson(objects)

            attrs = [
                "dataSetPermId",
                "dataStore",
                "downloadUrl",
                "path",
                "directory",
                "fileLength",
                "checksumCRC32",
                "checksum",
                "checksumType",
            ]

            dataSetFiles = None
            if len(objects) == 0:
                dataSetFiles = DataFrame(columns=attrs)
            else:
                dataSetFiles = DataFrame(objects)
                dataSetFiles["downloadUrl"] = dataSetFiles["dataStore"].map(
                    extract_downloadUrl
                )
                dataSetFiles["checksumCRC32"] = (
                    dataSetFiles["checksumCRC32"]
                    .fillna(0.0)
                    .astype(int)
                    .map(signed_to_unsigned)
                )
                dataSetFiles["dataStore"] = dataSetFiles["dataStore"].map(extract_code)
                dataSetFiles["dataSetPermId"] = dataSetFiles["dataSetPermId"].map(
                    extract_permid
                )
            return dataSetFiles[attrs]

        return Things(
            openbis_obj=self.openbis,
            entity="dataSetFile",
            identifier_name="dataSetPermId",
            start_with=start_with,
            count=count,
            totalCount=resp.get("totalCount"),
            response=resp,
            df_initializer=create_data_frame,
        )

    def download(
        self,
        files=None,
        destination=None,
        create_default_folders=True,
        wait_until_finished=True,
        workers=10,
        linked_dataset_fileservice_url=None,
        content_copy_index=0,
    ):
        """download the files of the dataSet.

        files -- a single file or a list of files. If no files are specified, all files of a given dataset are downloaded.
        destination -- if destination is specified, files are downloaded in __current_dir__/destination/permId/ If no destination is specified, the hostname is chosen instead of destination
        create_default_folders -- by default, this download method will automatically create destination/permId/original/DEFAULT. If create_default_folders is set to False, all these folders will be ommited. Use with care and by specifying the destination folder.
        workers -- Default: 10. Files are usually downloaded in parallel, using 10 workers by default.
        wait_unitl_finished -- True. If you want to immediately continue and run the download in background, set this to False.
        """

        if files == None:
            files = self.file_list
        elif isinstance(files, str):
            files = [files]

        if destination is None:
            destination = self.openbis.download_prefix
            # destination = self.openbis.hostname

        kind = None
        if "kind" in self.data:  # openBIS 18.6.x DTO
            kind = self.data["kind"]
        elif ("type" in self.data) and (
            "kind" in self.data["type"]
        ):  # openBIS 16.5.x DTO
            kind = self.data["type"]["kind"]

        if kind in ["PHYSICAL", "CONTAINER"]:
            return self._download_physical(
                files, destination, create_default_folders, wait_until_finished, workers
            )
        elif kind == "LINK":
            if linked_dataset_fileservice_url is None:
                raise ValueError(
                    "Can't download a LINK data set without the linked_dataset_fileservice_url parameters."
                )
            return self._download_link(
                files,
                destination,
                wait_until_finished,
                workers,
                linked_dataset_fileservice_url,
                content_copy_index,
            )
        else:
            raise ValueError(f"Can't download data set of kind {kind}.")

    def _download_physical(
        self, files, destination, create_default_folders, wait_until_finished, workers
    ):
        """Download for data sets of kind PHYSICAL."""

        final_destination = ""
        if create_default_folders:
            final_destination = os.path.join(destination, self.permId)
        else:
            final_destination = destination

        self.__dict__["download_path"] = final_destination

        download_url = self._get_download_url()
        base_url = download_url + "/datastore_server/" + self.permId + "/"
        with DataSetDownloadQueue(workers=workers) as queue:
            # get file list and start download
            for filename in files:
                fi_df = self.get_dataset_files().df
                file_size = fi_df[fi_df["path"] == filename]["fileLength"].values[0]
                download_url = base_url + filename + "?sessionID=" + self.openbis.token
                download_url = quote(download_url, safe=":/?=")
                filename_dest = ""
                if create_default_folders:
                    # create original/ or original/DEFAULT subfolders
                    filename_dest = os.path.join(final_destination, filename)
                else:
                    # ignore original/ and original/DEFAULT folders that come from openBIS
                    if filename.startswith("original/"):
                        filename = filename.replace("original/", "", 1)
                    if filename.startswith("DEFAULT/"):
                        filename = filename.replace("DEFAULT/", "", 1)
                    filename_dest = os.path.join(final_destination, filename)

                queue.put(
                    [
                        download_url,
                        filename,
                        filename_dest,
                        file_size,
                        self.openbis.verify_certificates,
                        "wb",
                    ]
                )

            # wait until all files have downloaded
            if wait_until_finished:
                queue.join()

            if VERBOSE:
                print(f"Files downloaded to: {os.path.join(final_destination)}")
            return final_destination

    def _download_link(
        self,
        files,
        destination,
        wait_until_finished,
        workers,
        linked_dataset_fileservice_url,
        content_copy_index,
    ):
        """Download for data sets of kind LINK.
        Requires the microservice server to be running at the given linked_dataset_fileservice_url.
        """

        with DataSetDownloadQueue(
            workers=workers, collect_files_with_wrong_length=True
        ) as queue:

            if content_copy_index >= len(self.data["linkedData"]["contentCopies"]):
                raise ValueError("Content Copy index out of range.")
            content_copy = self.data["linkedData"]["contentCopies"][content_copy_index]

            for filename in files:
                fi_df = self.get_dataset_files().df
                file_size = fi_df[fi_df["path"] == filename]["fileLength"].values[0]

                download_url = linked_dataset_fileservice_url
                download_url += "?sessionToken=" + self.openbis.token
                download_url += "&datasetPermId=" + self.data["permId"]["permId"]
                download_url += (
                    "&externalDMSCode=" + content_copy["externalDms"]["code"]
                )
                download_url += "&contentCopyPath=" + content_copy["path"].replace(
                    "/", "%2F"
                )
                download_url += "&datasetPathToFile=" + urllib.parse.quote(filename)

                filename_dest = os.path.join(destination, self.permId, filename)

                # continue download if file is not complete - do nothing if it is
                write_mode = "wb"
                if os.path.exists(filename_dest):
                    actual_size = os.path.getsize(filename_dest)
                    if actual_size == int(file_size):
                        continue
                    elif actual_size < int(file_size):
                        write_mode = "ab"
                        download_url += "&offset=" + str(actual_size)

                queue.put(
                    [
                        download_url,
                        filename,
                        filename_dest,
                        file_size,
                        self.openbis.verify_certificates,
                        write_mode,
                    ]
                )

            if wait_until_finished:
                queue.join()

            if VERBOSE:
                print(
                    "Files downloaded to: %s" % os.path.join(destination, self.permId)
                )
            return destination, queue.files_with_wrong_length

    @property
    def folder(self):
        return self.__dict__["folder"]

    @property
    def file_list(self):
        """Returns the list of files including their directories as an array of strings.
        Folders are not listed.
        """

        if self.is_new:
            return self.files
        else:
            fl = self.get_dataset_files().df
            return fl[fl["directory"] == False]["path"].to_list()

    @property
    def file_links(self):
        """Returns a dictionary of absolute file links for every file in this dataSet.
        As the link also contains a session token (sessionID), sharing this link might be
        a security risk. When the token is no longer valid, the link will no longer work either.
        """
        if self.is_new:
            return ""
        url = self.openbis.url
        location_part = self.physicalData.location.split("/")[-1]
        token = self.openbis.token

        file_links = {}
        for filepath in self.file_list:
            quoted_filepath = urllib.parse.quote(filepath, safe="")
            file_links[filepath] = (
                "/".join([url, "datastore_server", location_part, quoted_filepath])
                + "?sessionID="
                + token
            )

        return file_links

    @property
    def rel_file_links(self):
        """Returns a dictionary of relative file links for every file in this dataSet. These relative file link can be embedded in a <img src="{rel_link}">
        element within a XML property. If the dataSet file happens to be a picture, in ELN-LIMS, the picture will be displayed inline.
        """
        if self.is_new:
            return ""
        url = self.openbis.url
        location_part = self.physicalData.location.split("/")[-1]

        rel_file_links = {}
        for filepath in self.file_list:
            quoted_filepath = urllib.parse.quote(filepath, safe="")
            rel_file_links[filepath] = "/".join(
                ["/datastore_server", location_part, quoted_filepath]
            )

        return rel_file_links

    def get_files(self, start_folder="/"):
        """Returns a DataFrame of all files in this dataset"""
        if start_folder.startswith("/"):
            start_folder = start_folder[1:]
        file_list = self.get_dataset_files().df
        file_list[file_list["path"].str.startswith(start_folder)]
        new_file_list = file_list[
            ["directory", "path", "fileLength", "checksumCRC32"]
        ].rename(
            columns={
                "directory": "isDirectory",
                "path": "pathInDataSet",
                "fileLength": "fileSize",
                "checksumCRC32": "crc32Checksum",
            }
        )
        return new_file_list

    def _get_download_url(self):
        download_url = ""
        if "downloadUrl" in self.data["dataStore"]:
            download_url = self.data["dataStore"]["downloadUrl"]
        else:
            # fallback, if there is no dataStore defined
            datastores = self.openbis.get_datastores()
            download_url = datastores["downloadUrl"][0]
        return download_url

    def get_file_list(self, recursive=True, start_folder="/"):
        """Lists all files of a given dataset. You can specifiy a start_folder other than "/".
        By default, all directories and their containing files are listed recursively. You can
        turn off this option by setting recursive=False.
        """
        print("This method is deprecated. Consider using get_files() instead")
        request = {
            "method": "listFilesForDataSet",
            "params": [
                self.openbis.token,
                self.permId,
                start_folder,
                recursive,
            ],
            "id": "1",
        }
        download_url = self._get_download_url()
        resp = requests.post(
            download_url + "/datastore_server/rmi-dss-api-v1.json",
            json.dumps(request),
            verify=self.openbis.verify_certificates,
        )

        if resp.ok:
            data = resp.json()
            if "error" in data:
                raise ValueError("Error from openBIS: " + data["error"]["message"])
            elif "result" in data:
                return data["result"]
            else:
                raise ValueError(
                    "request to openBIS did not return either result nor error"
                )
        else:
            raise ValueError("internal error while performing post request")

    def _generate_plugin_request(self, dss, permId=None):
        """generates a request to activate the dataset-uploader ingestion plugin to
        register our files as a new dataset
        """

        sample_identifier = None
        if self.sample is not None:
            sample_identifier = self.sample.identifier

        experiment_identifier = None
        if self.experiment is not None:
            experiment_identifier = self.experiment.identifier

        parentIds = self.parents
        if parentIds is None:
            parentIds = []

        dataset_type = self.type.code
        properties = self.props.all_nonempty()

        request = {
            "method": "createReportFromAggregationService",
            "params": [
                self.openbis.token,
                dss,
                PYBIS_PLUGIN,
                {
                    "permId": permId,
                    "method": "insertDataSet",
                    "sampleIdentifier": sample_identifier,
                    "experimentIdentifier": experiment_identifier,
                    "dataSetType": dataset_type,
                    "folderName": self.folder,
                    "fileNames": self.files_in_wsp,
                    "isZipDirectoryUpload": self.isZipDirectoryUpload,
                    "properties": properties,
                    "parentIdentifiers": parentIds,
                },
            ],
        }
        return request

    def save(self, permId=None):
        for prop_name, prop in self.props._property_names.items():
            if prop["mandatory"]:
                if (
                    getattr(self.props, prop_name) is None
                    or getattr(self.props, prop_name) == ""
                ):
                    raise ValueError(
                        f"Property '{prop_name}' is mandatory and must not be None"
                    )

        if self.is_new:
            datastores = self.openbis.get_datastores()

            if self.sample is None and self.experiment is None:
                raise ValueError(
                    "A DataSet must be either connected to a Sample or an Experiment"
                )

            if self.kind == "PHYSICAL":
                if self.files is None or len(self.files) == 0:
                    raise ValueError(
                        "Cannot register a dataset without a file. Please provide at least one file"
                    )

                # for uploading phyiscal data, we first upload it to the session workspace
                self.upload_files(
                    datastore_url=datastores["downloadUrl"][0],
                    files=self.files,
                    folder="",
                    wait_until_finished=True,
                )

                # activate the ingestion plugin, as soon as the data is uploaded
                # this will actually register the dataset in the datastore and the AS
                request = self._generate_plugin_request(
                    dss=datastores["code"][0],
                    permId=permId,
                )
                resp = self.openbis._post_request(self.openbis.reg_v1, request)
                if resp["rows"][0][0]["value"] == "OK":
                    permId = resp["rows"][0][2]["value"]
                    if permId is None or permId == "":
                        self.__dict__["is_new"] = False
                        if VERBOSE:
                            print(
                                "DataSet successfully created. Because you connected to an openBIS version older than 16.05.04, you cannot update the object."
                            )
                    else:
                        new_dataset_data = self.openbis.get_dataset(
                            permId, only_data=True
                        )
                        self._set_data(new_dataset_data)
                        if VERBOSE:
                            print("DataSet successfully created.")
                        return self
                else:
                    import json

                    print(json.dumps(request))
                    raise ValueError(
                        "Error while creating the DataSet: "
                        + resp["rows"][0][1]["value"]
                    )
            # CONTAINER
            else:
                if self.files is not None and len(self.files) > 0:
                    raise ValueError(
                        "DataSets of kind CONTAINER or LINK cannot contain data"
                    )

                request = self._new_attrs()

                # if no code for the container was provided, let openBIS
                # generate the code automatically
                if self.code is None or self.code == "":
                    request["params"][1][0]["autoGeneratedCode"] = True
                else:
                    request["params"][1][0]["autoGeneratedCode"] = False

                props = self.p._all_props()
                DSpermId = datastores["code"][0]
                request["params"][1][0]["properties"] = props
                request["params"][1][0]["dataStoreId"] = {
                    "permId": DSpermId,
                    "@type": "as.dto.datastore.id.DataStorePermId",
                }
                resp = self.openbis._post_request(self.openbis.as_v3, request)

                if VERBOSE:
                    print("DataSet successfully created.")
                new_dataset_data = self.openbis.get_dataset(
                    resp[0]["permId"], only_data=True
                )
                self._set_data(new_dataset_data)
                return self

        # updating the DataSEt
        else:
            request = self._up_attrs()
            props = self.p._all_props()
            request["params"][1][0]["properties"] = props

            self.openbis._post_request(self.openbis.as_v3, request)
            if VERBOSE:
                print("DataSet successfully updated.")

    def zipit(self, file_or_folder, zipf):
        """Takes a directory or a file, and a zipfile instance. For every file that is encountered,
        we issue the write() method to add that file to the zipfile.
        If we have a directory, we walk that directory and add every file inside it,
        including the starting folder name.
        """
        if os.path.isfile(file_or_folder):
            # if a file is provided, we want to always store it in the root of the zip file
            # ../../somedir/file.txt       -->   file.txt
            (realpath, filename) = os.path.split(os.path.realpath(file_or_folder))
            zipf.write(file_or_folder, filename)
        elif os.path.isdir(file_or_folder):
            # if a directory is provided, we want to store it (and its content) also in the root of the zip file
            # ../../somedir/               -->   somedir/
            # ../../somedir/other/file.txt -->   somedir/other/file.txt
            (head, tail) = os.path.split(os.path.realpath(file_or_folder))
            for dirpath, dirnames, filenames in os.walk(file_or_folder):
                realpath = os.path.realpath(dirpath)
                for filename in filenames:
                    zipf.write(
                        os.path.relpath(
                            os.path.join(dirpath, filename),
                            os.path.join(filename, ".."),
                        ),
                        os.path.join(realpath[len(head) + 1 :], filename),
                    )

    def upload_files(
        self, datastore_url=None, files=None, folder=None, wait_until_finished=False
    ):

        if datastore_url is None:
            datastore_url = self.openbis._get_dss_url()

        if files is None:
            raise ValueError("Please provide a filename.")

        if folder is None:
            # create a unique foldername
            folder = time.strftime("%Y-%m-%d_%H-%M-%S")

        if isinstance(files, str):
            files = [files]

        contains_dir = False
        for f in files:
            if os.path.isdir(f):
                contains_dir = True

        if contains_dir:
            # if the file list contains at least one directory, we need to zip the
            # whole thing in order to get it safely to openBIS.
            file_ending = "".join(
                random.choice(
                    "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                )
                for i in range(6)
            )
            filename = time.strftime("%Y-%m-%d_%H-%M-%S") + file_ending + ".zip"
            buf = ZipBuffer(
                openbis_obj=self.openbis, host=datastore_url, filename=filename
            )
            zipf = zipfile.ZipFile(file=buf, mode="w", compression=zipfile.ZIP_DEFLATED)
            for file_or_folder in files:
                self.zipit(file_or_folder, zipf)
            # self.__dict__['folder'] = '/'
            self.__dict__["files_in_wsp"] = [filename]
            self.__dict__["isZipDirectoryUpload"] = True
            return self.files_in_wsp

        # define a queue to handle the upload threads
        with DataSetUploadQueue() as queue:

            real_files = []
            for filename in files:
                if os.path.isdir(filename):
                    real_files.extend(
                        [
                            os.path.join(dp, f)
                            for dp, dn, fn in os.walk(os.path.expanduser(filename))
                            for f in fn
                        ]
                    )
                else:
                    real_files.append(os.path.join(filename))

            # compose the upload-URL and put URL and filename in the upload queue
            for filename in real_files:
                file_in_wsp = os.path.join(folder, os.path.basename(filename))
                url_filename = os.path.join(
                    folder, urllib.parse.quote(os.path.basename(filename))
                )
                self.files_in_wsp.append(file_in_wsp)

                upload_url = (
                    datastore_url
                    + "/datastore_server/session_workspace_file_upload"
                    + "?filename="
                    + url_filename
                    + "&id=1"
                    + "&startByte=0&endByte=0"
                    + "&sessionID="
                    + self.openbis.token
                )
                queue.put([upload_url, filename, self.openbis.verify_certificates])

            # wait until all files have uploaded
            if wait_until_finished:
                queue.join()

            # return files with full path in session workspace
            return self.files_in_wsp


class DataSetUploadQueue:
    def __init__(self, workers=20):
        # maximum files to be uploaded at once
        self.upload_queue = Queue()
        self.workers = workers

        # define number of threads and start them
        for t in range(workers):
            t = Thread(target=self.upload_file)
            t.start()

    def __enter__(self, *args, **kwargs):
        return self

    def __exit__(self, *args, **kwargs):
        """This method is called at the end of a with statement."""
        # stop the workers
        for i in range(self.workers):
            self.upload_queue.put(None)

    def put(self, things):
        """expects a list [url, filename] which is put into the upload queue"""
        self.upload_queue.put(things)

    def join(self):
        """needs to be called if you want to wait for all uploads to be finished"""
        # block until all tasks are done
        self.upload_queue.join()

    def upload_file(self):
        while True:
            # get the next item in the queue
            queue_item = self.upload_queue.get()
            if queue_item is None:
                # when we call the .join() method of the DataSetUploadQueue and empty the queue
                break
            upload_url, filename, verify_certificates = queue_item

            filesize = os.path.getsize(filename)

            # upload the file to our DSS session workspace
            with open(filename, "rb") as f:
                resp = requests.post(upload_url, data=f, verify=verify_certificates)
                resp.raise_for_status()
                data = resp.json()
                if filesize != int(data["size"]):
                    raise ValueError(
                        f'size of file uploaded: {filesize} != data received: {int(data["size"])}'
                    )

            # Tell the queue that we are done
            self.upload_queue.task_done()


class ZipBuffer(object):
    """A file-like object for zipfile.ZipFile to write into.
    zipfile invokes the write method to store its zipped content.
    We will send this content directly to the session_workspace as a POST request.
    """

    def __init__(self, openbis_obj, host, filename):
        self.openbis = openbis_obj
        self.startByte = 0
        self.endByte = 0
        self.filename = filename
        self.upload_url = (
            host + "/datastore_server/session_workspace_file_upload?"
            "filename={}"
            "&id=1"
            "&startByte={}"
            "&endByte={}"
            "&sessionID={}"
        )
        self.session = Session()

    def write(self, data):

        self.startByte = self.endByte
        self.endByte += len(data)
        attempts = 0

        while True:
            attempts += 1
            resp = self.session.post(
                url=self.upload_url.format(
                    self.filename, self.startByte, self.endByte, self.openbis.token
                ),
                data=data,
                verify=self.openbis.verify_certificates,
            )
            if resp.status_code == 200:
                break
            if attempts > 10:
                raise Exception("Upload failed after more than 10 attempts")

    def tell(self):
        """Return the current stream position."""
        return self.endByte

    def flush(self):
        """Flush the write buffers of the stream if applicable."""
        self.session.close()
        pass


class DataSetDownloadQueue:
    def __init__(self, workers=20, collect_files_with_wrong_length=False):
        self.collect_files_with_wrong_length = collect_files_with_wrong_length
        # maximum files to be downloaded at once
        self.workers = workers
        self.download_queue = Queue()
        self.files_with_wrong_length = []

        # define number of threads
        for i in range(workers):
            thread = Thread(target=self.download_file)
            thread.start()

    def __enter__(self, *args, **kwargs):
        return self

    def __exit__(self, *args, **kwargs):
        """This method is called at the end of a with statement."""
        # stop all workers
        for i in range(self.workers):
            self.download_queue.put(None)

    def put(self, things):
        """expects a list [url, filename] which is put into the download queue"""
        self.download_queue.put(things)

    def join(self):
        """needs to be called if you want to wait for all downloads to be finished"""
        self.download_queue.join()

    def download_file(self):
        while True:
            try:
                queue_item = self.download_queue.get()
                if queue_item is None:
                    # when we call the .join() method of the DataSetDownloadQueue and empty the queue
                    break
                (
                    url,
                    filename,
                    filename_dest,
                    file_size,
                    verify_certificates,
                    write_mode,
                ) = queue_item
                # create the necessary directory structure if they don't exist yet
                os.makedirs(os.path.dirname(filename_dest), exist_ok=True)

                # request the file in streaming mode
                r = requests.get(url, stream=True, verify=verify_certificates)
                if r.ok == False:
                    raise ValueError(
                        f"Could not download from {url}: HTTP {r.status_code}. Reason: {r.reason}"
                    )

                with open(filename_dest, write_mode) as fh:
                    for chunk in r.iter_content(chunk_size=1024 * 1024):
                        if chunk:  # filter out keep-alive new chunks
                            fh.write(chunk)

                r.raise_for_status()
                actual_file_size = os.path.getsize(filename_dest)
                if actual_file_size != int(file_size):
                    if self.collect_files_with_wrong_length:
                        self.files_with_wrong_length.append(filename)
                    else:
                        print(
                            f"WARNING! File {filename_dest} has the wrong length: Expected: {int(file_size)} Actual size: {actual_file_size}"
                        )
                        print(
                            "REASON: The connection has been silently dropped upstreams.",
                            "Please check the http timeout settings of the openBIS datastore server",
                        )
            except Exception as err:
                print(f"ERROR while writing file {filename_dest}: {err}")

            finally:
                self.download_queue.task_done()


class PhysicalData:
    def __init__(self, data=None):
        if data is None:
            data = {}
        self.data = data
        self.attrs = [
            "speedHint",
            "complete",
            "shareId",
            "size",
            "fileFormatType",
            "storageFormat",
            "location",
            "presentInArchive",
            "storageConfirmation",
            "locatorType",
            "status",
        ]

    def __dir__(self):
        return self.attrs

    def __getattr__(self, name):
        if name in self.attrs:
            if name in self.data:
                return self.data[name]

    def __getitem__(self, key):
        if key in self.attrs:
            if key in self.data:
                return self.data[key]

    def _repr_html_(self):
        html = """
            <table border="1" class="dataframe">
            <thead>
                <tr style="text-align: right;">
                <th>attribute</th>
                <th>value</th>
                </tr>
            </thead>
            <tbody>
        """

        for attr in self.attrs:
            html += f"<tr> <td>{attr}</td> <td>{getattr(self, attr, '')}</td> </tr>"

        html += """
            </tbody>
            </table>
        """
        return html

    def __repr__(self):
        headers = ["attribute", "value"]
        lines = []
        for attr in self.attrs:
            lines.append([attr, getattr(self, attr, "")])
        return tabulate(lines, headers=headers)


class LinkedData:
    def __init__(self, data=None):
        self.data = data if data is not None else []
        self.attrs = ["externalCode", "contentCopies"]

    def __dir__(self):
        return self.attrs

    def __getattr__(self, name):
        if name in self.attrs:
            if name in self.data:
                return self.data[name]
        else:
            return ""
