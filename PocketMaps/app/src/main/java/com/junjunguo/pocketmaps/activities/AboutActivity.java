package com.junjunguo.pocketmaps.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.junjunguo.pocketmaps.R;

public class AboutActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionCode = getVersionString();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/junjunguo/PocketMaps/")));
            }
        });
        TextView v = (TextView) findViewById(R.id.version_text);
        v.setText(v.getText() + " " + versionCode);
    }
    
    String getVersionString()
    {
        try
        {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return "v" + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {}
        return "";
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
