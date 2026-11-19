export interface RecommendedMovie {
  id: string;
  title: string;
  description: string;
  releaseYear: number;
  durationMinutes: number;
  posterUrl: string | null;
  trailerUrl: string | null;
  director: string | null;
  imdbRating: number | null;
  genres: string[];
  recommendationReason: string;
  matchScore: number;
}
