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

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends ListActivity {

    ArrayList<ChatObject> chatList;
    ChatAdapter chatAdapter;

    FloatingActionButton sendButton;
    EditText messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String jsonString = "[{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"false\"},{\"user\":\"me\",\"message\":\"hello_world\",\"fromMe\":\"true\"}]";
        chatList = makeChatList(jsonString);

        chatAdapter = new ChatAdapter(this, chatList);
        setListAdapter(chatAdapter);

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
        messageObject.defineChatObject("me", message, "true");

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
                        jsonObject.getString("fromMe"));

                res.add(chatObject);
            }
            catch (JSONException exception){
                Log.e("Chat object missing", "Chat object data incomplete");
            }
        }

        return res;
    }
}
