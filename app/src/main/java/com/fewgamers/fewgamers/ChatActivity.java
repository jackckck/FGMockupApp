package com.fewgamers.fewgamers;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// this class is currently not in use, due to the absence of a chat feature in our service
public class ChatActivity extends ListActivity {

    ArrayList<ChatObject> chatList;
    ChatAdapter chatAdapter;

    FloatingActionButton sendButton;
    EditText messageText;

    Calendar chatCalendar;
    TimeZone chatTimeZone;

    DateFormat chatTimeFormat, chatDateFormat;

    Date chatDate;

    Integer previousMonth, previousDay, previousYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTimeZone = TimeZone.getDefault();
        chatTimeFormat = new SimpleDateFormat("HH:mm");
        chatDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        String jsonString = "[{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"false\",\"date\":\"01-01-1997\",\"time\":\"1:00\"},{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"true\",\"date\":\"01-01-1997\",\"time\":\"1:00\"}]";
        chatList = makeChatList(jsonString);

        chatAdapter = new ChatAdapter(this, chatList);
        setListAdapter(chatAdapter);

        if (chatList.size() > 0) {
            String previousDateString = chatList.get(chatList.size() - 1).getDate();
            previousMonth = Integer.parseInt(previousDateString.substring(0, 2));
            previousDay = Integer.parseInt(previousDateString.substring(3, 5));
            previousYear = Integer.parseInt(previousDateString.substring(6, 10));
        }

        sendButton = (FloatingActionButton) findViewById(R.id.sendFloatingActionButton);
        messageText = (EditText) findViewById(R.id.messageEditText);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(messageText.getText().toString());
                notifyOfMessage("sender", messageText.getText().toString());
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

        Integer currentMonth, currentDay, currentYear;
        String chatDateString = chatDateFormat.format(chatDate);
        currentMonth = Integer.parseInt(chatDateString.substring(0, 2));
        currentDay = Integer.parseInt(chatDateString.substring(3, 5));
        currentYear = Integer.parseInt(chatDateString.substring(6, 10));

        if ((currentMonth >= previousMonth && currentDay > previousDay) || currentYear > previousYear) {
            ChatObject dateNotifier = new ChatObject();
            dateNotifier.setDate(chatDateFormat.format(chatDate));
            dateNotifier.setAsDateNotifer();
            chatList.add(dateNotifier);

            previousMonth = currentMonth;
            previousDay = currentDay;
            previousYear = currentYear;
        }

        messageObject.defineChatObject("me", message, "true",
                chatDateString, chatTimeFormat.format(chatDate));

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

    private void notifyOfMessage(String sender, String message) {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.placeholder_notification_icon, "Test notification", pendingIntent);
        builder.setSmallIcon(R.drawable.placeholder_notification_icon);
        builder.setContentTitle("Test notification");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
