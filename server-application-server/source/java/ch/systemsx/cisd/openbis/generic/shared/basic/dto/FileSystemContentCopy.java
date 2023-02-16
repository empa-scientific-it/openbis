/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

public class FileSystemContentCopy implements IContentCopy
{
    private static final long serialVersionUID = 1L;

    private String code;

    private String label;

    private String host;

    private String directory;

    private String path;

    private String hash;

	private String repositoryId;

    public FileSystemContentCopy()
    {
    }

    public FileSystemContentCopy(String code, String label, String host, String directory, String path, String hash, String repositoryId)
    {
        this.code = code;
        this.label = label;
        this.host = host;
        this.directory = directory;
        this.path = path;
        this.hash = hash;
        this.repositoryId = repositoryId;
    }

    @Override
    public String getLocation()
    {

        String location = repr("External DMS", code) + repr("Host", host) + repr("Directory", path);
        location += repr("Connect cmd", "ssh -t " + host + " \"cd " + path + "; bash\"");        	

        if (hash != null)
        {
            location += repr("Commit hash", hash);
        }
        if (repositoryId != null)
        {
            location += repr("Repository id", repositoryId);
        }
        return location;
    }

    private String repr(String label, String value) {
    	return "<p><b>" + label + ":</b> " + value + "</p>";
    }

    @Override
    public String getExternalDMSCode()
    {
        return this.code;
    }

    @Override
    public String getExternalDMSLabel()
    {
        return this.label;
    }

    @Override
    public String getExternalDMSAddress()
    {
        return this.host;
    }

    @Override
    public String getPath()
    {
        return this.path;
    }

    @Override
    public String getCommitHash()
    {
        return this.hash;
    }

	@Override
	public String getRespitoryId() {
		return repositoryId;
	}

    @Override
    public String getExternalCode()
    {
        return null;
    }

}
