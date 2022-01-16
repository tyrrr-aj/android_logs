package pl.edu.agh.student.adbreader.requests;

import java.util.List;

public class Request
{
    private int pid;
    private List<String> lines;

    public Request(int pid, List<String> lines)
    {
        this.pid = pid;
        this.lines = lines;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }

    public List<String> getLines()
    {
        return lines;
    }

    public void setLines(List<String> lines)
    {
        this.lines = lines;
    }
}
