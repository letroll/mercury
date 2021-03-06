package it.skarafaz.mercury.log;

import android.os.Bundle;

import it.skarafaz.mercury.R;
import it.skarafaz.mercury.infrastructure.MercuryActivity;

public class LogActivity extends MercuryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log);
        if (getIntent().getExtras() != null) {
            LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.list);
            logFragment.write(getIntent().getExtras().getString("result"));
        }
    }
}
