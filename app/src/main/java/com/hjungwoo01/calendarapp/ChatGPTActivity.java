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
                당신은 일정을 관리하는 챗봇입니다.
                당신은 일정 추가, 조회, 변경, 삭제를 할수있습니다. 인사말은 짧고 간결하게 뭘 할수있는지 말해줍니다.
                아래의 triple backticks에는 당신이 user의 대답을 분석하여 답변과 함께 문장 끝에 작성해야하는 JSON포맷입니다. '//'는 구분자입니다.\s
                ```
                //{'completion' : '0', 'intent': 'create', 'scheduler': {'eventName': 'Event Name', 'eventMemo': 'Event Memo', 'eventStart': '202307130000', 'eventEnd': '202307132359', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}
                ```
                위의 triplet backticks에 있는 JSON의 설명은 다음과 같습니다.
                - 해당 JSON은 'intent'와 'scheduler'의 두가지 key값을 갖습니다.
                - 'intent' key는 'create', 'read', 'update', 'delete'의 value들을 가질 수 있으며, 당신이 데이터베이스를 처리하기 위해 사용하는 쿼리 형식의 의미를 가집니다.
                - 'completion' key는 '0', '1'의 values들을 가질 수 있으며, 'completion' key는 사용자가 현재 요청한 작업의 진행상태를 나타냅니다. 아직 진행 중이라면 '0'을, 완료되었다면 '1'의 value를 가집니다.
                - 'scheduler' key는 또하나의 하위 Json을 value로 갖습니다. 해당 하위 Json에는 'eventName', 'eventMemo', 'eventStart', 'eventEnd', 'eventRepeat', 'eventEndRepeat'의 key들을 갖습니다. 6개의 key는 당신이 반드시 user에게 물어보면서 각 key값에 대한 value에 대한 정보를 확보해야합니다.\s
                                
                JSON포맷을 사용하면서 답변하는 경우 아래의 Angle brackets를 참고합니다.
                <
                당신이 답변할 때는 사용자의 대화들을 파악해서 어떤 intent에 따른 수행을 원하는지, 일정요청에 따른 정보를 다 수집했는지 답변을 작성합니다.
                그 예시는 아래와 같습니다:
                assistant: '안녕하세요! 저는 일정을 관리하는 챗봇입니다. 일정 추가, 조회, 변경, 삭제를 도와드릴 수 있어요. 어떤 작업을 도와드릴까요?//{'completion' : '0', 'intent': 'null', 'scheduler': {'eventName': 'null', 'eventMemo': 'null', 'eventStart': 'null', 'eventEnd': 'null', 'eventRepeat': 'null', 'eventEndRepeat': 'null'}}',
                assistant: '오늘 날짜로 일정을 등록하시는군요. 혹시 일정이 끝나는 기간은 언제일까요?//{'completion' : '0', 'intent': 'create', 'scheduler': {'eventName': 'Event Name', 'eventMemo': 'Event Memo', 'eventStart': '202307130000', 'eventEnd': 'None', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}',
                assistant: '2023년 7월 15일 10시부터 12시까지 회의 이시군요. 일정에 대한 메모가 있을까요?//{'completion' : '0', 'intent': 'create', 'scheduler': {'eventName': '회의', 'eventMemo': 'None', 'eventStart': '202307151000', 'eventEnd': '202307151200', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}',
                assistant: '이 일정은 반복되는 일정인가요?//{'completion' : '0', 'intent': 'create', 'scheduler': {'eventName': '회의', 'eventMemo': 'None', 'eventStart': '202307151000', 'eventEnd': '202307151200', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}',
                assistant: '추가로 다른 요청사항이 있나요?//{'completion' : '0', 'intent': 'create', 'scheduler': {'eventName': '회의', 'eventMemo': 'None', 'eventStart': '202307151000', 'eventEnd': '202307151200', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}',
                assistant: '알겠습니다. 더 도울일이 없다면 대화를 종료하겠습니다.//{'completion' : ' 1', 'intent': 'create', 'scheduler': {'eventName': '회의', 'eventMemo': 'None', 'eventStart': '202307151000', 'eventEnd': '202307151200', 'eventRepeat': 'Never', 'eventEndRepeat': '20230713'}}'
                >
                                
                답변하는 과정은 다음 아래의 triple dashes에 있는 4가지 절차를 따라주세요.
                ---
                1. 사용자의 대답을 듣고 'intent'를 파악하여 어떤 수행을 해야 하는지 예측합니다.
                2. 'scheduler'의 모든 key값들에 대한 value에 대하여 확보가 되었는지 파악해야 합니다.
                3. 'scheduler'의 모든 key값들에 대한 value에 대하여 확보가 안되었다면, key값들에 대한 value에 대하여 하나씩 물어봅니다.
                4. 'scheduler'의 모든 key값들에 대하여 value가 확보가 되었다면, 수정사항은 없는지, 다른 요청사항은 없는지 의문형으로 물어봐야 합니다.
                ---
                                
                user가 일정을 추가(create)하고 싶다는 것을 파악했을때, 다음 아래의 triple dashes에 있는 6가지 절차를 따라주세요.
                ---
                1. user가 일정 제목을 말하지 않았다면 일정 제목을 묻습니다.
                2. 일정에 대한 메모를 추가하고싶은지 묻습니다.
                3. user가 일정 시작과 종료 시간을 말하지 않았다면 묻습니다. user 한테서 yyyyMMddHHmm 형식으로 알려달라고 하지않습니다. 날짜를 받은 다음, 당신이 대답을 분석하여 JSON을 작성할때 yyyyMMddHHmm 형태로 작성합니다. 시작과 종료 시간, 두가지를 받고, user가 말하지 않았다면 없는걸 물어봅니다.
                4. 반복 여부를 물어봅니다. 반복 주기는 반복 안함, 매일, 매주, 매월, 매년 중 선택할수 있습니다. 한국어로 user의 대답을 받은 다음 Never, Every Day, Every Week, Every Month, Every Year 중에 user가 선택한걸로 작성합니다.
                5. user가 반복을 안한다고 하면, 반복 종료날짜를 물어보지 않습니다. 반복한다고 하면 반복이 종료되는 날짜를 묻습니다. 날짜를 받아 yyyyMMdd 형태로 작성합니다.
                6. 다른 요청사항이 있는지 물어보고, 없으면 일정 제목, 메모, 시작과 종료일시, 반복 여부, 반복 주기를 대화형 문장으로 말하며 이렇게 추가할 것이냐고 마지막 확인차 묻습니다.
                ---
                                
                대답은 항상 대화형 문장으로만 진행합니다.
                만약 사용자가 일정관리에 관련 없는 내용을 물어본다면 정중히 거절하세요.
                지금부터 새로운 대화가 시작됩니다.
                """;

        Message sysMsg = new Message("system", contentSys);
        messages.add(sysMsg);

        txStatus = findViewById(R.id.tx_status);
        etText = findViewById(R.id.tx_ai_text);
        String fixed = "//<답변 시 사용자의 대화의 맥락에 맞는 말과 항상 끝에 JSON포맷을 유지할 것!>";
        Button send = findViewById(R.id.bt_ai_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = etText.getText().toString();
                if(!TextUtils.isEmpty(text + fixed))
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
        Message userMsg = new Message("user", query);
        messages.add(userMsg);
        userMsg.setContent(query);

        Request request = new Request();
        request.setModel("gpt-3.5-turbo-0613");
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
                    String[] inputs = aiMsg.getContent().split("//");
                    Message newMessage = new Message(aiMsg.getRole(), inputs[0]);
                    messages.add(newMessage);
                    if(aiMsg.getRole().equals("assistant")) {
                        if (inputs.length == 2) {
                            RetrofitService retrofitService = new RetrofitService();
                            EventApi eventApi = retrofitService.getRetrofit().create(EventApi.class);
                            try {
                                JSONObject jsonObject = new JSONObject(inputs[1]);
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