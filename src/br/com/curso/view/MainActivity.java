package br.com.curso.view;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.json.JSONException;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import br.com.curso.adapter.ListAdapterAluno;
import br.com.curso.controller.HttpController;
import br.com.curso.dao.AlunoDao;
import br.com.curso.model.Aluno;
import br.com.curso.model.ConnectionPreferences;
import br.com.curso.utils.Constantes;
import br.com.curso.utils.Sms;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity implements OnItemClickListener, OnInitListener {

	private ListAdapterAluno adapter = null;
	
//	@Inject
	private AlunoDao alunoDao;
	
	@InjectView(R.id.lvAlunos)
	private ListView lvAlunos;

	@InjectResource(R.array.menu_opcoes)
	private String [] menu_opcoes;

	@Inject
	private ConnectionPreferences preferences;
	
	// TextToSpeech
	private static final int REQ_CODE_TTS = 101;
	private TextToSpeech tts;
	private String textToSpeech = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Instancia o Adapter
		adapter = new ListAdapterAluno(getApplicationContext(),
				R.layout.adapter_aluno_item, null);
		
		alunoDao = new AlunoDao(getApplicationContext());
		
		// Configura o ListView
		lvAlunos.setClickable(true);
		lvAlunos.setOnItemClickListener(this);
		registerForContextMenu(lvAlunos);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// Obtem a lista dos alunos que estao salvos no diretorio
		alunoDao.open();
		List<Aluno> alunos = alunoDao.listar();
		alunoDao.close();
		
		// Adiciona os alunos na tela
		adapter.newList(alunos);
		lvAlunos.setAdapter(adapter);
	}
	
	@Override
	protected void onPause() {
//		alunoDao.close();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_novo:
			// Inicia a Activity do Formulario
			startActivity(new Intent(this, FormActivity.class));			
			break;
			
		case R.id.menu_buscar:
			// Inicia a Activity do Buscar
			startActivity(new Intent(this, BuscarActivity.class));			
			break;
			
		case R.id.menu_baixar:
			sendHttpGet(Constantes.SERVLET_RECEBER_ALUNO, "");			
			break;
			
		case R.id.menu_preferences:
			startActivity(new Intent(this, PreferencesActivity.class));			
			break;
			
		}
		
		return true;
	}
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) {
    	
    	if(v.getId() == R.id.lvAlunos){    		
			AdapterView.AdapterContextMenuInfo info = 
					(AdapterView.AdapterContextMenuInfo) menuInfo;
			ListAdapterAluno adapter = (ListAdapterAluno) lvAlunos.getAdapter();				
			menu.setHeaderTitle(adapter.getAlunos().get(info.position).getNome());
			
			for (int i = 0; i < menu_opcoes.length; i++) {
				menu.add(Menu.NONE, i, i, menu_opcoes[i]);
			}		
    	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	ListAdapterAluno adapter = (ListAdapterAluno) lvAlunos.getAdapter();				
				
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final Aluno selecionado = adapter.getAlunos().get(info.position);
		
		int menuItemIndex = item.getItemId();
		String menuItemName = menu_opcoes[menuItemIndex];
		
//		String listItemName = selecionado.getNome();
//		Log.i("aluno", String.format("Selected %s for item %s", menuItemName, listItemName));
		
		if(selecionado != null){					
			if(menuItemName.equalsIgnoreCase("Fazer uma Ligação")){
				fazerLigacao(selecionado);
				
			} else if(menuItemName.equalsIgnoreCase("Enviar SMS")){						
				enviarSms(selecionado);				
				
			} else if(menuItemName.equalsIgnoreCase("Buscar no Mapa")){
				localizarEndereco(selecionado);
				
			} else if(menuItemName.equalsIgnoreCase("Editar")){
				editar(selecionado);
				
			} else if(menuItemName.equalsIgnoreCase("Remover")){
				remover(selecionado);
				
			} else if(menuItemName.equalsIgnoreCase("Falar")){
				textToSpeech(selecionado);
				
			} else if(menuItemName.equalsIgnoreCase("Enviar por HTTP")){
				
				try {
					sendHttpPost(Constantes.SERVLET_ENVIAR_ALUNO, Aluno.toJson(selecionado));
				} catch (JSONException e) {
					Log.e(Constantes.CATEGORIA, "Erro ao converter aluno para JSON");
				}
				
			}
		}
		
		return true;
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position,
			long id) {
		
		Aluno selecionado = (Aluno) adapter.getItem(position);		
		editar(selecionado);
	}

	private void fazerLigacao(final Aluno selecionado) {
		Uri uri = Uri.parse("tel:" + selecionado.getTelefone());
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(it);
	}

	private void localizarEndereco(final Aluno aluno) {
//		String endereco = aluno.getEndereco().trim();
//		endereco = endereco.replace(" ", "+");
//		
//		Uri uri = Uri.parse("geo:0,0?q=" + endereco);
//		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		
		Intent it = new Intent(getApplicationContext(), MapaActivity.class);
		it.putExtra("aluno", aluno);
		startActivity(it);
	}

	private void enviarSms(final Aluno selecionado) {
		LayoutInflater li = getLayoutInflater();
		View dialogSms = li.inflate(R.layout.dialog_sms, null);
		Button btDialogEnviar = (Button) dialogSms.findViewById(R.id.btDialogEnviar);
		final EditText etDialogEnviar = (EditText) dialogSms.findViewById(R.id.etDialogEnviar);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("SMS para " + selecionado.getNome());
		builder.setView(dialogSms);
		final AlertDialog alerta = builder.create();
		alerta.show();

		btDialogEnviar.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View arg0) {
		    	String texto = etDialogEnviar.getText().toString().trim();
		    	
		    	boolean isEnviado = Sms.enviarSms(getApplicationContext(), selecionado.getTelefone(), texto);
				
				if(isEnviado){
					Toast.makeText(getApplicationContext(), "Mensagem enviada!", Toast.LENGTH_SHORT).show();
				} else{
					Toast.makeText(getApplicationContext(), "Falha ao enviar a mensagem!", Toast.LENGTH_SHORT).show();
				}
				
				// Get instance of Vibrator from current Context
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(new long[]{ 100, 250, 100, 500 }, -1);
				
		        alerta.dismiss();
		    }
		});
	}

	private void editar(Aluno aluno) {
		if(aluno != null){
//			String idStr = String.valueOf(aluno.getId());
			
			Intent it = new Intent(this, FormActivity.class);
			it.putExtra(FormActivity.INTENT_EXTRA_DATA_ALUNO, aluno);
			
			startActivity(it);
		}
	}

	private void remover(final Aluno aluno) {
		if(aluno != null){
//			Log.i("aluno", "[remover] id_aluno: " + aluno.getId());
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Confirmação");
			alert.setMessage("Deseja remover o(a) aluno(a) " + aluno.getNome() + "?");
			
			alert.setPositiveButton("Sim", new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					alunoDao.open();
					alunoDao.deletar(aluno);
					alunoDao.close();
					
					onResume();
				}
			});
			
			alert.setNegativeButton("Não", new OnClickListener() {				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			
			alert.show();
		}
	}

	private void textToSpeech(Aluno selecionado) {
		this.textToSpeech = selecionado.getNome();
		
		Intent intent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, REQ_CODE_TTS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_TTS){
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
				tts = new TextToSpeech(MainActivity.this, MainActivity.this);
				tts.setLanguage(new Locale("pt_BR"));
				
				// Listar os idiomas disponiveis
//				Log.i("-------------",Arrays.toString(Locale.getAvailableLocales()));				
			} else{
				// Dados TTS ainda nao carregados, tenta instala-lo
				Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onInit(int status) {
		if(status == TextToSpeech.SUCCESS){
			if(textToSpeech != null && !textToSpeech.equals("")){
				tts.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null);
				textToSpeech = "";
			}
		} else{
			tts.shutdown();
		}
	}

	private void sendHttpGet(String servletName, String message) {
		// Ler das configuracoes salvas
		HttpController.IP_PORT = preferences.getServerIp() + ":" + preferences.getServerPort();
		
		RequestParams params = new RequestParams();
		params.put("message", message);
		
		HttpController.get(servletName, params, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	
		    	if(response.trim().equalsIgnoreCase("error")){
		    		Toast.makeText(MainActivity.this, "Erro ao receber o aluno no servidor!", Toast.LENGTH_SHORT).show();
		    		
		    	} else{
		    		try {
						Aluno aluno = Aluno.fromJson(response);
						
						Toast.makeText(
								MainActivity.this,
								"Aluno " + aluno.getNome() + " recebido com sucesso",
								Toast.LENGTH_SHORT).show();
						
					} catch (JSONException e) {
						Log.e(Constantes.CATEGORIA, "Erro ao converter aluno para JSON");
					}
		    	}
		    }
		});
	}

	private void sendHttpPost(String servletName, String message) {
		// Ler das configuracoes salvas
		HttpController.IP_PORT = preferences.getServerIp() + ":" + preferences.getServerPort();
		
		RequestParams params = new RequestParams();
		params.put("message", message);
		
		HttpController.post(servletName, params, new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		    	if(response.trim().equalsIgnoreCase("ok")){
		    		Toast.makeText(MainActivity.this, "Aluno enviado com sucesso", Toast.LENGTH_SHORT).show();
		    	} else{
		    		Toast.makeText(MainActivity.this, "Erro ao receber o aluno no servidor!", Toast.LENGTH_SHORT).show();
		    	}
		    }
		});
	}

}