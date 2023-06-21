package com.example.bigtemi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;

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
    private boolean player_one_win, player_one_lose, player_two_win, player_two_lose, gamestart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.big);
        //게임 종료시 초기값 0으로 설정
        databaseReference.child("time_over").setValue(0);

        //playTemiSound의 상황별 조건문을 위한 초기화
        gamestart = true;
        player_one_win = false;
        player_one_lose = false;
        player_two_win = false;
        player_two_lose = false;

        //temi앱에서 시작버튼을 눌렀을 때(game level에 따라 음성파일이 달라짐)
        databaseReference.child("game").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                game = Integer.parseInt(rdata.toString());
                //난이도 : 쉬움
                if(game == 1) {
                    level = 1;
                    playTemiSound();
                    gamestart = false;
                    rest(6000);
                }
                //난이도 : 중간
                else if (game == 2) {
                    level = 2;
                    playTemiSound();
                    gamestart = false;
                    rest(4000);
                }
                //난이도 : 어려움
                else if (game == 3) {
                    level = 3;
                    playTemiSound();
                    gamestart = false;
                    rest(2000);
                }
                //게임 종료 버튼 클릭시를 위한 변수값 초기화
                else if (game == 0){
                    gamestart = true;
                    player_one_win = false;
                    player_one_lose = false;
                    player_two_win = false;
                    player_two_lose = false;
                    count = 3;
                }
                //temi에게 오는 신호인 game 변수값 초기화
                game = -1;
                //조교temi나 아두이노한테 센서 on으로 변경 신호 보냄
                if(count >0){
                    databaseReference.child("해당코드작성").setValue(1);
                }
                else{
                    mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gameover);
                    mediaPlayer.start();
                    //조교temi에게 게임종료 신호 전송
                    databaseReference.child("time_over").setValue(1);
                    //아두이노에게도 필요하면 전송
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //조교 temi로 부터 신호가 와서 1번 참가자 승리 음성 출력하고 게임 종료
        databaseReference.child("win/one").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                one_win = Integer.parseInt(rdata.toString());
                if(one_win == 1){
                    gamestart = false;
                    player_one_win = true;
                    GameoverSound();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 1번 참가자 탈락 음성 출력 후 게임 재개
        databaseReference.child("member/one").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                one_lose = Integer.parseInt(rdata.toString());
                if (one_lose == 0) {
                    gamestart = false;
                    player_one_lose = true;
                    GameoverSound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //조교 temi로 부터 신호가 와서 2번 참가자 승리 음성 출력하고 게임 종료
        databaseReference.child("win/two").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                two_win = Integer.parseInt(rdata.toString());
                if(two_win == 1){
                    gamestart = false;
                    player_two_win = true;
                    GameoverSound();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //조교 temi로 부터 신호가 와서 1번 참가자 탈락 음성 출력 후 게임 재개
        databaseReference.child("member/two").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object rdata = snapshot.getValue();
                two_lose = Integer.parseInt(rdata.toString());
                if(two_lose == 0){
                    gamestart = false;
                    player_two_lose = true;
                    GameoverSound();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GameoverSound(){
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
    }
    private void playTemiSound() {
        //게임 시작 버튼 클릭 시 "게임을 시작합니다" 음성 출력
        if (gamestart) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.gamestart);
            mediaPlayer.start();
        }

        rest(5000);

        //level에 따른 "무궁화 꽃이 피었습니다" 음성출력, 단 count가 0이 될 시 게임 종료 음성 출력
        if (level == 1) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.one);
        } else if (level == 2) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.two);
        } else if (level == 3) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.three);
        }
        //횟수 초과로 인해 while루프를 빠져 나간 후 탈락자 처리.
        mediaPlayer.start();
        count--;
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