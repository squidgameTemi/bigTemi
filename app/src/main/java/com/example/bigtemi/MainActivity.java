package com.example.bigtemi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
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
    private ImageView imageView;
    public MainActivity() {this.robot = Robot.getInstance();}
    public Robot getRobot() {return robot;}

    MediaPlayer mediaPlayer;
    int level,game,one_win,one_lose,two_win,two_lose;
    int count = 3;
    private boolean signal, player_one_win, player_one_lose, player_two_win, player_two_lose, gamestart,stop,count_over;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.big);
        databaseReference.child("time_over").setValue(0);
        signal = true;
        gamestart = true;
        player_one_win = false;
        player_one_lose = false;
        player_two_win = false;
        player_two_lose = false;
        count_over = false;
        stop = false;

        //temi앱에서 시작버튼을 눌렀을 때(game level에 따라 음성파일이 달라짐) + 조교temi가 탈락자를 처리한 이후
        databaseReference.child("game").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                game = Integer.parseInt(rdata.toString());
                if(game == 1) {
                    level = 1;
                    playTemiSound();
                    gamestart = false;
                } else if (game == 2) {
                    level = 2;
                    playTemiSound();
                    gamestart = false;
                } else if (game == 3) {
                    level = 3;
                    playTemiSound();
                    gamestart = false;
                } else if (game == 0){
                    signal = true;
                    gamestart = true;
                    player_one_win = false;
                    player_one_lose = false;
                    player_two_win = false;
                    player_two_lose = false;
                    count_over = false;
                    stop = false;
                    count = 3;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 1번 참가자 승리 음성 출력
        databaseReference.child("win/one").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                one_win = Integer.parseInt(rdata.toString());
                if(one_win == 1){
                    stop = true;
                    signal = false;
                    gamestart = false;
                    player_one_win = true;
                    playTemiSound();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 1번 참가자 탈락 음성 출력
        databaseReference.child("member/one").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                one_lose = Integer.parseInt(rdata.toString());
                if(one_lose == 0) {
                    stop = true;
                    gamestart = false;
                    signal = false;
                    player_one_lose = true;
                    playTemiSound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 2번 참가자 승리를 울리고 게임 종료
        databaseReference.child("win/two").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                two_win = Integer.parseInt(rdata.toString());
                if(two_win == 1){
                    stop = true;
                    signal = false;
                    player_two_win = true;
                    playTemiSound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 2번 참가자 탈락 음성 출력
        databaseReference.child("member/two").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                two_lose = Integer.parseInt(rdata.toString());
                if(two_lose == 0){
                    stop = true;
                    signal = false;
                    player_two_lose = true;
                    playTemiSound();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void playTemiSound() {
        if (gamestart) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gamestart);
            mediaPlayer.start();

        }
        rest(5000);

        while (signal) {
            if (level == 1) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one);
                imageView.setImageResource(R.drawable.big2);
            } else if (level == 2) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two);
                imageView.setImageResource(R.drawable.big2);
            } else if (level == 3) {
                mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.three);
                imageView.setImageResource(R.drawable.big2);
            }
            if (count == 0) {
                imageView.setImageResource(R.drawable.big1);
                count_over = true;
                break;
            }
            mediaPlayer.start();
            count--;

            /*5초간 파이어베이스를 통해 신호가 오기를 기다리다가 오지 않으면 다시 while loop를 진행.*/
            rest(10000);

            if (stop) {
                imageView.setImageResource(R.drawable.big1);
                break;
            }
            if(count == 0){
                count_over = true;
            }
        }

        if (player_one_lose) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one_lose);
            mediaPlayer.start();
            player_one_lose = false;
        }
        if (player_two_lose) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two_lose);
            mediaPlayer.start();
            player_two_lose = false;
        }
        //종료조건
        if (player_one_win) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one_win);
            mediaPlayer.start();
            player_one_win = false;
        }
        if (player_two_win) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two_win);
            mediaPlayer.start();
            player_two_win = false;
        }
        if (count_over) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameover);
            mediaPlayer.start();
            databaseReference.child("time_over").setValue(1);
        }
    }
    protected void onStart(){
        super.onStart();
        robot.addOnRobotReadyListener(this);
    }

    protected void onStop(){
        super.onStop();
        robot.addOnRobotReadyListener(this);
    }

    private void rest(int time){
        CountDownTimer timer = new CountDownTimer(time, 1000) {
            public void onTick(long millisUntilFinished) {
                // 아무것도 하지 않음
            }
            public void onFinish() {
            }
        }.start();

        try {
            Thread.sleep(time); // 5초간 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.cancel(); // 타이머 취소
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