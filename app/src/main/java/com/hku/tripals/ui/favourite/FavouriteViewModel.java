package com.hku.tripals.ui.favourite;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavouriteViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FavouriteViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Favourite fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}