package com.hjungwoo01.calendarapp.memo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.R;
import com.hjungwoo01.calendarapp.model.Memo;

import java.util.List;
import java.util.Locale;

public class MemoRecyclerAdapter extends RecyclerView.Adapter<MemoRecyclerAdapter.ViewHolder> {
    private final List<Memo> memoList;

    public MemoRecyclerAdapter(List<Memo> memos) {
        this.memoList = memos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Memo memo = memoList.get(position);
        holder.bind(memo);
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Memo memo);
    }
    private OnItemClickListener itemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView memoName;
        private final TextView memoCreated;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memoName = itemView.findViewById(R.id.memoName);
            memoCreated = itemView.findViewById(R.id.memoCreated);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Memo memo = memoList.get(position);
                        itemClickListener.onItemClick(memo);
                    }
                }
            });
        }

        public void bind(Memo memo) {
            String memoCreatedString = "Last Updated On: " + memo.getCreatedYear() + "/" + makeTwoDigit(memo.getCreatedMonth()) + "/" +
                    makeTwoDigit(memo.getCreatedDay()) + "  " + makeTwoDigit(memo.getCreatedHour()) + ":" + makeTwoDigit(memo.getCreatedMinute());

            memoName.setText(memo.getMemoName());
            memoCreated.setText(memoCreatedString);
        }

        private String makeTwoDigit(int number) {
            return String.format(Locale.KOREA,"%02d", number);
        }
    }
}
