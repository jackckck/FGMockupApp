package com.fewgamers.fgmockup;

import android.app.ListActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ChatActivity extends ListActivity {

    ArrayList<ChatObject> chatList;
    ChatAdapter chatAdapter;

    FloatingActionButton sendButton;
    EditText messageText;

    Calendar chatCalendar;
    TimeZone chatTimeZone;

    DateFormat chatTimeFormat, chatDateFormat, chatMonthFormat, chatDayFormat;

    Date chatDate;

    Integer previousMonth, previousDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTimeZone = TimeZone.getDefault();
        chatTimeFormat = new SimpleDateFormat("HH:mm");
        chatDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        chatMonthFormat = new SimpleDateFormat("MM");
        chatDayFormat = new SimpleDateFormat("dd");

        String jsonString = "[{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"false\",\"date\":\"01-01-1997\",\"time\":\"1:00\"},{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"true\",\"date\":\"01-01-1997\",\"time\":\"1:00\"}]";
        chatList = makeChatList(jsonString);

        chatAdapter = new ChatAdapter(this, chatList);
        setListAdapter(chatAdapter);

        if (chatList.size() > 0) {
            String previousDateString = chatList.get(chatList.size() - 1).getDate();
            previousMonth = Integer.parseInt(previousDateString.substring(0, 2));
            previousDay = Integer.parseInt(previousDateString.substring(3, 5));
        }

        sendButton = (FloatingActionButton) findViewById(R.id.sendFloatingActionButton);
        messageText = (EditText) findViewById(R.id.messageEditText);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(messageText.getText().toString());
            }
        });
    }

    private void sendMessage(String message) {
        if (message.equals("") || message == null) {
            return;
        }

        ChatObject messageObject = new ChatObject();

        chatCalendar = Calendar.getInstance(chatTimeZone);
        chatDate = chatCalendar.getTime();

        Integer currentMonth, currentDay;
        currentMonth = Integer.parseInt(chatMonthFormat.format(chatDate));
        currentDay = Integer.parseInt(chatDayFormat.format(chatDate));

        if (currentMonth >= previousMonth && currentDay > previousDay) {
            ChatObject dateNotifier = new ChatObject();
            dateNotifier.setDate(chatDateFormat.format(chatDate));
            dateNotifier.setAsDateNotifer();
            chatList.add(dateNotifier);

            previousMonth = currentMonth;
            previousDay = currentDay;
        }

        messageObject.defineChatObject("me", message, "true",
                chatDateFormat.format(chatDate), chatTimeFormat.format(chatDate));

        chatAdapter.add(messageObject);

        messageText.setText("");
    }

    private ArrayList<ChatObject> makeChatList(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException exception) {
            Log.e("Chat list not found", "Something went wrong when loading chat data");
        }

        ArrayList<ChatObject> res = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ChatObject chatObject = new ChatObject();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                chatObject.defineChatObject(jsonObject.getString("user"), jsonObject.getString("message"),
                        jsonObject.getString("fromMe"), jsonObject.getString("date"), jsonObject.getString("time"));

                res.add(chatObject);
            } catch (JSONException exception) {
                Log.e("Chat object missing", "Chat object data incomplete");
            }
        }

        return res;
    }
}
