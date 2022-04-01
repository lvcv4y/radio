import 'package:brap_radio/cubits/mute_cubit.dart';
import 'package:brap_radio/cubits/playing_cubit.dart';
import 'package:brap_radio/cubits/playing_progress_cubit.dart';
import 'package:brap_radio/cubits/vote_cubit.dart';
import 'package:brap_radio/cubits/vote_progress_cubit.dart';
import 'package:brap_radio/models/request/music_entity.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/material.dart';
import 'package:coast/coast.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

String getStringFromPlayingState(PlayingState state, String disconnected, String connecting, String nowPlaying){
  if(state is PlayingConnecting) {
    return connecting;
  }

  if(state is PlayingNow) {
    return nowPlaying == "TITLE" ? state.music.title : state.music.authors;
  }

  // is either error, initial or disconnect state, same value (because same result : user is not connected) -> disconnected
  return disconnected;
}

class PlayerPage extends StatefulWidget {
  @override
  _PlayerPageState createState() => _PlayerPageState();
}

class _PlayerPageState extends State<PlayerPage> {

  Color backgroundPositiveVoteButton;
  Color backgroundNegativeVoteButton;
  Color iconColorPositiveVoteButton;
  Color iconColorNegativeVoteButton;

  int globalValue;
  int totalSeconds = 100;

  @override
  Widget build(BuildContext context) {

    if(backgroundPositiveVoteButton == null){
      backgroundNegativeVoteButton = Theme.of(context).scaffoldBackgroundColor;
      backgroundPositiveVoteButton = Theme.of(context).scaffoldBackgroundColor;
      iconColorNegativeVoteButton = Colors.red;
      iconColorPositiveVoteButton = Colors.green;
    }

    Column topContainerChild;

    final titleText = PlayingCubitTextCrab (
        textBuilder: (context, state) => Text(getStringFromPlayingState(state, "Déconnecté", "Connection...", "TITLE"),
            style: Theme.of(context).textTheme.headline5, textAlign: TextAlign.center),
        crabTag: 'player_title_text',
    );

    final authorsText = PlayingCubitTextCrab (
        textBuilder: (context, state) => Text(getStringFromPlayingState(state, "Vous êtes déconnecté(e) de la radio", "Connection en cours", "AUTHORS"),
            style: Theme.of(context).textTheme.subtitle2, textAlign: TextAlign.center),
        crabTag: "player_authors_text",
    );

    if(MediaQuery.of(context).orientation == Orientation.portrait){
      topContainerChild = Column (
          children: [
            Expanded (
                flex: 3,
                child: Padding (
                    padding: EdgeInsets.all(20),
                    child: PlayingCubitImageCrab()
                )
            ),

            Expanded (
                flex: 1,
                child: Column (
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      titleText,
                      authorsText
                    ]
                )
            ),
          ]
      );
    } else {
      topContainerChild = Column (
          children: [
            Expanded (
              child: Row (
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children : [
                    Padding (
                        padding: EdgeInsets.only(top: 7.5),
                        child: PlayingCubitImageCrab()
                    ),

                    Padding (
                        padding: EdgeInsets.only(top: 7.5),
                        child: Column (
                            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                            children: [
                              titleText,
                              authorsText
                            ]
                        )
                    )
                  ]
              ),
            ),

          ]
      );
    }

