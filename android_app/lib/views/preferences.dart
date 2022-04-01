import 'package:brap_radio/cubits/preferences_cubit.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:flutter/material.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class _EndSwitchOption extends StatelessWidget {

  final String title, description;
  final double maxDescriptionWidth;
  final Function onPressed, onChanged;
  final bool value;

  _EndSwitchOption({
    @required this.title,
    @required this.description,
    @required this.maxDescriptionWidth,
    @required this.value,
    this.onPressed,
    this.onChanged});

  @override
  Widget build(BuildContext context) {
    return EndWidgetPreferenceButton(
      title: title,
      description: description,
      maxDescriptionWidth: maxDescriptionWidth,
      onPressed: onPressed,
      endWidget: Switch(value: value, onChanged: onChanged),
    );
  }
}

class PreferencesMainScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: Text("Paramètres")),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Column (
              children: [
                NoEndWidgetPreferenceButton(
                    title: "Apparence",
                    description: "Couleurs, Thème",
                    onPressed: (){Navigator.push(context, MaterialPageRoute(builder: (BuildContext ctx) => _Appearances()));
                }),
                NoEndWidgetPreferenceButton(
                    title: "Consommation",
                    description: "Connexion au chat, Gestion de la consommation des données",
                    onPressed: (){Navigator.push(context, MaterialPageRoute(builder: (BuildContext ctx) => _Consumption()));
                }),
                NoEndWidgetPreferenceButton(
                    title: "Données personnelles",
                    description: "accessibilités des données",
                    onPressed: (){Navigator.push(context, MaterialPageRoute(builder: (BuildContext ctx) => Privacy()));
                }),
              ],
            )
          ],
        )
    );
  }
}

class _Appearances extends StatelessWidget {
  final UserPreferences prefs = UserPreferences();
  @override
  Widget build(BuildContext context) {
    final double maxDescriptionWidth = MediaQuery.of(context).size.width - 100;
    return Scaffold(
        appBar: AppBar(
          title: Text('Paramètres - Apparence'),
        ),

        body : Column (
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [

            SinglePreferenceBlocBuilder(
                preferenceName: UserPreferencesName.THEME,
                preferenceType: String,
                defaultValue: prefs.theme,
                builder: (context, value){
                  return NoEndWidgetPreferenceButton(title: "Thème", description: value,
                      onPressed: (){
                        showDialog(
                            context: context,
                            builder: (_) => DialogRadioButton (
                                title: "Thème",
                                radioButtonsWithValue : {"Thème par défaut": 'default', "Thème sombre": 'dark'},
                                currentValue: prefs.theme, // accessing from preferences cause otherwise value (got from bloc) not updating
                                onChanged: (String newTheme) =>
                                    BlocProvider.of<PreferencesCubit>(context).updatePreference(UserPreferencesName.THEME, newTheme)
                            ),
                        );
                      });
                }),

            SinglePreferenceBlocBuilder(
                preferenceName: UserPreferencesName.NIGHT_MODE,
                preferenceType: bool,
                defaultValue: false,
                builder: (context, value){
                  return _EndSwitchOption(title: "Mode nocturne",
                      description: "activer cette option réduira le taux de lumière bleue et l'intensité des couleurs utilisées",
                      maxDescriptionWidth: maxDescriptionWidth, value: value,
                      onPressed: () => BlocProvider.of<PreferencesCubit>(context).updatePreference(UserPreferencesName.NIGHT_MODE, !value),
                      onChanged: (bool b) => BlocProvider.of<PreferencesCubit>(context).updatePreference(UserPreferencesName.NIGHT_MODE, b));
                }
            ),
          ],
        )
    );
  }
}

class _Consumption extends StatelessWidget {

  void updatePrefsOnEcoModeChange(bool newVal, PreferencesCubit bloc){
    if(newVal){
      bloc.updatePreference(UserPreferencesName.GET_HISTORY, false);
      bloc.updatePreference(UserPreferencesName.VOTE_REFRESH_DELAY, 5.0);
    }

    bloc.updatePreference(UserPreferencesName.ECO_MODE, newVal);
  }


