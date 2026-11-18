import { Component, OnInit, inject, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { Navbar } from '../../../../shared/navbar/navbar';
import { MovieResponse } from '../../../movies/models/movie.models';
import { WatchlistApiService } from '../../services/watchlist-api.service';

interface WatchlistCard {
  id: string;
  title: string;
  meta: string;
  bg: string;
}

interface RatedCard {
  title: string;
  meta: string;
  mine: number;
  bg: string;
}

type Tab = 'watch' | 'ratings';

const FALLBACK_GRADIENTS = [
  'linear-gradient(160deg,#6A4423,#120B06)',
  'linear-gradient(160deg,#454C78,#0C0E18)',
  'linear-gradient(160deg,#6E3560,#150A13)',
  'linear-gradient(160deg,#5A4128,#0E0A06)',
  'linear-gradient(160deg,#256270,#08161A)',
];

@Component({
  selector: 'app-watchlist',
  imports: [Navbar, NgStyle],
  templateUrl: './watchlist.html',
})
export class Watchlist implements OnInit {
  private readonly watchlistApi = inject(WatchlistApiService);

  activeTab = signal<Tab>('watch');
  isLoading = signal(true);
  loadError = signal<string | null>(null);
  removeMessage = signal<string | null>(null);

  watch: WatchlistCard[] = [];

  rated: RatedCard[] = [
    { title: 'Inception', meta: '2010 · Sci-Fi', mine: 5, bg: 'linear-gradient(160deg,#2A5388,#0B1220)' },
    { title: 'The Matrix', meta: '1999 · Sci-Fi', mine: 4, bg: 'linear-gradient(160deg,#15553F,#08130F)' },
    { title: 'Parasite', meta: '2019 · Thriller', mine: 5, bg: 'linear-gradient(160deg,#4A5E34,#0E130A)' },
    { title: 'Whiplash', meta: '2014 · Drama', mine: 4, bg: 'linear-gradient(160deg,#7A262C,#130708)' },
    { title: 'Pulp Fiction', meta: '1994 · Crime', mine: 3, bg: 'linear-gradient(160deg,#6E5A1F,#141005)' },
  ];

  ngOnInit(): void {
    this.watchlistApi.getWatchlist(0, 10).subscribe({
      next: (res) => {
        this.watch = res.data.content.map((movie, index) => this.toCard(movie, index));
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(err.error?.message ?? 'Could not load your watchlist from the server.');
      },
    });
  }

  onRemove(movie: WatchlistCard): void {
    this.removeMessage.set(null);

    this.watchlistApi.removeFromWatchlist(movie.id).subscribe({
      next: () => {
        this.watch = this.watch.filter((m) => m.id !== movie.id);
      },
      error: (err) => {
        this.removeMessage.set(err.error?.message ?? 'Could not remove this movie.');
      },
    });
  }

  private toCard(movie: MovieResponse, index: number): WatchlistCard {
    const genreNames = movie.genres.map((g) => g.name).join(', ');

    return {
      id: movie.id,
      title: movie.title,
      meta: genreNames ? `${movie.releaseYear} · ${genreNames}` : `${movie.releaseYear}`,
      bg: movie.posterUrl
        ? `center / cover url('${movie.posterUrl}')`
        : FALLBACK_GRADIENTS[index % FALLBACK_GRADIENTS.length],
    };
  }

  showWatch(): void {
    this.activeTab.set('watch');
  }

  showRatings(): void {
    this.activeTab.set('ratings');
  }

  watchTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'watch';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }

  ratingsTabStyle(): Record<string, string> {
    const active = this.activeTab() === 'ratings';
    return {
      background: active ? '#FF5A5F' : 'transparent',
      color: active ? '#0D1017' : '#C9D0DC',
      'border-color': active ? '#FF5A5F' : '#5B6579',
    };
  }
}
