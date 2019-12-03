package com.example.jose.mychatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import com.example.jose.mychatapp.Model.ChatMessage;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
//import java.text.DateFormat;


public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;

    //add emojicon
    EmojiconEditText emojiconEditText;
    ImageView emojibutton, submitButton;
    EmojIconActions emojiconActions;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST) {
            Snackbar.make(activity_main, "Successfully sign in, Welcome:  " , Snackbar.LENGTH_SHORT).show();
            displayChatMessage();
        } else {
            Snackbar.make(activity_main, "We can't sign in" , Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, "SignOut ", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }


        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout) findViewById(R.id.activity_main);

        //add Emoji
        emojibutton = (ImageView) findViewById(R.id.emoji_button);
        submitButton = (ImageView) findViewById(R.id.submit_button);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojiconActions = new EmojIconActions(getApplicationContext(), activity_main, emojibutton, emojiconEditText);
        emojiconActions.ShowEmojicon();


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("OldChat").push().setValue(new ChatMessage(
                        emojiconEditText.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                //Clear message when send
                emojiconEditText.setText("");
                emojiconEditText.requestFocus();
            }
        });

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);
        } else {
            Snackbar.make(activity_main, "Welcome:  " + FirebaseAuth.getInstance()
                    .getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            //load content
            displayChatMessage();
        }

    }

    private void displayChatMessage() {
        ListView listOfMessage = (ListView) findViewById(R.id.list_of_message);

        Query query = FirebaseDatabase.getInstance().getReference().child("OldChat");

        FirebaseListOptions<ChatMessage> options =
                new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).setLayout(R.layout.list_item)
                .build();

        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageUser, messageTime, messageText;
                messageText = (BubbleTextView) v.findViewById(R.id.message_text);
                messageUser = (TextView) v.findViewById(R.id.message_user);
                messageTime = (TextView) v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }
        };
        adapter.startListening();
        listOfMessage.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        if(adapter != null)
           adapter.stopListening();
        super.onStop();
    }
}
