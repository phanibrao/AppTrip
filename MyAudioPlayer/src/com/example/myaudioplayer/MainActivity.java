package com.example.myaudioplayer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	
	private File downloadingMediaFile;
	private static final int INTIAL_KB_BUFFER = 192*10/8;
	
	/************ Track for display by progressBar**************/
	
	
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	private MediaPlayer mediaPlayer;
	
	/******** Create Handler to call View updates on the main UI thread.*******/
	private final Handler handler = new Handler();
	private boolean isInterrupted;
	private int counter = 0;
	ProgressDialog dialog;
	
	private ImageView mPlayMedia;
	private ImageView mPauseMedia;
	private SeekBar mMediaSeekBar;
	private TextView mRunTime;
	private TextView mTotalTime;
	
	
	private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;

	// TODO: externalize the error messages.
	private static final String ERROR_PLAYVIEW_NULL = "Play view cannot be null";
	private static final String ERROR_PLAYTIME_CURRENT_NEGATIVE = "Current playback time cannot be negative";
	private static final String ERROR_PLAYTIME_TOTAL_NEGATIVE = "Total playback time cannot be negative";

	private Handler mProgressUpdateHandler;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.awd_main);
		
		initViews();
		
		if(new NetworkUtils().isOnline(MainActivity.this)){
				
		dialog=createProgressDialog(MainActivity.this);
		
		setViewsVisibility(false);
		
		startStreaming("http://programmerguru.com/android-tutorial/wp-content/uploads/2013/04/hosannatelugu.mp3");
	           
		//startStreaming("http://www.virginmegastore.me/Library/Music/CD_001214/Tracks/Track1.mp3");
		
		
		mPlayMedia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Play", Toast.LENGTH_SHORT).show();
				// get-set-go. Lets dance.
				//pause();
				if (mPlayMedia == null) {
					throw new IllegalStateException("ERROR_PLAYVIEW_NULL");
				}

				if (mediaPlayer == null) {
					throw new IllegalStateException("Call init() before calling this method");
				}

				if (mediaPlayer.isPlaying()) {
					return;
				}
				
				mediaPlayer.start();
				mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
				mPauseMedia.setVisibility(View.VISIBLE);
				mPlayMedia.setVisibility(View.GONE);
			}
		});
		
		
		

		mPauseMedia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Pause", Toast.LENGTH_SHORT).show();
			
				mediaPlayer.pause();
				mPauseMedia.setVisibility(View.GONE);
				mPlayMedia.setVisibility(View.VISIBLE);
			}
		});   
		
		}else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Connect to wifi or quit")
			.setCancelable(false)
			.setPositiveButton("Connect to WIFI", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
			        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			   }
			 })
			.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
			        MainActivity.this.finish();
			   }
			});
			 AlertDialog alert = builder.create();
			 alert.show();
		}
	
	}
	
	
	
	/*****************  Initialise views  ************/
	
	public void initViews(){
	
		    mTotalTime = (TextView) findViewById(R.id.total_time);
			mPlayMedia = (ImageView)findViewById(R.id.play);
			mPauseMedia = (ImageView)findViewById(R.id.pause);
			mMediaSeekBar = (SeekBar) findViewById(R.id.media_seekbar);
			mRunTime = (TextView) findViewById(R.id.run_time);
	}
	


	public void play() {
		if (mPlayMedia == null) {
			throw new IllegalStateException("ERROR_PLAYVIEW_NULL");
		}
		if (mediaPlayer == null) {
			throw new IllegalStateException("Call init() before calling this method");
		}
		if (mediaPlayer.isPlaying()) {
			return;
		}
		mediaPlayer.start();
		setPausable();
	}
		
			


	
	public void pause() {

		if (mediaPlayer == null) {
			return;
		}

		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			setPlayable();
		}
	}
	
	
	
	
	private void setPlayable() {
		if (mPlayMedia != null) {
			mPlayMedia.setVisibility(View.VISIBLE);
		}

		if (mPauseMedia != null) {
			mPauseMedia.setVisibility(View.GONE);
		}
	}
	
	private void setViewsVisibility() {

		if (mMediaSeekBar != null) {
			mMediaSeekBar.setVisibility(View.VISIBLE);
		}
		if (mRunTime != null) {
			mRunTime.setVisibility(View.VISIBLE);
		}

		if (mTotalTime != null) {
			mTotalTime.setVisibility(View.VISIBLE);
		}

		if (mPlayMedia != null) {
			mPlayMedia.setVisibility(View.VISIBLE);
		}

		if (mPauseMedia != null) {
			mPauseMedia.setVisibility(View.VISIBLE);
		}
	}
	

		
	private void setViewsVisibility(boolean status) {
		   
		if(status){
			   
			if (mMediaSeekBar != null) {
				mMediaSeekBar.setVisibility(View.VISIBLE);
			}

			if (mRunTime != null) {
				mRunTime.setVisibility(View.VISIBLE);
			}

			if (mTotalTime != null) {
				mTotalTime.setVisibility(View.VISIBLE);
			}

			if (mPlayMedia != null) {
				mPlayMedia.setVisibility(View.GONE);
			}

			if (mPauseMedia != null) {
				mPauseMedia.setVisibility(View.VISIBLE);
			}

		}else
		{
			if (mMediaSeekBar != null) {
				mMediaSeekBar.setVisibility(View.GONE);
			}

			if (mRunTime != null) {
				mRunTime.setVisibility(View.GONE);
			}

			if (mTotalTime != null) {
				mTotalTime.setVisibility(View.GONE);
			}

			if (mPlayMedia != null) {
				mPlayMedia.setVisibility(View.VISIBLE);
			}

			if (mPauseMedia != null) {
				mPauseMedia.setVisibility(View.GONE);
			}
		}
	}

	private void setPausable() {
		if (mPlayMedia != null) {
			mPlayMedia.setVisibility(View.VISIBLE);
		}
		if (mPauseMedia != null) {
			mPauseMedia.setVisibility(View.GONE);
		}
	}

	
	public void startStreaming(final String mediaUrl){
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	            		downloadAudioIncrement(mediaUrl);
	            } catch (IOException e) {
	            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);
	            	return;
	            }   
	        }   
	    };   
	    new Thread(r).start();
	}
	
  public void downloadAudioIncrement(String mediaUrl) throws IOException {
    	
    	URLConnection cn = new URL(mediaUrl).openConnection();   
        cn.connect();   
        InputStream stream = cn.getInputStream();
       
        if (stream == null) {
        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaUrl);
        }
        
		downloadingMediaFile = new File(MainActivity.this.getCacheDir(),"downloadingMedia.dat");
		
		if (downloadingMediaFile.exists()) {
			downloadingMediaFile.delete();
			
		}

        FileOutputStream out = new FileOutputStream(downloadingMediaFile);   
        byte buf[] = new byte[16384];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        do {
        	int numread = stream.read(buf);   
            if (numread <= 0)   
                break;   
            out.write(buf, 0, numread);
            totalBytesRead += numread;
            incrementalBytesRead += numread;
            totalKbRead = totalBytesRead/1000;
            
            testMediaBuffer();
           	fireDataLoadUpdate();
        } while (validateNotInterrupted());   
       		stream.close();
        if (validateNotInterrupted()) {
	       	fireDataFullyLoaded();
        }
    }  
    
    
    private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
	        public void run() {
	        	
	    		float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
	    		
	        }
	    };
	    handler.post(updater);
    }
    
      

    private void fireDataFullyLoaded() {
    	Runnable updater = new Runnable() { 
    		public void run() {
    			
    			transferBufferToMediaPlayer();
    			downloadingMediaFile.delete();
    			
    		}
    	};
    	handler.post(updater);
    }
    private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				//mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
    }

    private void  testMediaBuffer() {
    	Runnable updater = new Runnable() {
    		public void run() {
    			if (mediaPlayer == null) {
    				//  Only create the MediaPlayer once we have the minimum buffered data
    				if ( totalKbRead >= INTIAL_KB_BUFFER) {
    					try {
    						startMediaPlayer();
    						dialog.dismiss();
    						if(dialog!=null)
    						   dialog=null;
    						   setViewsVisibility(true);
    						
    					} catch (Exception e) {
    						Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
    					}
    				}
    			} else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
    				transferBufferToMediaPlayer();
    			}
    		}
    	};
    	handler.post(updater);
    }
    
    
    private void startMediaPlayer() {
        try {   
        	File bufferedFile = new File(MainActivity.this.getCacheDir(),"playingMedia" + (counter++) + ".dat");
        	
        	moveFile(downloadingMediaFile,bufferedFile);
    		
        	Log.e(getClass().getName(),"Buffered File path: " + bufferedFile.getAbsolutePath());
        	Log.e(getClass().getName(),"Buffered File length: " + bufferedFile.length()+"");
        	
        	mediaPlayer = createMediaPlayer(bufferedFile);
        	
        	
        	mProgressUpdateHandler = new Handler();
        	 	
        	initMediaSeekBar();
        	setTotalTime();
        	
        	mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
        	
	    	mediaPlayer.start();
	    	boolean status=mediaPlayer.isPlaying();
	        play();
        
          } catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
        	return;
        }   
    }
    
    private void setTotalTime() {

		if (mTotalTime == null) {
			return;
		}

		StringBuilder playbackStr = new StringBuilder();
		long totalDuration = 0;

		if (mediaPlayer != null) {
			try {
				totalDuration = mediaPlayer.getDuration();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (totalDuration < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_TOTAL_NEGATIVE);
		}

		if (totalDuration != 0) {
			playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
		}

		mTotalTime.setText(playbackStr);
	}
    
    private Runnable mUpdateProgress = new Runnable() {

		public void run() {

			if (mMediaSeekBar == null) {
				return;
			}

			if (mProgressUpdateHandler != null && mediaPlayer.isPlaying()) {
				mMediaSeekBar.setProgress((int) mediaPlayer.getCurrentPosition());
				int currentTime = mediaPlayer.getCurrentPosition();
				updatePlaytime(currentTime);
				updateRuntime(currentTime);
				setTotalTime();
				mProgressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
				
			} else {

			}
		}
	};

    
	public void moveFile(File	oldLocation, File	newLocation)
	throws IOException {

		if ( oldLocation.exists( )) {
			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
            try {
		        byte[]  buff = new byte[8192];
		        int numChars;
		        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
		        	writer.write( buff, 0, numChars );
      		    }
            } catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){                    	
                    	writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
            }
        } else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
	}
    
    private MediaPlayer createMediaPlayer(File mediaFile)
    throws IOException {
    	MediaPlayer mPlayer = new MediaPlayer();
    	mPlayer.setOnErrorListener(
				new MediaPlayer.OnErrorListener() {
			        public boolean onError(MediaPlayer mp, int what, int extra) {
			        	Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
			    		return false;
			        }
			    });
    	
    	mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                setPausable();
                initMediaSeekBar();
               }
        });
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		mPlayer.prepare();
		return mPlayer;
    }
    

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
        	    dialog.setMessage("Buffering..");
        	    dialog.setCanceledOnTouchOutside(false);
        	    dialog.setCancelable(false);
                dialog.show();
               
             } catch (BadTokenException e) {

        }
       
        return dialog;
}
    
    private void transferBufferToMediaPlayer() {
	    try {
	   
	    	boolean wasPlaying = mediaPlayer.isPlaying();
	    	int curPosition = mediaPlayer.getCurrentPosition();
	    	
	    	File oldBufferedFile = new File(MainActivity.this.getCacheDir(),"playingMedia" + counter + ".dat");
	    	File bufferedFile = new File(MainActivity.this.getCacheDir(),"playingMedia" + (counter++) + ".dat");

	    	bufferedFile.deleteOnExit();   
	    	moveFile(downloadingMediaFile,bufferedFile);

	    	mediaPlayer.pause();
        	mediaPlayer = createMediaPlayer(bufferedFile);
        	mediaPlayer.seekTo(curPosition);
    	  
        	boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}
	    	oldBufferedFile.delete();
	    	
	    }catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(mediaPlayer!=null){
    		
    		mediaPlayer.stop();
    		mediaPlayer=null;
    	}
    }
    
    
    private void initMediaSeekBar() {

		if (mMediaSeekBar == null) {
			return;
		}


		long finalTime = mediaPlayer.getDuration();
		mMediaSeekBar.setMax((int) finalTime);

		mMediaSeekBar.setProgress(0);

		mMediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mediaPlayer.seekTo(seekBar.getProgress());
			
				updateRuntime(seekBar.getProgress());
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				mMediaSeekBar.setMax((int) mediaPlayer.getDuration());
			}
		});
	}

    @Deprecated
	private void updatePlaytime(int currentTime) {

		if (currentTime < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_CURRENT_NEGATIVE);
		}

		StringBuilder playbackStr = new StringBuilder();

		playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));
		playbackStr.append("/");

		long totalDuration = 0;

		if (mediaPlayer != null) {
			try {
				totalDuration = mediaPlayer.getDuration();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (totalDuration != 0) {
			playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
		} else {
                 //			Log.w("audio track", "Something strage this audio track duration in zero");
		}
	}

	private void updateRuntime(int currentTime) {

		if (mRunTime == null) {
			return;
		}

		if (currentTime < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_CURRENT_NEGATIVE);
		}

		StringBuilder playbackStr = new StringBuilder();

		playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

		mRunTime.setText(playbackStr);
	}

	
	@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			super.onBackPressed();
			release();
			dialog=null;
			mPlayMedia.setOnClickListener(null);
			mPauseMedia.setOnClickListener(null);
			mPlayMedia=null;
			mPauseMedia=null;
			mMediaSeekBar=null;
			mRunTime=null;
			mTotalTime=null;
			finish();
		}
	
	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null	&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null	&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;
		
	}  
	
	
	
	public void release() {

		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
			mProgressUpdateHandler = null;
			downloadingMediaFile=null;
			if(mMediaSeekBar!=null){
				mMediaSeekBar.setOnSeekBarChangeListener(null);
				mMediaSeekBar=null;
			}
		
			
			//mMediaSeekBar.setOnSeekBarChangeListener(null);
			
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Toast.makeText(MainActivity.this, "player", Toast.LENGTH_SHORT).show();
	}
	
	
	
}
