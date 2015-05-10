package delahoz.floor.detection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.example.floor_detection.R;

public class MainActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean mIsJavaCamera = true;
	Mat mrgb;
	Mat img;
	int counter = 0;
	final static Object lock = new Object();

	private FrameProcessing FP;
	int k = 0;

	double startTimeMod, startTimeFD, TimeFD, TimeMod1, TimeMod2, TimeMod3,
			TimeMod4, TimeMod5;

	double avgTimeMod1 = 0, avgTimeMod2 = 0, avgTimeMod3 = 0, avgTimeMod4 = 0,
			avgTimeMod5 = 0, avgTimeFD = 0;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FP = new FrameProcessing();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.helloopencvlayout);
		if (mIsJavaCamera) {
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
			mOpenCvCameraView.setMaxFrameSize(320, 240);

		} else
			mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView2);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {

	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// return inputFrame.gray();

		img = inputFrame.rgba();

		Thread FloorDetector = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (lock) {

					// TODO Auto-generated method stub
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
					Mat svImg = FP.FindFloor(img);
					TimeMod5 = System.currentTimeMillis() - startTimeMod;

					TimeFD = System.currentTimeMillis() - startTimeFD;
					FP.SaveImage(svImg, "P_");

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

			}
		});

		FloorDetector.start();

		return (img);

	}

}
