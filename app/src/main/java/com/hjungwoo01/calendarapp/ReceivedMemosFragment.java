package com.hjungwoo01.calendarapp;

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
    private RecyclerView receivedMemosRecyclerView;
    private List<Memo> receivedMemoList;
    private MemoRecyclerAdapter receivedMemoAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_received_memos, container, false);
        initWidgets();
        fetchMemos();
        receivedMemoAdapter.setOnItemClickListener(new MemoRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Memo memo) {
                showMemoDetails(memo);
            }
        });
        return view;
    }
    private void initWidgets() {
        receivedMemosRecyclerView = view.findViewById(R.id.receivedMemosRecyclerView);
        receivedMemosRecyclerView.setHasFixedSize(true);
        receivedMemosRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        receivedMemoList = new ArrayList<>();
        receivedMemoAdapter = new MemoRecyclerAdapter(receivedMemoList);
        receivedMemosRecyclerView.setAdapter(receivedMemoAdapter);
    }

    private void fetchMemos() {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        memoApi.getMemosByReceiver(OwnerSelectionActivity.getSelectedOwner())
                .enqueue(new Callback<List<Memo>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Memo>> call, @NonNull Response<List<Memo>> response) {
                if (response.isSuccessful()) {
                    List<Memo> newMemoList = response.body();
                    if (newMemoList != null) {
                        receivedMemoList.clear();
                        receivedMemoList.addAll(sortMemos(newMemoList));
                        receivedMemoAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), "No memos.", Toast.LENGTH_SHORT).show();
                    receivedMemoList.clear();
                    receivedMemoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Memo>> call, Throwable t) {
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
        intent.putExtra("memoId", memo.getId());
        startActivity(intent);
    }

}
