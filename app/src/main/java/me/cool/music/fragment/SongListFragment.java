package me.cool.music.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import me.cool.music.adapter.SongListAdapter;
import me.cool.music.application.AppCache;
import me.cool.music.constants.Extras;
import me.cool.music.enums.LoadStateEnum;
import me.cool.music.utils.NetworkUtils;
import me.cool.music.utils.ViewUtils;
import me.cool.music.utils.binding.Bind;
import me.cool.music.activity.OnlineMusicActivity;
import me.cool.music.model.SongListInfo;

/**
 * 在线音乐
 * Created by wcy on 2015/11/26.
 */
public class SongListFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @Bind(me.cool.music.R.id.lv_song_list)
    private ListView lvSongList;
    @Bind(me.cool.music.R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(me.cool.music.R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private List<SongListInfo> mSongLists;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(me.cool.music.R.layout.fragment_song_list, container, false);
    }

    @Override
    protected void init() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ViewUtils.changeViewState(lvSongList, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        mSongLists = AppCache.getSongListInfos();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(me.cool.music.R.array.online_music_list_title);
            String[] types = getResources().getStringArray(me.cool.music.R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }
        SongListAdapter adapter = new SongListAdapter(mSongLists);
        lvSongList.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        lvSongList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongListInfo songListInfo = mSongLists.get(position);
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
        startActivity(intent);
    }
}
