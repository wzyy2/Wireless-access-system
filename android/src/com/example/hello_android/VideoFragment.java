package com.example.hello_android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import com.example.hello_android.MainActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;

public class VideoFragment extends BaseFragment implements
		SurfaceHolder.Callback, Camera.PreviewCallback {

	private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
	private Camera mCamera = null; // Camera对象，相机预览
	/** 服务器地址 */
	private String pUsername = "XZY";
	/** 服务器地址 */
	private String serverUrl = "192.168.1.100";
	/** 服务器端口 */
	private int serverPort = 3333;
	/** 视频刷新间隔 */
	private int VideoPreRate = 1;
	/** 当前视频序号 */
	private int tempPreRate = 0;
	/** 视频质量 */
	private int VideoQuality = 85;
	/** 发送视频宽度比例 */
	private float VideoWidthRatio = 1;
	/** 发送视频高度比例 */
	private float VideoHeightRatio = 1;
	/** 发送视频宽度 */
	private int VideoWidth = 320;
	/** 发送视频高度 */
	private int VideoHeight = 240;
	/** 视频格式索引 */
	private int VideoFormatIndex = 0;
	/** 是否发送视频 */
	private boolean startSendVideo = false;
	/** 是否连接主机 */
	private boolean connectedServer = false;

	private Button myBtn01, myBtn02;
	private MainActivity mMainActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = (MainActivity) getActivity();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View videoLayout = inflater.inflate(R.layout.video_layout, container,
				false);
		mMainActivity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mSurfaceview = (SurfaceView) videoLayout
				.findViewById(R.id.surfaceView1);
		myBtn01 = (Button) videoLayout.findViewById(R.id.button1);
		myBtn02 = (Button) videoLayout.findViewById(R.id.button2);
		LayoutParams lp = mSurfaceview.getLayoutParams();
		lp.width = 320;
		lp.height =240;
		mSurfaceview.setLayoutParams(lp);
		

		return videoLayout;
	}

	@Override
	public void onStart()// 重新启动的时候
	{
		mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		mSurfaceHolder.addCallback(this); // SurfaceHolder加入回调接口
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 设置显示器类型，setType必须设置
		// 读取配置文件
		SharedPreferences preParas = PreferenceManager
				.getDefaultSharedPreferences(mMainActivity);
		pUsername = preParas.getString("Username", "XZY");
		serverUrl = preParas.getString("ServerUrl", "192.168.0.100");
		String tempStr = preParas.getString("ServerPort", "8888");
		serverPort = Integer.parseInt(tempStr);
		tempStr = preParas.getString("VideoPreRate", "1");
		VideoPreRate = Integer.parseInt(tempStr);
		tempStr = preParas.getString("VideoQuality", "85");
		VideoQuality = Integer.parseInt(tempStr);
		tempStr = preParas.getString("VideoWidthRatio", "100");
		VideoWidthRatio = Integer.parseInt(tempStr);
		tempStr = preParas.getString("VideoHeightRatio", "100");
		VideoHeightRatio = Integer.parseInt(tempStr);
		VideoWidthRatio = VideoWidthRatio / 100f;
		VideoHeightRatio = VideoHeightRatio / 100f;

		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		MainActivity.curFragmentTag = getString(R.string.video_fg);
		InitCamera();

	}

	/** 初始化摄像头 */
	private void InitCamera() {
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			if (mCamera != null) {
				mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		if (mCamera == null) {
			return;
		}
		 
		mCamera.stopPreview();
		mCamera.setPreviewCallback(this);
		mCamera.setDisplayOrientation(90); // 设置横行录制
		// 获取摄像头参数
		Camera.Parameters parameters = mCamera.getParameters();
		Size size = parameters.getPreviewSize();
		VideoWidth = size.width;
		VideoHeight = size.height;
//		Log.i("1", String.format("%d", VideoWidth));
//		Log.i("1", String.format("%d", VideoHeight));
		VideoFormatIndex = parameters.getPreviewFormat();
		mCamera.startPreview();
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		// 如果没有指令传输视频，就先不传
		if (!startSendVideo)
			return;
		if (tempPreRate < VideoPreRate) {
			tempPreRate++;
			return;
		}
		tempPreRate = 0;
		try {
			if (data != null) {
				YuvImage image = new YuvImage(data, VideoFormatIndex,
						VideoWidth, VideoHeight, null);
				if (image != null) {
					ByteArrayOutputStream outstream = new ByteArrayOutputStream();
					// 在此设置图片的尺寸和质量
					image.compressToJpeg(new Rect(0, 0,
							(int) (VideoWidthRatio * VideoWidth),
							(int) (VideoHeightRatio * VideoHeight)),
							VideoQuality, outstream);
					outstream.flush();
					// 启用线程将图像数据发送出去
					Thread th = new MySendFileThread(outstream, pUsername,
							serverUrl, serverPort);
					th.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (null != mCamera) {
			mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	

	/** 发送命令线程 */
	class MySendCommondThread extends Thread {
		private String commond;

		public MySendCommondThread(String commond) {
			this.commond = commond;
		}

		public void run() {
			// 实例化Socket
			try {
				Socket socket = new Socket(serverUrl, serverPort);
				PrintWriter out = new PrintWriter(socket.getOutputStream());
				out.println(commond);
				out.flush();
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}
	}


	/** 发送文件线程 */
	class MySendFileThread extends Thread {
		private String username;
		private String ipname;
		private int port;
		private byte byteBuffer[] = new byte[1024];
		private OutputStream outsocket;
		private ByteArrayOutputStream myoutputstream;

		public MySendFileThread(ByteArrayOutputStream myoutputstream,
				String username, String ipname, int port) {
			this.myoutputstream = myoutputstream;
			this.username = username;
			this.ipname = ipname;
			this.port = port;
			try {
				myoutputstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				// 将图像数据通过Socket发送出去
				Socket tempSocket = new Socket(ipname, port);
				outsocket = tempSocket.getOutputStream();
				// 写入头部数据信息
				String msg = java.net.URLEncoder.encode("PHONEVIDEO|"
						+ username + "|", "utf-8");
				byte[] buffer = msg.getBytes();
				outsocket.write(buffer);

				ByteArrayInputStream inputstream = new ByteArrayInputStream(
						myoutputstream.toByteArray());
				int amount;
				while ((amount = inputstream.read(byteBuffer)) != -1) {
					outsocket.write(byteBuffer, 0, amount);
				}
				myoutputstream.flush();
				myoutputstream.close();
				tempSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
