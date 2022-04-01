// GENERATED CODE - DO NOT MODIFY BY HAND

part of '../request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Request _$RequestFromJson(Map<String, dynamic> json) {
  return Request(
    json['type'] as String,
    json['is_error'] as bool,
    json['error_msg'] as String,
  )
    ..intArgs = (json['int_args'] as List)?.map((e) => e as int)?.toList()
    ..stringArgs =
        (json['string_args'] as List)?.map((e) => e as String)?.toList()
    ..musicArgs = (json['music_args'] as List)
        ?.map((e) =>
            e == null ? null : MusicEntity.fromJson(e as Map<String, dynamic>))
        ?.toList()
    ..eventArgs = (json['event_args'] as List)
        ?.map((e) =>
            e == null ? null : EventEntity.fromJson(e as Map<String, dynamic>))
        ?.toList()
    ..userArgs = (json['user_entity_args'] as List)
        ?.map((e) =>
            e == null ? null : UserEntity.fromJson(e as Map<String, dynamic>))
        ?.toList()
    ..sanctionArgs = (json['sanction_entity_args'] as List)
        ?.map((e) => e == null
            ? null
            : SanctionEntity.fromJson(e as Map<String, dynamic>))
        ?.toList();
}

Map<String, dynamic> _$RequestToJson(Request instance) => <String, dynamic>{
      'type': instance.type,
      'error_msg': instance.errorMessage,
      'is_error': instance.isError,
      'int_args': instance.intArgs,
      'string_args': instance.stringArgs,
      'music_args': instance.musicArgs?.map((e) => e?.toJson())?.toList(),
      'event_args': instance.eventArgs?.map((e) => e?.toJson())?.toList(),
      'user_entity_args': instance.userArgs?.map((e) => e?.toJson())?.toList(),
      'sanction_entity_args': instance.sanctionArgs?.map((e) => e?.toJson())?.toList(),
    };
