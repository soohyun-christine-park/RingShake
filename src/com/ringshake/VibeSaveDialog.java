/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */  

package com.ringshake;  

import java.util.ArrayList;

import com.ringshake.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class VibeSaveDialog extends Dialog {       

	public static final int FILE_KIND_Vibe = 0;      
    
	private TextView mFilename;  //이름 보여주기로 수정
	private Message mResponse;     
	private String mOriginalName;           
	
	public VibeSaveDialog(Context context,                           
						Resources resources,                           
						String originalName,                          
						Message response) {         
		super(context);          
		
		// Inflate our UI from its XML layout description.         
		setContentView(R.layout.file_save);          
		setTitle(resources.getString(R.string.file_save_title)); 

//** 이름변경기능제거 -		mFilename = (EditText)findViewById(R.id.filename); 
		mFilename = (TextView)findViewById(R.id.filename); 
		
		mOriginalName = originalName;                   
		setFilenameEditBoxFromName(false);
	
		Button save = (Button)findViewById(R.id.save);         
		save.setOnClickListener(saveListener);         
		Button cancel = (Button)findViewById(R.id.cancel);         
		cancel.setOnClickListener(cancelListener);         
		mResponse = response;     
		}      
	
	private void setFilenameEditBoxFromName(boolean onlyIfNotEdited) {         
		if (onlyIfNotEdited) {             
			CharSequence currentText = mFilename.getText();             
			String expectedText = mOriginalName + " " ;            
			if (!expectedText.contentEquals(currentText)) {                 
				return;             
				}         
			} 
		}      

	
	private View.OnClickListener saveListener = new View.OnClickListener() {             
		public void onClick(View view) {                 
			mResponse.obj = mFilename.getText();                                
			mResponse.sendToTarget();                 
			dismiss();             
			}        
		};      
		
		private View.OnClickListener cancelListener = new View.OnClickListener() {             
			public void onClick(View view) {                
				dismiss();            
				}        
			};		
} 
