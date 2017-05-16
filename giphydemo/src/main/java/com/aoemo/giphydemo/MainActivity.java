package com.aoemo.giphydemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.socks.library.KLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String mSearchAPI = "http://api.giphy.com/v1/gifs/search?api_key=dc6zaTOxFJmzC&limit=100&q=";
    private RecyclerView mRecyclerView;
    private List<GifBean> mGifBeanList;
    private GifAdapter mGifAdapter;
    private EditText mEditText;
    private String mSearchUrl;
    private OkHttpClient mOkHttpClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEditText = (EditText) findViewById(R.id.editText);
        findViewById(R.id.deleteIv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mGifBeanList = new ArrayList<>();
        mGifAdapter = new GifAdapter(this, mGifBeanList);
        mRecyclerView.setAdapter(mGifAdapter);

        mOkHttpClient = new OkHttpClient();

        mSearchUrl = mSearchAPI + "minions";
        mEditText.setText("minions");

        /**
         * 当快速点击FAB时会出现这个错误
         * ava.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder{ccdcdb0 position=0 id=-1, oldPos=0, pLpos:0 scrap [attachedScrap] tmpDetached not recyclable(1) no parent}
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                String string = mEditText.getText().toString();
                int size = mGifBeanList.size();
                if (size != 0) {
                    mGifBeanList.clear();
                    mGifAdapter.notifyItemRangeRemoved(0, size);
                }
                if (TextUtils.isEmpty(string)) {
                    return;
                }
                mSearchUrl = mSearchAPI + string;
                new Thread(MainActivity.this).start();
                mProgressDialog.show();
            }
        });
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        new Thread(this).start();
        mProgressDialog.show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 203);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 203) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Handler mHandler = new Handler();

    @Override
    public void run() {
        Request request = new Request.Builder()
                .url(mSearchUrl)
                .method("GET", null)
                .build();
        mOkHttpClient
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        KLog.e(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
//                        KLog.e(json);
                        GiphyBean giphyBean = new Gson().fromJson(json, GiphyBean.class);
                        List<GiphyBean.DataBean> dataBeanList = giphyBean.getData();
                        int count = giphyBean.getPagination().getCount();
                        if (count == 0) {
                            KLog.e(json);
                            Looper.prepare();
                            Toast.makeText(MainActivity.this, "无数据---》" + mSearchUrl, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            return;
                        }
                        final List<GifBean> gifBeanList = new ArrayList<>();
                        for (int i = 0; i < dataBeanList.size(); i++) {
                            GiphyBean.DataBean dataBean = dataBeanList.get(i);
                            String url = dataBean.getImages().getFixed_width_small().getUrl();
                            String url_still = dataBean.getImages().getFixed_width_small_still().getUrl();

                            GifBean gifBean = new GifBean();
                            gifBean.setUrl(url);
                            gifBean.setUrl_still(url_still);
                            gifBeanList.add(gifBean);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mProgressDialog.dismiss();
                                int size = mGifBeanList.size();
                                int size1 = gifBeanList.size();
                                if (size != 0) {
                                    mGifBeanList.clear();
                                    mGifAdapter.notifyItemRangeRemoved(0, size);
                                } else {
                                    mGifBeanList.addAll(gifBeanList);
                                    mGifAdapter.notifyItemRangeChanged(0, size1);
                                }
                            }
                        });
                    }
                });
    }
}
