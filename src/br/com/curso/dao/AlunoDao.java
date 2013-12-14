package br.com.curso.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import br.com.curso.model.Aluno;
import br.com.curso.utils.Constantes;
import br.com.curso.utils.FileUtils;

/**
 * 
 * Classe DAO que encapsula a persistencia no Banco
 * SQLite
 * 
 * @author Gabriel Tavares
 *
 */
public class AlunoDao {

	private SQLiteDatabase database;
	private BaseDao baseDao;
	
	private String[] columns = {
			BaseDao.ALUNO_ID,
			BaseDao.ALUNO_NOME,
			BaseDao.ALUNO_FOTO,
			BaseDao.ALUNO_TELEFONE,
			BaseDao.ALUNO_EMAIL,
			BaseDao.ALUNO_ENDERECO,
			BaseDao.ALUNO_CURSO,
			BaseDao.ALUNO_SEXO,
			BaseDao.ALUNO_MATRICULADO
	};
	
	public AlunoDao(Context context) {
		baseDao = new BaseDao(context);
	}	
	
	public void open() throws SQLException {
		database = baseDao.getWritableDatabase();
	}	
	
	public void close() {
		baseDao.close();
	}
		
	public long salvarOuAlterar(Aluno aluno){
		long id = aluno.getId();
		
		if(id == 0){
			id = salvar(aluno);
		} else{
			id = atualizar(aluno);
		}
		
		return id;
	}

	/**
	 * Insere uma entidade no banco de dados.
	 * 
	 * SQL: insert into aluno values...
	 * 
	 * @param aluno
	 * @return ID
	 */
	public long salvar(Aluno aluno) {
		String imgSalva = salvaImagem(aluno);
		
		ContentValues values = new ContentValues();
		
		values.put(BaseDao.ALUNO_NOME, aluno.getNome());
		values.put(BaseDao.ALUNO_FOTO, imgSalva);
		values.put(BaseDao.ALUNO_TELEFONE, aluno.getTelefone());
		values.put(BaseDao.ALUNO_EMAIL, aluno.getEmail());
		values.put(BaseDao.ALUNO_ENDERECO, aluno.getEndereco());
		values.put(BaseDao.ALUNO_CURSO, aluno.getCurso());
		values.put(BaseDao.ALUNO_SEXO, aluno.getSexo());
		values.put(BaseDao.ALUNO_MATRICULADO, String.valueOf(aluno.isMatriculado()));

		return salvar(values);
	}

	/**
	 * Atualiza a entidade no banco. o id da entidade eh utilizado.
	 * 
	 * SQL: update aluno set nome = ?, telefone = ?, ... where id = ID_ALUNO; 
	 * 
	 * @param aluno
	 * @return cont
	 */
	public int atualizar(Aluno aluno){
		String imgSalva = salvaImagem(aluno);		
		
		ContentValues values = new ContentValues();
		
		values.put(BaseDao.ALUNO_ID, aluno.getId());
		values.put(BaseDao.ALUNO_NOME, aluno.getNome());
		values.put(BaseDao.ALUNO_FOTO, imgSalva);
		values.put(BaseDao.ALUNO_TELEFONE, aluno.getTelefone());
		values.put(BaseDao.ALUNO_EMAIL, aluno.getEmail());
		values.put(BaseDao.ALUNO_ENDERECO, aluno.getEndereco());
		values.put(BaseDao.ALUNO_CURSO, aluno.getCurso());
		values.put(BaseDao.ALUNO_SEXO, aluno.getSexo());
		values.put(BaseDao.ALUNO_MATRICULADO, String.valueOf(aluno.isMatriculado()));
		
		String id = String.valueOf(aluno.getId());
		String where = BaseDao.ALUNO_ID + "=?";
		String[] whereArgs = new String[] { id };

		int cont = atualizar(values, where, whereArgs);
		return cont;
	}
	
	/**
	 * Deleta a entidade com o id fornecido.
	 * 
	 * SQL: delete from aluno where id = ID;
	 * 
	 * @param id
	 * @return
	 */
	public int deletar(Aluno aluno) {
		deletaImagem(aluno);
		
		String where = BaseDao.ALUNO_ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(aluno.getId()) };

