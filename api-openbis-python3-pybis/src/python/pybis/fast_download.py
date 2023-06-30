#   Copyright ETH 2023 Zürich, Scientific IT Services
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
import binascii
import functools
import json
import os
import time
from pathlib import Path
from threading import Lock, Thread
from urllib.parse import urljoin

import requests
from requests.adapters import HTTPAdapter, Retry

DSS_V3 = "/datastore_server/rmi-data-store-server-v3.json"
REQUEST_RETRIES_COUNT = 3
DOWNLOAD_RETRIES_COUNT = 3


def make_fileserver_body_params(**params):
    """create a proper pam of key-values for fileserver request"""
    result = {}
    for key, value in params.items():
        result[str(key)] = [str(value)]
    return result


def comma_separated_items(arr):
    """Create comma-separated string from the list of items"""
    return functools.reduce(lambda a, b: a + ", " + b, arr)


def create_session(download_url_base):
    """Create a session object to handle retries in case of server failure"""
    session = requests.Session()
    retries = Retry(total=REQUEST_RETRIES_COUNT, backoff_factor=1, status_forcelist=[502, 503, 504])
    session.mount(download_url_base, HTTPAdapter(max_retries=retries))
    return session


def post_request(session, full_url, verify_certificates, request, parse_response=True):
    """Perform POST call to server"""
    try:
        if request:
            resp = session.post(full_url, json.dumps(request), verify=verify_certificates)
        else:
            resp = session.post(full_url, verify=verify_certificates)
    except requests.exceptions.SSLError as exc:
        raise requests.exceptions.SSLError(
            "Certificate validation failed. Use o=Openbis(url, verify_certificates=False) if you are using self-signed certificates."
        ) from exc
    except requests.ConnectionError as exc:
        raise requests.ConnectionError(
            "Could not connecto to the openBIS server. Please check your internet connection, the specified hostname and port."
        ) from exc
    if resp.ok:
        if parse_response is True:
            resp = resp.json()
            if "error" in resp:
                print(json.dumps(resp))
                raise ValueError(resp["error"]["message"])
        return resp
    else:
        raise ValueError("general error while performing post request")


def queue_chunks(session, base_url, download_session_id, chunks, verify_certificates):
    """Queue particular session chunks for download"""
    queue_request = make_fileserver_body_params(method='queue',
                                                downloadSessionId=download_session_id,
                                                ranges=comma_separated_items(chunks))
    response = post_request(session, base_url, verify_certificates, queue_request,
                            parse_response=False)
    if response.ok is False:
        raise ValueError(
            "error during queueing for download! Message:" + response["error"]["message"])


def deserialize_chunk(byte_array):
    sequence_number_bytes = 4
    download_item_id_length_bytes = 2
    is_directory_bytes = 1
    file_path_length_bytes = 2
    file_offset_bytes = 8
    payload_length_bytes = 4
    sent_header_checksum_bytes = 8
    sent_payload_checksum_bytes = 8

    result = {
        "invalid": False,
        'invalid_reason': ""
    }

    if len(byte_array) == 0:
        result['invalid'] = True
        result['invalid_reason'] = "HEADER"
        return result

    start, end = 0, sequence_number_bytes
    result['sequence_number'] = int.from_bytes(byte_array[start:end], "big")
    start, end = end, end + download_item_id_length_bytes
    download_item_id_length = int.from_bytes(byte_array[start:end], "big")
    start, end = end, end + is_directory_bytes
    result['is_directory'] = bool.from_bytes(byte_array[start:end], "big")
    start, end = end, end + file_path_length_bytes
    file_path_length = int.from_bytes(byte_array[start:end], "big")
    start, end = end, end + file_offset_bytes
    result['file_offset'] = int.from_bytes(byte_array[start:end], "big")
    start, end = end, end + payload_length_bytes
    result['payload_length'] = int.from_bytes(byte_array[start:end], "big")
    start, end = end, end + download_item_id_length
    result['download_item_id'] = byte_array[start:end].decode("utf8")
    start, end = end, end + file_path_length
    result['file_path'] = byte_array[start:end].decode("utf8")
    calculated_header_checksum = binascii.crc32(byte_array[:end])
    # End of header
    start, end = end, end + sent_header_checksum_bytes
    sent_header_checksum = int.from_bytes(byte_array[start:end], "big")
    if sent_header_checksum != calculated_header_checksum:
        result['invalid'] = True
        result['invalid_reason'] = "HEADER"
        return result
    start, end = end, end + result['payload_length']
    result['payload'] = byte_array[start:end]
    calculated_payload_checksum = binascii.crc32(byte_array[start:end])
    # End of payload
    start, end = end, end + sent_payload_checksum_bytes
    sent_payload_checksum = int.from_bytes(byte_array[start:end], "big")
    if sent_payload_checksum != calculated_payload_checksum:
        result['invalid'] = True
        result['invalid_reason'] = "PAYLOAD"
        return result

    return result


