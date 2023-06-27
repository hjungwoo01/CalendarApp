package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hjungwoo01.calendarapp.model.Memo;
import com.hjungwoo01.calendarapp.retrofit.MemoApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ReceivedMemosFragment extends Fragment {

    private ListView receivedMemosListView;
    private List<Memo> memoList;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_received_memos, container, false);
        initWidgets();
        memoList = new ArrayList<>();
        fetchMemos();
        return view;
    }

    private void fetchMemos() {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        Call<List<Memo>> call = memoApi.getMemosByReceiver(OwnerSelectionActivity.getSelectedOwner());

        call.enqueue(new Callback<List<Memo>>() {
            @Override
            public void onResponse(@NonNull Call<List<Memo>> call, @NonNull Response<List<Memo>> response) {
                if (response.isSuccessful()) {
                    memoList = response.body();
                    fetchMemoList();
                } else {
                    Toast.makeText(getContext(), "No memos.", Toast.LENGTH_SHORT).show();
                    memoList = new ArrayList<>();
                    fetchMemoList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Memo>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to fetch memos.", Toast.LENGTH_SHORT).show();
                Log.e("ReceivedMemosFragment", "Error occurred: " + t.getMessage());
            }
        });
    }
    private List<Memo> sortMemos(List<Memo> memos) {
        memos.sort(new Comparator<Memo>() {
            @Override
            public int compare(Memo m1, Memo m2) {
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

    private void fetchMemoList() {
        MemoListAdapter adapter = new MemoListAdapter(requireContext(), sortMemos(memoList));
        receivedMemosListView.setAdapter(adapter);
    }

    private void initWidgets() {
        receivedMemosListView = view.findViewById(R.id.receivedMemosListView);

        receivedMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Memo selectedMemo = (Memo) parent.getItemAtPosition(position);
                showMemoDetails(selectedMemo);
            }
        });
    }

    private void showMemoDetails(Memo memo) {
        Intent intent = new Intent(getContext(), MemoDetailsActivity.class);
        intent.putExtra("memoId", memo.getId());
        startActivity(intent);
    }
}
