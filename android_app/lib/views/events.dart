import 'package:brap_radio/cubits/event_cubit.dart';
import 'package:brap_radio/models/database/event_database_api.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

const Map<int, String> daysConverter = {
  DateTime.monday : "Lundi",
  DateTime.tuesday : "Mardi",
  DateTime.wednesday : "Mercredi",
  DateTime.thursday : "Jeudi",
  DateTime.friday : "Vendredi",
  DateTime.saturday : "Samedi",
  DateTime.sunday : "Dimanche",
};

const Map<int, String> monthsConverter = {
  DateTime.january: "Janvier",
  DateTime.february: "Février",
  DateTime.march: "Mars",
  DateTime.april: "Avril",
  DateTime.may: "Mai",
  DateTime.june: "Juin",
  DateTime.july: "Juillet",
  DateTime.august: "Août",
  DateTime.september: "Septembre",
  DateTime.october: "Octobre",
  DateTime.november: "Novembre",
  DateTime.december: "Décembre"
};

String _getStringFromDate(final DateTime date) => "${daysConverter[date.weekday]} ${date.day}"
    " ${monthsConverter[date.month]} à ${date.hour}h${date.minute < 10 ? "0" : ''}${date.minute}";



class EventView extends StatefulWidget {

  @override
  State createState() => _EventViewState();
}

class _EventViewState extends State<EventView> with SingleTickerProviderStateMixin {

  final _animatedListKey = GlobalKey<AnimatedListState>();
  AnimationController _fadeController;
  final List<EventDatabaseModel> events = [];

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(vsync: this, duration: const Duration(milliseconds: 400));
  }

  @override
  void dispose() {
    _fadeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold (
      appBar: AppBar (
        title: Text("Évènements de la semaine"),
      ),


      body: BlocConsumer<EventsCubit, EventsState>(
          listener: (context, state) {
            print("listener triggered by a state");
            if(state is EventsReset) {
              print("events reset spotted");

              for(var i = events.length - 1; i >= 0; i){
                final event = events.removeAt(i);
                if(_animatedListKey.currentState != null) {
                  _animatedListKey.currentState.removeItem(i, (context, animation) =>
                      SlideAnimatedEventListTile(event, i, animation));
                }
              }

              _fadeController.forward();

            } else if(state is EventsRefreshed) {
              if(state.addedEvent != null) { // an event has been added
                final index = events.length;
                events.add(state.addedEvent);
                if(_fadeController.isAnimating || _fadeController.isCompleted)
                  _fadeController.reverse();
                if(_animatedListKey.currentState != null)
                  _animatedListKey.currentState.insertItem(index);
              }

              if(state.deletedEvent != null) { // an event has been deleted
                final index = events.indexOf(state.deletedEvent);
                print("[LISTENER] index variable : $index");
                if(index != -1) { // the event is actually in the list
                  final event = events.removeAt(index);
                  if(_animatedListKey.currentState != null)
                    _animatedListKey.currentState.removeItem(index, (context, animation) =>
                        AnimatedOutEventListTile(event, index, animation, key: ObjectKey(event.hashCode)),
                        duration: const Duration(milliseconds: 400));
                  print("[LISTENER] event's list : $events");
                }
              }

              if(events.length == 0){
                _fadeController.forward(from:0.0);
              }
            } else if(state is EventsModified) {
              // todo implements when existing (always existing ?) event modified
            }
          },

          builder: (context, state) {
            if(state is EventsInitial){
              return Center(child: CircularProgressIndicator());
            } else if(state is EventsRefreshed && events.length == 0) { // page just loaded, set callback up
              WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {

                if(state.events.length == 0){
                  _fadeController.forward(from: 0.0);
                } else {
                  events.addAll(state.events);
                  for(var i = 0; i < events.length; i++){
                    await Future.delayed(const Duration(milliseconds: 100));
                    _animatedListKey.currentState.insertItem(i);
                  }
                }

              });
            } else if(state is EventsReset){
              WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {
                await Future.delayed(const Duration(milliseconds: 200));
                _fadeController.forward(from: 0.0);
              });
            }

            // State is EventsRefreshed, EventsReset or EventsModified
            // building if first load, so listener can modify it if any modification are received

            return Stack (
                children: [
                  AnimatedList (
                    key: _animatedListKey,
                    itemBuilder: (context, index, animation) =>
                        SlideAnimatedEventListTile(events[index], index, animation, key: ObjectKey(events[index].hashCode)),
                  ),
                  Center (
                      child: FadeTransition (
                          opacity: _fadeController,
                          child: Padding (
                              padding: EdgeInsets.all(10),
                              child: Text (
                                "Il semblerait qu'il n'y ait pas encore d'évènements prévus cette semaine...",
                                style: Theme.of(context).textTheme.subtitle1, textAlign: TextAlign.center,
                              )
                          )
                      )
                  ),
                ]
            );
          },

        buildWhen: (previous, current) =>
        !((previous is EventsReset || previous is EventsRefreshed) && (current is EventsReset || current is EventsRefreshed)),
      ),
    );
  }
}

