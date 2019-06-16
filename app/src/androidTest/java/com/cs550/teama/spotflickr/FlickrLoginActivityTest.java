package com.cs550.teama.spotflickr;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.webkit.CookieManager;

import com.cs550.teama.spotflickr.activity.auth.FlickrLoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FlickrLoginActivityTest{
    private FlickrLoginActivity activity;
    //private String webViewCurrentUrl;

    @Rule
    public ActivityTestRule<FlickrLoginActivity> mActivityRule = new ActivityTestRule<FlickrLoginActivity>(
            FlickrLoginActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        activity = mActivityRule.launchActivity(new Intent());

        // remove the cookies (to remove saved credentials from webView)
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

    @After
    public void tearDown() throws Exception {
        mActivityRule.finishActivity();
    }

    @Test
    public void testLaunching() {
        assertViewIsDisplayed(R.id.toolbar);
        assertViewIsDisplayed(R.id.webView);
        onView(withId(R.id.progressBar_cyclic)).check(matches(not(isDisplayed())));
        assertViewIsDisplayed(R.id.swipe);
    }

    private void assertViewIsDisplayed(int id){
        onView(withId(id)).check(matches(isDisplayed()));
    }

    @Test
    public void testFlickrLogin() throws Exception{
        String urlWithoutToken = "https://www.flickr.com/services/oauth/authorize?oauth_token=";
        assertViewIsDisplayed(R.id.webView);

        // Set a time limit since internet connection could vary
        int timeLimit = 5; // seconds
        // Assert getting authorization url within time limit
        for (int i = 1; i <= timeLimit; i++){
            if (!activity.oauth.getAuthorizationURL().equals(urlWithoutToken + "null"))
                break; // true asserted
            if (i >= timeLimit)
                fail();
            Thread.sleep(1000);
        }
        assertWebViewUrl("https://identity.flickr.com/login", 10);
        Thread.sleep(3000);

        // Type in email
        onView(withId(R.id.webView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB));
        Thread.sleep(500);
        onView(withId(R.id.webView)).perform(typeText(activity.getString(R.string.flickr_user_email)+"\n"));
        closeSoftKeyboard();
        assertWebViewUrl("https://login.yahoo.com/", 10);
        Thread.sleep(1000);

        // Press next button
        onView(withId(R.id.webView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));
        assertWebViewUrl("https://login.yahoo.com/account/challenge/password", 10);
        Thread.sleep(1000);

        // Type in password
        onView(withId(R.id.webView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB));
        Thread.sleep(500);
        onView(withId(R.id.webView)).perform(typeText(activity.getString(R.string.flickr_user_password)+"\n"));
        closeSoftKeyboard();
        assertWebViewUrl("https://www.flickr.com/services/oauth/authorize", 20);
        Thread.sleep(1000);

        // Press Authorize App button
        onView(withId(R.id.webView)).perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_TAB))
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER));
        Thread.sleep(1000);

        // Assert the activity is finishing (or finished) within a time limit
        timeLimit = 10; // seconds
        for (int i = 1; i <= timeLimit; i++){
            if (mActivityRule.getActivity().isFinishing())
                break; // true asserted
            if (i >= timeLimit)
                fail("Activity did not finish");
            Thread.sleep(1000);
        }

        // Assert the resultCode is OK, indicating a successful oauth
        assertEquals(mActivityRule.getActivityResult().getResultCode(), Activity.RESULT_OK);
    }

    private void assertWebViewUrl(String baseUrl, int timeLimit){
        final String[] webViewCurrentUrl = {"http://www.test.com/test?test=test"}; // Updated from the webView thread. Might be slow to update
        for (int i = 1; i <= timeLimit; i++){
            try {
                // Make a request for the current url of the webView. This might take some time.
                activity.webView.post(() -> webViewCurrentUrl[0] = activity.webView.getUrl());

                // webViewCurrentUrl could return an old url.
                String curBaseUrl;
                if (webViewCurrentUrl[0].indexOf('?') != -1)
                    curBaseUrl = webViewCurrentUrl[0].substring(0, webViewCurrentUrl[0].indexOf('?'));
                else
                    curBaseUrl = webViewCurrentUrl[0];
                if (curBaseUrl.equals(baseUrl))
                    return; // True asserted
                Thread.sleep(1000);
            } catch(InterruptedException e){
                break;
            }
        }
        fail("Failed to assert URL");
    }

    @Test
    public void onBackPressed() throws Exception{
        activity.webView.post(()->{
            activity.onBackPressed();

            // Assert the resultCode is Canceled, indicating a successful oauth
            assertEquals(mActivityRule.getActivityResult().getResultCode(), Activity.RESULT_CANCELED);
        });
    }

    @Test
    public void onLoginSuccess() {
        activity.webView.post(()->{
            activity.onLoginSuccess();

            // Assert the resultCode is Canceled, indicating a successful oauth
            assertEquals(mActivityRule.getActivityResult().getResultCode(), Activity.RESULT_OK);
        });
    }

    @Test
    public void onLoginFail() {
        activity.webView.post(()->{
            activity.onLoginFail();

            // Assert the resultCode is Canceled, indicating a successful oauth
            assertEquals(mActivityRule.getActivityResult().getResultCode(), Activity.RESULT_CANCELED);
        });
    }
}