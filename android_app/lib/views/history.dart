import 'package:brap_radio/cubits/history_cubit.dart';
import 'package:brap_radio/models/database/history_database_api.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class History extends StatefulWidget {

  @override
  State createState() => _HistoryState();
}

class _HistoryState extends State<History> with SingleTickerProviderStateMixin {
  final _animatedListKey = GlobalKey<AnimatedListState>();
  AnimationController _fadeController;
  final List<MusicDatabaseModel> history = [];

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(vsync: this, duration: const Duration(milliseconds: 600));
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
        title: Text("Historique"),
        ),

        body: BlocConsumer<HistoryCubit, HistoryState>(
          listener: (context, state) {
            if(state is HistoryRefreshed && state.addedMusic != null){
              if(_fadeController.isAnimating || _fadeController.isCompleted)
                _fadeController.reverse();

              history.insert(0, state.addedMusic);
              if(_animatedListKey.currentState != null)
                _animatedListKey.currentState.insertItem(0);

              // delete all item of index > 50
              for(var i = history.length - 1; i > 49; i--){
                final music = history.removeLast();
                if(_animatedListKey.currentState != null){
                  _animatedListKey.currentState.removeItem(i, (context, animation) =>
                      SlideAnimatedHistoryListTile(music, i, animation, key: ObjectKey(music.hashCode)));
                }
              }
            }
          },

          builder: (context, state){
            if(state is HistoryInitial)
              return Center(child: CircularProgressIndicator());

            if(history.length == 0) { // page just showed up, setting  callbacks up
              WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {
                if(state.history.length == 0){
                  _fadeController.forward(from: 0.0);
                } else {
                  history.addAll(state.history);
                  for(var i = 0; i < history.length; i++){
                    await Future.delayed(Duration(milliseconds: 10*i));
                    if(_animatedListKey.currentState != null)
                      _animatedListKey.currentState.insertItem(i);
                  }
                }
              });
            } else if(state is HistoryUpdatedRatio) { // if history length != 0 && state is historyupdatedratio
              history[0] = history[0].copyWithUpdatedRatio(state.newRatio);
            }

            return Stack (
              children: [
                AnimatedList (
                key: _animatedListKey,
                itemBuilder: (context, index, animation) =>
                    SlideAnimatedHistoryListTile(history[index], index, animation,
                        key: ObjectKey(history[index].hashCode)),
              ),
                Center (
                  child: FadeTransition (
                      opacity: _fadeController,
                      child: Padding (
                          padding: EdgeInsets.all(10),
                          child: Text (
                            "Il semblerait qu'il n'y ait rien à voir ici...\n"
                                "Essayez d'activer la récupération automatique "
                                "de l'historique, ou attendre qu'une musique ait"
                                " fini de se jouer !",
                            style: Theme.of(context).textTheme.subtitle1,
                            textAlign: TextAlign.center,
                          )
                      )
                  )
                ),
              ],
            );
          },

          buildWhen: (previous, current){
            return !(previous is HistoryRefreshed && current is HistoryRefreshed);
          }
        )
    );
  }
}

class SlideAnimatedHistoryListTile extends StatelessWidget {
  const SlideAnimatedHistoryListTile
      (this.music, this.index, this.animation, {Key key}) : super(key: key);

  final MusicDatabaseModel music;
  final int index;
  final Animation<double> animation;

  @override
  Widget build(BuildContext context) {
    return SlideTransition ( // todo: make insertion smoother
        position: Tween<Offset>(
          begin: const Offset(2.0, 0.0),
          end: Offset.zero,
        ).animate(CurvedAnimation(parent: animation, curve: Curves.fastOutSlowIn)),

      child: HistoryListTile(music, index),
    );
  }
}


class HistoryListTile extends StatelessWidget {
  const HistoryListTile(this.music, this.index);

  final MusicDatabaseModel music;
  final int index;

  @override
  Widget build(BuildContext context) {
    return CustomListTile (
      title: music.title,
      subtitle: music.authors,
      leadingWidget: Hero (
          tag: "music#$index",
          child: ClipRRect (
              borderRadius: BorderRadius.circular(10),
              child: Image.network(music.imageUrl)
          )
      ),
      trailingIcon: Icons.keyboard_arrow_right,
      onTap: () => Navigator.push(context,
          MaterialPageRoute(builder: (_) => HistoryDetails(music, "music#$index"))),
    );
  }
}

class HistoryDetails extends StatelessWidget {
  final MusicDatabaseModel _music;
  final String _heroTag;

  HistoryDetails(this._music, this._heroTag);

  @override
  Widget build(BuildContext context) {
    return Scaffold (
        appBar: AppBar (
          title: Text("Historique - Détails"),
        ),

        body: Column (
          children: [
            Flexible (
                flex: 3,
                child: Container (
                  padding: EdgeInsets.all(20),
                  child: Hero (
                    tag: _heroTag,
                    child: ClipRRect (
                      borderRadius: BorderRadius.circular(10),
                      child: Image.network(_music.imageUrl),
                    ),
                  ),
                )
            ),

            Flexible (
                child: Center (
                    child: Text(_music.title, style:Theme.of(context).textTheme.headline1)
                )
            ),

            Flexible (
                flex: 5,
                child: Container (
                    margin: EdgeInsets.all(20),
                    child : Column (
                        children: [
                          Padding (
                              padding: EdgeInsets.only(top: 15, bottom: 15),
                              child: Row (
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Column (
                                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text("Auteurs", style:Theme.of(context).textTheme.headline2),
                                          Text(_music.authors, style:Theme.of(context).textTheme.bodyText1),
                                        ]
                                    )
                                  ]
                              )
                          ),

                          Padding (
                              padding: EdgeInsets.only(top: 15, bottom: 15),
                              child: Row (
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Column (
                                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text("Album", style:Theme.of(context).textTheme.headline2),
                                          Text(_music.albumName, style:Theme.of(context).textTheme.bodyText1),
                                        ]
                                    )
                                  ]
                              )
                          ),

                          Padding (
                              padding: EdgeInsets.only(top: 15, bottom: 15),
                              child: Row (
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Column (
                                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text("Taux de vote positif", style:Theme.of(context).textTheme.headline2),
                                          Text(
                                              _music.positiveVoteRatio == -1 ?
                                              "Non stable (lecture en cours)" :
                                              "${(_music.positiveVoteRatio * 100).toStringAsFixed(1)}%",

                                              style:Theme.of(context).textTheme.bodyText1
                                          ),
                                        ]
                                    )
                                  ]
                              )
                          ),
                        ]
                    )
                )
            )
          ],
        )
    );
  }
}