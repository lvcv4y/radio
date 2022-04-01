import 'music_entity.dart';
import 'event_entity.dart';
import 'user_entity.dart';
import 'sanction_entity.dart';
import 'package:intl/intl.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_serializers/request.g.dart';

@JsonSerializable(explicitToJson: true)
class Request {
  static final DateFormat format = DateFormat("yyyy-MM-dd HH:mm:ss");
  final String type;
  String errorMessage;
  bool isError;

  List<int> intArgs;
  List<String> stringArgs;
  List<MusicEntity> musicArgs;
  List<EventEntity> eventArgs;
  List<UserEntity> userArgs;
  List<SanctionEntity> sanctionArgs;

  Request(this.type, this.isError, this.errorMessage);

  Request.notError(this.type) {
    isError = false;
    this.errorMessage = null;
  }

  Request.error(this.type, this.errorMessage): isError = true;

  factory Request.fromJson(Map<String, dynamic> data) => _$RequestFromJson(data);

  Map<String, dynamic> toJson() => _$RequestToJson(this);

  void addIntArgs(List<int> args){
    intArgs ??= [];
    intArgs.addAll(args);
  }

  void addStringArgs(List<String> args){
    stringArgs ??= [];
    stringArgs.addAll(args);
  }

  void addMusicArgs(List<MusicEntity> args){
    musicArgs ??= [];
    musicArgs.addAll(args);
  }

  void addUserArgs(List<UserEntity> args){
    userArgs ??= [];
    userArgs.addAll(args);
  }

  void addEventArgs(List<EventEntity> args){
    eventArgs ??= [];
    eventArgs.addAll(args);
  }

  void addSantionArgs(List<SanctionEntity> args){
    sanctionArgs ??= [];
    sanctionArgs.addAll(args);
  }

  @override
  String toString() => "{Request : TYPE=$type; IS_ERROR=$isError, errorMsg=$errorMessage}";
}