package pl.edu.agh.student.adbreader.utils;

public class Log
{
    private int pid;
    private long time;
    private String line;

    public Log(int pid, long time, String line)
    {
        this.pid = pid;
        this.time = time;
        this.line = line;
    }

    public int getPid()
    {
        return pid;
    }

    public long getTime()
    {
        return time;
    }

    public String getLine()
    {
        return line;
    }
}
