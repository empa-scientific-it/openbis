import json
import os
from http.server import BaseHTTPRequestHandler, HTTPServer
from threading import Thread

import pytest
from pybis.fast_download import FastDownload


def get_download_response(sequence_number, perm_id, file, is_directory, offset, payload):
    # binascii.crc32(byte_array[:end])
    import binascii
    result = b''
    result += sequence_number.to_bytes(4, "big")
    download_item_id = perm_id + "/" + file
    result += len(download_item_id).to_bytes(2, "big")
    result += is_directory.to_bytes(1, "big")
    result += len(file).to_bytes(2, "big")
    # file offset here
    result += int(offset).to_bytes(8, "big")
    # payload length here
    payload_length = len(payload)
    result += int(payload_length).to_bytes(4, "big")
    result += download_item_id.encode()
    result += file.encode()
    result += binascii.crc32(result).to_bytes(8, "big")
    result += payload
    result += binascii.crc32(payload).to_bytes(8, "big")
    return result


class MyServer(BaseHTTPRequestHandler):
    """Message handler implementation for faking server. It is BaseHTTPRequestHandler is stateless
    so communication happens via static variable"""
    next_response = {}
    response_code = 200

    def do_POST(self):
        content_len = int(self.headers.get('Content-Length'))
        post_body = self.rfile.read(content_len)
        body = json.loads(post_body.decode("utf8"))

        method = body['method']
        if type(method) == list:
            method = method[0]
        if method == "download":
            response = next(MyServer.next_response['download'])
        else:
            response = MyServer.next_response[method].encode()
        self.send_response(MyServer.response_code)
        self.end_headers()
        self.wfile.write(response)


def createFastDownloadSession(permId, files, download_url, wished_number_of_streams):
    return '''{ "jsonrpc": "2.0", "id": "2", "result": {
        "@type": "dss.dto.datasetfile.fastdownload.FastDownloadSession", "@id": 1,
        "downloadUrl": "''' + download_url + '''",
        "fileTransferUserSessionId": "admin-230317133255245x9E432E4241A58D972496ACD02AC95BC9",
        "files": [  { "@type": "dss.dto.datasetfile.id.DataSetFilePermId", "@id": 2,
                "dataSetId": { "@type": "as.dto.dataset.id.DataSetPermId", "@id": 3,
                    "permId": "''' + permId + '''"
                },  "filePath": "''' + permId + "/" + files + '''"  } ],
        "options": {
            "@type": "dss.dto.datasetfile.fastdownload.FastDownloadSessionOptions", "@id": 4,
            "wishedNumberOfStreams": ''' + wished_number_of_streams + ''' } } }'''


def startDownloadSession(ranges, wished_number_of_streams):
    return """{
        "downloadSessionId": "72863f8d-1ed1-4795-a531-4d93a5081562",
        "ranges": {
           """ + ", ".join([f'"{k}": "{v}"' for k, v in ranges.items()]) + """
        },
        "streamIds": [""" + ", ".join([f'"s{x}"' for x in range(int(wished_number_of_streams))]) \
        + """] }"""


def cleanup(directory):
    import shutil
    for files in os.listdir(directory):
        path = os.path.join(directory, files)
        try:
            shutil.rmtree(path)
        except OSError:
            os.remove(path)


@pytest.fixture(scope="session")
def base_data(tmp_path_factory):
    folder = tmp_path_factory.mktemp("temp_folder")
    port = 12354
    url = "127.0.0.1"
    download_url = "http://" + url + ":" + str(port)

    httpserver = HTTPServer((url, port), MyServer)
    Thread(target=httpserver.serve_forever, daemon=True).start()
    return folder, download_url


@pytest.fixture(autouse=True)
def run_around_tests(base_data):
    temp_folder = base_data[0]
    download_url = base_data[1]
    streams = "2"
    perm_id = "1"
    file = "original/test_file.txt"
    ranges = {
        "1/original/test_file.txt": "0:9"
    }
    MyServer.next_response = {
        'queue': "",
        'finishDownloadSession': "",
        'counter': 0,
        'parts': 10,
        'createFastDownloadSession': createFastDownloadSession(perm_id,
                                                               file,
                                                               download_url,
                                                               streams),
        'startDownloadSession': startDownloadSession(ranges, streams)
    }
    MyServer.response_code = 200
    yield temp_folder, download_url, streams, perm_id, file
    cleanup(temp_folder)


def test_download_fails_after_retry(run_around_tests):
    temp_folder, download_url, streams, perm_id, file = run_around_tests

    def generate_download_response():
        while True:
            yield b''

    MyServer.next_response['download'] = generate_download_response()

    fast_download = FastDownload("", download_url, perm_id, file, str(temp_folder),
                                 True, True, False, streams)
    try:
        fast_download.download()
        assert False
    except ValueError as error:
        assert str(error) == 'Reached maximum retry count:3. Aborting.'


def test_download_file(run_around_tests):
    temp_folder, download_url, streams, perm_id, file = run_around_tests

    def generate_download_response():
        parts = MyServer.next_response['parts']
        counter = MyServer.next_response['counter']
        payload_length = 10
        while counter < parts:
            response = get_download_response(counter, perm_id, file, False,
                                             counter * payload_length,
                                             bytearray([counter] * payload_length))
            counter += 1
            MyServer.next_response['counter'] = counter % parts
            yield response

    MyServer.next_response['download'] = generate_download_response()

    fast_download = FastDownload("", download_url, perm_id, file, str(temp_folder),
                                 True, True, False, streams)
    fast_download.download()

    downloaded_files = [
        os.path.join(dp, f)
        for dp, dn, fn in os.walk(temp_folder)
        for f in fn
    ]
    assert len(downloaded_files) == 1
    assert downloaded_files[0].endswith(file)
    import functools
    expected_outcome = functools.reduce(lambda a, b: a + b,
                                        [bytearray([x] * 10) for x in range(10)])
    with open(downloaded_files[0], 'rb') as fn:
        data = fn.read()
        assert len(data) == 100
        assert expected_outcome == data


def test_download_file_starts_with_fail(run_around_tests):
    temp_folder, download_url, streams, perm_id, file = run_around_tests

    def generate_download_response():
        MyServer.response_code = 502
        yield b''
        yield b''
        MyServer.response_code = 200
        parts = MyServer.next_response['parts']
        counter = MyServer.next_response['counter']
        payload_length = 10
        while counter < parts:
            response = get_download_response(counter, perm_id, file, False,
                                             counter * payload_length,
                                             bytearray([counter] * payload_length))
            counter += 1
            MyServer.next_response['counter'] = counter % parts
            yield response

    MyServer.next_response['download'] = generate_download_response()

    fast_download = FastDownload("", download_url, perm_id, file, str(temp_folder),
                                 True, True, False, streams)
    fast_download.download()

    downloaded_files = [
        os.path.join(dp, f)
        for dp, dn, fn in os.walk(temp_folder)
        for f in fn
    ]
    assert len(downloaded_files) == 1
    assert downloaded_files[0].endswith(file)
    import functools
    expected_outcome = functools.reduce(lambda a, b: a + b,
                                        [bytearray([x] * 10) for x in range(10)])
    with open(downloaded_files[0], 'rb') as fn:
        data = fn.read()
        assert len(data) == 100
        assert expected_outcome == data