  @override
  Widget build(BuildContext context) {
    final double maxDescriptionWidth = MediaQuery.of(context).size.width - 100;
    final UserPreferences prefs = UserPreferences();

    return Scaffold(
        appBar: AppBar(
          title: Text('Paramètres - Consommation'),
        ),

        body: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SinglePreferenceBlocBuilder(
                preferenceName: UserPreferencesName.ECO_MODE,
                preferenceType: bool,
                defaultValue: prefs.ecoMode,
                builder: (context, value) {
                  return _EndSwitchOption(
                      title: 'Mode Économie',
                      description: "Désactive toutes les fonctions optionnelles automatiques, afin "
                          "d'économiser les données (recommandé si vous avez un abonnement internet limité)",
                      maxDescriptionWidth: maxDescriptionWidth,value: value,
                      onPressed: () => updatePrefsOnEcoModeChange(!value, BlocProvider.of<PreferencesCubit>(context)),
                      onChanged: (bool b) => updatePrefsOnEcoModeChange(b, BlocProvider.of<PreferencesCubit>(context))
                  );
                }),

            MultiplePreferencesNotifier(
                preferencesNames: [UserPreferencesName.GET_HISTORY, UserPreferencesName.ECO_MODE],
                preferencesTypes: [bool, bool],
                defaultPreferenceNameIndex: 0,
                defaultValue: prefs.getHistory,
                builder: (context, prefName, value){

                  return _EndSwitchOption(
                      title: "Récupération de l'historique",
                      description: "Permet la récupération de l'historique précédent votre connexion",
                      maxDescriptionWidth: maxDescriptionWidth,
                      value: prefs.getHistory, // more simple like this, since value can be both ecomode && history (what if ecomode false is received ??)
                      onPressed: prefs.ecoMode ? null : () {
                        BlocProvider.of<PreferencesCubit>(context)
                            .updatePreference(UserPreferencesName.GET_HISTORY, !prefs.getHistory);
                      },
                      onChanged: prefs.ecoMode ? null : (bool b) {
                        BlocProvider.of<PreferencesCubit>(context)
                            .updatePreference(UserPreferencesName.GET_HISTORY, b);
                      });
                }),

            MultiplePreferencesNotifier(
                preferencesNames: [UserPreferencesName.VOTE_REFRESH_DELAY, UserPreferencesName.ECO_MODE],
                preferencesTypes: [double, bool],
                defaultPreferenceNameIndex: 0,
                defaultValue: prefs.voteRefreshingDelay,
                builder: (context, prefName, value) {
                  return NoEndWidgetPreferenceButton(
                      title: "Rafraichissement des votes",
                      description: "Définit le temps d'attente entre chaque rafraichissement",
                      onPressed: prefName == UserPreferencesName.ECO_MODE && value ? null :
                          () {
                        showDialog(context: context,
                            builder: (_) => DialogRadioButton(
                              title: "Rafraichissement des votes (en secondes)",
                              radioButtonsWithValue: {"0.25": 0.25, "0.5":0.5, "1": 1.0, "1.5": 1.5, "2": 2.0, "3":3.0, "5": 5.0},
                              currentValue: prefs.voteRefreshingDelay,
                              onChanged: (double val) => BlocProvider.of<PreferencesCubit>(context)
                                  .updatePreference(UserPreferencesName.VOTE_REFRESH_DELAY, val),
                            )
                        );
                      }
                  );
                }
            )
          ],
        )
    );
  }
}

class Privacy extends StatelessWidget {

  void _updatePrefsOnAnonymousModeChange(bool newVal, PreferencesCubit bloc){
    bloc.updatePreference(UserPreferencesName.ANONYMOUS_MODE, newVal);

    if(newVal){
      bloc.updatePreference(UserPreferencesName.SHOW_EMAIL, false);
      bloc.updatePreference(UserPreferencesName.SHOW_NICKNAME, false);
      bloc.updatePreference(UserPreferencesName.SHOW_STATUS, false);
      bloc.updatePreference(UserPreferencesName.SHOW_PROPOSITIONS, false);
    }
  }

