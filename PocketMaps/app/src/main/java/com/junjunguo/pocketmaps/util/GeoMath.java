package com.junjunguo.pocketmaps.util;

public class GeoMath
{
  public final static double DEGREE_PER_METER = 0.000008993;
  public final static double METER_PER_DEGREE = 1.0/0.000008993;
  public final static double KMH_TO_MSEC = 0.2777777778;

  /** The square of x.
   *  @return x * x **/
  public static double sqr(double x) { return x * x; }
  
  /** The distance between 2 points squared.
   *  @return The squared distance **/
  public static double dist2(double v_x, double v_y, double w_x, double w_y)
  {
    return sqr(v_x - w_x) + sqr(v_y - w_y);
  }
  
  /** Calculates the estimated distance in degree. Function from GeoPoint.java! **/
  public static double fastDistance(double lat1, double lon1, double lat2, double lon2)
  {
    return Math.hypot(lon1 - lon2, lat1 - lat2);
  }
  
  /** Calculate distance to line-segment .
   *  Hint from: https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
   *  @param p_x The point.x
   *  @param p_y The point.y
   *  @param v_x The startpoint.x of line.
   *  @param v_y The startpoint.y of line.
   *  @param w_x The endpoint.x of line.
   *  @param w_y The endpoint.y of line. **/
  public static double distToLineSegment(double p_x, double p_y, double v_x, double v_y, double w_x, double w_y)
  {
    double l2 = dist2(v_x, v_y, w_x, w_y);
    if (l2 == 0) { return dist2(p_x, p_y, v_x, v_y); }
    double t = ((p_x - v_x) * (w_x - v_x) + (p_y - v_y) * (w_y - v_y)) / l2;
    t = Math.max(0, Math.min(1, t));
    double distSquared = dist2(p_x, p_y, v_x + t * (w_x - v_x), v_y + t * (w_y - v_y) );
    return Math.sqrt(distSquared);
  }
}
