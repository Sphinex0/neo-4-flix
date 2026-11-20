import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserApiService } from '../../services/user-api.service';
import { Profile } from '../profile/profile';
import { UserProfile } from '../user-profile/user-profile';

@Component({
  selector: 'app-profile-gate',
  imports: [Profile, UserProfile],
  templateUrl: './profile-gate.html',
})
export class ProfileGate implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly userApi = inject(UserApiService);

  isOwnProfile = signal<boolean | null>(null);

  ngOnInit(): void {
    const routeUserId = this.route.snapshot.paramMap.get('userId');

    this.userApi.getMe().subscribe({
      next: (res) => {
        this.isOwnProfile.set(res.data.id === routeUserId);
      },
      error: () => {
        this.isOwnProfile.set(false);
      },
    });
  }
}
