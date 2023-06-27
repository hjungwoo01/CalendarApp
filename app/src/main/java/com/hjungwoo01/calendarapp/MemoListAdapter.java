package com.hjungwoo01.calendarapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjungwoo01.calendarapp.model.Memo;

import java.util.List;
import java.util.Locale;

public class MemoListAdapter extends ArrayAdapter<Memo> {
    public MemoListAdapter(@NonNull Context context, List<Memo> memos) {
        super(context, 0, memos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Memo memo = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_memo, parent, false);

        TextView memoName = convertView.findViewById(R.id.memoName);
        TextView memoCreated = convertView.findViewById(R.id.memoCreated);

        String memoCreatedString = "Last Updated On: " + memo.getCreatedYear() + "/" + makeTwoDigit(memo.getCreatedMonth()) + "/" +
                makeTwoDigit(memo.getCreatedDay()) + "  " + makeTwoDigit(memo.getCreatedHour()) + ":" + makeTwoDigit(memo.getCreatedMinute());

        memoName.setText(memo.getMemoName());
        memoCreated.setText(memoCreatedString);

        return convertView;
    }

    private String makeTwoDigit(int number) {
        return String.format(Locale.KOREA,"%02d", number);
    }
}