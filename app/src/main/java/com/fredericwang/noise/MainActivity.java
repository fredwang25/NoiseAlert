package com.fredericwang.noise;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;


import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    boolean toggle = false, manToggle=false, doneOnce = false, isOn = false, toggle1=false;
    SoundMeter sm = new SoundMeter();
    int defaultTurnOffTime =  0;
    private static final int REQUEST_PERMISSION= 1;
    InputFilter timeFilter;
     MediaPlayer mp,mpcustom;
     AlertDialog alertDialog;
     boolean cd=false;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public void startrec(){
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.RECORD_AUDIO  },
                    123 );
        }else{
            cooldown(1000);
            isOn=true;
            sm.start();
        }
    }

    public void cooldown(int delay){
        cd=true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                cd=false;
            }
        }, delay);
    }

    CountDownTimer timer=new CountDownTimer(250,20) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            try {

                if(isOn && sm.getAmplitude()>27000 && !cd) {
                    System.out.println(sm.getAmplitude());
                    CheckBox tex=(CheckBox)findViewById(R.id.textBox);
                    CheckBox aud=(CheckBox)findViewById(R.id.audioBox);
                    if(tex.isChecked()) {
                        if(alertDialog!=null && alertDialog.isShowing()){
                            alertDialog.dismiss();

                        }
                        EditText display = (EditText) findViewById(R.id.DisplayText);
                        String t1 = display.getText().toString();
                        alertDialog = new AlertDialog.Builder(MainActivity.this)

                                .setTitle("")
                                .setMessage(t1)
                                .create();
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        }else {
                            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        }
                        alertDialog.show();
                    /*    final Timer timer2 = new Timer();
                        timer2.schedule(new TimerTask() {
                            public void run() {
                                alertDialog.dismiss();
                                timer2.cancel(); //this will cancel the timer of the system
                            }
                        }, 5000);*/
                    }
                    if(aud.isChecked()){
                       Spinner spinner = (Spinner)findViewById(R.id.spinner);
                        String text = spinner.getSelectedItem().toString();

                            if (text.equals("Please be quiet")) {
                                mp.start();
                            } else if (text.equals("Lullaby")) {
                                mpcustom.start();
                            }
                    }
                    cooldown(10000);
                    //here
                }
                timer.start();
            } catch (Exception e) {
                Log.e("Error", "Error: " + e.toString());
            }
        }
    }.start();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp=MediaPlayer.create(MainActivity.this, R.raw.bequiet);
        mpcustom=MediaPlayer.create(MainActivity.this,R.raw.crop);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_PERMISSION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (!Settings.canDrawOverlays(this)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);

            }
        }else if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                    REQUEST_PERMISSION);
        }

       final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);




        timeFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                       int dstart, int dend) {

                if (source.length() > 1 && doneOnce == false) {
                    source = source.subSequence(source.length() - 1, source.length());
                    if (source.charAt(0) >= '0' && source.charAt(0) <= '2') {
                        doneOnce = true;
                        return source;
                    } else {
                        return "";
                    }
                }


                if (source.length() == 0) {
                    return null;// deleting, keep original editing
                }
                String result = "";
                result += dest.toString().substring(0, dstart);
                result += source.toString().substring(start, end);
                result += dest.toString().substring(dend, dest.length());

                if (result.length() > 5) {
                    return "";// do not allow this edit
                }
                boolean allowEdit = true;
                char c;
                if (result.length() > 0) {
                    c = result.charAt(0);
                    allowEdit &= (c >= '0' && c <= '2');
                }
                if (result.length() > 1) {
                    c = result.charAt(1);
                    if (result.charAt(0) == '0' || result.charAt(0) == '1')
                        allowEdit &= (c >= '0' && c <= '9');
                    else
                        allowEdit &= (c >= '0' && c <= '3');
                }
                if (result.length() > 2) {
                    c = result.charAt(2);
                    allowEdit &= (c == ':');
                }
                if (result.length() > 3) {
                    c = result.charAt(3);
                    allowEdit &= (c >= '0' && c <= '5');
                }
                if (result.length() > 4) {
                    c = result.charAt(4);
                    allowEdit &= (c >= '0' && c <= '9');
                }
                return allowEdit ? null : "";
            }

        };
        EditText start = (EditText) findViewById(R.id.starttime);
        EditText end = (EditText) findViewById(R.id.endtime);

        start.setFilters(new InputFilter[]{timeFilter});
        end.setFilters(new InputFilter[]{timeFilter});

        timer.start();
    }


    public class SoundMeter {

        private AudioRecord ar = null;
        private int minSize;

        public void start() {
            minSize= AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
            ar.startRecording();
        }

        public void stop() {
            if (ar != null) {
                ar.stop();
            }
        }

        public double getAmplitude() {
            short[] buffer = new short[minSize];
            ar.read(buffer, 0, minSize);
            int max = 0;
            for (short s : buffer)
            {
                if (Math.abs(s) > max)
                {
                    max = Math.abs(s);
                }
            }
            return max;
        }
    }


    public void mantoggleclick(View view){
        sm.stop();
        toggle1=!toggle1;
        ToggleButton tglbtn = (ToggleButton)findViewById(R.id.toggleButton);
        tglbtn.setChecked(false);
        toggle=false;

        if(toggle1==true){
            startrec();
        }else{
            isOn=false;
            sm.stop();
        }
    }

    public void toggleclick(View view) {
        sm.stop();
        EditText start = (EditText)findViewById(R.id.starttime);
        EditText end = (EditText)findViewById(R.id.endtime);
        int bufferSize=0;
        AudioRecord audio=null;
        int sampleRate = 8000;
        ToggleButton tglbtn = (ToggleButton)findViewById(R.id.toggleButton2);
        tglbtn.setChecked(false);
        ToggleButton thistglbtn = (ToggleButton)findViewById(R.id.toggleButton);
        toggle1=false;
        toggle=!toggle;


        if (toggle) {
            String t1=start.getText().toString();
            String t2=end.getText().toString();
            if((t1==null || t2==null) || (t1.length()<5 || t2.length()<5)){
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)

                        .setTitle("")
                        .setMessage("Please fix the time input issues.")
                        .create();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }else{
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }
                alertDialog.show();
                toggle=false;
                thistglbtn.setChecked(false);
                return;
            }
            int hour1=Integer.parseInt(t1.substring(0,2));
            int hour2=Integer.parseInt(t2.substring(0,2));
            int min1=Integer.parseInt(t1.substring(3,5));
            int min2=Integer.parseInt(t2.substring(3,5));
            int time1=hour1*60+min1;
            int time2=hour2*60+min2;
            Calendar c = Calendar.getInstance();
            int cHour = c.get(Calendar.HOUR_OF_DAY);
            int cMinute = c.get(Calendar.MINUTE);
            int cTime=cHour*60+cMinute;
            if((time1<cTime && cTime<time2) || (time1>time2 && !(time1>cTime && cTime>time2))){
               startrec();
                }else{
                isOn=false;
                sm.stop();
            }
        }else{
            isOn=false;
        }
    }




}
