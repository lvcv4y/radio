import 'package:brap_radio/models/request/user_entity.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:flutter/material.dart';


Map<String, Color> _getStatusColorMapFromColors(Color admin, Color mod, Color self, Color other){
  return <String, Color>{
    Status.ADMIN: admin,
    Status.MOD: mod,
    Status.SELF: self,
    Status.OTHER: other,
  };
}


class Themes {
  static final Map<String, ThemeData> themes = {
    "default": ThemeData.light().copyWith (
        textTheme: TextTheme (
          headline1: TextStyle(color: Color(0xff333333), fontSize: 20, fontWeight: FontWeight.w600),
          headline2: TextStyle(color: Color(0xff333333), fontSize: 18, fontWeight: FontWeight.w600),
          headline3: TextStyle(color: Color(0xff333333), fontSize: 16, fontWeight: FontWeight.w600),
          headline4: TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.w600), // ListTile titles
          headline5: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w600),
          headline6: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600),
          subtitle1: TextStyle(color: Colors.black26, fontSize: 14, fontWeight: FontWeight.w500),
          subtitle2: TextStyle(color: Colors.white70, fontSize: 14, fontWeight: FontWeight.w500), // ListTile subtitle
          bodyText1: TextStyle(color: Colors.black, fontSize: 16),
          bodyText2: TextStyle(color: Colors.black, fontSize: 14),
          caption: TextStyle(color: Colors.white24, fontSize: 14),
        ),
        snackBarTheme: SnackBarThemeData (
          backgroundColor: Colors.white,
          contentTextStyle: TextStyle(color: Colors.black, fontSize: 14, fontWeight: FontWeight.w500),
        ),
        accentIconTheme: IconThemeData(color: Colors.white) // ListTile icon theme
    ),

    "dark": ThemeData.dark().copyWith (
        textTheme: TextTheme(
            headline1: TextStyle(color: Color(0xffcccccc), fontSize: 22, fontWeight: FontWeight.w600),
            headline2: TextStyle(color: Color(0xffcccccc), fontSize: 20, fontWeight: FontWeight.w600),
            headline3: TextStyle(color: Color(0xffcccccc), fontSize: 18, fontWeight: FontWeight.w600),
            subtitle1: TextStyle(color: Colors.white24, fontSize: 14),
            subtitle2: TextStyle(color: Colors.white24, fontSize: 12),
            bodyText1: TextStyle(color: Colors.white, fontSize: 16),
            bodyText2: TextStyle(color: Colors.white, fontSize: 14),
        ),
        snackBarTheme: SnackBarThemeData (
          backgroundColor: Colors.black54,
          contentTextStyle: TextStyle(color: Colors.white24, fontSize: 14, fontWeight: FontWeight.w500),
      ),
    )
  };

  static getThemeByName(String name) => themes[name];

  static get currentTheme => getThemeByName(UserPreferences().theme);

  /*
  chat's status colors (linked to themes)
   */

  static final Map<String, Map<String, Color>> chatStatusColorByThemes = {

    "default": _getStatusColorMapFromColors(
        Colors.red,
        Colors.lightGreenAccent,
        Colors.black,
        themes["default"].textTheme.bodyText1.color
    ),

    "dark": _getStatusColorMapFromColors(
        Colors.red,
        Colors.lightGreenAccent,
        Colors.black,
        themes["default"].textTheme.bodyText1.color
    )
  };
}