    topContainerChild.children.add (
        Expanded (
            flex: 1,
            child: Row (
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Flexible (
                  child: BlocBuilder<PlayingProgressCubit, double>(
                    builder: (context, value) {

                      final state = BlocProvider.of<PlayingCubit>(context).state;
                      String text;

                      if(state is PlayingNow){
                        text = getStringFromSeconds(((value ?? 0) * state.music.duration).floor());
                      } else { // progressing without playingnow ?? shouldn't happen outside disconnection, resetting counter
                        text = "0:00";
                      }

                      return Text(text, style: Theme.of(context).textTheme.subtitle2);
                    },
                  )
                ),
                Flexible(
                    flex: 5,
                    child: PlayingProgressCrabLinearProgressIndicator()
                ),
                Flexible (
                  child: BlocBuilder<PlayingCubit, PlayingState>(
                    builder: (context, state) {

                      String text;

                      if(state is PlayingNow){
                        text = getStringFromSeconds(state.music.duration);
                      } else {
                        text = "6:67";
                      }

                      return Text(text, style: Theme.of(context).textTheme.subtitle2);
                    },
                  )
                )
              ],
            )
        )
    );

    return Padding (
      padding: EdgeInsets.only(top: 50, bottom: 15),
      child: Column (
        children: [
          Expanded (
              flex: 3,
              child: Stack (
                  clipBehavior: Clip.none,
                  children: [

                    Positioned.fill (
                        child: Container (
                            padding: EdgeInsets.only(left: 10, right: 10, bottom: 10),
                            child: Crab (
                                tag: "player_top_container",
                                child: Container (
                                  decoration: BoxDecoration (
                                    color: Theme.of(context).accentColor,
                                    borderRadius: BorderRadius.all(Radius.circular(20)),
                                  ),
                                )
                            )
                        )
                    ),

                    Positioned (
                      top: -10,
                      child: Crab (
                          flightShuttleBuilder: (flightCtx, animation, flightDirection, fromCtx, toCtx) {
                            if(toCtx == null)
                              return null;

                            return FadeTransition (
                                opacity: animation.drive(Tween<double>(begin: 0.0, end:1.0)
                                    .chain(CurveTween(curve: Curves.easeInQuart))),
                                child: (toCtx.widget as Crab).child
                            );
                          },
                          tag: "chat_develop_arrow",
                          child: IconTheme (
                              data: Theme.of(context).accentIconTheme,
                              child: Icon(Icons.arrow_right_rounded)
                          )
                      ),
                    ),

                    Container (
                        width: double.infinity,
                        margin: EdgeInsets.only(left: 10, right: 10, bottom: 10),
                        child: topContainerChild
                    )
                  ]
              )
          ),

          Expanded (
              flex: 3,
              child: Container (
                  margin: EdgeInsets.only(bottom: 10),
                  child: Column (
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children : [
                        Flexible (
                            flex: 3,
                            child: Container (
                                margin: EdgeInsets.all(10),
                                decoration: BoxDecoration (
                                  borderRadius: BorderRadius.circular(10),
                                ),
                                child: BlocBuilder<VoteProgressCubit, double>(
                                  builder: (context, state) {
                                    return ClipRRect (
                                      borderRadius: BorderRadius.circular(10),
                                      child: LinearProgressIndicator(
                                        backgroundColor: Colors.redAccent,
                                        valueColor: AlwaysStoppedAnimation<Color>(Colors.green),
                                        minHeight: 10,
                                        value: state,
                                      ),
                                    );
                                  },
                                )
                            )
                        ),

                        Flexible (
                            flex: 4,
                            child: Container (
                                margin: EdgeInsets.all(10),
                                child: Row (
                                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                    children: [
                                      VoteButton(isPositiveVoteButton: true),
                                      VoteButton(isPositiveVoteButton: false)
                                    ]
                                )
                            )
                        ),

                        Flexible (
                            flex: 2,
                            child: Row (
                              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                              children: [
                                /*CustomIconButton(icon: Icons.add_rounded, onPressed: (){
                                  BlocProvider.of<PlayingProgressCubit>(context).refresh((BlocProvider.of<PlayingProgressCubit>(context).state ?? 0.0) + 0.01);
                                }),*/

                                BlocBuilder<PlayingCubit, PlayingState>(
                                  builder: (context, state) {
                                    IconData icon = Icons.play_arrow_rounded;
                                    Function onPressed;

                                    if(state is PlayingNow){
                                      icon = Icons.stop_rounded;
                                      onPressed = () => BlocProvider.of<PlayingCubit>(context).disconnect();
                                    } else if(state is! PlayingConnecting){
                                      onPressed = () => BlocProvider.of<PlayingCubit>(context).connect();
                                    }

                                    // todo change color / disable button / CircularProgressIndicator on PlayingConnecting ?
                                    return CustomIconButton(icon: icon, onPressed: onPressed);
                                  },
                                ),

                                BlocBuilder<MuteCubit, bool>(
                                    builder: (state, isMute){
                                      IconData icon = Icons.volume_off_rounded;

                                      if(isMute)
                                        icon = Icons.volume_up_rounded;

                                      return CustomIconButton (
                                        icon: icon,
                                        onPressed: () => BlocProvider.of<MuteCubit>(context).mute(!isMute),
                                      );
                                    }
                                ),

                              ],
                            )
                        )
                      ]
                  )
              )
          )
        ],
      )
    );
  }

  String getStringFromSeconds(int seconds) {
    final min = seconds % 60;
    return "${(seconds / 60).floor()}:${min < 10 ? "0$min" : min}";
  }
}


