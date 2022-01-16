package pl.edu.agh.student.adbreader.utils;

public class TableRow
{
    private long id;
    private double result;

    public TableRow(long id, double result)
    {
        this.id = id;
        this.result = result;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getResult()
    {
        if (result == -1) {
            return "N/A";
        }

        return String.valueOf(result);
    }

    public void setResult(double result)
    {
        this.result = result;
    }
}
