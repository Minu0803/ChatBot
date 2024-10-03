package com.example.chatbot;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;  // 로그를 사용하기 위해 import 추가
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText editTextQuestion;
    private Button buttonSend;
    private TextView textViewResponse;

    // OpenAI API 키와 URL
    private static final String OPENAI_API_KEY = "api 키 입력";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String TAG = "ChatBot";  // 로그에 사용할 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextQuestion = findViewById(R.id.editTextQuestion);
        buttonSend = findViewById(R.id.buttonSend);
        textViewResponse = findViewById(R.id.textViewResponse);

        // 버튼 클릭 이벤트 처리
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button clicked");  // 버튼이 클릭되었는지 확인
                String question = editTextQuestion.getText().toString().trim();
                if (!question.isEmpty()) {
                    sendMessageToChatBot(question);
                } else {
                    textViewResponse.setText("질문을 입력하세요.");
                }
            }
        });
    }

    // ChatGPT에 질문을 보내는 메소드
    private void sendMessageToChatBot(String question) {
        try {
            Log.d(TAG, "Sending message: " + question);  // 메시지 전송 로그

            // JSON 본문 생성 (messages는 JSONArray로 구성되어야 합니다)
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-3.5-turbo");

            // messages는 JSONArray로 만들어야 함
            JSONArray messagesArray = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messagesArray.put(userMessage);

            jsonBody.put("messages", messagesArray);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

            // 요청 생성
            Request request = new Request.Builder()
                    .url(OPENAI_API_URL)
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .post(body)
                    .build();

            // 비동기 네트워크 요청
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request failed: " + e.getMessage());  // 실패 로그
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewResponse.setText("오류 발생: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Response received: " + responseBody);  // 응답 로그
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String botResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewResponse.setText("챗봇: " + botResponse);
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());  // JSON 파싱 오류 로그
                        }
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code());  // 비정상 응답 로그
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());  // JSON 생성 오류 로그
        }
    }
}