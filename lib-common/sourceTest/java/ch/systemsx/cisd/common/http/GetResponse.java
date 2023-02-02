package ch.systemsx.cisd.common.http;

public class GetResponse
{
    private int status;

    private String content;

    public GetResponse(int status, String content)
    {
        this.status = status;
        this.content = content;

    }

    public int getStatus()
    {
        return status;
    }

    public String getContent()
    {
        return content;
    }
}
