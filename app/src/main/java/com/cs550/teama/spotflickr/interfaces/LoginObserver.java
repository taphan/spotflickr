package com.cs550.teama.spotflickr.interfaces;

import com.cs550.teama.spotflickr.services.OAuthService;

public interface LoginObserver {
    void onRequestTokenReceived(OAuthService oauth);
    void onLoginFail();
    void onLoginSuccess();
}
