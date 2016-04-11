
package com.gmail.sanovikov71.githubclient.ui;

import com.gmail.sanovikov71.githubclient.model.User;

import java.util.List;

public interface UiElement {
    void showProgressDialog();

    void hideProgressDialog();

    void showError(int stringId);
}
