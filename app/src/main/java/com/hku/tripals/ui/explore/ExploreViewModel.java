package com.hku.tripals.ui.explore;

import com.hku.tripals.R;
import com.hku.tripals.model.Destination;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExploreViewModel extends ViewModel {

    private MutableLiveData<List<Destination>> destinations;

    public ExploreViewModel() {
        destinations = new MutableLiveData<>();
        List<Destination> destinationsList = new ArrayList<>();
        destinationsList.add(new Destination(1, R.drawable.hong_kong, R.string.hong_kong, "22.396427", "114.109497"));
        destinationsList.add(new Destination(2, R.drawable.macau, R.string.macau, "22.198746", "113.543877"));
        destinationsList.add(new Destination(3, R.drawable.tai_pei, R.string.tai_pei, "25.032969", "121.565414"));
        destinationsList.add(new Destination(4, R.drawable.tokyo, R.string.tokyo, "35.680923", "139.760562"));
        destinationsList.add(new Destination(5, R.drawable.singapore, R.string.singapore, "1.352083", "103.819839"));
        destinations.setValue(destinationsList);
    }

    public LiveData<List<Destination>> getDestinations() {
        return destinations;
    }
}