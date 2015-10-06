package com.github.rodrigohenriques.samples;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rodrigohenriques.samples.customview.CoordinatedImageView;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ScrollingActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    @Bind(R.id.backdrop)
    ImageView imageViewBackdrop;

    @Bind(R.id.logo)
    CoordinatedImageView coordinatedImageView;

    @Bind(R.id.toolbar_title)
    TextView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatedImageView.setOnChangeVisibilityListener(new CoordinatedImageView.OnChangeVisibilityListener() {
            @Override
            public void onHide() {
                titleView.setText("Fulano de Tal");
            }

            @Override
            public void onShow() {
                titleView.setText("");
            }
        });

        Picasso.with(this).load("http://i.imgur.com/DvpvklR.png").into(imageViewBackdrop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
