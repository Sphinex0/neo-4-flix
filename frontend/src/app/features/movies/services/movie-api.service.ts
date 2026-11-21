import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { PageResponse } from '../../../core/models/page-response';
import { ResponseData } from '../../../core/models/response-data';
import { GenreResponse, MovieCreateRequest, MovieResponse } from '../models/movie.models';

export interface MovieSearchParams {
  title?: string;
  year?: number;
  genre?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: string;
}

@Injectable({ providedIn: 'root' })
export class MovieApiService {
  private readonly http = inject(HttpClient);

  getMovieById(id: string): Observable<ResponseData<MovieResponse>> {
    return this.http.get<ResponseData<MovieResponse>>(`${API_BASE_URL}/api/movies/${id}`);
  }

  searchMovies(params: MovieSearchParams): Observable<ResponseData<PageResponse<MovieResponse>>> {
    let httpParams = new HttpParams();
    if (params.title && params.title.trim()) {
      httpParams = httpParams.set('title', params.title.trim());
    }
    if (params.year) {
      httpParams = httpParams.set('year', params.year);
    }
    if (params.genre && params.genre !== 'All') {
      httpParams = httpParams.set('genre', params.genre);
    }
    if (params.page !== undefined) {
      httpParams = httpParams.set('page', params.page);
    }
    if (params.size !== undefined) {
      httpParams = httpParams.set('size', params.size);
    }
    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.direction) {
      httpParams = httpParams.set('direction', params.direction);
    }

    return this.http.get<ResponseData<PageResponse<MovieResponse>>>(`${API_BASE_URL}/api/movies`, {
      params: httpParams,
    });
  }

  getAllGenres(): Observable<ResponseData<GenreResponse[]>> {
    return this.http.get<ResponseData<GenreResponse[]>>(`${API_BASE_URL}/api/genres`);
  }

  createMovie(request: MovieCreateRequest): Observable<ResponseData<MovieResponse>> {
    return this.http.post<ResponseData<MovieResponse>>(`${API_BASE_URL}/api/movies`, request);
  }

  updateMovie(id: string, request: MovieCreateRequest): Observable<ResponseData<MovieResponse>> {
    return this.http.put<ResponseData<MovieResponse>>(`${API_BASE_URL}/api/movies/${id}`, request);
  }
}
