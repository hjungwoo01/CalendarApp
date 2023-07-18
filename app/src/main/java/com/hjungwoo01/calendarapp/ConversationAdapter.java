package com.hjungwoo01.calendarapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.open_ai.data.Message;

import java.util.ArrayList;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private ArrayList<Message> list;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position != 0)
            holder.onBind(list.get(position));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setConversationList(ArrayList<Message> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.item_string_text);
        }

        void onBind(Message item){
            String messageTemplate = itemView.getContext().getString(R.string.message_template);
            String formattedMessage = String.format(messageTemplate, item.getRole(), item.getContent());
            message.setText(formattedMessage);
        }
    }
}
