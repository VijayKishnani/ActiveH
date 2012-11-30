package com.example.activehistorian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.hardware.Camera;
import android.media.MediaPlayer.*;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.FileInputStream;



public class MainActivity extends Activity implements SurfaceHolder.Callback, OnInfoListener, OnErrorListener{

	private Button record;
	private Button stop;
	private Button upload;
	private TextView recordingstatus;
	private VideoView videoview;
	private SurfaceHolder holder;
	private Camera camera;
	private MediaRecorder recorder;
	private String outputFileName;
	
	final static private String APP_KEY = "le2ytsy64bt80ix";
	final static private String APP_SECRET = "mtpgb0cdzzp6hj4";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	private DropboxAPI<AndroidAuthSession> mDBApi;
	
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		record = (Button) findViewById(R.id.Record);
		stop = (Button) findViewById(R.id.Stop);
		upload = (Button) findViewById(R.id.dropbox);
		recordingstatus = (TextView) findViewById(R.id.Status);
		videoview = (VideoView) findViewById(R.id.Video);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		

		
		upload.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
				AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
				mDBApi = new DropboxAPI<AndroidAuthSession>(session);
				
				mDBApi.getSession().startAuthentication(MainActivity.this);
				
			    if (mDBApi.getSession().authenticationSuccessful()) {
			        try {
			            // MANDATORY call to complete auth.
			            // Sets the access token on the session
			            mDBApi.getSession().finishAuthentication();

			            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

			            // Provide your own storeKeys to persist the access token pair
			            // A typical way to store tokens is using SharedPreferences
			            storeKeys(tokens.key, tokens.secret);
			        } catch (IllegalStateException e) {
			            Log.i("DbAuthLog", "Error authenticating", e);
			        } catch (NullPointerException e) {
			        	Log.i("NPE", "NullPointerException", e);
			        }
			        
			    }
			    
			    String fullPath = "/Users/panayiotisstylianou/Desktop";
			    File tmpFile = new File(fullPath, "download.jpg");
				
				FileInputStream inputStream = null;
				try {
				    inputStream = new FileInputStream(tmpFile);
	                DropboxAPI.Entry newEntry = mDBApi.putFileOverwrite("download.jpg", inputStream, tmpFile.length(), null);
				    Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
				} catch (DropboxUnlinkedException e) {
				    // User has unlinked, ask them to link again here.
				    Log.e("DbExampleLog", "User has unlinked.");
				} catch (DropboxException e) {
				    Log.e("DbExampleLog", "Something went wrong while uploading.");
				} catch (FileNotFoundException e) {
				    Log.e("DbExampleLog", "File not found.");
				} finally {
				    if (inputStream != null) {
				        try {
				            inputStream.close();
				        } catch (IOException e) {}
				    }
				}
				
				
			}
			
			
			
			
		
		});
		record.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
				recordingstatus.setText("Recording");
				init();
				recorder.start();
				
				
			}});
		
		stop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				recordingstatus.setText("Not Recording");
				if(recorder != null){
					try{recorder.stop();} 
					catch (IllegalStateException e){}
					releaseRecorder();
					releaseCamera();
				}
			}
		});
	}

	private void releaseCamera() {
		if(camera != null){
			try{
				camera.reconnect();
			}catch(IOException e){}
			camera.release();
			camera = null;
		}
		
	}
	
	private void releaseRecorder() {
		if(recorder != null){
			recorder.release();
			recorder = null;
		}
	}

	public void init(){
		if(recorder != null){ return;}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentDateandTime = sdf.format(new Date());
		outputFileName = Environment.getExternalStorageDirectory() + "/" + currentDateandTime +".mp4";
		File outfile  = new File(outputFileName);
		
		try{
			camera.stopPreview();
			camera.unlock();
			recorder = new MediaRecorder();
			recorder.setCamera(camera);
			recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			recorder.setVideoSize(175, 144);
			recorder.setVideoFrameRate(15);
			recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setMaxDuration(360000);
			recorder.setPreviewDisplay(holder.getSurface());
			recorder.setOutputFile(outputFileName);
			recorder.prepare();
		}catch(Exception e){}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {return false;}

	@Override
	public void onInfo(MediaRecorder arg0, int arg1, int arg2) {}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try{
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch(IOException e){}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}
	
	@Override
	protected void onResume(){
		super.onResume();


		if(!initCamera()){finish();}
	}
	
    private void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
	private boolean initCamera() {
		try{
			camera = Camera.open();
			Camera.Parameters camParams = camera.getParameters();
			camera.lock();
			holder = videoview.getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (RuntimeException e){return false;}
		

		return true;
	}
}
