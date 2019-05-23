package com.cs550.teama.spotflickr.interfaces;

import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.OAuthServiceWithMapParams;

public interface LoginObserver {
    void onRequestTokenReceived(OAuthService oauth);
    void onLoginFail();
    void onLoginSuccess();

    void onRequestTokenReceived(OAuthServiceWithMapParams oauth);
}
