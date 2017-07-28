package com.example.natepowers.cameragallerysampleapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    final int CAMERA_CAPTURE = 1;
    private Uri picUri;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private GridView grid;
    private List<String> listOfImagesPath;

    public static final String GridViewDemo_ImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GridViewDemo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grid = (GridView) findViewById(R.id.gridviewimg);

        listOfImagesPath = null;
        listOfImagesPath = RetriveCapturedImagePath();
        if (listOfImagesPath != null) {
            grid.setAdapter(new ImageListAdapter(this, listOfImagesPath));
        }
    }

    @Override
    public void onClick(View arg0) {
        try {

            //use standard intent to capture an image
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //we will handle the returned data in onActivityResult
            startActivityForResult(captureIntent, CAMERA_CAPTURE);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if (requestCode == CAMERA_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap thePic = extras.getParcelable("data");
                String imgcurTime = dateFormat.format(new Date());
                File imageDirectory = new File(GridViewDemo_ImagePath);
                imageDirectory.mkdirs();
                String _path = GridViewDemo_ImagePath + imgcurTime + ".jpg";
                try {
                    FileOutputStream out = new FileOutputStream(_path);
                    thePic.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.getMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listOfImagesPath = null;
                listOfImagesPath = RetriveCapturedImagePath();
                if (listOfImagesPath != null) {
                    grid.setAdapter(new ImageListAdapter(this, listOfImagesPath));
                }
            }
        }
    }

    private List<String> RetriveCapturedImagePath() {
        List<String> tFileList = new ArrayList<String>();
        File f = new File(GridViewDemo_ImagePath);
        if (f.exists()) {
            File[] files = f.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory())
                        continue;
                    tFileList.add(file.getPath());
                }
            }

        }
        return tFileList;
    }

    public class ImageListAdapter extends BaseAdapter {
        private Context context;
        private List<String> imgPic;

        public ImageListAdapter(Context c, List<String> thePic) {
            context = c;
            imgPic = thePic;
        }

        public int getCount() {
            if (imgPic != null)
                return imgPic.size();
            else
                return 0;
        }

        //---returns the ID of an item---
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (position == (getCount() - 1)) {
                imageView = new ImageView(context);
                imageView.setBackgroundResource(R.drawable.ic_add_photo_icon);
                imageView.setLayoutParams(new GridView.LayoutParams(320, 320));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e(TAG, "onClick: clicked!");
                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //we will handle the returned data in onActivityResult
                        startActivityForResult(captureIntent, CAMERA_CAPTURE);
                    }
                });
            } else {
                BitmapFactory.Options bfOptions = new BitmapFactory.Options();
                bfOptions.inDither = false;                     //Disable Dithering mode
                bfOptions.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
                bfOptions.inInputShareable = true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
                bfOptions.inTempStorage = new byte[32 * 1024];
                if (convertView == null) {
                    imageView = new ImageView(context);
                    imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imageView.setPadding(0, 0, 0, 0);
                } else {
                    imageView = (ImageView) convertView;
                }

                FileInputStream fs = null;
                Bitmap bm;

                try {
                    fs = new FileInputStream(new File(imgPic.get(position).toString()));

                    if (fs != null) {
                        bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
                        imageView.setImageBitmap(bm);
                        imageView.setId(position);
                        imageView.setLayoutParams(new GridView.LayoutParams(320, 320));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return imageView;
        }
    }
}