class AtomicChecker:
    """Helper class for keeping watch of chunks to download"""

    def __init__(self, values_to_download: set):
        self._value = 0
        self._max = len(values_to_download)  # limit to not have an infinite download sessions
        self._set = values_to_download
        self._lock = Lock()

    def should_continue(self):
        with self._lock:
            if self._value >= self._max:
                return False
            self._value += 1
            return True

    def repeat_call(self):
        with self._lock:
            self._max += 1

    def break_count(self):
        with self._lock:
            self._max = 0

    def remove_value(self, value):
        with self._lock:
            if value in self._set:
                self._set.remove(value)

    def get_remaining_values(self):
        return self._set


def _get_json(response):
    try:
        return True, response.json()
    except:
        return False, response


class DownloadThread(Thread):
    """Helper class defining single stream download"""

    def __init__(self, session, download_url_base, download_session_id, stream_id,
                 counter: AtomicChecker, verify_certificates, create_default_folders, destination,
                 number_of_chunks=1):
        Thread.__init__(self)
        self.session = session
        self.download_url = download_url_base
        self.download_session_id = download_session_id
        self.stream_id = stream_id
        self.counter = counter
        self.number_of_chunks = number_of_chunks
        self.create_default_folders = create_default_folders
        self.destination = destination
        self.verify_certificates = verify_certificates
        self.exc = None

    def run(self):
        repeated_chunks = {}
        download_params = make_fileserver_body_params(method='download',
                                                      downloadSessionId=self.download_session_id,
                                                      numberOfChunks=self.number_of_chunks,
                                                      downloadStreamId=self.stream_id)
        retry_counter = 0
        while self.counter.should_continue():
            try:
                download_response = self.session.post(self.download_url,
                                                      data=json.dumps(download_params), stream=True,
                                                      verify=self.verify_certificates)
                if download_response.ok is True:
                    data = deserialize_chunk(download_response.content)
                    if data['invalid'] is True:
                        is_json, response = _get_json(download_response)
                        if is_json:
                            if 'retriable' in response and response['retriable'] is False:
                                self.counter.break_count()
                                raise ValueError(response["error"])
                        else:
                            if data['invalid_reason'] == "PAYLOAD":
                                sequence_number = data['sequence_number']
                                if repeated_chunks.get(sequence_number, 0) >= DOWNLOAD_RETRIES_COUNT:
                                    self.counter.break_count()
                                    raise ValueError(
                                        "Received incorrect payload multiple times. Aborting.")
                                repeated_chunks[sequence_number] = repeated_chunks.get(sequence_number,
                                                                                       0) + 1
                                queue_chunks(self.session, self.download_url,
                                             self.download_session_id,
                                             [f"{sequence_number}:{sequence_number}"],
                                             self.verify_certificates)
                                self.counter.repeat_call()  # queue additional download chunk run

                        if retry_counter >= REQUEST_RETRIES_COUNT:
                            self.counter.break_count()
                            raise ValueError("Consecutive download calls to the server failed.")

                        # Exponential backoff for the consecutive failures
                        time.sleep(2 ** retry_counter)
                        retry_counter += 1

                    else:
                        retry_counter = 0
                        sequence_number = data['sequence_number']
                        self.save_to_file(data)
                        self.counter.remove_value(sequence_number)
            except Exception as e:
                self.exc = e
        return True

    def save_to_file(self, deserialized_response):
        file_name = deserialized_response['file_path']
        if self.create_default_folders:
            # create original/ or original/DEFAULT subdirectories
            filename_dest = os.path.join(self.destination, file_name)
        else:
            # ignore original/ and original/DEFAULT folders that come from openBIS
            if file_name.startswith("original/"):
                file_name = file_name.replace("original/", "", 1)
            if file_name.startswith("DEFAULT/"):
                file_name = file_name.replace("DEFAULT/", "", 1)
            filename_dest = os.path.join(self.destination, file_name)

        # create the necessary directory structure if they don't exist yet
        os.makedirs(os.path.dirname(filename_dest), exist_ok=True)

        if deserialized_response['is_directory'] is False:
            # create file if it does not exist already
            Path(filename_dest).touch(exist_ok=True)

            file_offset = deserialized_response['file_offset']
            with open(filename_dest, "rb+") as file:
                file.seek(file_offset)
                file.write(deserialized_response['payload'])


