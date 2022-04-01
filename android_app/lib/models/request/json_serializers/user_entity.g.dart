// GENERATED CODE - DO NOT MODIFY BY HAND

part of '../user_entity.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserEntity _$UserEntityFromJson(Map<String, dynamic> json) {
  return UserEntity(
    json['id'] as int,
    json['email'] as String,
    json['nickname'] as String,
    (json['status'] as List)?.map((e) => e as String)?.toList(),
    json['credits'] as int,
  );
}

Map<String, dynamic> _$UserEntityToJson(UserEntity instance) =>
    <String, dynamic>{
      'id': instance.id,
      'credits': instance.credits,
      'email': instance.email,
      'nickname': instance.nickname,
      'status': instance.status,
    };
