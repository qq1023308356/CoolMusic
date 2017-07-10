package me.cool.music.adapter;

import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.cool.music.R;
import me.cool.music.model.Music;

/**
 * Created by xiong on 17-7-10.
 */

public class HistoryAdapter extends BaseQuickAdapter<Music,BaseViewHolder> {
    private Music music;
    private BaseViewHolder pos;
    public HistoryAdapter(@LayoutRes int layoutResId, @Nullable List<Music> data,Music music) {
        super(layoutResId, data);
        this.music=music;
    }

    @Override
    protected void convert(BaseViewHolder helper, Music item) {
        helper.setText(R.id.title,item.getTitle()).setText(R.id.geshou,item.getArtist());
        if (music.getId()==item.getId()){
            pos=helper;
            helper.getView(R.id.play).setVisibility(View.VISIBLE);
            helper.setTextColor(R.id.geshou,Color.parseColor("#F44336")).setTextColor(R.id.title,Color.parseColor("#F44336"));
        }
    }
    public void setMusic(Music music1){
        music=music1;
    }
    public void setpos(){
        pos.getView(R.id.play).setVisibility(View.GONE);
        pos.setTextColor(R.id.geshou,Color.parseColor("#8b8a8a")).setTextColor(R.id.title,Color.parseColor("#000000"));
    }
}
