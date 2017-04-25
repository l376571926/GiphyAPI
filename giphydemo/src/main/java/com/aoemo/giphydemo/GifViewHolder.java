package com.aoemo.giphydemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by liyiwei
 * on 2017/4/13.
 */

public class GifViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Runnable {
    private static final String AUTHORITY = "com.aoemo.giphydemo";
    private List<GifBean> gifBeanList;
    private ImageView mImageView;
    private Context mContext;
    private GifBean mCurrentBean;

    public GifViewHolder(View itemView, List<GifBean> gifBeanList) {
        super(itemView);
        this.gifBeanList = gifBeanList;
        mImageView = (ImageView) itemView.findViewById(R.id.imageView);
        mImageView.setOnClickListener(this);
        mContext = mImageView.getContext();
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GifBean gifBean = gifBeanList.get(position);
        mCurrentBean = gifBean;
        String url = gifBean.getUrl();
        String url_still = gifBean.getUrl_still();
        GifRequestBuilder<String> requestBuilder = Glide.with(mContext)
                .load(url_still)
                .asGif()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE);
        Glide.with(mContext)
                .load(url)
                .asGif()
                .placeholder(R.drawable.gif_loading)
                .thumbnail(requestBuilder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
    }

    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        Glide.clear(mImageView);
    }

    @Override
    public void onClick(View v) {
        new Thread(this).start();
    }

    @Override
    public void run() {
        String url = mCurrentBean.getUrl();
        try {
            File file = Glide.with(mContext)
                    .load(url)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();

            String fileName = file.getPath().substring(file.getPath().lastIndexOf("/"), file.getPath().length());
            String newFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;

            copyFile(file.getPath(), newFilePath);

//            Uri uri = FileProvider.getUriForFile(mContext, AUTHORITY, new File(newFilePath));
            Uri uri = Uri.fromFile(new File(newFilePath));

            Intent intent = new Intent(Intent.ACTION_SEND);
            //方式1
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setType("image/gif");
            //方式2
            intent.setDataAndType(uri, "image/gif");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mContext.startActivity(intent);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            KLog.e(e);
        }

    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }
}
