package com.example.thread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class Saudioclient extends Thread {

	protected AudioRecord m_in_rec;
	protected int m_in_buf_size;
	protected byte[] m_in_bytes;
	protected boolean m_keep_running;
	protected Socket s;
	protected DataOutputStream dout;
	protected LinkedList<byte[]> m_in_q;

	public void run() {
		try {
			byte[] bytes_pkg;
			m_in_rec.startRecording();
			while (m_keep_running) {
				m_in_rec.read(m_in_bytes, 0, m_in_buf_size);
				bytes_pkg = m_in_bytes.clone();
				if (m_in_q.size() >= 2) {
					dout.write(m_in_q.removeFirst(), 0,
							m_in_q.removeFirst().length);
				}
				m_in_q.add(bytes_pkg);
			}

			m_in_rec.stop();
			m_in_rec = null;
			m_in_bytes = null;
			dout.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		m_in_buf_size = AudioRecord.getMinBufferSize(5000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_8BIT);

		m_in_rec = new AudioRecord(MediaRecorder.AudioSource.MIC, 5000,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_8BIT, m_in_buf_size);

		m_in_bytes = new byte[m_in_buf_size];

		m_keep_running = true;
		m_in_q = new LinkedList<byte[]>();

		try {
			s = new Socket("192.168.23.1", 3333);
			dout = new DataOutputStream(s.getOutputStream());
			// new Thread(R1).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void free() {
		m_keep_running = false;
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Log.d("sleep exceptions...\n", "");
		}
	}
}
