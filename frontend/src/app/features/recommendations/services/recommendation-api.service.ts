import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import { RecommendedMovie } from '../models/recommendation.models';

@Injectable({ providedIn: 'root' })
export class RecommendationApiService {
  private readonly http = inject(HttpClient);

  getRecommendations(genre?: string | null, year?: number | null, limit: number = 12): Observable<ResponseData<RecommendedMovie[]>> {
    let params = new HttpParams().set('limit', limit);
    if (genre && genre !== 'All') {
      params = params.set('genre', genre);
    }
    if (year) {
      params = params.set('year', year);
    }
    return this.http.get<ResponseData<RecommendedMovie[]>>(`${API_BASE_URL}/api/recommendations`, { params });
  }

  getTrending(genre?: string | null, year?: number | null, limit: number = 12): Observable<ResponseData<RecommendedMovie[]>> {
    let params = new HttpParams().set('limit', limit);
    if (genre && genre !== 'All') {
      params = params.set('genre', genre);
    }
    if (year) {
      params = params.set('year', year);
    }
    return this.http.get<ResponseData<RecommendedMovie[]>>(`${API_BASE_URL}/api/recommendations/trending`, { params });
  }

  getSimilarMovies(movieId: string, limit: number = 6): Observable<ResponseData<RecommendedMovie[]>> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<ResponseData<RecommendedMovie[]>>(`${API_BASE_URL}/api/recommendations/movies/${movieId}`, { params });
  }
}
