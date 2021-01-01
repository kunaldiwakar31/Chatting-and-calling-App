package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                requestFragment requestfragment=new requestFragment();
                return requestfragment;
            case 1:
                chatFragment chatfragment=new chatFragment();
                return chatfragment;
            case 2:
                FriendFragment friendfragment=new FriendFragment();
                return friendfragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
