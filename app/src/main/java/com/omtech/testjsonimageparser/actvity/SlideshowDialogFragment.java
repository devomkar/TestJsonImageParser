package com.omtech.testjsonimageparser.actvity;

/**
 * Created by Omkar on 04/01/2017.
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.omtech.testjsonimageparser.R;
import com.omtech.testjsonimageparser.helper.ImageFileProvider;
import com.omtech.testjsonimageparser.model.Image;


public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<Image> images;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private TextView lblCount, lblTitle, lblDate;
    private int selectedPosition = 0,check_action_bit=0;
    private ImageButton share_image_btn,download_image_btn;

    static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_slider, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        lblCount = (TextView) v.findViewById(R.id.lbl_count);
        lblTitle = (TextView) v.findViewById(R.id.title);
        lblDate = (TextView) v.findViewById(R.id.date);
        share_image_btn = (ImageButton) v.findViewById(R.id.share_btn);
        download_image_btn = (ImageButton) v.findViewById(R.id.download_btn);

        images = (ArrayList<Image>) getArguments().getSerializable("images");
        selectedPosition = getArguments().getInt("position");

        Log.e(TAG, "position: " + selectedPosition);
        Log.e(TAG, "images size: " + images.size());

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return v;
    }

    private void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //  page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + images.size());

        final Image image = images.get(position);
        lblTitle.setText(image.getName());
        lblDate.setText(image.getTimestamp());

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_NoActionBar);
    }

    //  adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container,final int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            final ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);

            final Image image = images.get(position);

            Glide.with(getActivity()).load(image.getLarge())
                    .placeholder(R.drawable.ic_crop_original_cyan_600_48dp)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPreview);

            container.addView(view);
            share_image_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Image shareimage = images.get(position-1);
                    check_action_bit=0;
                    new DownloadImageFromCacheTask(getContext(),check_action_bit).execute(shareimage.getLarge());
                }
            });
            download_image_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Image downloadimg = images.get(position-1);
                    check_action_bit=1;
                    new DownloadImageFromCacheTask(getContext(),check_action_bit).execute(downloadimg.getLarge());
                }
            });
            return view;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class DownloadImageFromCacheTask extends AsyncTask<String, Void, File> {
        private final Context context;
        private final int checbt;

        public DownloadImageFromCacheTask(Context context,int checkfilebit) {
            this.context = context;
            this.checbt = checkfilebit;
        }

        @Override
        protected File doInBackground(String... params) {

            FutureTarget<File> future = Glide.with(getContext())
                    .load(params[0])
                    .downloadOnly(500, 500);

            File file = null;
            try {
                file = future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return file;
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return ;
            }

            Uri uri = ImageFileProvider.getUriForFile(getContext(), "com.omtech.testjsonimageparser.share", result);
            if(checbt==0){
            share(uri);}else {
            savefile(uri);}
        }

        private void share(Uri result) {


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, result);
            context.startActivity(Intent.createChooser(intent, "Share image"));
        }
        void savefile(Uri sourceuri)
        {
            Toast.makeText(context,"Ssave",Toast.LENGTH_SHORT).show();

        }
    }
}