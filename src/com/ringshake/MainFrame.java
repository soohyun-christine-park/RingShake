package com.ringshake;

import com.ringshake.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainFrame extends Activity implements OnClickListener{
	private Button btnStart;
	private Button btnVibe;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.main_frame);
       
       btnStart = (Button)findViewById(R.id.main_frame_btn_start);
       btnVibe = (Button)findViewById(R.id.main_frame_btn_vibe);

       btnStart.setOnClickListener(this);
       btnVibe.setOnClickListener(this);
    
	}
	
    @Override
   	public void onClick(View v) {
    	// TODO Auto-generated method stub
    	Intent intent;
    	switch (v.getId()) {
		case R.id.main_frame_btn_start:
			intent = new Intent(this, RingshakeSelectActivity.class);
			startActivity(intent);
			break;
		case R.id.main_frame_btn_vibe:
			intent = new Intent(this, RingtoneSelectActivity.class);
			startActivity(intent);
			break;
		default:
			break;
    	}
			
    }
}
