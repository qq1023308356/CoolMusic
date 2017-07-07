package me.cool.music.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.cool.music.adapter.OnMoreClickListener;
import me.cool.music.adapter.OnlineMusicAdapter;
import me.cool.music.constants.Extras;
import me.cool.music.enums.LoadStateEnum;
import me.cool.music.executor.DownloadOnlineMusic;
import me.cool.music.executor.PlayOnlineMusic;
import me.cool.music.executor.ShareOnlineMusic;
import me.cool.music.http.HttpCallback;
import me.cool.music.http.HttpClient;
import me.cool.music.model.Music;
import me.cool.music.model.OnlineMusic;
import me.cool.music.model.OnlineMusicList;
import me.cool.music.model.SongListInfo;
import me.cool.music.utils.FileUtils;
import me.cool.music.utils.ImageUtils;
import me.cool.music.utils.ScreenUtils;
import me.cool.music.utils.ToastUtils;
import me.cool.music.utils.ViewUtils;
import me.cool.music.utils.binding.Bind;
import me.cool.music.widget.AutoLoadListView;

public class OnlineMusicActivity extends BaseActivity implements OnItemClickListener
        , OnMoreClickListener, AutoLoadListView.OnLoadListener {
    private static final int MUSIC_LIST_SIZE = 20;

    @Bind(me.cool.music.R.id.lv_online_music_list)
    private AutoLoadListView lvOnlineMusic;
    @Bind(me.cool.music.R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(me.cool.music.R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private View vHeader;
    private SongListInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    private ProgressDialog mProgressDialog;
    private int mOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(me.cool.music.R.layout.activity_online_music);

        mListInfo = (SongListInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());

        init();
        onLoad();
    }

    private void init() {
        vHeader = LayoutInflater.from(this).inflate(me.cool.music.R.layout.activity_online_music_list_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(me.cool.music.R.string.loading));
        ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    @Override
    protected void setListener() {
        lvOnlineMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                lvOnlineMusic.onLoadComplete();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else if (offset == 0) {
                    initHeader();
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                lvOnlineMusic.onLoadComplete();
                if (e instanceof RuntimeException) {
                    // 歌曲全部加载完成
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                if (offset == 0) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                } else {
                    ToastUtils.show(me.cool.music.R.string.load_fail);
                }
            }
        });
    }

    @Override
    public void onLoad() {
        getMusic(mOffset);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        play((OnlineMusic) parent.getAdapter().getItem(position));
    }

    @Override
    public void onMoreClick(int position) {
        final OnlineMusic onlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? me.cool.music.R.array.online_music_dialog_without_download : me.cool.music.R.array.online_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 查看歌手信息
                        artistInfo(onlineMusic);
                        break;
                    case 2:// 下载
                        download(onlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void initHeader() {
        final ImageView ivHeaderBg = (ImageView) vHeader.findViewById(me.cool.music.R.id.iv_header_bg);
        final ImageView ivCover = (ImageView) vHeader.findViewById(me.cool.music.R.id.iv_cover);
        TextView tvTitle = (TextView) vHeader.findViewById(me.cool.music.R.id.tv_title);
        TextView tvUpdateDate = (TextView) vHeader.findViewById(me.cool.music.R.id.tv_update_date);
        TextView tvComment = (TextView) vHeader.findViewById(me.cool.music.R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(me.cool.music.R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        ImageSize imageSize = new ImageSize(200, 200);
        ImageLoader.getInstance().loadImage(mOnlineMusicList.getBillboard().getPic_s640(), imageSize,
                ImageUtils.getCoverDisplayOptions(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ivCover.setImageBitmap(loadedImage);
                        ivHeaderBg.setImageBitmap(ImageUtils.blur(loadedImage));
                    }
                });
    }

    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                mProgressDialog.cancel();
                getPlayService().play(music);
                ToastUtils.show(getString(me.cool.music.R.string.now_play, music.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(me.cool.music.R.string.unable_to_play);
            }
        }.execute();
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
            }
        }.execute();
    }

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
                ToastUtils.show(getString(me.cool.music.R.string.now_download, onlineMusic.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(me.cool.music.R.string.unable_to_download);
            }
        }.execute();
    }
}
