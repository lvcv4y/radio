import 'package:brap_radio/cubits/user_cubit.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AccountSettingsMainScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Paramètres du compte"),
      ),
      body: Column (

        children: [
          Flexible (
            flex: 2,
            child: Row (
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  Flexible (
                      flex: 3,
                      child: Padding (
                        padding: EdgeInsets.all(10),
                        child: ClipOval (
                            child: Image.asset("images/logo.png", fit:BoxFit.cover)
                        )
                      )
                  ),
                  Flexible (
                      flex: 5,
                      child: Container (
                        width: double.infinity,
                        padding: EdgeInsets.only(left: 10, right: 10),
                        child: BlocBuilder<UserCubit, UserState>(
                          builder: (context, state) {

                            String nickname = "Pseudo", email = "Email";

                            if(state is UserModel) {
                              nickname = state.nickname;
                              email = state.email;
                            }

                            return Column (
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                TextButton (
                                    onPressed: () {
                                      showDialog(context: context, builder: (BuildContext ctx) =>
                                          _ChangingDialog (
                                              title: "Changer le pseudo",
                                              changingField: "Pseudo",
                                              currentValue: nickname,
                                              onValidate: (String newValue) =>
                                                  BlocProvider.of<UserCubit>(context).refreshNickname(newValue)
                                          )
                                      );
                                    },
                                    child: Row (
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [
                                        Flexible (
                                            flex: 10,
                                            child: Text(nickname, style: Theme.of(context).textTheme.bodyText1)
                                        ),
                                        Flexible (
                                            flex: 1,
                                            child: Icon(Icons.arrow_right_rounded)
                                        )
                                      ],
                                    )
                                ),
                                TextButton (
                                    onPressed: () {
                                      showDialog(context: context, builder: (BuildContext ctx) =>
                                          _ChangingDialog (
                                              title:"Changer l'email",
                                              changingField: "Email",
                                              currentValue: email,
                                              onValidate: (String newValue) =>
                                                  BlocProvider.of<UserCubit>(context).refreshEmail(newValue)
                                          )
                                      );
                                    },
                                    child: Row (
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [
                                        Flexible (
                                            flex: 10,
                                            child: Text(email, style: Theme.of(context).textTheme.bodyText2, overflow: TextOverflow.ellipsis,)
                                        ),
                                        Flexible (
                                            flex: 1,
                                            child: Icon(Icons.arrow_right_rounded)
                                        )
                                      ],
                                    )
                                )
                              ],
                            );
                          },
                        )
                      )
                  )
                ]
            )
          ),

          Flexible (
            flex: 3,
            child: Column (
              mainAxisAlignment: MainAxisAlignment.start,
              children: [
                NoEndWidgetPreferenceButton (
                  title: "Modifier le mot de passe",
                  description: "Changer votre mot de passe",
                  onPressed: () => showDialog(context: context, builder: (_) => _ChangingPasswordDialog()),
                ),

                // SizedBox(height: 20),
                Expanded (
                  child: Column (
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Flexible (
                        child: TextButton(onPressed: () => null, child: Padding (
                            padding: EdgeInsets.all(5),
                            child: Text("Se déconnecter", style: Theme.of(context).textTheme.headline3)
                        )
                        )
                      ),
                      Flexible (
                        child: TextButton(onPressed: () => null, child: Padding (
                            padding: EdgeInsets.all(5),
                            child: Text("Supprimer le compte", style: Theme.of(context).textTheme.headline3.copyWith(color: Colors.red))
                        )
                        ),
                      )

                    ],
                  ),
                )
              ],
            )
          )
        ]
      )
    );
  }
}

class _ChangingDialog extends StatefulWidget {
  final String title;
  final String changingField;
  final String currentValue;
  final Function onValidate;


  _ChangingDialog({
    Key key,
    @required this.title,
    @required this.changingField,
    @required this.currentValue,
    this.onValidate}) : super(key: key);

  @override
  State createState() => _ChangingDialogState();

}


class _ChangingDialogState extends State<_ChangingDialog> {

  TextEditingController controller = TextEditingController();

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    final List<Widget> children = [];

    if(controller.text.isEmpty){
      children.add(
          Row (
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.warning_amber_rounded, color: Colors.red),
              Text("Vous devez remplir le champ !", style:Theme.of(context).textTheme.bodyText2.copyWith(color:Colors.red))
            ],
          )
      );
    }

    return SimpleDialog (

      title: Text(widget.title, style: Theme.of(context).textTheme.headline2),
        shape: RoundedRectangleBorder (
            borderRadius: BorderRadius.all(Radius.circular(15))
        ),

      children: children..addAll([
        Padding (
            padding: EdgeInsets.all(10),
            child: TextFormField (
              controller: controller,
              style: Theme.of(context).textTheme.bodyText2,
              decoration: InputDecoration (
                  labelText: widget.changingField,
                  labelStyle: Theme.of(context).textTheme.bodyText2,
              ),
              onChanged: (String s) {
                setState(() => null);
              }
            )
        ),

        Padding (
            padding: EdgeInsets.only(left:10, right:10),
            child: Row (
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(child: Text("Annuler"), onPressed: () => Navigator.of(context).pop()),
                TextButton(child: Text("Valider"), onPressed: () {

                  if(controller.text == widget.currentValue){
                    Navigator.of(context).pop();
                    return;
                  }
                  if(controller.text.isNotEmpty){
                    widget.onValidate(controller.text);
                    Navigator.of(context).pop();
                  } else {
                    setState(() => null);
                  }
                }),
              ]),
            )
      ],
    ));
  }
}

