package com.hookmobile.ageui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.apache.commons.lang3.text.WordUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey Chorniy on December 13, 2013
 *
 * Take the name from the list of accounts returned by {@link android.accounts.AccountManager#getAccountsByType(String)}
 */
public class CurrentUserInviteProvider extends InviterProvider.StaticValueInviterProvider {


    public CurrentUserInviteProvider(Context context) {
        super(resolveUsernameByAccount(context));
    }


    private static String resolveUsernameByAccount(Context context) {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            for (String possibleEmail : possibleEmails) {
                String name = convertToName(possibleEmail);
                if (name != null) {
                    return name;
                }
            }
            return null;
        } else
            return null;
    }

    private static String convertToName (String email) {
        String[] parts = email.split("@");
        if (parts.length > 0 && parts[0] != null) {
            String namePart = parts[0];
            String name = namePart.replaceAll("[\\W]", " ");

            return WordUtils.capitalizeFully(name);
        }
        return null;
    }

}
