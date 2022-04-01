import 'package:brap_radio/cubits/user_cubit.dart';
import 'package:flutter/material.dart';
import 'package:coast/coast.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'player.dart';
import 'chat.dart';

import 'package:brap_radio/views/events.dart';
import 'package:brap_radio/views/history.dart';
import 'package:brap_radio/views/preferences.dart';
import 'package:brap_radio/views/account_settings.dart';
import 'package:brap_radio/views/help_us.dart';
import 'package:brap_radio/views/contact_us.dart';


class MainPage extends StatefulWidget {
  @override
  MainPageState createState() => MainPageState();

  static MainPageState of(BuildContext context) => context.findAncestorStateOfType();
}

class MainPageState extends State<MainPage> {
  GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();
  List<Beach> _beaches = [
      Beach(builder: (_) => PlayerPage()),
      Beach(builder: (_) => ChatPage()),
  ];

  CoastController _pageController;
  int selectedIndex = 1;

  void animateToPlayer(){
    _pageController.animateTo(beach: 0);
  }

  @override
  void initState() {
    super.initState();
    _pageController = CoastController (
      initialPage: 0,
    );
  }


  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  bool _isKeyboardVisible(){
    return !(MediaQuery.of(context).viewInsets.bottom == 0);
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold (
      key: _scaffoldKey,
      drawer: MainPageDrawer (
        onOpen: () => setState(() => selectedIndex = 0),
        onClose: () => setState(() => selectedIndex = _pageController.beach.toInt() + 1),
      ),
      body: Coast (
        physics: _isKeyboardVisible() ? NeverScrollableScrollPhysics() : AlwaysScrollableScrollPhysics(),
        controller: _pageController,
        onPageChanged: (int i){
          if(this.mounted) setState(() => selectedIndex= i+1);
        },
        beaches: _beaches,
        observers: [
          CrabController()
        ],
      ),

      bottomNavigationBar: BottomNavigationBar (
          currentIndex: selectedIndex,
          onTap: (int i) => setState((){
            selectedIndex = i;

            if(i == 0)
              _scaffoldKey.currentState.openDrawer();
            else
              _pageController.animateTo(beach: i - 1, duration: Duration(milliseconds: 300), curve: Curves.ease);
          }),
          items: [
            BottomNavigationBarItem(icon: Icon(Icons.settings_rounded), label: "paramètres"),
            BottomNavigationBarItem (icon: Icon(Icons.play_arrow_rounded), label: "lecteur"),
            BottomNavigationBarItem(icon: Icon(Icons.chat), label: "Chat")
          ]
      ),
    );
  }
}


class MainPageDrawer extends StatefulWidget {
  final Function onOpen;
  final Function onClose;

  MainPageDrawer({this.onOpen, this.onClose});

  @override
  _MainPageDrawerState createState() => _MainPageDrawerState();
}

class _MainPageDrawerState extends State<MainPageDrawer> {

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.onOpen();
    });
  }

  @override
  void dispose() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      widget.onClose();
    });
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Drawer (
        child: ListView (
          children: [
            DrawerHeader (
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.secondary,
                ),
                child: Row (
                    children: [
                      Expanded (
                          flex: 2,
                          child: Column (
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              ClipOval(child: Image.asset("images/logo.png"))
                            ],
                          )
                      ),

                      Expanded (
                          flex: 4,
                          child: BlocBuilder<UserCubit, UserState>(
                            builder: (context, state) {
                              String nick = "pseudo", status = "Membre";

                              if(state is UserModel){
                                nick = state.nickname;

                                if(state.status.isNotEmpty)
                                  status = state.status.join(", ");
                              }


                              return Column (
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Text(nick, style: Theme.of(context).textTheme.headline5),
                                    SizedBox(height:10),
                                    // Text("999 credits", style: Theme.of(context).textTheme.subtitle2), todo credits ?
                                    Text(status, style: Theme.of(context).textTheme.subtitle2)
                                  ]
                              );
                            },
                          ),
                      )
                    ]
                )
            ),
            Padding (
                padding: EdgeInsets.only(left:20, top:5, bottom:5),
                child: Text("Paramètres", style: Theme.of(context).textTheme.subtitle1)
            ),
            ListTile (
              leading: Icon(Icons.history_rounded),
              title: Text("Historique"),
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => History())),
            ),
            ListTile (
              leading: Icon(Icons.event_note_rounded),
              title: Text("Évènements de la semaine"),
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => EventView())),
            ),
            ListTile (
              leading: Icon(Icons.description_rounded),
              title: Text("Règlement du chat"),
            ),

            Divider(thickness:1),
            Padding (
                padding: EdgeInsets.only(left:20, top:5, bottom:5),
                child: Text("Informations", style: Theme.of(context).textTheme.subtitle1)
            ),

            ListTile (
              leading: Icon(Icons.app_settings_alt_rounded),
              title: Text("Paramètres de l'application"),
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => PreferencesMainScreen())),
            ),
            ListTile (
                leading: Icon(Icons.settings_rounded),
                title: Text("Gérer le compte"),
                onTap : () => Navigator.push(context, MaterialPageRoute(builder: (_) => AccountSettingsMainScreen()))
            ),
            Divider(thickness:1),
            Padding (
              padding: EdgeInsets.only(left:20, top:5, bottom:5),
              child: Text("A propos de nous", style: Theme.of(context).textTheme.subtitle1),
            ),
            ListTile (
              leading: Icon(Icons.info_outline_rounded),
              title: Text("Nous aider"),
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => HelpUs())),
            ),
            ListTile (
                leading: Icon(Icons.info_outline_rounded),
                title: Text("Nous contacter / Nous suivre"),
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => ContactUs()))
            ),
            Divider(thickness: 1),
            ListTile (
              title: Text("À propos"),
            ),
          ],
        )
    );
  }
}