		int cont = deletar(where, whereArgs);
		return cont;
	}
	
	/**
	 * Faz uma consulta pelo id do aluno
	 * select * from aluno where id = ID_ALUNO;
	 * @param id
	 * 
	 * @return Aluno
	 */
	public Aluno buscar(long id) {
		Cursor c = database.query(BaseDao.TABLE_NAME, columns, 
				BaseDao.ALUNO_ID + " = " + id, null, null, null, null);
		
		Aluno aluno = null;
		
		String fotoPath = "";
		
		if(c.getCount() > 0){
			aluno = new Aluno();
			c.moveToFirst();
			
			aluno.setId(c.getInt(c.getColumnIndex(BaseDao.ALUNO_ID)));
			aluno.setNome(c.getString(c.getColumnIndex(BaseDao.ALUNO_NOME)));
			aluno.setTelefone(c.getString(c.getColumnIndex(BaseDao.ALUNO_TELEFONE)));
			aluno.setEmail(c.getString(c.getColumnIndex(BaseDao.ALUNO_EMAIL)));
			aluno.setEndereco(c.getString(c.getColumnIndex(BaseDao.ALUNO_ENDERECO)));
			aluno.setCurso(c.getString(c.getColumnIndex(BaseDao.ALUNO_CURSO)));
			aluno.setSexo(c.getString(c.getColumnIndex(BaseDao.ALUNO_SEXO)));
			aluno.setMatriculado(c.getString(c.getColumnIndex(BaseDao.ALUNO_MATRICULADO)).equals("true") ? true : false);
			
			fotoPath = c.getString(c.getColumnIndex(BaseDao.ALUNO_FOTO));
			
			c.close();

			// Carrega a imagem
			if(fotoPath != null && !fotoPath.equals("")){
				File arquivo = new File(fotoPath);
				aluno.setFoto(FileUtils.lerImagemBitmap(arquivo));
			}
		}
				
		return aluno;
	}

	public Aluno buscar(String nome) {
		Cursor c = database.query(BaseDao.TABLE_NAME, columns, 
				BaseDao.ALUNO_NOME + " = ?", new String[]{ nome }, null, null, null);
		
		Aluno aluno = null;
		
		String fotoPath = "";
		
		if(c.getCount() > 0){
			aluno = new Aluno();
			c.moveToFirst();
			
			aluno.setId(c.getInt(c.getColumnIndex(BaseDao.ALUNO_ID)));
			aluno.setNome(c.getString(c.getColumnIndex(BaseDao.ALUNO_NOME)));
			aluno.setTelefone(c.getString(c.getColumnIndex(BaseDao.ALUNO_TELEFONE)));
			aluno.setEmail(c.getString(c.getColumnIndex(BaseDao.ALUNO_EMAIL)));
			aluno.setEndereco(c.getString(c.getColumnIndex(BaseDao.ALUNO_ENDERECO)));
			aluno.setCurso(c.getString(c.getColumnIndex(BaseDao.ALUNO_CURSO)));
			aluno.setSexo(c.getString(c.getColumnIndex(BaseDao.ALUNO_SEXO)));
			aluno.setMatriculado(c.getString(c.getColumnIndex(BaseDao.ALUNO_MATRICULADO)).equals("true") ? true : false);
			
			fotoPath = c.getString(c.getColumnIndex(BaseDao.ALUNO_FOTO));
			
			c.close();

			// Carrega a imagem
			if(fotoPath != null && !fotoPath.equals("")){
				File arquivo = new File(fotoPath);
				aluno.setFoto(FileUtils.lerImagemBitmap(arquivo));
			}
		}
				
		return aluno;
	}

	public List<Aluno> listar() {
		Cursor c = database.query(BaseDao.TABLE_NAME, columns, null, null, null, null, null, null);
		
		List<Aluno> alunos = null;
		Aluno aluno = null;
		
		String fotoPath = "";
		
		if(c != null){
			alunos = new ArrayList<Aluno>();
			
			if(c.moveToFirst()){
				do{
					aluno = new Aluno();
					
					aluno.setId(c.getInt(c.getColumnIndex(BaseDao.ALUNO_ID)));
					aluno.setNome(c.getString(c.getColumnIndex(BaseDao.ALUNO_NOME)));
					aluno.setTelefone(c.getString(c.getColumnIndex(BaseDao.ALUNO_TELEFONE)));
					aluno.setEmail(c.getString(c.getColumnIndex(BaseDao.ALUNO_EMAIL)));
					aluno.setEndereco(c.getString(c.getColumnIndex(BaseDao.ALUNO_ENDERECO)));
					aluno.setCurso(c.getString(c.getColumnIndex(BaseDao.ALUNO_CURSO)));
					aluno.setSexo(c.getString(c.getColumnIndex(BaseDao.ALUNO_SEXO)));
					aluno.setMatriculado(c.getString(c.getColumnIndex(BaseDao.ALUNO_MATRICULADO)).equals("true") ? true : false);
					
					fotoPath = c.getString(c.getColumnIndex(BaseDao.ALUNO_FOTO));
					
					// Carrega a imagem
					if(fotoPath != null && !fotoPath.equals("")){
						File arquivo = new File(fotoPath);
						aluno.setFoto(FileUtils.lerImagemBitmap(arquivo));
					}
					
					alunos.add(aluno);
					
				} while(c.moveToNext());
				
				c.close();
			}
		}
		
		return alunos;
	}

	
	private long salvar(ContentValues values) {
		long id = database.insert(BaseDao.TABLE_NAME, null, values);
		Log.i(BaseDao.LOG_BD, "Salvou o registro [" + id + "].");
		
		return id;
	}

	private int atualizar(ContentValues values, String where, String[] whereArgs) {
		int cont = database.update(BaseDao.TABLE_NAME, values, where, whereArgs);
		Log.i(BaseDao.LOG_BD, "Atualizou [" + cont + "] registros.");
				
		return cont;
	}
	
	private int deletar(String where, String[] whereArgs) {
		int cont = database.delete(BaseDao.TABLE_NAME, where, whereArgs);
		Log.i(BaseDao.LOG_BD, "Deletou [" + cont + "] registros.");
				
		return cont;
	}

	private String salvaImagem(Aluno aluno) {
		File fileImg = deletaImagem(aluno);
		boolean isSucesso = FileUtils.salvaImagemBitmap(aluno.getFoto(), fileImg);
		
		if(isSucesso){
			return fileImg.getAbsolutePath();
		}
		
		return "";
	}

	private File deletaImagem(Aluno aluno) {
		String nomeArquivo = aluno.getEmail().replace(".", "");
		File fileImg = new File(Constantes.DIR_IMAGENS, nomeArquivo + ".jpg");
		
		if(fileImg.exists()){
			fileImg.delete();
		}
		
		return fileImg;
	}
	
}
