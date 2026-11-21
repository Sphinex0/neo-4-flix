export interface GenreResponse {
  id: string;
  name: string;
}

export interface MovieCreateRequest {
  title: string;
  description: string;
  releaseYear: number;
  durationMinutes: number;
  posterUrl: string | null;
  trailerUrl: string | null;
  director: string | null;
  genres: string[];
}

export interface MovieResponse {
  id: string;
  title: string;
  description: string;
  releaseYear: number;
  durationMinutes: number;
  posterUrl: string | null;
  trailerUrl: string | null;
  director: string | null;
  imdbRating: number | null;
  genres: GenreResponse[];
  createdAt: string;
  updatedAt: string;
}
