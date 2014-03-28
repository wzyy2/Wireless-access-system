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

	private SurfaceView mSurfaceview = null; // SurfaceView����(��ͼ���)��Ƶ��ʾ
	private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder����(����ӿ�)SurfaceView֧����
	private Camera mCamera = null; // Camera�������Ԥ��
	/** ��������ַ */
	private String pUsername = "XZY";
	/** ��������ַ */
	private String serverUrl = "192.168.1.100";
	/** �������˿� */
	private int serverPort = 3333;
	/** ��Ƶˢ�¼�� */
	private int VideoPreRate = 1;
	/** ��ǰ��Ƶ��� */
	private int tempPreRate = 0;
	/** ��Ƶ���� */
	private int VideoQuality = 85;
	/** ������Ƶ���ȱ��� */
	private float VideoWidthRatio = 1;
	/** ������Ƶ�߶ȱ��� */
	private float VideoHeightRatio = 1;
	/** ������Ƶ���� */
	private int VideoWidth = 320;
	/** ������Ƶ�߶� */
	private int VideoHeight = 240;
	/** ��Ƶ��ʽ���� */
	private int VideoFormatIndex = 0;
	/** �Ƿ�����Ƶ */
	private boolean startSendVideo = false;
	/** �Ƿ��������� */
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
	public void onStart()// ����������ʱ��
	{
		mSurfaceHolder = mSurfaceview.getHolder(); // ��SurfaceView��ȡ��SurfaceHolder����
		mSurfaceHolder.addCallback(this); // SurfaceHolder����ص��ӿ�
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// ������ʾ�����ͣ�setType��������
		// ��ȡ�����ļ�
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

	/** ��ʼ������ͷ */
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
				mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
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
		mCamera.setDisplayOrientation(90); // ���ú���¼��
		// ��ȡ����ͷ����
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
		// ���û��ָ�����Ƶ�����Ȳ���
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
					// �ڴ�����ͼƬ�ĳߴ������
					image.compressToJpeg(new Rect(0, 0,
							(int) (VideoWidthRatio * VideoWidth),
							(int) (VideoHeightRatio * VideoHeight)),
							VideoQuality, outstream);
					outstream.flush();
					// �����߳̽�ͼ�����ݷ��ͳ�ȥ
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
			mCamera.setPreviewCallback(null); // �������������ǰ����Ȼ�˳�����
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	

	/** ���������߳� */
	class MySendCommondThread extends Thread {
		private String commond;

		public MySendCommondThread(String commond) {
			this.commond = commond;
		}

		public void run() {
			// ʵ����Socket
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


	/** �����ļ��߳� */
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
				// ��ͼ������ͨ��Socket���ͳ�ȥ
				Socket tempSocket = new Socket(ipname, port);
				outsocket = tempSocket.getOutputStream();
				// д��ͷ��������Ϣ
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