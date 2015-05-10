package delahoz.floor.detection;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class FrameProcessing extends Activity {
	private static double counter = 0;
	public static int kval=0;

	private final String TAG_S = "SAVING";
	private final String TAG_R = "READING";
	private ImageView iv;
	private ArrayList<Line> vertical_lines_left;
	private ArrayList<Line> vertical_lines_right;
	private Line temp1 = null, temp2 = null;

	private ArrayList<Line> obliques_lines_left;
	private ArrayList<Line> obliques_lines_right;

	private ArrayList<Line> horizontal_lines;

	public FrameProcessing(ImageView iv) {
		this.iv = iv;

	}

	private void upCounter() {
		counter++;
	}

	public static double getCounter() {
		return counter;
	}

	public FrameProcessing() {
	}

	public Mat ReadImage(File path, String name) {
	
		File file = new File(path, name);
		Mat src = null;
		String filename = file.toString();
		src = Highgui.imread(filename, Highgui.CV_LOAD_IMAGE_COLOR);
		int width = src.cols();
		int height = src.rows();

		/*if (width < 640 / 2 && height < 480 / 2)
			return new Mat();
		if ((width * height) != 76800)
			Imgproc.resize(src, src, new Size(320, 240));
*/
		if (!src.empty()) {
			Log.d(TAG_R, "SUCCESS Reading the image " + name);
		} else
			Log.d(TAG_R, "Fail Reading the image " + name);
		return src;

	}

	public void SaveImage(Mat img, String name) {
		File ph = new File("/sdcard/Floor"
						+ "/Output");
		Mat img2 = new Mat(img.size(), img.type());
		String val="";
		if (kval<10)
			val ="000"+kval;
		else if (kval>=10 && kval<100 )
			val="00"+kval;
		else if (kval>=100 && kval <1000)
			val="0"+kval;
		else
			val=""+kval;
		String filename = name+val+".png";
		kval++;
		File file = new File(ph, filename);
		if (file.exists())
			file.delete();
		Boolean bool = null;
		String filenm = file.toString();
		Imgproc.cvtColor(img, img2, Imgproc.COLOR_RGB2BGR);
		bool = Highgui.imwrite(filenm, img);

		if (bool == true) {
			Log.d(TAG_S, "SUCCESS writing image " + name);
		} else
			Log.d(TAG_S, "Fail writing image");

	}

	public void DisplayImg(Mat img) {
		// convert to bitmap:
		final Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(img, bm);

		// find the imageview and draw it!
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				iv.setImageBitmap(bm);

			}
		});

	}

	public Mat Smooth(Mat src) {
		Mat gray = new Mat();
		Mat dst = new Mat();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(gray, dst, new Size(5, 5), 0);

		return dst;
	}

	public Mat FindEdges(Mat src) {
		Mat gray = new Mat();
		Mat edges = new Mat();

		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Canny(gray, edges, 30, 90);
		// Imgproc.dilate(edges, edges, new Mat());
		// Imgproc.erode(edges,edges, new Mat());
		return edges;
	}

	public Mat FindLines(Mat edges) {
		Mat lines = new Mat();
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 50, 10);
		//Imgproc.HoughLines(edges, lines, 1,  Math.PI / 180, 50);
	
		return lines;
	}

	public void FindWallFloorBoundary(Mat lines, Mat img) {

		int width = img.cols();
		int height = img.rows();

		vertical_lines_left = new ArrayList<Line>();
		vertical_lines_right = new ArrayList<Line>();

		obliques_lines_left = new ArrayList<Line>();
		obliques_lines_right = new ArrayList<Line>();
		horizontal_lines = new ArrayList<Line>();

		for (int x = 0; x < lines.cols(); x++) {

			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			double angle;

			Point endpoint1 = new Point(x1, y1);
			Point endpoint2 = new Point(x2, y2);

			Line line = new Line(endpoint1, endpoint2);
			double theta = line.getAngle();
			// Log.i("Angle", theta + "");

			angle = Math.abs(theta);

			// If angle is 90 +- 5 then line is a vertical line
			if (angle - 90 > -10 && angle - 90 < 10) {

				// if line endpoint is below height/2 then it is a possible line
				// that collide with floor
				if (line.getEnd().y > height / 3)

					// split lines into left and right lines
					if (line.getStart().x < width / 2) {
						vertical_lines_left.add(line);
					} else if (line.getStart().x > width / 2) {
						vertical_lines_right.add(line);
					}

			}

			// If angle is 45 +- 10 then line is an oblique line

			if (angle - 45 > -20 && angle - 45 < 20) {

				// if the line endpoint is below height/2 then it is a
				// possible wall-floor line
				if (line.getEnd().y > height / 2)

					// split lines into left and right lines
					if (line.getStart().x < width / 2) {
						if (theta < 0)
							obliques_lines_left.add(line);
					} else if (line.getStart().x > width / 2) {
						if (theta > 0)
							obliques_lines_right.add(line);
					}
			}

			if ((angle - 0 > -5 && angle - 0 < 5)
					|| (angle - 180 > -5 && angle - 180 < 5)) {
				if (line.getEnd().y > height / 3
						&& line.getEnd().y < 2 * height / 3)
					horizontal_lines.add(line);
			}

		}
	}

	public Mat FindFloorM(Mat img) {
		Mat imgColor = img;

		if (img.type() == 0) {
			Imgproc.cvtColor(img, imgColor, Imgproc.COLOR_GRAY2RGB);
		}
		if (!obliques_lines_left.isEmpty()) {

			Line theone = getCandidates(obliques_lines_left).get(0);
			Core.line(imgColor, theone.getStart(), theone.getEnd(), new Scalar(
					0, 0, 255), 2);

		}

		if (!obliques_lines_right.isEmpty()) {
			Line theone = getCandidates(obliques_lines_right).get(0);
			Core.line(imgColor, theone.getStart(), theone.getEnd(), new Scalar(
					0, 0, 255), 2);

		}
		return imgColor;
	}

	public Mat FindFloor(Mat img) {
		Mat imgColor = img;

		if (img.type() == 0) {
			Imgproc.cvtColor(img, imgColor, Imgproc.COLOR_GRAY2RGB);
		}

		int height = img.rows();
		int width = img.cols();
		Point center = new Point(width / 2, height / 2);

		if (!horizontal_lines.isEmpty()) {
			horizontal_lines = getCandidates(horizontal_lines);
			Line theone = horizontal_lines.get(0);
			Point left;
			Point right;
			Point CornerL;
			Point CornerR;
			if (theone.getStart().x < theone.getEnd().x) {
				left = theone.getStart();
				right = theone.getEnd();
			} else {
				right = theone.getStart();
				left = theone.getEnd();
			}
			if (left.x < width / 2 && right.x > width / 2) {
				CornerL = new Point(0, height - 1);
				CornerR = new Point(width - 1, height - 1);

				temp1 = new Line(left, CornerL);
				temp2 = new Line(right, CornerR);
				// Core.line(imgColor, theone.getStart(), theone.getEnd(), new
				// Scalar(
				// 0, 255, 0), 3);
			}

		}

		if (!vertical_lines_left.isEmpty()) {
			ArrayList<Line> candidates = getCandidates(vertical_lines_left);
			Line theone = candidates.get(0);
			double m = -1.4;
			double b = theone.getEnd().y - (m) * theone.getEnd().x;
			int y = height / 2;

			int x = (int) Math.ceil(((y - b) / m));

			Point midPoint = new Point(x, y);
			int y1 = height - 1;
			int x1 = (int) ((y1 - b) / m);
			if (x1 < 0) {
				x1 = 0;
				y1 = (int) b;
			}
			Point lastPoint = new Point(x1, y1);

			// Core.line(imgColor, theone.getStart(), theone.getEnd(), new
			// Scalar(
			// 255, 0, 0), 3);
			temp1 = new Line(midPoint, lastPoint);

		}

		if (!vertical_lines_right.isEmpty()) {
			ArrayList<Line> candidates = getCandidates(vertical_lines_right);
			int canSize = candidates.size();
			Line theone = candidates.get(0);
			double m = 1.4;
			double b = theone.getEnd().y - (m) * theone.getEnd().x;
			int y = height / 2;

			int x = (int) (((y - b) / m));

			Point midPoint = new Point(x, y);
			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));
			if (x1 > width) {
				x1 = width - 1;
				y1 = (int) (m * x1 + b);
			}
			Point lastPoint = new Point(x1, y1);

			// Core.line(imgColor, theone.getStart(), theone.getEnd(), new
			// Scalar(
			// 255, 0, 0), 3);
			temp2 = new Line(midPoint, lastPoint);

		}

		if (!obliques_lines_left.isEmpty()) {
			obliques_lines_left = getCandidates(obliques_lines_left);
			Line theone = null;
			theone = obliques_lines_left.get(0);
			int c = 0;
			for (int i = 0; i < obliques_lines_left.size(); i++) {

				if (obliques_lines_left.get(i).distanceTocenter(center) < theone
						.distanceTocenter(center))
					theone = obliques_lines_left.get(i);
				c++;
				if (c == 3)
					break;
			}
			double b = theone.yIntercept();
			double m = theone.getSlope();

			Point midPoint = null;
			if (theone.getStart().y < height / 2) {

				int y = height / 2;
				int x = (int) (((y - b) / m));

				midPoint = new Point(x, y);
			} else {
				midPoint = theone.getStart();
			}

			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));

			if (x1 < 0) {
				x1 = 0;
				y1 = (int) b;
			}

			Point lastPoint = new Point(x1, y1);

			// Core.line(imgColor, theone.getStart(), theone.getEnd(), new
			// Scalar(
			// 0, 0, 255), 3);
			temp1 = new Line(midPoint, lastPoint);

		}

		if (!obliques_lines_right.isEmpty()) {
			obliques_lines_right = getCandidates(obliques_lines_right);
			Line theone = obliques_lines_right.get(0);
			int c = 0;
			for (int i = 0; i < obliques_lines_right.size(); i++) {
				double curD = obliques_lines_right.get(i).distanceTocenter(
						center);
				double oD = theone.distanceTocenter(center);

				if (curD < oD)
					theone = obliques_lines_right.get(i);
				c++;
				if (c == 3)
					break;
			}
			double b = theone.yIntercept();
			double m = theone.getSlope();

			Point midPoint = null;
			if (theone.getStart().y < height / 2) {

				int y = height / 2;
				int x = (int) (((y - b) / m));

				midPoint = new Point(x, y);
			} else {
				midPoint = theone.getStart();
			}

			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));
			if (x1 > width) {
				x1 = width - 1;
				y1 = (int) (m * x1 + b);
			}
			Point lastPoint = new Point(x1, y1);
			// Core.line(imgColor, theone.getStart(), theone.getEnd(), new
			// Scalar(
			// 0, 0, 255), 3);
			temp2 = new Line(midPoint, lastPoint);

		}

		if (temp1 != null && temp2 != null) {
			Core.line(imgColor, temp1.getStart(), temp1.getEnd(), new Scalar(
					255, 255, 255), 5);
			Core.line(imgColor, temp2.getStart(), temp2.getEnd(), new Scalar(
					255, 255, 255), 5);
			Core.line(imgColor, temp1.getStart(), temp2.getStart(), new Scalar(
					255, 255, 255), 5);
			upCounter();

		}
		return imgColor;
	}

	public Mat drawLines(Mat img) {
		Mat imgColor = img;

		if (img.type() == 0) {
			Imgproc.cvtColor(img, imgColor, Imgproc.COLOR_GRAY2RGB);
		}

		Iterator<Line> iter = vertical_lines_left.iterator();
		while (iter.hasNext()) {
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(255,
					0, 0), 2);
		}

		iter = vertical_lines_right.iterator();
		while (iter.hasNext()) {
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(255,
					0, 0), 2);
		}

		iter = obliques_lines_left.iterator();
		while (iter.hasNext()) {
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(0,
					0, 255), 2);
		}

		iter = obliques_lines_right.iterator();
		while (iter.hasNext()) {
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(0,
					0, 255), 2);
		}
		iter = horizontal_lines.iterator();
		while (iter.hasNext()) {
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(0,
					255, 255), 2);
		}

		return imgColor;

	}

	public Mat drawAllLines(Mat lines, Mat imgColor) {

		for (int x = 0; x < lines.cols(); x++) {

			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			Point endpoint1 = new Point(x1, y1);
			Point endpoint2 = new Point(x2, y2);

			Line temp = new Line(endpoint1, endpoint2);
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(255,
					255, 255), 2);
		}
		// Imgproc.cvtColor(imgColor, imgColor, Imgproc.COLOR_BGR2GRAY);
		// Core.bitwise_not(imgColor, imgColor);
		return imgColor;

	}

	private ArrayList<Line> getCandidates(ArrayList<Line> lines) {
		Sorting S = new Sorting();
		// Sort Lines based on their lenghts
		ArrayList<Line> Sortedlines = S.HeapSort(lines);
		int ArraySize = Sortedlines.size();
		boolean k = false;
		// Discard lines that are not as long as the longest line
		ArrayList<Line> temp = new ArrayList<Line>();
		int c = 0;
		for (int i = ArraySize - 1; i >= 0; i--) {
			temp.add(Sortedlines.get(i));
		}
		return temp;
	}

}
