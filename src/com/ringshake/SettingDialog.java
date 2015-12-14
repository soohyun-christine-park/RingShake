/*  * Copyright (C) 2009 Google Inc.  * 
 * Copyright [2012] [Joljak]
 * Licensed under the Apache License, Version 2.0 (the "License");  
 ** you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */  
package com.ringshake;  
import android.app.Dialog; 
import android.content.Context; 
import android.content.Intent;
import android.content.res.Resources; 
import android.os.Message; 
import android.view.View; 
import android.widget.ArrayAdapter; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.Spinner;  
import java.util.ArrayList; 
import java.util.HashMap;  

import com.ringshake.R;

public class SettingDialog extends Dialog {      
	private Message mResponse;
	
	public SettingDialog(Context context, Message response) {         
		super(context);         

		// Inflate our UI from its XML layout description. 
		
		setContentView(R.layout.setting_action);          
		setTitle(R.string.alert_title_success); 
		((Button)findViewById(R.id.button_make_default)) 
	//** make default 버튼. 로 전환.	((Button)findViewById(R.id.button_auto_vibrate))
		.setOnClickListener(new View.OnClickListener() {                     
			public void onClick(View view) {                         
		closeAndSendResult(R.id.button_make_default);
	//** make default 버튼. 로 전환.			closeAndSendResult(R.id.button_auto_vibrate);
				}                 
			}); 
		mResponse = response;     
		}      
	private void closeAndSendResult(int clickedButtonId) {         
		mResponse.arg1 = clickedButtonId;         
		mResponse.sendToTarget();         
		dismiss();     
	}
}
