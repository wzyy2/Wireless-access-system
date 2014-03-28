package com.example.hello_android;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.example.thread.*;

public class PhoneFragment extends BaseFragment {

	private Button mybtn01, mybtn02, mybtn03;
	// protected Saudioserver m_player;
	// protected Saudioclient m_recorder;
	private MediaRecorder mediaRecorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View phoneLayout = inflater.inflate(R.layout.phone_layout, container,
				false);
		mybtn01 = (Button) phoneLayout.findViewById(R.id.button1);
		mybtn02 = (Button) phoneLayout.findViewById(R.id.button2);
		mybtn03 = (Button) phoneLayout.findViewById(R.id.button3);

		mybtn01.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startTranscribeAudio();
			}
		});
		mybtn02.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		mybtn03.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mediaRecorder != null) {
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
				}
			}
		});

		return phoneLayout;
	}

	private void startTranscribeAudio() {
		// �ļ�·��
		StringBuilder builder = new StringBuilder();
		builder.append(Environment.getExternalStorageDirectory().getPath())
				.append("/").append("music");
		String path = builder.toString();
		File folder = new File(path);
		// �ļ��в������򴴽�
		if (!folder.exists())
			folder.mkdirs();
		String mAudioPath = path
				+ "/"
				+ new DateFormat().format("yyyyMMdd_hhmmss",
						Calendar.getInstance(Locale.CHINA)) + ".3gp";
		File file = new File(mAudioPath);
		// ����¼������
		mediaRecorder = new MediaRecorder();
		// ����˷�Դ����¼��
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// ���������ʽ
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// ���ñ����ʽ
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mediaRecorder.setAudioSamplingRate(8000);
		mediaRecorder.setAudioChannels(1);
		// ��������ļ�
		mediaRecorder.setOutputFile(file.getAbsolutePath());
		try {
			// �����ļ�
			file.createNewFile();
			// ׼��¼��
			mediaRecorder.prepare();
			// ��ʼ¼��
			mediaRecorder.start();
		} catch (Exception e) {
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		MainActivity.curFragmentTag = getString(R.string.phone_fg);
	}

}
