package br.com.curso.view;

import javax.inject.Inject;

import br.com.curso.model.ConnectionPreferences;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@ContentView(R.layout.activity_preferences)
public class PreferencesActivity extends RoboActivity implements OnClickListener {

	@InjectView(R.id.etIp)
	private EditText etIp;

	@InjectView(R.id.etPort)
	private EditText etPort;
	
	@InjectView(R.id.btConnect)
	private Button btConnect;
	
	@Inject
	private ConnectionPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		btConnect.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(btConnect == v){
			String ip = etIp.getText().toString();
			String port = etPort.getText().toString();
			
			if(!ip.equals("") && !port.equals("")){
				preferences.store(ip, port);
				Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
				
				return;
			}
			
			Toast.makeText(this, "Insira um IP e Porta válido!", Toast.LENGTH_SHORT).show();
		}		
	}
}