package delahoz.floor.detection;

import org.opencv.core.Point;

public class Line {
	
	private double length;
	private double angle;
	private Point start;
	private Point end;
	
	public Line(Point endp1, Point endp2){
		
		if (endp1.y<endp2.y){
			start = endp1;
			end = endp2;
		}
		else {
			start = endp2;
			end = endp1;
		}
		calculateAngle();
		calculateLength();
		
	}
	
	public double getAngle(){
		return angle;
	}
	
	public double getLength(){
		return length;
	}
	
	private void calculateAngle(){
		double dy = (end.y - start.y);
		double dx = (end.x - start.x);
		
		if (dx == 0) {
			angle = 90;
		} else {
			double m = dy / dx;
			angle = Math.toDegrees(Math.atan(m));

		}
	}
	
	private void calculateLength(){
		double fterm = Math.pow(end.x - start.x, 2);
		double sterm = Math.pow(end.y - start.y, 2);
		length = Math.sqrt(fterm + sterm);
	}
	
	public Point getStart(){
		return start;
	}

	public Point getEnd(){
		return end;
	}
	
	@Override
	public String toString(){
		
		return length+"";
	}
}
