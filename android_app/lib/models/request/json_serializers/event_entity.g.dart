// GENERATED CODE - DO NOT MODIFY BY HAND

part of '../event_entity.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventEntity _$EventEntityFromJson(Map<String, dynamic> json) {
  return EventEntity(
    json['title'] as String,
    json['description'] as String,
    json['start_at'] == null ? null : Request.format.parse(json['start_at'] as String),
    json['end_at'] == null ? null : Request.format.parse(json['end_at'] as String),
  );
}

Map<String, dynamic> _$EventEntityToJson(EventEntity instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': instance.description,
      'start_at': instance.startAt == null ? null: Request.format.format(instance.startAt),
      'end_at': instance.endAt == null ? null: Request.format.format(instance.endAt),
    };
