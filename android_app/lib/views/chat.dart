import 'package:brap_radio/cubits/chat_cubit.dart';
import 'package:brap_radio/cubits/user_cubit.dart';
import 'package:brap_radio/models/request/user_entity.dart';
import 'package:brap_radio/models/service_api.dart';
import 'package:brap_radio/models/themes.dart';
import 'package:brap_radio/views/main_screen_views.dart';
import 'package:brap_radio/views/player.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:coast/coast.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../models/widgets.dart';

class ChatPage extends StatefulWidget {
  @override
  _ChatPageState createState() => _ChatPageState();
}

class _ChatPageState extends State<ChatPage> {

  String title = "Titre";
  String authors = "Auteurs";

  FocusNode _textFieldFocusNode;

  TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    _textFieldFocusNode = FocusNode();
  }

  @override
  void dispose() {
    _controller.dispose();
    _textFieldFocusNode.dispose();
    super.dispose();
  }

  void _sendMessage(){
    ServiceApi.sendMessage(_controller.text);
    _controller.clear();
    _textFieldFocusNode.requestFocus();
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        FocusManager.instance.primaryFocus.unfocus();
      },
      child: Column (
          children: [
            Container (
                margin: EdgeInsets.only(top: 40),
                padding: EdgeInsets.all(7),
                child: Stack (
                    children: [
                      Positioned.fill(
                          child: Crab (
                              tag: "player_top_container",
                              child: Material (
                                color: Colors.transparent,
                                child: Container (
                                    decoration: BoxDecoration (
                                        color: Theme.of(context).accentColor,
                                        borderRadius: BorderRadius.circular(20)
                                    )
                                )
                              )
                          )
                      ),

                      Container (
                        margin: EdgeInsets.all(8),
                        child: Row (
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded (
                                flex: MediaQuery.of(context).orientation == Orientation.portrait ? 4 : 2,
                                child: Container (
                                    margin: EdgeInsets.all(5),
                                    child: Column (
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [
                                        PlayingCubitImageCrab()
                                      ],
                                    )
                                )
                            ),

                            Expanded (
                                flex: 15,
                                child: Container (
                                    padding: EdgeInsets.symmetric(horizontal: 5),
                                    child: Column (
                                        crossAxisAlignment: CrossAxisAlignment.center,
                                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          Row (
                                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                              crossAxisAlignment: CrossAxisAlignment.center,
                                              children: [
                                                Expanded (
                                                    flex: 6,
                                                    child: PlayingCubitTextCrab (
                                                      textBuilder: (context, state) =>
                                                          Text(
                                                            getStringFromPlayingState(state, "Déconnecté", "Connection...", "TITLE"),
                                                            style: Theme.of(context).textTheme.headline6.copyWith(fontSize: 15),
                                                            textAlign: TextAlign.center,
                                                            overflow: TextOverflow.fade,
                                                            softWrap: false,
                                                          ),
                                                      crabTag: 'player_title_text',
                                                    )
                                                ),
                                                Flexible (
                                                  flex: 1,
                                                  child: Text("-", style: Theme.of(context).textTheme.headline6.copyWith(fontSize: 15))
                                                ),
                                                Expanded (
                                                    flex: 6,
                                                    child: PlayingCubitTextCrab (
                                                      textBuilder: (context, state) =>
                                                          Text(
                                                              getStringFromPlayingState(state, "Vous êtes déconnecté(e) de la radio", "Connection en cours", "AUTHORS"),
                                                            style: Theme.of(context).textTheme.headline6.copyWith(fontSize: 15),
                                                            textAlign: TextAlign.center,
                                                            overflow: TextOverflow.fade,
                                                            softWrap: false
                                                          ),
                                                      crabTag: "player_authors_text",
                                                    )
                                                ),
                                              ]
                                          ),
                                          SizedBox(height: 15),
                                          //Spacer(),

                                          PlayingProgressCrabLinearProgressIndicator(),
                                        ]
                                    )
                                )
                            ),

                            Expanded (
                                flex: 2,
                                child: Container (
                                    padding: EdgeInsets.only(right: 5),
                                    child: Column (
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      crossAxisAlignment: CrossAxisAlignment.end,
                                      children: [
                                        Crab (
                                            flightShuttleBuilder: (flightCtx, animation, flightDirection, fromCtx, toCtx) {
                                              if(toCtx == null)
                                                return null;

                                              return FadeTransition (
                                                  opacity: animation.drive(Tween<double>(begin: 1.0, end:0.0)
                                                      .chain(CurveTween(curve: Curves.easeOutExpo))),
                                                  child: (toCtx.widget as Crab).child
                                              );
                                            },
                                            tag: "chat_develop_arrow",
                                            child: IconTheme(data: Theme.of(context).accentIconTheme, child: Icon(Icons.arrow_right_rounded))
                                        ),
                                      ],
                                    )
                                )
                            )
                          ],
                        )
                      ),

                      Positioned.fill (
                        child: Material (
                            color: Colors.transparent,
                            child:  InkWell (
                              borderRadius: BorderRadius.circular(20),
                              onTap: () => MainPage.of(context).animateToPlayer(),
                            )
                        )
                      ),
                    ]
                )
            ),

            Divider(thickness: 1.5,),
            Expanded (
                child: BlocBuilder<ChatCubit, ChatState>(
                    builder: (context, state) {
                      String selfNickname = BlocProvider.of<UserCubit>(context).nickname + " (Vous)";

                      if(state is ChatInitial){
                        return Center (
                            child: Text("Aucun message n'a été reçu pour le moment...",
                                style: Theme.of(context).textTheme.caption)
                        );
                      }


                      if(state is ChatRefreshed){

                        if(state.messages.isEmpty){
                          return Center (
                            child: Text("Aucun message n'a été reçu pour le moment...",
                                style: Theme.of(context).textTheme.subtitle1)
                          );
                        }

                        return ListView.builder(
                          itemCount: state.messages.length,
                          itemBuilder: (context, i) {
                            final currentMessage = state.messages[i];
                            final defaultTheme = Theme.of(context).textTheme.bodyText1;
                            final statusColors = Themes.chatStatusColorByThemes[UserPreferences().theme];


                            return Container (
                                  margin: EdgeInsets.symmetric(vertical: 2.5, horizontal: 5),
                                  child: RichText (
                                  text: TextSpan (
                                      style: defaultTheme.copyWith(fontWeight: FontWeight.w600),
                                      children: [
                                        TextSpan(
                                            text: currentMessage.userFrom == Status.SELF ? selfNickname : currentMessage.userFrom,
                                            style: defaultTheme.copyWith(color: statusColors[currentMessage.authorStatus] ?? statusColors[Status.OTHER])
                                        ),

                                        TextSpan(text: " : "),
                                        TextSpan(text: currentMessage.messageContent, style: defaultTheme.copyWith(fontWeight: FontWeight.w400))
                                      ]
                                  ),
                                )
                            );
                          },
                        );
                      }

                      // state is ChatInitial
                      return Center(child: CircularProgressIndicator());
                    }
                ),
            ),

            Container (
                margin: EdgeInsets.all(10),
                padding: EdgeInsets.all(10),
                decoration: BoxDecoration (
                  // border: Border.all(color: Theme.of(context).accentColor),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: TextField (
                  controller: _controller,
                  focusNode: _textFieldFocusNode,

                  onSubmitted: (String msg) => _sendMessage(),
                  /*onTap: () {
                    FocusScopeNode currentFocus = FocusScope.of(context);
                    if (!currentFocus.hasPrimaryFocus && currentFocus.focusedChild != null) {
                      currentFocus.focusedChild.unfocus();
                    }
                  },
                  autofocus: false,*/
                  style: Theme.of(context).textTheme.bodyText1,
                  decoration: InputDecoration (
                    suffixIcon: ClipOval(
                      child: Material(
                        color: Theme.of(context).scaffoldBackgroundColor, // Button color
                        child: InkWell(
                          splashColor: Theme.of(context).accentColor, // Splash color
                          onTap: () {
                            if(_controller.text.isNotEmpty)
                              _sendMessage();
                          },
                          child: Icon(Icons.send_rounded, color: Theme.of(context).accentColor),
                        ),
                      ),
                    ),
                    labelText: "Écrire un message...",
                    labelStyle: Theme.of(context).textTheme.subtitle1,
                  ),

                )
            )
          ]
      )
    );
  }
}