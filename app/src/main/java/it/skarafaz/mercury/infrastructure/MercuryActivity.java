package it.skarafaz.mercury.infrastructure;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import it.skarafaz.mercury.infrastructure.fragment.ProgressDialogFragment;

public abstract class MercuryActivity extends AppCompatActivity {
    private static final int ACTION_BAR_ELEVATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarElevation();
    }

    private void setActionBarElevation() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(ACTION_BAR_ELEVATION);
        }
    }

    protected void showProgressDialog(String content) {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.add(ProgressDialogFragment.newInstance(content), ProgressDialogFragment.TAG);
        ft.commitAllowingStateLoss();
    }

    protected void dismissProgressDialog() {
        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment frag = fm.findFragmentByTag(ProgressDialogFragment.TAG);
        if (frag != null) {
            ft.remove(frag);
        }
        ft.commitAllowingStateLoss();
    }

    protected void shortToast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void shortToast(final int message) {
        Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show();
    }

    protected void longToast(final String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void longToast(final int message) {
        Toast.makeText(this, getString(message), Toast.LENGTH_LONG).show();
    }
}
