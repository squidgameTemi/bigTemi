package com.example.bigtemi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class MainActivity extends AppCompatActivity implements OnRobotReadyListener {
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();
    private Robot robot;
    public MainActivity() {this.robot = Robot.getInstance();}
    public Robot getRobot() {return robot;}
    //private boolean waitForSignal = false;
    MediaPlayer mediaPlayer;
    public int level,player;
    private boolean signal, player_lose, player_win,gamestart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference.child("app").child("start").setValue(0);
        databaseReference.child("temi").child("loss").setValue(0);
        databaseReference.child("temi").child("win").setValue(0);
        signal = true;
        gamestart = true;
        player_lose = false;
        player_win = false;


        //temi앱에서 시작버튼을 눌렀을 때(game level에 따라 음성파일이 달라짐) + 조교temi가 탈락자를 처리한 이후
        databaseReference.child("app/start").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playTemiSound();
                gamestart = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 테미가 탈락자를 처리하고 다시 게임을 진행하는 신호를 줌.
        databaseReference.child("temi/continue").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playTemiSound();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //조교 temi로 부터 신호가 와서 참가자 탈락 음성 출력
        databaseReference.child("temi/lose").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                signal = false;
                player_lose = true;
                playTemiSound();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //조교 temi로 부터 신호가 와서 참가자 승리를 울리고 게임 종료
        databaseReference.child("temi/win").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                signal = false;
                player_win = true;
                playTemiSound();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void playTemiSound() {
        if(gamestart){
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gamestart);
            mediaPlayer.start();
        }

        while(signal){
            if(level == 1){
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one);
            } else if (level == 2) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two);
            } else if (level == 3) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.three);
            }
            mediaPlayer.start();

            /*5초간 파이어베이스를 통해 신호가 오기를 기다리다가 오지 않으면 다시 while loop를 진행.*/
        }

        if(player_lose){
            if(player == 1){
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one_lose);
                mediaPlayer.start();
            } else if (player == 2) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two_lose);
                mediaPlayer.start();
            }

        }

        if(player_win){
            if(player == 1){
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one_win);
                mediaPlayer.start();
            } else if (player == 2) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two_win);
                mediaPlayer.start();
            }
        }



        /*
        // 5초 동안 다른 temi들의 신호를 대기
        waitForSignal = true;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (waitForSignal) {
                    // 다른 temi들로부터의 신호가 없는 경우, temi.mp4 음성 출력
                    playTemiSound();
                }
                else{

                }
            }
        }, 5000);*/
    }
    protected void onStart(){
        super.onStart();
        robot.addOnRobotReadyListener(this);
    }

    protected void onStop(){
        super.onStop();
        robot.addOnRobotReadyListener(this);
    }

    public void onRobotReady(boolean isReady){
        if(isReady){
            try{
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch(PackageManager.NameNotFoundException e){
                throw new RuntimeException(e);
            }
        }
    }
}