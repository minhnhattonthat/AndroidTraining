package com.nhatton.threadsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ProgressBar progressBar1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv1 = (ImageView) findViewById(R.id.image_view_1);
        iv1.setOnClickListener(loadImage);
        progressBar1 = (ProgressBar) findViewById(R.id.progress_bar_1);
        progressBar1.setVisibility(View.INVISIBLE);
        progressBar1.setMax(10);

        iv2 = (ImageView) findViewById(R.id.image_view_2);
        iv2.setOnClickListener(loadImage);

        iv3 = (ImageView) findViewById(R.id.image_view_3);
        iv3.setOnClickListener(loadImage);

        iv4 = (ImageView) findViewById(R.id.image_view_4);
        iv4.setOnClickListener(loadImage);

    }

    private View.OnClickListener loadImage = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.image_view_1:
                    progressBar1.setVisibility(View.VISIBLE);
                    progressBar1.setProgress(0);
                    new DownloadImageTask(iv1).
                            execute("http://www.freedigitalphotos.net/images/previews/childs-eye-examination-10061829.jpg");
                    break;
                case R.id.image_view_2:
                    new DownloadImageTask(iv2).
                            execute("http://www.freedigitalphotos.net/images/previews/a-pile-of-pine-nuts-100277153.jpg");
                    break;
                case R.id.image_view_3:
                    new DownloadImageTask(iv3).
                            execute("http://www.freedigitalphotos.net/images/previews/cactus-plants-in-minimal-garden-100464933.jpg");
                    break;
                case R.id.image_view_4:
                    new DownloadImageTask(iv4).
                            execute("http://www.freedigitalphotos.net/images/previews/carunda-or-karonda-isolated-on-white-background-100420872.jpg");
                    break;
            }
        }
    };

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
        ImageView imageView;

        DownloadImageTask(ImageView bmImage) {
            this.imageView = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            return loadImageFromNetwork(urls[0]);
        }

        protected void onPostExecute(Bitmap result) {
            progressBar1.setVisibility(View.GONE);
            imageView.setImageBitmap(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar = new ProgressBar(MainActivity.this);
            progressBar.getProgress();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar1.setProgress(values[0]);
        }
    }

    private Bitmap loadImageFromNetwork(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
