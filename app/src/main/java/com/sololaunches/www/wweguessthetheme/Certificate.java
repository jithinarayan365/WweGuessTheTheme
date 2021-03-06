package com.sololaunches.www.wweguessthetheme;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Certificate extends AppCompatActivity {

    private boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
            //  System.exit(0);

        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);


        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_certificate);

        // widget
        TextView score = (TextView) findViewById(R.id.score);
        TextView playerName = (TextView) findViewById(R.id.player);
        ImageButton nxt_btn = (ImageButton) findViewById(R.id.nxt);
        ImageButton rate = (ImageButton) findViewById(R.id.rate);

        String points = (String) getIntent().getSerializableExtra("points");
        String player = (String) getIntent().getSerializableExtra("player");
        score.setText("Score :" + points + " points");
        playerName.setText(" " + player);


        nxt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName())));

                } catch (Exception e1) {
                    Toast toast = Toast.makeText(getApplicationContext(), "you are unable to open the link", Toast.LENGTH_LONG);
                    toast.show();

                }
            }
        });


    }
}
