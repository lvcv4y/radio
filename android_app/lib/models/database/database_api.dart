import 'package:brap_radio/models/service_api.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseAPI {

  static final DatabaseAPI _instance = DatabaseAPI._ctor();
  Future<Database> database;

  factory DatabaseAPI() {
    return _instance;
  }

  DatabaseAPI._ctor();

  Future<Null> init() async {
    database = openDatabase (
        join(await getDatabasesPath(), "brap.db"),
        onCreate: (db, version) async {
          await db.execute (
              "CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                  "title VARCHAR, authors VARCHAR, album_name VARCHAR, image_url VARCHAR,"
                  " positive_vote_ratio DOUBLE, spotify_id VARCHAR DEFAULT \"null\", "
                  "deezer_id VARCHAR DEFAULT \"null\")"
          );

          await db.execute (
              "CREATE TABLE events (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                  "title VARCHAR, description TEXT, start_at DATETIME, end_at DATETIME)"
          );
        },
        version: 1
    );

    await database;
    ServiceApi.historyCubit.initHistory();
    ServiceApi.eventCubit.initEvents();

  }
}