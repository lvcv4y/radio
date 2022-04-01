import 'package:brap_radio/models/database/database_api.dart';
import 'package:brap_radio/models/database/history_database_api.dart';
import 'package:brap_radio/models/service_api.dart';
import 'package:brap_radio/models/themes.dart';
import 'package:brap_radio/models/user_preferences.dart';
import 'package:brap_radio/models/widgets.dart';
import 'package:brap_radio/views/login.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get.dart';
import 'package:loader_overlay/loader_overlay.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await UserPreferences().init();
  await DatabaseAPI().init();
  await HistoryDatabaseAPI().resetDatabase();
  ServiceApi.init();
  runApp(App());
}

class App extends StatelessWidget {

  @override
  Widget build(BuildContext context) {

    return MultiBlocProvider (
        providers: [
          BlocProvider (
              lazy: false,
              create: (_) => ServiceApi.eventCubit
          ),
          BlocProvider (
              lazy: false,
              create: (_) => ServiceApi.historyCubit
          ),

          BlocProvider (
              lazy: false,
              create: (_) => ServiceApi.playingCubit,
          ),

          BlocProvider (
              create: (_) => ServiceApi.playingProgressCubit
          ),

          BlocProvider (
              lazy: false,
              create: (_) => ServiceApi.preferencesCubit
          ),

          BlocProvider (
              lazy: false,
              create: (_) => ServiceApi.userCubit
          ),

          BlocProvider (
              create: (_) => ServiceApi.chatCubit
          ),

          BlocProvider (
              create: (_) => ServiceApi.muteCubit
          ),

          BlocProvider (
              create: (_) => ServiceApi.voteCubit
          ),

          BlocProvider (
              create: (_) => ServiceApi.connectionCubit
          ),

          BlocProvider (
              create: (_) => ServiceApi.voteProgressCubit
          ),
        ],

        child: GestureDetector (
            onTap: (){
              FocusScopeNode currentFocus = FocusScope.of(context);
              if (!currentFocus.hasPrimaryFocus && currentFocus.focusedChild != null) {
                currentFocus.focusedChild.unfocus();
              }
            },

            child: SinglePreferenceBlocBuilder(
                preferenceName: UserPreferencesName.THEME,
                preferenceType: String,
                defaultValue: UserPreferences().theme,
                builder: (context, value) {


                  return GetMaterialApp (
                      title: 'B-Rap Radio',
                      theme: Themes.getThemeByName(value),
                      debugShowCheckedModeBanner: false,
                      home: LoaderOverlay (
                          child: LoginPage()
                      )
                  );
                }
            )
        )
    );
  }
}
