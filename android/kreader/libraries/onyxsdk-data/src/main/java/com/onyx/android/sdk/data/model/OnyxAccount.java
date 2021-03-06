package com.onyx.android.sdk.data.model;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by solskjaer49 on 15/5/6 17:34.
 */
public class OnyxAccount extends BaseData {

    private static final String CLOUD_TYPE = "onyxCloud";
    private static final String ACCOUNT_INFO_TAG = "accountInfo";
    private static final String JSON_TAG = "json";

    static public class AccountInfo {
        public LinkedHashMap<String, OnyxAccount> accountMap = new LinkedHashMap(16, 0.75F, true);
        public OnyxAccount currentAccount;

        public OnyxAccount getCurrentAccount() {
            if (currentAccount == null) {
                if (accountMap.size() > 0) {
                    Iterator<Map.Entry<String, OnyxAccount>> iterator = accountMap.entrySet().iterator();
                    for (int i = 0; i < accountMap.size() - 1; i++) {
                        iterator.next();
                    }
                    currentAccount = iterator.next().getValue();
                }
            }
            return currentAccount;
        }

        public void addAccount(final OnyxAccount account) {
            accountMap.put(account.sessionToken, account);
            currentAccount = account;
        }

        public void clear() {
            accountMap.clear();
            currentAccount = null;
        }
    }

    private static AccountInfo accountInfo = new AccountInfo();

    public String nickName;
    public String firstName;
    public String lastName;
    public String fullName;
    public String password;
    public String email;
    public String mobile;

    public String sessionToken;

    public String captchaId;
    public String captchaValue;
    public boolean isInstallationId = false;
    public String deviceClient = "boox";
    public String type;
    public String avatarUrl;
    public int status;

    public OnyxAccount() {
    }

    public OnyxAccount(String fullName, String password, String email) {
        this.fullName = fullName;
        this.password = password;
        this.email = email;
    }

    static public OnyxAccount getCurrentAccount() {
        return accountInfo.getCurrentAccount();
    }

    static public OnyxAccount defaultAccount() {
        OnyxAccount account = new OnyxAccount("john2", "123456", "john2@onyx-international.com");
        return account;
    }

    static public void generateDefaultAccount(final Context context) {
        clear(context);
        saveAccount(context, defaultAccount());
    }

    static public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    static public void saveAccount(final Context context, final OnyxAccount onyxAccount) {
        accountInfo.addAccount(onyxAccount);
        SecurePreferences preferences = new SecurePreferences(context, CLOUD_TYPE, ACCOUNT_INFO_TAG, true);
        preferences.put(JSON_TAG, JSON.toJSONString(accountInfo));
    }

    static public OnyxAccount loadAccount(final Context context) {
        SecurePreferences preferences = new SecurePreferences(context, CLOUD_TYPE, ACCOUNT_INFO_TAG, true);
        final String string = preferences.getString(JSON_TAG);
        if (StringUtils.isNullOrEmpty(string)) {
            accountInfo = new AccountInfo();
            return null;
        }
        accountInfo = JSON.parseObject(string, AccountInfo.class);
        return accountInfo.getCurrentAccount();
    }

    static public String loadAccountSessionToken(Context context) {
        if (accountInfo.getCurrentAccount() != null) {
            return accountInfo.getCurrentAccount().sessionToken;
        }
        OnyxAccount account = loadAccount(context);
        return account == null ? null : account.sessionToken;
    }

    static public void clear(final Context context) {
        SecurePreferences preferences = new SecurePreferences(context, CLOUD_TYPE, ACCOUNT_INFO_TAG, true);
        preferences.clear();
        accountInfo.clear();
    }

}
