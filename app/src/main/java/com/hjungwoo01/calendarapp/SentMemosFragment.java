package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjungwoo01.calendarapp.model.Memo;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.MemoApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SentMemosFragment extends Fragment {
    private RecyclerView sentMemosRecyclerView;
    private List<Memo> sentMemoList;
    private MemoRecyclerAdapter sentMemoAdapter;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sent_memos, container, false);
        initWidgets();
        fetchMemos();
        sentMemoAdapter.setOnItemClickListener(new MemoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Memo memo) {
                showMemoDetails(memo);
            }
        });
        return view;
    }

    private void initWidgets() {
        sentMemosRecyclerView = view.findViewById(R.id.sentMemosRecyclerView);
        sentMemosRecyclerView.setHasFixedSize(true);
        sentMemosRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        sentMemoList = new ArrayList<>();
        sentMemoAdapter = new MemoRecyclerAdapter(sentMemoList);
        sentMemosRecyclerView.setAdapter(sentMemoAdapter);
    }


    private void fetchMemos() {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        Call<List<Memo>> call = memoApi.getMemosByOwner(OwnerSelectionActivity.getSelectedOwner());

        call.enqueue(new Callback<List<Memo>>() {
            @Override
            public void onResponse(@NonNull Call<List<Memo>> call, @NonNull Response<List<Memo>> response) {
                if (response.isSuccessful()) {
                    List<Memo> newMemoList = response.body();
                    if (newMemoList != null) {
                        sentMemoList.clear();
                        sentMemoList.addAll(sortMemos(newMemoList));
                        sentMemoAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "No memos.", Toast.LENGTH_SHORT).show();
                    sentMemoList.clear();
                    sentMemoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Memo>> call, Throwable t) {

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
        intent.putExtra("memoId", memo.getId());
        startActivity(intent);
    }
}
