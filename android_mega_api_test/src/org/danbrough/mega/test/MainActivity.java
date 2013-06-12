package org.danbrough.mega.test;

import org.danbrough.mega.GetUserRequest;
import org.danbrough.mega.LoginRequest;
import org.danbrough.mega.MegaAPI;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MainActivity.class.getSimpleName());

  MegaAPI mega;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mega = new MegaAPI();
    mega.start();

    log.error("TERMSDS: " + mega.getTermsHTML());

    final TextView username = (TextView) findViewById(R.id.txtUserName);
    final TextView password = (TextView) findViewById(R.id.txtPassword);

    Button b = (Button) findViewById(R.id.button1);
    b.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        log.info("ok");

        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {

            try {
              loginTest(username.getText().toString(), password.getText()
                  .toString());
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
            return null;
          }
        }.execute();

      }
    });
  }

  private void loginTest(String email, String password) throws Exception {
    log.info("loginTest()");

    mega.sendRequest(new LoginRequest(email, password) {

      @Override
      protected void onResponse(JSONObject response) {
        log.debug("got response: {}", response);
        mega.sendRequest(new GetUserRequest(ctx));
      }
    });

  }
}
