import 'package:brap_radio/cubits/playing_cubit.dart';
import 'package:brap_radio/cubits/playing_progress_cubit.dart';
import 'package:brap_radio/cubits/preferences_cubit.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:coast/coast.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class CustomListTile extends StatelessWidget {
  final String title;
  final String subtitle;
  final EdgeInsetsGeometry subtitlePadding;
  final Function onTap;
  final Widget leadingWidget;
  final IconData trailingIcon;

  CustomListTile({
    @required this.title,
    @required this.subtitle,
    this.subtitlePadding, this.onTap,
    this.leadingWidget, this.trailingIcon});

  @override
  Widget build(BuildContext context) {
    return Material (
        child: Container (
            margin: EdgeInsets.all(10),
            child: Ink (
                decoration: BoxDecoration (
                  color: Theme.of(context).accentColor,
                  borderRadius: BorderRadius.all(Radius.circular(20)),
                ),

                child: InkWell (
                    onTap: onTap,
                    borderRadius: BorderRadius.all(Radius.circular(20)),
                    child: Container (
                        margin: EdgeInsets.all(10),
                        padding: EdgeInsets.only(top: 5, bottom: 5),
                        child: Row (
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded (
                                flex: MediaQuery.of(context).orientation == Orientation.portrait ? 4 : 2,
                                child: Container (
                                    margin: EdgeInsets.all(5),
                                    child: Column (
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [leadingWidget],
                                    )
                                )
                            ),

                            Expanded (
                                flex: 15,
                                child: Container (
                                    padding: EdgeInsets.only(left: 10, right: 10),
                                    child: Column (
                                        crossAxisAlignment: CrossAxisAlignment.start,
                                        children: [
                                          Text(title, style: Theme.of(context).textTheme.headline5),
                                          SizedBox(height: 3,),
                                          Padding(padding: subtitlePadding ?? EdgeInsets.zero,
                                              child: Text(subtitle, style:Theme.of(context).textTheme.subtitle2))
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
                                      children: [IconTheme(data: Theme.of(context).accentIconTheme, child: Icon(trailingIcon))],
                                    )
                                )
                            )
                          ],
                        )
                    )
                )
            )
        )
    );
  }
}

class NoEndWidgetPreferenceButton extends StatelessWidget {
  final Function onPressed;
  final String title;
  final String description;


  NoEndWidgetPreferenceButton({
    @required this.title,
    @required this.description,
    @required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Container (
        width: double.infinity,
        child: TextButton (
            onPressed: onPressed,
            child: Align (
                alignment: Alignment.topLeft,
                child: Padding (
                    padding: EdgeInsets.all(10),
                    child: Column (
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(title, style: Theme.of(context).textTheme.headline2),
                        SizedBox(height: 5),
                        Text(description, style: Theme.of(context).textTheme.subtitle1)
                      ],
                    )
                )
            )
        )
    );
  }
}



class EndWidgetPreferenceButton extends StatelessWidget {

  final String title;
  final String description;
  final double maxDescriptionWidth;
  final Widget endWidget;
  final Function onPressed;

  EndWidgetPreferenceButton({
    @required this.title,
    @required this.description,
    @required  this.maxDescriptionWidth,
    @required this.endWidget,
    this.onPressed});

  @override
  Widget build(BuildContext context) {
    return TextButton(
        onPressed: onPressed,
        child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Padding(
                  padding: EdgeInsets.all(10),
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(title, style: Theme
                            .of(context)
                            .textTheme
                            .headline2),
                        SizedBox(height: 5),
                        Container(
                            width: maxDescriptionWidth,
                            child: Text(description, style: Theme
                                .of(context)
                                .textTheme
                                .subtitle1)
                        )
                      ]
                  )
              ),
              endWidget
            ]
        )
    );
  }
}

class PlayingCubitImageCrab extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Crab (
        tag: "player_cover_image",
        child: ClipRRect (
            borderRadius: BorderRadius.circular(10),
            child: BlocBuilder<PlayingCubit, PlayingState>(
              builder: (context, state){
                // todo cache management ?
                if(state is PlayingNow)
                  return Image.network(state.music.albumImageUrl);

                return Image.asset("images/logo.png");
              },
            )
        )
    );
  }

}


class PlayingCubitTextCrab extends StatelessWidget {
  final Function(BuildContext context, PlayingState state) textBuilder;
  final String crabTag;

  const PlayingCubitTextCrab(
      {@required this.textBuilder,
        @required this.crabTag,
        Key key}) : super(key: key);


