/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */ 

package com.ringshake;  
import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ringshake.R;
import com.ringshake.soundfile.CheapSoundFile;

public class RingtoneSelectActivity     
	extends ListActivity     
	implements TextWatcher
{     
	private TextView mFilter;     
	private SimpleCursorAdapter mAdapter;    
//** 레코드 기능 제거	private boolean mWasGetContentIntent;     
	private boolean mShowAll;     
	// Result codes    
	private static final int REQUEST_CODE_EDIT = 1;    
//** contact 기능 제거 -	
//	private static final int REQUEST_CODE_CHOOSE_CONTACT = 2;  
	// Menu commands    
	private static final int CMD_ABOUT = 1;   
	private static final int CMD_PRIVACY = 2;     
	private static final int CMD_SHOW_ALL = 3;     
	// Context menu    
	private static final int CMD_EDIT = 4;  
	private static final int CMD_DELETE = 5;    
//** default 기능 제거 ???? -
	private static final int CMD_SET_AS_DEFAULT = 6;  
//** contact 기능 제거 -	
//	private static final int CMD_SET_AS_CONTACT = 7;       
	public RingtoneSelectActivity() {     
	}      
	
	/**      * Called when the activity is first created.      */    
	@Override     
	public void onCreate(Bundle icicle) {         
		super.onCreate(icicle);         
		mShowAll = false;        
		String status = Environment.getExternalStorageState();         
		if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {             
			showFinalAlert(getResources().getText(R.string.sdcard_readonly));            
			return;         
		}        
		if (status.equals(Environment.MEDIA_SHARED)) {         
			showFinalAlert(getResources().getText(R.string.sdcard_shared));            
			return;        
		}         
		if (!status.equals(Environment.MEDIA_MOUNTED)) {   
			showFinalAlert(getResources().getText(R.string.no_sdcard));           
			return;       
		}          
		Intent intent = getIntent();       
      
		setContentView(R.layout.media_select);
	         
		
		try {          
			mAdapter = new SimpleCursorAdapter(        
					this,                   
					// Use a template that displays a text view                
					R.layout.media_select_row,              
					// Give the cursor to the list adatper      
					createCursor(""),                    
					// Map from database columns...       
					new String[] {                      
						MediaStore.Audio.Media.ARTIST,     
						MediaStore.Audio.Media.ALBUM,      
						MediaStore.Audio.Media.TITLE,                     
						MediaStore.Audio.Media._ID,                    
						MediaStore.Audio.Media._ID},                    
						// To widget ids in the row layout...           
						new int[] {                       
						R.id.row_artist,                 
						R.id.row_album,                     
						R.id.row_title,                
						R.id.row_icon,                  
						R.id.row_options_button});        
			setListAdapter(mAdapter);           
			getListView().setItemsCanFocus(true);          
			// Normal click - open the editor            
			getListView().setOnItemClickListener(new OnItemClickListener() {          
				public void onItemClick(AdapterView parent,                       
						View view,                        
						int position,                       
						long id) {                   
	
					startManualViber();
					   
						}      
					});        
			} catch (SecurityException e) {            
				// No permission to retrieve audio?        
				Log.e("RingShake", e.toString());         
				// todo error 1        
			} catch (IllegalArgumentException e) {         
				// No permission to retrieve audio?        
				Log.e("RingShake", e.toString());        
				// todo error 2         
			}         
			mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {          
				public boolean setViewValue(View view,                
						Cursor cursor,                   
						int columnIndex) {            
					if (view.getId() == R.id.row_options_button){         
						// Get the arrow image view and set the onClickListener to open the context menu.    
						ImageView iv = (ImageView)view;                   
						iv.setOnClickListener(new View.OnClickListener() {       
							public void onClick(View v) {                    
								openContextMenu(v);                      
							}                
						});               
						
						return true;              
					} else if (view.getId() == R.id.row_icon) {   
						setSoundIconFromCursor((ImageView) view, cursor);      
						return true;               
					}                 
					return false;           
				}        
			});         
			
			// Long-press opens a context menu     
			registerForContextMenu(getListView());      
			mFilter = (TextView) findViewById(R.id.search_filter);     
			if (mFilter != null) {        
				mFilter.addTextChangedListener(this);        
			}     
		}      
	
		private void setSoundIconFromCursor(ImageView view, Cursor cursor) {     
			if (0 != cursor.getInt(cursor.getColumnIndexOrThrow(            
					MediaStore.Audio.Media.IS_RINGTONE))) {           
				view.setImageResource(R.drawable.type_ringtone);       
				((View) view.getParent()).setBackgroundColor(          
						getResources().getColor(R.drawable.type_bkgnd_ringtone));      
			}       
			
			String filename = cursor.getString(cursor.getColumnIndexOrThrow( 
					//** DATA를 IS_RINGTONE으로 수정cancle
					MediaStore.Audio.Media.DATA));         
			if (!CheapSoundFile.isFilenameSupported(filename)) {            
				((View) view.getParent()).setBackgroundColor(                  
						getResources().getColor(R.drawable.type_bkgnd_unsupported));   
			}    
		}    
		
		/** Called with an Activity we started with an Intent returns. */    
		@Override     
		protected void onActivityResult(int requestCode, int resultCode,          
				Intent dataIntent) {       
			if (requestCode != REQUEST_CODE_EDIT) {          
				return;        
			}        
			if (resultCode != RESULT_OK) {    
				return;     
			}         
			setResult(RESULT_OK, dataIntent);     
			finish();    
		}     
		
		@Override    
		public boolean onCreateOptionsMenu(Menu menu) {     
			super.onCreateOptionsMenu(menu);    
			MenuItem item;         
			item = menu.add(0, CMD_ABOUT, 0, R.string.menu_about);    
			item.setIcon(R.drawable.menu_about);         
			item = menu.add(0, CMD_PRIVACY, 0, R.string.menu_privacy);   
			item.setIcon(android.R.drawable.ic_menu_share);         
			item = menu.add(0, CMD_SHOW_ALL, 0, R.string.menu_show_all_audio);  
			item.setIcon(R.drawable.menu_show_all_audio);        
			return true;    
		}      
		
		@Override   
		public boolean onPrepareOptionsMenu(Menu menu) {    
			super.onPrepareOptionsMenu(menu);       
			menu.findItem(CMD_ABOUT).setVisible(true);   
			menu.findItem(CMD_PRIVACY).setVisible(true); 
			menu.findItem(CMD_SHOW_ALL).setVisible(true);   
			menu.findItem(CMD_SHOW_ALL).setEnabled(!mShowAll);  
			return true;   
		}      
		
		@Override    
		public boolean onOptionsItemSelected(MenuItem item) {     
			switch (item.getItemId()) {         
			case CMD_ABOUT:             
				RingshakeEditActivity.onAbout(this);         
				return true;        
			case CMD_PRIVACY:         
				showPrivacyDialog();         
				return true;       
			case CMD_SHOW_ALL:        
				mShowAll = true;          
				refreshListView();        
				return true;      
			default:         
				return false;       
			}   
		}    
		
		@Override    
		public void onCreateContextMenu(ContextMenu menu,      
				View v,        
				ContextMenuInfo menuInfo) {   
			super.onCreateContextMenu(menu, v, menuInfo);    
			Cursor c = mAdapter.getCursor();    
			String title = c.getString(c.getColumnIndexOrThrow(      
					MediaStore.Audio.Media.TITLE));         
			menu.setHeaderTitle(title);       
			menu.add(0, CMD_EDIT, 0, R.string.context_menu_edit);       
			menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);      
			
			// Add items to the context menu item based on file type        
			if (0 != c.getInt(c.getColumnIndexOrThrow(               
					MediaStore.Audio.Media.IS_RINGTONE))) {           
//** defalt 버튼 제거 되며 기능 제거 -menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_ringtone);     
//** contact 기능 제거 - 
//				menu.add(0, CMD_SET_AS_CONTACT, 0, R.string.context_menu_contact);       
			} else if (0 != c.getInt(c.getColumnIndexOrThrow(       
					MediaStore.Audio.Media.IS_NOTIFICATION))) {           
				menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_notification);   
			}    
		}      
		
		@Override    
		public boolean onContextItemSelected(MenuItem item) {     
			AdapterContextMenuInfo info =          
					(AdapterContextMenuInfo) item.getMenuInfo();   
			switch (item.getItemId()) {      
			case CMD_EDIT:           
				startManualViber();
				return true;         
			case CMD_DELETE:      
				confirmDelete();            
				return true;         
			case CMD_SET_AS_DEFAULT:        
				setAsDefaultRingtoneOrNotification();    
				return true;        
		default:            
				return super.onContextItemSelected(item);        
			}    
		}    

		private void showPrivacyDialog() {    
			try {           
				Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(""));  
				intent.putExtra("privacy", true);          
				intent.setClassName("com.ringshake",                      
						"com.ringshake.RingshakeEditActivity");     
				startActivityForResult(intent, REQUEST_CODE_EDIT);       
			} catch (Exception e) {            
				Log.e("FeelTheMusic", "Couldn't show privacy dialog");       
			}     
		}   

		private void setAsDefaultRingtoneOrNotification(){      
			Cursor c = mAdapter.getCursor();      
			if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))){       
				RingtoneManager.setActualDefaultRingtoneUri(                
						RingtoneSelectActivity.this,                  
						RingtoneManager.TYPE_RINGTONE,                  
						getUri());          
				Toast.makeText(                
						RingtoneSelectActivity.this,      
						R.string.default_ringtone_success_message,   
						Toast.LENGTH_SHORT)             
						.show();         
			} else {           
				RingtoneManager.setActualDefaultRingtoneUri(          
						RingtoneSelectActivity.this,         
						RingtoneManager.TYPE_NOTIFICATION,     
						getUri());           
				Toast.makeText(             
						RingtoneSelectActivity.this,     
						R.string.default_notification_success_message,             
						Toast.LENGTH_SHORT)                   
						.show();       
			}    
		}    
		
		private Uri getUri(){      
			//Get the uri of the item that is in the row         
			Cursor c = mAdapter.getCursor();     
			int uriIndex = c.getColumnIndex(          
					"\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");       
			if (uriIndex == -1) {           
				uriIndex = c.getColumnIndex(                 
						"\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");   
			}       
			
			String itemUri = c.getString(uriIndex) + "/" +     
			c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));   
			return (Uri.parse(itemUri));     
		}    
 
		private void confirmDelete() {           
			Cursor c = mAdapter.getCursor();       
			int artistIndex = c.getColumnIndexOrThrow(  
              MediaStore.Audio.Media.ARTIST);      
			String artist = c.getString(c.getColumnIndexOrThrow(         
					MediaStore.Audio.Media.ARTIST));       
			CharSequence ringshakeArtist =           
					getResources().getText(R.string.artist_name);   
			CharSequence message;       
			if (artist.equals(ringshakeArtist)) {         
				message = getResources().getText(            
						R.string.confirm_delete_ringshake);  
			} else {          
				message = getResources().getText(           
						R.string.confirm_delete_non_ringshake);    
			}         
			CharSequence title;     
			if (0 != c.getInt(c.getColumnIndexOrThrow(     
					MediaStore.Audio.Media.IS_RINGTONE))) {        
				title = getResources().getText(R.string.delete_ringtone);     
			}  
			
			new AlertDialog.Builder(RingtoneSelectActivity.this)                  
				.setMessage(message)         
				.setPositiveButton(                
						R.string.delete_ok_button,           
						new DialogInterface.OnClickListener() {   
							public void onClick(DialogInterface dialog,          
									int whichButton) {                     
								onDelete();                    
							}                
						})          
				.setNegativeButton(      
						R.string.delete_cancel_button,           
						new DialogInterface.OnClickListener() {                
							public void onClick(DialogInterface dialog,           
									int whichButton) {                   
							}                 
						})             
				.setCancelable(true)            
				.show();   
		}      
		
		private void onDelete() {        
			Cursor c = mAdapter.getCursor();      
			int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);     
			String filename = c.getString(dataIndex);         
			int uriIndex = c.getColumnIndex(               
					"\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");       
			if (uriIndex == -1) {           
				uriIndex = c.getColumnIndex(                 
						"\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");      
			}        
			if (uriIndex == -1) {        
				showFinalAlert(getResources().getText(R.string.delete_failed));  
				return;       
			}         
			if (!new File(filename).delete()) {     
				showFinalAlert(getResources().getText(R.string.delete_failed));         
			}         
			
			String itemUri = c.getString(uriIndex) + "/" +        
			c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));         
			getContentResolver().delete(Uri.parse(itemUri), null, null);    
		}   
		
		private void showFinalAlert(CharSequence message) {        
			new AlertDialog.Builder(RingtoneSelectActivity.this)     
			.setTitle(getResources().getText(R.string.alert_title_failure))        
			.setMessage(message)       
			.setPositiveButton(              
					R.string.alert_ok_button,              
					new DialogInterface.OnClickListener() {             
						public void onClick(DialogInterface dialog,                     
								int whichButton) {                     
							finish();                   
						}               
					})              
					.setCancelable(false)              
					.show();   
		}      
	
			private void startManualViber() {
			Cursor c = mAdapter.getCursor(); 
			int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);     
			String filename = c.getString(dataIndex);     
			try {           
				Intent intent = new Intent(Intent.ACTION_EDIT,          
						Uri.parse(filename));         
				intent.setClassName(                    
						"com.ringshake",             
					"com.ringshake.ManualVibeActivity");	
				startActivityForResult(intent, REQUEST_CODE_EDIT);    
			} catch (Exception e) {         
				Log.e("RingShake", "Couldn't start editor");    
			}    
		}     
		
		private Cursor getInternalAudioCursor(String selection,           
				String[] selectionArgs) {     
			return managedQuery(             
					MediaStore.Audio.Media.INTERNAL_CONTENT_URI,      
					INTERNAL_COLUMNS,              
					selection,               
					selectionArgs,              
					MediaStore.Audio.Media.DEFAULT_SORT_ORDER);    
		}    
		
		private Cursor getExternalAudioCursor(String selection,       
				String[] selectionArgs) {        
			return managedQuery(     
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,     
					EXTERNAL_COLUMNS,              
					selection,                
					selectionArgs,               
					MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  
		}     
		
		Cursor createCursor(String filter) {        
			ArrayList<String> args = new ArrayList<String>();     
			String selection;      
			if (mShowAll) {         
				selection = "(_DATA LIKE ?)";          
				args.add("ringtones");       
			} else {            
				selection = "(";            
				for (String extension : CheapSoundFile.getSupportedExtensions()) {       
					args.add("%Ringtone%" + extension);                
					if (selection.length() > 1) {                    
						selection += " OR ";          
					}                
					selection += "(_DATA LIKE ?)";            
				}             
				selection += ")";              
				selection = "(" + selection + ") AND (_DATA NOT LIKE ?)";     
				args.add("%espeak-data/scratch%");       
			}         
			if (filter != null && filter.length() > 0) {            
				filter = "%" + filter + "%";          
				selection =                 
						"(" + selection + " AND " +       
						"((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))";        
				args.add(filter);         
				args.add(filter);           
				args.add(filter);         
			}        
			
			String[] argsArray = args.toArray(new String[args.size()]);        
			Cursor external = getExternalAudioCursor(selection, argsArray);      
			Cursor internal = getInternalAudioCursor(selection, argsArray);        
			Cursor c = new MergeCursor(new Cursor[] {          
					getExternalAudioCursor(selection, argsArray),      
					getInternalAudioCursor(selection, argsArray)});       
			startManagingCursor(c);    
			return c;    
		}    
		
		public void beforeTextChanged(CharSequence s, int start,    
				int count, int after) {     
		}     
		
		public void onTextChanged(CharSequence s,          
				int start, int before, int count) {    
		}     
		
		public void afterTextChanged(Editable s) {         
			refreshListView();     
		}      
		
		private void refreshListView() {         
			String filterStr = mFilter.getText().toString();   
			mAdapter.changeCursor(createCursor(filterStr));    
		}     
		
		private static final String[] INTERNAL_COLUMNS = new String[] {      
			MediaStore.Audio.Media._ID,     
			MediaStore.Audio.Media.DATA,      
			MediaStore.Audio.Media.TITLE,       
			MediaStore.Audio.Media.ARTIST,      
			MediaStore.Audio.Media.ALBUM,        
			MediaStore.Audio.Media.IS_RINGTONE,     
			"\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\""   
		};    
		
		private static final String[] EXTERNAL_COLUMNS = new String[] {    
			MediaStore.Audio.Media._ID,     
			MediaStore.Audio.Media.DATA,     
			MediaStore.Audio.Media.TITLE,     
			MediaStore.Audio.Media.ARTIST,      
			MediaStore.Audio.Media.ALBUM,        
			MediaStore.Audio.Media.IS_RINGTONE,     
			"\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\""    
		}; 
	
} 
