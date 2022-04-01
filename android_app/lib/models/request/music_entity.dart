
import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_serializers/music_entity.g.dart';

@JsonSerializable()
class MusicEntity extends Equatable {
  final String title, authors, albumName, albumImageUrl;
  final int duration, totalVoteNumber, positiveVoteNumber, size, playedPart;

  MusicEntity(this.title, this.albumName, this.authors, this.albumImageUrl, this.duration,
      this.totalVoteNumber, this.positiveVoteNumber, this.size, this.playedPart);

  factory MusicEntity.fromJson(Map<String, dynamic> data) => _$MusicEntityFromJson(data);

  Map<String, dynamic> toJson() => _$MusicEntityToJson(this);

  List<Object> get props => [title, authors, albumImageUrl, totalVoteNumber, positiveVoteNumber];
}