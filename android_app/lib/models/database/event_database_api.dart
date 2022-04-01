import 'package:brap_radio/models/database/database_api.dart';
import 'package:brap_radio/models/request/event_entity.dart';
import 'package:brap_radio/models/request/request.dart';
import 'package:equatable/equatable.dart';
import 'package:sqflite/sqflite.dart';

class EventDatabaseModel extends Equatable {
  final int id;
  final String title, description;
  final DateTime startAt, endAt;

  const EventDatabaseModel (
      this.id, this.title, this.description, this.startAt, this.endAt);

  factory EventDatabaseModel.fromEventEntity(EventEntity eventEntity) {
    return EventDatabaseModel(-1, eventEntity.title, eventEntity.description,
        eventEntity.startAt, eventEntity.endAt);
  }

  Map<String, dynamic> toMap() => {
    EventDatabaseAPI._ID: id == -1 ? null : id,
    EventDatabaseAPI._TITLE: title,
    EventDatabaseAPI._DESCRIPTION: description,
    EventDatabaseAPI._START_AT: Request.format.format(startAt),
    EventDatabaseAPI._END_AT: Request.format.format(endAt),
  };

  @override
  List<Object> get props => [title, description, startAt, endAt];
}


class EventDatabaseAPI {

  static const _TABLE_NAME = "events";

  static const _ID = "id";
  static const _TITLE = "title";
  static const _DESCRIPTION = "description";
  static const _START_AT = "start_at";
  static const _END_AT = "end_at";

  static final EventDatabaseAPI _instance = EventDatabaseAPI._ctor();

  factory EventDatabaseAPI() => _instance;

  EventDatabaseAPI._ctor();

  Future<void> add(EventDatabaseModel eventToAdd) async {
    final Database db = await DatabaseAPI().database;

    await db.insert(_TABLE_NAME, eventToAdd.toMap());
  }

  Future<void> delete(EventDatabaseModel eventToDelete) async {
    final Database db = await DatabaseAPI().database;
    final count = await db.delete(_TABLE_NAME, where: "$_TITLE = ? AND $_DESCRIPTION = ? AND $_START_AT = ? AND $_END_AT = ?",
        whereArgs: [eventToDelete.title, eventToDelete.description,
          Request.format.format(eventToDelete.startAt), Request.format.format(eventToDelete.endAt)]);
    print("[DB] rows affected : $count");
  }

  Future<void> resetDatabase() async {
    final Database db = await DatabaseAPI().database;

    await db.execute("DELETE FROM $_TABLE_NAME");
    await db.delete("sqlite_sequence", where: "name = ?", whereArgs: [_TABLE_NAME]);
  }

  Future<int> updateEvent(EventDatabaseModel targetedEvent, EventDatabaseModel modifyTo) async {
    final Database db = await DatabaseAPI().database;

    return await db.update(_TABLE_NAME, modifyTo.toMap(),
        where: "$_TITLE = ? AND $_DESCRIPTION = ? AND $_START_AT = ? AND $_END_AT = ?",
        whereArgs: [targetedEvent.title, targetedEvent.description,
          Request.format.format(targetedEvent.startAt), Request.format.format(targetedEvent.endAt)]);
  }

  Future<List<EventDatabaseModel>> getEvents() async {
    final Database db = await DatabaseAPI().database;

    List<Map<String, dynamic>> maps = await db.query(_TABLE_NAME);

    return List.generate(maps.length, (index) {
      Map<String, dynamic> currentMap = maps[index];
      return EventDatabaseModel(currentMap[_ID], currentMap[_TITLE],
          currentMap[_DESCRIPTION], Request.format.parse(currentMap[_START_AT]), Request.format.parse(currentMap[_END_AT]));
    });
  }
}