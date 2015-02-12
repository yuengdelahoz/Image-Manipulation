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
				File folder = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM
								+ "/Images");
				for (final File fileEntry : folder.listFiles()) {
					FP = new FrameProcessing();
					String name = fileEntry.getName();
					img = FP.ReadImage(name);
					//If I cannot read the image ignore
					if (img.empty()) continue;
					Mat edges = FP.FindEdges(img);
					Mat lines = FP.FindLines(edges);
					FP.FindWallFloorBoundary(lines, img);
					img = FP.FindFloor(edges);
//					Imgproc.cvtColor(edges,edges, Imgproc.COLOR_GRAY2RGB);
//					img = FP.drawLines(edges);
					FP.SaveImage(img, name + "_processed.png");
//					k++;
//					if (k == 50)
//						break;
				}

				// FP = new FrameProcessing();
				// img = FP.ReadImage("img.png");
				// Mat edges = FP.FindEdges(img);
				// Mat lines = FP.FindLines(edges);
				// FP.FindWallFloorBoundary(lines, img);
				// img = FP.FindFloor(img);
				// FP.SaveImage(img, "processed.png");
				// k++;
				System.exit(0);

				// Mat ones = Mat.ones(img.size(), CvType.CV_8UC3);
				// img = FP.drawLines(ones);

				// final Mat temp = FP.drawAllLines(lines, ones);
				//
				// img = FP.Smooth(img);

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