// GENERATED CODE - DO NOT MODIFY BY HAND

part of '../music_entity.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MusicEntity _$MusicEntityFromJson(Map<String, dynamic> json) {
  return MusicEntity(
    json['title'] as String,
    json['album_name'] as String,
    json['authors'] as String,
    json['album_image_url'] as String,
    json['duration'] as int,
    json['total_vote_num'] as int,
    json['positive_vote_num'] as int,
    json['size'] as int,
    json['played_part'] as int,
  );
}

Map<String, dynamic> _$MusicEntityToJson(MusicEntity instance) =>
    <String, dynamic>{
      'title': instance.title,
      'authors': instance.authors,
      'album_name': instance.albumName,
      'album_image_url': instance.albumImageUrl,
      'duration': instance.duration,
      'total_vote_num': instance.totalVoteNumber,
      'positive_vote_num': instance.positiveVoteNumber,
      'size': instance.size,
      'played_part': instance.playedPart,
    };
