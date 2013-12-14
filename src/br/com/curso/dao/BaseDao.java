package br.com.curso.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BaseDao extends SQLiteOpenHelper {

/**
 * 
 * Classe de configurações do Banco SQLite
 * 
 * @author Gabriel Tavares
 *
 */
public static final String LOG_BD = "bd";
	

	public static final String TABLE_NAME = "aluno";
	
	public static final String ALUNO_ID = "_id";
	public static final String ALUNO_NOME = "nome";
	public static final String ALUNO_FOTO = "foto";
	public static final String ALUNO_TELEFONE = "telefone";
	public static final String ALUNO_EMAIL = "email";
	public static final String ALUNO_ENDERECO = "endereco";
	public static final String ALUNO_CURSO = "curso";
	public static final String ALUNO_SEXO = "sexo";
	public static final String ALUNO_MATRICULADO = "matriculado";
	
	public static final String DATABASE_NAME = "alunoce.db";
	public static final int DATABASE_VERSION = 2;
	
	public static final String CREATE_ALUNO = 
			"create table " + TABLE_NAME + " ( "
							+ ALUNO_ID				+ " integer primary key autoincrement" 
							+ ", "
							+ ALUNO_NOME			+ " text not null" 
							+ ", "
							+ ALUNO_FOTO			+ " text not null" 
							+ ", "
							+ ALUNO_TELEFONE		+ " text not null"
							+ ", "
							+ ALUNO_EMAIL			+ " text not null"
							+ ", "
							+ ALUNO_ENDERECO		+ " text not null"
							+ ", "
							+ ALUNO_CURSO			+ " text not null"
							+ ", "
							+ ALUNO_SEXO			+ " text not null"
							+ ", "
							+ ALUNO_MATRICULADO		+ " text not null"
				+ ");";

	public static final String DROP_ALUNO = "DROP TABLE IF EXISTS " + TABLE_NAME;


	public BaseDao(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(LOG_BD, "Criando banco com sql.");
		Log.i(LOG_BD, CREATE_ALUNO);
		
		db.execSQL(CREATE_ALUNO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(LOG_BD, "Atualizando a versao: " + oldVersion + " para " + 
				newVersion +". Todos os registros serao removidos.");
		
		Log.i(LOG_BD, DROP_ALUNO);
		
		db.execSQL(DROP_ALUNO);
		onCreate(db);
	}

}
