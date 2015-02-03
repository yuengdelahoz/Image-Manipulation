package delahoz.floor.detection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

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
	private Mat mIntermediateMat;
	private FrameProcessing FP;

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
		
		Mat img = inputFrame.rgba();
		img = FP.FindEdges(img);
		Mat lines = FP.FindLines(img);
		FP.FindWallFloorBoundary(lines, img);
		img = FP.FindFloor(img);
		//img = FP.Smooth(img);
		
		
		

		return (img);

	}

}
