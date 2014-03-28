package com.example.hello_android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HomeFragment extends BaseFragment {
	private ToggleButton togglebtn;
	private TextView hometext;
	public boolean doorsta=false;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View homeLayout = inflater.inflate(R.layout.home_layout,
				container, false);	
		
		hometext = (TextView) homeLayout.findViewById(R.id.textView2);
		togglebtn = (ToggleButton) homeLayout.findViewById(R.id.toggleButton1);		
		togglebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doorsta = !doorsta;
				if(doorsta)
					hometext.setText("OPEN");
				else
					hometext.setText("CLOSED");
			}
		});
		
		return homeLayout;
	}

	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MainActivity.curFragmentTag = getString(R.string.home_fg);
	}
	
	
}
