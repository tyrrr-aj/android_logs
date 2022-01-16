package pl.edu.agh.student.adbreader.requests;

public class Response
{
    private int idx;
    private double score;

    public Response(int idx, double score)
    {
        this.idx = idx;
        this.score = score;
    }

    public int getIdx()
    {
        return idx;
    }

    public void setIdx(int idx)
    {
        this.idx = idx;
    }

    public double getScore()
    {
        return score;
    }

    public void setScore(double score)
    {
        this.score = score;
    }
}
