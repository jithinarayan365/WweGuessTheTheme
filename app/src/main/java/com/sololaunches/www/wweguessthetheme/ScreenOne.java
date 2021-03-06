package com.sololaunches.www.wweguessthetheme;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import com.startapp.android.publish.adsCommon.VideoListener;

import java.util.ArrayList;

public class ScreenOne extends AppCompatActivity {

    WweDBAdapter wweDBAdapter;
    WweConvenience wweConvenience;
    boolean firstSignal;
    ImageButton playBtn, check, resolve, hints, add_Coins, share, nxt_btn;
    SeekBar seekbar;
    MediaPlayer mediaPlayer;
    Handler handler;
    int songID;
    Runnable runnable;
    boolean pauseFlag, playFlag;
    String PACKAGE_NAME;
    InputMethodManager imm;
    EditText textVal;
    TextView congrats, level, coinStats, trivia;
    WweMainBean beanUnsolved;
    PlayerStatsBean playerStatsBean, playerStatsBean2;
    int coins;
    boolean interstitial, videoAd;
    private StartAppAd startAppAdInst;
    private StartAppAd startAppAd;
    private boolean exit = false;


    @Override
    public void onBackPressed() {
        if (exit) {

            mediaPlayer.pause();
            playBtn.setImageResource(R.drawable.play_new);
            pauseFlag = true;
            playFlag = false;
            finish();

        } else {

            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;

            if (mediaPlayer.isPlaying()) {
                playBtn.setImageResource(R.drawable.play_new);
                pauseFlag = true;
                playFlag = false;
                mediaPlayer.pause();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);


        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        // insert here your instructions
        if (mediaPlayer != null) {
            playBtn.setImageResource(R.drawable.pause_new);
            mediaPlayer.pause();

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Make to run your application only in portrait mode
        StartAppSDK.init(this, "203580885", true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |     WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_screen_one);

        // widgets
        trivia = (TextView) findViewById(R.id.trivia);
        textVal = (EditText) findViewById(R.id.textView);
        congrats = (TextView) findViewById(R.id.congrats);
        level = (TextView) findViewById(R.id.level_txt);
        coinStats = (TextView) findViewById(R.id.coin_txt);
        nxt_btn = (ImageButton) findViewById(R.id.nxt_btn);
        resolve = (ImageButton) findViewById(R.id.resolve);
        add_Coins = (ImageButton) findViewById(R.id.add_Coins);
        share = (ImageButton) findViewById(R.id.share);
        playBtn = (ImageButton) findViewById(R.id.play);
        textVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);



        add_Coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // ARE WE CONNECTED TO THE NET
                if (conMgr.getActiveNetworkInfo() != null
                        && conMgr.getActiveNetworkInfo().isAvailable()
                        && conMgr.getActiveNetworkInfo().isConnected()) {
                    startAppAd.showAd();

                } else {
                    Snackbar mySnackbar = Snackbar.make(v, "Make sure you have an active internet connection!", 5000);
                    mySnackbar.show();
                }
            }
        });
        seekbar = (SeekBar) findViewById(R.id.seekBar);



        // startApp
        startAppAdInst = new StartAppAd(this);
        startAppAd = new StartAppAd(this);
        startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO);
        imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        wweDBAdapter = new WweDBAdapter(this, null, null, 1);
        wweConvenience = new WweConvenience();
        playerStatsBean = wweConvenience.getPlayerStats(getApplicationContext());
        Cursor cur2 = wweDBAdapter.getTimeTableForStation();
        PACKAGE_NAME = getApplicationContext().getPackageName();


        if (cur2.getCount() == 0) {
            return;
        }

        beanUnsolved = new WweMainBean();
        while (cur2.moveToNext()) {
            beanUnsolved.setPosition(cur2.getString(0));
            beanUnsolved.setPlayer(cur2.getString(1));
            beanUnsolved.setAlias(cur2.getString(2));
            beanUnsolved.setPath(cur2.getString(3));
            beanUnsolved.setDisplay(cur2.getString(4));
            beanUnsolved.setHint(cur2.getString(5));
            beanUnsolved.setHintStatus(cur2.getString(6));
            beanUnsolved.setAdStatus(cur2.getString(7));
            beanUnsolved.setStatus(cur2.getString(8));
            beanUnsolved.setTrivia(cur2.getString(9));
        }

        songID = getResources().getIdentifier(PACKAGE_NAME + ":raw/" + beanUnsolved.getPlayer(), null, null);
        mediaLoad();

        startAppAd.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                playerStatsBean2 = wweDBAdapter.getPlayerStats();
                int coins = Integer.parseInt(playerStatsBean2.getCoins()) + 50;
                playerStatsBean2.setCoins(String.valueOf(coins));
                coinStats.setText(playerStatsBean2.getCoins());
                wweDBAdapter.updateAddedCoins(playerStatsBean2);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        textVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        trivia.setVisibility(View.INVISIBLE);
        congrats.setVisibility(View.INVISIBLE);
        coinStats.setText(playerStatsBean.getCoins());
        nxt_btn.setVisibility(View.INVISIBLE);
        level.setText("Level -" + beanUnsolved.getPosition());

        handler = new Handler();

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mediaPlayer != null) {
                    seekbar.setMax(mediaPlayer.getDuration());
                    playCycle();
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if (input) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String sub = "WWE guess the theme song ";
                String body = "Download -'guess the WWE theme song'  app from playstore from the link " + "http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
                intent.putExtra(Intent.EXTRA_SUBJECT, sub);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, " CHOOSE USING "));
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    mediaLoad(getApplicationContext());
                }

                if (!playFlag) {
                    playBtn.setImageResource(R.drawable.pause_new);
                    mediaPlayer.start();
                    seekbar.setProgress(0);
                    playCycle();
                    playFlag = true;
                    pauseFlag = false;
                } else if (!pauseFlag) {
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.play_new);
                    pauseFlag = true;
                    playFlag = false;
                }
            }
        });


        textVal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

        coins = Integer.parseInt(playerStatsBean.getCoins());

        resolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins < 50) {
                    Snackbar mySnackbar = Snackbar.make(v, "You dont have enough Coins, Click on add Coins to add few coins", 2000);
                    mySnackbar.show();
                } else {
                    mediaPlayer.stop();
                    Intent intent = new Intent(getApplicationContext(), ResolveActivity.class);
                    intent.putExtra("image", beanUnsolved.getPlayer());
                    intent.putExtra("player", beanUnsolved.getDisplay());
                    intent.putExtra("finalstat", beanUnsolved.getDisplay());
                    intent.putExtra("trivia", beanUnsolved.getTrivia());
                    startActivity(intent);
                    finish();
                }
            }
        });


        hints = (ImageButton) findViewById(R.id.hints);
        hints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WweMainBean mainBean = wweConvenience.getSuperStarBean(beanUnsolved.getPlayer());
                if (beanUnsolved.getHintStatus().equalsIgnoreCase("Y")) {
                    Snackbar mySnackbar = Snackbar.make(v, mainBean.getHint(), 10000);
                    mySnackbar.show();
                } else if (Integer.parseInt(playerStatsBean.getCoins()) < 20) {
                    Snackbar mySnackbar = Snackbar.make(v, "You dont have Enough Coins, watch an AD to add Coins", 2000);
                    mySnackbar.show();
                } else {
                    int coins = Integer.parseInt(playerStatsBean.getCoins()) - 20;
                    playerStatsBean.setCoins(String.valueOf(coins));
                    beanUnsolved.setHintStatus("Y");
                    wweDBAdapter.updateHintUsed(playerStatsBean, mainBean.getPlayer());
                    coinStats.setText(playerStatsBean.getCoins());
                    Snackbar mySnackbar = Snackbar.make(v, mainBean.getHint(), 10000);
                    mySnackbar.show();
                }

            }
        });

        nxt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                Intent intent = new Intent(getApplicationContext(), CongratuationsActivity.class);
                intent.putExtra("coins", playerStatsBean.getCoins());
                intent.putExtra("points", playerStatsBean.getPoints());
                intent.putExtra("ad", beanUnsolved.getAdStatus());
                startActivity(intent);
                finish();
            }
        });

        check = (ImageButton) findViewById(R.id.check);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

                String player = textVal.getText().toString().trim();
                if ("".equals(player)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Enter the players name", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                } else {

                    boolean val = wweConvenience.getResult(beanUnsolved.getPlayer(), player);
                    boolean val2 = false;
                    if (!val) {
                        if (beanUnsolved.getAlias() != null && !("".equals(beanUnsolved.getAlias().trim()))) {
                            val2 = wweConvenience.getResult(beanUnsolved.getAlias(), player);
                        }
                    }

                    if (val || val2) {
                        nxt_btn.setVisibility(View.VISIBLE);
                        ImageView superstar = (ImageView) findViewById(R.id.super_star);
                        int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + beanUnsolved.getPlayer(), null, null);
                        superstar.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));
                        congrats.setVisibility(View.VISIBLE);
                        congrats.setText(beanUnsolved.getDisplay());
                        playBtn.setVisibility(View.INVISIBLE);
                        check.setVisibility(View.INVISIBLE);
                        seekbar.setVisibility(View.INVISIBLE);
                        hints.setVisibility(View.INVISIBLE);
                        resolve.setVisibility(View.INVISIBLE);
                        Snackbar mySnackbar = Snackbar.make(v, "Congratulations, Correct answer", 5000);
                        mySnackbar.show();
                        trivia.setVisibility(View.VISIBLE);
                        trivia.setText(beanUnsolved.getTrivia());
                        imm.hideSoftInputFromWindow(textVal.getWindowToken(), 0);
                        textVal.setVisibility(View.INVISIBLE);
                        int coins = Integer.parseInt(playerStatsBean.getCoins()) + 5;
                        int points = Integer.parseInt(playerStatsBean.getPoints()) + 100;
                        playerStatsBean.setPoints(String.valueOf(points));
                        playerStatsBean.setCoins(String.valueOf(coins));
                        wweDBAdapter.updateStateUp(playerStatsBean, beanUnsolved.getPlayer());


                        Runnable r = new Runnable() {
                            @Override
                            public void run() {

                                if (beanUnsolved.getAdStatus().equals("F")) {
                                    wweDBAdapter.updateFinishGame(playerStatsBean.getPlayer());
                                    Intent intent1 = new Intent(getApplicationContext(), Certificate.class);
                                    intent1.putExtra("points", playerStatsBean.getPoints());
                                    intent1.putExtra("player", playerStatsBean.getPlayer());
                                    startActivity(intent1);
                                    finish();
                                } else {
                                    if (interstitial) {
                                        startAppAdInst.showAd();
                                    } else if (videoAd) {
                                        startAppAd.showAd();
                                    }
                                }
                            }
                        };
                        Handler h = new Handler();
                        h.postDelayed(r, 2500);

                    } else {
                        imm.hideSoftInputFromWindow(textVal.getWindowToken(), 0);
                        textVal.setText("");
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                        textVal.clearFocus();
                        Snackbar mySnackbar = Snackbar.make(v, "Wrong Answer", 5000);
                        mySnackbar.show();
                    }
                }

            }
        });

    }

    private void mediaLoad() {
        mediaPlayer = MediaPlayer.create(this, songID);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void mediaLoad(Context ctxt) {
        mediaPlayer = MediaPlayer.create(ctxt, songID);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }


    public void playCycle() {
        seekbar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();
                    int currentTime = (wweConvenience.getProgressPercentage(totalDuration, currentPosition));
                    seekbar.setProgress(currentTime);
                    if (currentTime == 100) {
                        playBtn.setImageResource(R.drawable.play_new);
                    }
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 50);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
        Runtime.getRuntime().gc();

    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            mediaPlayer = MediaPlayer.create(this, songID);
            playBtn.setImageResource(R.drawable.play_new);
            pauseFlag = true;
            playFlag = false;

        }catch (Exception e){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}