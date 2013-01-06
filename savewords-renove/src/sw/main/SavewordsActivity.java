package sw.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class SavewordsActivity extends Activity {

	EditText input;
	String wordInput;
	RadioButton r0, r1;
	TextView tvTitulo;
	ListView lv;
	TextView seleccionado;
	List<String> resultados = new ArrayList<String>();

	String inst1 = "¡Puedes guardar tus traducciones!";
	String inst2 = "Cuando se muestren los resultados, haz click en la acepción que quieras guardar.";
	String inst3 = "Todas tus selecciones se iran guardando en el archivo \"savewords.txt\", almacenado en la raíz de tu tarjeta SD.";
	String inst4 = "Cuando quieras recuperarlas todas, puedes mandarlas por mail";
	String inst5 = "Existe la opcion de almacenar una direccion de correo favorita, para no tener que introducirla en cada envío que realices.";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		input = (EditText) findViewById(R.id.editText1);
		tvTitulo = new TextView(this);
		r0 = (RadioButton) findViewById(R.id.radio0); // sp -> en
		r1 = (RadioButton) findViewById(R.id.radio1);
		lv = (ListView) findViewById(R.id.list);
		seleccionado = (TextView) findViewById(R.id.seleccionado);

		input.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				input.setText("");
				tvTitulo.setText("");
			}
		});

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Filemanager fm = new Filemanager();
				String acepcion = resultados.get(position);
				if (acepcion.equals(inst1) | acepcion.equals(inst2)
						| acepcion.equals(inst3) | acepcion.equals(inst4)
						| acepcion.equals(inst5)) {
					seleccionado
							.setText("¡No se pueden guardar instrucciones!");
				} else {
					fm.writeFile("savewords.txt",
							wordInput + "\n" + resultados.get(position)
									+ "\n\n\n");
					seleccionado.setText("Traducción guardada.");
				}

			}
		});

	}// Cierre onCreate

	String prepararWordInput() {
		String word = input.getText().toString();
		word = word.replaceAll(" ", "-");
		if (word.endsWith("-"))
			word = (String) word.subSequence(0, word.length() - 1);
		System.out.println(word);
		return word;
	}

	public void go(View view) {
		Elements elementos;
		String lang = "";

		ocultarTeclado(input);

		wordInput = prepararWordInput();
		System.out.println("la word es " + wordInput);

		if (wordInput == "") {
			seleccionado.setText("¿Qué estamos buscando?");
		} else {

			if (r0.isChecked() == true) {
				lang = "intoEng";
				elementos = getTextFromWeb(lang, wordInput);
			} else {
				lang = "aEspa";
				elementos = getTextFromWeb(lang, wordInput);
			}

			// El primer elemento es la pronunciacion, el resto es cada uno una
			// acepcion
			try {
				if (!elementos.isEmpty()) {
					for (Element e : elementos) {
						resultados.add(e.text());
					}
					System.out.println("lo q debe ser pronunciacion"
							+ resultados.get(0));
					if (resultados.get(0) == new Element(Tag.valueOf("a"),
							"not found").text())
						seleccionado.setText("Pronunciación no encontrada");
					else if (lang.equals("aEspa"))
						seleccionado.setText("Pronunciación: "
								+ resultados.get(0));
					if (lang.equals("aEspa"))
						resultados.remove(0);
					lv.setAdapter(new ArrayAdapter<String>(this,
							R.layout.my_item_list, resultados));
				} else {
					// if (lang.equals("aEsp"))
					// seleccionado.setText("Pronunciación no encontrada ");
					Toast.makeText(this, "No encontrado", Toast.LENGTH_SHORT)
							.show();
					seleccionado.setText("No encontrado");
				}
			} catch (Exception e) { // que no pete!
				System.out.println("No se ha encontrado nada!");
				seleccionado.setText("No encontrado");
				Toast.makeText(this, "No encontrado", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public Elements getTextFromWeb(String lang, String word) {
		// borrar las cositas antiguas
		Toast t = Toast.makeText(this, "Buscando...", Toast.LENGTH_SHORT);
		t.setGravity(Gravity.TOP, 0, 0); // el show lo meto en el try
		seleccionado.setText("");
		resultados.clear();
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.my_item_list,
				resultados));

		Elements res = null;
		if (!networkAvailable(getApplicationContext())) {
			Toast.makeText(this, "¡Necesitas acceso a internet!",
					Toast.LENGTH_SHORT).show();
		} else {
			String url = "http://www.wordreference.com/es/translation.asp?tranword=";
			if (lang == "aEspa") {
				url += word;
				t.show();
				/* De ingles a español */
				try {
					Document doc = Jsoup.connect(url).get();
					/* Concise Oxford Spanish Dictionary © 2009 Oxford */
					if (doc.toString().contains(
							"Concise Oxford Spanish Dictionary")) {
						res = procesarOxford(doc);
					}
					/* Diccionario Espasa Concise © 2000 Espasa Calpe */
					else if (doc.toString().contains(
							"Diccionario Espasa Concise")) {
						res = procesarEspasa(doc);
					}
					/* WordReference English-Spanish Dictionary © 2012 */
					else if (doc.toString().contains(
							"WordReference English-Spanish Dictionary")) {
						res = procesarWR(doc);
					}
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "Error getting text from web",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				url = "http://www.wordreference.com/es/en/translation.asp?spen="
						+ word;
				t.show();
				/* De español a ingles */
				try {
					Document doc = Jsoup.connect(url).get();
					/* Concise Oxford Spanish Dictionary © 2009 Oxford */
					if (doc.toString().contains(
							"Concise Oxford Spanish Dictionary")) {
						res = procesarOxford2(doc);
					}
					/* Diccionario Espasa Concise © 2000 Espasa Calpe */
					else if (doc.toString().contains(
							"Diccionario Espasa Concise")) {
						res = procesarEspasa2(doc);
					}
					/* WordReference English-Spanish Dictionary © 2012 */
					// no hay
					// else if (doc.toString().contains(
					// "WordReference English-Spanish Dictionary")) {
					// res = procesarWR2(doc);
					// }
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this, "Error getting text from web",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
		return res;
	}

	/*
	 * ESPAÑOL -> INGLÉS Metodos para obtener la información de los
	 * diccionarios. Devuelve un conjunto de elementos, cuyo primer elemento es
	 * la pronunciacion y el resto las acepciones
	 */
	public Elements procesarOxford(Document doc) {
		System.out.println("Entramos a procesar Oxford!");
		Elements todo = new Elements();
		Element pronunciacion = doc.getElementById("article")
				.getElementsByClass("prn").first();
		// cojo totodo. Quito los especificos. Añado especificos
		Elements acepciones = doc.select("ol li");
		// System.out.println("start");
		acepciones.remove(acepciones.contains(doc.select("ol li ol li")));
		acepciones.addAll(doc.select("ol li ol li"));
		// System.out.println("fin");
		if (acepciones.size() < 2)
			acepciones = doc.getElementById("article").getElementsByClass("e");
		todo.add(pronunciacion);
		todo.addAll(acepciones);
		// System.out.println("Contenido en procesarOxford: " + todo);
		// System.out.println("Tamaño de todo en procesarOxford: " +
		// todo.size());
		if (todo.size() < 3)
			todo = procesarEspasa(doc);
		return todo;
	}

	public Elements procesarEspasa(Document doc) {
		System.out.println("Entramos a procesar Espasa!");
		Element pronunciacion = doc.getElementById("EsIPA");
		Elements acepciones = doc.select("div").select("span").select("table");
		Elements todo = new Elements();
		// System.out.println("Ha conseguido: " + pronunciacion + acepciones);
		todo.add(pronunciacion);
		todo.addAll(acepciones);
		// System.out.println("Contenido en procesarEspasa: " + todo);
		// System.out.println("Tamaño de todo en procesarEspasa: " +
		// todo.size());
		if (todo.size() < 3)
			todo = doc.getElementsByClass("e");
		if (todo.size() < 3)
			todo = procesarWR(doc);
		return todo;// en realidad lleva todo
	}

	public Elements procesarWR(Document doc) {
		System.out.println("Entramos a procesar WR!");
		// slowpoke, give up, get up
		System.out.println(doc);
		Elements todo = new Elements();
		todo.add(new Element(Tag.valueOf("a"), "not found"));
		todo.addAll(doc.getElementsByClass("even"));
		todo.addAll(doc.getElementsByClass("odd"));
		// todo.addAll(doc.select("td div table"));
		// System.out.println("Contenido en procesarWR: " + todo);
		// System.out.println("Tamaño de todo en procesarWR: " + todo.size());
		// todo.remove(todo.size()-1); //quitar report error
		return todo;

	}

	/*
	 * INGLÉS -> ESPAÑOL Metodos para obtener la información de los
	 * diccionarios. Devuelve un conjunto de elementos, que seran las acepciones
	 */
	public Elements procesarOxford2(Document doc) {
		System.out.println("Entramos a procesar Oxford2!");
		// System.out.println(doc);
		// System.out.println("----------------");
		Elements acepciones = doc.select("ol li");
		System.out.println("start");
		acepciones.remove(acepciones.contains(doc.select("ol li ol li")));
		acepciones.addAll(doc.select("ol li ol li"));
		// Casos pequeñitos como el de hola
		if (acepciones.size() < 2)
			acepciones = doc.getElementById("article").getElementsByClass(
					"clickable");
		// System.out.println("Contenido en procesarOxford2: " + acepciones);
		// System.out.println("Tamaño de todo en procesarOxford2: "+
		// acepciones.size());
		if (acepciones.size() < 1)
			acepciones = procesarEspasa(doc);
		return acepciones;
	}

	public Elements procesarEspasa2(Document doc) {
		System.out.println("Entramos a procesar Espasa2!");
		Elements acepciones = doc.select("div").select("span").select("table");
		if (acepciones.size() < 2)
			acepciones = doc.select("div").select("span");
		Elements todo = new Elements();
		todo.addAll(acepciones);
		// System.out.println("Contenido en procesarEspasa2: " + todo);
		// System.out.println("Tamaño de todo en procesarEspasa2: " +
		// todo.size());
		return todo;
	}

	// No hay para español
	// public Elements procesarWR2(Document doc) {
	// System.out.println("Entramos a procesar WR2!");
	// // http://www.wordreference.com/es/translation.asp?tranword=slowpoke
	// Elements todo = new Elements();
	// todo.add(new Element(Tag.valueOf("a"), "not found"));
	// todo.addAll(doc.getElementsByClass("even"));
	// todo.addAll(doc.getElementsByClass("odd"));
	// System.out.println("Contenido en procesarWR2: " + todo);
	// System.out.println("Tamaño de todo en procesarWR2: " + todo.size());
	// // todo.remove(todo.size()-1); //quitar report error
	// return todo;
	//
	// }

	/* Otros */

	public void options(View view) {
		startActivity(new Intent(this, Options.class));
	}

	public void instrucciones(View view) {
		resultados.clear();
		resultados.add(inst1);
		resultados.add(inst2);
		resultados.add(inst3);
		resultados.add(inst4);
		resultados.add(inst5);
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.my_item_list,
				resultados));
		ocultarTeclado(input);
	}

	public void sendFile(View view) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(
				Intent.EXTRA_EMAIL,
				new String[] { getSharedPreferences("datos",
						Context.MODE_PRIVATE).getString("mail", "") });
		sendIntent.putExtra(Intent.EXTRA_STREAM,
				Uri.fromFile(new File("/sdcard/savewords.txt")));
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Savewords mail!");
		sendIntent.putExtra(Intent.EXTRA_TEXT, "");
		sendIntent.setType("**/**");
		startActivity(Intent.createChooser(sendIntent, "Send by"));

	}

	public boolean networkAvailable(Context appContext) {
		Context context = appContext;
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectMgr != null) {
			NetworkInfo[] netInfo = connectMgr.getAllNetworkInfo();
			if (netInfo != null) {
				for (NetworkInfo net : netInfo) {
					if (net.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} else {
			Log.d("NETWORK", "No network available");
		}
		return false;
	}

	public void ocultarTeclado(EditText myEditText) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
	}

}