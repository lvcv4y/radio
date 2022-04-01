import 'package:bloc/bloc.dart';
import 'package:brap_radio/models/database/event_database_api.dart';
import 'package:equatable/equatable.dart';

part 'events_state.dart';

class EventsCubit extends Cubit<EventsState> {
  EventsCubit() : super(EventsInitial());

  final EventDatabaseAPI databaseAPI = EventDatabaseAPI();
  List<EventDatabaseModel> events = [];

  Future<void> initEvents() async {
    events = await databaseAPI.getEvents() ?? [];
    emit(events.length > 0 ? EventsRefreshed(events, null, null) : EventsReset());
  }

  void add(EventDatabaseModel event) {
    if(!events.contains(event)){
      databaseAPI.add(event);
      events.add(event);
      emit(EventsRefreshed(events, event, null));
    }
  }

  void delete(EventDatabaseModel event) {
    if(events.contains(event)){
      databaseAPI.delete(event);
      emit(EventsRefreshed(events, null, event));
    }
  }

  void modify(EventDatabaseModel eventToModify, EventDatabaseModel modifiedEvent) async {
    final count = await databaseAPI.updateEvent(eventToModify, modifiedEvent);
    if(count == 0){ // no update, means that event not in db, so we need to add it
      add(modifiedEvent);
    } else {
      emit(EventsModified(events, eventToModify, modifiedEvent));
    }
  }

  void reset(){
    databaseAPI.resetDatabase();
    events = [];
    emit(EventsReset());
  }
}
