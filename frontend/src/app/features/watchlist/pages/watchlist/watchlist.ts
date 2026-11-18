import { Component, OnInit, inject, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { Navbar } from '../../../../shared/navbar/navbar';
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

@Component({
  selector: 'app-watchlist',
  imports: [Navbar, NgStyle],
  templateUrl: './watchlist.html',
})
export class Watchlist implements OnInit {
  private readonly watchlistApi = inject(WatchlistApiService);

  activeTab = signal<Tab>('watch');
  loadMessage = signal<string | null>(null);
  removeMessage = signal<string | null>(null);

  watch: WatchlistCard[] = [
    { id: '1', title: 'Interstellar', meta: '2014 · Sci-Fi', bg: 'linear-gradient(160deg,#6A4423,#120B06)' },
    { id: '2', title: 'Arrival', meta: '2016 · Sci-Fi', bg: 'linear-gradient(160deg,#454C78,#0C0E18)' },
    { id: '3', title: 'Spirited Away', meta: '2001 · Animation', bg: 'linear-gradient(160deg,#6E3560,#150A13)' },
    { id: '4', title: 'The Godfather', meta: '1972 · Crime', bg: 'linear-gradient(160deg,#5A4128,#0E0A06)' },
    { id: '5', title: 'Blade Runner 2049', meta: '2017 · Sci-Fi', bg: 'linear-gradient(160deg,#256270,#08161A)' },
  ];

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
        this.loadMessage.set(`Loaded ${res.data.totalElements} movie(s) from your real watchlist on the server.`);
      },
      error: (err) => {
        this.loadMessage.set(err.error?.message ?? 'Could not load your watchlist from the server.');
      },
    });
  }

  onRemove(movie: WatchlistCard): void {
    this.removeMessage.set(null);

    this.watchlistApi.removeFromWatchlist(movie.id).subscribe({
      next: (res) => {
        this.removeMessage.set(res.message);
      },
      error: (err) => {
        this.removeMessage.set(err.error?.message ?? 'Could not remove this movie.');
      },
    });
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
