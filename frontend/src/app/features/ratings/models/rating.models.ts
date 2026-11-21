export interface RatingResponse {
  movieId: string;
  score: number;
  createdAt: string;
  updatedAt: string;
}

export interface MyRatingResponse {
  movieId: string;
  score: number;
  createdAt: string;
  updatedAt: string;
}

export interface MovieRatingResponse {
  userId: string;
  score: number;
  createdAt: string;
  updatedAt: string;
}

export interface RatingRequest {
  movieId: string;
  score: number;
}

export interface ChangeRatingRequest {
  score: number;
}

export interface UserRatingStatsResponse {
  userId: string;
  ratingsCount: number;
  averageRating: number | null;
}

export interface SharedRatingItem {
  movieId: string;
  myScore: number;
  theirScore: number;
}

export interface OtherUserRatingItem {
  movieId: string;
  theirScore: number;
  ratedAt: string;
}

export interface RatingComparisonResponse {
  bothRatedCount: number;
  tasteMatchPercent: number | null;
  bothLoved: SharedRatingItem[];
  recentRatings: OtherUserRatingItem[];
}
