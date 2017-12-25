package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Administrator on 12/24/2017.
 */

public class FragFriendsInfo extends Fragment {

    String username, email, firstName, lastName;
    TextView userNameDisplay, emailDisplay, firstNameDisplay, lastNameDisplay;

    public void setFriendInfo(FriendObject thisFriend) {
        this.username = thisFriend.getFriendName();
        this.email = thisFriend.getFriendEMail();
        this.firstName = thisFriend.getFirstName();
        this.lastName = thisFriend.getLastName();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragfriendsinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userNameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoUsernameDisplay);
        emailDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoEMailDisplay);
        firstNameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoFirstNameDisplay);
        lastNameDisplay = (TextView) getActivity().findViewById(R.id.friendsInfoLastNameDisplay);

        setFriendDisplayTexts();
    }

    private void setFriendDisplayTexts() {
        userNameDisplay.setText(username);
        emailDisplay.setText(email);
        firstNameDisplay.setText(firstName);
        lastNameDisplay.setText(lastName);
    }
}
