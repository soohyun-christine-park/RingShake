package com.ringshake;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ringshake.R;
import com.ringshake.soundfile.CheapSoundFile;

public class ManualVibeActivity extends Activity implements
OnClickListener, OnCompletionListener {

	Bitmap musicWhite;
	Bitmap musicBlue;
	int c = 0;
	Beeper beep;
	ArrayList<Long> checkVibe = new ArrayList<Long>();
	long [] pattern;
	Vibrator CustomVibe;
	SensorManager sm;
	SensorEventListener acc;
	Sensor accSensor;
	TextView x,y,z;
	TextView last_time;
	private float speed; 
	private long startTime = 0;
	private long endtime;
	private long lastTime;
	private float lastX;
	private float lastY;
	private float lastZ;
	private Button mPlayButton;
	private Button mPlayVibe;
	private Button mVibeBeep;
	private Button mSet;
	private boolean mIsPlaying;     
	private MediaPlayer mPlayer;
	private boolean mWasGetContentIntent;
	private String mFilename;
	private CheapSoundFile mSoundFile;  
	private File mFile;
	private boolean mKeyDown;
	private String mRecordingFilename;
	private Uri mRecordingUri;
	private boolean mCanSeekAccurately;
	
	///////**
	private long mLoadingStartTime;     
	private long mLoadingLastUpdateTime;     
	private boolean mLoadingKeepGoing;     
	private ProgressDialog mProgressDialog;   
	private String mDstFilename;     
	private String mArtist;     
	private String mAlbum;     
	private String mGenre;     
	private String mTitle;     
	private int mYear;     
	private String mExtension;         
	private int mNewFileKind;       
	private WaveformView mWaveformView;     
	private MarkerView mStartMarker;     
	private MarkerView mEndMarker;     
	private TextView mStartText;     
	private TextView mEndText;     
	private TextView mInfo;               
	private Handler mHandler;       
	private float X, Y, Z;
	private static final int SHAKE_THRESHOLD = 700;
	
	private static final int DATA_X = SensorManager.DATA_X;
	private static final int DATA_Y = SensorManager.DATA_Y;
	private static final int DATA_Z = SensorManager.DATA_Z;
	
	// Result codes     
	private static final int REQUEST_CODE_EDIT = 1;  
	
	Uri audioFileUri = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manual_vibe);
	
		beep = new Beeper(this, R.raw.beep); 
		musicWhite = BitmapFactory.decodeResource(getResources(), R.drawable.music_white);
		musicBlue = BitmapFactory.decodeResource(getResources(), R.drawable.music_blue);
		
		
		
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		CustomVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		//** 배열이 진동->무진동->진동->무진동  이런식.. 그래서 첫배열엔 이벤트 시점 전까지 진동이 울리지
		//**  않게 하기 위해 첫번째 배열요소로 0 을 우선 줌.
		checkVibe.add(new Long(0));
		
		mPlayer=null;
		
		Intent intent = getIntent();
		mFilename = intent.getData().toString();
		if (!mFilename.equals("record")) {             
			loadFromFile();         
		} 
		
		mPlayButton = (Button) this.findViewById(R.id.play);
		mPlayButton.setOnClickListener(this);
		mPlayButton.setEnabled(true);
		
		mPlayVibe = (Button) this.findViewById(R.id.play_vibe);
		mPlayVibe.setOnClickListener(this);
		mPlayVibe.setEnabled(false);
		
		mVibeBeep = (Button) this.findViewById(R.id.vibe_beep);
		mVibeBeep.setOnClickListener(this);
		mVibeBeep.setEnabled(false);
	
		mSet = (Button) this.findViewById(R.id.set);
		mSet.setOnClickListener(this);
		mSet.setEnabled(false);
		
	}
	
	
	public void onClick(View v) {
		
		if(v == mPlayButton) {
			acc = new accListener();
			onResume();
			try {
				mPlayer.reset();
				mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);                     
/*				FileInputStream subsetInputStream = new FileInputStream(                         
					mFile.getAbsolutePath());                     
					mPlayer.setDataSource(subsetInputStream.getFD(),                                           
											startByte, endByte - startByte);                     
					mPlayer.prepare();                   
*/
				mPlayer.setDataSource(mFile.getAbsolutePath());
				mPlayer.prepare();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				mPlayer.reset();  
			}
			mPlayer.setOnCompletionListener(this);
			mPlayer.start();
			mPlayButton.setEnabled(false);
			startTime =  System.currentTimeMillis();
			
		}else if(v == mPlayVibe) {
			pattern = new long[checkVibe.size()];
			for(int i =0; i<checkVibe.size(); i++)
				pattern[i]= checkVibe.get(i);

			//** 패턴을 우리의 계산식말고 주어진 값에서도 작동되는지 한번확인할것.
			mPlayer.start();
			CustomVibe.vibrate(pattern, -1);
		}else if(v == mVibeBeep) {
			///beep code
			c =0;
			for(int i=0; i<checkVibe.size(); i++)
			beep.play(pattern[i]);
		}else if(v == mSet) {
			//setting dialog intent      
		
			final Handler handler = new Handler() {                 
				public void handleMessage(Message response) {                     
					int actionId = response.arg1;                    
					switch (actionId) {                    
		//** make_default 기능 
							case R.id.button_make_default:                         
						RingtoneManager.setActualDefaultRingtoneUri(                             
								ManualVibeActivity.this,                             
								RingtoneManager.TYPE_RINGTONE,                            
								audioFileUri);  //**임시방편임. 원래 newUri                      
						Toast.makeText(                            
								ManualVibeActivity.this,                             
								R.string.default_ringtone_success_message,                            
								Toast.LENGTH_SHORT)                             
								.show();                         
//						sendStatsToServerIfAllowedAndFinish();                        
						break;  
									                   
		//** contact 기능 제거 -	
		/*						case R.id.button_choose_contact:                         
						chooseContactForRingtone(newUri);                         
						break;                     
		*/			default:                     
					case R.id.button_do_nothing:                         
						//sendStatsToServerIfAllowedAndFinish();   
						//Finish();
						break;                     
					}                 
				}             
			};        
			Message message = Message.obtain(handler);         
			SettingDialog dlog = new SettingDialog(this, message);
			dlog.show();
		}
	}
	
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_EDIT) {
			audioFileUri = data.getData();
			mPlayButton.setEnabled(true);
		}
	}
	
	
	private void loadFromFile() {        
		mFile = new File(mFilename);        
		mExtension = getExtensionFromFilename(mFilename);          
		SongMetadataReader metadataReader = new SongMetadataReader(             
				this, mFilename);         
		mTitle = metadataReader.mTitle;        
		mArtist = metadataReader.mArtist;        
		mAlbum = metadataReader.mAlbum;        
		mYear = metadataReader.mYear;        
		mGenre = metadataReader.mGenre;          
		String titleLabel = mTitle;         
		if (mArtist != null && mArtist.length() > 0) {            
			titleLabel += " - " + mArtist;         
			}         
		setTitle(titleLabel);          
		mLoadingStartTime = System.currentTimeMillis();         
		mLoadingLastUpdateTime = System.currentTimeMillis();         
		mLoadingKeepGoing = true;        
		mProgressDialog = new ProgressDialog(ManualVibeActivity.this);         
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);         
		mProgressDialog.setTitle(R.string.progress_dialog_loading);        
		mProgressDialog.setCancelable(true);         
		mProgressDialog.setOnCancelListener(             
				new DialogInterface.OnCancelListener() {                 
					public void onCancel(DialogInterface dialog) {                     
						mLoadingKeepGoing = false;                 
						}            
					});         
		mProgressDialog.show();         
		
		final CheapSoundFile.ProgressListener listener =             
				new CheapSoundFile.ProgressListener() {                
					public boolean reportProgress(double fractionComplete) {                     
							long now = System.currentTimeMillis();                     
								if (now - mLoadingLastUpdateTime > 100) {                         
									mProgressDialog.setProgress(                             
											(int)(mProgressDialog.getMax() *                                   
													fractionComplete));                         
									mLoadingLastUpdateTime = now;                     
								}                    
								return mLoadingKeepGoing;                 
					}            
			};      
			
			// Create the MediaPlayer in a background thread         
			mCanSeekAccurately = false;         
			new Thread() {             
				public void run() {                 
					mCanSeekAccurately = SeekTest.CanSeekAccurately(                   
							getPreferences(Context.MODE_PRIVATE));                  
					System.out.println("Seek test done, creating media player.");                
					try {                    
						MediaPlayer player = new MediaPlayer();                   
						player.setDataSource(mFile.getAbsolutePath());                     
						player.setAudioStreamType(AudioManager.STREAM_MUSIC);                     
						player.prepare();                     
						mPlayer = player;                 
						} catch (final java.io.IOException e) {                     
							Runnable runnable = new Runnable() {                         
								public void run() {                            
					//** 구글앱서버 사용제거	handleFatalError(                                 
					//						"ReadError",                                 
					//						getResources().getText(R.string.read_error),                                 
					//						e);                         
									}                     
								};                     
								mHandler.post(runnable);                 
							};             
						}         
				}.start();          
				
				// Load the sound file in a background thread         
				new Thread() {              
					public void run() {                  
						try {                     
							mSoundFile = CheapSoundFile.create(mFile.getAbsolutePath(),                                                        
																listener);                      
							if (mSoundFile == null) {                         
								mProgressDialog.dismiss();                         
								String name = mFile.getName().toLowerCase();                         
								String[] components = name.split("\\.");                         
								String err;                         
								if (components.length < 2) {                             
									err = getResources().getString(                                 
											R.string.no_extension_error);                         
									} else {                             
										err = getResources().getString(                               
												R.string.bad_extension_error) + " " +                                 
												components[components.length - 1];                         
									}                         
									final String finalErr = err;                         
									Runnable runnable = new Runnable() {                            
										public void run() {                                 
						//** 구글앱서버 사용제거		handleFatalError(                                   
						//							"UnsupportedExtension",                                   
						//							finalErr,                                   
						//							new Exception());                             
											}                         
										};                         
										mHandler.post(runnable);                         
										return;                     
										}                 
							} catch (final Exception e) {                     
								mProgressDialog.dismiss();                     
								e.printStackTrace();                     
								mInfo.setText(e.toString());                      
								Runnable runnable = new Runnable() {                             
									public void run() {                                 
				//** 구글앱서버 사용제거						handleFatalError(                                   
				//								"ReadError",                                   
				//								getResources().getText(R.string.read_error),                                   
				//								e);                             
										}                         
									};                     
									mHandler.post(runnable);                     
									return;                 
								}                 
								mProgressDialog.dismiss();                  
								if (!mLoadingKeepGoing) {                                         
										ManualVibeActivity.this.finish();                 
								}             
						}          
				}.start();     
	}
	
	private String getExtensionFromFilename(String filename) {         
		return filename.substring(filename.lastIndexOf('.'),                                  
				filename.length());     
	}
	
	
	public void onCompletion(MediaPlayer mp) {
		mPlayButton.setEnabled(true);
		mPlayVibe.setEnabled(true);
		mVibeBeep.setEnabled(true);
		mSet.setEnabled(true);
		onPause();
		
	}
	
	@Override 
    public void onPause() {
        super.onPause();
        
       sm.unregisterListener(acc);    // unregister orientation listener
    }
	
	@Override
    public void onResume() {
        super.onResume();
                
        sm.registerListener(acc, accSensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	
	
private class accListener implements SensorEventListener {
	public void onSensorChanged(SensorEvent event) {
		int p=1;
		
	//	last_time.setText(Long.toString(lastTime));
		//* currentTime, gabOfTime,startTime <위와 같이 만들어주고 변수 TextView위에 선언>
		
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { 
            long currentTime = System.currentTimeMillis(); 
            long gabOfTime = (currentTime - lastTime); 
 
            if (gabOfTime > 100) { 
                lastTime = currentTime; 
   
                X = event.values[SensorManager.DATA_X]; 
                Y = event.values[SensorManager.DATA_Y]; 
                Z = event.values[SensorManager.DATA_Z]; 

   
 
                speed = Math.abs(X + Y + Z - lastX - lastY - lastZ) / gabOfTime * 10000;
 
   
 
                if (speed > SHAKE_THRESHOLD) { 
                    // 이벤트 발생!! 
                	p = 0;
                	currentTime = System.currentTimeMillis();
                	checkVibe.add(currentTime-startTime);
                	startTime=currentTime;
                	Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    		        vibe.vibrate((long)500);
                } else if(speed <= SHAKE_THRESHOLD && p == 0){
                	lastTime = System.currentTimeMillis() + (long)500;
                	checkVibe.add(lastTime-startTime);
                	lastTime=startTime;
                	p=1;
                }

                lastX = event.values[DATA_X]; 
                lastY = event.values[DATA_Y]; 
                lastZ = event.values[DATA_Z]; 
            } 
        } 		
	}
		
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
}

	class Beeper {
		MediaPlayer player;
	    Beeper(Context context, int id) {
	      player = MediaPlayer.create(context, id);
	    }
	 
	    void play(long second) {
	      player.seekTo(0);
	      if(c%2 == 0){
	    	  player.start();
	    	  c++;
	      }
	      else if(c%2 != 0){
	      try{
	    	  Thread.sleep(second);
	    	  c++;
	      }catch(InterruptedException e){}
	      }
	    }

	}

}
	
