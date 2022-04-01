import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';


class HelpUs extends StatefulWidget {

  @override
  State createState() => _HelpUsState();
}

class _HelpUsState extends State<HelpUs> with TickerProviderStateMixin {

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    return Scaffold (
      appBar: AppBar(
        title: Text("Nous aider"),
      ),

      body: ListView (
        children: [

          Container (
            decoration: BoxDecoration (
              border: Border.all(color: Theme.of(context).accentColor),
              borderRadius: BorderRadius.circular(20),
            ),
            padding: EdgeInsets.all(20),
            margin: EdgeInsets.only(top: 20, right: 10, left: 10, bottom: 10),
            child: Text("Vous souhaitez nous aider dans notre developpement ?",
              style:Theme.of(context).textTheme.headline2.copyWith(color: Theme.of(context).accentColor),
              textAlign: TextAlign.center,)
          ),

          Container(margin: EdgeInsets.only(top: 10, bottom: 10),
              child: Divider(thickness: 1.5)
          ),

          CustomListTile (
              title: "Nous faire un don sur *platform*", // todo set platform name (on subtitle too)
              subtitle: "En nous faisant une donation, vous nous soutenez financièrement "
                        "en plus d\'avoir accès à des avantages directement dans l\'application "
                        "(une fois avoir lié votre compte à votre compte *PLATFORM* (en nous le "
                        "signalant via Twitter ou Discord) !",
              subtitlePadding: EdgeInsets.only(top: 7.5, bottom: 7.5),
              trailingIcon: Icons.keyboard_arrow_right,
              leadingWidget: ClipOval(child: Image.asset("images/logo.png")), // todo set platform logo
              onTap: () => null,
          ),

          Container(margin: EdgeInsets.only(top: 10, bottom: 10),
              child: Divider(thickness: 1.5)
          ),

          CustomListTile (
            title: "Regarder une pub",
            subtitle: "Vous pouvez nous soutenir financièrement sans dépenser le moindre centime !"
                " Pour cela, il vous suffit de regarder une vidéo en cliquant ici.", // todo set reward
            subtitlePadding: EdgeInsets.only(top: 7.5, bottom: 7.5),
            trailingIcon: Icons.keyboard_arrow_right,
            leadingWidget: IconTheme(data: Theme.of(context).accentIconTheme,
                child: Icon(Icons.play_arrow_rounded)
            ),
            onTap: () => null,
          ),

          Container(margin: EdgeInsets.only(top: 10, bottom: 10),
              child: Divider(thickness: 1.5)
          ),

          Container (
              decoration: BoxDecoration (
                border: Border.all(color: Theme.of(context).accentColor),
                borderRadius: BorderRadius.all(Radius.circular(20)),
              ),
              padding: EdgeInsets.all(20),
              margin: EdgeInsets.only(top: 10, right: 10, left: 10, bottom: 20),
              child: Text("En dehors de l'aide financière, vous pouvez nous soutenir en "
                  "partageant cette application",
                style:Theme.of(context).textTheme.headline2.copyWith(color: Theme.of(context).accentColor),
                textAlign: TextAlign.center,)
          ),
        ]
      )
    );
  }
}