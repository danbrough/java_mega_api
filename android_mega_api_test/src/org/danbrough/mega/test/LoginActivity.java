package org.danbrough.mega.test;

import org.danbrough.mega.LoginRequest;
import org.danbrough.mega.MegaAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonElement;

public class LoginActivity extends Activity {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(LoginActivity.class.getSimpleName());

  MegaAPI mega = TestApplication.getInstance().getMega();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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

              mega.createUserContext(username.getText().toString(), password
                  .getText().toString());

              loginTest();
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
            return null;
          }
        }.execute();

      }
    });
  }

  private void loginTest() {
    log.info("loginTest()");

    try {
      new LoginRequest(mega) {
        @Override
        public void onResponse(JsonElement response) {
          super.onResponse(response);
          TestApplication.getInstance().saveUserContext();
          startActivity(new Intent(getApplicationContext(), TestActivity.class));
        };

      }.send();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

  }
}
