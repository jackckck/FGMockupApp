package com.fewgamers.fgmockup;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends ListActivity {

    ArrayList<ChatObject> chatList;
    ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String jsonString = "[{\"message\":\"hello_world\",\"fromMe\":\"true\"},\n" +
                "{\"message\":\"hello_world\",\"fromMe\":\"true\"},\n" +
                "{\"message\":\"hello_world\",\"fromMe\":\"false\"},\n" +
                "{\"message\":\"hello_world\",\"fromMe\":\"true\"}]";

        chatList = makeChatList(jsonString);

        chatAdapter = new ChatAdapter(this, chatList);
        setListAdapter(chatAdapter);
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
                chatObject.defineChatObject(jsonArray.getJSONObject(i));

                res.add(chatObject);
            }
            catch (JSONException exception){
                Log.e("Chat object missing", "Chat object data incomplete");
            }
        }

        return res;
    }
}