  @override
  Widget build(BuildContext context) {
    UserPreferences prefs = UserPreferences();

    final double maxDescriptionWidth = MediaQuery.of(context).size.width - 100;

    return Scaffold(
        appBar: AppBar(
            title: Text('Paramètres - Données Personnelles')
        ),

        body: MultiplePreferencesNotifier(
            preferencesNames: [UserPreferencesName.ANONYMOUS_MODE, UserPreferencesName.PRIVACY_MODE],
            preferencesTypes: [bool, String],
            defaultPreferenceNameIndex: 1,
            defaultValue: "public",
            builder: (context, prefName, value){
              return ListView (
                scrollDirection: Axis.vertical,
                physics: AlwaysScrollableScrollPhysics(),
                shrinkWrap: true,
                children: [
                  MultiplePreferencesNotifier(
                      preferencesNames: [UserPreferencesName.PRIVACY_MODE, UserPreferencesName.ANONYMOUS_MODE],
                      preferencesTypes: [String, bool],
                      defaultPreferenceNameIndex: 0,
                      defaultValue: prefs.privacyMode,
                      builder: (context, prefName, value){
                        return NoEndWidgetPreferenceButton (
                            title: "Accessibilité du profile",
                            description: "Visibilité du profile (NB : le Staff aura toujours accès à votre profile)",
                            onPressed: prefs.anonymousMode ? null : () {
                              showDialog(context: context, builder: (_) => DialogRadioButton(
                                title: "Visibilité du profile",
                                radioButtonsWithValue: {"Public": "public", "Privé": "private", "Personnalisé": "custom"},
                                currentValue: prefs.privacyMode,
                                onChanged: (String newValue) {
                                  final bloc = BlocProvider.of<PreferencesCubit>(context);
                                  bloc.updatePreference(UserPreferencesName.PRIVACY_MODE, newValue);

                                  if("public" == newValue){
                                    bloc.updatePreference(UserPreferencesName.SHOW_EMAIL, false);
                                    bloc.updatePreference(UserPreferencesName.SHOW_NICKNAME, true);
                                    bloc.updatePreference(UserPreferencesName.SHOW_STATUS, true);
                                    bloc.updatePreference(UserPreferencesName.SHOW_PROPOSITIONS, true);
                                  } else if("private" == newValue){
                                    bloc.updatePreference(UserPreferencesName.SHOW_EMAIL, false);
                                    bloc.updatePreference(UserPreferencesName.SHOW_NICKNAME, true);
                                    bloc.updatePreference(UserPreferencesName.SHOW_STATUS, false);
                                    bloc.updatePreference(UserPreferencesName.SHOW_PROPOSITIONS, false);
                                  }
                                },
                              ));
                            }
                        );
                      }),

                  SinglePreferenceBlocBuilder(
                      preferenceName: UserPreferencesName.ANONYMOUS_MODE,
                      preferenceType: bool,
                      defaultValue: prefs.anonymousMode,
                      builder: (context, value){
                        final bloc = BlocProvider.of<PreferencesCubit>(context);
                        return _EndSwitchOption(
                            title: "Mode Anonyme",
                            description: "Si activé, aucune information de votre profile ne sera accessible (sauf par le Staff)",
                            maxDescriptionWidth: maxDescriptionWidth,
                            value: value,
                            onPressed: () => _updatePrefsOnAnonymousModeChange(!value, bloc),
                            onChanged: (bool b) => _updatePrefsOnAnonymousModeChange(b, bloc)
                        );
                      }
                  ),

                  if(prefs.privacyMode == "custom" && !prefs.anonymousMode)...[

                    PrivacySinglePreferenceSwitch(
                        preferenceName: UserPreferencesName.SHOW_NICKNAME,
                        guiName: "mon pseudo", isGuiNamePlural: false,
                        defaultValue: prefs.showNickname,
                        maxDescriptionWidth: maxDescriptionWidth
                    ),

                    PrivacySinglePreferenceSwitch(
                        preferenceName: UserPreferencesName.SHOW_EMAIL,
                        guiName: "mon email", isGuiNamePlural: false,
                        defaultValue: prefs.showEmail,
                        maxDescriptionWidth: maxDescriptionWidth
                    ),

                    PrivacySinglePreferenceSwitch(
                        preferenceName: UserPreferencesName.SHOW_STATUS,
                        guiName: "mon status", isGuiNamePlural: false,
                        defaultValue: prefs.showStatus,
                        maxDescriptionWidth: maxDescriptionWidth
                    ),

                    PrivacySinglePreferenceSwitch(
                        preferenceName: UserPreferencesName.SHOW_PROPOSITIONS,
                        guiName: "mes propositions", isGuiNamePlural: true,
                        defaultValue: prefs.showPropositions,
                        maxDescriptionWidth: maxDescriptionWidth
                    )
                  ]
                ],
              );
            }
        )
    );
  }
}

