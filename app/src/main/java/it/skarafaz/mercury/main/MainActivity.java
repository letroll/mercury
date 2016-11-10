package it.skarafaz.mercury.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.skarafaz.mercury.R;
import it.skarafaz.mercury.help.HelpActivity;
import it.skarafaz.mercury.infrastructure.MercuryActivity;
import it.skarafaz.mercury.infrastructure.adapter.ServerPagerAdapter;
import it.skarafaz.mercury.infrastructure.event.SshCommandConfirm;
import it.skarafaz.mercury.infrastructure.event.SshCommandEnd;
import it.skarafaz.mercury.infrastructure.event.SshCommandMessage;
import it.skarafaz.mercury.infrastructure.event.SshCommandPassword;
import it.skarafaz.mercury.infrastructure.event.SshCommandPubKeyInput;
import it.skarafaz.mercury.infrastructure.event.SshCommandStart;
import it.skarafaz.mercury.infrastructure.event.SshCommandYesNo;
import it.skarafaz.mercury.infrastructure.manager.ConfigManager;
import it.skarafaz.mercury.infrastructure.manager.ExportPublicKeyStatus;
import it.skarafaz.mercury.infrastructure.manager.LoadConfigFilesStatus;
import it.skarafaz.mercury.infrastructure.manager.SshManager;
import it.skarafaz.mercury.infrastructure.ssh.SshCommandPubKey;
import it.skarafaz.mercury.log.LogActivity;

public class MainActivity extends MercuryActivity implements MainView {
    private static final int STORAGE_PERMISSION_CONFIG_REQ = 1;
    private static final int STORAGE_PERMISSION_PUB_REQ = 2;
    private static final int APP_INFO_REQ = 1;

    @Bind(R.id.progress)
    protected ProgressBar progress;
    @Bind(R.id.empty)
    protected LinearLayout empty;
    @Bind(R.id.message)
    protected TextView message;
    @Bind(R.id.settings)
    protected TextView settings;
    @Bind(R.id.pager)
    protected ViewPager pager;

    private ServerPagerAdapter adapter;
    private boolean busy = false;
    private MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainPresenter = new MainPresenter(this);
        mainPresenter.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainPresenter.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        mainPresenter.onStop();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadConfigFiles();
                return true;
            case R.id.action_export_public_key:
                exportPublicKey();
                return true;
            case R.id.action_send_public_key:
                new SshCommandPubKey().start();
                return true;
            case R.id.action_log:
                startActivity(new Intent(this, LogActivity.class));
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case STORAGE_PERMISSION_CONFIG_REQ:
                    loadConfigFiles();
                    break;
                case STORAGE_PERMISSION_PUB_REQ:
                    exportPublicKey();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_INFO_REQ) {
            loadConfigFiles();
        }
    }

    @Override
    public void loadConfigFiles() {
        if (!busy) {
            new AsyncTask<Void, Void, LoadConfigFilesStatus>() {
                @Override
                protected void onPreExecute() {
                    busy = true;
                    progress.setVisibility(View.VISIBLE);
                    empty.setVisibility(View.INVISIBLE);
                    pager.setVisibility(View.INVISIBLE);
                }

                @Override
                protected LoadConfigFilesStatus doInBackground(Void... params) {
                    return ConfigManager.getInstance().loadConfigFiles();
                }

                @Override
                protected void onPostExecute(LoadConfigFilesStatus status) {
                    progress.setVisibility(View.INVISIBLE);
                    if (ConfigManager.getInstance().getServers().size() > 0) {
                        adapter.updateServers(ConfigManager.getInstance().getServers());
                        pager.setVisibility(View.VISIBLE);
                        if (status == LoadConfigFilesStatus.ERROR) {
                            Toast.makeText(MainActivity.this, getString(status.message()), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        message.setText(getString(status.message(), ConfigManager.getInstance().getConfigDir()));
                        empty.setVisibility(View.VISIBLE);
                        if (status == LoadConfigFilesStatus.PERMISSION) {
                            settings.setVisibility(View.VISIBLE);
                            requestStoragePermission(STORAGE_PERMISSION_CONFIG_REQ);
                        } else {
                            settings.setVisibility(View.GONE);
                        }
                    }
                    busy = false;
                }
            }.execute();
        }
    }

    @Override
    public void initUi() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAppInfo();
            }
        });
    }

    private void exportPublicKey() {
        if (!busy) {
            new AsyncTask<Void, Void, ExportPublicKeyStatus>() {
                @Override
                protected void onPreExecute() {
                    showProgressDialog(getString(R.string.exporting_public_key));
                }

                @Override
                protected ExportPublicKeyStatus doInBackground(Void... params) {
                    return SshManager.getInstance().exportPublicKey();
                }

                @Override
                protected void onPostExecute(ExportPublicKeyStatus status) {
                    dismissProgressDialog();

                    boolean toast = true;
                    if (status == ExportPublicKeyStatus.PERMISSION) {
                        toast = !requestStoragePermission(STORAGE_PERMISSION_PUB_REQ);
                    }
                    if (toast) {
                        Toast.makeText(MainActivity.this, getString(status.message(), SshManager.getInstance().getPublicKeyExportedFile()), Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        }
    }

    private boolean requestStoragePermission(int req) {
        boolean requested = false;
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, req);
            requested = true;
        }
        return requested;
    }

    private void startAppInfo() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, APP_INFO_REQ);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandConfirm(final SshCommandConfirm event) {
        new MaterialDialog.Builder(this)
                .title(R.string.confirm_exec)
                .content(event.getCmd())
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(true);
                    }

                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(false);
                    }
                })
                .show();

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandStart(SshCommandStart event) {
        showProgressDialog(getString(R.string.sending_command));
        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandEnd(SshCommandEnd event) {
        shortToast(event.getStatus().message());
        dismissProgressDialog();

        EventBus.getDefault().removeStickyEvent(event);
        Intent logIntent = new Intent(this, LogActivity.class);
        logIntent.putExtra("result", event.getResult());
        startActivity(logIntent);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandPassword(final SshCommandPassword event) {
        new MaterialDialog.Builder(this)
                .title(R.string.password)
                .content(event.getMessage())
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .input(null, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        event.getDrop().put(input.toString());
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(null);
                    }
                })
                .show();

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandYesNo(final SshCommandYesNo event) {
        new MaterialDialog.Builder(this)
                .content(event.getMessage())
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(true);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(false);
                    }
                })
                .show();

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandMessage(final SshCommandMessage event) {
        new MaterialDialog.Builder(this)
                .content(event.getMessage())
                .positiveText(R.string.ok)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(true);
                    }
                })
                .show();

        EventBus.getDefault().removeStickyEvent(event);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSshCommandPubKeyInput(final SshCommandPubKeyInput event) {
        new MaterialDialog.Builder(this)
                .title(R.string.send_publick_key)
                .content(R.string.connection_string_message)
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .cancelable(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .alwaysCallInputCallback()
                .input(R.string.connection_string_hint, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (isConnectionStringValid(input.toString())) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        } else {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (dialog.getInputEditText() != null) {
                            String input = StringUtils.trimToNull(dialog.getInputEditText().getText().toString());
                            event.getDrop().put(input);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        event.getDrop().put(null);
                    }
                })
                .show();

        EventBus.getDefault().removeStickyEvent(event);
    }

    private boolean isConnectionStringValid(String input) {
        return input.matches("^.+@.+$");
    }

    @Override
    public void showServerList() {
        adapter = new ServerPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
    }
}
