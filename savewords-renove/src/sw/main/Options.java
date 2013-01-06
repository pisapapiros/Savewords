package sw.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Options extends Activity {

	EditText et1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.options);

		et1 = (EditText) findViewById(R.id.editText1);

		SharedPreferences prefe = getSharedPreferences("datos",
				Context.MODE_PRIVATE);
		et1.setText(prefe.getString("mail", ""));

	}

	public void saveMail(View v) {
		SharedPreferences preferencias = getSharedPreferences("datos",
				Context.MODE_PRIVATE);
		Editor editor = preferencias.edit();
		editor.putString("mail", et1.getText().toString());
		editor.commit();
		finish();
	}

}
