#   Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
import base64
import os


class Attachment:
    def __init__(self, filename, title=None, description=None):
        if not os.path.exists(filename):
            raise ValueError(f"File not found: {filename}")
        self.fileName = filename
        self.title = title
        self.description = description

    def get_data_short(self):
        return {
            "fileName": self.fileName,
            "title": self.title,
            "description": self.description,
        }

    def get_data(self):
        with open(self.fileName, "rb") as att:
            content = att.read()
            contentb64 = base64.b64encode(content).decode()
        return {
            "fileName": self.fileName,
            "title": self.title,
            "description": self.description,
            "content": contentb64,
            "@type": "as.dto.attachment.create.AttachmentCreation",
        }
