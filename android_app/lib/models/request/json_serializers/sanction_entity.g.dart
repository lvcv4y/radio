// GENERATED CODE - DO NOT MODIFY BY HAND

part of '../sanction_entity.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SanctionEntity _$SanctionEntityFromJson(Map<String, dynamic> json) {
  return SanctionEntity(
    json['id'] as int,
    json['type'] as String,
    json['description'] as String,
    json['user'] == null
        ? null
        : UserEntity.fromJson(json['user'] as Map<String, dynamic>),
    json['start_at'] == null ? null : Request.format.parse(json['start_at'] as String),
    json['end_at'] == null ? null : Request.format.parse(json['end_at'] as String),
  );
}

Map<String, dynamic> _$SanctionEntityToJson(SanctionEntity instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': instance.type,
      'description': instance.description,
      'user': instance.user?.toJson(),
      'start_at': instance.startAt == null ? null : Request.format.format(instance.startAt),
      'end_at': instance.endAt == null ? null : Request.format.format(instance.endAt),
    };
