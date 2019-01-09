package com.junjunguo.pocketmaps.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.junjunguo.pocketmaps.R;

public class AboutActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction
                // ("Action", null)
                //                        .show();

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/junjunguo/PocketMaps/")));

            }
        });

        //         set status bar
//        new SetStatusBarColor().setSystemBarColor(findViewById(R.id.statusBarBackgroundSettings),
//                getResources().getColor(R.color.my_primary_dark), this);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
