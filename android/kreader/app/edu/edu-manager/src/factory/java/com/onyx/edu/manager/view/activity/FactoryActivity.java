package com.onyx.edu.manager.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.DatabaseInfo;
import com.onyx.android.sdk.data.db.ContentDatabase;
import com.onyx.android.sdk.data.model.v2.DeviceBind;
import com.onyx.android.sdk.data.model.v2.DeviceBind_Table;
import com.onyx.android.sdk.data.request.data.db.BackupRestoreDBRequest;
import com.onyx.android.sdk.data.request.data.db.BaseDBRequest;
import com.onyx.android.sdk.data.utils.JSONObjectParseUtils;
import com.onyx.android.sdk.ui.utils.ToastUtils;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.edu.manager.R;
import com.onyx.edu.manager.view.ui.DividerDecoration;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by suicheng on 2017/6/24.
 */

public class FactoryActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_QR_CODE = 1000;

    private static final int NOTHING_PERMISSIONS_REQUEST = -1000;
    private static final int STORAGE_PERMS_REQUEST_CODE = 1012;
    private static final String[] STORAGE_PERMS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Bind(R.id.bindDevice_list_view)
    RecyclerView bindListView;
    @Bind(R.id.tv_count_scanned)
    TextView countScannedTv;

    private DataManager dataManager;
    private List<DeviceBind> deviceBindList = new ArrayList<>();

    private long countScanned = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory);
        ButterKnife.bind(this);

        initConfig();
        initView();
        initData();
    }

    private void initData() {
        queryDeviceBindTableCount();
    }

    private void initConfig() {
        try {
            FlowConfig.Builder builder = new FlowConfig.Builder(getApplicationContext());
            FlowManager.init(builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("扫码绑定");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bindListView.setLayoutManager(new LinearLayoutManager(this));
        bindListView.addItemDecoration(new DividerDecoration(this));
        bindListView.setAdapter(new RecyclerView.Adapter<DeviceBindHolder>() {
            @Override
            public DeviceBindHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new DeviceBindHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_bind_item, null));
            }

            @Override
            public void onBindViewHolder(DeviceBindHolder holder, int position) {
                holder.macTv.setText(String.valueOf(deviceBindList.get(position).mac));
            }

            @Override
            public int getItemCount() {
                return CollectionUtils.getSize(deviceBindList);
            }
        });
    }

    @OnClick(R.id.btn_scanner)
    public void onDeviceScanClick() {
        Intent intent = new Intent("onyx.intent.action.QrCodeScanner");
        startActivityForResult(intent, REQUEST_QR_CODE);
    }

    private File getContentDatabaseFile() {
        return getDatabasePath(ContentDatabase.NAME + ".db");
    }

    //@OnClick(R.id.toolbar_db_export)
    public void onDBExportClick() {
        if (!getContentDatabaseFile().exists()) {
            ToastUtils.showToast(getApplicationContext(), "数据文件不存在");
            return;
        }
        requestExportDbFile();
    }

    public void onDBClearClick() {
        showAlertDialog("警告", "此操作会清除掉数据库里所有的记录，请谨慎操作！！", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Delete.table(DeviceBind.class);
                queryDeviceBindTableCount();
            }
        });
    }

    private void showAlertDialog(String title, String content, MaterialDialog.SingleButtonCallback positiveCallback) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColor(Color.GRAY)
                .positiveText("确认")
                .negativeText("取消")
                .onPositive(positiveCallback);
        if (StringUtils.isNotBlank(title)) {
            builder.title(title);
        }
        if (StringUtils.isNotBlank(content)) {
            builder.content(content);
        }
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_QR_CODE:
                processRequestQrCode(intent);
                break;
        }
    }

    private void processRequestQrCode(Intent intent) {
        if (intent == null) {
            return;
        }
        String qrCode = intent.getStringExtra("qrCode");
        final DeviceBind deviceBind = JSONObjectParseUtils.parseObject(qrCode, DeviceBind.class);
        if (deviceBind != null) {
            saveDeviceBind(deviceBind, new ProcessModelTransaction.OnModelProcessListener<DeviceBind>() {
                @Override
                public void onModelProcessed(long current, long total, DeviceBind modifiedModel) {
                    deviceBindList.add(0, modifiedModel);
                    bindListView.getAdapter().notifyItemInserted(0);
                    updateCountScanned(countScanned++);
                }
            });
        }
    }

    private void saveDeviceBind(DeviceBind deviceBind, ProcessModelTransaction.OnModelProcessListener<DeviceBind> listener) {
        ProcessModelTransaction<DeviceBind> processModelTransaction =
                new ProcessModelTransaction.Builder<>(new ProcessModelTransaction.ProcessModel<DeviceBind>() {
                    @Override
                    public void processModel(DeviceBind model, DatabaseWrapper wrapper) {
                        model.save();
                    }
                }).processListener(listener).add(deviceBind).build();
        Transaction transaction = FlowManager.getDatabase(ContentDatabase.class)
                .beginTransactionAsync(processModelTransaction).build();
        transaction.execute();
    }

    private void queryDeviceBindTableCount() {
        final CountRequest countRequest = new CountRequest();
        getDataManager().submit(this, countRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                updateCountScanned(countScanned = countRequest.getCount());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_factory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_export_db:
                onDBExportClick();
                return true;
            case R.id.action_clear_db:
                onDBClearClick();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("DefaultLocale")
    private void updateCountScanned(long count) {
        countScannedTv.setText(String.format("已扫描过的设备数量：%d", countScanned));
    }

    protected int getPermissionRequestCode() {
        return 1;
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (getPermissionRequestCode() == NOTHING_PERMISSIONS_REQUEST) {
            Toast.makeText(getApplicationContext(), "Permission RequestCode must be override",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
            showAppSettingsDialog(FactoryActivity.this, getPermissionRequestCode());
        } else {
            processTemporaryPermissionsDenied(requestCode, list);
        }
    }

    private void showAppSettingsDialog(Activity context, int requestCode) {
        new AppSettingsDialog.Builder(context, "db文件导出到sd卡，需要获取读取外部存储卡的权限")
                .setTitle("去往权限管理设置界面")
                .setPositiveButton("前往")
                .setNegativeButton("取消", null)
                .setRequestCode(requestCode)
                .build()
                .show();
    }

    protected void processTemporaryPermissionsDenied(int requestCode, List<String> list) {
        Toast.makeText(getApplicationContext(), "申请权限被拒绝，无法往下操作",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @AfterPermissionGranted(STORAGE_PERMS_REQUEST_CODE)
    private void requestExportDbFile() {
        String[] perms = STORAGE_PERMS;
        if (EasyPermissions.hasPermissions(this, perms)) {
            afterExportDbPermissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, "导出db文件，需要申请读取存储卡的权限",
                    STORAGE_PERMS_REQUEST_CODE, perms);
        }
    }

    private void afterExportDbPermissionGranted() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showAlertDialog(null, "导出的路径：" + getExportFilePath().replaceAll(Environment.getExternalStorageDirectory().getAbsolutePath(), ""),
                    new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            exportDbFileToSdCard(getExportFilePath());
                        }
                    });
        } else {
            ToastUtils.showToast(getApplicationContext(), "没有外部存储");
        }
    }

    private String getExportFilePath() {
        File dir = new File(Environment.getExternalStorageDirectory(), "Onyx工厂管理/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File exportDBFile = new File(dir, ContentDatabase.NAME + "-" +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(new Date()) +
                ".db");
        return exportDBFile.getAbsolutePath();
    }

    private void exportDbFileToSdCard(String exportFilePath) {
        Map<DatabaseInfo, DatabaseInfo> backupRestoreDBMap = new HashMap<>();
        backupRestoreDBMap.put(DatabaseInfo.create(ContentDatabase.NAME, ContentDatabase.VERSION,
                getContentDatabaseFile().getAbsolutePath()),
                DatabaseInfo.create(exportFilePath));
        BackupRestoreDBRequest restoreDBRequest = new BackupRestoreDBRequest(backupRestoreDBMap, true);
        getDataManager().submit(this, restoreDBRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                ToastUtils.showToast(request.getContext().getApplicationContext(), e != null ? "导出失败" : "导出成功");
            }
        });
    }

    private class CountRequest extends BaseDBRequest {
        long count = 0;

        public long getCount() {
            return count;
        }

        @Override
        public void execute(DataManager dataManager) throws Exception {
            //SELECT COUNT(DISTINCT column(s)) FROM table
            count = new Select(Method.count(DeviceBind_Table.mac.distinct()))
                    .from(DeviceBind.class).where().count();
        }
    }

    private DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    class DeviceBindHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.mac_tv)
        TextView macTv;

        public DeviceBindHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
