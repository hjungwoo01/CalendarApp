package com.hjungwoo01.calendarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.hjungwoo01.calendarapp.scheduler.EventActivity;

import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatgpt);


        //open ai set
        messages = new ArrayList<>();
        String contentSys = """
                        당신은 일정을 관리하는 챗봇입니다. 대화 간 user는 아래의 triple backticks에 있는 JSON포맷의 key-value에 맞게 당신에게 요청할 것입니다.
                        
                        user : 안녕
                        assistant : 안녕하세요! 일정을 관리하는 챗봇입니다. 무엇을 도와드릴까요?
                        
                        
                        아래의 triple backticks에는 당신이 user의 대답을 분석하여 작성해야하는 JSON포맷입니다. user한테서 JSON 포맷을 요구하지 않습니다. 당신은 user의 대답을 분석하여 JSON포맷으로 작성합니다.
                        ```
                        {'intent': 'create', 'scheduler': {'eventName': 'Event Name', 'eventMemo': 'Event Memo', 'eventStart': '202307130000', 'eventEnd': '202307132359', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}
                        ```
                        
                        위의 triplet backticks에 있는 JSON의 설명은 다음과 같습니다.
                        - 해당 JSON은 'intent'와 'scheduler'의 두가지 key값을 갖습니다.
                        - 'intent' key는 'create', 'read', 'update', 'delete'의 value들을 가질 수 있으며, 당신이 데이터베이스를 처리하기 위해 사용하는 쿼리 형식의 의미를 가집니다.
                        - 'intent' 파악이 최우선입니다. 파악할때까지 의문형으로 물어보세요.
                        
                        
                        답변하는 과정은 다음 아래의 angle brackets에 있는 4가지 절차를 따라주세요.
                        <
                        1. 사용자의 대답을 듣고 'intent'를 파악하여 어떤 수행을 해야 하는지 예측합니다.
                        2. 'scheduler'의 모든 key값들에 대한 value에 대하여 확보가 되었는지 파악해야 합니다.
                        3. 'scheduler'의 모든 key값들에 대한 value에 대하여 확보가 안되었다면, key값들에 대한 value에 대하여 하나씩 물어봅니다.
                        4. 'scheduler'의 모든 key값들에 대하여 value가 확보가 되었다면, 수정사항은 없는지, 다른 요청사항은 없는지 의문형으로 물어봐야 합니다.
                        >
                        
                        user가 일정을 추가하고 싶다는 것을 파악했을때만 다음 아래의 triple dashes에 있는 6가지 절차를 따라주세요. 모든지 물어볼때 하나씩 물어봅니다. user 대답을 분석하여 없는것만 물어봅니다.\s
                        ---
                        1. user가 일정 제목을 말하지 않았다면 일정 제목을 묻습니다.
                        2. 일정에 대한 메모를 추가하고싶은지 묻습니다.
                        3. user가 일정 시작과 종료 시간을 말하지 않았다면 묻습니다. user 한테서 yyyyMMddHHmm 형태를 요구하지 않습니다. 날짜를 받은 다음, 당신이 대답을 분석하여 yyyyMMddHHmm 형태로 작성합니다. 시작과 종료 시간, 두가지를 받고, user가 말하지 않았다면 없는걸 물어봅니다. json형태로 저장할때 시작과 종료 날짜는 yyyyMMddHHmm 형태여야합니다.
                        4. 반복 여부를 물어봅니다. 반복 주기는 반복 안함, 매일, 매주, 매월, 매년 중 선택할수 있습니다. 한국어로 user의 대답을 받은 다음 Never, Every Day, Every Week, Every Month, Every Year 중에 user가 선택한걸로 작성합니다.
                        5. user가 반복을 안한다고 하면, 반복 종료날짜는 일정 시작 날짜로 설정하고, 반복 종료날짜를 물어보지 않습니다. 반복한다고 하면 반복이 종료되는 날짜를 묻습니다.
                        6. 다른 요청사항이 있는지 물어보고, 없으면 일정 제목, 메모, 시작과 종료일시, 반복 여부, 반복 주기를 보여주며 이렇게 추가하시겠습니까? 라고 묻습니다.
                        ---
                        
                        user가 JSON포맷을 요청할 경우 아래의 Angle brackets를 참고합니다.
                        <
                        user: 'JSON포맷 보여줘'
                        assistent: '요청하신 JSON 포맷입니다. {'intent': 'create', 'scheduler': {'eventName': 'Event Name', 'eventMemo': 'Event Memo', 'eventStart': '202307130000', 'eventEnd': '202307132359', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}'
                        >
                        
                        당신은 답변할 때 반드시 문장 끝에 ~~처럼 대화 포맷을 유지해야 합니다. 이 규칙을 무조건 지켜주세요.
                        만약 사용자가 관련 없는 내용을 물어본다면 정중히 거절하세요.
                        지금부터 새로운 대화가 시작됩니다.
                """;

        Message sysMsg = new Message();
        sysMsg.setRole("system");
        sysMsg.setContent(contentSys);
        messages.add(sysMsg);

        txStatus = findViewById(R.id.tx_status);
        etText = findViewById(R.id.tx_ai_text);

        Button send = findViewById(R.id.bt_ai_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etText.getText().toString();
                if(!TextUtils.isEmpty(text))
                    OpenAI(text);
                else
                    Toast.makeText(getApplicationContext(), "입력해주세요.", Toast.LENGTH_LONG).show();
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
        Message userMsg = new Message();
        userMsg.setRole("user");
        userMsg.setContent(query);
        messages.add(userMsg);

        Request request = new Request();
        request.setModel("gpt-3.5-turbo");
        request.setMessages(messages);
        request.setTemperature(0.3f);
        request.setMax_tokens(100);
        request.setTop_p(1);
        request.setFrequency_penalty(0.0f);
        request.setPresence_penalty(0.0f);

        OpenAiAPI openAiAPI = new OpenAiAPI();
        openAiAPI.chat_gpt(request, new RetrofitCallback() {
            @Override
            public void onSuccess(int code, Object receivedData) {
                OpenAiResponse response = (OpenAiResponse) receivedData;
                if(response != null) {
                    Message aiMsg = response.getChoices().get(0).getMessage();
                    messages.add(aiMsg);
                    if(aiMsg.getRole().equals("user")) {
                        String userMsg = aiMsg.getContent();
                        RetrofitService retrofitService = new RetrofitService();
                        EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
                        try {
                            JSONObject jsonObject = new JSONObject(userMsg);
                            String intent = jsonObject.optString("intent");
                            if (intent.equals("create")) {
                                JSONObject schedulerObj = jsonObject.optJSONObject("scheduler");
                                if (schedulerObj != null) {
                                    Event event = new Event();
                                    String eventName = schedulerObj.getString("eventName");
                                    String eventMemo = schedulerObj.optString("eventMemo");
                                    String eventStart = schedulerObj.getString("eventStart");
                                    String eventEnd = schedulerObj.getString("eventEnd");
                                    String eventRepeat = schedulerObj.optString("eventRepeat");
                                    String eventEndRepeat = schedulerObj.optString("eventEndRepeat");

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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    recyclerAdapter.setConversationList((ArrayList<Message>) messages);
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
}