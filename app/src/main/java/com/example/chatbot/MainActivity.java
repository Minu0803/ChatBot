package com.example.chatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextQuestion;
    private Button buttonSend;
    private TextView textViewResponse;

    private static final String API_KEY = "YOUR_OPENAI_API_KEY"; // OpenAI API 키 입력
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 레이아웃 요소 연결
        editTextQuestion = findViewById(R.id.editTextQuestion);
        buttonSend = findViewById(R.id.buttonSend);
        textViewResponse = findViewById(R.id.textViewResponse);

        // 버튼 클릭 이벤트 설정
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = editTextQuestion.getText().toString().trim();
                if (!question.isEmpty()) {
                    textViewResponse.setText("챗봇이 응답 중입니다...");
                    sendMessageToOpenAI(question);
                } else {
                    textViewResponse.setText("질문을 입력해주세요.");
                }
            }
        });
    }

    // OpenAI API에 메시지 전송
    private void sendMessageToOpenAI(String question) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        try {
            // JSON 요청 생성
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo"); // 사용할 모델
            requestBody.put("messages", new JSONObject[]{
                    new JSONObject().put("role", "user").put("content", question)
            });

            // 요청 생성
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(JSON, requestBody.toString()))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            // 비동기 요청
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> textViewResponse.setText("오류가 발생했습니다: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseBody = new JSONObject(response.body().string());
                            String reply = responseBody
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            runOnUiThread(() -> textViewResponse.setText(reply));
                        } catch (Exception e) {
                            runOnUiThread(() -> textViewResponse.setText("응답 처리 중 오류 발생: " + e.getMessage()));
                        }
                    } else {
                        runOnUiThread(() -> textViewResponse.setText("API 오류: " + response.message()));
                    }
                }
            });
        } catch (Exception e) {
            textViewResponse.setText("요청 생성 중 오류 발생: " + e.getMessage());
        }
    }
}