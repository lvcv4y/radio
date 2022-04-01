import 'package:brap_radio/models/animated_widgets.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:flutter/material.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({Key key}) : super(key: key);

  @override
  _RegisterPageState createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {

  TextEditingController _nicknameController = TextEditingController();
  TextEditingController _emailController = TextEditingController();
  TextEditingController _passwordController = TextEditingController();
  TextEditingController _passwordConfirmController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final openingKeyboard = MediaQuery.of(context).viewInsets.bottom > 0;
    final isOrientationPortrait = MediaQuery.of(context).orientation == Orientation.portrait;

    return Scaffold (
        // resizeToAvoidBottomInset: false,
        body: Stack (
          children: [
            Container (
              height: size.height - 200,
              color: Theme.of(context).accentColor,
            ),

            AnimatedPositioned (
              top: openingKeyboard || !isOrientationPortrait ? -size.height / 3.3 : 0.0,
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
                      Text( "S'enregister",
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
                        ? openingKeyboard ? 10 : 40
                        : 20,
                    horizontal: 20),
                child: Column (
                  mainAxisAlignment: MainAxisAlignment.end,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    ConnectionTextField(
                      labelText: "nom d'utilisateur",
                      prefixIconData: Icons.account_circle_outlined,
                    ),

                    SizedBox(height: 5),

                    ConnectionTextField(
                      labelText: "Email",
                      prefixIconData: Icons.email_outlined,
                    ),

                    SizedBox(height: 5),

                    ConnectionTextField(
                      labelText: "Mot de passe",
                      prefixIconData: Icons.lock_outlined,
                      suffixIconData: Icons.visibility_off,
                      obscureText: true,
                      onSuffixIconTap: (state) {
                        state.obscureText = !state.obscureText;
                        state.suffixIconData = state.obscureText ? Icons.visibility_off : Icons.visibility;
                      },
                    ),

                    SizedBox(height: 5),
                    ConnectionTextField(
                      labelText: "Confirmer votre mot de passe",
                      prefixIconData: Icons.lock_outlined,
                      suffixIconData: Icons.visibility_off,
                      obscureText: true,
                      onSuffixIconTap: (state) {
                        state.obscureText = !state.obscureText;
                        state.suffixIconData = state.obscureText ? Icons.visibility_off : Icons.visibility;
                      },
                    ),

                    SizedBox(height: 10),

                    ConnectionTextButton(text: "S'enregister", withBorder: true, onPressed: () => null),
                    if(!openingKeyboard)...[
                      Row (
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          TextButton (
                              onPressed: () => Navigator.of(context).pop(),
                              child: Text("J'ai déjà un compte")
                          )
                        ],
                      )
                    ]
                  ],
                )
            )
          ],
        )
    );
  }
}
