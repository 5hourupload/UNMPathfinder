package fhu.unmpathway;

public class Node
{
    int xPosition;
    int yPosition;
    double pathDistance;
    double heuristicDistance;
    double weight;
    int xOrigin;
    int yOrigin;

    public Node(int x, int y, double pDist, int xN, int yN, int x0, int y0)
    {
        xPosition = x;
        yPosition = y;
        pathDistance = pDist;
        heuristicDistance = Math.sqrt(Math.pow(xN - x, 2) + Math.pow(yN - y, 2));
        yOrigin = y0;
        xOrigin = x0;
        weight = pathDistance + heuristicDistance;
    }
}
