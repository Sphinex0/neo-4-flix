import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/http/api-config';
import { ResponseData } from '../../../core/models/response-data';
import {
  ChangeRatingRequest,
  MovieRatingResponse,
  MyRatingResponse,
  RatingComparisonResponse,
  RatingRequest,
  RatingResponse,
  UserRatingStatsResponse,
} from '../models/rating.models';

@Injectable({ providedIn: 'root' })
export class RatingApiService {
  private readonly http = inject(HttpClient);

  rateMovie(request: RatingRequest): Observable<ResponseData<RatingResponse>> {
    return this.http.post<ResponseData<RatingResponse>>(`${API_BASE_URL}/api/ratings`, request);
  }

  changeRating(movieId: string, request: ChangeRatingRequest): Observable<ResponseData<RatingResponse>> {
    return this.http.put<ResponseData<RatingResponse>>(`${API_BASE_URL}/api/ratings/${movieId}`, request);
  }

  deleteRating(movieId: string): Observable<ResponseData<string>> {
    return this.http.delete<ResponseData<string>>(`${API_BASE_URL}/api/ratings/${movieId}`);
  }

  getMyRatings(): Observable<ResponseData<MyRatingResponse[]>> {
    return this.http.get<ResponseData<MyRatingResponse[]>>(`${API_BASE_URL}/api/ratings/me`);
  }

  getMovieRatings(movieId: string): Observable<ResponseData<MovieRatingResponse[]>> {
    return this.http.get<ResponseData<MovieRatingResponse[]>>(`${API_BASE_URL}/api/ratings/movie/${movieId}`);
  }

  getUserRatingStats(userId: string): Observable<ResponseData<UserRatingStatsResponse>> {
    return this.http.get<ResponseData<UserRatingStatsResponse>>(`${API_BASE_URL}/api/ratings/user/${userId}/stats`);
  }

  getComparison(userId: string): Observable<ResponseData<RatingComparisonResponse>> {
    return this.http.get<ResponseData<RatingComparisonResponse>>(`${API_BASE_URL}/api/ratings/user/${userId}/compare`);
  }
}
