package br.com.curso.model;

import javax.inject.Inject;
import android.content.SharedPreferences;

/**
 * 
 * Classe que armazena IP e Porta do servidor
 * nas preferencias.
 * 
 * @author Gabriel Tavares
 *
 */
public class ConnectionPreferences {

	private final static String SERVER_IP = "alunoce_ip";
	private final static String SERVER_PORT = "alunoce_port";

	@Inject 
	private SharedPreferences prefs;

	public void store(String ip, String port) {
		prefs.edit().putString(SERVER_IP, ip).commit();
		prefs.edit().putString(SERVER_PORT, port).commit();
	}

	public boolean isServerIp() {
		return prefs.contains(SERVER_IP);
	}

	public boolean isServerPort() {
		return prefs.contains(SERVER_PORT);
	}

	public String getServerIp() {
		return prefs.getString(SERVER_IP, "10.0.2.2");
	}
	
	public String getServerPort() {
		return prefs.getString(SERVER_PORT, "8080");
	}

}
