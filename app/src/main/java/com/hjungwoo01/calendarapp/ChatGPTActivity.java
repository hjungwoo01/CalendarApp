package com.hjungwoo01.calendarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hjungwoo01.calendarapp.model.Event;
import com.hjungwoo01.calendarapp.open_ai.OpenAiAPI;
import com.hjungwoo01.calendarapp.open_ai.RetrofitCallback;
import com.hjungwoo01.calendarapp.open_ai.data.Message;
import com.hjungwoo01.calendarapp.open_ai.data.OpenAiResponse;
import com.hjungwoo01.calendarapp.open_ai.data.Request;
import com.hjungwoo01.calendarapp.retrofit.EventApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGPTActivity extends AppCompatActivity {
    private static final String TAG = ChatGPTActivity.class.getSimpleName();
    private TextView txStatus;
    private EditText etText;
    private RecyclerView recyclerView;
    private ConversationAdapter recyclerAdapter;
    private List<Message> messages;
    private String sch_default;
    private String sch_create;
    private String sch_read;
    private String new_sch_read;
//    private String sch_update;
//    private String sch_delete;
    private String prev_Intent = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);

        Context context = getApplicationContext();
        String[] fileNames = {"sch_default.txt", "sch_create.txt", "sch_read.txt"/*, "sch_update.txt", "sch_delete.txt"*/};
        List<String> fileContents = new ArrayList<>();
        try {
            for (String fileName : fileNames) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
                StringBuilder contentBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }

                reader.close();
                fileContents.add(contentBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sch_default = fileContents.get(0);
        sch_create = fileContents.get(1);
        sch_read = fileContents.get(2);
//        sch_update = fileContents.get(3);
//        sch_delete = fileContents.get(4);
        loadDB();

        messages = new ArrayList<>();
        Message sysMsg = new Message("system", sch_default);
        messages.add(sysMsg);

        txStatus = findViewById(R.id.tx_status);
        etText = findViewById(R.id.tx_ai_text);
        Button send = findViewById(R.id.bt_ai_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etText.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    OpenAI(text);
                } else {
                    Toast.makeText(getApplicationContext(), "입력해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        recyclerView = findViewById(R.id.list_ai_history);

        recyclerAdapter = new ConversationAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        recyclerAdapter.setConversationList((ArrayList<Message>) messages);
    }


    private void OpenAI(String query) {
        Message userMsg = new Message("user", query);
        messages.add(userMsg);
        userMsg.setContent(query);

        Request request = new Request();
        request.setModel("gpt-3.5-turbo-0613");
        request.setMessages(messages);
        request.setTemperature(0.5f);
        request.setMax_tokens(500);
        request.setTop_p(1);
        request.setFrequency_penalty(0.0f);
        request.setPresence_penalty(0.0f);

        OpenAiAPI openAiAPI = new OpenAiAPI();
        openAiAPI.chat_gpt(request, new RetrofitCallback() {
            @Override
            public void onSuccess(int code, Object receivedData) throws JSONException {
                OpenAiResponse response = (OpenAiResponse) receivedData;
                if (response != null) {
                    Message aiMsg = response.getChoices().get(0).getMessage();
                    String[] inputs = aiMsg.getContent().split("//");
                    messages.add(aiMsg);
                    if (aiMsg.getRole().equals("assistant")) {
                        if (inputs.length == 2) {
                            RetrofitService retrofitService = new RetrofitService();
                            EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
                            JSONArray jsonArray = new JSONArray(inputs[1]);
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String completion = jsonObject.getString("completion");
                                String intent = jsonObject.getString("intent");
                                switch (intent) {
                                    case "create": {
                                        if(!prev_Intent.equals("create")) {
                                            messages.clear();
                                            Message sysMsg = new Message("system", sch_create);
                                            messages.add(sysMsg);
                                            messages.add(userMsg);
                                            messages.add(aiMsg);
                                            prev_Intent = "create";
                                        }
                                        if (completion.equals("end")) {
                                            JSONObject schedulerObj = jsonObject.optJSONObject("scheduler");
                                            if (schedulerObj != null) {
                                                String eventName = schedulerObj.getString("eventName");
                                                String eventMemo = schedulerObj.optString("eventMemo");
                                                String eventStart = schedulerObj.getString("eventStart");
                                                String eventEnd = schedulerObj.getString("eventEnd");
                                                String eventRepeat = schedulerObj.optString("eventRepeat");
                                                String eventEndRepeat = schedulerObj.optString("eventEndRepeat");
                                                Event event = new Event();
                                                event.setOwner("Person1");
                                                event.setEventName(eventName);
                                                event.setEventMemo(eventMemo);
                                                event.setEventStart(eventStart);
                                                event.setEventEnd(eventEnd);
                                                event.setEventRepeat(eventRepeat);
                                                event.setEventEndRepeat(eventEndRepeat);
                                                eventApi.save(event).enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                                        if (response.isSuccessful()) {
                                                            Toast.makeText(ChatGPTActivity.this, "Save successful.", Toast.LENGTH_SHORT).show();
                                                            loadDB();
                                                        } else {
                                                            Toast.makeText(ChatGPTActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                                        Toast.makeText(ChatGPTActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                                                        Log.e("ChatGPTActivity", "Error occurred: " + t.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                        break;
                                    }
                                    case "read": {
                                        if(!prev_Intent.equals("read")) {
                                            messages.clear();
                                            Message sysMsg = new Message("system", new_sch_read);
                                            messages.add(sysMsg);
                                            messages.add(userMsg);
                                            messages.add(aiMsg);
                                            prev_Intent = "read";
                                        }
                                        break;
                                    }
//                                    case "update": {
//                                        JSONObject schedulerObj = jsonObject.optJSONObject("scheduler");
//                                        if (schedulerObj != null) {
//                                            long id = Long.parseLong(schedulerObj.getString("id"));
//                                            String eventName = schedulerObj.getString("eventName");
//                                            String eventMemo = schedulerObj.optString("eventMemo");
//                                            String eventStart = schedulerObj.getString("eventStart");
//                                            String eventEnd = schedulerObj.getString("eventEnd");
//                                            String eventRepeat = schedulerObj.optString("eventRepeat");
//                                            String eventEndRepeat = schedulerObj.optString("eventEndRepeat");
//
//                                            Event updatedEvent = new Event();
//                                            updatedEvent.setOwner("Person1");
//                                            updatedEvent.setEventName(eventName);
//                                            updatedEvent.setEventMemo(eventMemo);
//                                            updatedEvent.setEventStart(eventStart);
//                                            updatedEvent.setEventEnd(eventEnd);
//                                            updatedEvent.setEventRepeat(eventRepeat);
//                                            updatedEvent.setEventEndRepeat(eventEndRepeat);
//
//                                            eventApi.updateEvent(id, updatedEvent).enqueue(new Callback<Void>() {
//                                                @Override
//                                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                                                    if (response.isSuccessful()) {
//                                                        Toast.makeText(ChatGPTActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();
//                                                    } else {
//                                                        Toast.makeText(ChatGPTActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
//                                                    Toast.makeText(ChatGPTActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
//                                                    Log.e("ChatGPTActivity", "Error occurred: " + t.getMessage());
//                                                }
//                                            });
//                                        }
//                                        break;
//                                    }
//                                    case "delete": {
//                                        JSONObject schedulerObj = jsonObject.optJSONObject("scheduler");
//                                        if (schedulerObj != null) {
//                                            long id = Long.parseLong(schedulerObj.getString("id"));
//                                            eventApi.deleteEvent(id).enqueue(new Callback<Void>() {
//                                                @Override
//                                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                                                    if (response.isSuccessful()) {
//                                                        Toast.makeText(ChatGPTActivity.this, "Event deleted.", Toast.LENGTH_SHORT).show();
//                                                    } else {
//                                                        Toast.makeText(ChatGPTActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
//                                                    Toast.makeText(ChatGPTActivity.this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
//                                                    Log.e("ChatGPTActivity", "Error occurred: " + t.getMessage());
//                                                }
//                                            });
//                                        }
//                                        break;
//                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    recyclerAdapter.setConversationList(messages);
                    etText.setText("");
                }
            }

            @Override
            public void onFailure(int code) {

            }

            @Override
            public void onError(Throwable t) {

            }
        });
    }

    private void loadDB() {
        RetrofitService retrofitService = new RetrofitService();
        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
        eventApi.getEventsByOwner("Person1").enqueue(new Callback<List<Event>>() {
                    @Override
                    public void onResponse
                            (@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                        if (response.isSuccessful()) {
                            Event.eventsList = response.body();
                            new_sch_read = sch_read.replace("{{events}}", Event.eventsListToString());
                        } else {
                            Toast.makeText(ChatGPTActivity.this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                        Toast.makeText(ChatGPTActivity.this, "Failed to fetch events.", Toast.LENGTH_SHORT).show();
                        Log.e("ChatGPTActivity", "Error occurred: " + t.getMessage());
                    }
                });
    }
}