class _ChangingPasswordDialog extends StatefulWidget {

  @override
  State createState() => _ChangingPasswordDialogState();

}


class _ChangingPasswordDialogState extends State<_ChangingPasswordDialog> {

  TextEditingController _currentPasswordController = TextEditingController();
  TextEditingController _newPasswordController = TextEditingController();
  TextEditingController _confNewPwdController = TextEditingController();

  @override
  void dispose() {
    _currentPasswordController.dispose();
    _newPasswordController.dispose();
    _confNewPwdController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    /*final List<Widget> children = [];

    if(_currentPasswordController.text.isEmpty){
      children.add(
          Row (
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.warning_amber_rounded, color: Colors.red),
              Text("Vous devez remplir le champ !", style:Theme.of(context).textTheme.bodyText2.copyWith(color:Colors.red))
            ],
          )
      );
    }

    if(_newPasswordController.text.isEmpty){
      children.add(
          Row (
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.warning_amber_rounded, color: Colors.red),
              Text("Vous devez remplir le champ !", style:Theme.of(context).textTheme.bodyText2.copyWith(color:Colors.red))
            ],
          )
      );
    }

    if(_confNewPwdController.text.isEmpty){
      children.add(
          Row (
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.warning_amber_rounded, color: Colors.red),
              Text("Vous devez remplir le champ !", style:Theme.of(context).textTheme.bodyText2.copyWith(color:Colors.red))
            ],
          )
      );
    }*/

    return SimpleDialog (
        title: Text("Changer le mot de passe", style: Theme.of(context).textTheme.headline2),
        shape: RoundedRectangleBorder (
            borderRadius: BorderRadius.all(Radius.circular(15))
        ),

        children: [

          Padding (
              padding: EdgeInsets.all(10),
              child: TextFormField (
                  controller: _currentPasswordController,
                  style: Theme.of(context).textTheme.bodyText2,
                  decoration: InputDecoration (
                      labelStyle: Theme.of(context).textTheme.bodyText2,
                      hintText: "Mot de passe actuel",
                      hintStyle: Theme.of(context).textTheme.bodyText2.copyWith(color: Colors.red),
                      enabledBorder: UnderlineInputBorder(borderSide: BorderSide (
                          color: _currentPasswordController.text.isEmpty ? Colors.red : Colors.green)
                      )
                  ),
                  onChanged: (String s) {
                    if(s.isNotEmpty) setState(() {});
                  }
              )
          ),

          Padding (
              padding: EdgeInsets.all(10),
              child: TextFormField (
                  controller: _newPasswordController,
                  style: Theme.of(context).textTheme.bodyText2,
                  decoration: InputDecoration (
                      labelStyle: Theme.of(context).textTheme.bodyText2,
                      hintText: "Nouveau mot de passe",
                      hintStyle: Theme.of(context).textTheme.bodyText2.copyWith(color: Colors.red),
                      enabledBorder: UnderlineInputBorder(borderSide: BorderSide (
                          color: _newPasswordController.text.isEmpty ? Colors.red : Colors.green)
                      )
                  ),
                  onChanged: (String s) {
                    if(s.isNotEmpty) setState(() {});
                  }
              )
          ),

          Padding (
              padding: EdgeInsets.all(10),
              child: TextFormField (
                  controller: _confNewPwdController,
                  style: Theme.of(context).textTheme.bodyText2,
                  decoration: InputDecoration (
                      labelStyle: Theme.of(context).textTheme.bodyText2,
                      hintText: "Confirmer le nouveau mot de passe",
                      hintStyle: Theme.of(context).textTheme.bodyText2.copyWith(color: Colors.red),
                      enabledBorder: UnderlineInputBorder(borderSide: BorderSide (
                          color: _confNewPwdController.text.isEmpty ? Colors.red : Colors.green)
                      )
                  ),
                  onChanged: (String s) {
                    if(s.isNotEmpty) setState(() {});
                  }
              )
          ),

          Padding (
            padding: EdgeInsets.only(left:10, right:10),
            child: Row (
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  TextButton(child: Text("Annuler"), onPressed: () => Navigator.of(context).pop()),
                  TextButton(child: Text("Valider"), onPressed: () {
                    final bloc = BlocProvider.of<UserCubit>(context);
                    if(bloc.state is UserModel){

                    }

                    Navigator.pop(context);
                  }),
                ]),
          )

        ]);
  }
}