package it.skarafaz.mercury.main;

/**
 * Created by letroll on 10/11/16.
 */

public class MainPresenter {

    private MainView mainView;

    public MainPresenter(final MainView mainView) {
        this.mainView = mainView;
    }

    public void onCreate() {
        mainView.initUi();
        mainView.showServerList();
        mainView.loadConfigFiles();
    }

    public void onStart() {

    }

    public void onStop() {

    }

    public boolean isConnectionStringValid(String input) {
        return input.matches("^.+@.+$");
    }
}
