import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { PageResponse } from '../../../core/models/page-response';
import { ResponseData } from '../../../core/models/response-data';
import { MovieResponse } from '../../movies/models/movie.models';

@Injectable({ providedIn: 'root' })
export class WatchlistApiService {
  private readonly http = inject(HttpClient);

  getWatchlist(page: number, size: number): Observable<ResponseData<PageResponse<MovieResponse>>> {
    return this.http.get<ResponseData<PageResponse<MovieResponse>>>(`${API_BASE_URL}/api/movies/watchlist`, {
      params: { page, size },
    });
  }

  addToWatchlist(movieId: string): Observable<ResponseData<string>> {
    return this.http.post<ResponseData<string>>(`${API_BASE_URL}/api/movies/${movieId}/watchlist`, {});
  }

  removeFromWatchlist(movieId: string): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/movies/${movieId}/watchlist`);
  }
}