  @override
  Widget build(BuildContext context) {
    return Crab (
      flightShuttleBuilder: (flightCtx, animation, flightDirection, fromCtx, toCtx) {
        if(fromCtx == null || toCtx == null)
          return null;

        final beginTextStyle = ((((fromCtx.widget as Crab).child as BlocBuilder).build(fromCtx, PlayingInitial()) as Container).child as Text).style;
        final endTextStyle = ((((toCtx.widget as Crab).child as BlocBuilder).build(fromCtx, PlayingInitial()) as Container).child as Text).style;
        final Text text = textBuilder(context, BlocProvider.of<PlayingCubit>(context).state);

        return DefaultTextStyleTransition (
            style: animation.drive(TextStyleTween(begin: beginTextStyle, end:endTextStyle)),
            child: Text(text.data, textAlign: text.textAlign)
        );
      },
      tag: crabTag,
      child: BlocBuilder<PlayingCubit, PlayingState>(
        builder: (context, state) => Container (
          // todo find solution to avoid animations bug between beaches on main pages
          width: MediaQuery.of(context).orientation == Orientation.portrait ? double.infinity : null,
          child: textBuilder(context, state)
        )
      ),
    );
  }
}

class PlayingProgressCrabLinearProgressIndicator extends StatelessWidget {

  const PlayingProgressCrabLinearProgressIndicator({
    this.valueColor,
    this.backgroundColor,
    this.borderColor,
    Key key
  }) : super(key: key);

  final Animation<Color> valueColor;
  final Color borderColor;
  final Color backgroundColor;

  @override
  Widget build(BuildContext context) {
    return Crab (
        tag: "CrabLinearProgressIndicator",
        child: Container (
            /*decoration: BoxDecoration (
              border: Border.all(color: borderColor ?? Theme.of(context).scaffoldBackgroundColor, width: 2.5),
              borderRadius: BorderRadius.circular(10),
            ),*/
            child: ClipRRect (
              borderRadius: BorderRadius.circular(10),
              child: BlocBuilder<PlayingProgressCubit, double>(
                builder: (context, state) {
                  return LinearProgressIndicator (
                    backgroundColor: backgroundColor ?? Color(Theme.of(context).accentColor.value - 0x1F1F1F),
                    minHeight: 7,
                    valueColor: valueColor ?? AlwaysStoppedAnimation<Color>(Theme.of(context).scaffoldBackgroundColor),
                    value: state,
                  );
                },
              ),
            )
        )
    );
  }
}

class SinglePreferenceBlocBuilder extends StatelessWidget {
  const SinglePreferenceBlocBuilder({
    @required this.preferenceName,
    @required this.preferenceType,
    @required this.defaultValue,
    @required this.builder,
    Key key
  }) : super(key: key);

  final String preferenceName;
  final Type preferenceType;
  final dynamic defaultValue;
  final Widget Function(BuildContext context, dynamic value) builder;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<PreferencesCubit, PreferencesState>(
      builder: (context, state){
        //return builder(context, state is PreferencesInitial ? defaultValue : (state as UpdatedPreference).newPreferencesValue);
        return builder(context, UserPreferences().getPreference(preferenceType, preferenceName) ?? defaultValue);
      },

      buildWhen: (previous, current) {
        return current is PreferencesInitial ||
            (current is UpdatedPreference && current.preferenceName == preferenceName);
      },
    );
  }
}

class MultiplePreferencesNotifier extends StatelessWidget {
  const MultiplePreferencesNotifier({
    Key key,
    @required this.preferencesNames,
    @required this.preferencesTypes,
    @required this.defaultPreferenceNameIndex,
    @required this.defaultValue,
    @required this.builder
  }) : super(key: key);

  final List<String> preferencesNames;
  final List<Type> preferencesTypes;
  final int defaultPreferenceNameIndex;
  final dynamic defaultValue;
  final Widget Function(BuildContext context, String preferenceName, dynamic value) builder;

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<PreferencesCubit, PreferencesState>(
      builder: (context, state){

        if(state is UpdatedPreference) // updated preference in prefs names list, see buildWhen
          return builder(context, state.preferenceName, state.newPreferencesValue);

        return builder(context, preferencesNames[defaultPreferenceNameIndex], defaultValue);
      },

      buildWhen: (previous, current){
        return current is PreferencesInitial || (current is UpdatedPreference && preferencesNames.contains(current.preferenceName));
      },
    );
  }
}

class ConnectionTextField extends StatefulWidget {

