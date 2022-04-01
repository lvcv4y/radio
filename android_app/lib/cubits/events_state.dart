part of 'event_cubit.dart';

abstract class EventsState extends Equatable {
  const EventsState();
}

class EventsInitial extends EventsState {
  @override
  List<Object> get props => [];
}

class EventsRefreshed extends EventsState {
  const EventsRefreshed(this.events, this.addedEvent, this.deletedEvent);

  final List<EventDatabaseModel> events;
  final EventDatabaseModel addedEvent;
  final EventDatabaseModel deletedEvent;

  @override
  List<Object> get props => [events, addedEvent, deletedEvent];
}

class EventsReset extends EventsState {
  @override
  List<Object> get props => [];
}

class EventsModified extends EventsState {

  const EventsModified(this.events, this.targetedEvent, this.modifiedEvent);

  final List<EventDatabaseModel> events;
  final EventDatabaseModel targetedEvent;
  final EventDatabaseModel modifiedEvent;

  @override
  List<Object> get props => [events, targetedEvent, modifiedEvent];
}
