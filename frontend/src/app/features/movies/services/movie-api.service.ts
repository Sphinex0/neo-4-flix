import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { MovieResponse } from '../models/movie.models';

@Injectable({ providedIn: 'root' })
export class MovieApiService {
  private readonly http = inject(HttpClient);

  getMovieById(id: string): Observable<ResponseData<MovieResponse>> {
    return this.http.get<ResponseData<MovieResponse>>(`${API_BASE_URL}/api/movies/${id}`);
  }
}
