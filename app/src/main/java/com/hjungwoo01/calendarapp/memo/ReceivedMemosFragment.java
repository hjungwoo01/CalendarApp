package com.hjungwoo01.calendarapp.memo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.OwnerSelectionActivity;
import com.hjungwoo01.calendarapp.R;
import com.hjungwoo01.calendarapp.memo.MemoDetailsActivity;
import com.hjungwoo01.calendarapp.memo.MemoRecyclerAdapter;
import com.hjungwoo01.calendarapp.model.Memo;
import com.hjungwoo01.calendarapp.retrofit.MemoApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReceivedMemosFragment extends Fragment {
    private RecyclerView readMemosRecyclerView;
    private RecyclerView unreadMemosRecyclerView;
    private List<Memo> readMemoList;
    private List<Memo> unreadMemoList;
    private MemoRecyclerAdapter readMemoAdapter;
    private MemoRecyclerAdapter unreadMemoAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_received_memos, container, false);
        initWidgets();
        fetchMemos();
        readMemoAdapter.setOnItemClickListener(new MemoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Memo memo) {
                showMemoDetails(memo);
            }
        });
        unreadMemoAdapter.setOnItemClickListener(new MemoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Memo memo) {
                RetrofitService retrofitService = new RetrofitService();
                MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);
                if(memo.getReadReceivers() != null) {
                    memo.setReadReceivers(memo.getReadReceivers() + OwnerSelectionActivity.getSelectedOwner() + ",");
                } else {
                    memo.setReadReceivers(OwnerSelectionActivity.getSelectedOwner() + ",");
                }
                memoApi.updateMemo(memo.getId(), memo).enqueue(new Callback<Memo>() {
                    @Override
                    public void onResponse(@NonNull Call<Memo> call, @NonNull Response<Memo> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Memo read.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Memo read unsuccessful.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Memo> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Failed to update memo.", Toast.LENGTH_SHORT).show();
                    }
                });
                showMemoDetails(memo);
            }
        });
        return view;
    }
    private void initWidgets() {
        readMemosRecyclerView = view.findViewById(R.id.readMemosRecyclerView);
        unreadMemosRecyclerView = view.findViewById(R.id.unreadMemosRecyclerView);
        readMemosRecyclerView.setHasFixedSize(true);
        unreadMemosRecyclerView.setHasFixedSize(true);
        readMemosRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        unreadMemosRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        readMemoList = new ArrayList<>();
        unreadMemoList = new ArrayList<>();
        readMemoAdapter = new MemoRecyclerAdapter(readMemoList);
        unreadMemoAdapter = new MemoRecyclerAdapter(unreadMemoList);
        readMemosRecyclerView.setAdapter(readMemoAdapter);
        unreadMemosRecyclerView.setAdapter(unreadMemoAdapter);
    }

    private void fetchMemos() {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);
        String selectedOwner = OwnerSelectionActivity.getSelectedOwner();
        memoApi.getMemosByReceiver(selectedOwner)
                .enqueue(new Callback<List<Memo>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Memo>> call, @NonNull Response<List<Memo>> response) {
                if (response.isSuccessful()) {
                    List<Memo> newMemoList = response.body();
                    if (newMemoList != null) {
                        readMemoList.clear();
                        unreadMemoList.clear();
                        for(Memo memo: newMemoList) {
                            String readReceivers = memo.getReadReceivers();
                            if(readReceivers != null && readReceivers.contains(selectedOwner)) {
                                readMemoList.add(memo);
                            } else {
                                unreadMemoList.add(memo);
                            }
                        }

                        readMemoList = sortMemos(readMemoList);
                        unreadMemoList = sortMemos(unreadMemoList);
                        readMemoAdapter.notifyDataSetChanged();
                        unreadMemoAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "No memos.", Toast.LENGTH_SHORT).show();
                    readMemoList.clear();
                    unreadMemoList.clear();
                    readMemoAdapter.notifyDataSetChanged();
                    unreadMemoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Memo>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to fetch memos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Memo> sortMemos(List<Memo> memos) {
        memos.sort(new Comparator<Memo>() {
            @Override
            public int compare(Memo m2, Memo m1) {
                int yearComparison = Integer.compare(m1.getCreatedYear(), m2.getCreatedYear());
                if (yearComparison != 0) {
                    return yearComparison;
                }
                int monthComparison = Integer.compare(m1.getCreatedMonth(), m2.getCreatedMonth());
                if (monthComparison != 0) {
                    return monthComparison;
                }
                int dayComparison = Integer.compare(m1.getCreatedDay(), m2.getCreatedDay());
                if (dayComparison != 0) {
                    return dayComparison;
                }
                int hourComparison = Integer.compare(m1.getCreatedHour(), m2.getCreatedHour());
                if (hourComparison != 0) {
                    return hourComparison;
                }
                return Integer.compare(m1.getCreatedMinute(), m2.getCreatedMinute());
            }
        });
        return memos;
    }
    private void showMemoDetails(Memo memo) {
        Intent intent = new Intent(getContext(), MemoDetailsActivity.class);
        intent.putExtra("hideButtons", true);
        intent.putExtra("memoId", memo.getId());
        intent.putExtra("memoSender", memo.getOwner());
        startActivity(intent);
    }

}
