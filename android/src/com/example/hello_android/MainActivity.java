package com.example.hello_android;

import com.example.hello_android.BaseFragment;
import com.example.hello_android.HomeFragment;
import com.example.hello_android.VideoFragment;
import com.example.hello_android.PhoneFragment;
import com.example.hello_android.R;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;





public class MainActivity extends FragmentActivity implements OnClickListener{

	private static final String TAG = "MainActivity";	
	
	public  HomeFragment homeFragment;

	private PhoneFragment phoneFragment;
	
	private VideoFragment videoFragment;


	private View homeLayout ,phoneLayout, videoLayout;

	private ImageView homeImage,phoneImage,videoImage;
	
	private TextView homeText,phoneText,videoText,menuText;
	private ImageButton menubtn;
	

	/**
	 * ���ڶ�Fragment���й���
	 */
	private FragmentManager fragmentManager;
	private FragmentTransaction mFragmentTransaction;

	
	public boolean connectsta=false;

	
	
			
			
	public static  String curFragmentTag;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// ��ʼ������Ԫ��
		initViews();
		menubtn = (ImageButton) findViewById(R.id.imageButton1);
		menuText = (TextView) findViewById(R.id.textView1);
		menubtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connectsta = !connectsta;
				if(connectsta)
					menuText.setText("��ǰ����״̬��"+"������");
				else
					menuText.setText("��ǰ����״̬��"+"δ����");
			}
		});
		fragmentManager = getSupportFragmentManager();
		// ��һ������ʱĬ��ѡ�е�һ��tab
		setCurrentFragment();
	}
	
	
	private void initViews() {
		homeLayout = findViewById(R.id.home_layout);
		phoneLayout = findViewById(R.id.phone_layout);
		videoLayout = findViewById(R.id.video_layout);
		homeImage = (ImageView) findViewById(R.id.home_image);
		phoneImage = (ImageView) findViewById(R.id.phone_image);
		videoImage = (ImageView) findViewById(R.id.video_image);
		homeText = (TextView) findViewById(R.id.home_text);
		phoneText = (TextView) findViewById(R.id.phone_text);
		videoText = (TextView) findViewById(R.id.video_text);
		homeLayout.setOnClickListener(this);
		phoneLayout.setOnClickListener(this);
		videoLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_layout:
			// ���������Ϣtabʱ��ѡ�е�1��tab
			setTabSelection(getString(R.string.home_fg));
			break;
		case R.id.phone_layout:
			// ���������ϵ��tabʱ��ѡ�е�2��tab
			setTabSelection(getString(R.string.phone_fg));
			break;
		case R.id.video_layout:
			// ������˶�̬tabʱ��ѡ�е�3��tab
			setTabSelection(getString(R.string.video_fg));
			break;
		default:
			break;
		}
	}

	private void setCurrentFragment(){
		clearSelection();
		mFragmentTransaction = fragmentManager.beginTransaction();
		homeImage.setImageResource(R.drawable.message_selected);
		homeText.setTextColor(Color.WHITE);
		if (homeFragment == null) {
			// ���MessageFragmentΪ�գ��򴴽�һ������ӵ�������
			homeFragment = new HomeFragment();
			//transaction.add(0, messageFragment);
			mFragmentTransaction.add(R.id.content, homeFragment,getString(R.string.home_fg));
			commitTransactions();
		}
		curFragmentTag = getString(R.string.home_fg);
	}
	
	
	/**
	 * ���ݴ����tag����������ѡ�е�tabҳ��
	 * 
	 * @param tag
	 *          
	 */
	public  void setTabSelection(String tag) {
		// ÿ��ѡ��֮ǰ��������ϴε�ѡ��״̬
		clearSelection();
		// ����һ��Fragment����
		mFragmentTransaction = fragmentManager.beginTransaction();
		Log.e("setTagSele", "currentTag"+curFragmentTag+"-----tag----"+tag);
		 if(TextUtils.equals(tag, getString(R.string.home_fg))){
			// ���������Ϣtabʱ���ı�ؼ���ͼƬ��������ɫ
			homeImage.setImageResource(R.drawable.message_selected);
			homeText.setTextColor(Color.WHITE);
			
			if (homeFragment == null) {
				homeFragment = new HomeFragment();
			} 
			
		}else if(TextUtils.equals(tag, getString(R.string.phone_fg))){
			phoneImage.setImageResource(R.drawable.contacts_selected);
			phoneText.setTextColor(Color.WHITE);
			Log.e(TAG, "contact");
			if (phoneFragment == null) {
				phoneFragment = new PhoneFragment();
			} 
			
		}else if(TextUtils.equals(tag, getString(R.string.video_fg))){
			videoImage.setImageResource(R.drawable.news_selected);
			videoText.setTextColor(Color.WHITE);
			if (videoFragment == null) {
				videoFragment = new VideoFragment();
			}
			
		}
		 switchFragment(tag);
		 
	}

	public  void switchFragment(String tag){
		if(TextUtils.equals(tag, curFragmentTag)){
			Log.e("switchFragment", "curTag == tag");
			return;
		}
		
		if(curFragmentTag != null){
			detachFragment(getFragment(curFragmentTag));
			
		}
		attachFragment(R.id.content,getFragment(tag),tag);
		curFragmentTag = tag;
		Log.e(" after switchFrag", "currenttag--->"+curFragmentTag);
		commitTransactions();
	} 
	
	private void detachFragment(Fragment f){
		
		if(f != null && !f.isDetached()){
			Log.d("detachFragment-->", f.getTag());
			ensureTransaction();
			mFragmentTransaction.detach(f);
		}
	}
	
	private FragmentTransaction ensureTransaction( ){
		if(mFragmentTransaction == null){
			mFragmentTransaction = fragmentManager.beginTransaction();
			mFragmentTransaction
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			
		}
		return mFragmentTransaction;
		
	}
	
	private void attachFragment(int layout, Fragment f,String tag){
		if(f != null){
			if(f.isDetached()){
				ensureTransaction();
				mFragmentTransaction.attach(f);
				
			}else if(!f.isAdded()){
				ensureTransaction();
				mFragmentTransaction.add(layout,f, tag);
			}
		}
	}
	
	private void commitTransactions(){
		if (mFragmentTransaction != null && !mFragmentTransaction.isEmpty()) {
			mFragmentTransaction.commit();
			mFragmentTransaction = null;
		}
	}
	private Fragment getFragment(String tag){
		
		Fragment f = fragmentManager.findFragmentByTag(tag);
		
		if(f == null){
//			Toast.makeText(getApplicationContext(), "fragment == null", Toast.LENGTH_SHORT).show();		
			f = BaseFragment.newInstance(getApplicationContext(), tag);
		}
		return f;
		
	}
	
	
	/**
	 * ��������е�ѡ��״̬��
	 */
	private void clearSelection() {
		homeImage.setImageResource(R.drawable.message_unselected);
		homeText.setTextColor(Color.parseColor("#82858b"));
		phoneImage.setImageResource(R.drawable.contacts_unselected);
		phoneText.setTextColor(Color.parseColor("#82858b"));
		videoImage.setImageResource(R.drawable.news_unselected);
		videoText.setTextColor(Color.parseColor("#82858b"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
