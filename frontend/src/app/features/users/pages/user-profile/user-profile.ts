import { Component } from '@angular/core';
import { NgStyle } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Navbar } from '../../../../shared/navbar/navbar';

interface SharedMovie {
  title: string;
  mine: number;
  theirs: number;
  bg: string;
}

interface RecentRatedMovie {
  title: string;
  meta: string;
  theirs: number;
  when: string;
  bg: string;
}

@Component({
  selector: 'app-user-profile',
  imports: [Navbar, RouterLink, NgStyle],
  templateUrl: './user-profile.html',
})
export class UserProfile {
  initials = 'SB';
  firstName = 'Sara';
  fullName = 'Sara Benali';
  username = 'sara.b';
  memberSince = 'January 2026';

  ratingsCount = 148;
  averageRating = 3.9;
  bothRatedCount = 34;
  tasteMatchPercent = 82;

  bothLoved: SharedMovie[] = [
    { title: 'Inception', mine: 5, theirs: 5, bg: 'linear-gradient(160deg,#2A5388,#0B1220)' },
    { title: 'Parasite', mine: 5, theirs: 4, bg: 'linear-gradient(160deg,#4A5E34,#0E130A)' },
    { title: 'The Matrix', mine: 4, theirs: 5, bg: 'linear-gradient(160deg,#15553F,#08130F)' },
    { title: 'Whiplash', mine: 4, theirs: 4, bg: 'linear-gradient(160deg,#7A262C,#130708)' },
    { title: 'Interstellar', mine: 5, theirs: 5, bg: 'linear-gradient(160deg,#6A4423,#120B06)' },
    { title: 'Spirited Away', mine: 4, theirs: 5, bg: 'linear-gradient(160deg,#6E3560,#150A13)' },
  ];

  recentRatings: RecentRatedMovie[] = [
    { title: 'Arrival', meta: '2016 · Sci-Fi', theirs: 5, when: '2 days ago', bg: 'linear-gradient(160deg,#454C78,#0C0E18)' },
    { title: 'Blade Runner 2049', meta: '2017 · Sci-Fi', theirs: 4, when: 'last week', bg: 'linear-gradient(160deg,#256270,#08161A)' },
    { title: 'The Godfather', meta: '1972 · Crime', theirs: 5, when: 'last week', bg: 'linear-gradient(160deg,#5A4128,#0E0A06)' },
    { title: 'Pulp Fiction', meta: '1994 · Crime', theirs: 4, when: '2 weeks ago', bg: 'linear-gradient(160deg,#6E5A1F,#141005)' },
  ];
}