  const ConnectionTextField({
    Key key,
    @required this.labelText,
    this.obscureText,
    this.prefixIconData,
    this.suffixIconData,
    this.onPrefixIconTap,
    this.onSuffixIconTap,
    this.onChanged,
    this.initialValue,
  }) : super(key: key);

  final String labelText;
  final bool obscureText;

  final IconData prefixIconData, suffixIconData;
  final Function(ConnectionTextFieldState) onPrefixIconTap, onSuffixIconTap;
  final Function onChanged;
  final String initialValue;

  @override
  ConnectionTextFieldState createState() => ConnectionTextFieldState();

}

class ConnectionTextFieldState extends State<ConnectionTextField>{

  TextEditingController _controller;
  bool obscureText;
  IconData prefixIconData, suffixIconData;
  Function(ConnectionTextFieldState) onPrefixIconTap, onSuffixIconTap;
  Function(ConnectionTextFieldState, String) onChanged;
  Color accentColor;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.initialValue);
    obscureText = widget.obscureText;
    prefixIconData = widget.prefixIconData;
    suffixIconData = widget.suffixIconData;
    onPrefixIconTap = widget.onPrefixIconTap;
    onSuffixIconTap = widget.onSuffixIconTap;
    onChanged = widget.onChanged;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  String get text => _controller.text;

  set text(String val) => _controller.text = val;

  void showError(){
    setState(() {
      accentColor = Colors.red;
    });
  }

  @override
  Widget build(BuildContext context) {

    accentColor ??= Theme.of(context).accentColor;

    return TextField (
      controller: _controller,
      style: Theme.of(context).textTheme.bodyText2
          .copyWith(color: accentColor),
      obscureText: obscureText ?? false,

      decoration: InputDecoration (
          labelText: widget.labelText,
          filled: true,
          prefixIcon: GestureDetector (
              onTap: onPrefixIconTap == null ? null : () =>  setState(() => onPrefixIconTap(this)),
              child: Icon (prefixIconData, color: accentColor)
          ),

          suffixIcon: GestureDetector (
            onTap: onSuffixIconTap == null ? null : () =>  setState(() => onSuffixIconTap(this)),
            child: Icon (suffixIconData, color: accentColor),
          ),

          enabledBorder: UnderlineInputBorder(
            borderRadius: BorderRadius.circular(10),
            borderSide: BorderSide.none,
          ),

          focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: BorderSide(color: accentColor)
          ),

          labelStyle: Theme.of(context).textTheme.bodyText2.copyWith(
              color: accentColor
          ),

          focusColor: accentColor
      ),

      onChanged: (str) {
        setState(() {
          if(onChanged != null)
            onChanged(this, str);

          if(accentColor == Colors.red)
            accentColor = Theme.of(context).accentColor;
        });
      },

    );
  }
}

class ConnectionTextButton extends StatelessWidget {
  const ConnectionTextButton({
    Key key,
    @required this.text,
    @required this.withBorder,
    this.onPressed,
    this.textAndBorderColor,
    this.fillColor,
  }) : super(key: key);

  final String text;
  final bool withBorder;
  final Function onPressed;
  final Color textAndBorderColor, fillColor;


  @override
  Widget build(BuildContext context) {

    final bFillColor = fillColor ?? withBorder
        ? Theme.of(context).scaffoldBackgroundColor
        : Theme.of(context).accentColor;

    final bTextAndBorderColor = textAndBorderColor ?? withBorder
        ? Theme.of(context).accentColor
        : Theme.of(context).scaffoldBackgroundColor;

    return Material (
        child: Ink (
            decoration: BoxDecoration (
              color: bFillColor,

              border: withBorder
                  ? Border.all (
                  color: bTextAndBorderColor,
                  width: 2)
                  : Border.fromBorderSide(BorderSide.none),

              borderRadius: BorderRadius.circular(15),
            ),


            child: InkWell (
                onTap: (){
                  FocusScopeNode currentFocus = FocusScope.of(context);
                  if (!currentFocus.hasPrimaryFocus && currentFocus.focusedChild != null) {
                    currentFocus.focusedChild.unfocus();
                  }

                  if(onPressed != null)
                    onPressed();
                },
                splashColor: bTextAndBorderColor,
                borderRadius: BorderRadius.circular(15),
                child: Padding (
                    padding: EdgeInsets.symmetric(vertical: withBorder ? 27 : 29),
                    child: Center (
                        child: Text(text, style: Theme.of(context).textTheme.headline2.copyWith(
                            color: bTextAndBorderColor
                        ))
                    )
                )
            )
        )
    );
  }
}