// ***********
// WIDGETS
// ***********

class VoteButton extends StatelessWidget {
  final bool isPositiveVoteButton;

  VoteButton({
    @required this.isPositiveVoteButton
  });

  @override
  Widget build(BuildContext context) {
    return Expanded (
        child: Container (
            margin: EdgeInsets.symmetric(horizontal: 10, vertical: 5),
            child: BlocBuilder<VoteCubit, VoteState>(
              builder: (context, state) {
                // todo change colors according to theme (same thing as status ?)
                Color iconColor;
                Color backgroundColor;
                Color splashColor;

                if(isPositiveVoteButton){
                  if(state.isVotePositive){
                    iconColor = Theme.of(context).scaffoldBackgroundColor; // like here
                    splashColor = Theme.of(context).scaffoldBackgroundColor; // or here
                    backgroundColor = Colors.green;
                  } else {
                    iconColor = Colors.green;
                    splashColor = Colors.lightGreen;
                    backgroundColor = Theme.of(context).scaffoldBackgroundColor;
                  }
                } else {
                  if(state.isVoteNegative){
                    iconColor = Theme.of(context).scaffoldBackgroundColor;
                    splashColor = Theme.of(context).scaffoldBackgroundColor;
                    backgroundColor = Colors.red;
                  } else {
                    iconColor = Colors.red;
                    splashColor = Colors.red[700];
                    backgroundColor = Theme.of(context).scaffoldBackgroundColor;
                  }
                }

                return Ink (
                    decoration: BoxDecoration (
                      border: Border.all(
                          color: isPositiveVoteButton ? Colors.green : Colors.red,
                          width: 3
                      ),
                      borderRadius: BorderRadius.circular(15),
                      color: backgroundColor,
                    ),
                    child: InkWell (
                        borderRadius: BorderRadius.circular(10),
                        splashColor: splashColor,
                        onTap: (){
                          if(!((isPositiveVoteButton && state.isVotePositive)
                              || (!isPositiveVoteButton && state.isVoteNegative))) {
                            BlocProvider.of<VoteCubit>(context).vote(isPositiveVoteButton);
                          }
                        },
                        child: Center (
                            child: LayoutBuilder (
                                builder: (context, constraints){
                                  double size = constraints.biggest.height - constraints.biggest.height * 0.5;
                                  return Icon(
                                      isPositiveVoteButton ? Icons.thumb_up_rounded : Icons.thumb_down_rounded,
                                      size: size, color: iconColor
                                  );
                                }
                            )
                        )
                    )
                );
              },
            )
        )
    );
  }
}


class CustomIconButton extends StatefulWidget {

  final IconData icon;
  final Function onPressed;

  CustomIconButton({@required this.icon, this.onPressed});

  @override
  _CustomIconButtonState createState() => _CustomIconButtonState();
}

class _CustomIconButtonState extends State<CustomIconButton> {

  @override
  Widget build(BuildContext context) {
    return Flexible (
        child: RawMaterialButton (
          onPressed: widget.onPressed,
          elevation: 2,
          fillColor: Theme.of(context).scaffoldBackgroundColor,
          child: LayoutBuilder (
              builder: (context, constraints) {
                return Icon(widget.icon,
                    size: constraints.biggest.height,
                    color: Theme.of(context).accentColor
                );
              }
          ) ,
          padding: EdgeInsets.all(10),
          shape: CircleBorder(),
        )
    );
  }
}