package delahoz.floor.detection;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.example.floor_detection.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class PicActivity extends Activity {
	FrameProcessing FP;
	private Mat img;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);
		
		
		 
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (status == LoaderCallbackInterface.SUCCESS) {
				ImageView  iv = (ImageView) findViewById(R.id.imageView1);
				FP = new FrameProcessing(iv);
				img = FP.ReadImage("img.png");
				img = FP.FindEdges(img);
				Mat lines = FP.FindLines(img);
				FP.FindWallFloorBoundary(lines, img);
				Log.i("Image_Type", ""+img.type());
				img = FP.FindFloor(lines);
				//img = FP.Smooth(img);
				
				Thread saver = new Thread (new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						FP.SaveImage(img, "outputImg2.png");
						
					}
				});
				saver.start();
				
				
				
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