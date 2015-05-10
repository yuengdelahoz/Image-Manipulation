package delahoz.floor.detection;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.example.floor_detection.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class PicActivity extends Activity {
	FrameProcessing FP;
	private Mat img;
	double startTimeMod, startTimeFD, TimeFD, TimeMod1, TimeMod2, TimeMod3,
			TimeMod4, TimeMod5;

	double avgTimeMod1 = 0, avgTimeMod2 = 0, avgTimeMod3 = 0, avgTimeMod4 = 0,
			avgTimeMod5 = 0, avgTimeFD = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);

	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (status == LoaderCallbackInterface.SUCCESS) {

				int k = 0;

				File folder = new File("/sdcard/Floor" + "/Holster 01");
				while (true) {
					for (final File fileEntry : folder.listFiles()) {
						FP = new FrameProcessing();

						String name = fileEntry.getName();
						img = FP.ReadImage(folder, name);
						// If I cannot read the image ignore
						if (img.empty())
							continue;

						startTimeFD = System.currentTimeMillis();

						startTimeMod = System.currentTimeMillis();
						Mat smot = FP.Smooth(img);
						TimeMod1 = System.currentTimeMillis() - startTimeMod;

						startTimeMod = System.currentTimeMillis();
						Mat edges = FP.FindEdges(img);
						TimeMod2 = System.currentTimeMillis() - startTimeMod;

						startTimeMod = System.currentTimeMillis();
						Mat lines = FP.FindLines(edges);
						TimeMod3 = System.currentTimeMillis() - startTimeMod;

						startTimeMod = System.currentTimeMillis();
						FP.FindWallFloorBoundary(lines, img);
						TimeMod4 = System.currentTimeMillis() - startTimeMod;

						startTimeMod = System.currentTimeMillis();
						img = FP.FindFloor(img);
						TimeMod5 = System.currentTimeMillis() - startTimeMod;

						TimeFD = System.currentTimeMillis() - startTimeFD;

						FP.SaveImage(edges, "P_" + name);
						k++;
						avgTimeMod1 += TimeMod1;
						avgTimeMod2 += TimeMod2;
						avgTimeMod3 += TimeMod3;
						avgTimeMod4 += TimeMod4;
						avgTimeMod5 += TimeMod5;
						avgTimeFD += TimeFD;

						Log.i("TimeMod", "TimeMod1 " + TimeMod1);
						Log.i("TimeMod", "TimeMod2 " + TimeMod2);
						Log.i("TimeMod", "TimeMod3 " + TimeMod3);
						Log.i("TimeMod", "TimeMod4 " + TimeMod4);
						Log.i("TimeMod", "TimeMod5 " + TimeMod5);
						Log.i("TimeFD", "Time Floor Detection " + TimeFD);

					}

					avgTimeMod1 /= k;
					avgTimeMod2 /= k;
					avgTimeMod3 /= k;
					avgTimeMod4 /= k;
					avgTimeMod5 /= k;
					avgTimeFD /= k;

					Log.i("avgTimeMod", "avgTimeMod1 " + avgTimeMod1);
					Log.i("avgTimeMod", "avgTimeMod2 " + avgTimeMod2);
					Log.i("avgTimeMod", "avgTimeMod3 " + avgTimeMod3);
					Log.i("avgTimeMod", "avgTimeMod4 " + avgTimeMod4);
					Log.i("avgTimeMod", "avgTimeMod5 " + avgTimeMod5);

					Log.i("avgTimeFD", "avgTimeFD " + avgTimeFD);
				}

			} else {
				super.onManagerConnected(status);
			}
		}
	};
	private String TAG = "Saving";

	@Override
	public void onResume() {
		super.onResume();
		// you may be tempted, to do something here, but it's *async*, and may
		// take some time,
		// so any opencv call here will lead to unresolved native errors.

	}

}

// File folder = Environment
// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM
// + "/Paper Pictures");
//
// FP = new FrameProcessing();
// String name = "corridor.png";
// img = FP.ReadImage(folder,name);
// //If I cannot read the image ignore
// Mat edges = FP.FindEdges(img);
// Mat lines = FP.FindLines(edges);
// FP.FindWallFloorBoundary(lines, img);
// Mat ones =Mat.ones(img.size(), CvType.CV_8UC1);
// Mat img1 = FP.FindFloor(ones);
// Imgproc.cvtColor(img1,img1, Imgproc.COLOR_BGR2GRAY);
// Core.bitwise_not(img1, img1);
// FP.SaveImage(img1, "Floor_"+name );