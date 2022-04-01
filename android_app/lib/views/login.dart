import 'package:brap_radio/cubits/connection_cubit.dart';
import 'package:brap_radio/cubits/preferences_cubit.dart';
import 'package:brap_radio/models/animated_widgets.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:brap_radio/views/main_screen_views.dart';
import 'package:brap_radio/views/register.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get.dart';
import 'package:loader_overlay/loader_overlay.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({Key key}) : super(key: key);

  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {

  GlobalKey<ConnectionTextFieldState> _idKey = GlobalKey<ConnectionTextFieldState>();
  GlobalKey<ConnectionTextFieldState> _passKey = GlobalKey<ConnectionTextFieldState>();

  String _getLoginErrorDesc(String error){
    switch(error){
      case LoginErrorNames.BAD_PASSWORD:
        return "Le mot de passe est erroné";
      case LoginErrorNames.BAD_ID:
        return "L'identifiant (email ou nom d'utilisateur) est erroné";
      case LoginErrorNames.MISSING_ARGS:
        return "Tous les champs du formulaires doivent etre remplis"; // shouldn't happen
      case LoginErrorNames.BANNED:
        return "Vous avez été banni de notre radio ! :("; // todo "reclamation discord twitter blabla"
      case LoginErrorNames.SERVER_ERROR:
        return "Une erreur est apparue du côté du serveur, essayez de vous reconnectez"; // todo log / bug form discord
      default:
        return "Erreur inconnue";
    }
  }


  String _getConnectionErrorDesc(String error){
    switch(error){
      case ConnectionErrorNames.NO_INTERNET_FOUND:
        return "Aucune connexion internet active n'a pu être trouvée";
      default:
        return "Erreur inconnue";
    }
  }

  @override
  Widget build(BuildContext context) {

    final prefs = UserPreferences();

    final size = MediaQuery.of(context).size;
    final openingKeyboard = MediaQuery.of(context).viewInsets.bottom > 0;
    final isOrientationPortrait = MediaQuery.of(context).orientation == Orientation.portrait;
    final rememberMeOnAndPreferencesSet = prefs.rememberMe && prefs.nickname != "NICKNAME"
        && prefs.email != "EMAIL";


    return Scaffold (
        body: BlocListener<ConnectionCubit, SocketConnectionState>(

          listener: (context, state) {
            switch(state.runtimeType){

              case LoggedIn:
                print("[LOGIN PAGE] logged in spotted !");

                context.loaderOverlay.hide();
                //Navigator.push(context, MaterialPageRoute(builder: (_) => MainPage()));
                Get.off(() => MainPage());
                break;

              case LoggingIn:
              case Connecting:
                context.loaderOverlay.show(
                  widget: Center (
                    child: Column (
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Padding (
                          padding: const EdgeInsets.symmetric(horizontal: 20.0),
                          child: CircularProgressIndicator(),
                        ),
                        SizedBox(height: 12),
                        Text ('${state is Connecting
                            ? "Connexion" : "Authentification"} en cours...',
                        ),
                      ],
                    ),
                  )
                );
                break;

              case LoginError:
                if((state as LoginError).error == LoginErrorNames.BAD_ID)
                  _idKey.currentState.showError();
                else if((state as LoginError).error == LoginErrorNames.BAD_PASSWORD)
                  _passKey.currentState.showError();

                continue showError;

              showError:
              case ConnectionError:
                context.loaderOverlay.hide();
                showDialog (
                    context: context,
                    builder: (_) => ErrorDialog(
                        title: "Erreur",
                        description: state is LoginError
                          ? _getLoginErrorDesc(state.error)
                          : _getConnectionErrorDesc((state as ConnectionError).error)
                    )
                );
                break;

              case Connected:
                BlocProvider.of<ConnectionCubit>(context)
                    .login(_idKey.currentState.text, _passKey.currentState.text);
                break;
            }
          },

          child: Stack (
            children: [

              Container (
                height: size.height - 200 <= 0 ? 100 : size.height - 200,
                color: Theme.of(context).accentColor,
              ),

              AnimatedPositioned (
                top: openingKeyboard || !isOrientationPortrait ? -size.height / 3.7 : 0.0,
                child: WaveAnimationWidget (
                    size: size,
                    yOffset: size.height / 3.0,
                    color: Theme.of(context).scaffoldBackgroundColor
                ),
                duration: const Duration(milliseconds: 400),
                curve: Curves.easeOutQuint,
              ),

              Padding (
                  padding: EdgeInsets.only(
                      top: isOrientationPortrait ? 100 : 50
                  ),
                  child: Row (
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text( "Se connecter",
                            style: Theme.of(context).textTheme.headline4.copyWith(
                                fontSize: 40.0,
                                color: Theme.of(context).scaffoldBackgroundColor,
                                fontWeight: FontWeight.bold
                            )
                        )
                      ]
                  )
              ),

              Padding (
                  padding: EdgeInsets.symmetric(
                      vertical: isOrientationPortrait
                          ? openingKeyboard ? 30 : 100
                          : 20,
                      horizontal: 20),
                  child: Column (
                    mainAxisAlignment: MainAxisAlignment.end,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      ConnectionTextField (
                        key: _idKey,
                        initialValue: rememberMeOnAndPreferencesSet ? prefs.nickname : null,
                        labelText: "Email ou nom d'utilisateur",
                        prefixIconData: Icons.account_circle_outlined,
                      ),
                      SizedBox(height: 10),
                      ConnectionTextField (
                        key: _passKey,
                        initialValue: null,
                        labelText: "Mot de passe",
                        prefixIconData: Icons.lock_outlined,
                        suffixIconData: Icons.visibility_off,
                        obscureText: true,
                        onSuffixIconTap: (state) {
                          state.obscureText = !state.obscureText;
                          state.suffixIconData = state.obscureText
                              ? Icons.visibility_off
                              : Icons.visibility;
                        },

                      ),

                      SizedBox(height:10),

                      Row (
                        children: [
                          SinglePreferenceBlocBuilder(
                              preferenceName: UserPreferencesName.REMEMBER_ME,
                              preferenceType: bool,
                              defaultValue: prefs.rememberMe,
                              builder: (context, value) {
                                return Checkbox (
                                  value: value,
                                  onChanged: (b) => BlocProvider.of<PreferencesCubit>(context)
                                      .updatePreference(UserPreferencesName.REMEMBER_ME, b),
                                );
                              }
                          ),

                          Text("Se souvenir de moi", style: Theme.of(context).textTheme.bodyText2)
                        ],
                      ),
                      SizedBox(height: 10),
                      ConnectionTextButton (
                          text: "Se connecter",
                          withBorder: false,
                          onPressed: (){
                            final idText = _idKey.currentState.text;
                            String passText = _passKey.currentState.text;
                            if(idText.isEmpty || passText.isEmpty){
                              showDialog(context: context, builder: (_) => ErrorDialog (
                                  title: "Erreur",
                                  description: "Vous devez remplir tous les champs"
                                      " du formulaire pour pouvoir vous connecter")
                              );

                              if(idText.isEmpty)
                                _idKey.currentState.showError();

                              if(passText.isEmpty)
                                _passKey.currentState.showError();
                              return;
                            }

                            final currentState = BlocProvider.of<ConnectionCubit>(context).state;

                            if(currentState is LoginError || currentState is Connected) // already connected, but not logged in
                              BlocProvider.of<ConnectionCubit>(context).login(idText, passText);
                            else // not connected, not logged in
                              BlocProvider.of<ConnectionCubit>(context).connect();
                          }),
                      Row (
                        // change to spacebetween when adding "forgot password ?"
                        mainAxisAlignment: MainAxisAlignment.end,

                        children: [
                          /*TextButton (
                            onPressed: () => null,
                            child: Text("Mot de passe oublié ?")
                        ),*/
                          TextButton (
                              onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (_) => RegisterPage())),
                              child: Text("Créer un compte")
                          )
                        ],
                      )
                    ],
                  )
              )
            ],
          )
        )
    );
  }
}

class ErrorDialog extends StatelessWidget {

  const ErrorDialog({
    Key key,
    @required this.title,
    @required this.description,
  }) : super(key: key);

  final String title, description;

  @override
  Widget build(BuildContext context) {
    return AlertDialog (
      title: Text(title),
      titleTextStyle: Theme.of(context).textTheme.headline2,

      content: Text(description),
      contentTextStyle: Theme.of(context).textTheme.bodyText1.copyWith(
        fontWeight: FontWeight.w400
      ),

      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),

      actions: [
        TextButton (
          child: Text("OK",
              style: Theme.of(context).textTheme.bodyText1.copyWith(
                  color: Theme.of(context).accentColor
              )),
          onPressed: () => Navigator.of(context).pop(),
        )
      ],
    );
  }
}