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
