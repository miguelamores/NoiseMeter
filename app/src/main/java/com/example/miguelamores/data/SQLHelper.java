package com.example.miguelamores.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by miguelamores on 7/2/15.
 */
public class SQLHelper extends SQLiteOpenHelper {

    final private static String DBName = "NoiseMeterTable";
    final private static int DBVersion = 1;

    final private static String _ID = "_id";
    final private static String NOMBRE_USUARIO = "nombre";
    final private static String APELLIDO_USUARIO = "apellido";
    final private static String EMAIL = "email";
    final private static String CONTRASENA = "contrasena";

    final private static String _MEDICIONID = "medicion_id";
    final private static String VALORDB = "valor_db";
    final private static String LATITUD = "latitud";
    final private static String LONGITUD = "longitud";
    final private static String HORA = "hora";
    final private static String USUARIOID = "usuario_id";

    final private static String TABLA_USUARIO = "CREATE TABLE usuario(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            NOMBRE_USUARIO + " text, " +
            APELLIDO_USUARIO + " text, " +
            EMAIL + " text," +
            CONTRASENA + " text)";

    final private static String TABLA_MEDICION = "CREATE TABLE medicion(" +
            _MEDICIONID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            VALORDB + " double, " +
            LATITUD + " double, " +
            LONGITUD + " double," +
            HORA + " Date," + USUARIOID + " integer, FOREIGN KEY(" + USUARIOID + ") REFERENCES" + TABLA_USUARIO + "(" + _ID + "))";

    public SQLHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_USUARIO);
        db.execSQL(TABLA_MEDICION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
