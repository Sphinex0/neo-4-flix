import { Component, signal } from '@angular/core';
import { NgStyle } from '@angular/common';
import { Navbar } from '../../../../shared/navbar/navbar';

interface WatchlistCard {
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
export class Watchlist {
  activeTab = signal<Tab>('watch');

  watch: WatchlistCard[] = [
    { title: 'Interstellar', meta: '2014 · Sci-Fi', bg: 'linear-gradient(160deg,#6A4423,#120B06)' },
    { title: 'Arrival', meta: '2016 · Sci-Fi', bg: 'linear-gradient(160deg,#454C78,#0C0E18)' },
    { title: 'Spirited Away', meta: '2001 · Animation', bg: 'linear-gradient(160deg,#6E3560,#150A13)' },
    { title: 'The Godfather', meta: '1972 · Crime', bg: 'linear-gradient(160deg,#5A4128,#0E0A06)' },
    { title: 'Blade Runner 2049', meta: '2017 · Sci-Fi', bg: 'linear-gradient(160deg,#256270,#08161A)' },
  ];

  rated: RatedCard[] = [
    { title: 'Inception', meta: '2010 · Sci-Fi', mine: 5, bg: 'linear-gradient(160deg,#2A5388,#0B1220)' },
    { title: 'The Matrix', meta: '1999 · Sci-Fi', mine: 4, bg: 'linear-gradient(160deg,#15553F,#08130F)' },
    { title: 'Parasite', meta: '2019 · Thriller', mine: 5, bg: 'linear-gradient(160deg,#4A5E34,#0E130A)' },
    { title: 'Whiplash', meta: '2014 · Drama', mine: 4, bg: 'linear-gradient(160deg,#7A262C,#130708)' },
    { title: 'Pulp Fiction', meta: '1994 · Crime', mine: 3, bg: 'linear-gradient(160deg,#6E5A1F,#141005)' },
  ];

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