class FastDownload:
    """Class for downloading data using FastDownload scheme"""

    def __init__(
            self,
            token,
            download_url,
            perm_id,
            files,
            destination,
            create_default_folders,
            wait_until_finished,
            verify_certificates,
            wished_number_of_streams=4
    ):
        self.dss_facade_url = urljoin(download_url, DSS_V3)
        self.session = create_session(download_url)
        self.token = token
        self.verify_certificates = verify_certificates
        self.perm_id = perm_id
        self.destination = destination
        self.create_default_folders = create_default_folders
        self.wait_until_finished = wait_until_finished
        self.wished_number_of_streams = wished_number_of_streams

        if files is None:
            raise ValueError("please provide at least one file")

        if isinstance(files, str):
            files = [files]

        self.files = files

    def download(self):
        """Fast download of files from dataset"""

        if self.token is None:
            raise ValueError("Your session expired, please log in again")

        # Step 1 - request DSS Facade to create fast download session in fileserver

        create_fast_download_response = \
            post_request(self.session, self.dss_facade_url, self.verify_certificates,
                         self._create_fast_download_session_request())['result']
        download_url = create_fast_download_response['downloadUrl']
        user_session_id = create_fast_download_response['fileTransferUserSessionId']

        # Step 2 - Request fileserver to start the download session

        files_str = comma_separated_items(
            map(lambda file: file['filePath'], create_fast_download_response['files']))
        start_session_params = make_fileserver_body_params(method="startDownloadSession",
                                                           userSessionId=user_session_id,
                                                           downloadItemIds=files_str,
                                                           wishedNumberOfStreams=self.wished_number_of_streams)

        start_download_session = post_request(self.session, download_url,
                                              self.verify_certificates,
                                              start_session_params)
        download_session_id = start_download_session['downloadSessionId']


        # Step 3 - Put files into fileserver download queue

        ranges = start_download_session['ranges']
        self._queue_all_files(download_url, download_session_id, ranges)

        # Step 4 & 5 - Download files in chunks and close connection

        session_stream_ids = list(start_download_session['streamIds'])

        exception_list = []
        thread = Thread(target=self._download_step,
                        args=(download_url, download_session_id, session_stream_ids, ranges,
                              exception_list))
        thread.start()

        if self.wait_until_finished is True:
            thread.join()
            if exception_list:
                raise exception_list[0]

        return self.destination

    def _create_fast_download_session_request(self):
        file_ids = list(
            map(lambda file_path: self._make_json_id(file_path), self.files))

        fast_download_session_options = {
            "wishedNumberOfStreams": self.wished_number_of_streams,
            "@type": "dss.dto.datasetfile.fastdownload.FastDownloadSessionOptions",
        }

        return {
            "id": "2",
            "jsonrpc": "2.0",
            "method": "createFastDownloadSession",
            "params": [self.token, file_ids, fast_download_session_options]
        }

    def _make_json_id(self, file_path):
        """Prepare JSON to create session for fileserver for given file in dataset"""
        return {
            "dataSetId": {
                "permId": self.perm_id,
                "@type": "as.dto.dataset.id.DataSetPermId"
            },
            "filePath": self.perm_id + "/" + file_path,
            "@type": "dss.dto.datasetfile.id.DataSetFilePermId"
        }

    def _queue_all_files(self, base_url, download_session_id, ranges):
        """
        queue all chunks for download from fileserver, each file receives different chunk range
        FileA: 0:4
        FileB: 5:6
        """
        chunks = []
        for file, chunks_range in ranges.items():
            chunks += [chunks_range]
        queue_chunks(self.session, base_url, download_session_id, chunks,
                     self.verify_certificates)

    def _download_step(self, download_url, download_session_id, session_stream_ids, ranges,
                       exception_list):
        """
        Perform downloading of chunks in separate threads
        :param download_url: url to use for downloading data
        :param download_session_id: download session id
        :param session_stream_ids: list of available streams
        :param ranges: ranges provided for files
        :return: nothing
        """
        min_chunk = min(map(lambda x: int(x.split(":")[0]), ranges.values()))
        max_chunk = max(map(lambda x: int(x.split(":")[1]), ranges.values()))
        chunks_to_download = set(range(min_chunk, max_chunk + 1))

        counter = 1
        try:
            while True:  # each iteration will create threads for streams
                checker = AtomicChecker(chunks_to_download)
                streams = [
                    DownloadThread(self.session, download_url, download_session_id, stream_id, checker,
                                   self.verify_certificates, self.create_default_folders,
                                   self.destination) for stream_id in session_stream_ids]

                for thread in streams:
                    thread.start()
                for thread in streams:
                    thread.join()

                if chunks_to_download == set():  # if there are no more chunks to download
                    break
                else:
                    if counter >= DOWNLOAD_RETRIES_COUNT:
                        print(f"Reached maximum retry count:{counter}. Aborting.")
                        exception_list += [
                            ValueError(f"Reached maximum retry count:{counter}. Aborting.")]
                        break
                    exceptions = [stream.exc for stream in streams if stream.exc is not None]
                    if exceptions:
                        print(f"Download failed with message: {exceptions[0]}")
                        exception_list += exceptions
                        break
                    counter += 1
                    # queue chunks that failed to download in the previous pass
                    queue_chunks(self.session, download_url, download_session_id,
                                 [f"{x}:{x}" for x in chunks_to_download],
                                 self.verify_certificates)
        finally:
            # Step 5 - Close the session
            finish_download_session_params = make_fileserver_body_params(
                method='finishDownloadSession',
                downloadSessionId=download_session_id)

            self.session.post(download_url,
                              data=json.dumps(finish_download_session_params),
                              verify=self.verify_certificates)
