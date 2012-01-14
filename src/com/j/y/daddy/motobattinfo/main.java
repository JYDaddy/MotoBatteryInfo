package com.j.y.daddy.motobattinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.j.y.daddy.motobattinfo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class main extends Activity {
	
	private static final int delaySecond = 10;
	
	private int di = delaySecond+1;
	
	private Button btn1 = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String VersionStr = "", PkgName = "";
        try {
        	PackageInfo pkgInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
        	VersionStr = pkgInfo.versionName;
        	PkgName = pkgInfo.packageName;
        } catch(NameNotFoundException e) { }
        
        TextView txtinfo = (TextView)findViewById(R.id.txtinfo1);
        //txtinfo.setText(this.getString(R.string.appinfo)+" "+VersionStr);
        txtinfo.setText(this.getString(R.string.appinfo));
        
        //refresh
    	btn1 = (Button)findViewById(R.id.button1);
    	btn1.setOnClickListener(new View.OnClickListener() {
 		   public void onClick(View v) {
 			   BtteryInfomationDisplay();
 		   }
    	});
    	
    	BtteryInfomationDisplay();
    	
    	if(Build.MANUFACTURER.toLowerCase().indexOf("motorola") >= 0) {
    		//refresh per delaySecond
    		hdl.sendEmptyMessage(0);
    		
    	} else {
    		showAlert("Your device is not an Motorola.\nThis application can be use only for Motorola devices.");
    	}
    }
    
    Handler hdl = new Handler() {
    	public void handleMessage(Message m) {
    		hdl.sendEmptyMessageDelayed(0, 1000);
    		if(--di <= 0) {
	    		BtteryInfomationDisplay();
    		}
    		btn1.setText(getString(R.string.refresh)+"("+di+")");
    	}
    };

	@Override
    protected void onDestroy() {
		try{
			//if(_timer != null) { _timer.cancel(); _timer = null; }
			if(hdl != null) hdl.removeMessages(0);
		} catch(Exception e) {
		}
		
		super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
	
	//Battery Information Display
	private void BtteryInfomationDisplay() {
		try {
			di = delaySecond+1;
			
			String[] battinfoitems = this.getResources().getStringArray(R.array.battinfoitems);
			String[] battinfoitemtails = this.getResources().getStringArray(R.array.battinfoitemtails);
			String[] battinfotitles = this.getResources().getStringArray(R.array.battinfotitles);
			
	    	StringBuffer sbuff = new StringBuffer();
	    	String tmpStr = "";
	    	sbuff.append("\n");
	    	
	    	//Phone Info
	    	sbuff.append("[Phone Information]\n");
	    	sbuff.append("Manufacturer: "+Build.MANUFACTURER+"\n");
	    	sbuff.append("Model: "+Build.MODEL+"\n");
	    	sbuff.append("Build ID: "+Build.ID+"\n");
	    	sbuff.append("Build Display: "+Build.DISPLAY+"\n");
	    	sbuff.append("Device: "+Build.DEVICE+"\n");
	    	sbuff.append("\n");
	    	
	    	sbuff.append("[Battery Information]\n");
	    	File tmpFile = null;
	    	String tmpRst = "";
	    	for(int i=0; i<battinfoitems.length; i++) {
	    		tmpFile = new File(this.getString(R.string.battsyspath)+"/"+battinfoitems[i]);
	    		if(tmpFile.exists() && tmpFile.isFile()) {
	    			tmpRst = ExcuteProcessResult("cat "+this.getString(R.string.battsyspath)+"/"+battinfoitems[i]);
	    			if(battinfoitems[i].toLowerCase().equals("temp")) tmpRst = tmpRst.substring(0,2)+"."+tmpRst.substring(2,3);
		    		tmpStr = battinfotitles[i]+" = "+tmpRst+battinfoitemtails[i];
		    		//if(i<battinfoitems.length-1) tmpStr+="\n";
	    			sbuff.append(tmpStr+"\n");
	    		}
	    	}
	    	
	    	//display
	        TextView battinfo = (TextView)findViewById(R.id.battinfo);
	        battinfo.setText(sbuff.toString());
	    	
    	} catch(Exception e) {
    		Log.d(this.getString(R.string.logerrtag),e.toString());
    		showAlert(e.toString());
    		
    	} finally {
    	}
	}
	

    //Alert Popup
	private void showAlert(String msg){
    	new AlertDialog.Builder(this).setTitle("Notification")
        .setMessage(msg)
        .setPositiveButton(this.getString(R.string.ok), new DialogInterface.OnClickListener(){
		    @Override
		    public void onClick( DialogInterface dialog, int which ) {
		        dialog.dismiss();
		    }
		 }).show();
    }
	
	//Process Command Execute
	private String ExcuteProcessResult(String cmd) {
		String rstr = "";
		Process p = null;
		
		try {
	    	p = Runtime.getRuntime().exec(cmd);
        	p.waitFor();
    		rstr = getProcessReturnString(p.getInputStream());
    		p.destroy();
	    	
    	} catch(IOException ie) {
    		Log.d(this.getString(R.string.logerrtag),ie.toString());
    		showAlert(ie.toString());
    		
    	} catch(Exception e) {
    		Log.d(this.getString(R.string.logerrtag),e.toString());
    		showAlert(e.toString());
    		
    	} finally {
    		try {
    			if(p != null) p.destroy();
    		} catch(Exception e) {
    		}
    	}
		
		return rstr==null ? "" : rstr;
	}

	//Process InputStream Convert String
	private String getProcessReturnString(InputStream is) {
		String rstr = "";
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String line="";
			while((line=br.readLine()) != null) {
				if(rstr.length() > 0) rstr += "\n";
				rstr += line;
			}
			br.close();
			isr.close();
			
    	} catch(IOException ie) {
    		Log.d(this.getString(R.string.logerrtag),ie.toString());
    		showAlert(ie.toString());
    		
    	} catch(Exception e) {
    		Log.d(this.getString(R.string.logerrtag),e.toString());
    		showAlert(e.toString());
    		
    	} finally {
    		try {
    			if(br != null) br.close();
    			if(isr != null) isr.close();
    		} catch(Exception e) {
    		}
		}
		
		return rstr==null ? "" : rstr;
	}	
}