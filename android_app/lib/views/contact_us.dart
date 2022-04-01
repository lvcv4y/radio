import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

const TWITTER_URL = "https://twitter.com";
const DISCORD_URL = "https://discord.gg";
const DONATION_URL = "https://tipeee.com";

class ContactUs extends StatefulWidget {
  @override
  _ContactUsState createState() => _ContactUsState();
}

class _ContactUsState extends State<ContactUs> {
  @override
  Widget build(BuildContext context) {
    return Scaffold (
      appBar: AppBar (
        title: Text("Nous contacter")
      ),
      
      body: ListView (
        children: [
          CustomListTile (
            title: "Discord",
            subtitle: "Une question ? Une suggestion ? Envie de participer à "
                "certains évènements ? Rejoins-nous sur Discord !",
            leadingWidget: ClipOval(child: Image.asset("images/logo.png")),
            trailingIcon: Icons.keyboard_arrow_right,
            onTap: () => startURL(DISCORD_URL, "Vous pouvez toujours nous rejoindre"
                " manuellement en copiant-collant l'invitation : $DISCORD_URL"),
          ),
          CustomListTile (
              title: "Twitter",
              subtitle: "Soyez au courant des  dernières nouveautés "
                  "en nous suivant sur twitter : @arobase",
              leadingWidget: ClipOval(child: Image.asset("images/logo.png")),
              trailingIcon: Icons.keyboard_arrow_right,
              onTap: () => startURL(TWITTER_URL, "Vous pouvez toujours trouver manuellement sur Twitter : @arobase"),
          ),
          CustomListTile (
            title: "Tipeee",
            subtitle: "Envie de devenir VIP ? Soutiens-nous via *plateforme* et"
                " obtient des avantages uniques !",
            leadingWidget: ClipOval(child: Image.asset("images/logo.png")),
            trailingIcon: Icons.keyboard_arrow_right,
            onTap: () => startURL(DONATION_URL, "Vous pouvez toujours nous trouver "
                "manuellement sur *PLATFORM* : *NAME*"),
          ),
        ],
      )
    );
  }

  Future<void> startURL(final String url, final String endErrorMessage) async {
    await canLaunch(url) ? await launch(url) : showDialog(context: context, builder:
        (BuildContext ctx) => ErrorDialog(endErrorMessage));
  }
}



class ErrorDialog extends StatelessWidget {

  final String endErrorMessage;

  ErrorDialog(this.endErrorMessage);

  @override
  Widget build(BuildContext context) {
    return AlertDialog (
      title: Text("Erreur"),
      content: Text("Mince ! il semblerait qu'une erreur soit apparue dans le processus d'ouverture de la page Web. $endErrorMessage"), // todo rapport ??
      actions: [TextButton(onPressed: () => Navigator.of(context).pop(), child: Text("OK"))],
    );
  }
}