class SlideAnimatedEventListTile extends StatelessWidget {
  final EventDatabaseModel event;
  final int index;
  final Animation<double> animation;

  const SlideAnimatedEventListTile(this.event, this.index, this.animation, {Key key}): super(key: key);

  @override
  Widget build(BuildContext context) {
    return SlideTransition (
        position: Tween<Offset>(
          begin: const Offset(2.0, 0.0),
          end: Offset.zero,
        ).animate(CurvedAnimation(parent: animation, curve: Curves.fastOutSlowIn)),


        child: EventListTile(event, index)
    );
  }
}

class AnimatedOutEventListTile extends StatelessWidget {
  final EventDatabaseModel event;
  final int index;
  final Animation<double> animation;

  const AnimatedOutEventListTile(this.event, this.index, this.animation, {Key key}): super(key: key);

  @override
  Widget build(BuildContext context) {
    return SizeTransition(
        sizeFactor: animation,
        child: EventListTile(event, index)
    );
  }
}

class EventListTile extends StatelessWidget {
  final EventDatabaseModel event;
  final int index;
  const EventListTile(this.event, this.index);

  @override
  Widget build(BuildContext context) {
    return CustomListTile (
      title: event.title,
      subtitle: getSubtitle(event.startAt, event.endAt),
      leadingWidget: Hero (
          tag: "event#$index",
          child: ClipOval(child: Image.asset("images/logo.png"))
      ),
      trailingIcon: Icons.keyboard_arrow_right,
      onTap: () => Navigator.push(context,
          MaterialPageRoute(builder: (_) => EventDetails(event, "event#$index"))),
    );
  }

  String getSubtitle(final DateTime start, final DateTime end){
    if(start.weekday == end.weekday)
      return "${daysConverter[start.weekday]} de ${start.hour}h à ${end.hour}h";

    return "de ${daysConverter[start.weekday]} à ${start.hour}h jusqu'à ${daysConverter[end.weekday]} à ${end.hour}h";
  }

}


class EventDetails extends StatelessWidget {
  final EventDatabaseModel _event;
  final String _heroTag;

  EventDetails(this._event, this._heroTag);


  @override
  Widget build(BuildContext context) {
    return Scaffold (
      appBar: AppBar (
        title: Text("Évènement - Détails")
      ),

      body: Column (
        children: [
          Flexible (
              flex: 3,
              child: Container (
                padding: EdgeInsets.all(20),
                child: Hero (
                  tag: _heroTag,
                  child: ClipOval (
                    child: Image.asset("images/logo.png"),
                  ),
                ),
              )
          ),

          Flexible (
              child: Center (
                  child: Text(_event.title, style:Theme.of(context).textTheme.headline1)
              )
          ),

          Flexible (
            flex: 5,
            child: ListView (
              children: [
                Divider(thickness: 1),
                Container (
                    margin: EdgeInsets.all(10),
                    padding: EdgeInsets.all(10),
                    child: Column (
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Text("Description", style: Theme.of(context).textTheme.headline3),
                        SizedBox(height: 5,),
                        Text(_event.description, style: Theme.of(context).textTheme.bodyText2),
                      ],
                    )
                ),
                Divider(thickness: 1),
                Container (
                    margin: EdgeInsets.all(10),
                    padding: EdgeInsets.all(10),
                    child: Column (
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Text("Commence le", style: Theme.of(context).textTheme.headline3),
                        SizedBox(height: 5,),
                        Text(_getStringFromDate(_event.startAt), style: Theme.of(context).textTheme.bodyText2),
                      ],
                    )
                ),
                Divider(thickness: 1),
                Container (
                    margin: EdgeInsets.all(10),
                    padding: EdgeInsets.all(10),
                    child: Column (
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        Text("Fini le", style: Theme.of(context).textTheme.headline3),
                        SizedBox(height: 5),
                        Text(_getStringFromDate(_event.endAt), style: Theme.of(context).textTheme.bodyText2),
                      ],
                    )
                ),
              ],
            )
          )
        ]
      ),
    );
  }
}