package easy;

import java.util.HashMap;
import java.util.Scanner;

class Def
{

    public final int _id;
    public final String _name;
    public final double _lon;
    public final double _lat;

    public Def(int id, String name, double longi, double lat)
    {
        this._id = id;
        this._name = name;
        this._lon = Math.toRadians(longi);
        this._lat = Math.toRadians(lat);
    }

    @Override
    public String toString()
    {
        return this._id + ":" + this._name + ":[" + this._lon + "," + this._lat + "]";
    }

}

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution10
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        String LON = in.next();
        in.nextLine();
        String LAT = in.next();
        in.nextLine();
        int N = in.nextInt();
        in.nextLine();

        HashMap<Integer, Def> map = new HashMap<Integer, Def>();

        for (int i = 0; i < N; i++)
        {
            String DEFIB = in.nextLine();
            String[] toParse = DEFIB.split(";");
            int id = Integer.parseInt(toParse[0]);
            String name = toParse[1];
            double dlon = Double.parseDouble(toParse[4].replace(',', '.'));
            double dlat = Double.parseDouble(toParse[5].replace(',', '.'));
            map.put(id, new Def(id, name, dlon, dlat));
        }
        // System.err.println(map);

        double lon = Math.toRadians(Double.parseDouble(LON.replace(',', '.')));
        double lat = Math.toRadians(Double.parseDouble(LAT.replace(',', '.')));

        double dmin = Double.MAX_VALUE;
        int idmin = -1;

        for (Def d : map.values())
        {
            double x = (lon - d._lon) * Math.cos((lat + d._lat) / 2);
            double y = lat - d._lat;
            double dist = Math.sqrt(x * x + y * y) * 6371;

            if (dist < dmin)
            {
                dmin = dist;
                idmin = d._id;
            }
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
        System.err.println(idmin);
        System.out.println(map.get(idmin)._name);
    }
}
