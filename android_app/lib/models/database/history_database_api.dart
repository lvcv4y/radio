import 'package:brap_radio/models/database/database_api.dart';
import 'package:brap_radio/models/request/music_entity.dart';
import 'package:equatable/equatable.dart';
import 'package:sqflite/sqflite.dart';

class MusicDatabaseModel extends Equatable {
  final int id;
  final String title, authors, imageUrl, albumName;
  final double positiveVoteRatio;

  MusicDatabaseModel(this.id, this.title, this.authors, this.albumName,
      this.imageUrl, this.positiveVoteRatio);

  factory MusicDatabaseModel.fromMusicEntity(MusicEntity musicEntity) {
    return MusicDatabaseModel(-1, musicEntity.title, musicEntity.authors, musicEntity.albumName,
        musicEntity.albumImageUrl, musicEntity.positiveVoteNumber / musicEntity.totalVoteNumber);
  }

  MusicDatabaseModel copyWithUpdatedRatio(double newRatio){
    return MusicDatabaseModel(id, title, authors, albumName, imageUrl, newRatio);
  }

  Map<String, dynamic> toMap() => {
    if(id != -1) HistoryDatabaseAPI._ID: id,
    HistoryDatabaseAPI._TITLE: title,
    HistoryDatabaseAPI._AUTHORS: authors,
    HistoryDatabaseAPI._ALBUM_NAME: albumName,
    HistoryDatabaseAPI._IMAGE_URL: imageUrl,
    HistoryDatabaseAPI._POSITIVE_VOTE_RATIO: positiveVoteRatio,
  };

  @override
  List<Object> get props => [id, title, authors, imageUrl, albumName, positiveVoteRatio];
}

class HistoryDatabaseAPI {

  static const HISTORY_SIZE = 35;

  static const _TABLE_NAME = 'history';
  static const _ID = "id";
  static const _TITLE = "title";
  static const _AUTHORS = "authors";
  static const _IMAGE_URL = "image_url";
  static const _ALBUM_NAME = "album_name";
  static const _POSITIVE_VOTE_RATIO = "positive_vote_ratio";


  static final HistoryDatabaseAPI _instance = HistoryDatabaseAPI._ctor();

  factory HistoryDatabaseAPI() {
    return _instance;
  }

  HistoryDatabaseAPI._ctor();

  Future<void> add(MusicDatabaseModel music) async {
    final Database db = await DatabaseAPI().database;

    await db.insert(_TABLE_NAME, music.toMap());
    int count = Sqflite.firstIntValue(
        await db.rawQuery("SELECT COUNT(*) FROM $_TABLE_NAME"));

    if (count > HISTORY_SIZE) {
      List<Map<String, dynamic>> mappedMusics = await db.query(
          _TABLE_NAME, limit: count - HISTORY_SIZE);

      mappedMusics.forEach((element) {
        _delete(element["id"]);
      });
    }
  }

  Future<void> updateLastElementVoteRatio(double ratio) async {
    final Database db = await DatabaseAPI().database;
    int lastId = (
        await db.query(_TABLE_NAME, columns: [_ID], orderBy: "$_ID DESC", limit: 1)
    )[0][_ID];

    db.update(_TABLE_NAME, {_POSITIVE_VOTE_RATIO : ratio},
        where: "$_ID = ?",
        whereArgs: [lastId],
    );
  }

  Future<void> delete(MusicDatabaseModel musicToDelete) async { // todo use unique id ?
    final Database db = await DatabaseAPI().database;
    await db.delete(_TABLE_NAME, where: "$_TITLE = ? AND $_AUTHORS = ?",
        whereArgs: [musicToDelete.title, musicToDelete.authors]);
  }

  Future<void> _delete(int id) async {
    final Database db = await DatabaseAPI().database;
    await db.delete(_TABLE_NAME, where: "$_ID = ?", whereArgs: [id]);
  }

  Future<List<MusicDatabaseModel>> getHistory() async {
    final Database db = await DatabaseAPI().database;

    List<Map<String, dynamic>> maps = await db.query(_TABLE_NAME);

    return List.generate(maps.length, (index){
      Map<String, dynamic> currentMap = maps[index];
      return MusicDatabaseModel(currentMap[_ID], currentMap[_TITLE], currentMap[_AUTHORS],
          currentMap[_ALBUM_NAME], currentMap[_IMAGE_URL], currentMap[_POSITIVE_VOTE_RATIO]);
    });
  }

  Future<void> resetDatabase() async {
    final Database db = await DatabaseAPI().database;
    await db.execute("DELETE FROM $_TABLE_NAME");
    await db.delete("sqlite_sequence", where: "name = ?", whereArgs: [_TABLE_NAME]);
  }
}