class PrivacySinglePreferenceSwitch extends StatelessWidget {
  const PrivacySinglePreferenceSwitch({
    Key key,
    @required this.preferenceName,
    @required this.guiName,
    @required this.isGuiNamePlural,
    @required this.defaultValue,
    @required this.maxDescriptionWidth
  }) : super(key: key);

  final String preferenceName;
  final String guiName;
  final bool isGuiNamePlural;
  final bool defaultValue;
  final double maxDescriptionWidth;

  @override
  Widget build(BuildContext context) {
    return SinglePreferenceBlocBuilder (
        preferenceName: preferenceName,
        preferenceType: bool,
        defaultValue: defaultValue,
        builder: (context, value) {
          return _EndSwitchOption(
            title: "Afficher $guiName",
            description: "rend $guiName visible${isGuiNamePlural ? 's' : ''} par les utilisateurs lambdas",
            maxDescriptionWidth: maxDescriptionWidth,
            value: value,
            onPressed: () => BlocProvider.of<PreferencesCubit>(context)
                .updatePreference(preferenceName, !value),
            onChanged: (bool b) => BlocProvider.of<PreferencesCubit>(context)
                .updatePreference(preferenceName, b),
          );
        });
  }
}


class DialogRadioButton extends StatefulWidget {

  final String title;
  final Map<String, dynamic> radioButtonsWithValue;
  final dynamic currentValue;
  final Function onChanged;

  const DialogRadioButton({
      Key key,
      this.title,
      this.radioButtonsWithValue,
      this.currentValue,
      this.onChanged}) : super(key: key);

  @override
  State createState() => _DialogRadioButtonState();
}

class _DialogRadioButtonState extends State<DialogRadioButton>{

  dynamic currentValue;

  @override
  void initState() {
    super.initState();
    currentValue = widget.currentValue;
    print("current value: $currentValue");
  }

  @override
  Widget build(BuildContext context) {

    return SimpleDialog (
      title: Text(widget.title, style: Theme.of(context).textTheme.headline1),
      shape: RoundedRectangleBorder (
            borderRadius: BorderRadius.all(Radius.circular(15))
      ),
      children: widget.radioButtonsWithValue.keys.map((String name){
        return TextButton (
            onPressed: () => setState(() {
              widget.onChanged(widget.radioButtonsWithValue[name]);
              currentValue = widget.radioButtonsWithValue[name];
              Navigator.pop(context);
            }),
            child: Container (
              margin: EdgeInsets.only(left: 10, right: 10),
              child: Row (
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(name, style:Theme.of(context).textTheme.headline3),
                  Radio(value: widget.radioButtonsWithValue[name], groupValue: currentValue, onChanged: (dynamic v){
                    setState(() {
                      widget.onChanged(v);
                      currentValue = v;
                      Navigator.pop(context);
                    });
                  })
                ],
              )
            )
        );
      }).toList()
    );
  }
}
