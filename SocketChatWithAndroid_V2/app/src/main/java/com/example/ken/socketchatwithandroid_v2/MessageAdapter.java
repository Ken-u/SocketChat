package com.example.ken.socketchatwithandroid_v2;

import android.content.Context;

import com.mtxc.universallistview.UniversalAdapter;
import com.mtxc.universallistview.ViewHolder;

import java.util.ArrayList;

/**
 * Created by ken on 16/6/2.
 */
public class MessageAdapter extends UniversalAdapter<MessageData> {
    public MessageAdapter(Context context, ArrayList<MessageData> datas, int itemLayoutId){
        super(context,datas,itemLayoutId);
    }

    @Override
    public void updateItem(ViewHolder holder, MessageData data){
        holder.setTextViewText(R.id.tv_name_item,data.getName());
        holder.setTextViewText(R.id.tv_content_item,data.getMsg());
        holder.setImageViewImageResource(R.id.img_head_item,data.getImgId());


    }
}
