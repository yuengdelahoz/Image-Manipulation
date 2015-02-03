package delahoz.floor.detection;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class FrameProcessing extends Activity {

	private final String TAG_S = "SAVING";
	private final String TAG_R = "READING";
	private ImageView iv;
	private File path;
	private ArrayList<Line> vertical_lines_left;
	private ArrayList<Line> vertical_lines_right;

	private ArrayList<Line> obliques_lines_left;
	private ArrayList<Line> obliques_lines_right;

	private ArrayList<Line> horizontal_lines;

	public FrameProcessing(ImageView iv) {
		this.iv = iv;
		path = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

	}

	public FrameProcessing() {
		path = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

	}

	public void SaveImage(Mat img, String filename) {
		File file = new File(path, filename);
		if (file.exists())
			file.delete();
		Boolean bool = null;
		filename = file.toString();
		bool = Highgui.imwrite(filename, img);

		if (bool == true) {
			DisplayImg(img);
			Log.d(TAG_S, "SUCCESS writing image to external storage");
		} else
			Log.d(TAG_S, "Fail writing image to external storage");

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

	public Mat ReadImage(String filename) {
		File file = new File(path, filename);
		Mat src = null;
		filename = file.toString();
		src = Highgui.imread(filename);
		Mat dst = new Mat();
		Imgproc.resize(src, dst, new Size(640, 480));
		if (!dst.empty()) {
			Log.d(TAG_R, "SUCCESS Reading the image");
		} else
			Log.d(TAG_R, "Fail Reading the image");
		return dst;

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
		Imgproc.Canny(gray, edges, 80, 90);

		return edges;
	}

	public Mat FindLines(Mat img) {
		Mat lines = new Mat();
		Imgproc.HoughLinesP(img, lines, 1, Math.PI / 180, 80, 10, 10);
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
			Log.i("Lines Coordinates", ""+x1+","+y1+"-"+x2+","+y2);
			double angle;

			Point endpoint1 = new Point(x1, y1);
			Point endpoint2 = new Point(x2, y2);

			Line line = new Line(endpoint1, endpoint2);
			angle = line.getAngle();
			
			

			// If angle is 90 +- 5 then line is a vertical line
			if (Math.abs(angle) - 90 > -5 && Math.abs(angle) - 90 < 5) {

				// if line endpoint is below height/2 then it is a possible line
				// that collide with floor
				if (line.getEnd().y>height/2)
					// discard line with length below 100
					if (line.getLength()> 100)
						// split lines into left and right lines
						if (endpoint1.x < width / 2) {
							vertical_lines_left.add(line);
							
						} else if (endpoint1.x > width / 2) {
							vertical_lines_right.add(line);
							
						}
				
			}

			// If angle is 45 +- 10 then line is an oblique line

			if (Math.abs(angle) - 45 > -10 && Math.abs(angle) - 45 < 10) {

				// if the line endpoint is below height/2 then it is a
				// possible wall-floor line
				if (line.getEnd().y>height/2)
					// discard line with length below 100
					if (line.getLength() > 100)
						// split lines into left and right lines
						if (endpoint1.x < width / 2) {
							obliques_lines_left.add(line);
						} else if (endpoint1.x > width / 2) {
							obliques_lines_right.add(line);
						}
			}

			if (Math.abs(angle) - 0 > -5 && Math.abs(angle) - 0 < 5) {
				
			}

		}

	}

	public Mat FindFloor(Mat img) {
		Mat imgColor =img;
		
		Iterator<Line> iter = vertical_lines_left.iterator();
		while (iter.hasNext()){
			Line temp = iter.next();
			Core.line(imgColor, temp.getStart(), temp.getEnd(), new Scalar(255, 0,
					0), 2);
		}
		
		iter = vertical_lines_right.iterator();
		while (iter.hasNext()){
			Line temp = iter.next();
			Core.line(imgColor,temp.getStart(), temp.getEnd(), new Scalar(0, 0,
					255), 2);
			Core.line(imgColor, temp.getEnd(), temp.getEnd(), new Scalar(255, 0,
					255), 10);
		}
		
		iter = obliques_lines_left.iterator();
		while (iter.hasNext()){
			Line temp = iter.next();
			Core.line(imgColor,temp.getStart(), temp.getEnd(), new Scalar(255, 255,
					0), 2);
		}
		
		iter = obliques_lines_right.iterator();
		while (iter.hasNext()){
			Line temp = iter.next();
			Core.line(imgColor,temp.getStart(), temp.getEnd(), new Scalar(0,255,
					255), 2);
		}
		return imgColor;
	}




}
