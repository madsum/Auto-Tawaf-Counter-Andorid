package nl.iprosoft.autotawafcounter.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    // Define constants for navigation state
    public static final int STATE_INITIAL = 0;
    public static final int STATE_NOT_ALIGNED = 1;
    public static final int STATE_START_TAWAF = 2;
//    private final MutableLiveData<String> mText;

    // LiveData to track the dialog state
    private final MutableLiveData<Integer> navigationState = new MutableLiveData<>();

    public HomeViewModel() {
        // Initialize with the alignment question state
        navigationState.setValue(STATE_INITIAL);
    }

/*    public LiveData<String> getText() {
        return mText;
    }*/

    // Method to get the current navigation state
    public LiveData<Integer> getNavigationState() {
        return navigationState;
    }

    // Methods to update state based on user actions
    public void onYesStartTawafClicked() {
        navigationState.setValue(STATE_START_TAWAF);
    }

    public void onNotYetClicked() {
        navigationState.setValue(STATE_NOT_ALIGNED);
    }

    public void onNotAlignedOkClicked() {
        // Go back to the initial question
        navigationState.setValue(STATE_INITIAL);
    }
}